# SkyOP Client 1.1.0 — Minecraft Java 26.3

A configurable client-side Fabric utility/cheat client with a category-based ClickGUI, combat automation, movement modules, entity highlighting, and a persistent configuration file.

[![Build SkyOP Client](https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml/badge.svg)](https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml)

## Target toolchain

| Component | Version |
|---|---:|
| Minecraft Java | **26.3** |
| Fabric Loader | **0.20.1** |
| Fabric API | **0.165.2+26.3** |
| Java | **25** |
| Fabric Loom | **1.18.4** |
| Gradle | **9.7.1** |

## In-game interface

Press **Right Shift** to open the SkyOP ClickGUI. It has separate Combat, Movement, Visual and Player categories. Left-click a module to toggle it; right-click a module to focus its settings. Slider controls and toggle controls update live.

The HUD can show enabled modules, player coordinates and FPS. The client configuration is saved to `config/skyop-client.properties` and loaded on the next launch.

## Modules

| Category | Module | Implementation |
|---|---|---|
| Combat | KillAura | Selects nearby configured targets, optional smooth rotation, attack range and tick delay settings. |
| Combat | TriggerBot | Attacks a selected valid entity under the crosshair. |
| Combat | AutoClicker | Repeats valid entity attacks while the attack key is held; respects vanilla attack cooldown. |
| Movement | Auto Sprint | Sprints when forward is held and sneak is not held. |
| Movement | Speed | Configurable horizontal movement boost. |
| Movement | Flight | Local no-gravity flight controls; jump rises and sneak descends. |
| Visual | Entity ESP | Uses the vanilla glowing outline for nearby loaded entities, with separate player/mob filters and range. |
| Visual | Gamma Boost | Sets the vanilla gamma option to its maximum normal value and restores the previous value when disabled. |
| Visual | HUD | Displays enabled modules, FPS and coordinates. |
| Player | NoFall | Clears the local fall-distance accumulator. |

Keybinds are also available in **Options → Controls → Key Binds → SkyOP Client**:

| Key | Action |
|---|---|
| Right Shift | Open ClickGUI |
| F4 | Toggle HUD |
| F5 | Toggle AutoClicker |
| F6 | Toggle Auto Sprint |
| F7 | Toggle Speed |
| F8 | Toggle KillAura |
| F9 | Toggle Entity ESP |
| F10 | Toggle NoFall |
| F11 | Toggle Flight |
| F12 | Disable all modules |

## Download the JAR

1. Open the repository's **Actions** tab.
2. Open the latest successful **Build SkyOP Client** run on `main`.
3. Download the `SkyOP-Client-Minecraft-26.3` artifact.
4. Extract the artifact ZIP.
5. Place `skyop-client-1.1.0.jar` into the Minecraft instance's `mods` directory.
6. Use Minecraft 26.3 with Fabric Loader 0.20.1 and Fabric API 0.165.2+26.3.

Every push to `main`, pull request, or manual workflow dispatch runs the build and uploads the JAR as a 30-day GitHub Actions artifact.

## Build locally on Windows

1. Install a 64-bit **JDK 25** and ensure `java -version` reports 25.
2. Clone or extract this repository.
3. Double-click `BUILD-WINDOWS.bat`.
4. Find the remapped JAR in `build\libs`.

The first build downloads Gradle and Minecraft/Fabric dependencies.

## Compatibility and honest limitations

- SkyOP is a **client-side mod**, not a server plugin and not an operator-permission tool.
- Combat and movement features send ordinary game actions or modify the local player state. A multiplayer server remains authoritative and can reject, correct, or flag those actions.
- This project does not include anti-cheat bypasses. Do not assume it works on protected public servers.
- Flight, Speed and NoFall are best tested in a local single-player world or a private server you control.
- Entity ESP only works for entities already tracked/loaded by the client. It is not an x-ray or a way to reveal entities that the client has not received.
- Gamma Boost only uses the maximum vanilla gamma option; it is not true shader-level fullbright.
- A successful CI build verifies compilation and JAR structure. It does not replace a manual launch and in-game feature test.

## Test plan

See [`docs/TEST-PLAN.md`](docs/TEST-PLAN.md) for the required launcher and multiplayer-limit checks.

## License

MIT. See [`LICENSE`](LICENSE).
