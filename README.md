# SkyOP Client — Minecraft Java 26.3

A client-side Fabric mod with toggleable movement, visual and hostile-mob combat modules.

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

## Modules and keybinds

| Key | Module | Behaviour |
|---|---|---|
| F4 | HUD | Shows the SkyOP module status overlay. |
| F6 | Auto Sprint | Sprints while the forward key is held and sneak is not held. |
| F7 | Speed Boost | Raises horizontal movement speed while a movement key is held. |
| F8 | KillAura | Automatically attacks nearby hostile `Monster` entities in front of the player. It does not target other players. |
| F9 | Entity ESP | Applies the vanilla glowing outline to nearby living entities and restores their previous glow state when disabled. |
| F10 | NoFall | Clears the local fall-distance accumulator. A remote server can still apply fall damage. |
| F12 | Disable All | Turns off all modules and restores ESP glow state. |

Keybinds can be changed under **Options → Controls → Key Binds → SkyOP Client**.

## Download the built JAR from GitHub

1. Open the repository's **Actions** tab.
2. Select the latest successful **Build SkyOP Client** run on `main`.
3. Download the `SkyOP-Client-Minecraft-26.3` artifact.
4. Extract the ZIP and put `skyop-client-1.0.0.jar` in the Minecraft instance's `mods` folder.
5. Install Fabric Loader 0.20.1 and Fabric API 0.165.2+26.3 in the same Minecraft 26.3 instance.

Every push to `main`, pull request, or manual workflow dispatch runs the build. The compiled JAR is uploaded as a GitHub Actions artifact for 30 days.

## Build locally on Windows

1. Install a 64-bit **JDK 25** and ensure `java -version` reports 25.
2. Extract or clone this repository to a writable folder.
3. Double-click `BUILD-WINDOWS.bat`.
4. After a successful build, find the remapped mod JAR in `build\libs`.

The first build downloads Gradle, Minecraft and Fabric dependencies, so an internet connection is required.

## Important behaviour and limitations

- SkyOP is a **client-side mod**. It does not install on a server or grant server operator permissions.
- Movement changes, NoFall and automated attacks are not guaranteed to work on multiplayer servers. Servers remain authoritative and may reject these actions or enforce their rules.
- KillAura currently targets hostile mobs only; it does not attack other players, villagers or passive mobs.
- Start testing in a local single-player world and back up important worlds before testing mods.

## Test checklist

See [`docs/TEST-PLAN.md`](docs/TEST-PLAN.md) for the manual in-game checks. A successful GitHub Actions build confirms compilation and JAR structure; it does not replace launching Minecraft and testing each feature in-game.

## License

MIT. See [`LICENSE`](LICENSE).
