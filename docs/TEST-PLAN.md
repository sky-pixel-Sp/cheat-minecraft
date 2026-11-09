# SkyOP Client 2.3.0 development test plan

## Automated CI checks

GitHub Actions builds with Java 25 and Gradle 9.7.1 using the non-remapping Fabric Loom 1.18.4 plugin required for unobfuscated Minecraft 26.1+. It runs clean build (including JUnit tests for RGB hex parsing, formatting and SDL keyboard-state index boundaries), verifies the installable JAR contains the client entrypoint, both client mixins and Fabric metadata, and uploads the JAR.

A green CI result proves compilation and packaging. It does not prove an in-game launch or multiplayer acceptance.

## Launch and compatibility

1. Create a separate Minecraft Java 26.3 Fabric instance.
2. Install Fabric Loader 0.20.1 and Fabric API 0.165.2+26.3.
3. Add skyop-client-2.3.0-dev.jar to that instance's mods folder.
4. Launch to the title screen and confirm no mixin or resource errors appear.
5. Join a local single-player world and wait for the loading screen to close; verify no IndexOutOfBoundsException occurs when Minecraft restores all key states. Confirm the InputConstantsMixin applies to the Minecraft 26.3 `isKeyDown(int)` method and does not report a missing injection target.
6. Repeat with SkyOP module keybinds left unassigned; verify unbound keys do not crash world loading or mouse capture; the vanilla UNKNOWN key uses SDL scancode 0, and invalid negative indexes must be rejected before ByteBuffer access.
7. Open ClickGUI with Right Shift, close it with Escape and verify the mouse is re-captured for camera movement without requiring another click.
8. Open ClickGUI, assign a module keybind, restart the client and verify the saved scancode is restored; invalid negative/out-of-range values must safely become Unbound.
7. Close ClickGUI while HUD editor drag mode is active; verify no stale drag state remains and mouse camera control resumes.
8. While chat or another screen is open, verify the mod does not force mouse capture.
6. Confirm another mod using mixins does not conflict with skyop.mixins.json.

## Language

1. Start with no SkyOP config file; verify the entire client interface is English.
2. Load a legacy config containing `language=id`; verify it is backed up and migrated to `language=en`.
3. Verify all six categories, module titles and descriptions, setting labels, keybind controls, profile/hotbar controls and HUD hints are English.
4. Open ClickGUI, verify the English labels remain stable while the screen is resized, then close and reopen Minecraft.
5. Verify the persisted config contains `language=en` and `configVersion=2`.

## ClickGUI and core

1. Press Right Shift and verify the GUI opens.
2. Verify Combat, Movement, Visual, Player, Inventory and Favorites categories.
3. Search by a module name and a description term.
4. Add/remove Favorites using the star; verify the Favorites category updates.
5. Scroll the module panel and the settings panel independently.
6. Hover module cards and check the animated description tooltip.
7. Click a module's Keybind row; press a different key; verify that it toggles the selected module. Press Escape to cancel capture.
8. Use keybinds from Options → Controls and verify there are no duplicate assignments.
9. Click the color swatch and choose each quick-palette color; verify HUD accent changes and persists.
10. Enter a custom #RRGGBB value, apply it, and verify the HUD accent changes. Also check #RGB shorthand and invalid input.
11. Enable several HUD panels, toggle HUD Edit, and drag each visible preview independently; release and reopen the GUI to verify positions persist.
12. Edit the Text GUI label by clicking its text row, typing, and pressing Enter.
12. Use Disable All; verify temporary ESP glow, gamma and flight state are restored.

## Inventory and survival

Run in a disposable local world with a copy of the inventory.

1. Auto Eat: set threshold; lower hunger; keep safe food in hotbar; verify it eats until full, restores the selected slot and stops if the player manually changes slots.
2. Confirm unsafe food is not selected by Auto Eat.
3. Auto Armor: leave an armor slot empty and place compatible armor in main inventory; verify it moves into the empty slot. Confirm equipped armor is not replaced.
4. Auto Tool: hold attack on several block types with tools in hotbar; verify the fastest hotbar tool is selected and previous selection restores after release.
5. Auto Refill: set threshold and hold a low-count stack in selected hotbar slot with a matching stack in main inventory; verify swap and count.
6. Inventory Cleaner: place incomplete matching stacks in main inventory and hotbar; verify stack consolidation without dropping/deleting items.
7. Auto Fish: use a rod in open water; verify it reels on a client-visible bite and recasts. Server timing and fish outcomes may vary.
8. Hotbar Presets: save slots 1–3; change hotbar; load each layout and inspect slots.
9. Confirm inventory utilities do nothing when the player inventory screen/container is not the player inventory menu.

## Profiles and settings

1. Save distinct settings into profile slots 1, 2 and 3.
2. Change settings and load each slot; verify values are restored.
3. Export JSON; inspect the exported file in the SkyOP config folder.
4. Copy an exported valid JSON file to skyop-profile-import.json; import and verify settings.
5. Verify import creates a backup before applying the profile.
6. Click Backup Now and verify a timestamped properties backup is created.
7. Try an invalid/empty import file; verify a clear error is shown and existing settings are not silently reset.
8. Verify settings are clamped to the ranges in SkyOPSettingsSchema.

## HUD and telemetry

1. Potion HUD: apply effects and verify names/durations update.
2. Durability HUD: hold a damageable item and verify percentage changes after use.
3. Compass and Direction HUD: rotate toward all four cardinal directions and verify labels.
4. Ping HUD: check on a multiplayer server; local/single-player may show unavailable.
5. FPS HUD: compare display with the title screen/F3 frame rate.
6. Radar: spawn entities within 32 blocks and verify only client-loaded entities appear.
7. Custom Text GUI: save a custom string and verify it appears in the overlay.
8. Speedometer: walk, sprint and stop; verify speed updates and returns near zero.
9. CPS HUD: attack a local target and verify the rolling attack rate.

