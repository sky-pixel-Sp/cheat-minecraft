# SkyOP Client 2.1.0 test plan

## Automated CI checks

GitHub Actions builds with Java 25 and Gradle 9.7.1 using Fabric Loom 1.18.4. It runs clean build (including JUnit tests for RGB hex parsing and formatting), verifies the remapped JAR contains the client entrypoint and Fabric metadata, and uploads the installable JAR.

A green CI result proves compilation and packaging. It does not prove an in-game launch or multiplayer acceptance.

## Launch and compatibility

1. Create a separate Minecraft Java 26.3 Fabric instance.
2. Install Fabric Loader 0.20.1 and Fabric API 0.165.2+26.3.
3. Add skyop-client-2.1.0.jar to that instance's mods folder.
4. Launch to the title screen and confirm no mixin or resource errors appear.
5. Join a local single-player world; capture logs/latest.log if a failure occurs.
6. Confirm another mod using mixins does not conflict with skyop.mixins.json.

## Language

1. Start with no SkyOP config file; verify English is selected.
2. Cycle through English, Bahasa Indonesia, Español, Português, Français, Chinese and Japanese.
3. Verify category names, module names, common setting names and the language label update.
4. Close/reopen Minecraft and verify the selected language is persisted.

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
