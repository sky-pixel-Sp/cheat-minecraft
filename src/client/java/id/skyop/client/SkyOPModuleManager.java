package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Registry and lifecycle dispatcher for all SkyOP modules.
 * Existing gameplay implementations are retained and invoked through module runtimes.
 */
public final class SkyOPModuleManager {
    private static final Map<SkyOPClient.Module, SkyOPModule> MODULES =
            new EnumMap<>(SkyOPClient.Module.class);
    private static boolean initialized;

    private SkyOPModuleManager() { }

    public static void initialize() {
        MODULES.clear();
        for (SkyOPClient.Module module : SkyOPClient.Module.values()) {
            MODULES.put(module, new RuntimeModule(module, settingsFor(module)));
        }
        initialized = true;
    }

    public static List<SkyOPModule> all() {
        return List.copyOf(MODULES.values());
    }

    public static SkyOPModule get(SkyOPClient.Module module) {
        return MODULES.get(module);
    }

    public static List<SkyOPSetting<?>> settings(SkyOPClient.Module module) {
        SkyOPModule runtime = get(module);
        return runtime == null ? List.of() : runtime.settings();
    }

    public static void syncAllStates(Minecraft client) {
        if (!initialized) return;
        for (SkyOPModule module : MODULES.values()) {
            if (module instanceof RuntimeModule runtime) runtime.syncState(client);
        }
    }

    public static void saveSettings(Properties properties) {
        if (!initialized) return;
        for (SkyOPModule module : MODULES.values()) {
            for (SkyOPSetting<?> setting : module.settings()) {
                if (setting instanceof KeybindSetting) continue; // Stored in the legacy-compatible keybind.<MODULE> property.
                properties.setProperty(settingKey(module.type(), setting), setting.serialize());
            }
        }
    }

    public static void loadSettings(Properties properties) {
        if (!initialized) return;
        for (SkyOPModule module : MODULES.values()) {
            for (SkyOPSetting<?> setting : module.settings()) {
                if (setting instanceof KeybindSetting) continue;
                String value = properties.getProperty(settingKey(module.type(), setting));
                if (value != null) setting.deserialize(value);
            }
        }
    }

    private static String settingKey(SkyOPClient.Module module, SkyOPSetting<?> setting) {
        return "setting." + module.name() + "." + setting.id();
    }

    public static void tick(Minecraft client) {
        if (!initialized || client == null || client.player == null || client.level == null) return;
        syncAllStates(client);
        SkyOPClient.trackDisabledJumpInputs(client);

        // Preserve the original phase order: movement, NoFall, visuals, combat, inventory.
        tickModules(client, List.of(
                SkyOPClient.Module.AUTO_SPRINT, SkyOPClient.Module.SPEED, SkyOPClient.Module.FLIGHT,
                SkyOPClient.Module.AUTO_JUMP, SkyOPClient.Module.STEP_ASSIST, SkyOPClient.Module.SPIDER,
                SkyOPClient.Module.AUTO_BRIDGE, SkyOPClient.Module.JUMP_BOOST, SkyOPClient.Module.INFINITE_JUMP
        ));
        tickModules(client, List.of(SkyOPClient.Module.NO_FALL));

        // ESP and gamma are shared by paired modules and are evaluated once per tick.
        if (SkyOPClient.isEnabled(SkyOPClient.Module.ENTITY_ESP)
                || SkyOPClient.isEnabled(SkyOPClient.Module.ITEM_ESP)) {
            SkyOPClient.tickEsp(client);
        } else {
            SkyOPClient.restoreGlow();
        }

        if (SkyOPClient.isEnabled(SkyOPClient.Module.GAMMA_BOOST)
                || SkyOPClient.isEnabled(SkyOPClient.Module.NIGHT_VISION)) {
            SkyOPClient.tickGamma(client);
        } else {
            SkyOPClient.restoreGamma(client);
        }

        tickModules(client, List.of(
                SkyOPClient.Module.KILL_AURA, SkyOPClient.Module.AUTO_HUNT,
                SkyOPClient.Module.TRIGGER_BOT, SkyOPClient.Module.AUTO_CLICKER
        ));
        SkyOPInventoryManager.tick(client);
    }

    private static void tickModules(Minecraft client, List<SkyOPClient.Module> order) {
        for (SkyOPClient.Module type : order) {
            SkyOPModule module = MODULES.get(type);
            if (module != null && SkyOPClient.isEnabled(type) && module.isReady(client)) {
                module.onTick(client);
            }
        }
    }

    public static void render2D(Minecraft client, GuiGraphicsExtractor graphics) {
        if (!initialized) return;
        // The legacy HUD renderer is still the rendering backend in Phase 1.
        SkyOPClient.renderHud(client, graphics);
        for (SkyOPModule module : MODULES.values()) {
            module.onRender2D(client, graphics);
        }
    }

