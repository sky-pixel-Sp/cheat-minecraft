# SkyOP Client test plan

## Automated build checks

The GitHub Actions workflow runs `gradle clean build` with Java 25 and Gradle 9.7.1. It then verifies that the remapped JAR contains the client entrypoint class and `fabric.mod.json`, and uploads the JAR as a workflow artifact.

## Manual in-game checks

Run these checks in a Minecraft 26.3 + Fabric Loader 0.20.1 instance with Fabric API 0.165.2+26.3 and SkyOP installed.

1. Launch with Fabric API and SkyOP installed; confirm the title screen and world load without a crash.
2. Open a local test world; confirm the SkyOP HUD appears.
3. Toggle each module with F6–F10 and verify the status overlay changes.
4. Verify Auto Sprint while moving forward; verify it stops activating while sneak is held.
5. Verify Speed Boost changes horizontal movement only while a movement key is held.
6. Spawn a hostile mob and verify KillAura only targets a mob in front and within range; verify passive mobs and other players are not targeted.
7. Verify Entity ESP outlines nearby living entities and disabling ESP restores their previous glow state.
8. Verify NoFall in a local test world. Treat remote-server results as server-dependent.
9. Press F12 and verify all modules switch off and ESP state is restored.
10. Check `logs/latest.log` and the crash-reports folder after the test session.

Record the Minecraft version, Loader version, Fabric API version, Java version and any crash report when reporting a failure.
