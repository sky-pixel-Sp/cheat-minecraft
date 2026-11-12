package id.skyop.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-client language selection, independent from Minecraft's global language.
 * Bahasa Indonesia is the default; English is the secondary language and fallback.
 */
public final class SkyOPLanguage {
    public record Language(String code, String displayName) { }

    // Keep only languages with a complete ClickGUI translation.
    private static final List<Language> LANGUAGES = List.of(
            new Language("id", "Bahasa Indonesia"),
            new Language("en", "English")
    );

    private static final Map<String, String> EN = new LinkedHashMap<>();
    private static final Map<String, Map<String, String>> DICTIONARIES = new LinkedHashMap<>();
    private static String languageCode = "id";

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
        put("gui.theme", "THEME");
        put("gui.next_theme", "NEXT");
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
        put("category.world", "World");
        put("category.player", "Player");
        put("category.safe", "Safe");
        put("category.dangerous", "Dangerous");
        put("gui.risk_safe", "SAFE");
        put("gui.risk_dangerous", "DANGEROUS");

        put("module.KILL_AURA.title", "KillAura");
        put("module.KILL_AURA.description", "Prioritises a nearby configured target.");
        put("module.TRIGGER_BOT.title", "TriggerBot");
        put("module.TRIGGER_BOT.description", "Attacks a valid target under the crosshair.");
        put("module.AUTO_CLICKER.title", "AutoClicker");
        put("module.AUTO_CLICKER.description", "Repeats attacks while the attack key is held.");
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
        put("module.ENTITY_ESP.description", "Highlights nearby loaded living entities.");
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
        put("module.FAST_BREAK.title", "Fast Break");
        put("module.FAST_BREAK.description", "Boosts local mining speed with a Haste I–VII equivalent.");
        put("module.AUTO_MLG.title", "Auto MLG");
        put("module.AUTO_MLG.description", "Attempts a normal water-bucket placement while falling toward a valid block.");
        put("module.AUTO_TOTEM.title", "Auto Totem");
        put("module.AUTO_TOTEM.description", "Moves a carried totem into your offhand when health is low.");

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
        put("setting.aim_fov", "Target field of view");
        put("setting.target_cone", "Full target field width in degrees, up to 360");
        put("setting.clear_view", "Require unobstructed visibility; off scans loaded targets behind blocks (the server may reject attacks)");
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
        put("setting.attack_delay", "Attack delay");
        put("setting.attack_delay_desc", "Ticks between successful attack attempts.");
        put("setting.hunt_range", "Hunt range");
        put("setting.hunt_range_desc", "Maximum loaded hostile-mob distance in blocks.");
        put("setting.placement_delay", "Placement delay");
        put("setting.placement_delay_desc", "Ticks between block placement attempts.");
        put("setting.jump_multiplier", "Jump multiplier");
        put("setting.jump_multiplier_desc", "Multiplier applied to the local jump impulse.");
        put("setting.fast_break_level", "Haste level");
        put("setting.fast_break_level_desc", "Haste VII equals a 2.4x local mining-speed multiplier; server rules still apply.");
        put("setting.auto_mlg_fall", "MLG trigger distance");
        put("setting.auto_mlg_fall_desc", "Minimum fall distance before a normal water-bucket use attempt.");
        put("setting.auto_totem_health", "Totem health threshold");
        put("setting.auto_totem_health_desc", "Moves a totem to offhand when health is at or below this value.");
        put("setting.show_items", "Show dropped items");
        put("setting.highlight_items", "Highlight item entities already loaded by the client.");
        put("setting.hud_accent", "HUD accent color");
        put("setting.hud_accent_desc", "Accent color used by SkyOP HUD panels.");
        put("setting.keybind_description", "Keyboard shortcut used to toggle this module.");
        put("dependency.join_world", "Join a world first.");
        put("dependency.player_required", "A player entity is required.");
        put("dependency.inventory_required", "Open the normal player inventory context first.");
        put("dependency.fishing_rod_required", "Hold a fishing rod in your main hand.");
        put("dependency.block_target_required", "Aim at a block first.");
        put("category.inventory", "Inventory");
        put("category.favorites", "Favorites");
        put("module.POTION_HUD.title", "Potion HUD");
        put("module.DURABILITY_HUD.title", "Durability HUD");
        put("module.COMPASS_HUD.title", "Compass HUD");
        put("module.DIRECTION_HUD.title", "Direction HUD");
        put("module.PING_HUD.title", "Ping HUD");
        put("module.FPS_HUD.title", "FPS HUD");
        put("module.RADAR_HUD.title", "Nearby Entity Radar");
        put("module.TEXT_GUI.title", "Custom Text HUD");
        put("module.AUTO_EAT.title", "Auto Eat");
        put("module.AUTO_ARMOR.title", "Auto Armor");
        put("module.AUTO_TOOL.title", "Auto Tool");
        put("module.AUTO_REFILL.title", "Auto Refill");
        put("module.AUTO_FISH.title", "Auto Fish");
        put("module.INVENTORY_CLEANER.title", "Inventory Cleaner");
        put("module.HOTBAR_PRESETS.title", "Hotbar Presets");
        put("module.PROFILE_MANAGER.title", "Profile Manager");
        put("setting.hunger_threshold", "Hunger threshold");
        put("setting.eat_below", "Start eating below this hunger level");
        put("setting.refill_threshold", "Refill threshold");
        put("setting.refill_low", "Refill when the selected stack is low");
        put("module.POTION_HUD.description", "Shows active potion effects and remaining duration.");
        put("module.DURABILITY_HUD.description", "Displays main-hand item durability.");
        put("module.COMPASS_HUD.description", "Shows compass direction and yaw.");
        put("module.DIRECTION_HUD.description", "Shows cardinal direction and coordinates.");
        put("module.PING_HUD.description", "Shows your current multiplayer latency.");
        put("module.FPS_HUD.description", "Shows the current frame rate.");
        put("module.RADAR_HUD.description", "Lists nearby entities already loaded by the client.");
        put("module.TEXT_GUI.description", "Displays a configurable custom HUD message.");
        put("module.AUTO_EAT.description", "Uses safe food from your hotbar when hunger is low.");
        put("module.AUTO_ARMOR.description", "Equips armor into empty armor slots.");
        put("module.AUTO_TOOL.description", "Selects the best hotbar tool for the targeted block.");
        put("module.AUTO_REFILL.description", "Refills low hotbar stacks from matching inventory items.");
        put("module.AUTO_FISH.description", "Automatically reels on bite and recasts your fishing rod.");
        put("module.INVENTORY_CLEANER.description", "Consolidates matching stacks without dropping items.");
        put("module.HOTBAR_PRESETS.description", "Saves and restores three hotbar layouts.");
        put("module.PROFILE_MANAGER.description", "Manages JSON profiles, imports, exports and backups.");

