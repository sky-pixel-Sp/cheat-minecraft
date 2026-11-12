# SkyOP Client 4.0 — Implemented Module & UI Specification

## Navigation
- Combat, Movement, Visual, World, Player, Inventory.
- Favorites is a user-managed filter.
- Safe and Dangerous are risk filters over registered modules, not empty feature categories.
- The inspector displays the selected module risk label, and now auto-selects the first matching item when those filters are opened.
- Category membership is implemented once in SkyOPCategory and reused by selection, listing and count logic.
- Compact navigation calculates row height from the actual category count.

## Quick Panel
- Responsive in-world panel with up to eight real module toggles; available count adapts to current scaled viewport.
- The header theme control cycles the persisted UI theme.
- Full GUI opens the complete ClickGUI.
- Disable All invokes the central cleanup path, restoring temporary client state.
- Panel drag position is persisted and clamped to the current viewport.

## New World HUD modules
| Module | Actual behavior | Data source |
|---|---|---|
| World Info HUD | Displays dimension, biome and weather | Local client world/player state |
| Chunk Info HUD | Displays chunk X/Z and local X/Y/Z coordinates | Local player block position |
| Performance HUD | Displays FPS and JVM used/max heap | Minecraft client FPS + JVM Runtime |
| Session Timer | Displays elapsed time in the current world session | Local elapsed time; resets on world/player context change |
| Food HUD | Displays hunger and saturation | Player FoodData |
| Air HUD | Displays current/max air supply | Player air supply |
| Experience HUD | Displays XP level and progress | Player experience state |
| Item Info HUD | Displays main-hand item name, stack count and durability | Main-hand ItemStack |
| Offhand HUD | Displays offhand item name and stack count | OFFHAND equipment slot |
| Block Info HUD | Displays block under crosshair and coordinates | Client block hit result |
| Armor Detail HUD | Displays HEAD/CHEST/LEGS/FEET and remaining durability | Equipped armor ItemStacks |

All eleven newly-added widgets are draggable in HUD Edit and use per-module HUD position persistence.

## KillAura targeting
- Target FOV is configurable from 1° through 360°; default is 360°.
- 360° target selection includes entities in front, behind, above and below, provided they are within the current bounded target range and loaded by the client.
- The line-of-sight option remains available. When disabled, client target scanning does not discard loaded entities merely because a block obscures the ray.
- Attack requests continue through Minecraft's normal game interaction method and vanilla attack cooldown. There is no custom packet, anti-cheat bypass, server-side data query, hidden-chunk loading, or guarantee that a server accepts an attack through blocks. Server authority and server rules apply.
- KillAura is marked DANGEROUS.

## Risk model
Dangerous labels identify modules that automate or alter combat, movement, mining or inventory actions and may violate server rules. Safe labels identify informational/local presentation modules. Labels are risk notices, not promises of multiplayer compatibility.

## Verification boundary
CI compilation and unit tests do not prove in-world or multiplayer behavior. The local-world manual plan is recorded in TEST-PLAN.md.
