from .color_filter import match_colors_by_item_type


def match_colors_logic(data):
    main_item = data['main_item']
    theories = data.get('theory', [])

    if isinstance(theories, str):
        theories = [theories] if theories else []

    if not theories:
        return {
            "main_item_id": main_item['clothId'],
            "pants_options": [],
            "tops_options": [],
            "outer_options": [],
            "blocked_items": {
                "pants": [],
                "tops": [],
                "outer": []
            }
        }

    lucky = data.get('lucky_colors', {})
    has_outer = data.get('has_outer', False)

    pants_candidates = data.get('pants_candidates', [])
    tops_candidates = data.get('tops_candidates', [])
    outer_candidates = data.get('outer_candidates', [])

    return match_colors_by_item_type(
        main_item=main_item,
        theories=theories,
        lucky=lucky,
        pants_candidates=pants_candidates,
        tops_candidates=tops_candidates,
        outer_candidates=outer_candidates,
        has_outer=has_outer
    )
