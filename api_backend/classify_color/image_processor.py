import cv2
import numpy as np
import requests


class ImageProcessor:

    def __init__(self, image_size=(400, 400), min_pixel_threshold=50):
        self.image_size = image_size
        self.min_pixel_threshold = min_pixel_threshold

    def load_image(self, image_path):
        try:
            if image_path.startswith(("http://", "https://")):
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

        except Exception as e:
            print(f"Error loading image: {e}")
            return None, None

    def extract_pixels(self, rgb, alpha):
        rgb_blurred = cv2.GaussianBlur(rgb, (5, 5), 0)
        rgb_resized = cv2.resize(rgb_blurred, self.image_size, interpolation=cv2.INTER_AREA)
        alpha_resized = cv2.resize(alpha, self.image_size, interpolation=cv2.INTER_AREA)
        mask = alpha_resized > 128

        rgb_masked = rgb_resized[mask]

        if len(rgb_masked) < self.min_pixel_threshold:
            return np.array([])

        return rgb_masked
