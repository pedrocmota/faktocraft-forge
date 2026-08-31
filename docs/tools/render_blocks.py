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
