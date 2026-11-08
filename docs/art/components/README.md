> Historical artwork version. Current replacement textures and generation records are in ../retexture-v2/.

# Component texture generation

Created with the built-in imagegen tool on 2026-11-08. Existing RabbitTools iron wand,
angel core and exchange core PNGs served as pixel-art and material-color references.
Original generated outputs are retained as *-source.png. Project textures are 16x16
RGBA exports; *-preview.png are exact nearest-neighbor 256x256 previews of those textures.
Pixel-center sampling is offset to retain the small redstone center in the generated artwork.

## Prompts

Building Component: one isolated compact clipped-corner square iron-gray fitting,
copper cross connector, redstone-red center; strict Minecraft 16x16 logical pixels,
transparent outer background and interior holes, top-left light highlights,
dark lower-right outlines, limited palette, no text, glow, or external shadows.
References: iron_wand.png and core_angel.png.

Precision Building Component: same fitting silhouette as ordinary component;
gold/amber outer frame and diamond-cyan cross connector with redstone-red center;
strict Minecraft pixel art and transparent holes, matching lighting and composition,
limited palette, no text, glow, or external shadows.
References: ordinary component generated source and core_exchange.png.

## Item recipes

Both recipes produce one item and have exactly the agreed material counts.

building_component: 4 iron ingots, 2 copper ingots, 1 redstone dust.
precision_building_component: 4 diamonds, 2 gold ingots, 1 blaze rod, 2 redstone dust.

These components now make iron/gold/diamond wand heads. Netherite and infinity heads extend that progression. The no-recipe blank core quota item gates special functional cores separately.

## Blank Core

Created with built-in imagegen, using the two component previews as style references.
Prompt: one compact clipped-corner dark gunmetal socket with an unlit muted gray-purple crystal; Minecraft 16x16 logical pixel art, transparent padding, top-left highlights and dark lower-right outlines; no glow, red center, gold, cyan, text or external shadow.

The original is blank_core-source.png. The project asset is a 16x16 RGBA export; blank_core-preview.png is its nearest-neighbor enlarged preview.
