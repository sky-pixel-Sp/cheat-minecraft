package id.skyop.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-client language selection, independent from Minecraft's global language.
 * English is deliberately the default and fallback language.
 */
public final class SkyOPLanguage {
    public record Language(String code, String displayName) { }

    private static final List<Language> LANGUAGES = List.of(new Language("en", "English"));
    private static final Map<String, String> EN = new LinkedHashMap<>();
    private static String languageCode = "en";

    static {
        put("gui.brand", "SKYOP");
        put("gui.client", "CLIENT / 26.3");
        put("gui.control_center", "MODULE CONTROL CENTER");
        put("gui.subtitle", "Profiles • Modules • Live settings");
        put("gui.ready", "CLIENT READY");
        put("gui.categories", "CATEGORIES");
        put("gui.active_modules", "ACTIVE MODULES");
        put("gui.inspector", "MODULE INSPECTOR");
        put("gui.select_module", "Select a module");
        put("gui.modules", "MODULES");
        put("gui.left_toggle_right_inspect", "Left click: toggle  •  Right click: inspect");
        put("gui.no_extra_settings", "No extra settings");
        put("gui.enabled", "ENABLED");
        put("gui.disabled", "DISABLED");
        put("gui.quick_profiles", "QUICK PROFILES");
        put("gui.language", "LANGUAGE");
        put("gui.disable_all", "DISABLE ALL");
        put("gui.client_ready", "CLIENT READY");
        put("gui.no_target", "NO TARGET");
        put("gui.active", "active");
        put("gui.save", "SAVE");
        put("gui.load", "LOAD");
        put("gui.favorites", "FAVORITES");
        put("gui.search", "SEARCH MODULES");
        put("gui.hud_color", "HUD COLOR");
        put("gui.apply", "APPLY");
        put("gui.hud_layout", "HUD LAYOUT");
        put("gui.drag_hud", "Drag each panel to reposition");

        put("gui.hud_edit", "HUD EDIT");
        put("gui.hud_edit_on", "HUD EDIT ON");
        put("gui.type_to_filter", "type to filter");
        put("gui.search_prefix", "SEARCH");
        put("gui.no_matching_modules", "No matching modules");
        put("gui.on", "ON");
        put("gui.off", "OFF");
        put("gui.press_a_key", "PRESS A KEY...");
        put("gui.unbound", "UNBOUND");
        put("gui.keybind", "KEYBIND");
        put("gui.edit_text", "EDIT TEXT");
        put("gui.custom_hud_text", "CUSTOM HUD TEXT");
        put("gui.edit_text_help", "Click this line, then type. Enter to finish.");
        put("gui.configuration_profiles", "CONFIGURATION PROFILES");
        put("gui.export_json", "EXPORT JSON");
        put("gui.import_json", "IMPORT JSON");
        put("gui.backup_now", "BACKUP NOW");
        put("gui.hotbar_layout_slots", "HOTBAR LAYOUT SLOTS");
        put("gui.save_hotbar", "SAVE HOTBAR");
        put("gui.load_hotbar", "LOAD HOTBAR");
        put("gui.requires", "REQUIRES");
        put("gui.potion_effects", "Potion effects");
        put("gui.active_effects_duration", "Active effects and their remaining duration");
        put("gui.main_hand_item", "Main-hand item");
        put("gui.remaining_durability", "Remaining durability percentage");
        put("gui.direction", "Direction");
        put("gui.cardinal_heading", "Cardinal heading based on player yaw");
        put("gui.latency", "Latency");
        put("gui.current_multiplayer_ping", "Current multiplayer ping; local worlds show unavailable");
        put("gui.frame_rate", "Frame rate");
        put("gui.current_fps", "Current client frames per second");
        put("gui.loaded_entities", "Loaded entities");
        put("gui.nearby_loaded_entities", "Nearby entities already tracked by your client");
        put("gui.armor_policy", "Armor policy");
        put("gui.only_empty_armor_slots", "Only fills empty armor slots; never replaces worn armor");
        put("gui.tool_selection", "Tool selection");
        put("gui.fastest_tool", "Selects the fastest hotbar tool for the targeted block");
        put("gui.fishing_automation", "Fishing automation");
        put("gui.reel_and_recast", "Reels on bite and recasts; server rules still apply");
        put("gui.safe_cleanup", "Safe cleanup");
        put("gui.stack_consolidation", "Consolidates matching stacks; never drops or destroys items");
        put("gui.three_layouts", "Three layouts");
        put("gui.save_restore_hotbar", "Save and restore hotbar item layouts");
        put("gui.json_profiles", "JSON profiles");
        put("gui.profile_slots_backup_restore", "Three slots, JSON import/export, backups and restore");
        put("gui.profile_action_completed", "Profile action completed");
        put("gui.profile_error", "Profile error");
        put("gui.hotbar_error", "Hotbar error");
        put("gui.saved_hotbar", "Saved hotbar");
        put("gui.loaded_hotbar", "Loaded hotbar");
        put("gui.hud_color_updated", "HUD color updated");
        put("gui.enter_hex_digits", "Enter 6 hex digits");
        put("gui.keybind_updated", "Keybind updated");
        put("gui.hud_text_edit_hint", "Click this line, then type. Enter to finish.");
        put("gui.profile_saved", "Profile saved");
        put("gui.profile_loaded", "Profile loaded");
        put("gui.main_hud", "Main HUD");
        put("gui.compass_direction", "Compass / Direction");
        put("gui.drag_to_move", "DRAG TO MOVE");
        put("gui.info", "INFO");


        put("category.combat", "Combat");
        put("category.movement", "Movement");
        put("category.visual", "Visual");
        put("category.player", "Player");

        put("module.KILL_AURA.title", "KillAura");
        put("module.KILL_AURA.description", "Prioritises a nearby configured target.");
        put("module.TRIGGER_BOT.title", "TriggerBot");
        put("module.TRIGGER_BOT.description", "Attacks a valid target under the crosshair.");
        put("module.AUTO_CLICKER.title", "AutoClicker");
        put("module.AUTO_CLICKER.description", "Repeats attacks while the attack key is held.");
        put("module.AUTO_HUNT.title", "Auto Hunt");
        put("module.AUTO_HUNT.description", "Automatically attacks the nearest loaded hostile mob without camera movement.");
        put("module.AUTO_BRIDGE.title", "Auto Bridge");
        put("module.AUTO_BRIDGE.description", "Places a selected hotbar block beneath the player while moving.");
        put("module.INFINITE_JUMP.title", "Infinite Jump");
        put("module.INFINITE_JUMP.description", "Allows another jump after leaving the ground when jump is pressed again.");
        put("module.JUMP_BOOST.title", "Jump Boost");
        put("module.JUMP_BOOST.description", "Adjusts the local jump impulse multiplier.");
        put("module.AUTO_SPRINT.title", "Auto Sprint");
        put("module.AUTO_SPRINT.description", "Sprints while moving forward.");
        put("module.SPEED.title", "Speed");
        put("module.SPEED.description", "Applies a configurable horizontal movement boost.");
        put("module.FLIGHT.title", "Flight");
        put("module.FLIGHT.description", "Local flight controls using jump and sneak.");
        put("module.AUTO_JUMP.title", "Auto Jump");
        put("module.AUTO_JUMP.description", "Automatically jumps while moving forward.");
        put("module.STEP_ASSIST.title", "Step Assist");
        put("module.STEP_ASSIST.description", "Jumps when forward movement meets a wall.");
        put("module.SPIDER.title", "Spider");
        put("module.SPIDER.description", "Climbs a wall while moving into it.");
        put("module.ENTITY_ESP.title", "Entity ESP");
        put("module.ENTITY_ESP.description", "Highlights nearby loaded living entities and dropped items.");
        put("module.ITEM_ESP.title", "Item ESP");
        put("module.ITEM_ESP.description", "Includes dropped item entities in the Entity ESP highlight filter.");
        put("module.WAYPOINTS.title", "Waypoints");
        put("module.WAYPOINTS.description", "Displays saved coordinates and distances; press V to save a waypoint.");
        put("module.MINIMAP.title", "Mini Map");
        put("module.MINIMAP.description", "Shows nearby entities already loaded by the client.");
        put("module.NIGHT_VISION.title", "Night Vision");
        put("module.NIGHT_VISION.description", "Improves local visibility using the client gamma setting.");
        put("module.GAMMA_BOOST.title", "Gamma Boost");
        put("module.GAMMA_BOOST.description", "Sets vanilla gamma to its maximum normal value.");
        put("module.HUD.title", "HUD");
        put("module.HUD.description", "Displays enabled modules, coordinates and FPS.");
        put("module.TARGET_HUD.title", "Target HUD");
        put("module.TARGET_HUD.description", "Shows health and distance for the crosshair target.");
        put("module.KEYSTROKES_HUD.title", "Keystrokes");
        put("module.KEYSTROKES_HUD.description", "Displays movement key states on screen.");
        put("module.ARMOR_HUD.title", "Armor HUD");
        put("module.ARMOR_HUD.description", "Displays health and armor status.");
        put("module.SPEEDOMETER.title", "Speedometer");
        put("module.SPEEDOMETER.description", "Displays horizontal movement speed in blocks per second.");
        put("module.CPS_HUD.title", "CPS HUD");
        put("module.CPS_HUD.description", "Displays successful attack actions per second.");
        put("module.NO_FALL.title", "NoFall");
        put("module.NO_FALL.description", "Clears the local fall-distance accumulator.");

        put("setting.target_players", "Target players");
        put("setting.include_other_players", "Include other players");
        put("setting.target_mobs", "Target hostile mobs");
        put("setting.include_hostile_mobs", "Include hostile mobs");
        put("setting.smooth_rotation", "Smooth rotation");
        put("setting.rotate_target", "Rotate toward selected target");
        put("setting.line_of_sight", "Line of sight");
        put("setting.clear_view", "Require a clear view");
        put("setting.attack_range", "Attack range");
        put("setting.distance_blocks", "Target distance in blocks");
        put("setting.lowest_health", "Lowest health first");
        put("setting.weakest_target", "Prioritise the weakest target");
        put("setting.aim_fov", "Aim field of view");
        put("setting.target_cone", "Target cone width in degrees");
        put("setting.rotation_speed", "Rotation speed");
        put("setting.rotation_interpolation", "Aim interpolation from slow to instant");
        put("setting.trigger_delay", "Trigger delay");
        put("setting.delay_between", "Ticks between attack attempts");
        put("setting.clicks_per_second", "Clicks per second");
        put("setting.vanilla_cooldown", "Respects vanilla attack cooldown");
        put("setting.movement_multiplier", "Movement multiplier");
        put("setting.horizontal_multiplier", "Horizontal movement multiplier");
        put("setting.flight_speed", "Flight speed");
        put("setting.vertical_speed", "Vertical speed per tick");
        put("setting.jump_interval", "Jump interval");
        put("setting.ticks_between_jumps", "Ticks between automatic jumps");
        put("setting.climb_speed", "Climb speed");
        put("setting.wall_vertical_speed", "Vertical motion while touching a wall");
        put("setting.show_players", "Show players");
        put("setting.highlight_players", "Highlight loaded player entities");
        put("setting.show_mobs", "Show mobs");
        put("setting.highlight_mobs", "Highlight loaded mob entities");
        put("setting.esp_range", "ESP range");
        put("setting.scan_radius", "Entity scan radius in blocks");
        put("setting.vanilla_gamma", "Vanilla gamma");
        put("setting.max_gamma", "Uses the maximum normal gamma value");
        put("setting.target_card", "Target card");
        put("setting.crosshair_target", "Shows the entity under your crosshair");
        put("setting.key_display", "Key display");
        put("setting.wasd_jump", "Shows W A S D and jump input");
        put("setting.player_status", "Player status");
        put("setting.health_armor", "Shows current health and armor points");
        put("setting.movement_speed", "Movement speed");
        put("setting.blocks_second", "Horizontal blocks per second");
        put("setting.attack_rate", "Attack rate");
        put("setting.successful_attacks", "Successful attack actions per second");
        put("setting.module_status", "Module status");
        put("setting.toggle_module_list", "Toggle this module from the module list");
    }

    private SkyOPLanguage() { }

    private static void put(String key, String value) { EN.put(key, value); }

    public static List<Language> languages() { return LANGUAGES; }

    public static String code() { return languageCode; }

    public static Language current() {
        return LANGUAGES.stream().filter(language -> language.code().equals(languageCode))
                .findFirst().orElse(LANGUAGES.getFirst());
    }

    /** Keeps the client English-only, including when loading older Indonesian configs. */
    public static void set(String ignoredCode) {
        languageCode = "en";
    }

    /** English is the only supported language in this build. */
    public static void next() {
        languageCode = "en";
    }

    public static String text(String key) {
        return EN.getOrDefault(key, key);
    }

    public static String moduleTitle(String name, String fallback) {
        return text("module." + name + ".title").equals("module." + name + ".title")
                ? fallback : text("module." + name + ".title");
    }

    public static String moduleDescription(String name, String fallback) {
        return text("module." + name + ".description").equals("module." + name + ".description")
                ? fallback : text("module." + name + ".description");
    }
}
