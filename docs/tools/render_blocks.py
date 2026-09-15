import json
import os
import sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
WIKI = os.path.dirname(HERE)
TEX = os.path.join(WIKI, "assets", "tex")
OUT = os.path.join(WIKI, "assets", "render")

S = 4
CANVAS = 32 * S

TOP_LIGHT = 1.0
LEFT_LIGHT = 0.82
RIGHT_LIGHT = 0.6


def load(path):
    img = Image.open(os.path.join(TEX, path + ".png")).convert("RGBA")
    if img.height != img.width:
        img = img.crop((0, 0, img.width, img.width))
    return img


def face_patch(path, uv, rotate=0):
    img = load(path)
    k = img.width / 16
    patch = img.crop((round(uv[0] * k), round(uv[1] * k), round(uv[2] * k), round(uv[3] * k)))
    if rotate:
        patch = patch.rotate(rotate, expand=True)
    return patch.resize((patch.width * max(1, S), patch.height * max(1, S)), Image.NEAREST)


def shade(img, factor):
    r, g, b, a = img.split()
    lut = [min(255, int(v * factor)) for v in range(256)]
    return Image.merge("RGBA", (r.point(lut), g.point(lut), b.point(lut), a))


def render_box(a, b, c, top, left, right):
    a *= S
    b *= S
    c *= S
    w = a + b
    h = (a + b) // 2 + c
    canvas = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    faces = (
        (top, TOP_LIGHT,
         lambda fw, fh: (0.5 * fw / a, -1.0 * fw / a, (a / 2) * fw / a,
                         0.5 * fh / b, 1.0 * fh / b, (-a / 2) * fh / b)),
        (left, LEFT_LIGHT,
         lambda fw, fh: (fw / b, 0.0, 0.0,
                         -0.5 * fh / c, fh / c, (-a / 2) * fh / c)),
        (right, RIGHT_LIGHT,
         lambda fw, fh: (fw / a, 0.0, -b * fw / a,
                         0.5 * fh / c, fh / c, (-(a + b) / 2 - b / 2) * fh / c)),
    )
    for patch, light, coeffs in faces:
        layer = shade(patch, light).transform(
            (w, h), Image.AFFINE, coeffs(patch.width, patch.height), resample=Image.NEAREST)
        canvas.alpha_composite(layer)
    out = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    out.alpha_composite(canvas, ((CANVAS - w) // 2, (CANVAS - h) // 2))
    return out


def render_block(top, front, side):
    full = (0, 0, 16, 16)
    return render_box(16, 16, 16,
                      face_patch(top, full),
                      face_patch(front, full),
                      face_patch(side, full))


BLADE_LIGHT = (168, 170, 176, 255)
BLADE_DARK = (108, 110, 116, 255)
HUB_COLOR = (68, 68, 72, 255)
BLADE_EDGE = (62, 63, 68, 255)
MAST_COLOR = (128, 130, 136, 255)


def iso_point(x, y, z, k, origin):
    return (origin[0] + (z - x) * k, origin[1] + (x + z) * (k * 0.5) - y * k)


def quad_coeffs(p00, pw0, p0h, w, h):
    ux = (pw0[0] - p00[0]) / w
    uy = (pw0[1] - p00[1]) / w
    vx = (p0h[0] - p00[0]) / h
    vy = (p0h[1] - p00[1]) / h
    det = ux * vy - uy * vx
    a, b = vy / det, -vx / det
    d, e = -uy / det, ux / det
    return (a, b, -(a * p00[0] + b * p00[1]), d, e, -(d * p00[0] + e * p00[1]))


def paste_face(canvas, patch, p00, pw0, p0h, light, clip=None):
    from PIL import ImageChops, ImageDraw
    layer = shade(patch, light).transform(
        (CANVAS, CANVAS), Image.AFFINE, quad_coeffs(p00, pw0, p0h, patch.width, patch.height),
        resample=Image.NEAREST)
    if clip is not None:
        mask = Image.new("L", (CANVAS, CANVAS), 0)
        ImageDraw.Draw(mask).polygon(clip, fill=255)
        layer.putalpha(ImageChops.multiply(layer.getchannel("A"), mask))
    canvas.alpha_composite(layer)


def octagon(radius):
    import math
    return [(0.5 + radius * math.cos(math.radians(22.5 + step * 45)),
             0.5 + radius * math.sin(math.radians(22.5 + step * 45))) for step in range(8)]


def side_light(nx, nz):
    total = max(nx, 0.0) + max(nz, 0.0)
    if total <= 0:
        return RIGHT_LIGHT
    ratio = max(nz, 0.0) / total
    return LEFT_LIGHT * (1.0 - ratio) + RIGHT_LIGHT * ratio


def prism_sides(canvas, points, bottom, top, patch, k, origin):
    cx = sum(p[0] for p in points) / len(points)
    cz = sum(p[1] for p in points) / len(points)
    for index, (x0, z0) in enumerate(points):
        x1, z1 = points[(index + 1) % len(points)]
        nx, nz = (x0 + x1) * 0.5 - cx, (z0 + z1) * 0.5 - cz
        if nx + nz <= 0.001:
            continue
        p00 = iso_point(x0, top, z0, k, origin)
        pw0 = iso_point(x1, top, z1, k, origin)
        if abs(pw0[0] - p00[0]) < 0.5:
            continue
        p0h = iso_point(x0, bottom, z0, k, origin)
        paste_face(canvas, patch, p00, pw0, p0h, side_light(nx, nz))


def prism_top(canvas, points, top, patch, k, origin):
    xs = [p[0] for p in points]
    zs = [p[1] for p in points]
    p00 = iso_point(min(xs), top, min(zs), k, origin)
    pw0 = iso_point(min(xs), top, max(zs), k, origin)
    p0h = iso_point(max(xs), top, min(zs), k, origin)
    clip = [iso_point(x, top, z, k, origin) for x, z in points]
    paste_face(canvas, patch, p00, pw0, p0h, TOP_LIGHT, clip)


def box_faces(canvas, low, high, bottom, top, top_patch, side_patch, k, origin):
    points = [(low, low), (high, low), (high, high), (low, high)]
    prism_sides(canvas, points, bottom, top, side_patch, k, origin)
    prism_top(canvas, points, top, top_patch, k, origin)


def rect(x0, z0, x1, z1):
    return [(x0 / 16.0, z0 / 16.0), (x1 / 16.0, z0 / 16.0), (x1 / 16.0, z1 / 16.0), (x0 / 16.0, z1 / 16.0)]


TANK_CORE = (118, 74, 188, 255)
GLASS_CORE = (206, 224, 233, 255)


def render_glass_tank(end, side):
    k = (CANVAS - 6) / 2.0
    origin = (CANVAS / 2.0, (CANVAS - 2.0 * k) / 2.0 + k)
    canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    body = rect(2, 2, 14, 14)
    prism_sides(canvas, body, 0.0, 1.0, Image.new("RGBA", (12 * S, 16 * S), GLASS_CORE), k, origin)
    prism_top(canvas, body, 1.0, Image.new("RGBA", (12 * S, 12 * S), GLASS_CORE), k, origin)
    prism_sides(canvas, body, 0.0, 1.0, face_patch(side, (2, 0, 14, 16)), k, origin)
    prism_top(canvas, body, 1.0, face_patch(end, (2, 2, 14, 14)), k, origin)
    return canvas


def render_tank(frame, band, panel, end):
    height = 17 / 16.0
    k = (CANVAS - 6) / (1.0 + height)
    origin = (CANVAS / 2.0, (CANVAS - (1.0 + height) * k) / 2.0 + height * k)
    canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    cover = face_patch(end, (1, 1, 15, 15))
    post = face_patch(frame, (0, 0, 2, 12))
    ring = rect(1, 1, 15, 15)
    prism_sides(canvas, ring, 0.0, 2 / 16.0, face_patch(band, (0, 0, 16, 2)), k, origin)
    prism_top(canvas, ring, 2 / 16.0, cover, k, origin)
    posts = sorted([(1, 1), (13, 1), (1, 13), (13, 13)], key=sum)
    for x0, z0 in posts[:3]:
        prism_sides(canvas, rect(x0, z0, x0 + 2, z0 + 2), 2 / 16.0, 14 / 16.0, post, k, origin)
    core = rect(3, 3, 13, 13)
    prism_sides(canvas, core, 2 / 16.0, 14 / 16.0, Image.new("RGBA", (10 * S, 12 * S), TANK_CORE), k, origin)
    prism_top(canvas, core, 14 / 16.0, Image.new("RGBA", (10 * S, 10 * S), TANK_CORE), k, origin)
    prism_sides(canvas, rect(posts[3][0], posts[3][1], posts[3][0] + 2, posts[3][1] + 2),
                2 / 16.0, 14 / 16.0, post, k, origin)
    bar = rect(3, 13, 13, 15)
    prism_sides(canvas, bar, 9 / 16.0, 13 / 16.0, face_patch(frame, (0, 0, 2, 4)), k, origin)
    prism_top(canvas, bar, 13 / 16.0, face_patch(frame, (3, 0, 13, 2)), k, origin)
    prism_sides(canvas, rect(3.5, 15, 12.5, 15.5), 9.5 / 16.0, 12.5 / 16.0,
                face_patch(panel, (3, 6, 13, 10)), k, origin)
    prism_sides(canvas, ring, 14 / 16.0, 1.0, face_patch(band, (0, 2, 16, 4)), k, origin)
    prism_top(canvas, ring, 1.0, cover, k, origin)
    box_faces(canvas, 5 / 16.0, 11 / 16.0, 1.0, height,
              face_patch(end, (5, 5, 11, 11)), face_patch(band, (0, 6, 16, 7)), k, origin)
    return canvas


def render_centrifuge(base_top, base_side, drum, band, cap):
    height = 1.16
    k = (CANVAS - 6) / (1.0 + height)
    origin = (CANVAS / 2.0, (CANVAS - (1.0 + height) * k) / 2.0 + height * k)
    canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    box_faces(canvas, 0.0, 1.0, 0.0, 0.25,
              face_patch(base_top, (0, 0, 16, 16)), face_patch(base_side, (0, 12, 16, 16)), k, origin)
    drum_points = octagon(0.3381)
    prism_sides(canvas, drum_points, 0.244, 0.9375, face_patch(drum, (0, 0, 4, 11)), k, origin)
    prism_top(canvas, drum_points, 0.9375, face_patch(cap, (0, 0, 16, 16)), k, origin)
    prism_sides(canvas, octagon(0.3720), 0.719, 0.844, face_patch(band, (0, 0, 4, 2)), k, origin)
    box_faces(canvas, 0.3125, 0.6875, 0.925, 1.031,
              face_patch(cap, (5, 5, 11, 11)), face_patch(band, (0, 8, 6, 10)), k, origin)
    box_faces(canvas, 0.4063, 0.5938, 1.019, height,
              face_patch(cap, (6, 6, 10, 10)), face_patch(band, (0, 8, 3, 10)), k, origin)
    return canvas


def render_windmill(top, front, side):
    from PIL import ImageDraw
    import math
    radius = 1.20
    standoff = 0.25
    root = 0.15
    tip = 0.075
    hub = 0.22
    angles = [math.radians(45 + index * 90) for index in range(4)]
    center = (32.0 - standoff * 64.0, 80.0 + standoff * 32.0)

    def plane(u, v):
        return (center[0] + u * 64.0, center[1] + u * 32.0 - v * 64.0)

    tips = [plane(radius * math.cos(a), radius * math.sin(a)) for a in angles]
    xs = [0.0, 128.0] + [p[0] for p in tips]
    ys = [0.0, 128.0] + [p[1] for p in tips]
    span = max(max(xs) - min(xs), max(ys) - min(ys))
    unit = (CANVAS - 2 * round(CANVAS * 0.03)) / span
    size = round(128.0 * unit)
    cube = render_block(top, front, side).resize((size, size), Image.NEAREST)
    shift = ((CANVAS - (max(xs) - min(xs)) * unit) / 2.0 - min(xs) * unit,
             (CANVAS - (max(ys) - min(ys)) * unit) / 2.0 - min(ys) * unit)
    canvas = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    canvas.alpha_composite(cube, (round(shift[0]), round(shift[1])))

    def screen(u, v):
        point = plane(u, v)
        return (point[0] * unit + shift[0], point[1] * unit + shift[1])

    parts = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    draw = ImageDraw.Draw(parts)
    for index, angle in enumerate(angles):
        dx, dy = math.cos(angle), math.sin(angle)
        nx, ny = -dy, dx
        color = BLADE_LIGHT if index % 2 == 0 else BLADE_DARK
        draw.polygon([screen(nx * root, ny * root), screen(-nx * root, -ny * root),
                      screen(dx * radius - nx * tip, dy * radius - ny * tip),
                      screen(dx * radius + nx * tip, dy * radius + ny * tip)],
                     fill=color, outline=BLADE_EDGE)
    draw.polygon([screen(hub * math.cos(math.radians(step * 30)), hub * math.sin(math.radians(step * 30)))
                  for step in range(12)], fill=HUB_COLOR, outline=BLADE_EDGE)
    canvas.alpha_composite(parts)
    return canvas


def render_panel(top, side):
    return render_box(16, 16, 7,
                      face_patch(top, (0, 0, 16, 16)),
                      face_patch(side, (0, 9, 16, 16)),
                      face_patch(side, (0, 9, 16, 16)))


def render_tinted(base, overlay, color):
    rgb = (int(color[0:2], 16), int(color[2:4], 16), int(color[4:6], 16))
    layer = load(base)
    red, green, blue, alpha = layer.split()
    tinted = Image.merge("RGBA", (
        red.point([min(255, v * rgb[0] // 255) for v in range(256)]),
        green.point([min(255, v * rgb[1] // 255) for v in range(256)]),
        blue.point([min(255, v * rgb[2] // 255) for v in range(256)]),
        alpha))
    top = load(overlay)
    if top.size != tinted.size:
        top = top.resize(tinted.size, Image.NEAREST)
    tinted.alpha_composite(top)
    return tinted


def render_pipe(path):
    return render_box(8, 8, 16,
                      face_patch(path, (4, 4, 12, 12)),
                      face_patch(path, (4, 0, 12, 16)),
                      face_patch(path, (4, 0, 12, 16)))


def render_cable(side, cap):
    return render_box(6, 16, 6,
                      face_patch(side, (0, 5, 16, 11), rotate=90),
                      face_patch(side, (0, 5, 16, 11)),
                      face_patch(cap, (5, 5, 11, 11)))


FLUID_TINTS = {
    "oil": 0x3A3A3A,
    "fuel": 0xDCAE3C,
    "biogas": 0xE6E312,
    "biomass": 0x1AF038,
    "coolant": 0x00FFFF,
    "matter": 0x800E7C,
    "sulfuric_acid": 0xFFFAE0,
}


def render_fluid(path):
    frame = load(path + "_still")
    tint = FLUID_TINTS.get(path.rsplit("/", 1)[-1])
    if tint is not None:
        tr, tg, tb = (tint >> 16) & 0xFF, (tint >> 8) & 0xFF, tint & 0xFF
        r, g, b, a = frame.split()
        frame = Image.merge("RGBA", (
            r.point([v * tr // 255 for v in range(256)]),
            g.point([v * tg // 255 for v in range(256)]),
            b.point([v * tb // 255 for v in range(256)]),
            a))
    return frame.resize((CANVAS, CANVAS), Image.NEAREST)


def expand_block(block):
    top = block.get("top") or block.get("all")
    front = block.get("front") or block.get("all")
    side = block.get("side") or front
    return top, front, side


def collect(obj, found):
    if isinstance(obj, dict):
        block = obj.get("block")
        if isinstance(block, dict):
            found.add(("block",) + expand_block(block))
        centrifuge = obj.get("centrifuge")
        if isinstance(centrifuge, dict):
            found.add(("centrifuge", centrifuge["base_top"], centrifuge["base_side"],
                       centrifuge["drum"], centrifuge["band"], centrifuge["cap"]))
        windmill = obj.get("windmill")
        if isinstance(windmill, dict):
            found.add(("windmill",) + expand_block(windmill))
        tinted = obj.get("tinted")
        if isinstance(tinted, dict):
            found.add(("tinted", tinted["base"], tinted["overlay"], tinted["color"]))
        glass = obj.get("glass_tank")
        if isinstance(glass, dict):
            found.add(("glass_tank", glass["end"], glass["side"]))
        tank = obj.get("tank")
        if isinstance(tank, dict):
            found.add(("tank", tank["frame"], tank["band"], tank["panel"], tank["end"]))
        panel = obj.get("panel")
        if isinstance(panel, dict):
            found.add(("panel", panel["top"], panel["side"]))
        pipe = obj.get("pipe")
        if isinstance(pipe, str):
            found.add(("pipe", pipe))
        cable = obj.get("cable")
        if isinstance(cable, dict):
            found.add(("cable", cable["side"], cable["cap"]))
        fluid = obj.get("fluid")
        if isinstance(fluid, str):
            found.add(("fluid", fluid))
        for value in obj.values():
            collect(value, found)
    elif isinstance(obj, list):
        for value in obj:
            collect(value, found)


def slug(spec):
    if spec[0] == "block":
        parts = spec[1:]
    else:
        parts = spec
    return "~".join(parts).replace("/", ".") + ".png"


def main():
    specs = set()
    for root, _dirs, files in os.walk(os.path.join(WIKI, "data")):
        for name in files:
            if name.endswith(".json"):
                with open(os.path.join(root, name), encoding="utf-8") as fh:
                    collect(json.load(fh), specs)
    os.makedirs(OUT, exist_ok=True)
    for stale in os.listdir(OUT):
        os.remove(os.path.join(OUT, stale))
    errors = 0
    for spec in sorted(specs):
        try:
            if spec[0] == "block":
                img = render_block(*spec[1:])
            elif spec[0] == "centrifuge":
                img = render_centrifuge(*spec[1:])
            elif spec[0] == "windmill":
                img = render_windmill(*spec[1:])
            elif spec[0] == "tinted":
                img = render_tinted(*spec[1:])
            elif spec[0] == "glass_tank":
                img = render_glass_tank(*spec[1:])
            elif spec[0] == "tank":
                img = render_tank(*spec[1:])
            elif spec[0] == "panel":
                img = render_panel(*spec[1:])
            elif spec[0] == "pipe":
                img = render_pipe(spec[1])
            elif spec[0] == "fluid":
                img = render_fluid(spec[1])
            else:
                img = render_cable(*spec[1:])
            img.save(os.path.join(OUT, slug(spec)))
        except FileNotFoundError as missing:
            errors += 1
            print("MISSING TEXTURE:", missing, file=sys.stderr)
    print("rendered", len(specs) - errors, "of", len(specs), "icons ->", OUT)
    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
