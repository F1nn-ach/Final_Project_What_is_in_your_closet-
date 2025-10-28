from .image_processor import ImageProcessor
from .color_extractor import ColorExtractor
from .color_merger import ColorMerger
from .color_mapper import ColorMapper


class ShirtColorClassifier:

    def __init__(self, image_size=(400, 400), max_clusters=8, min_pixel_threshold=50):

        self.image_processor = ImageProcessor(image_size, min_pixel_threshold)
        self.color_extractor = ColorExtractor(max_clusters)
        self.color_merger = ColorMerger(rgb_distance_threshold=50)
        self.color_mapper = ColorMapper()

    def extract_palette(self, image_path_or_url):
        rgb, alpha = self.image_processor.load_image(image_path_or_url)
        if rgb is None:
            return None

        pixels = self.image_processor.extract_pixels(rgb, alpha)
        if len(pixels) == 0:
            return None
        grouped_colors = self.color_extractor.get_dominant_colors(pixels)

        merged_colors = self.color_merger.merge_similar_colors(grouped_colors)

        results = []
        for color in merged_colors:
            result = self.color_mapper.color_to_result(color)
            results.append(result)
            print(result["hex"])  

        return results


def extract_color_palette(input_path_or_url):
    classifier = ShirtColorClassifier()
    return classifier.extract_palette(input_path_or_url)
