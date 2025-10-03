#!/usr/bin/env python3

import functools
import json
import os
import numpy as np

# Fix for numpy compatibility with colormath
if not hasattr(np, 'asscalar'):
    np.asscalar = lambda x: x.item()

from colormath.color_objects import sRGBColor, LabColor
from colormath.color_conversions import convert_color
from colormath.color_diff import delta_e_cie2000

# ----------------- OCTREE -----------------
def octree(depth):
    return [octree(depth-1) for _ in range(8)] if depth else None

def octree_index(r, g, b, d):
    """Return 0-7 index at depth d"""
    return ((r >> d & 1) << 2) | ((g >> d & 1) << 1) | (b >> d & 1)

def shrink(node):
    MULTI = 'MULTI'
    if type(node) is not list:
        return node
    res = None
    for i, child in enumerate(node):
        val = shrink(child)
        if val == MULTI:
            res = MULTI
        else:
            node[i] = val
            if val is not None:
                res = val if res is None else MULTI
    return res

def tree2dict(tree):
    d = {}
    for i, child in enumerate(tree):
        if type(child) is list:
            d[i] = tree2dict(child)
        elif child is not None:
            d[i] = child
    return d

# ----------------- LOAD COLORS -----------------
script_dir = os.path.dirname(os.path.abspath(__file__))
colors_json_path = os.path.join(script_dir, 'colors.json')
with open(colors_json_path, 'r') as f:
    colors = json.load(f)  # expects dict: {"Red": [255,0,0], ...}

# ----------------- BUILD OCTREE -----------------
tree = octree(6)  # depth = 6 for 24-bit colors
for name, rgb in colors.items():
    i,j,k,l,m,n = [octree_index(*rgb, d) for d in reversed(range(6))]
    if tree[i][j][k][l][m][n] is None:
        tree[i][j][k][l][m][n] = []
    tree[i][j][k][l][m][n].append(name)

shrink(tree)
tree = tree2dict(tree)

# ----------------- FIND FUNCTION -----------------
@functools.singledispatch
def find(r, g, b):
    if type(r) is not int or type(g) is not int or type(b) is not int:
        raise TypeError("R, G and B values must be int")
    if not (0 <= r < 256 and 0 <= g < 256 and 0 <= b < 256):
        raise ValueError("Invalid color value: must be 0 <= x < 256")
    return _search(tree, r, g, b)

@find.register(str)
def _find_hex(color):
    if color[0] == '#':
        color = color[1:]
    if len(color) == 3:
        return find(*[int(c*2,16) for c in color])
    if len(color) == 6:
        return find(*[int(color[i:i+2],16) for i in (0,2,4)])
    raise ValueError("Malformed hexadecimal color")

@find.register(tuple)
def _find_tuple(color):
    if len(color) != 3:
        raise ValueError("Color tuple must be size 3 (r,g,b)")
    return find(*color)

# ----------------- SEARCH FUNCTIONS -----------------
def _search(tree, r, g, b, d=5):
    """Recursive octree search"""
    if d < 0:
        # leaf node reached
        if isinstance(tree, str):
            return tree
        elif isinstance(tree, list):
            return tree[0]  # return first if multiple
        else:
            return _approximate(tree, r, g, b)
    i = octree_index(r, g, b, d)
    if i not in tree:
        return _approximate(tree, r, g, b)
    node = tree[i]
    if isinstance(node, str):
        return node
    elif isinstance(node, list):
        return node[0]
    else:
        return _search(node, r, g, b, d-1)

def _approximate(tree, r, g, b):
    """Use Delta E to find nearest color"""
    target_rgb = sRGBColor(r/255, g/255, b/255)
    target_lab = convert_color(target_rgb, LabColor)

    def _distance(colorname):
        x, y, z = colors[colorname]
        lab = convert_color(sRGBColor(x/255, y/255, z/255), LabColor)
        return delta_e_cie2000(target_lab, lab)

    return min(_descendants(tree), key=_distance)

def _descendants(tree):
    """Yield all color names in a tree"""
    if isinstance(tree, str):
        yield tree
    elif isinstance(tree, list):
        for item in tree:
            yield item
    elif isinstance(tree, dict):
        for child in tree.values():
            yield from _descendants(child)

# ----------------- TEST -----------------
if __name__ == "__main__":
    test_colors = [(255,0,0), (0,128,0), (0,0,255), (245,245,220)]
    for c in test_colors:
        name = find(c)
        print(c, "->", name)
