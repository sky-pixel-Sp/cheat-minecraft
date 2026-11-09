package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;

/**
 * Non-gameplay services: keybind registration, module toggles/presets, config IO,
 * HUD coordinates and the central tick/event bridge.
 */
public final class SkyOPCore {
    private SkyOPCore() { }

    private static final Identifier KEY_CATEGORY_ID = Identifier.fromNamespaceAndPath("skyop", "client");
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(KEY_CATEGORY_ID);
    private static final KeyMapping KEY_GUI = key("key.skyop.open_gui", InputConstants.KEY_RSHIFT);
    private static final KeyMapping KEY_HUD = key("key.skyop.toggle_hud", InputConstants.KEY_F4);
    private static final KeyMapping KEY_AUTO_CLICKER = key("key.skyop.toggle_autoclicker", InputConstants.KEY_F5);
    private static final KeyMapping KEY_SPRINT = key("key.skyop.toggle_sprint", InputConstants.KEY_F6);
    private static final KeyMapping KEY_SPEED = key("key.skyop.toggle_speed", InputConstants.KEY_F7);
    private static final KeyMapping KEY_AURA = key("key.skyop.toggle_aura", InputConstants.KEY_F8);
    private static final KeyMapping KEY_ESP = key("key.skyop.toggle_esp", InputConstants.KEY_F9);
    private static final KeyMapping KEY_NO_FALL = key("key.skyop.toggle_nofall", InputConstants.KEY_F10);
    private static final KeyMapping KEY_FLIGHT = key("key.skyop.toggle_flight", InputConstants.KEY_F11);
    private static final KeyMapping KEY_DISABLE_ALL = key("key.skyop.disable_all", InputConstants.KEY_F12);
    private static final KeyMapping KEY_ADD_WAYPOINT = key("key.skyop.add_waypoint", InputConstants.KEY_V);

    private static Object trackedLevelContext;
    private static Player trackedPlayerContext;
    private static boolean contextInitialized;

    public static void initialize() {
        registerModuleKeybinds();
        SkyOPModuleManager.initialize();
        loadConfig();
        SkyOPModuleManager.syncAllStates(Minecraft.getInstance());
        SkyOPWaypointManager.load();
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("skyop", "overlay"),
                (graphics, deltaTracker) -> SkyOPModuleManager.render2D(Minecraft.getInstance(), graphics)
        );
        ClientTickEvents.END_CLIENT_TICK.register(SkyOPCore::onClientTick);
    }

private static KeyMapping key(String translationKey, int keyCode) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translationKey, InputConstants.Type.KEYBOARD, keyCode, KEY_CATEGORY
        ));
    }

public static boolean isEnabled(SkyOPModuleId module) { return SkyOPState.ENABLED.contains(module); }

public static int enabledModuleCount() { return SkyOPState.ENABLED.size(); }

public static boolean isFavorite(SkyOPModuleId module) { return SkyOPState.FAVORITES.contains(module); }

public static boolean isModuleKeybindUnbound(SkyOPModuleId module) {
        return module == null || SkyOPState.MODULE_KEY_CODES.getOrDefault(module, -1) < 0;
    }

public static int favoriteCount() { return SkyOPState.FAVORITES.size(); }

public static int SkyOPState.hudAccentColor() { return SkyOPState.hudAccentColor; }

public static KeyMapping keybindFor(SkyOPModuleId module) { return SkyOPState.MODULE_KEYBINDS.get(module); }

public static int moduleKeyCode(SkyOPModuleId module) { return SkyOPState.MODULE_KEY_CODES.getOrDefault(module, -1); }

public static int hudXFor(SkyOPModuleId module, int defaultX, int screenWidth, int elementWidth) {
        SkyOPState.HudPosition saved = SkyOPState.HUD_POSITIONS.get(module);
        int preferred = saved != null ? saved.x() : (module == SkyOPModuleId.HUD ? SkyOPState.hudX : defaultX);
        return SkyOPMath.clamp(preferred, 0, Math.max(0, screenWidth - elementWidth));
    }

