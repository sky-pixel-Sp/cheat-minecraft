# SkyOP Client 4.1.0

<p align="center">
  <strong>Premium-style Minecraft Java 26.3 Fabric client</strong><br>
  Combat · Movement · Visual · Inventory · Profiles · HUD Studio · Typed Settings
</p>

<p align="center">
  <a href="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml">
    <img alt="Build status" src="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml/badge.svg?branch=feature%2F4.1.0-control-center-ui">
  </a>
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-Java%2026.3-3C8527?logo=minecraft&logoColor=white">
  <img alt="Fabric" src="https://img.shields.io/badge/Mod%20loader-Fabric-DBD0B4?logo=fabric&logoColor=222222">
  <img alt="Java" src="https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white">
  <img alt="Version" src="https://img.shields.io/badge/SkyOP-4.1.0-4BA3D9">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-blue.svg">
</p>

<p align="center">
  <a href="https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml?query=branch%3Afeature%2F4.1.0-control-center-ui">
    <strong>⬇️ DOWNLOAD LATEST SUCCESSFUL BUILD</strong>
  </a>
  &nbsp;·&nbsp;
  <a href="docs/TEST-PLAN.md">Test plan</a>
</p>

---

## SkyOP 4.1.0 Control Center

The 4.1.0 branch adds a multi-page Control Center on top of the existing 62 registered client modules. Gradle, the output JAR filename and embedded Fabric metadata all use version `4.1.0`. This is a UI and theme upgrade; it does not claim the 159-module specification or the 600-feature target is already implemented.

- **62 registered modules:** every module has a concrete runtime factory entry and a category/risk classification.
- **Control Center:** Right Shift opens a dashboard with Overview, Modules, Profiles, Waypoints, Appearance, Diagnostics and Connection pages. Its buttons call the existing module/profile/waypoint/diagnostic managers.
- **Profiles and hotbar:** three config slots, JSON import/export, backup, and three hotbar save/load slots from the same Control Center.
- **Waypoints:** save the current location, view stored coordinates and distance, copy coordinates to clipboard, and clear the local list.
- **Themes:** six specification palettes (Graphite, Midnight, Crimson, Emerald, Mono and Sunset). Historical config IDs are retained so existing settings remain readable.
- **ClickGUI navigation:** Combat, Movement, Visual, World, Player, Inventory, Favorites, Safe and Dangerous. Favorites and risk labels are filters over the registered modules.
- **Quick Panel:** press **O** in a world to open the floating panel. It provides up to eight module toggles, a saved theme cycle, Full GUI access and a Disable All cleanup action.
- **11 additional HUDs:** World Info (dimension/biome/weather), Chunk Info, Performance, Session Timer, Food, Air, Experience, Item Info, Offhand, Block Info and Armor Detail. Each has an independent saved position in HUD Edit.
- **KillAura targeting:** configurable target FOV from 1° to 360° (default 360°), with optional line-of-sight filtering. Normal Minecraft interactions and server authority still apply.
- **Input restoration:** Auto Walk restores the user's physical forward-key state when disabled.
- **Localization:** Indonesian titles and descriptions are present for all 62 registered modules. JUnit regression tests check both the previously missing module translations and full registry coverage.

See the [4.1.0 Control Center implementation notes](docs/UI-CONTROL-CENTER-4.1.md), [feature specification](docs/SKYOP-4.0-FEATURES.md), [600+ feature engineering prompt](docs/PROMPT-600-FEATURES.md) and [manual test plan](docs/TEST-PLAN.md).

The branch CI compiles with Java 25 and Gradle 9.7.1, executes JUnit tests, checks the installable JAR and uploads the artifact. A successful workflow proves build and packaging only; GUI interaction, HUD positioning, world behavior and multiplayer server acceptance still need manual testing.

## Historical 2.3.0 development additions

