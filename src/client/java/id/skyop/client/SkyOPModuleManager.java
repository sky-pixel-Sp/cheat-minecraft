package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Module registry and central lifecycle dispatcher. Runtime behaviour belongs to
 * concrete SkyOPModule subclasses; this class only creates, stores and dispatches them.
 */
public final class SkyOPModuleManager {
    private static final Map<SkyOPModuleId, SkyOPModule> MODULES = new EnumMap<>(SkyOPModuleId.class);
    private static final Set<SkyOPModuleId> ACTIVE = EnumSet.noneOf(SkyOPModuleId.class);

    private static final List<SkyOPModuleId> MOVEMENT_TICK_ORDER = List.of(
            SkyOPModuleId.AUTO_SPRINT, SkyOPModuleId.SPEED, SkyOPModuleId.FLIGHT,
            SkyOPModuleId.AUTO_JUMP, SkyOPModuleId.STEP_ASSIST, SkyOPModuleId.SPIDER,
            SkyOPModuleId.AUTO_BRIDGE, SkyOPModuleId.JUMP_BOOST, SkyOPModuleId.INFINITE_JUMP,
            SkyOPModuleId.AUTO_MLG, SkyOPModuleId.AUTO_WALK
    );
    private static final List<SkyOPModuleId> COMBAT_TICK_ORDER = List.of(
            SkyOPModuleId.KILL_AURA, SkyOPModuleId.AUTO_HUNT,
            SkyOPModuleId.TRIGGER_BOT, SkyOPModuleId.AUTO_CLICKER
    );
    private static final List<SkyOPModuleId> INVENTORY_TICK_ORDER = List.of(
            SkyOPModuleId.AUTO_EAT, SkyOPModuleId.AUTO_ARMOR, SkyOPModuleId.AUTO_TOTEM, SkyOPModuleId.AUTO_TOOL,
            SkyOPModuleId.AUTO_REFILL, SkyOPModuleId.INVENTORY_CLEANER, SkyOPModuleId.AUTO_FISH
    );
    private static final List<SkyOPModuleId> PLAYER_TICK_ORDER = List.of(
            SkyOPModuleId.LOW_HEALTH_ALERT, SkyOPModuleId.ARMOR_ALERT
    );
    private static final List<SkyOPModuleId> VISUAL_TICK_ORDER = List.of(
            SkyOPModuleId.ZOOM, SkyOPModuleId.CONTAINER_FINDER
    );

    private SkyOPModuleManager() { }