public static int hudYFor(SkyOPModuleId module, int defaultY, int screenHeight, int elementHeight) {
        SkyOPState.HudPosition saved = SkyOPState.HUD_POSITIONS.get(module);
        int preferred = saved != null ? saved.y() : (module == SkyOPModuleId.HUD ? SkyOPState.hudY : defaultY);
        return SkyOPMath.clamp(preferred, 0, Math.max(0, screenHeight - elementHeight));
    }

public static void setHudPosition(SkyOPModuleId module, int x, int y,
                                      int screenWidth, int screenHeight,
                                      int elementWidth, int elementHeight) {
        if (module == null) return;
        int safeX = SkyOPMath.clamp(x, 0, Math.max(0, screenWidth - elementWidth));
        int safeY = SkyOPMath.clamp(y, 0, Math.max(0, screenHeight - elementHeight));
        SkyOPState.HUD_POSITIONS.put(module, new SkyOPState.HudPosition(safeX, safeY));
        if (module == SkyOPModuleId.HUD) {
            SkyOPState.hudX = safeX;
            SkyOPState.hudY = safeY;
        }
    }

public static void resetHudPositions() {
        SkyOPState.HUD_POSITIONS.clear();
        SkyOPState.hudX = 8;
        SkyOPState.hudY = 8;
        saveConfig();
    }

public static void toggleFavorite(SkyOPModuleId module) {
        if (!SkyOPState.FAVORITES.add(module)) SkyOPState.FAVORITES.remove(module);
        saveConfig();
    }

public static void setModuleKeybind(SkyOPModuleId module, int scancode) {
        KeyMapping mapping = SkyOPState.MODULE_KEYBINDS.get(module);
        if (mapping == null) return;
        int safeCode = scancode > 0 && SkyOPSdlKeyboardIndex.isValidStateIndex(scancode) ? scancode : -1;
        SkyOPState.MODULE_KEY_CODES.put(module, safeCode);
        mapping.setKey(safeCode < 0
                ? InputConstants.UNKNOWN
                : InputConstants.Type.KEYBOARD.getOrCreate(safeCode));
        KeyMapping.resetMapping();
        saveConfig();
    }

public static void cycleHudColor() {
        int index = 0;
        for (int i = 0; i < HUD_PALETTE.length; i++) {
            if (HUD_PALETTE[i] == SkyOPState.hudAccentColor) index = i;
        }
        SkyOPState.hudAccentColor = HUD_PALETTE[(index + 1) % HUD_PALETTE.length];
        saveConfig();
    }

public static void setHudAccentColor(int color) {
        SkyOPState.hudAccentColor = 0xFF000000 | (color & 0x00FFFFFF);
        saveConfig();
    }

public static void setCustomHudText(String text) {
        if (text == null) return;
        String safe = text.strip();
        if (safe.length() > 80) safe = safe.substring(0, 80);
        SkyOPState.customHudText = safe;
        saveConfig();
    }

public static Path configPath() { return SkyOPState.CONFIG_FILE; }

public static void reloadConfigFromDisk() {
        SkyOPState.ENABLED.clear();
        SkyOPState.ENABLED.add(SkyOPModuleId.HUD);
        SkyOPState.FAVORITES.clear();
        loadConfig();
        SkyOPModuleManager.syncAllStates(Minecraft.getInstance());
        if (!SkyOPState.ENABLED.contains(SkyOPModuleId.ENTITY_ESP) && !SkyOPState.ENABLED.contains(SkyOPModuleId.ITEM_ESP)) SkyOPVisualModules.restoreGlow();
        if (!SkyOPState.ENABLED.contains(SkyOPModuleId.GAMMA_BOOST) && !SkyOPState.ENABLED.contains(SkyOPModuleId.NIGHT_VISION)) SkyOPVisualModules.restoreGamma(Minecraft.getInstance());
        if (!SkyOPState.ENABLED.contains(SkyOPModuleId.FLIGHT)) SkyOPMovementModules.restoreFlight(Minecraft.getInstance());
    }