The `feature/skyop-2.3.0-expansion` branch adds working client-side modules for Auto Hunt, Auto Bridge, Infinite Jump, Jump Boost, Item ESP, Waypoints and Mini Map, plus Night Vision using the local gamma setting. New gameplay settings are persisted and validated, new modules are available in ClickGUI, and V saves the player's current coordinates as a waypoint (up to 32).

A separate Windows password-generator source and GitHub Actions EXE packaging workflow are included under `tools/password-generator/`. This utility generates a local random password; **it is not a login or license server and does not enforce a login gate inside the Minecraft mod**.

The new features remain client-side. They do not grant server permissions, guarantee inventory or combat outcomes, or defeat anti-cheat. The mini map and ESP only use entities already loaded by the client. The Auto Bridge module places a block from the hotbar beneath the player when a valid support block and placeable block are available.

## What is new in 2.2.1

SkyOP 2.2.1 fixes a Minecraft 26.3 SDL keyboard-state crash that could interrupt world loading and mouse capture. A small Mixin bounds-checks keyboard-state lookups so negative or out-of-range indexes are treated as not pressed, while unbound module keys retain Minecraft's normal Unbound semantics. Newly captured and saved keybinds are validated; invalid config values are safely reset to Unbound. The client ships a complete Indonesian/English interface and mouse-capture recovery.

