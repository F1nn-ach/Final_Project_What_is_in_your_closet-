import colorsys
import numpy as np
from colormath.color_objects import LabColor, sRGBColor
from colormath.color_conversions import convert_color
from colormath.color_diff import delta_e_cie2000

# Patch for NumPy 2.0 compatibility
if not hasattr(np, 'asscalar'):
    np.asscalar = lambda x: x.item()

# ---------- Conversion Utilities ----------

def hex_to_rgb_float(hex_color):
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) / 255.0 for i in (0, 2, 4))

def rgb_to_hex(rgb_float):
    return '#' + ''.join(f'{int(c*255):02X}' for c in rgb_float)

def hex_to_hsv(hex_color):
    r, g, b = hex_to_rgb_float(hex_color)
    return colorsys.rgb_to_hsv(r, g, b)

def hsv_to_hex(h, s, v):
    r, g, b = colorsys.hsv_to_rgb(h, s, v)
    return rgb_to_hex((r, g, b))

# ---------- LAB / ΔE2000 Utilities ----------

def rgb_float_to_lab(rgb_float_tuple):
    rgb = sRGBColor(rgb_float_tuple[0], rgb_float_tuple[1], rgb_float_tuple[2])
    lab = convert_color(rgb, LabColor, target_illuminant='d65')
    return lab

def color_distance_deltaE2000(lab1, lab2):
    # colormath >=3.0 return float ตรง ๆ
    return delta_e_cie2000(lab1, lab2)

def is_neutral_lab(hex_color, ab_threshold=5):
    rgb = hex_to_rgb_float(hex_color)
    lab = rgb_float_to_lab(rgb)
    return abs(lab.lab_a) <= ab_threshold and abs(lab.lab_b) <= ab_threshold

# ---------- Theory Colors ----------

def get_theory_colors(main_hex, theories):
    if isinstance(theories, str):
        theories = [theories]

    all_colors = []
    h, s, v = hex_to_hsv(main_hex)
    h_degrees = h * 360

    for theory in theories:
        if theory == "complementary":
            comp_h = (h_degrees + 180) % 360
            all_colors.append(hsv_to_hex(comp_h/360.0, s, v))
        elif theory == "analogous":
            all_colors.append(hsv_to_hex(((h_degrees + 30)%360)/360.0, s, v))
            all_colors.append(hsv_to_hex(((h_degrees - 30)%360)/360.0, s, v))
        elif theory == "triadic":
            all_colors.append(hsv_to_hex(((h_degrees + 120)%360)/360.0, s, v))
            all_colors.append(hsv_to_hex(((h_degrees + 240)%360)/360.0, s, v))

    return list(set(all_colors))  # ลบซ้ำ

# ---------- Matching Logic ----------

def filter_and_match_colors(candidates, theory_colors_hex, good_lucky, bad_lucky, theory_threshold=20, lucky_threshold=20):
    valid = []
    blocked = []

    theory_colors_lab = [rgb_float_to_lab(hex_to_rgb_float(h)) for h in theory_colors_hex]
    bad_lucky_lab = [rgb_float_to_lab(hex_to_rgb_float(h)) for h in bad_lucky]
    good_lucky_lab = [rgb_float_to_lab(hex_to_rgb_float(h)) for h in good_lucky]

    for c in candidates:
        candidate_hex = c['subHex'].upper() 
        candidate_lab = rgb_float_to_lab(hex_to_rgb_float(candidate_hex))

        # Neutral → auto match theory (ยกเว้น unlucky)
        if is_neutral_lab(candidate_hex):
            # Check unlucky
            is_unlucky = False
            for ul_lab in bad_lucky_lab:
                dist = color_distance_deltaE2000(candidate_lab, ul_lab)
                if dist <= lucky_threshold:
                    blocked.append({'id': c['clothId'], 'reason': f'unlucky neutral color (ΔE2000: {dist:.2f})'})
                    is_unlucky = True
                    break
            if is_unlucky:
                continue

            # Check lucky (if lucky colors are specified, neutral must match)
            if good_lucky_lab:
                is_lucky = False
                for gl_lab in good_lucky_lab:
                    dist = color_distance_deltaE2000(candidate_lab, gl_lab)
                    if dist <= lucky_threshold:
                        is_lucky = True
                        break
                if not is_lucky:
                    blocked.append({'id': c['clothId'], 'reason': 'neutral color not a lucky color'})
                    continue

            # Passed all checks
            valid.append(c)
            continue

        # ---- check unlucky ----
        is_unlucky = False
        for ul_lab in bad_lucky_lab:
            dist = color_distance_deltaE2000(candidate_lab, ul_lab)
            if dist <= lucky_threshold:
                blocked.append({'id': c['clothId'], 'reason': f'unlucky color (ΔE2000: {dist:.2f})'})
                is_unlucky = True
                break
        if is_unlucky:
            continue

        # ---- check lucky ----
        if good_lucky_lab:
            is_lucky = False
            for gl_lab in good_lucky_lab:
                dist = color_distance_deltaE2000(candidate_lab, gl_lab)
                if dist <= lucky_threshold:
                    is_lucky = True
                    break
            if not is_lucky:
                blocked.append({'id': c['clothId'], 'reason': 'not a lucky color'})
                continue

        # ---- check theory ----
        is_theory_match = False
        min_theory_distance = float('inf')
        for t_lab in theory_colors_lab:
            dist = color_distance_deltaE2000(candidate_lab, t_lab)
            min_theory_distance = min(min_theory_distance, dist)
            if dist <= theory_threshold:
                is_theory_match = True
                break

        if is_theory_match:
            valid.append(c)
        else:
            blocked.append({'id': c['clothId'], 'reason': f'no theory match (ΔE2000 min: {min_theory_distance:.2f})'})

    return valid, blocked


def match_colors_logic(data):
    main_item = data['main_item']
    theories = data.get('theory', ['neutral'])
    if isinstance(theories, str):
        theories = [theories]

    lucky = data.get('lucky_colors', {})
    has_outer = data.get('has_outer', False)

    good_lucky = [c.upper() for c in lucky.get('good', [])]
    bad_lucky = [c.upper() for c in lucky.get('bad', [])]

    main_item_sub_hex = main_item['subHex'].upper() 
    main_item_type = main_item['clothTypeName']

    target_pants_candidates = []
    target_tops_candidates = []
    target_outer_candidates = []

    if main_item_type in ['เสื้อ', 'เสื้อคลุม']:
        target_pants_candidates = data.get('pants_candidates', [])
    elif main_item_type in ['กางเกง', 'กระโปรง']:
        target_tops_candidates = data.get('tops_candidates', [])
    
    if has_outer:
        target_outer_candidates = data.get('outer_candidates', [])

    theory_colors = get_theory_colors(main_item_sub_hex, theories)

    pants_filtered, pants_blocked = filter_and_match_colors(
        target_pants_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold=20, lucky_threshold=20
    )

    tops_filtered, tops_blocked = filter_and_match_colors(
        target_tops_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold=20, lucky_threshold=20
    )

    outer_filtered, outer_blocked = filter_and_match_colors(
        target_outer_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold=20, lucky_threshold=20
    )

    return {
        "main_item_id": main_item['clothId'],
        "pants_options": pants_filtered,
        "tops_options": tops_filtered,
        "outer_options": outer_filtered,
        "blocked_items": {
            "pants": pants_blocked,
            "tops": tops_blocked,
            "outer": outer_blocked
        }
    }
