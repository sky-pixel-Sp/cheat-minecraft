# SkyOP Client

<p align="center">
  <strong>A configurable Minecraft Java 26.3 Fabric client</strong><br>
  Combat · Movement · Visual · Player · ClickGUI
</p>

<p align="center">
  <a href="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml">
    <img alt="Build status" src="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml/badge.svg?branch=main">
  </a>
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-Java%2026.3-3C8527?logo=minecraft&logoColor=white">
  <img alt="Fabric" src="https://img.shields.io/badge/Mod%20loader-Fabric-DBD0B4?logo=fabric&logoColor=222222">
  <img alt="Java" src="https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white">
  <img alt="Version" src="https://img.shields.io/badge/SkyOP-1.1.0-4BA3D9">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-blue.svg">
</p>

<p align="center">
  <a href="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/runs/42497851407">
    <strong>⬇️ DOWNLOAD SKYOP CLIENT 1.1.0</strong>
  </a>
  &nbsp;·&nbsp;
  <a href="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml?query=branch%3Amain">
    Latest builds
  </a>
  &nbsp;·&nbsp;
  <a href="docs/TEST-PLAN.md">Test plan</a>
</p>

---

## About

SkyOP is a client-side Fabric mod for Minecraft Java 26.3. It brings together a category-based in-game module manager, configurable gameplay modules, an on-screen HUD, and saved user preferences.

The interface is designed around a familiar client layout: choose a category, select a module, toggle it, and adjust its available settings without typing commands.

> **Build status:** SkyOP Client 1.1.0 was compiled with Java 25 and Gradle 9.7.1. The GitHub Actions job also verified the remapped JAR's client entrypoint and Fabric metadata. A successful build is not the same as a complete in-game compatibility test.

## Download

### SkyOP Client 1.1.0 — Minecraft 26.3

<p align="center">
  <a href="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/runs/42497851407">
    <img alt="Download SkyOP Client 1.1.0" src="https://img.shields.io/badge/⬇%20Download-SkyOP%201.1.0-1677C8?style=for-the-badge">
  </a>
</p>

1. Open the **Build SkyOP Client** workflow run using the download link above.
2. Scroll down to **Artifacts**.
3. Download **SkyOP-Client-Minecraft-26.3**.
4. Extract the downloaded ZIP archive.
5. Copy `skyop-client-1.1.0.jar` into your Minecraft instance's `mods` folder.
6. Start the Minecraft 26.3 Fabric instance.

**Artifact availability:** GitHub Actions artifacts are temporary downloads and are retained for 30 days. For a newer build, open [Latest builds](https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml?query=branch%3Amain), select the newest successful run, and download its artifact.

## Requirements

| Component | Required version |
|---|---:|
| Minecraft Java Edition | **26.3** |
| Fabric Loader | **0.20.1 or newer compatible 26.3 build** |
| Fabric API | **0.165.2+26.3** |
| Java runtime | **25 or newer** |
| Fabric Loom (development) | **1.18.4** |
| Gradle (development) | **9.7.1** |

Install Fabric Loader and Fabric API in the **same game instance** as SkyOP. Do not put the mod in a Forge or NeoForge instance.

## Open the ClickGUI

Press **Right Shift** while in-game to open the SkyOP module manager.

The interface has four categories:

| Category | Modules |
|---|---|
| **Combat** | KillAura, TriggerBot, AutoClicker |
| **Movement** | Auto Sprint, Speed, Flight |
| **Visual** | Entity ESP, Gamma Boost, HUD |
| **Player** | NoFall |

### Interface controls

- **Left-click a module:** enable or disable it.
- **Right-click a module:** select it and inspect its settings.
- **Click a setting:** toggle it or adjust its slider.
- **Disable All:** turn modules off and restore temporary client-side state.
- **Close the GUI:** use the close control or press Right Shift again through the keybind.

### Default keybinds

| Key | Action |
|---|---|
| **Right Shift** | Open ClickGUI |
| F4 | Toggle HUD |
| F5 | Toggle AutoClicker |
| F6 | Toggle Auto Sprint |
| F7 | Toggle Speed |
| F8 | Toggle KillAura |
| F9 | Toggle Entity ESP |
| F10 | Toggle NoFall |
| F11 | Toggle Flight |
| F12 | Disable all modules |