private static void registerModuleKeybinds() {
        registerMapping(SkyOPModuleId.HUD, KEY_HUD, InputConstants.KEY_F4);
        registerMapping(SkyOPModuleId.AUTO_CLICKER, KEY_AUTO_CLICKER, InputConstants.KEY_F5);
        registerMapping(SkyOPModuleId.AUTO_SPRINT, KEY_SPRINT, InputConstants.KEY_F6);
        registerMapping(SkyOPModuleId.SPEED, KEY_SPEED, InputConstants.KEY_F7);
        registerMapping(SkyOPModuleId.KILL_AURA, KEY_AURA, InputConstants.KEY_F8);
        registerMapping(SkyOPModuleId.ENTITY_ESP, KEY_ESP, InputConstants.KEY_F9);
        registerMapping(SkyOPModuleId.NO_FALL, KEY_NO_FALL, InputConstants.KEY_F10);
        registerMapping(SkyOPModuleId.FLIGHT, KEY_FLIGHT, InputConstants.KEY_F11);

        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            if (SkyOPState.MODULE_KEYBINDS.containsKey(module)) continue;
            String keyName = "key.skyop.module." + module.name().toLowerCase();
            // Minecraft 26.3 uses SDL scancodes. Never pass raw -1 to the
            // native keyboard-state buffer for an unbound key mapping.
            KeyMapping mapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    keyName, InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY
            ));
            SkyOPState.MODULE_KEYBINDS.put(module, mapping);
            SkyOPState.MODULE_KEY_CODES.put(module, -1);
        }
    }

private static void registerMapping(SkyOPModuleId module, KeyMapping mapping, int keyCode) {
        SkyOPState.MODULE_KEYBINDS.put(module, mapping);
        SkyOPState.MODULE_KEY_CODES.put(module, keyCode);
    }

public static void setEnabled(Minecraft client, SkyOPModuleId module, boolean enabled) {
        if (enabled && !SkyOPModuleDependencies.isAvailable(client, module)) {
            String missing = SkyOPModuleDependencies.missingRequirement(client, module);
            if (client != null && client.player != null) {
                client.player.sendSystemMessage(Component.literal("[SkyOP] " + missing));
            }
            return;
        }
        if (enabled) SkyOPState.ENABLED.add(module); else SkyOPState.ENABLED.remove(module);
        SkyOPModuleManager.syncAllStates(client);

        if ((module == SkyOPModuleId.ENTITY_ESP || module == SkyOPModuleId.ITEM_ESP) && !enabled
                && !isEnabled(SkyOPModuleId.ENTITY_ESP) && !isEnabled(SkyOPModuleId.ITEM_ESP)) SkyOPVisualModules.restoreGlow();
        if (module == SkyOPModuleId.FLIGHT && !enabled) SkyOPMovementModules.restoreFlight(client);
        if ((module == SkyOPModuleId.GAMMA_BOOST || module == SkyOPModuleId.NIGHT_VISION) && !enabled
                && !isEnabled(SkyOPModuleId.GAMMA_BOOST) && !isEnabled(SkyOPModuleId.NIGHT_VISION)) SkyOPVisualModules.restoreGamma(client);

        saveConfig();
        notify(client, module.title(), enabled);
    }

public static void toggle(Minecraft client, SkyOPModuleId module) {
        setEnabled(client, module, !isEnabled(module));
    }

public static void cycleLanguage() {
        SkyOPLanguage.next();
        saveConfig();
    }

