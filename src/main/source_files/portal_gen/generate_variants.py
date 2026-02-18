# Quick color palette applying script

import os
from PIL import Image

PALETTE_FILE = "palette.png"
DYE_COLORS = [
    "black", "blue", "brown", "cyan", "gray", "green", "light_blue", "light_gray",
    "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow"
]

palette_img = Image.open(PALETTE_FILE).convert("RGBA")
palette_pixels = list(palette_img.getdata())
width, height = palette_img.size

# Row 0 = source colors
source_colors = palette_pixels[:width]

# Rows 1..16 = target colors
target_rows = [
    palette_pixels[i * width:(i + 1) * width]
    for i in range(1, height)
]

# ---- Process images ----
for filename in os.listdir("."):
    if not filename.lower().endswith(".png") or filename == PALETTE_FILE:
        continue

    img = Image.open(filename).convert("RGBA")
    pixels = list(img.getdata())

    # Build a mapping dict from source -> target for each dye
    for idx, color_name in enumerate(DYE_COLORS):
        target_colors = target_rows[idx]  # row idx corresponds to this dye

        color_map = {src: tgt for src, tgt in zip(source_colors, target_colors)}

        # Apply color mapping
        new_pixels = [color_map.get(px, px) for px in pixels]  # leave unchanged if not found
        new_img = Image.new("RGBA", img.size)
        new_img.putdata(new_pixels)

        # Save with new filename
        new_filename = "../" + filename.replace("blue", color_name)
        new_img.save(new_filename)
        print(f"Saved {new_filename}")
