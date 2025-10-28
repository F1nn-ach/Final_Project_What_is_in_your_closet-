from .color_distance import delta_e_distance, is_neutral_color


def filter_and_match_colors(
    candidates,
    theory_colors_hex,
    good_lucky,
    bad_lucky,
    theory_threshold=25,
    lucky_threshold=25
):
    valid = []
    blocked = []

    for c in candidates:
        candidate_hex = c['colorHex'].upper()

        is_unlucky = False
        for ul_hex in bad_lucky:
            delta_e = delta_e_distance(candidate_hex, ul_hex)
            if delta_e <= lucky_threshold:
                blocked.append({
                    'id': c['clothId'],
                    'reason': f'unlucky color (ΔE: {delta_e:.1f})'
                })
                is_unlucky = True
                break

        if is_unlucky:
            continue

        # เช็คว่าเป็นสี neutral หรือไม่
        is_neutral = is_neutral_color(candidate_hex)

        # ถ้าเป็นสี neutral
        if is_neutral:
            # ถ้าผู้ใช้เลือกสีมงคล → ต้องเช็คว่าสี neutral นี้เป็นสีมงคลด้วยหรือไม่
            if good_lucky:
                is_lucky = False
                for gl_hex in good_lucky:
                    delta_e = delta_e_distance(candidate_hex, gl_hex)
                    if delta_e <= lucky_threshold:
                        is_lucky = True
                        break

                if is_lucky:
                    # ผ่าน: สี neutral + เป็นสีมงคล
                    valid.append(c)
                else:
                    # บล็อค: สี neutral แต่ไม่ใช่สีมงคล
                    blocked.append({
                        'id': c['clothId'],
                        'reason': 'neutral color but not lucky color'
                    })
            else:
                # ผู้ใช้ไม่ได้เลือกสีมงคล → ผ่านเลย (neutral จับคู่ได้กับทุกสี)
                valid.append(c)
            continue

        # ถ้า theory_colors_hex เป็น list ว่าง แสดงว่าสีหลักเป็น neutral
        # → ให้จับคู่ได้กับทุกสี (ไม่ต้องเช็คทฤษฎี)
        if not theory_colors_hex:
            # สีหลักเป็น neutral → จับคู่ได้กับทุกสี
            if good_lucky:
                # แต่ถ้าผู้ใช้เลือกสีมงคล → ต้องเช็คว่า candidate เป็นสีมงคลด้วย
                is_lucky = False
                for gl_hex in good_lucky:
                    delta_e = delta_e_distance(candidate_hex, gl_hex)
                    if delta_e <= lucky_threshold:
                        is_lucky = True
                        break

                if is_lucky:
                    # ผ่าน: เป็นสีมงคล
                    valid.append(c)
                else:
                    # บล็อค: ไม่ใช่สีมงคล
                    blocked.append({
                        'id': c['clothId'],
                        'reason': 'not a lucky color'
                    })
            else:
                # ไม่ได้เลือกสีมงคล → ผ่านเลย (จับคู่ได้กับทุกสี)
                valid.append(c)
            continue

        # สีหลักไม่ใช่ neutral → เช็คตามทฤษฎี
        is_theory_match = False
        min_theory_delta_e = float('inf')

        for t_hex in theory_colors_hex:
            delta_e = delta_e_distance(candidate_hex, t_hex)
            min_theory_delta_e = min(min_theory_delta_e, delta_e)
            if delta_e <= theory_threshold:
                is_theory_match = True
                break

        # ตรวจสอบสีมงคล (เฉพาะเมื่อจับคู่ตามทฤษฎีได้แล้ว)
        if is_theory_match:
            # ถ้าจับคู่ตามทฤษฎีได้แล้ว
            # เช็คว่าผู้ใช้เลือกสีมงคลหรือไม่
            if good_lucky:
                # ผู้ใช้เลือกสีมงคล → ต้องเช็คว่า candidate เป็นสีมงคลด้วย
                is_lucky = False
                for gl_hex in good_lucky:
                    delta_e = delta_e_distance(candidate_hex, gl_hex)
                    if delta_e <= lucky_threshold:
                        is_lucky = True
                        break

                if is_lucky:
                    # ผ่าน: จับคู่ตามทฤษฎีได้ + เป็นสีมงคล
                    valid.append(c)
                else:
                    # บล็อค: จับคู่ตามทฤษฎีได้ แต่ไม่ใช่สีมงคล
                    blocked.append({
                        'id': c['clothId'],
                        'reason': 'theory match but not lucky color'
                    })
            else:
                # ผู้ใช้ไม่ได้เลือกสีมงคล → ผ่านเลย (จับคู่ตามทฤษฎีเท่านั้น)
                valid.append(c)
        else:
            # ถ้าจับคู่ตามทฤษฎีไม่ได้ → blocked (แม้จะเป็นสีมงคลก็ตาม)
            blocked.append({
                'id': c['clothId'],
                'reason': f'no theory match (ΔE: {min_theory_delta_e:.1f})'
            })

    return valid, blocked


def match_colors_by_item_type(
    main_item,
    theories,
    lucky,
    pants_candidates,
    tops_candidates,
    outer_candidates,
    has_outer=False
):
    from .color_theory import get_theory_colors

    good_lucky = [c.upper() for c in lucky.get('good', [])]
    bad_lucky = [c.upper() for c in lucky.get('bad', [])]

    color_hex = main_item['colorHex'].upper()
    main_item_type = main_item['clothTypeName']

    target_pants_candidates = []
    target_tops_candidates = []
    target_outer_candidates = []

    if main_item_type in ['เสื้อ', 'เสื้อคลุม']:
        target_pants_candidates = pants_candidates
    elif main_item_type in ['กางเกง', 'กระโปรง']:
        target_tops_candidates = tops_candidates

    if has_outer:
        target_outer_candidates = outer_candidates

    theory_colors = get_theory_colors(color_hex, theories)

    pants_filtered, pants_blocked = filter_and_match_colors(
        target_pants_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold=25, lucky_threshold=25
    )

    tops_filtered, tops_blocked = filter_and_match_colors(
        target_tops_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold=25, lucky_threshold=25
    )

    outer_filtered, outer_blocked = filter_and_match_colors(
        target_outer_candidates, theory_colors, good_lucky, bad_lucky,
        theory_threshold=25, lucky_threshold=25
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