public static void applyPreset(Minecraft client, SkyOPPreset preset) {
        SkyOPState.ENABLED.clear();
        SkyOPState.targetPlayers = false;
        SkyOPState.targetMobs = true;
        SkyOPState.auraTargetLowestHealth = false;

        switch (preset) {
            case PVE -> {
                SkyOPState.ENABLED.addAll(EnumSet.of(SkyOPModuleId.HUD, SkyOPModuleId.KILL_AURA, SkyOPModuleId.AUTO_SPRINT, SkyOPModuleId.STEP_ASSIST));
                SkyOPState.auraRange = 5.0;
                SkyOPState.auraDelayTicks = 4;
                SkyOPState.speedMultiplier = 1.4;
            }
            case OP -> {
                SkyOPState.ENABLED.addAll(EnumSet.of(
                        SkyOPModuleId.HUD, SkyOPModuleId.KILL_AURA, SkyOPModuleId.AUTO_CLICKER,
                        SkyOPModuleId.AUTO_SPRINT, SkyOPModuleId.SPEED, SkyOPModuleId.FLIGHT,
                        SkyOPModuleId.AUTO_JUMP, SkyOPModuleId.STEP_ASSIST, SkyOPModuleId.SPIDER,
                        SkyOPModuleId.ENTITY_ESP, SkyOPModuleId.TARGET_HUD, SkyOPModuleId.ARMOR_HUD, SkyOPModuleId.NO_FALL
                ));
                SkyOPState.auraRange = 6.0;
                SkyOPState.auraDelayTicks = 1;
                SkyOPState.auraTargetLowestHealth = true;
                SkyOPState.speedMultiplier = 2.8;
                SkyOPState.flightSpeed = 0.72;
                SkyOPState.spiderClimbSpeed = 0.32;
                SkyOPState.espRange = 96.0;
            }
            case VISUAL -> {
                SkyOPState.ENABLED.addAll(EnumSet.of(
                        SkyOPModuleId.HUD, SkyOPModuleId.ENTITY_ESP, SkyOPModuleId.GAMMA_BOOST,
                        SkyOPModuleId.TARGET_HUD, SkyOPModuleId.KEYSTROKES_HUD, SkyOPModuleId.ARMOR_HUD
                ));
            }
        }
        SkyOPModuleManager.syncAllStates(client);

        if (!isEnabled(SkyOPModuleId.ENTITY_ESP) && !isEnabled(SkyOPModuleId.ITEM_ESP)) SkyOPVisualModules.restoreGlow();
        if (!isEnabled(SkyOPModuleId.FLIGHT)) SkyOPMovementModules.restoreFlight(client);
        if (!isEnabled(SkyOPModuleId.GAMMA_BOOST) && !isEnabled(SkyOPModuleId.NIGHT_VISION)) SkyOPVisualModules.restoreGamma(client);
        if (!isEnabled(SkyOPModuleId.AUTO_FISH)) SkyOPInventoryManager.stop(client);
        saveConfig();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("[SkyOP] " + preset.name() + " profile loaded."));
        }
    }

public static void disableAll(Minecraft client) {
        SkyOPState.ENABLED.clear();
        SkyOPModuleManager.syncAllStates(client);
        auraCooldown = 0;
        triggerCooldown = 0;
        clickerCooldown = 0;
        autoJumpCooldown = 0;
        stepAssistCooldown = 0;
        autoHuntCooldown = 0;
        bridgeCooldown = 0;
        jumpKeyWasDown = false;
        jumpBoostKeyWasDown = false;
        SkyOPVisualModules.restoreGlow();
        SkyOPVisualModules.restoreGamma(client);
        SkyOPMovementModules.restoreFlight(client);
        SkyOPInventoryManager.stop(client);
        saveConfig();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("[SkyOP] All modules disabled."));
        }
    }

private static void onClientTick(Minecraft client) {
        handleKeybinds(client);
        ensureMouseCaptured(client);

        if (!contextInitialized) {
            trackedLevelContext = client.level;
            trackedPlayerContext = client.player;
            contextInitialized = true;
        } else if (trackedLevelContext != client.level || trackedPlayerContext != client.player) {
            SkyOPModuleManager.onWorldChange(client);
            trackedLevelContext = client.level;
            trackedPlayerContext = client.player;
        }

        if (client.player == null || client.level == null) {
            SkyOPVisualModules.restoreGlow();
            SkyOPVisualModules.restoreGamma(client);
            SkyOPMovementModules.restoreFlight(client);
            return;
        }

        updateTelemetry(client);
        if (client.gui.screen() != null) {
            SkyOPInventoryManager.stop(client);
            return;
        }

        SkyOPModuleManager.tick(client);
    }

private static void ensureMouseCaptured(Minecraft client) {
        if (client.player == null || client.level == null || !client.isWindowActive()) return;
        if (client.gui.screen() != null) return;
        if (!client.mouseHandler.isMouseGrabbed()) {
            client.mouseHandler.grabMouse();
        }
    }

private static void handleKeybinds(Minecraft client) {
        if (KEY_GUI.consumeClick()) client.gui.setScreen(new SkyOPClickGui());
        if (KEY_ADD_WAYPOINT.consumeClick()) {
            boolean added = SkyOPWaypointManager.addCurrent(client);
            if (client.player != null) client.player.sendSystemMessage(Component.literal(
                    added ? "[SkyOP] Waypoint saved." : "[SkyOP] Waypoint limit reached or no player."));
        }
        if (KEY_DISABLE_ALL.consumeClick()) {
            disableAll(client);
            return;
        }
        for (Map.Entry<SkyOPModuleId, KeyMapping> entry : SkyOPState.MODULE_KEYBINDS.entrySet()) {
            while (entry.getValue().consumeClick()) toggle(client, entry.getKey());
        }
    }

