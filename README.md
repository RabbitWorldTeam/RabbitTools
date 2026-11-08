# RabbitTools

Minecraft 1.21.1 / NeoForge 21.1.265 / Java 21.
Install RabbitTools on both the client and server. RabbitCore, RabbitMine and the
original Construction Wands Revived jar are not required.

## Included tools

- Iron, copper, gold and diamond mining hammers, migrated from RabbitMine.
  Mine a 3x3 plane of pickaxe-mineable blocks; sneak for single-block mining.
  Recipes, textures, outline preview and vanilla pickaxe enchantments are retained.
- Wood, stone, iron, gold, diamond, netherite and infinity construction wands.
- Construction, angel, destruction and exchange cores; wand upgrades and options.
- Placement preview, undo, random placement, direction/matching restrictions.
- Inventory, offhand, shulker box, bundle and generic NeoForge item-handler supplies.
- Void sack, recipes, translations and the upstream configuration system.
- Building and precision building components, plus five material-specific wand heads.
- Tradable blank cores (`rabbittools:blank_core`) with no recipe or automatic loot source.

All tool IDs use `rabbittools:`. The RabbitTools creative tab contains both tool families.
The upstream wand mechanics and operating limits are retained. Crafting uses a RabbitTools
progression: components -> material wand heads -> finished wands. Each angel, destruction
or exchange core additionally consumes one blank core.

## Controls and configuration

The default wand option modifier is CTRL (configurable in the client configuration).
Refer to the translated tooltips and Controls menu for the available bindings.
Sneak + modifier + right-click empty space opens wand options. Sneak + modifier
while targeting previous work previews undo; right-click to undo.

- Server defaults (NeoForge 21.1.265): `config/rabbittools-server.toml`.
- Optional world override: `<world>/serverconfig/rabbittools-server.toml`.
- Local client preferences: `config/rabbittools-client.toml`.

Server-type configuration is synchronized by NeoForge when connecting.
Each wand section supports `limit`, `angel`, `destruction`, `exchange` and `upgradeable`;
non-infinity wands also support `durability`. Set a core's numeric limit to 0 to disable it.
Hammers retain the original material durability and 3x3 behavior.

## Build and development

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

The distributable is `build/libs/rabbittools-1.0.0.jar`; the sources jar is for development.
If Windows Java fails to establish a loopback connection because its sandbox TEMP path
is too long, create `build/tmp/java` and set a short temporary path for the command:

```powershell
New-Item -ItemType Directory -Path build/tmp/java -Force
$env:JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=$((Get-Location).Path)\build\tmp\java"
.\gradlew.bat build
```

## Migration and scope

RabbitMine no longer registers hammers. Existing `rabbitmine:*_mining_hammer` stacks
are not remapped; this was explicitly approved for test worlds. Likewise, old
`constructionwand:*` items and configurations are not migrated.

Third-party-specific adapters (AE2, RS, Curios, Sophisticated Backpacks, Botania,
JEI/REI/EMI extensions) are excluded. Ordinary recipes still use standard Minecraft
recipe data. Generic item-handler containers remain supported without those adapters.


## Blank core supply control

Blank cores are ordinary transferable, stackable items. They have no crafting recipe,
loot table, automatic grant or player binding. They are visible in the creative tab.
Administrators can issue them manually or configure external quest/event rewards:

```mcfunction
give <player> rabbittools:blank_core 1
```

Each angel, destruction or exchange core consumes exactly one blank core in addition
to four glass panes, two of the previous corner material and two of the previous center material. The blank core occupies the center; the original center material occupies the top-left and bottom-right corners. Installing a completed core consumes only that core,
with no additional blank-core cost or blank-core refund. Existing completed cores and
installed upgrades remain usable. This controls new special-core production through
issued supply, rather than enforcing a per-player possession limit. Void sacks also
indirectly require this supply because their recipe uses a destruction core.

## Wand head crafting progression

| Wand head | Ingredients |
| --- | --- |
| Iron | 1 Building Component + 2 iron ingots, shapeless |
| Gold | 1 Building Component + 2 gold ingots, shapeless |
| Diamond | 1 Precision Building Component + 2 diamonds, shapeless |
| Netherite | Diamond Wand Head + netherite upgrade template + netherite ingot, smithing |
| Infinity | Netherite Wand Head + Precision Building Component + Nether Star, shapeless |

Every recipe produces one head. Each finished iron/gold/diamond/netherite/infinity wand
uses one matching head plus two wooden rods, using the original diagonal wand layout.
Wood and stone wand recipes remain unchanged. The old direct-material recipes for the
five higher-tier wands have been replaced, so those wands cannot bypass head crafting.

Wand heads are plain crafting materials, not installable functional cores, and do not
require blank cores. No completed wand is consumed in this progression: install special
cores after crafting the desired wand. Existing completed wands remain usable.

## Credits

Construction wand code and assets derive from Construction Wands Revived,
Neo/1.21.1 revision `7bee74d5986a35adda53a1f029cb8a02e41b24b0`.
See THIRD_PARTY_NOTICES.md and LICENSE. RabbitTools includes the license in its jar.

## Current artwork

The three components and five wand heads follow a modern vanilla material treatment:
restrained contrast, coherent pixel clusters, warm/cool hue shifts and selective colored
outlines. Their exact original 16x16 silhouettes remain unchanged. See docs/ART_STYLE.md
for the art guide and docs/art/modern-vanilla-v4/README.md for native previews, source
images, material palettes and full generation prompts. Recipes and mechanics are unchanged.