        translate("id", Map.ofEntries(
                e("gui.control_center", "PUSAT KONTROL MODUL"), e("gui.subtitle", "Profil • Modul • Pengaturan langsung"),
                e("gui.ready", "KLIEN SIAP"), e("gui.categories", "KATEGORI"), e("gui.active_modules", "MODUL AKTIF"),
                e("gui.theme", "TEMA"), e("gui.next_theme", "GANTI"),
                e("gui.inspector", "PENGATURAN MODUL"), e("gui.select_module", "Pilih modul"), e("gui.modules", "MODUL"),
                e("gui.left_toggle_right_inspect", "Klik kiri: aktif/nonaktif • Klik kanan: pengaturan"),
                e("gui.no_extra_settings", "Tidak ada pengaturan tambahan"), e("gui.enabled", "AKTIF"),
                e("gui.disabled", "NONAKTIF"), e("gui.quick_profiles", "PROFIL CEPAT"), e("gui.language", "BAHASA"),
                e("gui.disable_all", "MATIKAN SEMUA"), e("gui.no_target", "TIDAK ADA TARGET"), e("gui.active", "aktif"),
                e("category.combat", "Pertarungan"), e("category.movement", "Pergerakan"), e("category.visual", "Visual"), e("category.player", "Pemain"),
                e("setting.target_players", "Target pemain"), e("setting.include_other_players", "Sertakan pemain lain"),
                e("setting.target_mobs", "Target monster"), e("setting.include_hostile_mobs", "Sertakan monster"),
                e("setting.smooth_rotation", "Rotasi halus"), e("setting.rotate_target", "Arahkan ke target terpilih"),
                e("setting.line_of_sight", "Garis pandang"), e("setting.clear_view", "Wajib terlihat jelas"),
                e("setting.attack_range", "Jarak serang"), e("setting.distance_blocks", "Jarak target dalam blok"),
                e("setting.lowest_health", "HP terendah dahulu"), e("setting.weakest_target", "Prioritaskan target terlemah"),
                e("setting.aim_fov", "Bidang pandang bidik"), e("setting.target_cone", "Lebar sudut target"),
                e("setting.rotation_speed", "Kecepatan rotasi"), e("setting.rotation_interpolation", "Interpolasi bidikan"),
                e("setting.trigger_delay", "Jeda pemicu"), e("setting.delay_between", "Tick antar percobaan serangan"),
                e("setting.clicks_per_second", "Klik per detik"), e("setting.vanilla_cooldown", "Mengikuti cooldown serangan vanilla"),
                e("setting.movement_multiplier", "Pengali gerakan"), e("setting.horizontal_multiplier", "Pengali gerakan horizontal"),
                e("setting.flight_speed", "Kecepatan terbang"), e("setting.vertical_speed", "Kecepatan vertikal per tick"),
                e("setting.jump_interval", "Interval lompat"), e("setting.ticks_between_jumps", "Tick antar lompatan otomatis"),
                e("setting.climb_speed", "Kecepatan memanjat"), e("setting.wall_vertical_speed", "Gerakan vertikal saat menyentuh dinding"),
                e("setting.show_players", "Tampilkan pemain"), e("setting.highlight_players", "Sorot pemain yang dimuat"),
                e("setting.show_mobs", "Tampilkan monster"), e("setting.highlight_mobs", "Sorot monster yang dimuat"),
                e("setting.esp_range", "Jarak ESP"), e("setting.scan_radius", "Radius pemindaian entitas"),
                e("setting.vanilla_gamma", "Gamma vanilla"), e("setting.max_gamma", "Menggunakan gamma normal maksimum"),
                e("setting.target_card", "Kartu target"), e("setting.crosshair_target", "Menampilkan entitas di bidikan"),
                e("setting.key_display", "Tampilan tombol"), e("setting.wasd_jump", "Menampilkan tombol W A S D dan lompat"),
                e("setting.player_status", "Status pemain"), e("setting.health_armor", "Menampilkan health dan armor"),
                e("setting.movement_speed", "Kecepatan gerakan"), e("setting.blocks_second", "Blok horizontal per detik"),
                e("setting.attack_rate", "Laju serangan"), e("setting.successful_attacks", "Serangan berhasil per detik"),
                e("setting.module_status", "Status modul"), e("setting.toggle_module_list", "Ubah status melalui daftar modul")
        ));

        translate("es", Map.ofEntries(
                e("gui.control_center", "CENTRO DE CONTROL DE MÓDULOS"), e("gui.subtitle", "Perfiles • Módulos • Ajustes"),
                e("gui.ready", "CLIENTE LISTO"), e("gui.categories", "CATEGORÍAS"), e("gui.active_modules", "MÓDULOS ACTIVOS"),
                e("gui.inspector", "INSPECTOR DE MÓDULOS"), e("gui.select_module", "Selecciona un módulo"),
                e("gui.modules", "MÓDULOS"), e("gui.left_toggle_right_inspect", "Clic izq.: activar • Clic der.: configurar"),
                e("gui.no_extra_settings", "Sin ajustes adicionales"), e("gui.enabled", "ACTIVADO"), e("gui.disabled", "DESACTIVADO"),
                e("gui.quick_profiles", "PERFILES RÁPIDOS"), e("gui.language", "IDIOMA"), e("gui.disable_all", "DESACTIVAR TODO"),
                e("gui.no_target", "SIN OBJETIVO"), e("category.combat", "Combate"), e("category.movement", "Movimiento"),
                e("category.visual", "Visual"), e("category.player", "Jugador"),
                e("setting.target_players", "Atacar jugadores"), e("setting.target_mobs", "Atacar criaturas hostiles"),
                e("setting.smooth_rotation", "Rotación suave"), e("setting.line_of_sight", "Línea de visión"),
                e("setting.attack_range", "Alcance de ataque"), e("setting.lowest_health", "Menor salud primero"),
                e("setting.aim_fov", "Campo de visión"), e("setting.rotation_speed", "Velocidad de rotación"),
                e("setting.trigger_delay", "Retardo del disparador"), e("setting.clicks_per_second", "Clics por segundo"),
                e("setting.movement_multiplier", "Multiplicador de movimiento"), e("setting.flight_speed", "Velocidad de vuelo"),
                e("setting.jump_interval", "Intervalo de salto"), e("setting.climb_speed", "Velocidad de escalada"),
                e("setting.show_players", "Mostrar jugadores"), e("setting.show_mobs", "Mostrar criaturas"),
                e("setting.esp_range", "Alcance ESP"), e("setting.vanilla_gamma", "Gamma vanilla"),
                e("setting.target_card", "Tarjeta de objetivo"), e("setting.key_display", "Mostrar teclas"),
                e("setting.player_status", "Estado del jugador"), e("setting.movement_speed", "Velocidad de movimiento"),
                e("setting.attack_rate", "Frecuencia de ataque"), e("setting.module_status", "Estado del módulo")
        ));

        translate("pt", Map.ofEntries(
                e("gui.control_center", "CENTRAL DE CONTROLE DE MÓDULOS"), e("gui.subtitle", "Perfis • Módulos • Ajustes"),
                e("gui.ready", "CLIENTE PRONTO"), e("gui.categories", "CATEGORIAS"), e("gui.active_modules", "MÓDULOS ATIVOS"),
                e("gui.inspector", "INSPETOR DE MÓDULOS"), e("gui.select_module", "Selecione um módulo"),
                e("gui.modules", "MÓDULOS"), e("gui.left_toggle_right_inspect", "Clique esq.: ativar • Clique dir.: configurar"),
                e("gui.no_extra_settings", "Sem configurações extras"), e("gui.enabled", "ATIVADO"), e("gui.disabled", "DESATIVADO"),
                e("gui.quick_profiles", "PERFIS RÁPIDOS"), e("gui.language", "IDIOMA"), e("gui.disable_all", "DESATIVAR TUDO"),
                e("gui.no_target", "SEM ALVO"), e("category.combat", "Combate"), e("category.movement", "Movimento"),
                e("category.visual", "Visual"), e("category.player", "Jogador"),
                e("setting.target_players", "Alvejar jogadores"), e("setting.target_mobs", "Alvejar criaturas hostis"),
                e("setting.smooth_rotation", "Rotação suave"), e("setting.line_of_sight", "Linha de visão"),
                e("setting.attack_range", "Alcance de ataque"), e("setting.lowest_health", "Menor vida primeiro"),
                e("setting.aim_fov", "Campo de visão"), e("setting.rotation_speed", "Velocidade de rotação"),
                e("setting.trigger_delay", "Atraso do gatilho"), e("setting.clicks_per_second", "Cliques por segundo"),
                e("setting.movement_multiplier", "Multiplicador de movimento"), e("setting.flight_speed", "Velocidade de voo"),
                e("setting.jump_interval", "Intervalo de salto"), e("setting.climb_speed", "Velocidade de escalada"),
                e("setting.show_players", "Mostrar jogadores"), e("setting.show_mobs", "Mostrar criaturas"),
                e("setting.esp_range", "Alcance ESP"), e("setting.vanilla_gamma", "Gamma vanilla"),
                e("setting.target_card", "Cartão de alvo"), e("setting.key_display", "Exibir teclas"),
                e("setting.player_status", "Status do jogador"), e("setting.movement_speed", "Velocidade de movimento"),
                e("setting.attack_rate", "Taxa de ataque"), e("setting.module_status", "Status do módulo")
        ));

