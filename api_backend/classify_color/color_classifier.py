import cv2
import numpy as np
import requests
from sklearn.cluster import KMeans
from collections import defaultdict
from classify_color.color_group1 import classify_all_colors
from colormath.color_objects import LabColor, sRGBColor
from colormath.color_conversions import convert_color
from colormath.color_diff import delta_e_cie2000

def hex_to_rgb(hex_str):
    hex_str = hex_str.lstrip('#')
    return tuple(int(hex_str[i:i+2], 16) for i in (0, 2, 4))

class ShirtColorClassifierRGB:
    def __init__(self, image_size=(400,400), max_clusters=12, max_colors=10, min_pixel_threshold=50):
        self.image_size = image_size
        self.max_clusters = max_clusters
        self.max_colors = max_colors
        self.min_pixel_threshold = min_pixel_threshold

        color_dataset_rgb = classify_all_colors()
        
        self.color_dataset_lab = [] 
        for color_data in color_dataset_rgb:
            rgb = hex_to_rgb(color_data["sub_hex"])
            rgb_pixel = np.array([[rgb]], dtype=np.uint8)
            lab_pixel = cv2.cvtColor(rgb_pixel, cv2.COLOR_RGB2LAB)
            
            new_data = color_data.copy()
            new_data['lab'] = lab_pixel[0][0]
            self.color_dataset_lab.append(new_data)
    
    def _load_image(self, image_path):
        try:
            if image_path.startswith(("http://","https://")):
                response = requests.get(image_path, timeout=10)
                response.raise_for_status()
                img_array = np.frombuffer(response.content, np.uint8)
                image = cv2.imdecode(img_array, cv2.IMREAD_UNCHANGED)
            else:
                image = cv2.imread(image_path, cv2.IMREAD_UNCHANGED)
            
            if image is None:
                return None, None
            
            alpha = image[:, :, 3] if image.shape[2] == 4 else np.full(image.shape[:2], 255, dtype=np.uint8)
            rgb = cv2.cvtColor(image[:, :, :3], cv2.COLOR_BGR2RGB)
            return rgb, alpha
        except Exception:
            return None, None

    def _extract_pixels(self, rgb, alpha):
        # Apply Gaussian blur to smooth shadows and highlights
        rgb_blurred = cv2.GaussianBlur(rgb, (5, 5), 0)

        rgb_resized = cv2.resize(rgb_blurred, self.image_size, interpolation=cv2.INTER_AREA)
        alpha_resized = cv2.resize(alpha, self.image_size, interpolation=cv2.INTER_AREA)
        mask = alpha_resized > 128
        rgb_masked = rgb_resized[mask]

        if len(rgb_masked) < self.min_pixel_threshold:
            return np.array([])

        rgb_filtered = cv2.medianBlur(rgb_masked.reshape(-1, 1, 3).astype(np.uint8), 3).reshape(-1, 3)
        return rgb_filtered

    def _get_dominant_colors(self, pixels):
        if len(pixels) == 0:
            return np.array([]), np.array([])
            
        max_k = min(self.max_clusters, len(pixels) // 25, 12)
        if max_k < 2:
            k = 1
        else:
            kmeans_pre = KMeans(n_clusters=max_k, random_state=42, n_init="auto").fit(pixels)
            k = kmeans_pre.n_clusters

        kmeans = KMeans(n_clusters=k, random_state=42, n_init="auto").fit(pixels)
        colors = kmeans.cluster_centers_
        percentages = (np.unique(kmeans.labels_, return_counts=True)[1] / len(pixels)) * 100
        return colors, percentages

    def _rgb_to_lab(self, rgb):
        rgb_pixel = np.array([[rgb]], dtype=np.uint8)
        lab_pixel = cv2.cvtColor(rgb_pixel, cv2.COLOR_RGB2LAB)
        return lab_pixel[0][0]

    def _is_shadow_or_highlight(self, color1_lab, color2_lab, l_threshold=20, ab_threshold=15):
        """
        Check if color2 is a shadow/highlight of color1
        - Shadows/highlights have similar a,b (hue) but different L (brightness)
        """
        l1, a1, b1 = color1_lab.astype(float)
        l2, a2, b2 = color2_lab.astype(float)

        # Check if a,b values are similar (same hue)
        ab_diff = np.sqrt((a1 - a2)**2 + (b1 - b2)**2)

        # Check if L values are different (different brightness)
        l_diff = abs(l1 - l2)

        # It's a shadow/highlight if hue is similar but brightness differs
        return ab_diff <= ab_threshold and l_diff <= l_threshold

    def _find_similar_colors(self, grouped_colors, delta_e_threshold=10):
        """Merge similar colors based on LAB color distance (ΔE) and shadow detection"""
        if len(grouped_colors) <= 1:
            return grouped_colors

        # Convert all colors to LAB
        colors_with_lab = []
        for c in grouped_colors:
            r, g, b = c["rgb"]
            lab = self._rgb_to_lab([int(r), int(g), int(b)])
            lab_color = LabColor(lab[0], lab[1], lab[2])
            colors_with_lab.append({
                "rgb": c["rgb"],
                "lab": lab,
                "lab_color": lab_color,
                "percentage": c["percentage"]
            })

        # Sort by percentage (dominant first)
        colors_with_lab.sort(key=lambda x: x["percentage"], reverse=True)

        merged = []
        used = [False] * len(colors_with_lab)

        for i, color1 in enumerate(colors_with_lab):
            if used[i]:
                continue

            # Start a new group with this color
            group = {
                "rgb": color1["rgb"],
                "percentage": color1["percentage"]
            }
            used[i] = True

            # Find similar colors and merge them
            for j, color2 in enumerate(colors_with_lab):
                if used[j] or i == j:
                    continue

                # Calculate ΔE using colormath
                delta_e = delta_e_cie2000(color1["lab_color"], color2["lab_color"])

                # Check if it's a shadow/highlight
                is_shadow = self._is_shadow_or_highlight(color1["lab"], color2["lab"])

                if delta_e <= delta_e_threshold or is_shadow:
                    group["percentage"] += color2["percentage"]
                    used[j] = True

            merged.append(group)

        # Sort by percentage again after merging
        merged.sort(key=lambda x: x["percentage"], reverse=True)
        return merged

    def _map_colors_with_dataset(self, grouped_colors):
        mapped = []
        for c in grouped_colors:
            r, g, b = c["rgb"]
            hex_str = "#{:02x}{:02x}{:02x}".format(int(r), int(g), int(b))

            image_color_lab = self._rgb_to_lab([int(r), int(g), int(b)])
            image_lab_color = LabColor(image_color_lab[0], image_color_lab[1], image_color_lab[2])

            best_match = min(
                self.color_dataset_lab,
                key=lambda d: delta_e_cie2000(LabColor(d['lab'][0], d['lab'][1], d['lab'][2]), image_lab_color)
            )

            mapped.append({
                "hex": hex_str,
                "color_name": best_match["main_group"],
                "percentage": round(c["percentage"], 1)
            })
        return mapped
    
    def _merge_colors_by_name(self, colors):
        merged = {}
        for c in colors:
            name = c["color_name"]
            if name not in merged:
                merged[name] = c.copy() 
            else:
                merged[name]["percentage"] += c["percentage"]
        
        merged_list = sorted(merged.values(), key=lambda x: x["percentage"], reverse=True)
        return merged_list


    def classify_colors(self, image_path):
        rgb, alpha = self._load_image(image_path)
        if rgb is None:
            return None
            
        pixels = self._extract_pixels(rgb, alpha)
        if len(pixels) == 0:
            return None
            
        dominant_colors, percentages = self._get_dominant_colors(pixels)
        grouped_colors = [{"rgb": dominant_colors[i], "percentage": percentages[i]} for i in range(len(dominant_colors))]

        # Merge similar colors (e.g., shadows/highlights of the same color)
        grouped_colors = self._find_similar_colors(grouped_colors, delta_e_threshold=30)

        mapped_colors = self._map_colors_with_dataset(grouped_colors)
        if not mapped_colors:
            return None

        merged_colors = self._merge_colors_by_name(mapped_colors)
        if not merged_colors:
            return None

        main_color = merged_colors[0]

        r, g, b = tuple(int(v, 16) for v in (main_color["hex"][1:3], main_color["hex"][3:5], main_color["hex"][5:7]))
        image_color_lab = self._rgb_to_lab([r, g, b])
        image_lab_color = LabColor(image_color_lab[0], image_color_lab[1], image_color_lab[2])
        best_match = min(
            self.color_dataset_lab,
            key=lambda d: delta_e_cie2000(LabColor(d['lab'][0], d['lab'][1], d['lab'][2]), image_lab_color)
        )

        return {
            "main_group_color": {
                "main_group_name": best_match["main_group"],
                "main_hex": best_match["main_hex"]
            },
            "sub_group_color": {
                "sub_group_name": best_match["sub_group"],
                "sub_hex": best_match["sub_hex"]
            },
            "colors": merged_colors
        }


def extract_color_palette(input_path_or_url):
    classifier = ShirtColorClassifierRGB()
    return classifier.classify_colors(input_path_or_url)
