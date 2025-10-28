import numpy as np
from colormath.color_objects import sRGBColor, LabColor
from colormath.color_conversions import convert_color
from colormath.color_diff import delta_e_cie2000

from .color_converter import hex_to_rgb_int, hex_to_hsv


if not hasattr(np, 'asscalar'):
    np.asscalar = lambda a: a.item()


def rgb_distance(rgb1, rgb2):
    return np.sqrt(np.sum((np.array(rgb1) - np.array(rgb2))**2))


def delta_e_distance(hex1, hex2):
    r1, g1, b1 = hex_to_rgb_int(hex1)
    r2, g2, b2 = hex_to_rgb_int(hex2)

    color1_rgb = sRGBColor(r1/255.0, g1/255.0, b1/255.0)
    color2_rgb = sRGBColor(r2/255.0, g2/255.0, b2/255.0)

    color1_lab = convert_color(color1_rgb, LabColor)
    color2_lab = convert_color(color2_rgb, LabColor)

    return delta_e_cie2000(color1_lab, color2_lab)


def is_neutral_color(hex_color, saturation_threshold=0.1, value_threshold_min=0.2, value_threshold_max=0.9):
    h, s, v = hex_to_hsv(hex_color)
    has_low_saturation = s < saturation_threshold

    if not has_low_saturation:
        return False

    return True