        translate("fr", Map.ofEntries(
                e("gui.control_center", "CENTRE DE CONTRÔLE DES MODULES"), e("gui.subtitle", "Profils • Modules • Réglages"),
                e("gui.ready", "CLIENT PRÊT"), e("gui.categories", "CATÉGORIES"), e("gui.active_modules", "MODULES ACTIFS"),
                e("gui.inspector", "INSPECTEUR DE MODULE"), e("gui.select_module", "Sélectionner un module"),
                e("gui.modules", "MODULES"), e("gui.left_toggle_right_inspect", "Clic gauche : activer • Clic droit : régler"),
                e("gui.no_extra_settings", "Aucun réglage supplémentaire"), e("gui.enabled", "ACTIVÉ"), e("gui.disabled", "DÉSACTIVÉ"),
                e("gui.quick_profiles", "PROFILS RAPIDES"), e("gui.language", "LANGUE"), e("gui.disable_all", "TOUT DÉSACTIVER"),
                e("gui.no_target", "AUCUNE CIBLE"), e("category.combat", "Combat"), e("category.movement", "Déplacement"),
                e("category.visual", "Visuel"), e("category.player", "Joueur"),
                e("setting.target_players", "Cibler les joueurs"), e("setting.target_mobs", "Cibler les monstres hostiles"),
                e("setting.smooth_rotation", "Rotation fluide"), e("setting.line_of_sight", "Ligne de vue"),
                e("setting.attack_range", "Portée d'attaque"), e("setting.lowest_health", "Santé la plus faible d'abord"),
                e("setting.aim_fov", "Champ de vision"), e("setting.rotation_speed", "Vitesse de rotation"),
                e("setting.trigger_delay", "Délai du déclencheur"), e("setting.clicks_per_second", "Clics par seconde"),
                e("setting.movement_multiplier", "Multiplicateur de déplacement"), e("setting.flight_speed", "Vitesse de vol"),
                e("setting.jump_interval", "Intervalle de saut"), e("setting.climb_speed", "Vitesse d'escalade"),
                e("setting.show_players", "Afficher les joueurs"), e("setting.show_mobs", "Afficher les monstres"),
                e("setting.esp_range", "Portée ESP"), e("setting.vanilla_gamma", "Gamma vanilla"),
                e("setting.target_card", "Carte de cible"), e("setting.key_display", "Affichage des touches"),
                e("setting.player_status", "État du joueur"), e("setting.movement_speed", "Vitesse de déplacement"),
                e("setting.attack_rate", "Fréquence d'attaque"), e("setting.module_status", "État du module")
        ));

        translate("zh", Map.ofEntries(
                e("gui.control_center", "模组控制中心"), e("gui.subtitle", "配置 • 模组 • 实时设置"),
                e("gui.ready", "客户端已就绪"), e("gui.categories", "分类"), e("gui.active_modules", "已启用模组"),
                e("gui.inspector", "模组设置"), e("gui.select_module", "选择一个模组"), e("gui.modules", "模组"),
                e("gui.left_toggle_right_inspect", "左键：切换 • 右键：设置"), e("gui.no_extra_settings", "没有额外设置"),
                e("gui.enabled", "已启用"), e("gui.disabled", "已禁用"), e("gui.quick_profiles", "快速配置"),
                e("gui.language", "语言"), e("gui.disable_all", "全部关闭"), e("gui.no_target", "没有目标"),
                e("category.combat", "战斗"), e("category.movement", "移动"), e("category.visual", "视觉"), e("category.player", "玩家"),
                e("setting.target_players", "目标玩家"), e("setting.target_mobs", "目标敌对生物"), e("setting.smooth_rotation", "平滑转向"),
                e("setting.line_of_sight", "视线检查"), e("setting.attack_range", "攻击距离"), e("setting.lowest_health", "优先最低生命值"),
                e("setting.aim_fov", "瞄准视野角"), e("setting.rotation_speed", "转向速度"), e("setting.trigger_delay", "触发延迟"),
                e("setting.clicks_per_second", "每秒点击数"), e("setting.movement_multiplier", "移动倍率"), e("setting.flight_speed", "飞行速度"),
                e("setting.jump_interval", "跳跃间隔"), e("setting.climb_speed", "攀爬速度"), e("setting.show_players", "显示玩家"),
                e("setting.show_mobs", "显示生物"), e("setting.esp_range", "透视范围"), e("setting.vanilla_gamma", "原版伽马"),
                e("setting.target_card", "目标信息"), e("setting.key_display", "按键显示"), e("setting.player_status", "玩家状态"),
                e("setting.movement_speed", "移动速度"), e("setting.attack_rate", "攻击频率"), e("setting.module_status", "模组状态")
        ));

        translate("ja", Map.ofEntries(
                e("gui.control_center", "モジュールコントロールセンター"), e("gui.subtitle", "プロファイル • モジュール • 設定"),
                e("gui.ready", "クライアント準備完了"), e("gui.categories", "カテゴリー"), e("gui.active_modules", "有効なモジュール"),
                e("gui.inspector", "モジュール設定"), e("gui.select_module", "モジュールを選択"), e("gui.modules", "モジュール"),
                e("gui.left_toggle_right_inspect", "左クリック：切替 • 右クリック：設定"), e("gui.no_extra_settings", "追加設定なし"),
                e("gui.enabled", "有効"), e("gui.disabled", "無効"), e("gui.quick_profiles", "クイックプロファイル"),
                e("gui.language", "言語"), e("gui.disable_all", "すべて無効化"), e("gui.no_target", "ターゲットなし"),
                e("category.combat", "戦闘"), e("category.movement", "移動"), e("category.visual", "ビジュアル"), e("category.player", "プレイヤー"),
                e("setting.target_players", "プレイヤーを対象"), e("setting.target_mobs", "敵対Mobを対象"), e("setting.smooth_rotation", "スムーズ回転"),
                e("setting.line_of_sight", "視線チェック"), e("setting.attack_range", "攻撃範囲"), e("setting.lowest_health", "体力が低い順"),
                e("setting.aim_fov", "照準視野角"), e("setting.rotation_speed", "回転速度"), e("setting.trigger_delay", "トリガー遅延"),
                e("setting.clicks_per_second", "1秒あたりのクリック数"), e("setting.movement_multiplier", "移動倍率"), e("setting.flight_speed", "飛行速度"),
                e("setting.jump_interval", "ジャンプ間隔"), e("setting.climb_speed", "登る速度"), e("setting.show_players", "プレイヤーを表示"),
                e("setting.show_mobs", "Mobを表示"), e("setting.esp_range", "ESP範囲"), e("setting.vanilla_gamma", "バニラガンマ"),
                e("setting.target_card", "ターゲット情報"), e("setting.key_display", "キー表示"), e("setting.player_status", "プレイヤー状態"),
                e("setting.movement_speed", "移動速度"), e("setting.attack_rate", "攻撃速度"), e("setting.module_status", "モジュール状態")
        ));

