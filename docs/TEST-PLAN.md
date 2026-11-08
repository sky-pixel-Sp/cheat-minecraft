# SkyOP Client 1.3.0 test plan

## Automated CI checks

GitHub Actions builds with Java 25 and Gradle 9.7.1 using Fabric Loom 1.18.4. The workflow runs clean build, verifies the remapped JAR includes the client entrypoint and Fabric metadata, and uploads the installable JAR.

## In-game launch

1. Create a separate Minecraft 26.3 Fabric instance.
2. Install Fabric Loader 0.20.1 and Fabric API 0.165.2+26.3.
3. Add skyop-client-1.3.0.jar to that instance's mods folder.
4. Launch and verify the client reaches the title screen without a crash.
5. Join a new local test world and check logs/latest.log if anything fails.

## ClickGUI and profiles

1. Press Right Shift; the ClickGUI should open without pausing the integrated world.
2. Check category counts and the two-column module cards.
3. Switch through Combat, Movement, Visual and Player.
4. Left-click a module to toggle it; right-click to select it without toggling.
5. Click the PVE, OP and VISUAL preset buttons and verify the resulting module states.
6. Use Disable All and verify temporary state is restored.
7. Adjust each visible slider and toggle, close/reopen the GUI, and confirm values persist.

## Module checks in a local world

1. Auto Sprint: move forward; sprint should engage unless sneak is held.
2. Speed: adjust the multiplier and compare movement while holding movement keys.
3. Flight: enable; jump rises, sneak descends, and disable restores the original no-gravity flag.
4. Auto Jump: hold forward on flat ground and verify the configured interval.
5. Step Assist: walk into a one-block obstacle and verify the player jumps while on the ground.
6. Spider: hold forward against a wall and verify upward movement; disable and verify normal gravity.
7. NoFall: check local fall-distance behaviour; do not assume a remote server accepts it.
8. Entity ESP: spawn a hostile mob and a player in a test LAN world; verify filters/range and confirm glow is restored when disabling or leaving range.
9. Gamma Boost: verify gamma changes to its maximum and returns to the previous value when disabled.
10. KillAura: test hostile mobs first; then separately test player targeting only in a private test world. Verify target filters, line of sight, priority and rotation.
11. TriggerBot: aim at a configured valid target and verify delay.
12. AutoClicker: hold attack while aimed at a configured target; verify vanilla cooldown is respected.
13. Target HUD: aim at a living entity; check name, health bar and distance.
14. Keystrokes: press W/A/S/D and jump; the corresponding indicators should light up.
15. Armor HUD: verify health and armor values update while taking damage or changing armor.
16. Speedometer: move in a straight line, then stop; verify blocks-per-second updates and settles near zero.
17. CPS HUD: perform successful attacks in a local world and verify the rolling attack count updates.
18. F12: verify all modules switch off and temporary state is restored.

## Multiplayer checks

Use only a private server you control. Record whether the server accepts movement and attack actions. Server-side correction, combat restrictions or anti-cheat flags are not evidence of a compilation failure.

## Evidence to record

Record Minecraft version, Loader version, Fabric API version, Java version, selected modules, setting values, logs/latest.log, and any crash report. A successful GitHub Actions run is a compile/packaging check, not proof that every feature behaves correctly in every server environment.
