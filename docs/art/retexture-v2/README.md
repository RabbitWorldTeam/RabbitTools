> Historical artwork version. Current colors and shading are in ../modern-vanilla-v4/.

> The three component icons in this revision were superseded by ../components-v3/. The five wand-head recolors remain current.

# Item retexture v2

Current inventory assets are the 16x16 RGBA files under src/main/resources/assets/rabbittools/textures/item/. The preview sheet is a nearest-neighbor enlargement of the final PNGs: top row building component, precision building component, blank core; bottom row iron, gold, diamond, netherite, infinity wand heads.

## Method and references

Created using the built-in imagegen tool on 2026-11-08. Generated artwork is retained as <item-id>-source.png. Prior project textures are backed up in previous-textures/.

The Create precision mechanism at revision 4148fd8d5b05c85d88947edc00fb495a5074f57d was a structure/style reference for newly redrawn mechanical component icons. Its original PNG is only in ignored build/reference/create and is not copied into either jar. Create's assets are marked All Rights Reserved; see THIRD_PARTY_NOTICES.md for the original source and license URLs.

The five wand heads use the exact original MIT-licensed Construction Wands angel-core 16x16 pixel template, not the previously generated elongated gem silhouette. Imagegen produced material recolor palettes; the exporter maps them by brightness onto the original source palette. Source pixel positions, transparency, facets and highlight locations are preserved. No wand handle, extra frame or larger gem shape is added.

Export script: scripts/Export-ItemRetextures.ps1. Component images are sampled to 16x16. Palette normalization for heads uses the original angel-core source pixels. source-manifest.json references the archived generated sources for repeatable exports from the project root.

No item IDs, recipes or gameplay rules change in this revision.

## Exact imagegen prompts

### building_component

Use case: style-transfer. Asset type: RabbitTools Minecraft inventory texture BUILDING COMPONENT. Image 1 is the official Create precision mechanism item texture at nearest-neighbor enlargement, a STRUCTURE AND STYLE REFERENCE ONLY. Create a NEW hand-drawn mechanical assembly icon in that chunky vanilla 16x16 pixel vocabulary, with a compact copper-and-iron mechanism plate, interlocking brass/copper gear teeth, a small dark gray shaft or rivet, and layered stepped metal highlights. Replace the previous hollow ring/cross design entirely. Similar visual density and filling the 16x16 canvas like the precision mechanism reference, but use a newly arranged mechanical design: copper frame on left/bottom, silver-gray upper plate and a central copper cog with a dark spindle on lower-right. Ordinary-tier colors iron gray, warm copper, restrained gold highlights, tiny dark redstone accent, dark brown outlines. One isolated sprite, truly transparent background, no text, no handle, no letters, no glow, no external shadow. Strict 16x16 logical pixel grid uniformly enlarged, single flat color per logical pixel, hard stepped edges. No round smooth cogwheels, antialiasing or gradients. This is a mechanical component rather than a magic gem.

### precision_building_component

Use case: precise-object-edit. Image 1 is the newly drawn RabbitTools Building Component, the edit target and mechanical silhouette reference. Image 2 is the original Create precision mechanism as style/structure reference only. Recolor and lightly simplify image 1 into precision building component. Keep a compact assembly of metal plate, interlocking cogs and lower-right spindle, with the same basic silhouette/layout as image 1, but simplify to the sparse readable 16x16 Minecraft pixel vocabulary of image 2. Colors: gold/brass plates, bright gold cogs, a restrained diamond-cyan center spindle, copper shadows. This is a mechanical assembly, NOT a gemstone or ring/cross icon. Strict 16 by 16 logical pixel canvas uniformly enlarged. One flat color per logical pixel. Truly transparent background, no shadow, no text, no labels, no smooth circles, antialiasing or gradients.

### blank_core

Use case: precise-object-edit. Image 1 is the newly drawn RabbitTools Building Component, the edit target and mechanical silhouette reference. Image 2 is the original Create precision mechanism as style/structure reference only. Recolor and lightly simplify image 1 into blank core. Keep a compact assembly of metal plate, interlocking cogs and lower-right spindle, with the same basic silhouette/layout as image 1, but simplify to the sparse readable 16x16 Minecraft pixel vocabulary of image 2. Colors: dark iron/gunmetal plates, muted gray-purple inert center spindle and dull steel gears, subdued highlights; inactive, not glowing. This is a mechanical assembly, NOT a gemstone or ring/cross icon. Strict 16 by 16 logical pixel canvas uniformly enlarged. One flat color per logical pixel. Truly transparent background, no shadow, no text, no labels, no smooth circles, antialiasing or gradients.