        translate("id", Map.ofEntries(
                e("category.inventory", "Inventaris"), e("category.favorites", "Favorit"),
                e("module.POTION_HUD.title", "HUD Ramuan"), e("module.DURABILITY_HUD.title", "HUD Daya Tahan"),
                e("module.COMPASS_HUD.title", "HUD Kompas"), e("module.DIRECTION_HUD.title", "HUD Arah"),
                e("module.PING_HUD.title", "HUD Ping"), e("module.FPS_HUD.title", "HUD FPS"),
                e("module.RADAR_HUD.title", "Radar Entitas Terdekat"), e("module.TEXT_GUI.title", "Teks Kustom"),
                e("module.AUTO_EAT.title", "Makan Otomatis"), e("module.AUTO_ARMOR.title", "Armor Otomatis"),
                e("module.AUTO_TOOL.title", "Alat Otomatis"), e("module.AUTO_REFILL.title", "Isi Ulang Otomatis"),
                e("module.AUTO_FISH.title", "Mancing Otomatis"), e("module.INVENTORY_CLEANER.title", "Rapikan Inventaris"),
                e("module.HOTBAR_PRESETS.title", "Preset Hotbar"), e("module.PROFILE_MANAGER.title", "Pengelola Profil"),
                e("setting.hunger_threshold", "Ambang rasa lapar"), e("setting.eat_below", "Mulai makan di bawah nilai ini"),
                e("setting.refill_threshold", "Ambang isi ulang"), e("setting.refill_low", "Isi ulang saat jumlah stack di bawah nilai ini"),
                e("gui.hud_color", "WARNA HUD"),
                e("gui.apply", "TERAPKAN"),
                e("gui.hud_layout", "TATA LETAK HUD"),
                e("gui.drag_hud", "Seret setiap panel untuk memindahkannya"),
                e("module.POTION_HUD.description", "Menampilkan efek ramuan aktif dan sisa durasinya."),
                e("module.DURABILITY_HUD.description", "Menampilkan persentase daya tahan item di tangan utama."),
                e("module.COMPASS_HUD.description", "Menampilkan arah mata angin dan sudut yaw pemain."),
                e("module.DIRECTION_HUD.description", "Menampilkan arah mata angin saat ini."),
                e("module.PING_HUD.description", "Menampilkan latensi saat data daftar pemain tersedia."),
                e("module.FPS_HUD.description", "Menampilkan jumlah frame per detik klien."),
                e("module.RADAR_HUD.description", "Menampilkan entitas terdekat yang sudah dimuat klien."),
                e("module.TEXT_GUI.description", "Menampilkan pesan kustom di layar."),
                e("module.AUTO_EAT.description", "Memakan makanan aman dari hotbar saat rasa lapar melewati ambang."),
                e("module.AUTO_ARMOR.description", "Mengisi slot armor kosong tanpa mengganti armor yang sedang dipakai."),
                e("module.AUTO_TOOL.description", "Memilih alat tercepat di hotbar untuk blok yang ditargetkan."),
                e("module.AUTO_REFILL.description", "Mengisi ulang stack hotbar yang hampir habis dari inventaris."),
                e("module.AUTO_FISH.description", "Menggulung pancing saat gigitan terdeteksi lalu melemparnya kembali."),
                e("module.INVENTORY_CLEANER.description", "Menggabungkan stack sejenis tanpa menjatuhkan item."),
                e("module.HOTBAR_PRESETS.description", "Menyimpan tiga susunan hotbar menggunakan ID item."),
                e("module.PROFILE_MANAGER.description", "Mengimpor, mengekspor, dan mencadangkan profil konfigurasi.")
        ));
        translate("es", Map.ofEntries(
                e("category.inventory", "Inventario"), e("category.favorites", "Favoritos"),
                e("module.POTION_HUD.title", "HUD de pociones"), e("module.DURABILITY_HUD.title", "HUD de durabilidad"),
                e("module.COMPASS_HUD.title", "HUD de brújula"), e("module.DIRECTION_HUD.title", "HUD de dirección"),
                e("module.PING_HUD.title", "HUD de ping"), e("module.FPS_HUD.title", "HUD de FPS"),
                e("module.RADAR_HUD.title", "Radar de entidades cercanas"), e("module.TEXT_GUI.title", "Texto personalizado"),
                e("module.AUTO_EAT.title", "Comer automáticamente"), e("module.AUTO_ARMOR.title", "Armadura automática"),
                e("module.AUTO_TOOL.title", "Herramienta automática"), e("module.AUTO_REFILL.title", "Recarga automática"),
                e("module.AUTO_FISH.title", "Pesca automática"), e("module.INVENTORY_CLEANER.title", "Limpiador de inventario"),
                e("module.HOTBAR_PRESETS.title", "Perfiles de barra rápida"), e("module.PROFILE_MANAGER.title", "Gestor de perfiles"),
                e("setting.hunger_threshold", "Umbral de hambre"), e("setting.eat_below", "Comer por debajo de este nivel"),
                e("setting.refill_threshold", "Umbral de recarga"), e("setting.refill_low", "Recargar cuando la pila sea baja"),
                e("gui.hud_color", "COLOR DEL HUD"),
                e("gui.apply", "APLICAR"),
                e("gui.hud_layout", "DISEÑO DEL HUD"),
                e("gui.drag_hud", "Arrastra cada panel para moverlo"),
                e("module.POTION_HUD.description", "Muestra los efectos activos y su duración restante."),
                e("module.DURABILITY_HUD.description", "Muestra la durabilidad del objeto en la mano principal."),
                e("module.COMPASS_HUD.description", "Muestra el punto cardinal y el ángulo del jugador."),
                e("module.DIRECTION_HUD.description", "Muestra la dirección cardinal actual."),
                e("module.PING_HUD.description", "Muestra la latencia cuando hay datos de la lista de jugadores."),
                e("module.FPS_HUD.description", "Muestra los fotogramas por segundo del cliente."),
                e("module.RADAR_HUD.description", "Muestra entidades cercanas ya cargadas por el cliente."),
                e("module.TEXT_GUI.description", "Muestra un mensaje personalizado en pantalla."),
                e("module.AUTO_EAT.description", "Usa comida segura de la barra rápida cuando baja el hambre."),
                e("module.AUTO_ARMOR.description", "Completa ranuras de armadura vacías sin reemplazar equipo."),
                e("module.AUTO_TOOL.description", "Selecciona la herramienta más rápida de la barra para el bloque objetivo."),
                e("module.AUTO_REFILL.description", "Repone pilas pequeñas de la barra desde el inventario."),
                e("module.AUTO_FISH.description", "Recoge el anzuelo al detectar una picada y vuelve a lanzarlo."),
                e("module.INVENTORY_CLEANER.description", "Consolida pilas iguales sin tirar objetos."),
                e("module.HOTBAR_PRESETS.description", "Guarda tres diseños de barra rápida mediante identificadores."),
                e("module.PROFILE_MANAGER.description", "Importa, exporta y crea copias de perfiles.")
        ));
        translate("pt", Map.ofEntries(
                e("category.inventory", "Inventário"), e("category.favorites", "Favoritos"),
                e("module.POTION_HUD.title", "HUD de poções"), e("module.DURABILITY_HUD.title", "HUD de durabilidade"),
                e("module.COMPASS_HUD.title", "HUD de bússola"), e("module.DIRECTION_HUD.title", "HUD de direção"),
                e("module.PING_HUD.title", "HUD de ping"), e("module.FPS_HUD.title", "HUD de FPS"),
                e("module.RADAR_HUD.title", "Radar de entidades próximas"), e("module.TEXT_GUI.title", "Texto personalizado"),
                e("module.AUTO_EAT.title", "Comer automaticamente"), e("module.AUTO_ARMOR.title", "Armadura automática"),
                e("module.AUTO_TOOL.title", "Ferramenta automática"), e("module.AUTO_REFILL.title", "Reabastecimento automático"),
                e("module.AUTO_FISH.title", "Pesca automática"), e("module.INVENTORY_CLEANER.title", "Organizador de inventário"),
                e("module.HOTBAR_PRESETS.title", "Perfis da barra rápida"), e("module.PROFILE_MANAGER.title", "Gerenciador de perfis"),
                e("setting.hunger_threshold", "Limite de fome"), e("setting.eat_below", "Comer abaixo deste nível"),
                e("setting.refill_threshold", "Limite de reabastecimento"), e("setting.refill_low", "Reabastecer quando a pilha estiver baixa"),
                e("gui.hud_color", "COR DO HUD"),
                e("gui.apply", "APLICAR"),
                e("gui.hud_layout", "LAYOUT DO HUD"),
                e("gui.drag_hud", "Arraste cada painel para movê-lo"),
                e("module.POTION_HUD.description", "Mostra efeitos ativos e o tempo restante."),
                e("module.DURABILITY_HUD.description", "Mostra a durabilidade do item na mão principal."),
                e("module.COMPASS_HUD.description", "Mostra o ponto cardeal e o ângulo do jogador."),
                e("module.DIRECTION_HUD.description", "Mostra a direção cardeal atual."),
                e("module.PING_HUD.description", "Mostra a latência quando os dados da lista de jogadores estão disponíveis."),
                e("module.FPS_HUD.description", "Mostra os quadros por segundo do cliente."),
                e("module.RADAR_HUD.description", "Mostra entidades próximas já carregadas pelo cliente."),
                e("module.TEXT_GUI.description", "Mostra uma mensagem personalizada na tela."),
                e("module.AUTO_EAT.description", "Usa comida segura da barra rápida quando a fome está baixa."),
                e("module.AUTO_ARMOR.description", "Preenche espaços vazios de armadura sem substituir peças usadas."),
                e("module.AUTO_TOOL.description", "Seleciona a ferramenta mais rápida da barra para o bloco visado."),
                e("module.AUTO_REFILL.description", "Reabastece pilhas baixas da barra usando itens do inventário."),
                e("module.AUTO_FISH.description", "Recolhe a linha ao detectar uma mordida e lança novamente."),
                e("module.INVENTORY_CLEANER.description", "Consolida pilhas iguais sem descartar itens."),
                e("module.HOTBAR_PRESETS.description", "Salva três layouts da barra rápida usando IDs de itens."),
                e("module.PROFILE_MANAGER.description", "Importa, exporta e cria backups de perfis.")
        ));
        translate("fr", Map.ofEntries(
                e("category.inventory", "Inventaire"), e("category.favorites", "Favoris"),
                e("module.POTION_HUD.title", "HUD des potions"), e("module.DURABILITY_HUD.title", "HUD de durabilité"),
                e("module.COMPASS_HUD.title", "HUD boussole"), e("module.DIRECTION_HUD.title", "HUD direction"),
                e("module.PING_HUD.title", "HUD ping"), e("module.FPS_HUD.title", "HUD FPS"),
                e("module.RADAR_HUD.title", "Radar des entités proches"), e("module.TEXT_GUI.title", "Texte personnalisé"),
                e("module.AUTO_EAT.title", "Repas automatique"), e("module.AUTO_ARMOR.title", "Armure automatique"),
                e("module.AUTO_TOOL.title", "Outil automatique"), e("module.AUTO_REFILL.title", "Réapprovisionnement automatique"),
                e("module.AUTO_FISH.title", "Pêche automatique"), e("module.INVENTORY_CLEANER.title", "Nettoyage inventaire"),
                e("module.HOTBAR_PRESETS.title", "Profils de barre rapide"), e("module.PROFILE_MANAGER.title", "Gestionnaire de profils"),
                e("setting.hunger_threshold", "Seuil de faim"), e("setting.eat_below", "Manger sous ce niveau"),
                e("setting.refill_threshold", "Seuil de recharge"), e("setting.refill_low", "Recharger quand la pile est basse"),
                e("gui.hud_color", "COULEUR DU HUD"),
                e("gui.apply", "APPLIQUER"),
                e("gui.hud_layout", "DISPOSITION DU HUD"),
                e("gui.drag_hud", "Faites glisser chaque panneau pour le déplacer"),
                e("module.POTION_HUD.description", "Affiche les effets actifs et leur durée restante."),
                e("module.DURABILITY_HUD.description", "Affiche la durabilité de l'objet en main principale."),
                e("module.COMPASS_HUD.description", "Affiche le point cardinal et l'angle du joueur."),
                e("module.DIRECTION_HUD.description", "Affiche la direction cardinale actuelle."),
                e("module.PING_HUD.description", "Affiche la latence lorsque la liste des joueurs est disponible."),
                e("module.FPS_HUD.description", "Affiche les images par seconde du client."),
                e("module.RADAR_HUD.description", "Affiche les entités proches déjà chargées par le client."),
                e("module.TEXT_GUI.description", "Affiche un message personnalisé à l'écran."),
                e("module.AUTO_EAT.description", "Utilise de la nourriture sûre de la barre rapide lorsque la faim baisse."),
                e("module.AUTO_ARMOR.description", "Remplit les emplacements d'armure vides sans remplacer l'équipement porté."),
                e("module.AUTO_TOOL.description", "Choisit l'outil le plus rapide de la barre pour le bloc visé."),
                e("module.AUTO_REFILL.description", "Recharge les piles faibles de la barre depuis l'inventaire."),
                e("module.AUTO_FISH.description", "Rembobine lors d'une touche visible puis relance la canne."),
                e("module.INVENTORY_CLEANER.description", "Regroupe les piles identiques sans jeter d'objets."),
                e("module.HOTBAR_PRESETS.description", "Enregistre trois dispositions de barre via les identifiants d'objets."),
                e("module.PROFILE_MANAGER.description", "Importe, exporte et sauvegarde les profils.")
        ));
        translate("zh", Map.ofEntries(
                e("category.inventory", "物品栏"), e("category.favorites", "收藏"),
                e("module.POTION_HUD.title", "药水 HUD"), e("module.DURABILITY_HUD.title", "耐久 HUD"),
                e("module.COMPASS_HUD.title", "指南针 HUD"), e("module.DIRECTION_HUD.title", "方向 HUD"),
                e("module.PING_HUD.title", "延迟 HUD"), e("module.FPS_HUD.title", "帧率 HUD"),
                e("module.RADAR_HUD.title", "附近实体雷达"), e("module.TEXT_GUI.title", "自定义文本"),
                e("module.AUTO_EAT.title", "自动进食"), e("module.AUTO_ARMOR.title", "自动穿甲"),
                e("module.AUTO_TOOL.title", "自动工具"), e("module.AUTO_REFILL.title", "自动补充"),
                e("module.AUTO_FISH.title", "自动钓鱼"), e("module.INVENTORY_CLEANER.title", "物品栏整理"),
                e("module.HOTBAR_PRESETS.title", "快捷栏预设"), e("module.PROFILE_MANAGER.title", "配置管理器"),
                e("setting.hunger_threshold", "饥饿阈值"), e("setting.eat_below", "低于此数值时进食"),
                e("setting.refill_threshold", "补充阈值"), e("setting.refill_low", "物品数量较低时补充"),
                e("gui.hud_color", "HUD 颜色"),
                e("gui.apply", "应用"),
                e("gui.hud_layout", "HUD 布局"),
                e("gui.drag_hud", "拖动每个面板以调整位置"),
                e("module.POTION_HUD.description", "显示有效药水效果及剩余时间。"),
                e("module.DURABILITY_HUD.description", "显示主手物品的剩余耐久百分比。"),
                e("module.COMPASS_HUD.description", "显示玩家的罗盘方向和偏航角。"),
                e("module.DIRECTION_HUD.description", "显示当前的方位方向。"),
                e("module.PING_HUD.description", "有玩家列表数据时显示网络延迟。"),
                e("module.FPS_HUD.description", "显示客户端每秒帧数。"),
                e("module.RADAR_HUD.description", "显示客户端已加载的附近实体。"),
                e("module.TEXT_GUI.description", "在屏幕上显示自定义消息。"),
                e("module.AUTO_EAT.description", "饥饿值较低时使用快捷栏中的安全食物。"),
                e("module.AUTO_ARMOR.description", "填充空盔甲槽，不替换已穿戴装备。"),
                e("module.AUTO_TOOL.description", "为目标方块选择快捷栏中效率最高的工具。"),
                e("module.AUTO_REFILL.description", "从物品栏补充数量较少的快捷栏堆叠。"),
                e("module.AUTO_FISH.description", "检测到咬钩后收线并重新抛竿。"),
                e("module.INVENTORY_CLEANER.description", "合并相同物品堆叠，不会丢弃物品。"),
                e("module.HOTBAR_PRESETS.description", "使用物品 ID 保存三套快捷栏布局。"),
                e("module.PROFILE_MANAGER.description", "导入、导出并备份配置文件。")
        ));
        translate("ja", Map.ofEntries(
                e("category.inventory", "インベントリ"), e("category.favorites", "お気に入り"),
                e("module.POTION_HUD.title", "ポーション HUD"), e("module.DURABILITY_HUD.title", "耐久 HUD"),
                e("module.COMPASS_HUD.title", "コンパス HUD"), e("module.DIRECTION_HUD.title", "方向 HUD"),
                e("module.PING_HUD.title", "Ping HUD"), e("module.FPS_HUD.title", "FPS HUD"),
                e("module.RADAR_HUD.title", "近くのエンティティレーダー"), e("module.TEXT_GUI.title", "カスタムテキスト"),
                e("module.AUTO_EAT.title", "自動食事"), e("module.AUTO_ARMOR.title", "自動防具"),
                e("module.AUTO_TOOL.title", "自動ツール"), e("module.AUTO_REFILL.title", "自動補充"),
                e("module.AUTO_FISH.title", "自動釣り"), e("module.INVENTORY_CLEANER.title", "インベントリ整理"),
                e("module.HOTBAR_PRESETS.title", "ホットバープリセット"), e("module.PROFILE_MANAGER.title", "プロファイル管理"),
                e("setting.hunger_threshold", "空腹しきい値"), e("setting.eat_below", "この値を下回ると食事"),
                e("setting.refill_threshold", "補充しきい値"), e("setting.refill_low", "スタックが少ないと補充"),
                e("gui.hud_color", "HUD の色"),
                e("gui.apply", "適用"),
                e("gui.hud_layout", "HUD レイアウト"),
                e("gui.drag_hud", "各パネルをドラッグして移動"),
                e("module.POTION_HUD.description", "有効なポーション効果と残り時間を表示します。"),
                e("module.DURABILITY_HUD.description", "メインハンドの耐久度を表示します。"),
                e("module.COMPASS_HUD.description", "方角とプレイヤーのヨー角を表示します。"),
                e("module.DIRECTION_HUD.description", "現在の方角を表示します。"),
                e("module.PING_HUD.description", "プレイヤーリスト情報がある場合に遅延を表示します。"),
                e("module.FPS_HUD.description", "クライアントのフレームレートを表示します。"),
                e("module.RADAR_HUD.description", "クライアントが読み込み済みの近くのエンティティを表示します。"),
                e("module.TEXT_GUI.description", "画面にカスタムメッセージを表示します。"),
                e("module.AUTO_EAT.description", "空腹時にホットバーの安全な食料を使用します。"),
                e("module.AUTO_ARMOR.description", "装備中の防具を交換せず空きスロットを補います。"),
                e("module.AUTO_TOOL.description", "対象ブロックに最適なホットバーの道具を選択します。"),
                e("module.AUTO_REFILL.description", "インベントリから少ないホットバースタックを補充します。"),
                e("module.AUTO_FISH.description", "当たりを検知すると巻き上げて再び投げます。"),
                e("module.INVENTORY_CLEANER.description", "アイテムを捨てずに同種スタックをまとめます。"),
                e("module.HOTBAR_PRESETS.description", "アイテム ID を使って3つのホットバー配置を保存します。"),
                e("module.PROFILE_MANAGER.description", "設定プロファイルをインポート、エクスポート、バックアップします。")
        ));

