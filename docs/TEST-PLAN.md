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
9. Close ClickGUI while HUD editor drag mode is active; verify no stale drag state remains and mouse camera control resumes.
10. While chat or another screen is open, verify the mod does not force mouse capture.
11. Confirm another mod using mixins does not conflict with skyop.mixins.json.

## Language

1. Start with no SkyOP config file; verify Bahasa Indonesia is selected by default.
2. Load a legacy config containing `language=id`; verify it remains Indonesian while configVersion migrates to 2.
3. Select English from the in-client language control; verify categories, module titles/descriptions, setting labels, keybind labels, profile/hotbar controls and HUD hints change.
4. Change language while ClickGUI remains open; verify setting titles and descriptions update without reopening the screen.
5. Restart Minecraft; verify the chosen `language=id` or `language=en` setting is persisted.

## ClickGUI and core

1. Press Right Shift and verify the GUI opens.
2. Verify Combat, Movement, Visual, World, Player, Inventory, Favorites, Safe and Dangerous categories.
3. Verify Safe and Dangerous are filters over registered modules and each module inspector shows the correct risk label.
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
13. Use Disable All; verify temporary ESP glow, gamma and flight state are restored.

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
9. Fast Break: set Haste level to VII, verify the client multiplier is 2.4x, mine several breakable blocks, then disable the module and verify vanilla mining speed is restored. Confirm unbreakable blocks remain unbreakable and record any server-side correction; do not treat client prediction as proof of server acceptance.
10. Auto Totem: in a disposable world, keep a totem in inventory and leave offhand empty; lower health below the configured threshold and verify the normal inventory SWAP action equips the offhand. Confirm it does not replace an existing offhand item or duplicate/delete stacks.
11. Auto MLG: in a disposable world, keep a water bucket in the hotbar, fall toward a reachable solid block while aiming at its top/side face, and verify the module attempts vanilla item-use. Verify it does not trigger on ground, while rising, without a block hit below, or without a water bucket. Disable while airborne and confirm the prior hotbar slot is restored.
9. New settings: write out-of-range values for autoHuntRange, jumpBoostMultiplier and bridgeDelayTicks; verify values are clamped or reset to safe defaults.
10. Password EXE: confirm the Windows workflow runs py_compile, builds SkyOP-Password-Generator.exe, and uploads the artifact. Test generation with each character group enabled/disabled, copy, save, and ambiguous-character exclusion.
11. Disable All: enable all new modules, press F12, and confirm temporary glow/gamma state is restored and movement automation stops.


## GUI scale and compact layout acceptance

Run these checks in Minecraft 26.3 at GUI Scale **1, 2, 3 and 4**. Repeat after resizing the game window while ClickGUI remains open.

1. At GUI Scale 4 on a 1920x1080 display (approximately 480x270 GUI units), verify the panel stays fully inside the screen and never forces a 680x450 minimum.
2. Verify the compact category rail uses six icon buttons (Combat, Movement, Visual, Player, Inventory and Favorites); every icon is clickable and changes the module category.
3. Verify the module browser uses the available width, two columns when at least 320 GUI units are available, and a single column below that threshold.
4. Left-click a module to toggle it. Right-click a module to open the inspector overlay; close the overlay with its X control and confirm the module browser remains at the same scroll position.
5. Open the inspector overlay and test Boolean, Number, Mode, Multi, Color and Keybind settings. Verify each edit updates the live registered setting and persists after restart.
6. Scroll the module browser and inspector independently. Confirm neither scroll region clips the footer or the category rail.
7. Verify the compact header exposes the collapsed color swatch and HUD-edit controls. Open the color picker, choose a swatch, enter a custom RGB value, and close it without drawing outside the panel.
8. Verify compact footer controls for PVE, OP, VISUAL, language switching and Disable All are clickable and do not overlap.
9. At 854x480 GUI units, verify the regular two-panel layout remains intact, all four panel edges stay on-screen, and the inspector does not overlap the module browser.
10. At GUI Scale 1 and 2, verify the regular inspector remains visible and its registered setting rows, Profile Manager/Hotbar Presets actions, HUD editor and color picker retain their functions.
11. Resize continuously across the compact/wide breakpoint and verify the layout reflows without a stuck inspector overlay, lost selected module, or negative scroll position.
12. Capture screenshots for 480x270 and 854x480, and attach the latest.log file to the manual test report.

A green CI build does not replace these in-game checks.

The feature branch CI also runs on branch pushes so every incremental fix is compiled and the unobfuscated distributable is checked.


## Phase 1 architecture and configuration regression checks