    public static void render3D(Minecraft client, Object renderContext) {
        if (!initialized) return;
        for (SkyOPModule module : MODULES.values()) {
            if (SkyOPClient.isEnabled(module.type())) module.onRender3D(client, renderContext);
        }
    }

    public static void onWorldChange(Minecraft client) {
        if (!initialized) return;
        for (SkyOPModule module : MODULES.values()) module.onWorldChange(client);
        SkyOPClient.cleanupWorldState(client);
        syncAllStates(client);
    }

    private static List<SkyOPSetting<?>> settingsFor(SkyOPClient.Module module) {
        List<SkyOPSetting<?>> settings = new ArrayList<>();
        settings.add(new KeybindSetting(
                "keybind." + module.name(), "Keybind", "Keyboard shortcut for this module.",
                SkyOPClient.moduleKeyCode(module),
                () -> SkyOPClient.moduleKeyCode(module),
                value -> SkyOPClient.setModuleKeybind(module, value)
        ));

        switch (module) {
            case KILL_AURA -> {
                settings.add(bool("targetPlayers", "Target players", "Include other players.", () -> SkyOPClient.targetPlayers, value -> SkyOPClient.targetPlayers = value));
                settings.add(bool("targetMobs", "Target hostile mobs", "Include hostile mobs.", () -> SkyOPClient.targetMobs, value -> SkyOPClient.targetMobs = value));
                settings.add(bool("auraRotate", "Rotate to target", "Enable camera rotation toward the target.", () -> SkyOPClient.auraRotate, value -> SkyOPClient.auraRotate = value));
                settings.add(bool("auraRequireLineOfSight", "Require line of sight", "Only attack targets visible to the player.", () -> SkyOPClient.auraRequireLineOfSight, value -> SkyOPClient.auraRequireLineOfSight = value));
                settings.add(number("auraRange", "Attack range", "Maximum target distance in blocks.", () -> SkyOPClient.auraRange, value -> SkyOPClient.auraRange = value));
                settings.add(bool("auraTargetLowestHealth", "Prioritize lowest health", "Prefer the weakest valid target.", () -> SkyOPClient.auraTargetLowestHealth, value -> SkyOPClient.auraTargetLowestHealth = value));
                settings.add(number("auraFov", "Aim field of view", "Maximum targeting cone in degrees.", () -> SkyOPClient.auraFov, value -> SkyOPClient.auraFov = value));
                settings.add(number("auraRotationSpeed", "Rotation speed", "Interpolation speed for target rotation.", () -> SkyOPClient.auraRotationSpeed, value -> SkyOPClient.auraRotationSpeed = value));
                settings.add(number("auraDelayTicks", "Attack delay", "Ticks between attacks.", () -> (double) SkyOPClient.auraDelayTicks, value -> SkyOPClient.auraDelayTicks = value.intValue()));
            }
            case TRIGGER_BOT -> {
                settings.add(bool("targetPlayers", "Target players", "Include other players.", () -> SkyOPClient.targetPlayers, value -> SkyOPClient.targetPlayers = value));
                settings.add(bool("targetMobs", "Target hostile mobs", "Include hostile mobs.", () -> SkyOPClient.targetMobs, value -> SkyOPClient.targetMobs = value));
                settings.add(number("triggerDelayTicks", "Trigger delay", "Ticks between trigger attacks.", () -> (double) SkyOPClient.triggerDelayTicks, value -> SkyOPClient.triggerDelayTicks = value.intValue()));
            }
            case AUTO_CLICKER -> settings.add(number("autoClickerCps", "Clicks per second", "Attack rate while the attack key is held.", () -> (double) SkyOPClient.autoClickerCps, value -> SkyOPClient.autoClickerCps = value.intValue()));
            case AUTO_HUNT -> settings.add(number("autoHuntRange", "Hunt range", "Maximum hostile-mob distance in blocks.", () -> SkyOPClient.autoHuntRange, value -> SkyOPClient.autoHuntRange = value));
            case SPEED -> settings.add(number("speedMultiplier", "Movement multiplier", "Horizontal movement multiplier.", () -> SkyOPClient.speedMultiplier, value -> SkyOPClient.speedMultiplier = value));
            case FLIGHT -> settings.add(number("flightSpeed", "Flight speed", "Vertical flight speed.", () -> SkyOPClient.flightSpeed, value -> SkyOPClient.flightSpeed = value));
            case AUTO_JUMP -> settings.add(number("autoJumpDelayTicks", "Jump interval", "Ticks between automatic jumps.", () -> (double) SkyOPClient.autoJumpDelayTicks, value -> SkyOPClient.autoJumpDelayTicks = value.intValue()));
            case SPIDER -> settings.add(number("spiderClimbSpeed", "Climb speed", "Local vertical speed while climbing.", () -> SkyOPClient.spiderClimbSpeed, value -> SkyOPClient.spiderClimbSpeed = value));
            case AUTO_BRIDGE -> settings.add(number("bridgeDelayTicks", "Placement delay", "Ticks between block placements.", () -> (double) SkyOPClient.bridgeDelayTicks, value -> SkyOPClient.bridgeDelayTicks = value.intValue()));
            case JUMP_BOOST -> settings.add(number("jumpBoostMultiplier", "Jump multiplier", "Local vertical jump impulse multiplier.", () -> SkyOPClient.jumpBoostMultiplier, value -> SkyOPClient.jumpBoostMultiplier = value));
            case ENTITY_ESP -> {
                settings.add(bool("espPlayers", "Show players", "Highlight loaded player entities.", () -> SkyOPClient.espPlayers, value -> SkyOPClient.espPlayers = value));
                settings.add(bool("espMobs", "Show hostile mobs", "Highlight loaded hostile mobs.", () -> SkyOPClient.espMobs, value -> SkyOPClient.espMobs = value));
                settings.add(bool("espItems", "Show dropped items", "Highlight item entities already loaded by the client.", () -> SkyOPClient.espItems, value -> SkyOPClient.espItems = value));
                settings.add(number("espRange", "ESP range", "Entity highlight range in blocks.", () -> SkyOPClient.espRange, value -> SkyOPClient.espRange = value));
            }
            case ITEM_ESP -> {
                settings.add(bool("espItems", "Show dropped items", "Highlight item entities already loaded by the client.", () -> SkyOPClient.espItems, value -> SkyOPClient.espItems = value));
                settings.add(number("espRange", "ESP range", "Item highlight range in blocks.", () -> SkyOPClient.espRange, value -> SkyOPClient.espRange = value));
            }
            case AUTO_EAT -> settings.add(number("autoEatThreshold", "Hunger threshold", "Start eating below this hunger level.", () -> (double) SkyOPClient.autoEatThreshold, value -> SkyOPClient.autoEatThreshold = value.intValue()));
            case AUTO_REFILL -> settings.add(number("autoRefillThreshold", "Refill threshold", "Refill a selected stack at or below this count.", () -> (double) SkyOPClient.autoRefillThreshold, value -> SkyOPClient.autoRefillThreshold = value.intValue()));
            case HUD -> settings.add(new ColorSetting("hudAccentColor", "HUD accent", "Accent color used by the HUD.", 0xFF61C7F2,
                    SkyOPClient::hudAccentColor, SkyOPClient::setHudAccentColor));
            default -> { }
        }
        return List.copyOf(settings);
    }