        // Complete Indonesian coverage for every ClickGUI label and module.
        translate("id", Map.ofEntries(
                e("gui.brand", "SKYOP"),
                e("gui.client", "KLIEN / 26.3"),
                e("gui.hud_edit", "EDIT HUD"),
                e("gui.hud_edit_on", "EDIT HUD AKTIF"),
                e("gui.type_to_filter", "ketik untuk menyaring"),
                e("gui.search_prefix", "CARI"),
                e("gui.no_matching_modules", "Tidak ada modul yang cocok"),
                e("gui.on", "AKTIF"),
                e("gui.off", "MATI"),
                e("gui.press_a_key", "TEKAN SEBUAH TOMBOL..."),
                e("gui.unbound", "BELUM DIATUR"),
                e("gui.keybind", "TOMBOL"),
                e("gui.edit_text", "UBAH TEKS"),
                e("gui.custom_hud_text", "TEKS HUD KUSTOM"),
                e("gui.edit_text_help", "Klik baris ini, lalu ketik. Tekan Enter untuk selesai."),
                e("gui.configuration_profiles", "PROFIL KONFIGURASI"),
                e("gui.export_json", "EKSPOR JSON"),
                e("gui.import_json", "IMPOR JSON"),
                e("gui.backup_now", "CADANGKAN SEKARANG"),
                e("gui.hotbar_layout_slots", "SLOT SUSUNAN HOTBAR"),
                e("gui.save_hotbar", "SIMPAN HOTBAR"),
                e("gui.load_hotbar", "MUAT HOTBAR"),
                e("gui.requires", "MEMBUTUHKAN"),
                e("gui.potion_effects", "Efek ramuan"),
                e("gui.active_effects_duration", "Efek aktif dan sisa durasinya"),
                e("gui.main_hand_item", "Item di tangan utama"),
                e("gui.remaining_durability", "Persentase daya tahan tersisa"),
                e("gui.direction", "Arah"),
                e("gui.cardinal_heading", "Arah mata angin berdasarkan yaw pemain"),
                e("gui.latency", "Latensi"),
                e("gui.current_multiplayer_ping", "Ping multiplayer saat ini; tidak tersedia di dunia lokal"),
                e("gui.frame_rate", "Frame rate"),
                e("gui.current_fps", "Jumlah frame per detik klien saat ini"),
                e("gui.loaded_entities", "Entitas yang dimuat"),
                e("gui.nearby_loaded_entities", "Entitas terdekat yang sudah dilacak oleh klien"),
                e("gui.armor_policy", "Aturan armor"),
                e("gui.only_empty_armor_slots", "Hanya mengisi slot armor kosong; tidak mengganti armor yang dipakai"),
                e("gui.tool_selection", "Pemilihan alat"),
                e("gui.fastest_tool", "Memilih alat hotbar tercepat untuk blok yang ditargetkan"),
                e("gui.fishing_automation", "Otomatisasi memancing"),
                e("gui.reel_and_recast", "Menggulung saat umpan dimakan lalu melempar kembali; aturan server tetap berlaku"),
                e("gui.safe_cleanup", "Pembersihan aman"),
                e("gui.stack_consolidation", "Menggabungkan stack sejenis; tidak menjatuhkan atau menghapus item"),
                e("gui.three_layouts", "Tiga susunan"),
                e("gui.save_restore_hotbar", "Menyimpan dan memulihkan susunan item hotbar"),
                e("gui.json_profiles", "Profil JSON"),
                e("gui.profile_slots_backup_restore", "Tiga slot, impor/ekspor JSON, pencadangan, dan pemulihan"),
                e("gui.profile_action_completed", "Aksi profil selesai"),
                e("gui.profile_error", "Kesalahan profil"),
                e("gui.hotbar_error", "Kesalahan hotbar"),
                e("gui.saved_hotbar", "Hotbar disimpan"),
                e("gui.loaded_hotbar", "Hotbar dimuat"),
                e("gui.hud_color_updated", "Warna HUD diperbarui"),
                e("gui.enter_hex_digits", "Masukkan 6 digit heksadesimal"),
                e("gui.keybind_updated", "Tombol pintasan diperbarui"),
                e("gui.hud_text_edit_hint", "Klik baris ini, lalu ketik. Tekan Enter untuk selesai."),
                e("gui.profile_saved", "Profil disimpan"),
                e("gui.profile_loaded", "Profil dimuat"),
                e("gui.save", "SIMPAN"),
                e("gui.load", "MUAT"),
                e("gui.search", "CARI MODUL"),
                e("gui.main_hud", "HUD Utama"),
                e("gui.compass_direction", "Kompas / Arah"),
                e("gui.drag_to_move", "SERET UNTUK PINDAH"),
                e("gui.info", "INFO"),
                e("category.combat", "Pertarungan"),
                e("category.movement", "Pergerakan"),
                e("category.visual", "Visual"),
                e("category.player", "Pemain"),
                e("category.inventory", "Inventaris"),
                e("category.favorites", "Favorit"),
                e("module.KILL_AURA.title", "Aura Serang"),
                e("module.KILL_AURA.description", "Menyerang target terdekat yang memenuhi pengaturan."),
                e("module.TRIGGER_BOT.title", "Serang Otomatis"),
                e("module.TRIGGER_BOT.description", "Menyerang target valid yang berada tepat di crosshair."),
                e("module.AUTO_CLICKER.title", "Klik Otomatis"),
                e("module.AUTO_CLICKER.description", "Mengulangi serangan selama tombol serang ditekan."),
                e("module.AUTO_SPRINT.title", "Sprint Otomatis"),
                e("module.AUTO_SPRINT.description", "Melakukan sprint saat pemain bergerak maju."),
                e("module.SPEED.title", "Kecepatan"),
                e("module.SPEED.description", "Memberikan peningkatan kecepatan gerak horizontal yang dapat diatur."),
                e("module.FLIGHT.title", "Terbang"),
                e("module.FLIGHT.description", "Kontrol terbang lokal menggunakan tombol lompat dan jongkok."),
                e("module.AUTO_JUMP.title", "Lompat Otomatis"),
                e("module.AUTO_JUMP.description", "Melompat secara otomatis saat bergerak maju."),
                e("module.STEP_ASSIST.title", "Bantuan Langkah"),
                e("module.STEP_ASSIST.description", "Melompat saat gerakan maju terhalang dinding."),
                e("module.SPIDER.title", "Memanjat Dinding"),
                e("module.SPIDER.description", "Memanjat dinding saat bergerak ke arahnya."),
                e("module.ENTITY_ESP.title", "ESP Entitas"),
                e("module.ENTITY_ESP.description", "Menyorot entitas hidup di sekitar yang sudah dimuat."),
                e("module.GAMMA_BOOST.title", "Gamma Maksimal"),
                e("module.GAMMA_BOOST.description", "Mengatur gamma vanilla ke nilai normal maksimum."),
                e("module.HUD.title", "HUD"),
                e("module.HUD.description", "Menampilkan modul aktif, koordinat, dan FPS."),
                e("module.TARGET_HUD.title", "HUD Target"),
                e("module.TARGET_HUD.description", "Menampilkan health dan jarak target di crosshair."),
                e("module.KEYSTROKES_HUD.title", "HUD Tombol"),
                e("module.KEYSTROKES_HUD.description", "Menampilkan status tombol gerakan di layar."),
                e("module.ARMOR_HUD.title", "HUD Armor"),
                e("module.ARMOR_HUD.description", "Menampilkan status health dan armor."),
                e("module.SPEEDOMETER.title", "Pengukur Kecepatan"),
                e("module.SPEEDOMETER.description", "Menampilkan kecepatan gerak horizontal dalam blok per detik."),
                e("module.CPS_HUD.title", "HUD CPS"),
                e("module.CPS_HUD.description", "Menampilkan jumlah aksi serangan berhasil per detik."),
                e("module.NO_FALL.title", "Anti Jatuh"),
                e("module.NO_FALL.description", "Mengosongkan penghitung jarak jatuh lokal."),
                e("module.POTION_HUD.title", "HUD Ramuan"),
                e("module.DURABILITY_HUD.title", "HUD Daya Tahan"),
                e("module.COMPASS_HUD.title", "HUD Kompas"),
                e("module.DIRECTION_HUD.title", "HUD Arah"),
                e("module.PING_HUD.title", "HUD Ping"),
                e("module.FPS_HUD.title", "HUD FPS"),
                e("module.RADAR_HUD.title", "Radar Entitas Terdekat"),
                e("module.TEXT_GUI.title", "Teks Kustom"),
                e("module.AUTO_EAT.title", "Makan Otomatis"),
                e("module.AUTO_ARMOR.title", "Armor Otomatis"),
                e("module.AUTO_TOOL.title", "Alat Otomatis"),
                e("module.AUTO_REFILL.title", "Isi Ulang Otomatis"),
                e("module.AUTO_FISH.title", "Pancing Otomatis"),
                e("module.INVENTORY_CLEANER.title", "Pembersih Inventaris"),
                e("module.HOTBAR_PRESETS.title", "Preset Hotbar"),
                e("module.PROFILE_MANAGER.title", "Pengelola Profil"),
                e("setting.hunger_threshold", "Ambang lapar"),
                e("setting.eat_below", "Mulai makan di bawah nilai ini"),
                e("setting.refill_threshold", "Ambang isi ulang"),
                e("setting.refill_low", "Isi ulang saat jumlah stack rendah")
        ));
        translate("id", Map.ofEntries(
                e("gui.client_ready", "KLIEN SIAP"),
                e("gui.favorites", "FAVORIT")
        ));
        translate("id", Map.ofEntries(
                e("setting.attack_delay", "Jeda Serangan"),
                e("setting.attack_delay_desc", "Jumlah tick di antara percobaan serangan yang berhasil."),
                e("setting.hunt_range", "Jarak Berburu"),
                e("setting.hunt_range_desc", "Jarak maksimum mob agresif yang sudah dimuat."),
                e("setting.placement_delay", "Jeda Pemasangan"),
                e("setting.placement_delay_desc", "Jumlah tick di antara percobaan pemasangan blok."),
                e("setting.jump_multiplier", "Pengali Lompatan"),
                e("setting.jump_multiplier_desc", "Pengali yang diterapkan pada dorongan lompatan lokal."),
                e("setting.show_items", "Tampilkan Item Jatuh"),
                e("setting.highlight_items", "Sorot entitas item yang sudah dimuat oleh client."),
                e("setting.hud_accent", "Warna Aksen HUD"),
                e("setting.hud_accent_desc", "Warna aksen yang digunakan panel HUD SkyOP."),
                e("setting.keybind_description", "Tombol pintasan untuk mengaktifkan atau menonaktifkan modul.")
        ));
        translate("id", Map.ofEntries(
                e("dependency.join_world", "Masuk ke dunia terlebih dahulu."),
                e("dependency.player_required", "Entitas pemain diperlukan."),
                e("dependency.inventory_required", "Buka inventaris pemain terlebih dahulu."),
                e("dependency.fishing_rod_required", "Pegang pancing di tangan utama."),
                e("dependency.block_target_required", "Arahkan bidikan ke sebuah blok terlebih dahulu.")
        ));
        translate("id", Map.ofEntries(
                e("module.AUTO_MLG.title", "MLG Otomatis"),
                e("module.AUTO_MLG.description", "Mencoba memasang water bucket dengan aksi normal saat jatuh menuju blok valid."),
                e("module.AUTO_TOTEM.title", "Totem Otomatis"),
                e("module.AUTO_TOTEM.description", "Memindahkan totem ke offhand saat health rendah."),
                e("setting.auto_mlg_fall", "Jarak pemicu MLG"),
                e("setting.auto_mlg_fall_desc", "Jarak jatuh minimum sebelum mencoba menggunakan water bucket."),
                e("setting.auto_totem_health", "Ambang health totem"),
                e("setting.auto_totem_health_desc", "Memindahkan totem ke offhand saat health sama dengan atau di bawah nilai ini.")
        ));
        translate("id", Map.ofEntries(
                e("module.FAST_BREAK.title", "Hancur Cepat"),
                e("module.FAST_BREAK.description", "Meningkatkan kecepatan mining lokal setara Haste I–VII."),
                e("setting.fast_break_level", "Level Haste"),
                e("setting.fast_break_level_desc", "Haste VII setara pengali kecepatan mining lokal 2,4x; aturan server tetap berlaku.")
        ));