1. Start with no SkyOP config; verify defaults load and the first save writes `configVersion=2` and `language=id`.
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
12. Open ClickGUI, switch between Indonesian and English while the screen remains open, and verify every title, description, setting row, keybind, profile action and HUD hint updates.
13. Verify every module's inspector rows come from its registered `SkyOPSetting<?>` list; toggle Boolean, drag Number, cycle Mode, select Multi chips, edit Color and rebind Keybind settings.
14. Verify Profile Manager, Hotbar Presets and Custom Text HUD expose their actions/text input as registered setting rows in the same generic inspector; confirm the keybind row remains registry-backed.
15. Verify all module categories and module titles/descriptions are translated in both dictionaries, with no unresolved translation keys.
16. Import a profile with invalid numeric values; verify validated values are applied, the pre-import config backup exists, and the client remains responsive.
17. Switch worlds while the GUI is open, close it, and verify the client retains mouse capture only during active gameplay.

CI coverage for Phase 1 includes ConfigManager migration/corruption tests and typed-setting serialization tests. Manual Minecraft checks remain required for lifecycle and in-game behavior.


## Modular runtime checks

1. Confirm `SkyOPClient.java` contains only the Fabric bootstrap delegation.
2. Toggle each movement module and verify it is invoked through its own concrete module class.
3. Toggle KillAura, TriggerBot, AutoClicker and Auto Hunt independently; verify cooldown state resets on disable.
4. Toggle Entity ESP, Item ESP, Gamma Boost and Night Vision in combinations; verify shared glow/gamma state restores only when the final related module is disabled.
5. Test Auto Eat, Auto Armor, Auto Tool, Auto Refill, Inventory Cleaner and Auto Fish separately; verify each has a concrete tick/disable lifecycle and no inventory state remains after disable.
6. Open and close screens while inventory modules are active; verify use-key and hotbar selection are restored.
7. Disconnect and reconnect while Flight, ESP, Gamma and Auto Eat are active; verify gravity, entity glow, gamma and use-key state restore.
8. Change dimension and respawn; verify the module registry synchronizes active modules with the new player/world context.
9. Search for a module, favorite it, and verify the Favorites category and module counts update.
10. Verify the ClickGUI inspector has no per-module settings switch and uses the registered typed setting instance as the source of truth.

CI covers compilation, JUnit tests for legacy utilities, config migration, typed setting validation/serialization and dynamic bilingual setting labels. Manual Minecraft 26.3 launch/gameplay checks remain mandatory.


## SkyOP 3.0 review feedback — manual Minecraft 26.3 checks

These checks are required in a disposable singleplayer world and a private test server. CI passing is not evidence that the interactions work in-game.

### Auto Totem
1. Put a totem in each hotbar position in turn (slots 1–9), leave the offhand empty, lower health below the configured threshold, and verify it moves to the offhand.
2. Repeat with the offhand occupied by a shield or another item; verify Auto Totem does not overwrite it.
3. Wear a helmet and keep a totem in inventory; verify the armor menu slots are never mistaken for player-inventory source slots and the helmet remains equipped.
4. Repeat with the totem in main-inventory slots 10–36. Verify no stack is duplicated, deleted, or moved from armor/offhand.
5. Toggle Auto Totem off during its cooldown, then disconnect/reconnect; verify no delayed swap is issued after disable or world teardown.

### Auto MLG
1. Fall toward a reachable solid block with a water bucket in hotbar. Test while already looking down and while looking forward/up; verify the temporary downward pitch finds the block below and is restored immediately after both interaction paths.
2. Repeat with no water bucket; verify no item-use is sent and pitch/hotbar remain unchanged.
3. Test with no block below, while on ground, while rising, and below the configured fall-distance threshold; verify no placement attempt.
4. Toggle Auto MLG off during a fall and disconnect while the module is enabled; verify pitch and selected hotbar slot are restored and no subsequent attempt occurs.
5. Repeat on the private test server and record the server response; a denied/reverted bucket action is a server-authority result, not proof that the client-side API call failed.

### Fast Break — integrated server
1. In singleplayer, enable Fast Break VII and compare local client mining and integrated-server break progression for the same player UUID.
2. Verify a different UUID / remote multiplayer server player receives no multiplier.
3. Disable Fast Break and confirm vanilla mining speed is restored in both local and integrated-server paths.

Record Minecraft 26.3, Fabric Loader/API versions, GUI scale, client log, server log, and a short video for each check. Do not mark any item as game-tested until evidence is captured.


## Premium UI, theme and HUD regression checks

1. Open ClickGUI with Right Shift, open the HUD color popover, and cycle all six UI themes. Verify panel, hover, text and accent colors change while the separate HUD accent swatch remains unchanged.
2. Select a theme, close Minecraft, relaunch and verify the theme is restored from `uiTheme`.
3. Press **O** while in a world. Verify the floating Quick Panel opens without pausing the world and shows Auto Totem, Auto MLG, Fast Break, Auto Sprint, Entity ESP and Auto Eat.
4. Toggle each Quick Panel tile and verify the matching module registry state and ClickGUI state update together.
5. Drag the Quick Panel title bar to all four screen corners, release, close and reopen it. Verify the panel remains inside the viewport and its location persists after restart.
6. Enable Compass HUD and Direction HUD together. Verify both widgets render independently, Direction displays block X/Z coordinates, and moving either widget in HUD Edit does not move the other.
7. Enable several modules so the main HUD lists multiple rows. Verify the coordinates and FPS footer are inside the HUD background and the complete widget remains within the screen.
8. Apply more than seven potion effects. Verify the Potion HUD renders at most seven rows and its backdrop remains sized to the visible content.
9. At GUI widths below 20 units and at GUI scale 4, verify module descriptions/tooltips do not throw substring/index exceptions.
10. Click the padding left of the first Multi-setting chip; verify no option changes. Click each chip itself and verify selection still toggles correctly.
11. Open the color popover and click outside it. Verify the popover closes and the selected underlying control receives the new click normally.