    public static void initialize() {
        MODULES.clear();
        ACTIVE.clear();
        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            List<SkyOPSetting<?>> settings = settingsFor(module);
            MODULES.put(module, createModule(module, settings));
        }
    }

    public static List<SkyOPModule> all() {
        return List.copyOf(MODULES.values());
    }

    public static SkyOPModule get(SkyOPModuleId module) {
        return MODULES.get(module);
    }

    /** True when the module lifecycle has actually entered its enabled state. */
    public static boolean isActive(SkyOPModuleId module) {
        return module != null && ACTIVE.contains(module);
    }

    /** Number of modules whose enable lifecycle is currently active. */
    public static int activeCount() {
        return ACTIVE.size();
    }

    public static List<SkyOPSetting<?>> settings(SkyOPModuleId module) {
        SkyOPModule runtime = MODULES.get(module);
        return runtime == null ? List.of() : runtime.settings();
    }

    public static void syncAllStates(Minecraft client) {
        for (SkyOPModule module : MODULES.values()) {
            SkyOPModuleId type = module.type();
            boolean enabled = SkyOPCore.isEnabled(type);
            if (enabled && !ACTIVE.contains(type) && isModuleReady(client, module)) {
                if (invoke(client, module, "onEnable", () -> module.onEnable(client))) ACTIVE.add(type);
            } else if (!enabled && ACTIVE.remove(type)) {
                invoke(client, module, "onDisable", () -> module.onDisable(client));
            }
        }
    }

    public static void saveSettings(Properties properties) {
        for (SkyOPModule module : MODULES.values()) {
            for (SkyOPSetting<?> setting : module.settings()) {
                if (setting instanceof KeybindSetting || setting instanceof ActionSetting) continue;
                properties.setProperty(settingKey(module.type(), setting), setting.serialize());
            }
        }
    }

    public static void loadSettings(Properties properties) {
        for (SkyOPModule module : MODULES.values()) {
            for (SkyOPSetting<?> setting : module.settings()) {
                if (setting instanceof KeybindSetting || setting instanceof ActionSetting) continue;
                String value = properties.getProperty(settingKey(module.type(), setting));
                if (value != null) setting.deserialize(value);
            }
        }
    }

    private static String settingKey(SkyOPModuleId module, SkyOPSetting<?> setting) {
        return "setting." + module.name() + "." + setting.id();
    }

    public static void tick(Minecraft client) {
        if (client == null || client.player == null || client.level == null) return;
        if (client.gameMode == null) {
            SkyOPInventoryManager.stop(client);
            return;
        }
        syncAllStates(client);
        try {
            SkyOPMovementModules.trackDisabledJumpInputs(client);
        } catch (VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable error) {
            SkyOPDiagnostics.recordSystemError("disabled jump input tracking", error);
        }

        tickModules(client, MOVEMENT_TICK_ORDER);
        tickModule(client, SkyOPModuleId.NO_FALL);

        // Shared visual state must not prevent isolated module callbacks from running.
        try {
            SkyOPVisualModules.tickShared(client);
        } catch (VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable error) {
            SkyOPDiagnostics.recordSystemError("shared visual tick", error);
        }
        tickModules(client, VISUAL_TICK_ORDER);

        tickModules(client, COMBAT_TICK_ORDER);
        tickModules(client, INVENTORY_TICK_ORDER);
        tickModules(client, PLAYER_TICK_ORDER);
    }

    private static void tickModules(Minecraft client, List<SkyOPModuleId> order) {
        for (SkyOPModuleId type : order) tickModule(client, type);
    }

    private static void tickModule(Minecraft client, SkyOPModuleId type) {
        SkyOPModule module = MODULES.get(type);
        if (module != null && SkyOPCore.isEnabled(type) && isModuleReady(client, module)) {
            invoke(client, module, "onTick", () -> module.onTick(client));
        }
    }

    private static boolean isModuleReady(Minecraft client, SkyOPModule module) {
        try {
            boolean ready = module.isReady(client);
            SkyOPDiagnostics.recordReadiness(module.type(), ready,
                    ready ? "Ready" : SkyOPModuleDependencies.missingRequirement(client, module.type()));
            return ready;
        } catch (VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable error) {
            handleModuleError(client, module, "isReady", error);
            return false;
        }
    }

    private static boolean invoke(Minecraft client, SkyOPModule module, String phase, Runnable callback) {
        try {
            callback.run();
            SkyOPDiagnostics.recordSuccess(module.type());
            return true;
        } catch (VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable error) {
            handleModuleError(client, module, phase, error);
            return false;
        }
    }

    private static void handleModuleError(Minecraft client, SkyOPModule module, String phase, Throwable error) {
        SkyOPModuleId id = module.type();
        boolean disable = SkyOPDiagnostics.recordError(id, phase, error);
        boolean enableFailed = "onEnable".equals(phase);
        if (enableFailed || disable) {
            try {
                module.onDisable(client);
            } catch (VirtualMachineError | LinkageError fatal) {
                throw fatal;
            } catch (Throwable cleanupError) {
                SkyOPDiagnostics.recordSystemError(id.name() + " / onDisable after failure", cleanupError);
            }
        }
        if (disable) {
            SkyOPState.ENABLED.remove(id);
            ACTIVE.remove(id);
            SkyOPDiagnostics.markAutoDisabled(id);
            if (client != null && client.player != null) {
                client.player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "[SkyOP] " + id.name() + " auto-disabled after 3 consecutive errors."));
            }
        }
    }

    public static void render2D(Minecraft client, GuiGraphicsExtractor graphics) {
        if (client == null || graphics == null || !MODULES.containsKey(SkyOPModuleId.HUD)) return;
        try {
            SkyOPHudRenderer.render(client, graphics);
        } catch (VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable error) {
            SkyOPDiagnostics.recordSystemError("shared HUD renderer", error);
        }
        for (SkyOPModule module : MODULES.values()) {
            if (SkyOPCore.isEnabled(module.type()) && isModuleReady(client, module)) {
                invoke(client, module, "onRender2D", () -> module.onRender2D(client, graphics));
            }
        }
    }

    public static void render3D(Minecraft client, Object renderContext) {
        if (client == null) return;
        for (SkyOPModule module : MODULES.values()) {
            if (SkyOPCore.isEnabled(module.type()) && isModuleReady(client, module)) {
                invoke(client, module, "onRender3D", () -> module.onRender3D(client, renderContext));
            }
        }
    }

    public static void onWorldChange(Minecraft client) {
        for (SkyOPModule module : MODULES.values()) {
            invoke(client, module, "onWorldChange", () -> module.onWorldChange(client));
        }
        runSharedCleanup("movement cleanup", () -> SkyOPMovementModules.cleanup(client));
        runSharedCleanup("visual cleanup", () -> SkyOPVisualModules.cleanup(client));
        runSharedCleanup("player cleanup", () -> { SkyOPPlayerModules.cleanup(); });
        runSharedCleanup("inventory cleanup", () -> SkyOPInventoryManager.stop(client));
        runSharedCleanup("telemetry reset", SkyOPTelemetry::reset);
        runSharedCleanup("HUD reset", SkyOPHudRenderer::resetSessionTimer);
        ACTIVE.clear();
    }

    private static void runSharedCleanup(String context, Runnable action) {
        try {
            action.run();
        } catch (VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable error) {
            SkyOPDiagnostics.recordSystemError(context, error);
        }
    }

    private static SkyOPModule createModule(SkyOPModuleId module, List<SkyOPSetting<?>> settings) {
        return switch (module) {
            case AUTO_SPRINT -> new SkyOPMovementModules.AutoSprint(settings);
            case SPEED -> new SkyOPMovementModules.Speed(settings);
            case FLIGHT -> new SkyOPMovementModules.Flight(settings);
            case AUTO_JUMP -> new SkyOPMovementModules.AutoJump(settings);
            case STEP_ASSIST -> new SkyOPMovementModules.StepAssist(settings);
            case SPIDER -> new SkyOPMovementModules.Spider(settings);
            case AUTO_BRIDGE -> new SkyOPMovementModules.AutoBridge(settings);
            case JUMP_BOOST -> new SkyOPMovementModules.JumpBoost(settings);
            case AUTO_MLG -> new SkyOPMovementModules.AutoMlg(settings);
            case AUTO_WALK -> new SkyOPMovementModules.AutoWalk(settings);
            case INFINITE_JUMP -> new SkyOPMovementModules.InfiniteJump(settings);
            case NO_FALL -> new SkyOPMovementModules.NoFall(settings);
            case AUTO_RESPAWN -> new SkyOPPlayerModules.AutoRespawn(settings);
            case LOW_HEALTH_ALERT -> new SkyOPPlayerModules.LowHealthAlert(settings);
            case ARMOR_ALERT -> new SkyOPPlayerModules.ArmorAlert(settings);
            case FAST_BREAK -> new SkyOPPlayerModules.FastBreak(settings);

            case KILL_AURA -> new SkyOPCombatModules.KillAura(settings);
            case AUTO_HUNT -> new SkyOPCombatModules.AutoHunt(settings);
            case TRIGGER_BOT -> new SkyOPCombatModules.TriggerBot(settings);
            case AUTO_CLICKER -> new SkyOPCombatModules.AutoClicker(settings);

            case ENTITY_ESP -> new SkyOPVisualModules.EntityEsp(settings);
            case ITEM_ESP -> new SkyOPVisualModules.ItemEsp(settings);
            case ZOOM -> new SkyOPVisualModules.Zoom(settings);
            case CONTAINER_FINDER -> new SkyOPVisualModules.ContainerFinder(settings);
            case GAMMA_BOOST -> new SkyOPVisualModules.GammaBoost(settings);
            case NIGHT_VISION -> new SkyOPVisualModules.NightVision(settings);
            case WAYPOINTS -> new SkyOPVisualModules.Waypoints(settings);
            case MINIMAP -> new SkyOPVisualModules.MiniMap(settings);
            case HUD -> new SkyOPVisualModules.Hud(settings);
            case TARGET_HUD -> new SkyOPVisualModules.TargetHud(settings);
            case KEYSTROKES_HUD -> new SkyOPVisualModules.KeystrokesHud(settings);
            case ARMOR_HUD -> new SkyOPVisualModules.ArmorHud(settings);
            case SPEEDOMETER -> new SkyOPVisualModules.Speedometer(settings);
            case CPS_HUD -> new SkyOPVisualModules.CpsHud(settings);
            case POTION_HUD -> new SkyOPVisualModules.PotionHud(settings);
            case DURABILITY_HUD -> new SkyOPVisualModules.DurabilityHud(settings);
            case COMPASS_HUD -> new SkyOPVisualModules.CompassHud(settings);
            case DIRECTION_HUD -> new SkyOPVisualModules.DirectionHud(settings);
            case PING_HUD -> new SkyOPVisualModules.PingHud(settings);
            case FPS_HUD -> new SkyOPVisualModules.FpsHud(settings);
            case RADAR_HUD -> new SkyOPVisualModules.RadarHud(settings);
            case TEXT_GUI -> new SkyOPVisualModules.TextHud(settings);
            case WORLD_INFO_HUD -> new SkyOPVisualModules.WorldInfoHud(settings);
            case CHUNK_INFO_HUD -> new SkyOPVisualModules.ChunkInfoHud(settings);
            case PERFORMANCE_HUD -> new SkyOPVisualModules.PerformanceHud(settings);
            case SESSION_TIMER_HUD -> new SkyOPVisualModules.SessionTimerHud(settings);
            case FOOD_HUD -> new SkyOPVisualModules.FoodHud(settings);
            case AIR_HUD -> new SkyOPVisualModules.AirHud(settings);
            case EXPERIENCE_HUD -> new SkyOPVisualModules.ExperienceHud(settings);
            case ITEM_INFO_HUD -> new SkyOPVisualModules.ItemInfoHud(settings);
            case OFFHAND_HUD -> new SkyOPVisualModules.OffhandHud(settings);
            case BLOCK_INFO_HUD -> new SkyOPVisualModules.BlockInfoHud(settings);
            case ARMOR_DETAIL_HUD -> new SkyOPVisualModules.ArmorDetailHud(settings);

            case AUTO_EAT -> new SkyOPInventoryModules.AutoEat(settings);
            case AUTO_ARMOR -> new SkyOPInventoryModules.AutoArmor(settings);
            case AUTO_TOTEM -> new SkyOPInventoryModules.AutoTotem(settings);
            case AUTO_TOOL -> new SkyOPInventoryModules.AutoTool(settings);
            case AUTO_REFILL -> new SkyOPInventoryModules.AutoRefill(settings);
            case AUTO_FISH -> new SkyOPInventoryModules.AutoFish(settings);
            case INVENTORY_CLEANER -> new SkyOPInventoryModules.InventoryCleaner(settings);
            case HOTBAR_PRESETS -> new SkyOPInventoryModules.HotbarPresets(settings);
            case PROFILE_MANAGER -> new SkyOPInventoryModules.ProfileManager(settings);
        };
    }

    /**
     * Settings live on each registered module instance. UI code never maintains
     * a second module-to-setting switch or a separate mutable settings snapshot.
     */
    private static List<SkyOPSetting<?>> settingsFor(SkyOPModuleId module) {
        List<SkyOPSetting<?>> settings = new ArrayList<>();
        settings.add(new KeybindSetting(
                "keybind." + module.name(),
                "lang:gui.keybind",
                "lang:setting.keybind_description",
                SkyOPCore.moduleKeyCode(module),
                () -> SkyOPCore.moduleKeyCode(module),
                value -> SkyOPCore.setModuleKeybind(module, value)
        ));

        switch (module) {
            case KILL_AURA -> {
                settings.add(bool("targetPlayers", "setting.target_players", "setting.include_other_players",
                        () -> SkyOPState.targetPlayers, value -> SkyOPState.targetPlayers = value));
                settings.add(bool("targetMobs", "setting.target_mobs", "setting.include_hostile_mobs",
                        () -> SkyOPState.targetMobs, value -> SkyOPState.targetMobs = value));
                settings.add(bool("auraRotate", "setting.smooth_rotation", "setting.rotate_target",
                        () -> SkyOPState.auraRotate, value -> SkyOPState.auraRotate = value));
                settings.add(bool("auraRequireLineOfSight", "setting.line_of_sight", "setting.clear_view",
                        () -> SkyOPState.auraRequireLineOfSight, value -> SkyOPState.auraRequireLineOfSight = value));
                settings.add(number("auraRange", "setting.attack_range", "setting.distance_blocks",
                        () -> SkyOPState.auraRange, value -> SkyOPState.auraRange = value));
                settings.add(bool("auraTargetLowestHealth", "setting.lowest_health", "setting.weakest_target",
                        () -> SkyOPState.auraTargetLowestHealth, value -> SkyOPState.auraTargetLowestHealth = value));
                settings.add(number("auraFov", "setting.aim_fov", "setting.target_cone",
                        () -> SkyOPState.auraFov, value -> SkyOPState.auraFov = value));
                settings.add(number("auraRotationSpeed", "setting.rotation_speed", "setting.rotation_interpolation",
                        () -> SkyOPState.auraRotationSpeed, value -> SkyOPState.auraRotationSpeed = value));
                settings.add(number("auraDelayTicks", "setting.attack_delay", "setting.attack_delay_desc",
                        () -> (double) SkyOPState.auraDelayTicks, value -> SkyOPState.auraDelayTicks = value.intValue()));
            }
            case TRIGGER_BOT -> {
                settings.add(bool("targetPlayers", "setting.target_players", "setting.include_other_players",
                        () -> SkyOPState.targetPlayers, value -> SkyOPState.targetPlayers = value));
                settings.add(bool("targetMobs", "setting.target_mobs", "setting.include_hostile_mobs",
                        () -> SkyOPState.targetMobs, value -> SkyOPState.targetMobs = value));
                settings.add(number("triggerDelayTicks", "setting.trigger_delay", "setting.delay_between",
                        () -> (double) SkyOPState.triggerDelayTicks, value -> SkyOPState.triggerDelayTicks = value.intValue()));
            }
            case AUTO_CLICKER -> settings.add(number("autoClickerCps", "setting.clicks_per_second", "setting.vanilla_cooldown",
                    () -> (double) SkyOPState.autoClickerCps, value -> SkyOPState.autoClickerCps = value.intValue()));
            case AUTO_HUNT -> settings.add(number("autoHuntRange", "setting.hunt_range", "setting.hunt_range_desc",
                    () -> SkyOPState.autoHuntRange, value -> SkyOPState.autoHuntRange = value));
            case ZOOM -> settings.add(number("zoomFov", "setting.zoom_fov", "setting.zoom_fov_desc",
                    () -> (double) SkyOPState.zoomFov, value -> SkyOPState.zoomFov = value.intValue()));
            case CONTAINER_FINDER -> settings.add(number("containerFinderRange", "setting.container_range", "setting.container_range_desc",
                    () -> (double) SkyOPState.containerFinderRange, value -> SkyOPState.containerFinderRange = value.intValue()));
            case LOW_HEALTH_ALERT -> settings.add(number("lowHealthAlertThreshold", "setting.low_health_threshold", "setting.low_health_desc",
                    () -> (double) SkyOPState.lowHealthAlertThreshold, value -> SkyOPState.lowHealthAlertThreshold = value.intValue()));
            case ARMOR_ALERT -> settings.add(number("armorAlertPercent", "setting.armor_alert_percent", "setting.armor_alert_desc",
                    () -> (double) SkyOPState.armorAlertPercent, value -> SkyOPState.armorAlertPercent = value.intValue()));
            case FAST_BREAK -> settings.add(number("fastBreakLevel", "setting.fast_break_level", "setting.fast_break_level_desc",
                    () -> SkyOPState.fastBreakLevel, value -> SkyOPState.fastBreakLevel = value));
            case AUTO_MLG -> {
                settings.add(number("autoMlgFallDistance", "setting.auto_mlg_fall", "setting.auto_mlg_fall_desc",
                        () -> SkyOPState.autoMlgFallDistance, value -> SkyOPState.autoMlgFallDistance = value));
                settings.add(number("autoMlgPredictionWindow", "setting.auto_mlg_prediction_window",
                        "setting.auto_mlg_prediction_window_desc",
                        () -> (double) SkyOPState.autoMlgPredictionWindow,
                        value -> SkyOPState.autoMlgPredictionWindow = value.intValue()));
            }
            case AUTO_TOTEM -> settings.add(number("autoTotemHealth", "setting.auto_totem_health", "setting.auto_totem_health_desc",
                    () -> SkyOPState.autoTotemHealth, value -> SkyOPState.autoTotemHealth = value));
            case AUTO_BRIDGE -> settings.add(number("bridgeDelayTicks", "setting.placement_delay", "setting.placement_delay_desc",
                    () -> (double) SkyOPState.bridgeDelayTicks, value -> SkyOPState.bridgeDelayTicks = value.intValue()));
            case JUMP_BOOST -> settings.add(number("jumpBoostMultiplier", "setting.jump_multiplier", "setting.jump_multiplier_desc",
                    () -> SkyOPState.jumpBoostMultiplier, value -> SkyOPState.jumpBoostMultiplier = value));
            case SPEED -> settings.add(number("speedMultiplier", "setting.movement_multiplier", "setting.horizontal_multiplier",
                    () -> SkyOPState.speedMultiplier, value -> SkyOPState.speedMultiplier = value));
            case FLIGHT -> settings.add(number("flightSpeed", "setting.flight_speed", "setting.vertical_speed",
                    () -> SkyOPState.flightSpeed, value -> SkyOPState.flightSpeed = value));
            case AUTO_JUMP -> settings.add(number("autoJumpDelayTicks", "setting.jump_interval", "setting.ticks_between_jumps",
                    () -> (double) SkyOPState.autoJumpDelayTicks, value -> SkyOPState.autoJumpDelayTicks = value.intValue()));
            case SPIDER -> settings.add(number("spiderClimbSpeed", "setting.climb_speed", "setting.wall_vertical_speed",
                    () -> SkyOPState.spiderClimbSpeed, value -> SkyOPState.spiderClimbSpeed = value));
            case ENTITY_ESP -> {
                settings.add(bool("espPlayers", "setting.show_players", "setting.highlight_players",
                        () -> SkyOPState.espPlayers, value -> SkyOPState.espPlayers = value));
                settings.add(bool("espMobs", "setting.show_mobs", "setting.highlight_mobs",
                        () -> SkyOPState.espMobs, value -> SkyOPState.espMobs = value));
                settings.add(bool("espItems", "setting.show_items", "setting.highlight_items",
                        () -> SkyOPState.espItems, value -> SkyOPState.espItems = value));
                settings.add(number("espRange", "setting.esp_range", "setting.scan_radius",
                        () -> SkyOPState.espRange, value -> SkyOPState.espRange = value));
            }
            case ITEM_ESP -> {
                settings.add(bool("espItems", "setting.show_items", "setting.highlight_items",
                        () -> SkyOPState.espItems, value -> SkyOPState.espItems = value));
                settings.add(number("espRange", "setting.esp_range", "setting.scan_radius",
                        () -> SkyOPState.espRange, value -> SkyOPState.espRange = value));
            }
            case AUTO_EAT -> settings.add(number("autoEatThreshold", "setting.hunger_threshold", "setting.eat_below",
                    () -> (double) SkyOPState.autoEatThreshold, value -> SkyOPState.autoEatThreshold = value.intValue()));
            case AUTO_REFILL -> settings.add(number("autoRefillThreshold", "setting.refill_threshold", "setting.refill_low",
                    () -> (double) SkyOPState.autoRefillThreshold, value -> SkyOPState.autoRefillThreshold = value.intValue()));
            case HUD -> settings.add(new ColorSetting("hudAccentColor", "lang:setting.hud_accent", "lang:setting.hud_accent_desc",
                    0xFF61C7F2, SkyOPCore::hudAccentColor, SkyOPCore::setHudAccentColor));
            case TEXT_GUI -> settings.add(new StringSetting(
                    "customHudText", "lang:gui.custom_hud_text", "lang:gui.edit_text_help",
                    80, () -> SkyOPState.customHudText, SkyOPCore::setCustomHudText
            ));
            case PROFILE_MANAGER -> {
                for (int slot = 1; slot <= 3; slot++) {
                    int profileSlot = slot;
                    settings.add(action("saveProfile" + slot, "gui.save", "gui.configuration_profiles",
                            Integer.toString(slot), () -> SkyOPProfileManager.saveSlot(profileSlot)));
                    settings.add(action("loadProfile" + slot, "gui.load", "gui.configuration_profiles",
                            Integer.toString(slot), () -> SkyOPProfileManager.loadSlot(profileSlot)));
                }
                settings.add(action("exportProfile", "gui.export_json", "gui.configuration_profiles",
                        "", SkyOPProfileManager::exportCurrent));
                settings.add(action("importProfile", "gui.import_json", "gui.configuration_profiles",
                        "", SkyOPProfileManager::importProfile));
                settings.add(action("backupProfile", "gui.backup_now", "gui.configuration_profiles",
                        "", SkyOPProfileManager::backupCurrent));
            }
            case HOTBAR_PRESETS -> {
                for (int slot = 1; slot <= 3; slot++) {
                    int hotbarSlot = slot;
                    settings.add(action("saveHotbar" + slot, "gui.save_hotbar", "gui.hotbar_layout_slots",
                            Integer.toString(slot), () -> SkyOPProfileManager.saveHotbarPreset(hotbarSlot)));
                    settings.add(action("loadHotbar" + slot, "gui.load_hotbar", "gui.hotbar_layout_slots",
                            Integer.toString(slot), () -> SkyOPProfileManager.loadHotbarPreset(hotbarSlot)));
                }
            }
            default -> { }
        }
        return List.copyOf(settings);
    }

    private static ActionSetting action(String id, String titleKey, String descriptionKey,
                                        String suffix, ActionSetting.Operation operation) {
        return new ActionSetting(id, "lang:" + titleKey, "lang:" + descriptionKey, suffix, operation);
    }

    private static BooleanSetting bool(String id, String titleKey, String descriptionKey,
                                       Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new BooleanSetting(id, "lang:" + titleKey, "lang:" + descriptionKey,
                getter.get(), getter, setter);
    }

    private static NumberSetting number(String id, String titleKey, String descriptionKey,
                                        Supplier<Double> getter, Consumer<Double> setter) {
        SkyOPSettingsSchema.NumberRule rule = SkyOPSettingsSchema.rule(id);
        if (rule == null) throw new IllegalStateException("Missing numeric schema rule: " + id);
        return new NumberSetting(id, "lang:" + titleKey, "lang:" + descriptionKey,
                rule, getter, setter);
    }
}
