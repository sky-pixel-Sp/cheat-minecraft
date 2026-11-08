# SkyOP Client 1.1.0 test plan

## Automated CI checks

GitHub Actions builds with Java 25 and Gradle 9.7.1 using Fabric Loom 1.18.4. The workflow runs `clean build`, verifies the remapped JAR includes the client entrypoint and Fabric metadata, and uploads the JAR artifact.

## In-game launch

1. Create a separate Minecraft 26.3 Fabric instance.
2. Install Fabric Loader 0.20.1 and Fabric API 0.165.2+26.3.
3. Add `skyop-client-1.1.0.jar` to that instance's `mods` folder.
4. Launch and verify the client reaches the title screen without a crash.
5. Join a new local test world and check `logs/latest.log` if anything fails.

## ClickGUI and persistence

1. Press Right Shift; the ClickGUI should open without pausing the integrated world.
2. Switch through Combat, Movement, Visual and Player categories.
3. Left-click modules to enable/disable them; right-click a module to focus its settings.
4. Change KillAura range, target filters, AutoClicker CPS, Speed multiplier, Flight speed and ESP range.
5. Close and reopen the ClickGUI; the values should remain visible.
6. Exit and relaunch the game; inspect `config/skyop-client.properties` and verify settings were saved/loaded.

## Module checks in a local world

1. Auto Sprint: move forward; sprint should engage unless sneak is held.
2. Speed: adjust multiplier in ClickGUI and compare movement while holding movement keys.
3. Flight: enable it; jump rises, sneak descends, and disable restores the original no-gravity flag.
4. NoFall: check local fall-distance behaviour; do not assume a remote server accepts it.
5. Entity ESP: spawn a hostile mob and another player in a test LAN world; verify enabled entity types glow and disabling ESP restores their prior glow state.
6. Gamma Boost: verify vanilla gamma changes to its maximum and returns to the previous value when disabled.
7. KillAura: test first with hostile mobs; then separately enable player targeting only in a private test world. Verify range, line-of-sight and rotation settings.
8. TriggerBot: aim at a valid configured target and verify attack attempts follow the configured delay.
9. AutoClicker: hold the attack key while aimed at a valid target; verify attacks respect vanilla cooldown.
10. F12: verify all modules switch off and temporary state is restored.

## Multiplayer checks

Use only a private server you control. Record whether the server accepts movement/attack actions. Server-side correction, combat restrictions, or anti-cheat flags are not evidence of a local compilation failure.

## Evidence to record

Record Minecraft version, Loader version, Fabric API version, Java version, selected modules, setting values, `logs/latest.log`, and any crash report. A successful GitHub Actions run is a compile/packaging check, not proof that every feature behaves correctly in every server environment.