        // New utility pack — English labels remain the enum/source fallbacks.
        put("setting.zoom_fov", "Zoom FOV");
        put("setting.zoom_fov_desc", "Client field of view while Zoom is enabled.");
        put("setting.container_range", "Container scan range");
        put("setting.container_range_desc", "Search radius in already-loaded client chunks.");
        put("setting.low_health_threshold", "Low-health threshold");
        put("setting.low_health_desc", "Notify once when health falls to this value or lower.");
        put("setting.armor_alert_percent", "Armor durability threshold");
        put("setting.armor_alert_desc", "Notify when any equipped damageable armor reaches this remaining percentage.");

        translate("id", Map.ofEntries(
                e("module.AUTO_WALK.title", "Jalan Otomatis"),
                e("module.AUTO_WALK.description", "Menahan input gerak maju normal selama modul aktif."),
                e("module.ZOOM.title", "Zoom"),
                e("module.ZOOM.description", "Mempersempit FOV klien sementara menggunakan nilai FOV yang diatur."),
                e("module.CONTAINER_FINDER.title", "Pencari Peti"),
                e("module.CONTAINER_FINDER.description", "Mencantumkan peti, barrel, dan ender chest dalam chunk sekitar yang sudah dimuat klien."),
                e("module.AUTO_RESPAWN.title", "Respawn Otomatis"),
                e("module.AUTO_RESPAWN.description", "Mengirim permintaan respawn normal setelah mati; server tetap menentukan penerimaannya."),
                e("module.LOW_HEALTH_ALERT.title", "Peringatan Health Rendah"),
                e("module.LOW_HEALTH_ALERT.description", "Memberi notifikasi saat health melewati ambang rendah yang diatur."),
                e("module.ARMOR_ALERT.title", "Peringatan Armor"),
                e("module.ARMOR_ALERT.description", "Memberi notifikasi saat daya tahan armor mencapai persentase yang diatur."),
                e("setting.zoom_fov", "FOV Zoom"),
                e("setting.zoom_fov_desc", "Field of view klien saat Zoom aktif."),
                e("setting.container_range", "Jarak pemindaian peti"),
                e("setting.container_range_desc", "Radius pencarian pada chunk yang sudah dimuat klien."),
                e("setting.low_health_threshold", "Ambang health rendah"),
                e("setting.low_health_desc", "Kirim notifikasi sekali saat health mencapai nilai ini atau lebih rendah."),
                e("setting.armor_alert_percent", "Ambang daya tahan armor"),
                e("setting.armor_alert_desc", "Kirim notifikasi jika armor yang dipakai tersisa pada persentase ini.")
        ));
    }

    static {
        translate("id", Map.ofEntries(
                e("category.world", "Dunia"),
                e("category.safe", "Aman"),
                e("category.dangerous", "Bahaya"),
                e("gui.risk_safe", "AMAN"),
                e("gui.risk_dangerous", "BAHAYA"),
                e("setting.aim_fov", "Sudut pandang target"),
                e("setting.target_cone", "Lebar bidang target penuh hingga 360 derajat"),
                e("setting.clear_view", "Wajibkan pandangan tanpa halangan; nonaktif memindai target yang sudah dimuat di balik blok (serangan dapat ditolak server)")
        ));
    }

    private SkyOPLanguage() { }

    private static Map.Entry<String, String> e(String key, String value) {
        return Map.entry(key, value);
    }

    private static void put(String key, String value) { EN.put(key, value); }

    private static void translate(String code, Map<String, String> values) {
        DICTIONARIES.computeIfAbsent(code, ignored -> new LinkedHashMap<>()).putAll(values);
    }

    public static List<Language> languages() { return LANGUAGES; }

    public static String code() { return languageCode; }

    public static Language current() {
        return LANGUAGES.stream().filter(language -> language.code().equals(languageCode))
                .findFirst().orElse(LANGUAGES.getFirst());
    }

    public static void set(String code) {
        languageCode = LANGUAGES.stream().anyMatch(language -> language.code().equals(code)) ? code : "id";
    }

    public static void next() {
        int currentIndex = 0;
        for (int i = 0; i < LANGUAGES.size(); i++) {
            if (LANGUAGES.get(i).code().equals(languageCode)) currentIndex = i;
        }
        languageCode = LANGUAGES.get((currentIndex + 1) % LANGUAGES.size()).code();
    }

    public static String text(String key) {
        if ("en".equals(languageCode)) return EN.getOrDefault(key, key);
        return DICTIONARIES.getOrDefault(languageCode, Map.of())
                .getOrDefault(key, EN.getOrDefault(key, key));
    }

    public static String moduleTitle(String name, String fallback) {
        return text("module." + name + ".title").equals("module." + name + ".title")
                ? fallback : text("module." + name + ".title");
    }

    public static String moduleDescription(String name, String fallback) {
        return text("module." + name + ".description").equals("module." + name + ".description")
                ? fallback : text("module." + name + ".description");
    }

    
    // Indonesian translations for the eleven SkyOP 4.0 HUD modules.
    static {
        translate("id", Map.ofEntries(
                e("module.WORLD_INFO_HUD.title", "Info Dunia"),
                e("module.WORLD_INFO_HUD.description", "Menampilkan dimensi, bioma, dan kondisi cuaca saat ini."),
                e("module.CHUNK_INFO_HUD.title", "Info Chunk"),
                e("module.CHUNK_INFO_HUD.description", "Menampilkan koordinat chunk dan koordinat blok lokal."),
                e("module.PERFORMANCE_HUD.title", "HUD Performa"),
                e("module.PERFORMANCE_HUD.description", "Menampilkan FPS dan penggunaan memori JVM secara langsung."),
                e("module.SESSION_TIMER_HUD.title", "Timer Sesi"),
                e("module.SESSION_TIMER_HUD.description", "Menampilkan lama waktu sesi di dunia saat ini."),
                e("module.FOOD_HUD.title", "HUD Makanan"),
                e("module.FOOD_HUD.description", "Menampilkan tingkat lapar dan saturasi saat ini."),
                e("module.AIR_HUD.title", "HUD Udara"),
                e("module.AIR_HUD.description", "Menampilkan sisa pasokan udara."),
                e("module.EXPERIENCE_HUD.title", "HUD Pengalaman"),
                e("module.EXPERIENCE_HUD.description", "Menampilkan level pengalaman dan progres."),
                e("module.ITEM_INFO_HUD.title", "Info Item"),
                e("module.ITEM_INFO_HUD.description", "Menampilkan nama item tangan utama, jumlah, dan daya tahan."),
                e("module.OFFHAND_HUD.title", "Info Offhand"),
                e("module.OFFHAND_HUD.description", "Menampilkan item dan jumlah di tangan kedua."),
                e("module.BLOCK_INFO_HUD.title", "Info Blok"),
                e("module.BLOCK_INFO_HUD.description", "Menampilkan blok yang ditargetkan dan koordinatnya."),
                e("module.ARMOR_DETAIL_HUD.title", "Detail Armor"),
                e("module.ARMOR_DETAIL_HUD.description", "Menampilkan bagian armor yang dipakai dan sisa daya tahannya."),
                e("module.AUTO_HUNT.title", "Berburu Otomatis"),
                e("module.AUTO_HUNT.description", "Menyerang mob agresif terdekat yang sudah dimuat tanpa menggerakkan kamera."),
                e("module.AUTO_BRIDGE.title", "Jembatan Otomatis"),
                e("module.AUTO_BRIDGE.description", "Menempatkan blok hotbar terpilih di bawah pemain saat bergerak."),
                e("module.INFINITE_JUMP.title", "Lompat Berulang"),
                e("module.INFINITE_JUMP.description", "Memungkinkan satu lompatan tambahan saat tombol lompat ditekan kembali di udara."),
                e("module.JUMP_BOOST.title", "Peningkat Lompatan"),
                e("module.JUMP_BOOST.description", "Mengatur pengali dorongan lompatan lokal."),
                e("module.ITEM_ESP.title", "ESP Item"),
                e("module.ITEM_ESP.description", "Memasukkan entitas item yang terjatuh ke filter sorotan Entity ESP."),
                e("module.WAYPOINTS.title", "Titik Koordinat"),
                e("module.WAYPOINTS.description", "Menampilkan koordinat tersimpan dan jarak; tekan V untuk menyimpan titik."),
                e("module.MINIMAP.title", "Peta Mini"),
                e("module.MINIMAP.description", "Menampilkan entitas sekitar yang sudah dimuat oleh klien."),
                e("module.NIGHT_VISION.title", "Penglihatan Malam"),
                e("module.NIGHT_VISION.description", "Meningkatkan visibilitas lokal melalui pengaturan gamma klien.")
        ));
    }
}