public static void saveConfig() {
        Properties p = new Properties();
        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            p.setProperty("module." + module.name(), Boolean.toString(isEnabled(module)));
        }
        p.setProperty("language", SkyOPLanguage.code());
        p.setProperty("SkyOPState.targetPlayers", Boolean.toString(SkyOPState.targetPlayers));
        p.setProperty("SkyOPState.targetMobs", Boolean.toString(SkyOPState.targetMobs));
        p.setProperty("SkyOPState.auraRotate", Boolean.toString(SkyOPState.auraRotate));
        p.setProperty("SkyOPState.auraRequireLineOfSight", Boolean.toString(SkyOPState.auraRequireLineOfSight));
        p.setProperty("SkyOPState.auraTargetLowestHealth", Boolean.toString(SkyOPState.auraTargetLowestHealth));
        p.setProperty("SkyOPState.auraFov", Double.toString(SkyOPState.auraFov));
        p.setProperty("SkyOPState.auraRotationSpeed", Double.toString(SkyOPState.auraRotationSpeed));
        p.setProperty("SkyOPState.auraRange", Double.toString(SkyOPState.auraRange));
        p.setProperty("SkyOPState.auraDelayTicks", Integer.toString(SkyOPState.auraDelayTicks));
        p.setProperty("SkyOPState.triggerDelayTicks", Integer.toString(SkyOPState.triggerDelayTicks));
        p.setProperty("SkyOPState.autoClickerCps", Integer.toString(SkyOPState.autoClickerCps));
        p.setProperty("SkyOPState.speedMultiplier", Double.toString(SkyOPState.speedMultiplier));
        p.setProperty("SkyOPState.flightSpeed", Double.toString(SkyOPState.flightSpeed));
        p.setProperty("SkyOPState.autoJumpDelayTicks", Integer.toString(SkyOPState.autoJumpDelayTicks));
        p.setProperty("SkyOPState.spiderClimbSpeed", Double.toString(SkyOPState.spiderClimbSpeed));
        p.setProperty("SkyOPState.espRange", Double.toString(SkyOPState.espRange));
        p.setProperty("SkyOPState.espPlayers", Boolean.toString(SkyOPState.espPlayers));
        p.setProperty("SkyOPState.espMobs", Boolean.toString(SkyOPState.espMobs));
        p.setProperty("SkyOPState.espItems", Boolean.toString(SkyOPState.espItems));
        p.setProperty("SkyOPState.autoHuntRange", Double.toString(SkyOPState.autoHuntRange));
        p.setProperty("SkyOPState.jumpBoostMultiplier", Double.toString(SkyOPState.jumpBoostMultiplier));
        p.setProperty("SkyOPState.bridgeDelayTicks", Integer.toString(SkyOPState.bridgeDelayTicks));
        p.setProperty("SkyOPState.autoEatThreshold", Integer.toString(SkyOPState.autoEatThreshold));
        p.setProperty("SkyOPState.autoRefillThreshold", Integer.toString(SkyOPState.autoRefillThreshold));
        p.setProperty("SkyOPState.hudX", Integer.toString(SkyOPState.hudX));
        p.setProperty("SkyOPState.hudY", Integer.toString(SkyOPState.hudY));
        for (Map.Entry<SkyOPModuleId, SkyOPState.HudPosition> entry : SkyOPState.HUD_POSITIONS.entrySet()) {
            String prefix = "hud." + entry.getKey().name() + ".";
            p.setProperty(prefix + "x", Integer.toString(entry.getValue().x()));
            p.setProperty(prefix + "y", Integer.toString(entry.getValue().y()));
        }
        p.setProperty("SkyOPState.hudAccentColor", Integer.toString(SkyOPState.hudAccentColor));
        p.setProperty("SkyOPState.customHudText", SkyOPState.customHudText);
        for (SkyOPModuleId module : SkyOPState.FAVORITES) p.setProperty("favorite." + module.name(), "true");
        for (Map.Entry<SkyOPModuleId, KeyMapping> entry : SkyOPState.MODULE_KEYBINDS.entrySet()) {
            p.setProperty("keybind." + entry.getKey().name(),
                    Integer.toString(SkyOPState.MODULE_KEY_CODES.getOrDefault(entry.getKey(), -1)));
        }
        SkyOPModuleManager.saveSettings(p);

        try {
            SkyOPConfigManager.save(SkyOPState.CONFIG_FILE, p);
        } catch (IOException exception) {
            System.err.println("[SkyOP] Could not save config: " + exception.getMessage());
        }
    }

