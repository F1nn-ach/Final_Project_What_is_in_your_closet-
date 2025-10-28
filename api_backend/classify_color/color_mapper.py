from .tools.colorname2 import _find_hex


class ColorMapper:
    def rgb_to_hex(self, r, g, b):
        return "#{:02x}{:02x}{:02x}".format(int(r), int(g), int(b))

    def hex_to_color_name(self, hex_code):
        return _find_hex(hex_code)

    def color_to_result(self, color_dict):
        r, g, b = [int(v) for v in color_dict["rgb"]]
        hex_code = self.rgb_to_hex(r, g, b)
        color_name = self.hex_to_color_name(hex_code)

        return {
            "hex": hex_code,
            "color_name": color_name,
            "percentage": round(color_dict["percentage"], 1),
        }
