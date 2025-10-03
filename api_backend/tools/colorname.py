#!/usr/bin/env python3


import functools
import json
import os
import pprint
from colormath.color_diff import delta_e_cie2000


def octree(depth):
    return [octree(depth-1) for i in range(8)] if depth else None

def octree_index(r, g, b, d):
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

script_dir = os.path.dirname(os.path.abspath(__file__))
colors_json_path = os.path.join(script_dir, 'colors.json')
with open(colors_json_path, 'r') as f:
    colors = json.load(f)
tree = octree(6)
for name, rgb in colors.items():
    i,j,k,l,m,n= [octree_index(*rgb, d) for d in reversed(range(6))]
    tree[i][j][k][l][m][n]= name
    #,l,m,n,o,p 
    #[l][m][n][o][p] 
shrink(tree)
tree = tree2dict(tree)

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
        return find(*[int(c*2, 16) for c in color])
    if len(color) == 6:
        return find(*[int(color[i:i+2], 16) for i in (0, 2, 4)])
    raise ValueError("Malformed hexadecimal color representation")

@find.register(tuple)
def _find_tuple(color):
    if len(color) != 3:
        raise ValueError("Malformed color tuple: must be of size 3 (r, g, b)")
    return find(*color)

def _search(tree, r, g, b, d=7):
    i = octree_index(r, g, b, d)
    if i not in tree:
        return _approximate(tree, r, g, b)
    return tree[i] if type(tree[i]) is str else _search(tree[i], r, g, b, d-1)

def _approximate(tree, r, g, b):
    def _distance(colorname):
        x, y, z = colors[colorname]
        return (r - x)**2 + (g - y)**2 + (b - z)**2
    return min(_descendants(tree), key=_distance)

def _descendants(tree):
    for i, child in tree.items():
        if type(child) is str:
            yield child
        else:
            yield from _descendants(child)