private static void loadConfig() {
        Properties p;
        try {
            Path backupDirectory = FabricLoader.getInstance().getConfigDir()
                    .resolve("skyop-client").resolve("backups");
            p = SkyOPConfigManager.load(SkyOPState.CONFIG_FILE, backupDirectory);
            SkyOPLanguage.set(p.getProperty("language", "en"));
            for (SkyOPModuleId module : SkyOPModuleId.values()) {
                String value = p.getProperty("module." + module.name());
                if (value != null) {
                    if (Boolean.parseBoolean(value)) SkyOPState.ENABLED.add(module);
                    else SkyOPState.ENABLED.remove(module);
                }
            }
            SkyOPState.targetPlayers = bool(p, "SkyOPState.targetPlayers", SkyOPState.targetPlayers);
            SkyOPState.targetMobs = bool(p, "SkyOPState.targetMobs", SkyOPState.targetMobs);
            SkyOPState.auraRotate = bool(p, "SkyOPState.auraRotate", SkyOPState.auraRotate);
            SkyOPState.auraRequireLineOfSight = bool(p, "SkyOPState.auraRequireLineOfSight", SkyOPState.auraRequireLineOfSight);
            SkyOPState.auraTargetLowestHealth = bool(p, "SkyOPState.auraTargetLowestHealth", SkyOPState.auraTargetLowestHealth);
            SkyOPState.auraFov = number(p, "SkyOPState.auraFov", SkyOPState.auraFov, 30.0, 180.0);
            SkyOPState.auraRotationSpeed = number(p, "SkyOPState.auraRotationSpeed", SkyOPState.auraRotationSpeed, 0.10, 1.0);
            SkyOPState.auraRange = number(p, "SkyOPState.auraRange", SkyOPState.auraRange, 2.5, 6.0);
            SkyOPState.auraDelayTicks = integer(p, "SkyOPState.auraDelayTicks", SkyOPState.auraDelayTicks, 1, 20);
            SkyOPState.triggerDelayTicks = integer(p, "SkyOPState.triggerDelayTicks", SkyOPState.triggerDelayTicks, 1, 12);
            SkyOPState.autoClickerCps = integer(p, "SkyOPState.autoClickerCps", SkyOPState.autoClickerCps, 4, 20);
            SkyOPState.speedMultiplier = number(p, "SkyOPState.speedMultiplier", SkyOPState.speedMultiplier, 1.0, 2.8);
            SkyOPState.flightSpeed = number(p, "SkyOPState.flightSpeed", SkyOPState.flightSpeed, 0.12, 0.9);
            SkyOPState.autoJumpDelayTicks = integer(p, "SkyOPState.autoJumpDelayTicks", SkyOPState.autoJumpDelayTicks, 2, 12);
            SkyOPState.spiderClimbSpeed = number(p, "SkyOPState.spiderClimbSpeed", SkyOPState.spiderClimbSpeed, 0.08, 0.38);
            SkyOPState.espRange = number(p, "SkyOPState.espRange", SkyOPState.espRange, 8.0, 96.0);
            SkyOPState.espPlayers = bool(p, "SkyOPState.espPlayers", SkyOPState.espPlayers);
            SkyOPState.espMobs = bool(p, "SkyOPState.espMobs", SkyOPState.espMobs);
            SkyOPState.espItems = bool(p, "SkyOPState.espItems", SkyOPState.espItems);
            SkyOPState.autoHuntRange = number(p, "SkyOPState.autoHuntRange", SkyOPState.autoHuntRange, 2.5, 6.0);
            SkyOPState.jumpBoostMultiplier = number(p, "SkyOPState.jumpBoostMultiplier", SkyOPState.jumpBoostMultiplier, 1.0, 3.0);
            SkyOPState.bridgeDelayTicks = integer(p, "SkyOPState.bridgeDelayTicks", SkyOPState.bridgeDelayTicks, 1, 10);
            SkyOPState.autoEatThreshold = (int) SkyOPSettingsSchema.validate("SkyOPState.autoEatThreshold",
                    integer(p, "SkyOPState.autoEatThreshold", SkyOPState.autoEatThreshold, 2, 18), SkyOPState.autoEatThreshold);
            SkyOPState.autoRefillThreshold = (int) SkyOPSettingsSchema.validate("SkyOPState.autoRefillThreshold",
                    integer(p, "SkyOPState.autoRefillThreshold", SkyOPState.autoRefillThreshold, 1, 16), SkyOPState.autoRefillThreshold);
            SkyOPState.hudX = integer(p, "SkyOPState.hudX", SkyOPState.hudX, 0, 10000);
            SkyOPState.hudY = integer(p, "SkyOPState.hudY", SkyOPState.hudY, 0, 10000);
            SkyOPState.HUD_POSITIONS.clear();
            for (SkyOPModuleId module : SkyOPModuleId.values()) {
                String prefix = "hud." + module.name() + ".";
                if (p.getProperty(prefix + "x") != null && p.getProperty(prefix + "y") != null) {
                    int savedX = integer(p, prefix + "x", 0, 0, 10000);
                    int savedY = integer(p, prefix + "y", 0, 0, 10000);
                    SkyOPState.HUD_POSITIONS.put(module, new SkyOPState.HudPosition(savedX, savedY));
                }
            }
            SkyOPState.hudAccentColor = 0xFF000000 | (integer(p, "SkyOPState.hudAccentColor", SkyOPState.hudAccentColor,
                    Integer.MIN_VALUE, Integer.MAX_VALUE) & 0x00FFFFFF);
            SkyOPState.customHudText = p.getProperty("SkyOPState.customHudText", SkyOPState.customHudText);
            SkyOPState.FAVORITES.clear();
            for (SkyOPModuleId module : SkyOPModuleId.values()) {
                if (bool(p, "favorite." + module.name(), false)) SkyOPState.FAVORITES.add(module);
                String keybind = p.getProperty("keybind." + module.name());
                KeyMapping mapping = SkyOPState.MODULE_KEYBINDS.get(module);
                if (keybind != null && mapping != null) {
                    try {
                        int parsedCode = Integer.parseInt(keybind);
                        int code = parsedCode > 0 && SkyOPSdlKeyboardIndex.isValidStateIndex(parsedCode) ? parsedCode : -1;
                        SkyOPState.MODULE_KEY_CODES.put(module, code);
                        mapping.setKey(code < 0 ? InputConstants.UNKNOWN : InputConstants.Type.KEYBOARD.getOrCreate(code));
                    } catch (NumberFormatException ignored) { }
                }
            }
            SkyOPModuleManager.loadSettings(p);
            KeyMapping.resetMapping();
        } catch (IOException exception) {
            System.err.println("[SkyOP] Could not load config: " + exception.getMessage());
        }
    }

private static void notify(Minecraft client, String module, boolean enabled) {
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal(
                    "[SkyOP] " + module + ": " + (enabled ? "ON" : "OFF")));
        }
    }

private static boolean bool(Properties p, String key, boolean fallback) {
        String value = p.getProperty(key);
        if (value == null) return fallback;
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

private static int integer(Properties p, String key, int fallback, int min, int max) {
        try {
            return SkyOPMath.clamp(Integer.parseInt(p.getProperty(key, Integer.toString(fallback))), min, max);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

private static double number(Properties p, String key, double fallback, double min, double max) {
        try {
            double parsed = Double.parseDouble(p.getProperty(key, Double.toString(fallback)));
            return Double.isFinite(parsed) ? SkyOPMath.clamp(parsed, min, max) : fallback;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    static void cleanupWorldState(Minecraft client) {
        SkyOPMovementModules.cleanup(client);
        SkyOPVisualModules.cleanup(client);
        SkyOPInventoryManager.stop(client);
        SkyOPTelemetry.reset();
    }

}
