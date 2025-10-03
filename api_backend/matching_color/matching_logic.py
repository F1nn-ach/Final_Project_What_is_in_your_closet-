import colorsys
import math
import cv2
import numpy as np

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

def rgb_float_to_lab(rgb_float_tuple):
    rgb_uint8 = np.array([[tuple(int(c * 255) for c in rgb_float_tuple)]], dtype=np.uint8)
    lab_pixel = cv2.cvtColor(rgb_uint8, cv2.COLOR_RGB2LAB)
    return lab_pixel[0][0] 

def color_distance_lab(lab1, lab2):
    return np.sqrt(np.sum((lab1.astype(float) - lab2.astype(float)) ** 2))

def get_theory_colors(main_hex, theory):
    h, s, v = hex_to_hsv(main_hex)
    h_degrees = h * 360
    colors = []

    if theory == "complementary":
        # Opposite 180°
        comp_h = (h_degrees + 180) % 360
        colors.append(hsv_to_hex(comp_h/360.0, s, v))
    elif theory == "analogous":
        # Adjacent ±30°
        colors.append(hsv_to_hex(((h_degrees + 30)%360)/360.0, s, v))
        colors.append(hsv_to_hex(((h_degrees - 30)%360)/360.0, s, v))
    elif theory == "triadic":
        # Separated by 120°
        colors.append(hsv_to_hex(((h_degrees + 120)%360)/360.0, s, v))
        colors.append(hsv_to_hex(((h_degrees + 240)%360)/360.0, s, v))
    
    return colors

def filter_and_match_colors(candidates, theory_colors_hex, good_lucky, bad_lucky, theory_threshold_lab=30, lucky_threshold_lab=30):
    valid = []
    blocked = []
    
    theory_colors_lab = [rgb_float_to_lab(hex_to_rgb_float(h)) for h in theory_colors_hex]
    
    bad_lucky_lab = [rgb_float_to_lab(hex_to_rgb_float(h)) for h in bad_lucky]
    good_lucky_lab = [rgb_float_to_lab(hex_to_rgb_float(h)) for h in good_lucky]

    for c in candidates:
        candidate_hex = c['subHex'].upper() 
        
        candidate_lab = rgb_float_to_lab(hex_to_rgb_float(candidate_hex))

        is_unlucky = False
        for ul_lab in bad_lucky_lab:
            dist = color_distance_lab(candidate_lab, ul_lab)
            if dist <= lucky_threshold_lab:
                blocked.append({'id': c['clothId'], 'reason': f'unlucky color (distance: {dist:.2f})'})
                is_unlucky = True
                break
        if is_unlucky:
            continue

        if good_lucky_lab:
            is_lucky = False
            for gl_lab in good_lucky_lab:
                dist = color_distance_lab(candidate_lab, gl_lab)
                if dist <= lucky_threshold_lab:
                    is_lucky = True
                    break
            if not is_lucky:
                blocked.append({'id': c['clothId'], 'reason': 'not a lucky color'})
                continue

        is_theory_match = False
        min_theory_distance = float('inf')
        for t_lab in theory_colors_lab:
            dist = color_distance_lab(candidate_lab, t_lab)
            min_theory_distance = min(min_theory_distance, dist)
            if dist <= theory_threshold_lab:
                is_theory_match = True
                break

        if is_theory_match:
            valid.append(c)
        else:
            blocked.append({'id': c['clothId'], 'reason': f'no theory match (min distance: {min_theory_distance:.2f})'})

    return valid, blocked

def match_colors_logic(data):
    main_item = data['main_item']
    theory = data.get('theory', 'neutral')
    lucky = data.get('lucky_colors', {})
    has_outer = data.get('has_outer', False)

    good_lucky = [c.upper() for c in lucky.get('good', [])]
    bad_lucky = [c.upper() for c in lucky.get('bad', [])]

    main_item_sub_hex = main_item['subHex'].upper() 
    main_item_type = main_item['clothTypeName']

    target_pants_candidates = []
    target_tops_candidates = []
    target_outer_candidates = []

    if main_item_type == 'เสื้อ' or main_item_type == 'เสื้อคลุม':
        target_pants_candidates = data.get('pants_candidates', [])
    elif main_item_type == 'กางเกง' or main_item_type == 'กระโปรง':
        target_tops_candidates = data.get('tops_candidates', [])
    
    if has_outer:
        target_outer_candidates = data.get('outer_candidates', [])

    theory_colors = get_theory_colors(main_item_sub_hex, theory)

    pants_filtered, pants_blocked = filter_and_match_colors(
        target_pants_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold_lab=30, lucky_threshold_lab=30
    )

    tops_filtered, tops_blocked = filter_and_match_colors(
        target_tops_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold_lab=30, lucky_threshold_lab=30
    )

    outer_filtered, outer_blocked = filter_and_match_colors(
        target_outer_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold_lab=30, lucky_threshold_lab=30
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