### iron_wand_head

Use case: precise-object-edit. EDIT TARGET: attached image 1, the EXACT original RabbitTools angel core PNG shown at nearest-neighbor 16x enlargement, on transparent background. Recolor ONLY, converting it into iron wand head. Preserve the original 16 by 16 pixel positions, exact outline and alpha mask, the exact small core shape, position, size, number of pixels, facets, border and highlight locations. DO NOT redraw a larger elongated gemstone, do not add a socket, metal plate, staff handle, frame, glow, gradients, noise, or details. This is a palette swap of the existing core asset. Apply this material palette: silver/steel-gray interior, pale gray-white highlights and darker gray shadow; edge rim light gray, not cyan. Remove the original yellow hue and cyan edging where inconsistent with the new material. Uniform solid color per logical pixel. Export the same square canvas scaled uniformly; all logical pixels remain aligned to the exact original grid, preserve padding and transparency. No text or labels.

### gold_wand_head

Use case: precise-object-edit. EDIT TARGET: attached image 1, the EXACT original RabbitTools angel core PNG shown at nearest-neighbor 16x enlargement, on transparent background. Recolor ONLY, converting it into gold wand head. Preserve the original 16 by 16 pixel positions, exact outline and alpha mask, the exact small core shape, position, size, number of pixels, facets, border and highlight locations. DO NOT redraw a larger elongated gemstone, do not add a socket, metal plate, staff handle, frame, glow, gradients, noise, or details. This is a palette swap of the existing core asset. Apply this material palette: gold/yellow interior, pale cream highlights, amber shadows; edge rim pale gold, not cyan. Remove the original yellow hue and cyan edging where inconsistent with the new material. Uniform solid color per logical pixel. Export the same square canvas scaled uniformly; all logical pixels remain aligned to the exact original grid, preserve padding and transparency. No text or labels.

### diamond_wand_head

Use case: precise-object-edit. EDIT TARGET: attached image 1, the EXACT original RabbitTools angel core PNG shown at nearest-neighbor 16x enlargement, on transparent background. Recolor ONLY, converting it into diamond wand head. Preserve the original 16 by 16 pixel positions, exact outline and alpha mask, the exact small core shape, position, size, number of pixels, facets, border and highlight locations. DO NOT redraw a larger elongated gemstone, do not add a socket, metal plate, staff handle, frame, glow, gradients, noise, or details. This is a palette swap of the existing core asset. Apply this material palette: diamond cyan interior, icy white highlights, teal shadows; edge rim pale icy cyan. Remove the original yellow hue and cyan edging where inconsistent with the new material. Uniform solid color per logical pixel. Export the same square canvas scaled uniformly; all logical pixels remain aligned to the exact original grid, preserve padding and transparency. No text or labels.

### netherite_wand_head

Use case: precise-object-edit. EDIT TARGET: attached image 1, the EXACT original RabbitTools angel core PNG shown at nearest-neighbor 16x enlargement, on transparent background. Recolor ONLY, converting it into netherite wand head. Preserve the original 16 by 16 pixel positions, exact outline and alpha mask, the exact small core shape, position, size, number of pixels, facets, border and highlight locations. DO NOT redraw a larger elongated gemstone, do not add a socket, metal plate, staff handle, frame, glow, gradients, noise, or details. This is a palette swap of the existing core asset. Apply this material palette: dark warm gray/charcoal interior, muted light warm-gray highlights, dark brown shadows; edge rim medium warm-gray, not cyan. Remove the original yellow hue and cyan edging where inconsistent with the new material. Uniform solid color per logical pixel. Export the same square canvas scaled uniformly; all logical pixels remain aligned to the exact original grid, preserve padding and transparency. No text or labels.

### infinity_wand_head

Use case: precise-object-edit. EDIT TARGET: attached image 1, the EXACT original RabbitTools angel core PNG shown at nearest-neighbor 16x enlargement, on transparent background. Recolor ONLY, converting it into infinity wand head. Preserve the original 16 by 16 pixel positions, exact outline and alpha mask, the exact small core shape, position, size, number of pixels, facets, border and highlight locations. DO NOT redraw a larger elongated gemstone, do not add a socket, metal plate, staff handle, frame, glow, gradients, noise, or details. This is a palette swap of the existing core asset. Apply this material palette: violet-purple interior, lavender-white highlights, deep indigo shadows; edge rim pale lavender, not cyan. Remove the original yellow hue and cyan edging where inconsistent with the new material. Uniform solid color per logical pixel. Export the same square canvas scaled uniformly; all logical pixels remain aligned to the exact original grid, preserve padding and transparency. No text or labels.