Keybinds can also be reviewed under **Options → Controls → Key Binds → SkyOP Client**.

## Module overview

### Combat

| Module | Available controls | Behaviour |
|---|---|---|
| KillAura | Target players, target hostile mobs, rotation, line of sight, range | Selects a configured nearby target and sends an attack action. |
| TriggerBot | Target filters, trigger delay | Sends an attack attempt when a valid entity is under the crosshair. |
| AutoClicker | Clicks per second | Repeats valid entity attack attempts while the attack key is held. Vanilla attack cooldown is still respected. |

### Movement

| Module | Available controls | Behaviour |
|---|---|---|
| Auto Sprint | Toggle | Sprints while forward is held and sneak is not held. |
| Speed | Movement multiplier | Applies a configurable horizontal movement boost. |
| Flight | Flight speed | Uses local no-gravity movement; jump rises and sneak descends. |

### Visual

| Module | Available controls | Behaviour |
|---|---|---|
| Entity ESP | Players, mobs, range | Adds a vanilla glowing outline to nearby living entities already known to the client. |
| Gamma Boost | Toggle | Sets the vanilla gamma option to its maximum normal value and restores the previous value when disabled. |
| HUD | Toggle | Displays enabled modules, coordinates, and FPS. |

### Player

| Module | Behaviour |
|---|---|
| NoFall | Clears the local fall-distance accumulator. A server can still apply fall damage. |

## Configuration

SkyOP saves preferences in:

```text
.minecraft/config/skyop-client.properties
```

The file stores enabled modules and the settings exposed by the ClickGUI, including combat filters, ranges, delays, movement values, and ESP options.

You can back up this file to preserve your setup or remove it to return to default settings.

## Build from source on Windows

1. Install a 64-bit **JDK 25**.
2. Confirm `java -version` reports Java 25 or newer.
3. Clone or download this repository.
4. Run `BUILD-WINDOWS.bat`.
5. Wait for Gradle to download the required dependencies and complete the build.
6. Find the remapped JAR under `build/libs/`.

The build script downloads Gradle 9.7.1 into the user's local application-data directory. An internet connection is required for the first build.

### GitHub Actions

The workflow at [`.github/workflows/build.yml`](.github/workflows/build.yml) runs on pushes to `main`, pull requests, and manual dispatches. It:

1. Checks out the source.
2. Configures Java 25 and Gradle 9.7.1.
3. Runs `clean build`.
4. Checks that the remapped JAR contains the SkyOP client entrypoint and `fabric.mod.json`.
5. Uploads the installable JAR as **SkyOP-Client-Minecraft-26.3**.

## Compatibility and safety notes

- SkyOP is a **client-side mod**. It is not a server plugin and does not grant operator permissions.
- Multiplayer servers remain authoritative. They can reject or correct movement and combat actions, and server rules may prohibit these modules.
- This project does not claim to bypass anti-cheat systems. Do not assume it will work on protected public servers.
- Flight, Speed, NoFall, and combat automation should first be tested in a local single-player world or a private server you control.
- Entity ESP only works with entities already tracked by the game client. It is not an x-ray and cannot reveal entities the client has not received.
- Gamma Boost changes the vanilla gamma option; it is not shader-level fullbright.

## Testing

See **[docs/TEST-PLAN.md](docs/TEST-PLAN.md)** for the launch checklist, GUI checks, module checks, and evidence to collect.

The automated CI build verifies compilation and packaging. It does **not** prove that the game launches successfully or that every module behaves correctly in every world or on every server. Those checks require a real Minecraft 26.3 client session.

## Contributing and issues

Bug reports should include:

- Minecraft, Fabric Loader, Fabric API, and Java versions.
- The module and settings being tested.
- `logs/latest.log` or the relevant crash report.
- Clear reproduction steps.

Please avoid sharing access tokens, account credentials, or private server details in public issues.

## License

Released under the **MIT License**. See [LICENSE](LICENSE).
