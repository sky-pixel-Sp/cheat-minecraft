# SkyOP 2.3.0 feature implementation status

This document tracks the requested feature expansion against real implementation, not menu labels.

## Implemented in `feature/skyop-2.3.0-expansion`

- Auto Hunt: targets the nearest loaded hostile mob in a configurable range without rotating the player's camera.
- Auto Bridge: selects the first placeable block in the hotbar and attempts to place it beneath the player while movement input is active.
- Infinite Jump: a fresh jump-key press while airborne applies another local jump impulse.
- Jump Boost: configurable local jump impulse multiplier.
- Item ESP: dropped item entities can be included in the existing glow-based ESP.
- Waypoints: press V to save current block coordinates; up to 32 waypoints are stored in `config/skyop-client/waypoints.properties` and displayed in a HUD list.
- Mini Map: local HUD radar for nearby loaded players, hostile mobs and dropped items.
- Night Vision: client gamma-based visibility mode with state restoration.
- Settings validation and persistence for Auto Hunt range, Jump Boost multiplier and Auto Bridge delay.
- Indonesian and English module titles/descriptions and keybind labels.
- Windows password generator source and a GitHub Actions workflow that packages a standalone EXE.

## Existing modules retained

KillAura, TriggerBot, AutoClicker, Auto Sprint, Speed, Flight, Auto Jump, Step Assist, Spider, Entity ESP, Gamma Boost, NoFall, Auto Eat, Auto Armor, Auto Tool, Auto Refill, Auto Fish, Inventory Cleaner, Hotbar Presets, HUD telemetry, keybinds, profiles and HUD editor.

## Not implemented yet

These requested features need separate implementation and in-game verification; they are not represented as working modules in this branch:

- Ore X-Ray with per-ore selection, ore scan overlay, Chams and world-space Tracers.
- 3D waypoint rendering and full terrain minimap.
- Scaffold placement planning, block whitelist/blacklist and bridge placement modes.
- Projectile lead/aim, Auto Crit, Auto Potion/Heal, advanced hitbox controls and Auto Mace/Crystal.
- Freecam with a detached camera entity, collision-safe Noclip and camera orbit.
- Bot mining by 3D cuboid coordinates, block filtering, pause/resume and resumable job state.
- Auto MLG water bucket, Auto Craft, chest automation and advanced totem/armor management.
- Music player, playlist, synchronized 2D/3D lyrics and user asset import.
- Custom theme editor, draggable floating window modes and richer layout scaling.
- In-mod login/registration gate.

## Security and multiplayer boundary

The password-generator EXE creates random local passwords only. It does not validate accounts or unlock the mod. A real account login requires an authentication service and a client-side login screen; a password generator alone cannot provide that system.

This project does not implement anti-cheat evasion, ban/kick evasion, IP rotation for spam, or bypasses for server-side protections. Client-side movement, combat and inventory actions remain subject to the server's authority. Use experimental gameplay modules only in single-player or a private server where you have permission.

## Verification requirement

A successful CI run proves compilation, unit tests, metadata checks and packaging. It does not prove that the feature works in a live Minecraft world. The modules listed above require the manual checks in `docs/TEST-PLAN.md`.
