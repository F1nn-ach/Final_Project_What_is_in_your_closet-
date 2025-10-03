from tools.colorname import find
import unittest


class ColornamesTest(unittest.TestCase):
    def test_find(self):
        colors = [
            ("Red", (255, 0, 0)),
            ("Crimson", (220, 20, 60)),
            ("FireBrick", (178, 34, 34)),
            ("DarkRed", (139, 0, 0)),
            ("Pink", (255, 192, 203)),
            ("DeepPink", (255, 20, 147)),
            ("HotPink", (255, 105, 180)),
            ("PaleVioletRed", (219, 112, 147)),
            ("OrangeRed", (255, 69, 0)),
            ("DarkOrange", (255, 140, 0)),
            ("Tomato", (255, 99, 71)),
            ("Orange", (255, 165, 0)),
            ("Yellow", (255, 255, 0)),
            ("Gold", (255, 215, 0)),
            ("Khaki", (240, 230, 140)),
            ("DarkKhaki", (189, 183, 107)),
            ("Magenta", (255, 0, 255)),
            ("Purple", (128, 0, 128)),
            ("Indigo", (75, 0, 130)),
            ("Violet", (238, 130, 238)),
            ("Green", (0, 128, 0)),
            ("DarkGreen", (0, 100, 0)),
            ("Teal", (0, 128, 128)),
            ("Olive", (128, 128, 0)),
            ("Aqua", (0, 255, 255)),
            ("Blue", (0, 0, 255)),
            ("Navy", (0, 0, 128)),
            ("Turquoise", (64, 224, 208)),
            ("Brown", (165, 42, 42)),
            ("Maroon", (128, 0, 0)),
            ("Goldenrod", (218, 165, 32)),
            ("Tan", (210, 180, 140)),
            ("White", (255, 255, 255)),
            ("Beige", (245, 245, 220)),
            ("MistyRose", (255, 228, 225)),
            ("Azure", (240, 255, 255)),
            ("Black", (0, 0, 0)),
            ("Silver", (192, 192, 192)),
            ("DarkGray", (169, 169, 169)),
            ("Gray", (128, 128, 128)),
        ]

        for name, color in colors:
            with self.subTest(name=name, color=color):
                result = find(color)
                self.assertEqual(result, name)


if __name__ == "__main__":
    unittest.main()
