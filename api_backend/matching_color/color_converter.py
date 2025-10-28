import colorsys


def hex_to_rgb_float(hex_color):
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) / 255.0 for i in (0, 2, 4))


def hex_to_rgb_int(hex_color):
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))


def rgb_to_hex(rgb_float):
    return '#' + ''.join(f'{int(c*255):02X}' for c in rgb_float)


def hex_to_hsv(hex_color):
    r, g, b = hex_to_rgb_float(hex_color)
    return colorsys.rgb_to_hsv(r, g, b)


def hsv_to_hex(h, s, v):
    r, g, b = colorsys.hsv_to_rgb(h, s, v)
    return rgb_to_hex((r, g, b))
