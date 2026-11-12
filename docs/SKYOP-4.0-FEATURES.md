# SkyOP Client 4.0 — Implemented Module & UI Specification

## Navigation
- Combat, Movement, Visual, World, Player, Inventory.
- Favorites is a user-managed filter.
- Safe and Dangerous are risk filters over registered modules, not empty feature categories.
- The inspector displays the selected module risk label.
- Compact navigation calculates row height from the actual category count.

## New World HUD modules
| Module | Actual behavior | Data source |
|---|---|---|
| World Info HUD | Displays dimension, biome and weather | Local client world/player state |
| Chunk Info HUD | Displays chunk X/Z and local X/Y/Z coordinates | Local player block position |
| Performance HUD | Displays FPS and JVM used/max heap | Minecraft client FPS + JVM Runtime |
| Session Timer | Displays elapsed time in the current world session | Local elapsed time; resets on world/player context change |

All four widgets are draggable in HUD Edit and use per-module HUD position persistence.

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