## Utility module pack — singleplayer and multiplayer

Run all tests first in a disposable singleplayer world, then on a private multiplayer server where the operator has authorized client utility testing. Client-side state must be restored when a module is disabled, when a GUI opens, on respawn, and on world disconnect.

1. **Auto Walk:** enable while in a world and verify the normal forward input is held. Open inventory/ClickGUI and verify input is released. Disable the module and verify forward input returns to the physical keyboard state after the next input poll. Check that a mounted, dead or spectator player is not forced to walk.
2. **Zoom:** record the user's configured FOV, enable Zoom, verify the configured FOV (10–70) is applied, then disable and verify the original FOV is restored. Repeat after dimension change, disconnect and reconnect.
3. **Container Finder:** place a chest, trapped chest, barrel and ender chest within configured range. Verify only containers in chunks already loaded by the client are listed, sorted by distance, with their coordinates. Walk across a chunk boundary and verify the list refreshes; disable and verify the overlay clears. Do not interpret absent markers as evidence that unloaded or hidden chunks contain no containers.
4. **Auto Respawn:** enable, die in a disposable world and verify the client issues the normal vanilla respawn request. Repeat on the private server and record whether its rules accept the request. Confirm this module does not bypass death timers, permissions or server restrictions.
5. **Low Health Alert:** configure a threshold, cross below it and verify one notification per low-health interval. Heal above the threshold and repeat; verify the next crossing generates a new notification. Confirm no repeated chat spam while remaining below threshold.
6. **Armor Alert:** equip damageable armor and reduce durability below the configured threshold. Verify one notification while any equipped piece remains below threshold, recovery/replacement resets the alert, and empty/non-damageable slots are ignored.
7. Verify all four new numeric settings reject NaN/infinite values and clamp out-of-range values through SkyOPSettingsSchemaTest.
8. Switch between singleplayer and multiplayer while the new modules are enabled. Verify Zoom, Auto Walk and Container Finder restore/clear their client-side state on world change. Verify server-authoritative actions may be denied and do not cause the mod to claim success or bypass the server.

CI passing does not replace these in-game checks. Record Minecraft/Fabric versions, GUI scale, server type, latest.log, and a short capture for each module.

## Quick Panel

1. Press O while in a world; verify the panel opens without pausing the game.
2. Toggle each visible quick module and verify its status changes.
3. Resize the scaled GUI viewport; verify the panel and card grid stay inside the screen and the visible module count adapts.
4. Click the theme control; verify the UI theme cycles and persists.
5. Click Full GUI; verify the full ClickGUI opens.
6. Click Disable All; verify every module is disabled and temporary flight, zoom, gamma, item-use and movement state is restored.
7. Drag the title bar, release, reopen and verify the panel position persists.

## SkyOP 4.0 expansion checks

1. Set KillAura field of view to 360 degrees. In a controlled local test world, confirm the target selector can choose loaded entities in front, behind, above and below within configured range.
2. Disable the line-of-sight requirement and confirm the client scanner retains loaded entities behind blocks. Do not treat this as proof that a multiplayer server will accept attacks through blocks.
3. Toggle World Info HUD; verify dimension, biome, world clock and weather update with the world.
4. Toggle Chunk Info HUD; walk across chunk boundaries and confirm chunk and local block coordinates update.
5. Toggle Performance HUD; verify FPS and JVM heap values update without a placeholder/static value.
6. Toggle Session Timer; confirm elapsed time increments and resets on world/player context change.
7. Open HUD Edit and drag each new widget; close/reopen the GUI and confirm saved positions persist.
8. Check compact category navigation at 320x200 and 480x270; all nine category/filter rows must be visible and non-overlapping.
9. Review the risk label on each module. Dangerous is a warning, not a compatibility guarantee or permission to violate server rules.

## Additional player/item HUD verification

1. Food HUD: change hunger and saturation by eating/sprinting; verify both values update.
2. Air HUD: submerge and surface; verify current air ticks decrease and recover.
3. Experience HUD: collect XP and level up; verify level and percentage update.
4. Item Info HUD: select an empty hand, stackable item and damageable tool; verify name, count and durability.
5. Offhand HUD: swap an item and a totem/shield into offhand; verify name and count.
6. Block Info HUD: aim at different blocks and air; verify block name/coordinates and no-block state.
7. Armor Detail HUD: equip and remove armor pieces, then take durability damage; verify all four slots and percentages.
8. Drag all seven widgets in HUD Edit and reopen the GUI to verify their saved positions.
