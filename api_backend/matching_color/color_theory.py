from .color_converter import hex_to_hsv, hsv_to_hex
from .color_distance import is_neutral_color


def get_complementary_colors(main_hex, s=None, v=None):
    h, original_s, original_v = hex_to_hsv(main_hex)
    h_degrees = h * 360

    target_s = s if s is not None else original_s
    target_v = v if v is not None else original_v

    comp_h = (h_degrees + 180) % 360
    return [hsv_to_hex(comp_h/360.0, target_s, target_v)]


def get_analogous_colors(main_hex, s=None, v=None, angle=30):
    h, original_s, original_v = hex_to_hsv(main_hex)
    h_degrees = h * 360

    target_s = s if s is not None else original_s
    target_v = v if v is not None else original_v

    color1 = hsv_to_hex(((h_degrees + angle) % 360) / 360.0, target_s, target_v)
    color2 = hsv_to_hex(((h_degrees - angle) % 360) / 360.0, target_s, target_v)

    return [color1, color2]


def get_triadic_colors(main_hex, s=None, v=None):
    h, original_s, original_v = hex_to_hsv(main_hex)
    h_degrees = h * 360

    target_s = s if s is not None else original_s
    target_v = v if v is not None else original_v

    color1 = hsv_to_hex(((h_degrees + 120) % 360) / 360.0, target_s, target_v)
    color2 = hsv_to_hex(((h_degrees + 240) % 360) / 360.0, target_s, target_v)

    return [color1, color2]


def get_theory_colors(main_hex, theories):
    if isinstance(theories, str):
        theories = [theories]

    # เช็คว่าเป็นสี neutral หรือไม่
    if is_neutral_color(main_hex):
        # ถ้าเป็นสี neutral ให้คืนค่าว่าง เพราะจับคู่ได้กับทุกสี
        return []

    # ถ้าไม่ใช่สี neutral ให้ใช้ทฤษฎีสีตามปกติ
    all_colors = []
    h, s, v = hex_to_hsv(main_hex)

    target_s = max(s, 0.5)
    target_v = max(min(v, 0.85), 0.4)

    for theory in theories:
        if theory == "complementary":
            all_colors.extend(get_complementary_colors(main_hex, target_s, target_v))
        elif theory == "analogous":
            all_colors.extend(get_analogous_colors(main_hex, target_s, target_v))
        elif theory == "triadic":
            all_colors.extend(get_triadic_colors(main_hex, target_s, target_v))

    return list(set(all_colors))