## Existing modules

1. Auto Sprint: move forward; sprint engages unless sneak is held.
2. Speed: adjust multiplier and compare movement in a local world.
3. Flight: jump rises, sneak descends and disabling restores the original no-gravity flag.
4. Auto Jump: hold forward and verify jump interval.
5. Step Assist: approach a one-block obstacle and verify jump behaviour.
6. Spider: press into a wall and verify local vertical motion.
7. NoFall: verify local fall-distance behaviour; do not assume a server accepts it.
8. Entity ESP: verify player/mob filters and range; ensure original glow is restored on disable and range exit.
9. Gamma Boost: verify gamma is restored when disabled.
10. KillAura: test hostile mobs first in a local world; verify target filter, FOV, range, priority, rotation and cooldown.
11. TriggerBot: aim at a configured target and verify delay.
12. AutoClicker: hold attack and verify vanilla attack cooldown remains respected.
13. Target HUD, Keystrokes HUD and Armor HUD: verify target data, live key state, health and armor values.
14. F12: verify all modules disable and state restoration.

## Multiplayer checks

Only use a private server you control and are allowed to test. Server-side corrections, inventory rejections, combat restrictions and anti-cheat flags are server behaviour, not evidence of compile failure.

## Evidence to record

Record game, Loader, API and Java versions; module states/settings; video or screenshots for GUI; logs/latest.log; crash report; profile JSON and backup files; and the GitHub Actions run URL.

## SkyOP 2.3 development additions

Run these checks in a disposable single-player world before using a private server:

1. Auto Hunt: spawn several hostile mobs within the configured range; verify it selects the nearest visible mob, does not rotate the camera and respects attack cooldown.
2. Auto Bridge: place a supported gap in front of the player, keep placeable blocks in the hotbar, walk toward the gap and verify blocks are placed beneath the player. Check that no block is placed when there is no valid support or no hotbar BlockItem.
3. Infinite Jump: jump, release the jump key, then press it again in midair; verify one additional upward impulse per fresh press rather than continuous flight.
4. Jump Boost: compare the jump impulse at 1.0x and 3.0x, then verify the value persists after restart.
5. Item ESP: drop items in range and verify their glow is enabled only when Entity ESP or Item ESP is active; verify original glow state is restored after disabling both.
6. Waypoints: press V in a world; verify a coordinate entry appears in the Waypoints HUD and is persisted in config/skyop-client/waypoints.properties. Restart and verify it reloads. Test the 32-waypoint limit.
7. Mini Map: spawn a player, hostile mob and dropped item within 48 blocks; verify the HUD dots update as entities move and disappear when no longer client-loaded.
8. Night Vision: enable in a dark local area, then disable and verify the original gamma value is restored. Test toggling Gamma Boost and Night Vision in both orders.
9. New settings: write out-of-range values for autoHuntRange, jumpBoostMultiplier and bridgeDelayTicks; verify values are clamped or reset to safe defaults.
10. Password EXE: confirm the Windows workflow runs py_compile, builds SkyOP-Password-Generator.exe, and uploads the artifact. Test generation with each character group enabled/disabled, copy, save, and ambiguous-character exclusion.
11. Disable All: enable all new modules, press F12, and confirm temporary glow/gamma state is restored and movement automation stops.

A green CI build does not replace these in-game checks.

The feature branch CI also runs on branch pushes so every incremental fix is compiled and the unobfuscated distributable is checked.


## Phase 1 architecture and configuration regression checks

1. Start with no SkyOP config; verify defaults load and the first save writes `configVersion=2` and `language=en`.
2. Copy a legacy v0 config with existing module toggles, keybinds and numeric settings; start SkyOP and verify a backup is created before migration and all existing values are preserved.
3. Start with a v1 config; verify it migrates to v2 and the backup contains the exact pre-migration file.
4. Put an invalid Unicode escape in the Properties file; verify the original file is backed up, safe defaults are loaded, and the client does not crash.
5. Put malformed booleans, invalid numbers, NaN and out-of-range numbers in the config; verify invalid values fall back and numeric values respect the central schema.
6. Put a configVersion higher than the client version; verify SkyOP reports a load error and does not overwrite the newer file.
7. Enable Flight, ESP, Gamma and Auto Eat; disconnect while active; verify temporary gravity, glow, gamma and use-key state are restored.
8. Enable modules, change dimension, and respawn; verify no previous-world glow or flight state remains and active modules can initialize in the new context.
9. Rebind several modules, save/restart, and verify all keybinds round-trip; invalid scancodes return to their module default or Unbound.
10. Test all module categories and presets; verify the tick order remains Movement → NoFall → Visual → Combat → Inventory, and Disable All restores temporary state.
11. Resize the window repeatedly while ClickGUI is open; repeat at GUI scale 1 and 4, and at 854x480; record clipping/overlap for the upcoming compact-UI phase.
12. Open ClickGUI, verify all visible labels are English, and confirm legacy language config migration does not introduce an unsupported language option.
13. Import a profile with invalid numeric values; verify validated values are applied, the pre-import config backup exists, and the client remains responsive.
14. Switch worlds while the GUI is open, close it, and verify the client retains mouse capture only during active gameplay.

CI coverage for Phase 1 includes ConfigManager migration/corruption tests and typed-setting serialization tests. Manual Minecraft checks remain required for lifecycle and in-game behavior.
