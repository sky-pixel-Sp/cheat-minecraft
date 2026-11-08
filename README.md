# SkyOP Client

<p align="center">
  <strong>A configurable Minecraft Java 26.3 Fabric client</strong><br>
  Combat · Movement · Visual · Player · Quick Profiles · Two-column ClickGUI
</p>

<p align="center">
  <a href="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml">
    <img alt="Build status" src="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml/badge.svg?branch=main">
  </a>
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-Java%2026.3-3C8527?logo=minecraft&logoColor=white">
  <img alt="Fabric" src="https://img.shields.io/badge/Mod%20loader-Fabric-DBD0B4?logo=fabric&logoColor=222222">
  <img alt="Java" src="https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white">
  <img alt="Version" src="https://img.shields.io/badge/SkyOP-1.3.0-4BA3D9">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-blue.svg">
</p>

<p align="center">
  <a href="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml?query=branch%3Amain">
    <strong>⬇️ DOWNLOAD LATEST SUCCESSFUL BUILD</strong>
  </a>
  &nbsp;·&nbsp;
  <a href="docs/TEST-PLAN.md">Test plan</a>
</p>

---

## About

SkyOP is a client-side Fabric mod for Minecraft Java 26.3. Version 1.3.0 adds configurable target FOV and rotation speed, corrects combat cooldown accounting, introduces Speedometer and CPS HUD telemetry, and keeps the expanded module library and quick profiles.

The interface uses a two-column module grid, a live settings inspector, category counts, status indicators, and one-click profiles. Settings are persisted between launches.

> **Build status:** The GitHub Actions workflow compiles the project with Java 25 and Gradle 9.7.1, checks the remapped mod JAR and uploads it as an artifact. A successful build does not replace a real in-game test.

## Download

1. Open [Latest successful builds](https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml?query=branch%3Amain).
2. Select the newest run with a green **Success** status.
3. Scroll to **Artifacts** and download **SkyOP-Client-Minecraft-26.3**.
4. Extract the ZIP archive.
5. Copy skyop-client-1.3.0.jar into the Minecraft instance mods folder.
6. Launch Minecraft Java 26.3 with Fabric Loader and Fabric API installed.

GitHub Actions artifacts are temporary and retained for 30 days. Always use the newest successful build for the latest version.

## Requirements

| Component | Required version |
|---|---:|
| Minecraft Java Edition | **26.3** |
| Fabric Loader | **0.20.1 or newer compatible 26.3 build** |
| Fabric API | **0.165.2+26.3** |
| Java runtime | **25 or newer** |
| Fabric Loom (development) | **1.18.4** |
| Gradle (development) | **9.7.1** |

Install Fabric Loader, Fabric API, and SkyOP in the same game instance. Do not use this JAR in Forge or NeoForge.

## Open the ClickGUI

Press **Right Shift** while in-game.

### UI controls

| Input | Action |
|---|---|
| Left-click a module card | Enable or disable the module |
| Right-click a module card | Select it without toggling |
| Click a setting | Toggle it or set a slider value |
| PVE profile | Load a hostile-mob focused configuration |
| OP profile | Load the full local PvE feature preset |
| VISUAL profile | Load ESP and player HUD features |
| Disable All | Disable modules and restore temporary client state |
| Close button | Close the ClickGUI |

### Default keybinds

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

Keybinds are also listed under **Options → Controls → Key Binds → SkyOP Client**.

## Modules

### Combat

| Module | Settings and behaviour |
|---|---|
| KillAura | Target filters, smooth rotation, line of sight, range, lowest-health priority, aim FOV, rotation speed |
| TriggerBot | Target filters and trigger delay; attacks a valid entity under the crosshair |
| AutoClicker | Configurable CPS while the attack key is held; vanilla attack cooldown remains respected |

### Movement

| Module | Settings and behaviour |
|---|---|
| Auto Sprint | Automatically sprints while moving forward |
| Speed | Configurable horizontal movement multiplier |
| Flight | Local no-gravity movement; jump rises and sneak descends |
| Auto Jump | Automatically jumps while moving forward; jump interval is configurable |
| Step Assist | Jumps when forward movement collides with a wall |
| Spider | Applies upward motion while pushing into a wall; climb speed is configurable |

### Visual

| Module | Settings and behaviour |
|---|---|
| Entity ESP | Player/mob filters and scan radius; only highlights loaded entities |
| Gamma Boost | Sets vanilla gamma to its maximum normal value and restores the previous value |
| HUD | Active module list, coordinates and FPS |
| Target HUD | Crosshair target name, health bar and distance |
| Keystrokes | Live W/A/S/D and jump key indicators |
| Armor HUD | Current health and armor points |
| Speedometer | Horizontal movement speed in blocks per second |
| CPS HUD | Successful attack actions per second |

### Player

| Module | Settings and behaviour |
|---|---|
| NoFall | Clears the local fall-distance accumulator; a server can still apply fall damage |

## Profiles

- **PVE:** Enables the mob-focused combat and movement baseline.
- **OP:** Enables the full local PvE preset, including combat automation, movement, Spider, NoFall, ESP and player HUDs. Player targeting remains disabled by default.
- **VISUAL:** Enables ESP, gamma and the HUD overlays.

Profiles can be adjusted after loading. Their values are saved to the local config file.

## Configuration

Configuration path: .minecraft/config/skyop-client.properties

The file stores module states, combat filters, target priority, ranges, click timing, movement values, ESP options and profile values. Back up this file to preserve a setup; remove it to reset to defaults.

## Build from source on Windows

1. Install a 64-bit JDK 25.
2. Confirm java -version reports Java 25 or newer.
3. Clone or download this repository.
4. Run BUILD-WINDOWS.bat.
5. Find the remapped JAR under build/libs/.

The first build requires an internet connection so Gradle can download dependencies.

## Compatibility and safety notes

- SkyOP is a **client-side mod**. It does not grant operator permissions and is not a server plugin.
- Multiplayer servers remain authoritative and may reject, correct or flag automated movement and combat actions. Server rules may prohibit these modules.
- This project does not claim to bypass anti-cheat systems.
- Flight, Speed, Spider, NoFall and combat automation should first be tested in a local single-player world or a private server you control.
- ESP only works with entities already tracked by the client. It cannot reveal entities the client has not received.
- Gamma Boost changes the normal vanilla gamma option; it is not shader-level fullbright.

## Testing

See [docs/TEST-PLAN.md](docs/TEST-PLAN.md) for the launch checklist, GUI tests, module tests and evidence to collect.

The automated CI job verifies compilation and packaging. It does **not** prove that the client launches or that each module behaves correctly in every world or on every server.

## Contributing

Bug reports should include the Minecraft, Loader, Fabric API and Java versions, module settings, reproduction steps, logs/latest.log, and any crash report. Do not share account credentials or private server details.

## License

Released under the **MIT License**. See LICENSE.
