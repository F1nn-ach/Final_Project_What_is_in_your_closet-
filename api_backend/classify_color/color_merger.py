import numpy as np


class ColorMerger:
    def __init__(self, rgb_distance_threshold=50):
        self.rgb_distance_threshold = rgb_distance_threshold

    def rgb_distance(self, color1, color2):
        return np.sqrt(np.sum((np.array(color1) - np.array(color2)) ** 2))

    def merge_similar_colors(self, grouped_colors):
        if len(grouped_colors) <= 1:
            return grouped_colors

        grouped_colors.sort(key=lambda x: x["percentage"], reverse=True)

        merged = []
        used = [False] * len(grouped_colors)

        for i, color1 in enumerate(grouped_colors):
            if used[i]:
                continue

            group = {"rgb": color1["rgb"], "percentage": color1["percentage"]}
            used[i] = True

            for j, color2 in enumerate(grouped_colors):
                if used[j] or i == j:
                    continue

                distance = self.rgb_distance(color1["rgb"], color2["rgb"])

                if distance <= self.rgb_distance_threshold:
                    group["percentage"] += color2["percentage"]
                    used[j] = True

            merged.append(group)
        merged.sort(key=lambda x: x["percentage"], reverse=True)

        return merged