- **Language:** Bahasa Indonesia is the default and English is available from the in-client selector. The selected language is stored in `config/skyop-client.properties`.
- **Module browser:** search, category filtering, Favorites, two-column module cards and per-panel scrolling.
- **Input and HUD editor:** per-module keybind capture, independently draggable HUD panels, six quick color swatches, and a custom RGB hex field (#RRGGBB).
- **Inventory utilities:** Auto Eat, Auto Armor, Auto Tool, Auto Refill, Auto Fish and safe stack consolidation.
- **Profiles:** three JSON profile slots, JSON export/import through files in the config directory, timestamped backups and three hotbar layouts.
- **Extra HUDs:** potion effects, durability, compass/direction, ping, FPS, nearby client-loaded entities and custom text.

Minecraft 26.3 is unobfuscated, so this project uses Fabric Loom's non-remapping plugin and packages the ordinary `jar` output (there is no `remapJar` task for this target). CI checks compilation, metadata, RGB codec unit tests and artifact packaging. It does not replace a real Minecraft launch and in-world test.

## Download and install

1. Open [Latest successful GitHub Actions builds](https://github.com/sky-pixel-Sp/cheat-minecraft/actions/workflows/build.yml?query=branch%3Afeature%2F4.1.0-control-center-ui).
2. Choose the newest run with a green **Success** status.
3. Scroll to **Artifacts** and download **SkyOP-Client-Minecraft-26.3**.
4. Extract the downloaded ZIP archive.
5. Copy the versioned `skyop-client-*.jar` from the selected successful build into the mods folder of your Minecraft 26.3 Fabric instance.
6. Keep Fabric API installed in the same instance, then launch Minecraft.

GitHub Actions artifacts expire after 30 days. Use the latest successful run for the most recent build.

## Requirements

| Component | Version |
|---|---:|
| Minecraft Java Edition | **26.3** |
| Fabric Loader | **0.20.1 or a compatible newer 26.3 build** |
| Fabric API | **0.165.2+26.3** |
| Java runtime | **25 or newer** |
| Fabric Loom (development) | **1.18.4** |
| Gradle (development) | **9.7.1** |

Use Fabric, not Forge or NeoForge. Install the mod in the same game directory as Fabric API.

## Open and use the Control Center + ClickGUI

Press **Right Shift** in-game to open the Control Center. Open **Modules** to reach the full existing ClickGUI and dynamic setting inspector.

| Control | Action |
|---|---|
| Left-click a module card | Enable or disable the module |
| Right-click a module card | Select it without toggling |
| Click the star on a card | Add/remove the module from Favorites |
| Click the search field and type | Filter modules by name or description |
| Mouse wheel over module list | Scroll modules |
| Mouse wheel over settings | Scroll settings |
| Click a setting row | Toggle an option or set a slider |
| Click a module's Keybind row, then press a key | Change its keybind |
| Press Escape while capturing a key | Cancel key capture |
| Click the color swatch in the header | Open the six quick colors or enter a custom #RRGGBB value |
| Click HUD Edit, then drag any visible HUD panel | Move that HUD panel independently; release to save |
| Language control in the footer | Switch between Bahasa Indonesia and English |
| PVE / OP / VISUAL | Load the built-in quick preset |
| Disable All | Disable modules and restore temporary client state |

The keybinds also appear in **Options → Controls → Key Binds → SkyOP Client**. Avoid assigning the same key to multiple modules because Minecraft can dispatch both mappings.

## Modules

### Combat

| Module | Main controls |
|---|---|
| KillAura | Player/mob filters, line of sight, attack range, target priority, aim FOV and rotation interpolation |
| TriggerBot | Player/mob filters and trigger delay |
| AutoClicker | CPS target while holding the attack key; vanilla attack-strength cooldown remains respected |

### Movement

| Module | Main behaviour |
|---|---|
| Auto Sprint | Sprint while moving forward |
| Speed | Configurable horizontal movement multiplier |
| Flight | Local no-gravity movement; jump rises and sneak descends |
| Auto Jump | Automatic jumps while moving forward |
| Step Assist | Jump when forward movement hits a wall |
| Spider | Upward movement while pressing into a wall |
| Auto MLG | Attempts vanilla water-bucket placement when falling toward a valid block |

### Player

| Module | Main behaviour |
|---|---|
| NoFall | Clears the local fall-distance accumulator; server-side rules still apply |
| Fast Break | Configurable Haste I–VII-equivalent local mining speed; Haste VII = 2.4x |
| Auto Totem | Uses a normal inventory SWAP to move a totem into the offhand below a health threshold |

### Visual and HUD

| Module | Main behaviour |
|---|---|
| Entity ESP | Highlights entities already loaded by the client; player/mob filters and range |
| Gamma Boost | Uses maximum normal vanilla gamma and restores the previous value |
| HUD | Active modules, coordinates and FPS |
| Target HUD | Crosshair target, health and distance |
| Keystrokes | W/A/S/D and jump indicators |
| Armor HUD | Health and armor points |
| Speedometer | Horizontal blocks per second |
| CPS HUD | Successful local attack actions per second |
| Potion HUD | Active potion effects and duration |
| Durability HUD | Main-hand durability percentage |
| Compass HUD / Direction HUD | Cardinal direction and yaw |
| Ping HUD | Multiplayer latency, where player-list latency is available |
| FPS HUD | Current client frame rate |
| Nearby Entity Radar | Nearby entities already tracked by the client |
| Text GUI | Custom on-screen message |

Each active HUD panel has its own saved screen position. The editor keeps panels within the screen area. Accent color accepts any six-digit RGB hex value and is stored in the SkyOP configuration.

### Inventory and survival

| Module | Main behaviour |
|---|---|
| Auto Eat | Selects safe food from the hotbar below the configured hunger threshold |
| Auto Armor | Quick-moves armor into empty armor slots; it does not replace worn armor |
| Auto Tool | Selects the fastest tool available in the hotbar for the targeted block, then restores the previous slot |
| Auto Refill | Swaps a matching inventory stack into the selected hotbar slot when its count is low |
| Auto Fish | Reels when the client-visible fishing hook bite flag is set, then recasts |
| Inventory Cleaner | Consolidates matching item stacks; it does not drop or delete items |
| Hotbar Presets | Saves and loads three hotbar layouts using item identifiers |

Inventory changes use normal vanilla container actions. The server validates all inventory transactions. Auto Fish still depends on the server's fishing result and timing.

### Profiles and settings

Select **Profile Manager** under Inventory.

- Save or load slots 1–3.
- Export current settings to config/skyop-client/skyop-profile-export.json.
- To import, place a JSON profile at config/skyop-client/skyop-profile-import.json, then click **Import JSON**.
- **Backup Now** creates a timestamped copy of skyop-client.properties in config/skyop-client/backups/.
- Each profile is stored in config/skyop-client/profiles/.

Select **Hotbar Presets** under Inventory to save or load one of three layouts. A hotbar layout stores item identifiers, not item quantities, NBT contents, enchantments, or exact stack arrangements. Verify the hotbar after applying a layout.

The central settings schema declares numeric defaults and valid ranges. Settings loaded from disk are clamped to their documented limits.

## Language

SkyOP supports Bahasa Indonesia (default) and English. Existing configurations that contain `language=id` remain Indonesian; invalid or missing values fall back to Indonesian. Switching languages does not change Minecraft's global language setting.

## Configuration locations

| File/folder | Purpose |
|---|---|
| .minecraft/config/skyop-client.properties | Module states, settings, language, favorites, keybinds and HUD preferences |
| .minecraft/config/skyop-client/profiles/ | JSON profiles and hotbar layouts |
| .minecraft/config/skyop-client/skyop-profile-export.json | Current profile exported for sharing |
| .minecraft/config/skyop-client/skyop-profile-import.json | JSON file read by Import JSON |
| .minecraft/config/skyop-client/backups/ | Timestamped property-file backups |

Back up the config folder before experimenting with imported profiles.

## Build from source on Windows

1. Install a 64-bit JDK 25.
2. Confirm java -version reports Java 25 or newer.
3. Clone or download this repository.
4. Run BUILD-WINDOWS.bat.
5. Find `skyop-client-4.1.0.jar` under `build/libs/`.

The first build needs an internet connection to download Gradle and dependencies.

## Compatibility and safety

- SkyOP is a **client-side mod**. It does not grant operator permissions and is not a server plugin.
- Movement and combat actions may be rejected, corrected or flagged by a multiplayer server. Respect the server's rules.
- The project does not claim to bypass anti-cheat systems or server authority.
- Use automated combat or movement only in single-player or a private server where you are allowed to do so.
- ESP and radar only work with entities already received/tracked by your client. They do not reveal unloaded or untracked entities.
- Auto Armor, Auto Refill, Inventory Cleaner and Hotbar Presets use normal vanilla container actions; the server can reject a transaction.
- The HUD ping value is only available when the client has player-list latency information.
- A successful CI build proves compilation and packaging, not that every module works in every world or on every server.

## Testing

See [docs/TEST-PLAN.md](docs/TEST-PLAN.md) for the complete launch, GUI, inventory, HUD and profile checklist.

## Contributing

Bug reports should include Minecraft, Fabric Loader, Fabric API and Java versions; selected modules/settings; reproduction steps; logs/latest.log; and any crash report. Never share account credentials or private server details.

## License

Released under the **MIT License**. See LICENSE.


## Phase 1 module architecture

`SkyOPClient` is now a bootstrap-only Fabric entrypoint. It delegates startup to `SkyOPCore`. Runtime modules are registered as concrete subclasses of `SkyOPModule` through `SkyOPModuleManager`, split into movement, combat, visual/HUD and inventory module classes. `SkyOPState` owns shared persisted values and `SkyOPTelemetry` owns speed/CPS sampling.

The ClickGUI inspector obtains its rows from `SkyOPModuleManager.settings(module)`. Boolean, numeric, mode, multi-select, color and keybind settings render and edit through their typed `SkyOPSetting` instances; the inspector does not contain a per-module settings switch. Setting labels resolve their translation keys at render time so a language switch also updates an already-open GUI.

Bahasa Indonesia is the default. The English and Indonesian in-client dictionaries currently contain the same 232 keys. The Minecraft language resources `id_id.json` and `en_us.json` contain keybinding translations.
