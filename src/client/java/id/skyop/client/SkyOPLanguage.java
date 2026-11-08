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

    private static final List<Language> LANGUAGES = List.of(
            new Language("en", "English"),
            new Language("id", "Bahasa Indonesia"),
            new Language("es", "Español"),
            new Language("pt", "Português"),
            new Language("fr", "Français"),
            new Language("zh", "简体中文"),
            new Language("ja", "日本語")
    );

    private static final Map<String, String> EN = new LinkedHashMap<>();
    private static final Map<String, Map<String, String>> DICTIONARIES = new LinkedHashMap<>();
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

        translate("id", Map.ofEntries(
                e("gui.control_center", "PUSAT KONTROL MODUL"), e("gui.subtitle", "Profil • Modul • Pengaturan langsung"),
                e("gui.ready", "KLIEN SIAP"), e("gui.categories", "KATEGORI"), e("gui.active_modules", "MODUL AKTIF"),
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
                e("setting.refill_threshold", "Ambang isi ulang"), e("setting.refill_low", "Isi ulang saat jumlah stack di bawah nilai ini")
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
                e("setting.refill_threshold", "Umbral de recarga"), e("setting.refill_low", "Recargar cuando la pila sea baja")
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
                e("setting.refill_threshold", "Limite de reabastecimento"), e("setting.refill_low", "Reabastecer quando a pilha estiver baixa")
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
                e("setting.refill_threshold", "Seuil de recharge"), e("setting.refill_low", "Recharger quand la pile est basse")
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
                e("setting.refill_threshold", "补充阈值"), e("setting.refill_low", "物品数量较低时补充")
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
                e("setting.refill_threshold", "補充しきい値"), e("setting.refill_low", "スタックが少ないと補充")
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
        languageCode = LANGUAGES.stream().anyMatch(language -> language.code().equals(code)) ? code : "en";
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
}
