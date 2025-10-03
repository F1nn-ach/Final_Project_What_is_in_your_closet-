import colornames
import tkinter as tk
from tkinter import ttk
from collections import Counter
from colornames import _colors   

def rgb_to_hex(rgb):
    return "#{:02x}{:02x}{:02x}".format(*rgb)

def classify_all_colors(keywords=None):
    name_map = _colors
    lower_name_map = {k.lower(): v for k, v in name_map.items()}
    results = []

    all_words = []
    for name in name_map.keys():
        all_words.extend(name.split())
    counts = Counter(word.lower() for word in all_words)
    keywords = [k for k, v in counts.items() if v > 1]

    root_colors = sorted([kw for kw in keywords if kw in lower_name_map], key=len, reverse=True)

    root_color_hex_map = {
        color: rgb_to_hex(lower_name_map[color])
        for color in root_colors
    }
    
    root_color_rgb_map = {
        color: lower_name_map[color]
        for color in root_colors
    }

    for name, rgb in name_map.items():
        lower_name = name.lower()
        main_group = None

        for r_color in root_colors:
            if r_color in lower_name:
                main_group = r_color
                break
            
        if not main_group:
            min_dist = float('inf')
            best_match = None
            for r_color, r_rgb in root_color_rgb_map.items():
                dist = sum((a - b) ** 2 for a, b in zip(rgb, r_rgb))
                if dist < min_dist:
                    min_dist = dist
                    best_match = r_color
            main_group = best_match

        if main_group is None:
            continue

        main_hex = root_color_hex_map.get(main_group)

        if main_hex:
            results.append({
                "name": name,
                "main_group": main_group,
                "main_hex": main_hex,
                "sub_group": name,
                "sub_hex": rgb_to_hex(rgb)
            })

    return results