    private static BooleanSetting bool(String id, String title, String description,
                                       Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new BooleanSetting(id, title, description, false, getter, setter);
    }

    private static NumberSetting number(String id, String title, String description,
                                        Supplier<Double> getter, Consumer<Double> setter) {
        SkyOPSettingsSchema.NumberRule rule = SkyOPSettingsSchema.rule(id);
        if (rule == null) throw new IllegalStateException("Missing numeric schema rule: " + id);
        return new NumberSetting(id, title, description, rule, getter, setter);
    }

    private static final class RuntimeModule implements SkyOPModule {
        private final SkyOPClient.Module type;
        private final List<SkyOPSetting<?>> settings;
        private boolean active;

        private RuntimeModule(SkyOPClient.Module type, List<SkyOPSetting<?>> settings) {
            this.type = type;
            this.settings = settings;
        }

        @Override public SkyOPClient.Module type() { return type; }
        @Override public List<SkyOPSetting<?>> settings() { return settings; }

        @Override
        public void onEnable(Minecraft client) {
            // Existing modules acquire no external resource merely by being enabled.
        }

        @Override
        public void onDisable(Minecraft client) {
            SkyOPClient.onModuleDisabled(client, type);
        }

        @Override
        public void onTick(Minecraft client) {
            switch (type) {
                case AUTO_SPRINT -> SkyOPClient.tickAutoSprint(client);
                case SPEED -> SkyOPClient.tickSpeed(client);
                case FLIGHT -> SkyOPClient.tickFlight(client);
                case AUTO_JUMP -> SkyOPClient.tickAutoJump(client);
                case STEP_ASSIST -> SkyOPClient.tickStepAssist(client);
                case SPIDER -> SkyOPClient.tickSpider(client);
                case AUTO_BRIDGE -> SkyOPClient.tickAutoBridge(client);
                case JUMP_BOOST -> SkyOPClient.tickJumpBoost(client);
                case INFINITE_JUMP -> SkyOPClient.tickInfiniteJump(client);
                case NO_FALL -> SkyOPClient.tickNoFall(client);
                case KILL_AURA -> SkyOPClient.tickKillAura(client);
                case AUTO_HUNT -> SkyOPClient.tickAutoHunt(client);
                case TRIGGER_BOT -> SkyOPClient.tickTriggerBot(client);
                case AUTO_CLICKER -> SkyOPClient.tickAutoClicker(client);
                default -> { }
            }
        }

        private void syncState(Minecraft client) {
            boolean enabled = SkyOPClient.isEnabled(type);
            if (enabled == active) return;
            active = enabled;
            if (active) onEnable(client); else onDisable(client);
        }
    }
}
