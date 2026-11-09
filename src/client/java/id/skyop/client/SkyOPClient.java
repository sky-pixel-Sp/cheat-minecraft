package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * SkyOP Client modules for Minecraft Java 26.3.
 * Movement and combat requests are still validated by the connected server.
 */
public final class SkyOPClient implements ClientModInitializer {
    public enum Category {
        COMBAT("Combat"), MOVEMENT("Movement"), VISUAL("Visual"), PLAYER("Player"),
        INVENTORY("Inventory"), FAVORITES("Favorites");

        private final String fallbackTitle;
        Category(String title) { this.fallbackTitle = title; }
        public String title() {
            String key = "category." + name().toLowerCase();
            String translated = SkyOPLanguage.text(key);
            return translated.equals(key) ? fallbackTitle : translated;
        }
    }

    public enum Preset { PVE, OP, VISUAL }

    public enum Module {
        KILL_AURA("KillAura", "Prioritises a nearby configured target.", Category.COMBAT),
        TRIGGER_BOT("TriggerBot", "Attacks a valid target under the crosshair.", Category.COMBAT),
        AUTO_CLICKER("AutoClicker", "Repeats attacks while the attack key is held.", Category.COMBAT),
        AUTO_HUNT("Auto Hunt", "Automatically attacks the nearest loaded hostile mob without camera movement.", Category.COMBAT),

        AUTO_SPRINT("Auto Sprint", "Sprints while moving forward.", Category.MOVEMENT),
        SPEED("Speed", "Applies a configurable horizontal movement boost.", Category.MOVEMENT),
        FLIGHT("Flight", "Local flight controls using jump and sneak.", Category.MOVEMENT),
        AUTO_JUMP("Auto Jump", "Automatically jumps while moving forward.", Category.MOVEMENT),
        STEP_ASSIST("Step Assist", "Jumps when forward movement meets a wall.", Category.MOVEMENT),
        SPIDER("Spider", "Climbs a wall while moving into it.", Category.MOVEMENT),
        AUTO_BRIDGE("Auto Bridge", "Places a selected hotbar block beneath the player while moving.", Category.MOVEMENT),
        INFINITE_JUMP("Infinite Jump", "Allows another jump after leaving the ground when jump is pressed again.", Category.MOVEMENT),
        JUMP_BOOST("Jump Boost", "Adjusts the local jump impulse multiplier.", Category.MOVEMENT),

        ENTITY_ESP("Entity ESP", "Highlights nearby loaded living entities and dropped items.", Category.VISUAL),
        ITEM_ESP("Item ESP", "Includes dropped item entities in the Entity ESP highlight filter.", Category.VISUAL),
        WAYPOINTS("Waypoints", "Displays saved coordinates and distances; press V to save a waypoint.", Category.VISUAL),
        MINIMAP("Mini Map", "Shows nearby entities already loaded by the client.", Category.VISUAL),
        GAMMA_BOOST("Gamma Boost", "Sets vanilla gamma to its maximum normal value.", Category.VISUAL),
        NIGHT_VISION("Night Vision", "Improves local visibility using the client gamma setting.", Category.VISUAL),
        HUD("HUD", "Displays enabled modules, coordinates and FPS.", Category.VISUAL),
        TARGET_HUD("Target HUD", "Shows health and distance for the crosshair target.", Category.VISUAL),
        KEYSTROKES_HUD("Keystrokes", "Displays movement key states on screen.", Category.VISUAL),
        ARMOR_HUD("Armor HUD", "Displays health and armor status.", Category.VISUAL),
        SPEEDOMETER("Speedometer", "Displays horizontal movement speed in blocks per second.", Category.VISUAL),
        CPS_HUD("CPS HUD", "Displays successful attack actions per second.", Category.VISUAL),
        POTION_HUD("Potion HUD", "Shows active potion effects and remaining duration.", Category.VISUAL),
        DURABILITY_HUD("Durability HUD", "Displays main-hand item durability.", Category.VISUAL),
        COMPASS_HUD("Compass HUD", "Shows compass direction and yaw.", Category.VISUAL),
        DIRECTION_HUD("Direction HUD", "Shows cardinal direction and coordinates.", Category.VISUAL),
        PING_HUD("Ping HUD", "Shows your current multiplayer latency.", Category.VISUAL),
        FPS_HUD("FPS HUD", "Shows the current frame rate.", Category.VISUAL),
        RADAR_HUD("Nearby Entity Radar", "Lists nearby entities already loaded by the client.", Category.VISUAL),
        TEXT_GUI("Text GUI", "Displays a configurable custom HUD message.", Category.VISUAL),

        NO_FALL("NoFall", "Clears the local fall-distance accumulator.", Category.PLAYER),

        AUTO_EAT("Auto Eat", "Uses safe food from your hotbar when hunger is low.", Category.INVENTORY),
        AUTO_ARMOR("Auto Armor", "Equips armor into empty armor slots.", Category.INVENTORY),
        AUTO_TOOL("Auto Tool", "Selects the best hotbar tool for the targeted block.", Category.INVENTORY),
        AUTO_REFILL("Auto Refill", "Refills low hotbar stacks from matching inventory items.", Category.INVENTORY),
        AUTO_FISH("Auto Fish", "Automatically reels on bite and recasts your fishing rod.", Category.INVENTORY),
        INVENTORY_CLEANER("Inventory Cleaner", "Consolidates matching stacks without dropping items.", Category.INVENTORY),
        HOTBAR_PRESETS("Hotbar Presets", "Saves and restores three hotbar layouts.", Category.INVENTORY),
        PROFILE_MANAGER("Profile Manager", "Manages JSON profiles, imports, exports and backups.", Category.INVENTORY);

        private final String title;
        private final String description;
        private final Category category;

        Module(String title, String description, Category category) {
            this.title = title;
            this.description = description;
            this.category = category;
        }

        public String title() { return SkyOPLanguage.moduleTitle(name(), title); }
        public String description() { return SkyOPLanguage.moduleDescription(name(), description); }
        public Category category() { return category; }
    }

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

    private static final EnumSet<Module> ENABLED = EnumSet.of(Module.HUD);
    private static final EnumSet<Module> FAVORITES = EnumSet.noneOf(Module.class);
    private static final Map<Module, KeyMapping> MODULE_KEYBINDS = new EnumMap<>(Module.class);
    private static final Map<Module, Integer> MODULE_KEY_CODES = new EnumMap<>(Module.class);
    private static final Map<Module, HudPosition> HUD_POSITIONS = new EnumMap<>(Module.class);
    private static final Map<UUID, GlowState> ORIGINAL_GLOW = new HashMap<>();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("skyop-client.properties");

    public static boolean targetPlayers = false;
    public static boolean targetMobs = true;
    public static boolean auraRotate = true;
    public static boolean auraRequireLineOfSight = true;
    public static boolean auraTargetLowestHealth = false;
    public static double auraFov = 110.0;
    public static double auraRotationSpeed = 0.42;
    public static double auraRange = 4.2;
    public static int auraDelayTicks = 8;
    public static int triggerDelayTicks = 3;
    public static int autoClickerCps = 12;
    public static double speedMultiplier = 1.7;
    public static double flightSpeed = 0.38;
    public static int autoJumpDelayTicks = 4;
    public static double spiderClimbSpeed = 0.22;
    public static double espRange = 48.0;
    public static boolean espPlayers = true;
    public static boolean espMobs = true;
    public static boolean espItems = true;
    public static double autoHuntRange = 4.5;
    public static double jumpBoostMultiplier = 1.35;
    public static int bridgeDelayTicks = 2;
    public static int autoEatThreshold = 14;
    public static int autoRefillThreshold = 2;
    public static int hudX = 8;
    public static int hudY = 8;
    public static int hudAccentColor = 0xFF61C7F2;
    public static String customHudText = "SkyOP Client • Have a great game!";
    private static final int[] HUD_PALETTE = {
            0xFF61C7F2, 0xFF8B7CFF, 0xFFFFB45B, 0xFF57D69A, 0xFFFF7885, 0xFFF1F6FB
    };

    private static int auraCooldown;
    private static int triggerCooldown;
    private static int clickerCooldown;
    private static int autoJumpCooldown;
    private static int stepAssistCooldown;
    private static int autoHuntCooldown;
    private static int bridgeCooldown;
    private static boolean jumpKeyWasDown;
    private static int cpsWindowTicks;
    private static int attacksThisWindow;
    private static int currentCps;
    private static Vec3 previousPosition;
    private static double currentSpeedBps;
    private static Double originalGamma;
    private static Boolean originalNoGravity;
    private static Player flightPlayer;
    private static final float ATTACK_STRENGTH_THRESHOLD = 0.90F;

    private record GlowState(Entity entity, boolean wasGlowing) { }
    private record HudPosition(int x, int y) { }

    private static KeyMapping key(String translationKey, int keyCode) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translationKey, InputConstants.Type.KEYBOARD, keyCode, KEY_CATEGORY
        ));
    }

    @Override
    public void onInitializeClient() {
        registerModuleKeybinds();
        loadConfig();
        SkyOPWaypointManager.load();
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("skyop", "overlay"),
                (graphics, deltaTracker) -> renderHud(Minecraft.getInstance(), graphics)
        );
        ClientTickEvents.END_CLIENT_TICK.register(SkyOPClient::onClientTick);
    }

    public static boolean isEnabled(Module module) { return ENABLED.contains(module); }
    public static int enabledModuleCount() { return ENABLED.size(); }
    public static boolean isFavorite(Module module) { return FAVORITES.contains(module); }
    public static boolean isModuleKeybindUnbound(Module module) {
        return module == null || MODULE_KEY_CODES.getOrDefault(module, -1) < 0;
    }
    public static int favoriteCount() { return FAVORITES.size(); }
    public static int hudAccentColor() { return hudAccentColor; }
    public static KeyMapping keybindFor(Module module) { return MODULE_KEYBINDS.get(module); }

    public static int hudXFor(Module module, int defaultX, int screenWidth, int elementWidth) {
        HudPosition saved = HUD_POSITIONS.get(module);
        int preferred = saved != null ? saved.x() : (module == Module.HUD ? hudX : defaultX);
        return clamp(preferred, 0, Math.max(0, screenWidth - elementWidth));
    }

    public static int hudYFor(Module module, int defaultY, int screenHeight, int elementHeight) {
        HudPosition saved = HUD_POSITIONS.get(module);
        int preferred = saved != null ? saved.y() : (module == Module.HUD ? hudY : defaultY);
        return clamp(preferred, 0, Math.max(0, screenHeight - elementHeight));
    }

    public static void setHudPosition(Module module, int x, int y,
                                      int screenWidth, int screenHeight,
                                      int elementWidth, int elementHeight) {
        if (module == null) return;
        int safeX = clamp(x, 0, Math.max(0, screenWidth - elementWidth));
        int safeY = clamp(y, 0, Math.max(0, screenHeight - elementHeight));
        HUD_POSITIONS.put(module, new HudPosition(safeX, safeY));
        if (module == Module.HUD) {
            hudX = safeX;
            hudY = safeY;
        }
    }

    public static void resetHudPositions() {
        HUD_POSITIONS.clear();
        hudX = 8;
        hudY = 8;
        saveConfig();
    }

    public static void toggleFavorite(Module module) {
        if (!FAVORITES.add(module)) FAVORITES.remove(module);
        saveConfig();
    }

    public static void setModuleKeybind(Module module, int scancode) {
        KeyMapping mapping = MODULE_KEYBINDS.get(module);
        if (mapping == null) return;
        int safeCode = scancode > 0 && SkyOPSdlKeyboardIndex.isValidStateIndex(scancode) ? scancode : -1;
        MODULE_KEY_CODES.put(module, safeCode);
        mapping.setKey(safeCode < 0
                ? InputConstants.UNKNOWN
                : InputConstants.Type.KEYBOARD.getOrCreate(safeCode));
        KeyMapping.resetMapping();
        saveConfig();
    }

    public static void cycleHudColor() {
        int index = 0;
        for (int i = 0; i < HUD_PALETTE.length; i++) {
            if (HUD_PALETTE[i] == hudAccentColor) index = i;
        }
        hudAccentColor = HUD_PALETTE[(index + 1) % HUD_PALETTE.length];
        saveConfig();
    }

    public static void setHudAccentColor(int color) {
        hudAccentColor = 0xFF000000 | (color & 0x00FFFFFF);
        saveConfig();
    }

    public static void setCustomHudText(String text) {
        if (text == null) return;
        String safe = text.strip();
        if (safe.length() > 80) safe = safe.substring(0, 80);
        customHudText = safe;
        saveConfig();
    }

    public static Path configPath() { return CONFIG_FILE; }

    public static void reloadConfigFromDisk() {
        ENABLED.clear();
        ENABLED.add(Module.HUD);
        FAVORITES.clear();
        loadConfig();
        if (!ENABLED.contains(Module.ENTITY_ESP)) restoreGlow();
        if (!ENABLED.contains(Module.GAMMA_BOOST)) restoreGamma(Minecraft.getInstance());
        if (!ENABLED.contains(Module.FLIGHT)) restoreFlight(Minecraft.getInstance());
    }

    private static void registerModuleKeybinds() {
        registerMapping(Module.HUD, KEY_HUD, InputConstants.KEY_F4);
        registerMapping(Module.AUTO_CLICKER, KEY_AUTO_CLICKER, InputConstants.KEY_F5);
        registerMapping(Module.AUTO_SPRINT, KEY_SPRINT, InputConstants.KEY_F6);
        registerMapping(Module.SPEED, KEY_SPEED, InputConstants.KEY_F7);
        registerMapping(Module.KILL_AURA, KEY_AURA, InputConstants.KEY_F8);
        registerMapping(Module.ENTITY_ESP, KEY_ESP, InputConstants.KEY_F9);
        registerMapping(Module.NO_FALL, KEY_NO_FALL, InputConstants.KEY_F10);
        registerMapping(Module.FLIGHT, KEY_FLIGHT, InputConstants.KEY_F11);

        for (Module module : Module.values()) {
            if (MODULE_KEYBINDS.containsKey(module)) continue;
            String keyName = "key.skyop.module." + module.name().toLowerCase();
            // Minecraft 26.3 uses SDL scancodes. Never pass raw -1 to the
            // native keyboard-state buffer for an unbound key mapping.
            KeyMapping mapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    keyName, InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY
            ));
            MODULE_KEYBINDS.put(module, mapping);
            MODULE_KEY_CODES.put(module, -1);
        }
    }

    private static void registerMapping(Module module, KeyMapping mapping, int keyCode) {
        MODULE_KEYBINDS.put(module, mapping);
        MODULE_KEY_CODES.put(module, keyCode);
    }

    public static void setEnabled(Minecraft client, Module module, boolean enabled) {
        if (enabled && !SkyOPModuleDependencies.isAvailable(client, module)) {
            String missing = SkyOPModuleDependencies.missingRequirement(client, module);
            if (client != null && client.player != null) {
                client.player.sendSystemMessage(Component.literal("[SkyOP] " + missing));
            }
            return;
        }
        if (enabled) ENABLED.add(module); else ENABLED.remove(module);

        if (module == Module.ENTITY_ESP && !enabled) restoreGlow();
        if (module == Module.FLIGHT && !enabled) restoreFlight(client);
        if ((module == Module.GAMMA_BOOST || module == Module.NIGHT_VISION) && !enabled
                && !isEnabled(Module.GAMMA_BOOST) && !isEnabled(Module.NIGHT_VISION)) restoreGamma(client);

        saveConfig();
        notify(client, module.title(), enabled);
    }

    public static void toggle(Minecraft client, Module module) {
        setEnabled(client, module, !isEnabled(module));
    }

    public static void cycleLanguage() {
        SkyOPLanguage.next();
        saveConfig();
    }

    public static void applyPreset(Minecraft client, Preset preset) {
        ENABLED.clear();
        targetPlayers = false;
        targetMobs = true;
        auraTargetLowestHealth = false;

        switch (preset) {
            case PVE -> {
                ENABLED.addAll(EnumSet.of(Module.HUD, Module.KILL_AURA, Module.AUTO_SPRINT, Module.STEP_ASSIST));
                auraRange = 5.0;
                auraDelayTicks = 4;
                speedMultiplier = 1.4;
            }
            case OP -> {
                ENABLED.addAll(EnumSet.of(
                        Module.HUD, Module.KILL_AURA, Module.AUTO_CLICKER,
                        Module.AUTO_SPRINT, Module.SPEED, Module.FLIGHT,
                        Module.AUTO_JUMP, Module.STEP_ASSIST, Module.SPIDER,
                        Module.ENTITY_ESP, Module.TARGET_HUD, Module.ARMOR_HUD, Module.NO_FALL
                ));
                auraRange = 6.0;
                auraDelayTicks = 1;
                auraTargetLowestHealth = true;
                speedMultiplier = 2.8;
                flightSpeed = 0.72;
                spiderClimbSpeed = 0.32;
                espRange = 96.0;
            }
            case VISUAL -> {
                ENABLED.addAll(EnumSet.of(
                        Module.HUD, Module.ENTITY_ESP, Module.GAMMA_BOOST,
                        Module.TARGET_HUD, Module.KEYSTROKES_HUD, Module.ARMOR_HUD
                ));
            }
        }

        if (!isEnabled(Module.ENTITY_ESP)) restoreGlow();
        if (!isEnabled(Module.FLIGHT)) restoreFlight(client);
        if (!isEnabled(Module.GAMMA_BOOST)) restoreGamma(client);
        if (!isEnabled(Module.AUTO_FISH)) SkyOPInventoryManager.stop(client);
        saveConfig();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("[SkyOP] " + preset.name() + " profile loaded."));
        }
    }

    public static void disableAll(Minecraft client) {
        ENABLED.clear();
        auraCooldown = 0;
        triggerCooldown = 0;
        clickerCooldown = 0;
        autoJumpCooldown = 0;
        stepAssistCooldown = 0;
        autoHuntCooldown = 0;
        bridgeCooldown = 0;
        jumpKeyWasDown = false;
        restoreGlow();
        restoreGamma(client);
        restoreFlight(client);
        SkyOPInventoryManager.stop(client);
        saveConfig();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("[SkyOP] All modules disabled."));
        }
    }

    private static void onClientTick(Minecraft client) {
        handleKeybinds(client);
        ensureMouseCaptured(client);
        if (client.player == null || client.level == null) {
            restoreGlow();
            restoreGamma(client);
            restoreFlight(client);
            return;
        }

        updateTelemetry(client);
        if (client.gui.screen() != null) return;

        if (isEnabled(Module.AUTO_SPRINT)) tickAutoSprint(client);
        if (isEnabled(Module.SPEED)) tickSpeed(client);
        if (isEnabled(Module.FLIGHT)) tickFlight(client);
        if (isEnabled(Module.AUTO_JUMP)) tickAutoJump(client); else autoJumpCooldown = 0;
        if (isEnabled(Module.STEP_ASSIST)) tickStepAssist(client); else stepAssistCooldown = 0;
        if (isEnabled(Module.SPIDER)) tickSpider(client);
        if (isEnabled(Module.AUTO_BRIDGE)) tickAutoBridge(client); else bridgeCooldown = 0;
        if (isEnabled(Module.JUMP_BOOST)) tickJumpBoost(client);
        if (isEnabled(Module.INFINITE_JUMP)) tickInfiniteJump(client); else jumpKeyWasDown = client.options.keyJump.isDown();

        if (isEnabled(Module.NO_FALL) && client.player.fallDistance > 1.0F) {
            client.player.fallDistance = 0.0F;
        }
        if (isEnabled(Module.ENTITY_ESP)) tickEsp(client);
        if (isEnabled(Module.GAMMA_BOOST) || isEnabled(Module.NIGHT_VISION)) tickGamma(client);
        else restoreGamma(client);

        if (isEnabled(Module.KILL_AURA)) tickKillAura(client); else auraCooldown = 0;
        if (isEnabled(Module.AUTO_HUNT)) tickAutoHunt(client); else autoHuntCooldown = 0;
        if (isEnabled(Module.TRIGGER_BOT)) tickTriggerBot(client); else triggerCooldown = 0;
        if (isEnabled(Module.AUTO_CLICKER)) tickAutoClicker(client); else clickerCooldown = 0;
        SkyOPInventoryManager.tick(client);
    }

    /**
     * Recover Minecraft's relative mouse mode if another screen/client transition
     * leaves the pointer released while the player is back in active gameplay.
     * Never steal the pointer while the window is unfocused or a UI is open.
     */
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
        for (Map.Entry<Module, KeyMapping> entry : MODULE_KEYBINDS.entrySet()) {
            while (entry.getValue().consumeClick()) toggle(client, entry.getKey());
        }
    }

    private static void tickAutoSprint(Minecraft client) {
        var player = client.player;
        if (client.options.keyUp.isDown() && !client.options.keyShift.isDown()
                && player.isAlive() && !player.isPassenger()) {
            player.setSprinting(true);
        }
    }

    private static void tickSpeed(Minecraft client) {
        var player = client.player;
        if (!player.isAlive() || player.isPassenger()) return;

        double forward = (client.options.keyUp.isDown() ? 1.0 : 0.0)
                - (client.options.keyDown.isDown() ? 1.0 : 0.0);
        double side = (client.options.keyRight.isDown() ? 1.0 : 0.0)
                - (client.options.keyLeft.isDown() ? 1.0 : 0.0);
        if (forward == 0.0 && side == 0.0) return;

        float yaw = player.getYRot() * ((float) Math.PI / 180.0F);
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double dx = side * cos - forward * sin;
        double dz = forward * cos + side * sin;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-5) return;

        Vec3 current = player.getDeltaMovement();
        double boostedSpeed = 0.28 * clamp(speedMultiplier, 1.0, 2.8);
        player.setDeltaMovement(dx / length * boostedSpeed, current.y, dz / length * boostedSpeed);
    }

    private static void tickFlight(Minecraft client) {
        var player = client.player;
        if (originalNoGravity == null) {
            originalNoGravity = player.isNoGravity();
            flightPlayer = player;
        }
        player.setNoGravity(true);

        double vertical = 0.0;
        if (client.options.keyJump.isDown()) vertical += clamp(flightSpeed, 0.12, 0.9);
        if (client.options.keyShift.isDown()) vertical -= clamp(flightSpeed, 0.12, 0.9);
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, vertical, motion.z);
        player.fallDistance = 0.0F;
    }

    private static void tickAutoJump(Minecraft client) {
        if (autoJumpCooldown > 0) { autoJumpCooldown--; return; }
        var player = client.player;
        if (!client.options.keyUp.isDown() || client.options.keyShift.isDown()
                || !player.isAlive() || player.isPassenger() || !player.onGround()
                || player.isInWater() || player.isInLava()) return;
        player.jumpFromGround();
        autoJumpCooldown = clamp(autoJumpDelayTicks, 2, 12);
    }

    private static void tickStepAssist(Minecraft client) {
        if (stepAssistCooldown > 0) { stepAssistCooldown--; return; }
        var player = client.player;
        if (player.horizontalCollision && player.onGround()
                && client.options.keyUp.isDown() && !client.options.keyShift.isDown()
                && player.isAlive() && !player.isPassenger()) {
            player.jumpFromGround();
            stepAssistCooldown = 5;
        }
    }

    private static void tickSpider(Minecraft client) {
        var player = client.player;
        if (!player.horizontalCollision || !client.options.keyUp.isDown()
                || client.options.keyShift.isDown() || !player.isAlive() || player.isPassenger()) return;

        Vec3 motion = player.getDeltaMovement();
        double climb = clamp(spiderClimbSpeed, 0.08, 0.38);
        if (motion.y < climb) {
            player.setDeltaMovement(motion.x, climb, motion.z);
            player.fallDistance = 0.0F;
        }
    }

    private static void tickAutoBridge(Minecraft client) {
        if (bridgeCooldown > 0) { bridgeCooldown--; return; }
        if (client.gameMode == null) return;
        Player player = client.player;
        boolean moving = client.options.keyUp.isDown() || client.options.keyDown.isDown()
                || client.options.keyLeft.isDown() || client.options.keyRight.isDown();
        if (!moving || !player.isAlive() || player.isPassenger() || player.isSpectator()) return;

        BlockPos target = player.blockPosition().below();
        if (!client.level.getBlockState(target).isAir()) return;
        BlockPos support = target.below();
        if (client.level.getBlockState(support).isAir()) return;

        int previousSlot = player.getInventory().selected;
        int blockSlot = -1;
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof BlockItem) {
                blockSlot = slot;
                break;
            }
        }
        if (blockSlot < 0) return;

        player.getInventory().selected = blockSlot;
        BlockHitResult hit = new BlockHitResult(
                new Vec3(support.getX() + 0.5, support.getY() + 1.0, support.getZ() + 0.5),
                Direction.UP, support, false);
        client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        player.getInventory().selected = previousSlot;
        bridgeCooldown = clamp(bridgeDelayTicks, 1, 10);
    }

    private static void tickJumpBoost(Minecraft client) {
        Player player = client.player;
        if (!client.options.keyJump.isDown() || !player.onGround()
                || !player.isAlive() || player.isPassenger()) return;
        double boost = 0.42 * clamp(jumpBoostMultiplier, 1.0, 3.0);
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, Math.max(motion.y, boost), motion.z);
    }

    private static void tickInfiniteJump(Minecraft client) {
        Player player = client.player;
        boolean jumpDown = client.options.keyJump.isDown();
        if (jumpDown && !jumpKeyWasDown && !player.onGround()
                && player.isAlive() && !player.isPassenger()) {
            double boost = 0.42 * clamp(jumpBoostMultiplier, 1.0, 3.0);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, Math.max(motion.y, boost), motion.z);
            player.fallDistance = 0.0F;
        }
        jumpKeyWasDown = jumpDown;
    }

    private static void tickAutoHunt(Minecraft client) {
        if (autoHuntCooldown > 0) { autoHuntCooldown--; return; }
        Player player = client.player;
        double range = clamp(autoHuntRange, 2.5, 6.0);
        List<Monster> candidates = client.level.getEntitiesOfClass(
                Monster.class, player.getBoundingBox().inflate(range),
                entity -> entity != player && entity.isAlive() && !entity.isInvisible()
        );
        Monster target = null;
        double bestDistance = range * range;
        for (Monster candidate : candidates) {
            double distance = player.distanceToSqr(candidate);
            if (distance < bestDistance && player.hasLineOfSight(candidate)) {
                target = candidate;
                bestDistance = distance;
            }
        }
        if (target != null && attack(client, target)) {
            autoHuntCooldown = 4;
        }
    }

    private static void restoreFlight(Minecraft client) {
        Player target = flightPlayer != null ? flightPlayer : client.player;
        if (target != null && originalNoGravity != null) {
            target.setNoGravity(originalNoGravity);
        }
        originalNoGravity = null;
        flightPlayer = null;
    }

    private static void tickGamma(Minecraft client) {
        if (originalGamma == null) originalGamma = client.options.gamma().get();
        client.options.gamma().set(1.0);
    }

    private static void restoreGamma(Minecraft client) {
        if (originalGamma != null) {
            client.options.gamma().set(originalGamma);
            originalGamma = null;
        }
    }

    private static void tickEsp(Minecraft client) {
        var player = client.player;
        AABB area = player.getBoundingBox().inflate(clamp(espRange, 8.0, 96.0));
        List<Entity> entities = client.level.getEntitiesOfClass(
                Entity.class, area,
                entity -> entity != player && entity.isAlive() && shouldEsp(entity)
        );

        Set<UUID> seen = new HashSet<>();
        for (Entity entity : entities) {
            UUID id = entity.getUUID();
            seen.add(id);
            ORIGINAL_GLOW.putIfAbsent(id, new GlowState(entity, entity.isCurrentlyGlowing()));
            entity.setGlowingTag(true);
        }

        Iterator<Map.Entry<UUID, GlowState>> iterator = ORIGINAL_GLOW.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, GlowState> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
                GlowState state = entry.getValue();
                state.entity().setGlowingTag(state.wasGlowing());
                iterator.remove();
            }
        }
    }

    private static boolean shouldEsp(Entity entity) {
        if (entity instanceof Player) return espPlayers;
        if (entity instanceof ItemEntity) return espItems || isEnabled(Module.ITEM_ESP);
        return entity instanceof LivingEntity && espMobs;
    }

    private static void restoreGlow() {
        for (GlowState state : ORIGINAL_GLOW.values()) {
            state.entity().setGlowingTag(state.wasGlowing());
        }
        ORIGINAL_GLOW.clear();
    }

    private static void tickKillAura(Minecraft client) {
        if (auraCooldown > 0) { auraCooldown--; return; }

        var player = client.player;
        double range = clamp(auraRange, 2.5, 6.0);
        List<LivingEntity> candidates = client.level.getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(range),
                entity -> entity != player && entity.isAlive() && isCombatTarget(entity)
        );

        LivingEntity target = null;
        double bestDistance = range * range;
        float bestHealth = Float.MAX_VALUE;
        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition();

        for (LivingEntity candidate : candidates) {
            if (candidate.isInvisible()) continue;
            double distance = player.distanceToSqr(candidate);
            if (distance > range * range) continue;
            if (auraRequireLineOfSight && !player.hasLineOfSight(candidate)) continue;

            Vec3 direction = candidate.getBoundingBox().getCenter().subtract(eye).normalize();
            double halfFovRadians = Math.toRadians(clamp(auraFov, 30.0, 180.0) / 2.0);
            if (look.dot(direction) < Math.cos(halfFovRadians)) continue;

            if (auraTargetLowestHealth) {
                float health = candidate.getHealth();
                if (health < bestHealth || (health == bestHealth && distance < bestDistance)) {
                    bestHealth = health;
                    bestDistance = distance;
                    target = candidate;
                }
            } else if (distance < bestDistance) {
                bestDistance = distance;
                target = candidate;
            }
        }

        if (target == null) return;
        if (auraRotate) rotateToward(player, target);
        if (attack(client, target)) {
            auraCooldown = clamp(auraDelayTicks, 1, 20);
        }
    }

    private static void tickTriggerBot(Minecraft client) {
        if (triggerCooldown > 0) { triggerCooldown--; return; }
        if (!(client.hitResult instanceof EntityHitResult entityHit)) return;
        Entity entity = entityHit.getEntity();
        if (entity instanceof LivingEntity living && living != client.player
                && living.isAlive() && isCombatTarget(living)) {
            if (attack(client, living)) {
                triggerCooldown = clamp(triggerDelayTicks, 1, 12);
            }
        }
    }

    private static void tickAutoClicker(Minecraft client) {
        if (clickerCooldown > 0) { clickerCooldown--; return; }
        if (!client.options.keyAttack.isDown()) return;
        HitResult result = client.hitResult;
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity living
                && living != client.player && living.isAlive() && isCombatTarget(living)) {
            if (attack(client, living)) {
                clickerCooldown = Math.max(1, 20 / clamp(autoClickerCps, 4, 20));
            }
        }
    }

    private static boolean isCombatTarget(LivingEntity entity) {
        if (entity instanceof Player) return targetPlayers;
        return targetMobs && entity instanceof Monster;
    }

    private static void rotateToward(Player player, LivingEntity target) {
        Vec3 delta = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yawTarget = (float) (Mth.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0F;
        float pitchTarget = (float) (-(Mth.atan2(delta.y, horizontal) * (180.0 / Math.PI)));
        float rotationStep = (float) clamp(auraRotationSpeed, 0.10, 1.0);
        float yaw = player.getYRot() + Mth.wrapDegrees(yawTarget - player.getYRot()) * rotationStep;
        float pitch = player.getXRot() + Mth.wrapDegrees(pitchTarget - player.getXRot()) * rotationStep;
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);
    }

    private static boolean attack(Minecraft client, LivingEntity target) {
        if (client.player == null || client.gameMode == null
                || client.player.getAttackStrengthScale(0.0F) < ATTACK_STRENGTH_THRESHOLD) return false;
        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        attacksThisWindow++;
        return true;
    }

    private static void updateTelemetry(Minecraft client) {
        Player player = client.player;
        if (player == null) {
            previousPosition = null;
            currentSpeedBps = 0.0;
            return;
        }

        Vec3 position = player.position();
        if (previousPosition != null) {
            double dx = position.x - previousPosition.x;
            double dz = position.z - previousPosition.z;
            double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
            currentSpeedBps = speed <= 100.0 ? speed : 0.0;
        }
        previousPosition = position;

        cpsWindowTicks++;
        if (cpsWindowTicks >= 20) {
            currentCps = attacksThisWindow;
            attacksThisWindow = 0;
            cpsWindowTicks = 0;
        }
    }

    private static void renderHud(Minecraft client, GuiGraphicsExtractor graphics) {
        if (client.player == null || client.gui.hud.isHidden()) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        if (isEnabled(Module.HUD)) {
            int width = 184, lineHeight = 12;
            int activeCount = 0;
            for (Module module : Module.values()) {
                if (module != Module.HUD && isEnabled(module)) activeCount++;
            }
            int height = 44 + activeCount * lineHeight;
            int fullHeight = height + 26;
            int x = hudXFor(Module.HUD, hudX, screenWidth, 190);
            int y = hudYFor(Module.HUD, hudY, screenHeight, fullHeight);
            graphics.fill(x, y, x + width, y + height, 0xC5101726);
            graphics.fill(x, y, x + width, y + 2, hudAccentColor);
            graphics.text(client.font, Component.literal("SKYOP  /  CLIENT"), x + 10, y + 8, hudAccentColor, true);
            graphics.text(client.font, Component.literal("26.3  •  " + activeCount + " " + SkyOPLanguage.text("gui.active")),
                    x + 10, y + 22, 0xFFB9C8D8, true);

            int row = 0;
            for (Module module : Module.values()) {
                if (module != Module.HUD && isEnabled(module)) {
                    graphics.text(client.font, Component.literal("• " + module.title()),
                            x + 10, y + 40 + row * lineHeight, 0xFFE7F0F8, true);
                    row++;
                }
            }

            String coords = String.format("XYZ %.1f / %.1f / %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ());
            graphics.text(client.font, Component.literal(coords), x + 10, y + height + 7, 0xFFFFFFFF, true);
            graphics.text(client.font, Component.literal(client.getFps() + " FPS"),
                    x + 10, y + height + 19, 0xFF76D5FF, true);
        }

        if (isEnabled(Module.TARGET_HUD)) {
            int x = hudXFor(Module.TARGET_HUD, screenWidth - 194, screenWidth, 186);
            int y = hudYFor(Module.TARGET_HUD, 8, screenHeight, 58);
            graphics.fill(x, y, x + 186, y + 58, 0xC5101726);
            graphics.fill(x, y, x + 186, y + 2, hudAccentColor);
            if (client.hitResult instanceof EntityHitResult hit
                    && hit.getEntity() instanceof LivingEntity target && target.isAlive()) {
                String targetName = target.getName().getString();
                graphics.text(client.font, Component.literal(trimText(targetName, 22)),
                        x + 10, y + 9, 0xFFF1F6FB, true);
                graphics.text(client.font, Component.literal(
                                String.format("HP %.1f / %.1f  •  %.1fm",
                                        target.getHealth(), target.getMaxHealth(), client.player.distanceTo(target))),
                        x + 10, y + 25, 0xFFB9C8D8, true);
                int barWidth = 164;
                graphics.fill(x + 10, y + 43, x + 10 + barWidth, y + 49, 0xFF36465A);
                float ratio = target.getMaxHealth() <= 0 ? 0
                        : Mth.clamp(target.getHealth() / target.getMaxHealth(), 0.0F, 1.0F);
                graphics.fill(x + 10, y + 43, x + 10 + (int) (barWidth * ratio), y + 49, 0xFF57D69A);
            } else {
                graphics.text(client.font, Component.literal(SkyOPLanguage.text("gui.no_target")), x + 10, y + 25, 0xFF9AAABD, true);
            }
        }

        if (isEnabled(Module.KEYSTROKES_HUD)) {
            int x = hudXFor(Module.KEYSTROKES_HUD, 10, screenWidth, 86);
            int y = hudYFor(Module.KEYSTROKES_HUD, screenHeight - 62, screenHeight, 42);
            drawKey(graphics, x + 22, y, "W", client.options.keyUp.isDown());
            drawKey(graphics, x, y + 22, "A", client.options.keyLeft.isDown());
            drawKey(graphics, x + 22, y + 22, "S", client.options.keyDown.isDown());
            drawKey(graphics, x + 44, y + 22, "D", client.options.keyRight.isDown());
            drawKey(graphics, x + 66, y + 22, "J", client.options.keyJump.isDown());
        }

        if (isEnabled(Module.ARMOR_HUD)) {
            int x = hudXFor(Module.ARMOR_HUD, screenWidth - 150, screenWidth, 142);
            int y = hudYFor(Module.ARMOR_HUD, screenHeight - 38, screenHeight, 28);
            graphics.fill(x, y, x + 142, y + 28, 0xC5101726);
            graphics.text(client.font, Component.literal(
                            String.format("HP %.0f/%.0f  •  ARMOR %d",
                                    client.player.getHealth(), client.player.getMaxHealth(),
                                    client.player.getArmorValue())),
                    x + 8, y + 10, 0xFFF1F6FB, true);
        }

        if (isEnabled(Module.POTION_HUD)) {
            int x = hudXFor(Module.POTION_HUD, screenWidth - 198, screenWidth, 190);
            int y = hudYFor(Module.POTION_HUD, 76, screenHeight, 96);
            graphics.fill(x, y, x + 190, y + 8 + client.player.getActiveEffects().size() * 11, 0xC5101726);
            graphics.text(client.font, Component.literal("EFFECTS"), x + 8, y + 4, hudAccentColor, true);
            int row = 0;
            for (var effect : client.player.getActiveEffects()) {
                if (row >= 7) break;
                String name = effect.getEffect().value().getDisplayName().getString();
                graphics.text(client.font, Component.literal(trimText(name, 18) + "  " + effect.getDuration() / 20 + "s"),
                        x + 8, y + 17 + row * 11, 0xFFF1F6FB, true);
                row++;
            }
        }

        if (isEnabled(Module.DURABILITY_HUD)) {
            var stack = client.player.getMainHandItem();
            int x = hudXFor(Module.DURABILITY_HUD, screenWidth / 2 - 82, screenWidth, 164);
            int y = hudYFor(Module.DURABILITY_HUD, screenHeight - 94, screenHeight, 24);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
                int percent = (int) (100.0 * remaining / Math.max(1, stack.getMaxDamage()));
                graphics.fill(x, y, x + 164, y + 24, 0xC5101726);
                graphics.text(client.font, Component.literal("DURABILITY  " + percent + "%"),
                        x + 8, y + 8, hudAccentColor, true);
                graphics.fill(x + 8, y + 18, x + 156, y + 21, 0xFF36465A);
                graphics.fill(x + 8, y + 18, x + 8 + (int) (148 * percent / 100.0), y + 21, hudAccentColor);
            }
        }

        if (isEnabled(Module.COMPASS_HUD) || isEnabled(Module.DIRECTION_HUD)) {
            int x = hudXFor(Module.COMPASS_HUD, screenWidth / 2 - 48, screenWidth, 96);
            int y = hudYFor(Module.COMPASS_HUD, 8, screenHeight, 23);
            String direction = compassDirection(client.player.getYRot());
            graphics.fill(x, y, x + 96, y + 23, 0xC5101726);
            graphics.text(client.font, Component.literal(direction + "  " + Math.round(client.player.getYRot()) + "°"),
                    x + 8, y + 8, hudAccentColor, true);
        }

        if (isEnabled(Module.PING_HUD)) {
            int x = hudXFor(Module.PING_HUD, screenWidth - 100, screenWidth, 92);
            int y = hudYFor(Module.PING_HUD, 8, screenHeight, 22);
            int ping = getCurrentPing(client);
            graphics.fill(x, y, x + 92, y + 22, 0xC5101726);
            graphics.text(client.font, Component.literal("PING  " + (ping < 0 ? "--" : ping + " ms")),
                    x + 8, y + 7, hudAccentColor, true);
        }

        if (isEnabled(Module.FPS_HUD)) {
            int x = hudXFor(Module.FPS_HUD, screenWidth - 100, screenWidth, 92);
            int y = hudYFor(Module.FPS_HUD, 34, screenHeight, 22);
            graphics.fill(x, y, x + 92, y + 22, 0xC5101726);
            graphics.text(client.font, Component.literal("FPS  " + client.getFps()),
                    x + 8, y + 7, hudAccentColor, true);
        }

        if (isEnabled(Module.RADAR_HUD)) {
            List<LivingEntity> nearby = client.level.getEntitiesOfClass(LivingEntity.class,
                    client.player.getBoundingBox().inflate(32.0),
                    entity -> entity != client.player && entity.isAlive());
            nearby.sort(java.util.Comparator.comparingDouble(client.player::distanceToSqr));
            int count = Math.min(6, nearby.size());
            int x = hudXFor(Module.RADAR_HUD, screenWidth - 202, screenWidth, 194);
            int y = hudYFor(Module.RADAR_HUD, screenHeight / 2 - 12, screenHeight, 90);
            graphics.fill(x, y, x + 194, y + 24 + count * 11, 0xC5101726);
            graphics.text(client.font, Component.literal("NEARBY / LOADED ENTITIES"),
                    x + 8, y + 5, hudAccentColor, true);
            for (int i = 0; i < count; i++) {
                LivingEntity entity = nearby.get(i);
                graphics.text(client.font, Component.literal(
                                trimText(entity.getName().getString(), 17) + "  " +
                                        String.format("%.1fm", client.player.distanceTo(entity))),
                        x + 8, y + 18 + i * 11, 0xFFF1F6FB, true);
            }
        }

        if (isEnabled(Module.WAYPOINTS)) {
            List<SkyOPWaypointManager.Waypoint> points = SkyOPWaypointManager.nearest(client, 5);
            int x = hudXFor(Module.WAYPOINTS, 8, screenWidth, 190);
            int y = hudYFor(Module.WAYPOINTS, screenHeight / 2 + 40, screenHeight, 22 + points.size() * 12);
            graphics.fill(x, y, x + 190, y + 22 + points.size() * 12, 0xC5101726);
            graphics.fill(x, y, x + 190, y + 2, hudAccentColor);
            graphics.text(client.font, Component.literal("WAYPOINTS"), x + 8, y + 6, hudAccentColor, true);
            for (int i = 0; i < points.size(); i++) {
                SkyOPWaypointManager.Waypoint point = points.get(i);
                graphics.text(client.font, Component.literal(point.name() + "  "
                                + point.x() + " " + point.y() + " " + point.z() + "  "
                                + String.format("%.0fm", SkyOPWaypointManager.distance(client, point))),
                        x + 8, y + 18 + i * 12, 0xFFF1F6FB, true);
            }
        }

        if (isEnabled(Module.MINIMAP)) {
            int size = 104;
            int x = hudXFor(Module.MINIMAP, 8, screenWidth, size);
            int y = hudYFor(Module.MINIMAP, screenHeight - size - 8, screenHeight, size);
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            graphics.fill(x, y, x + size, y + size, 0xC5101726);
            graphics.fill(x, y, x + size, y + 2, hudAccentColor);
            graphics.fill(centerX - 1, y + 8, centerX + 1, y + size - 8, 0x664B6078);
            graphics.fill(x + 8, centerY - 1, x + size - 8, centerY + 1, 0x664B6078);
            graphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFFFF);
            double yaw = Math.toRadians(client.player.getYRot());
            for (Entity entity : client.level.getEntitiesOfClass(Entity.class,
                    client.player.getBoundingBox().inflate(48.0), candidate -> candidate != client.player)) {
                double dx = entity.getX() - client.player.getX();
                double dz = entity.getZ() - client.player.getZ();
                double localX = dx * Math.cos(yaw) + dz * Math.sin(yaw);
                double localZ = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
                int dotX = centerX + (int) (localX / 48.0 * 42.0);
                int dotY = centerY + (int) (localZ / 48.0 * 42.0);
                int color = entity instanceof Player ? 0xFF61C7F2
                        : entity instanceof Monster ? 0xFFFF7885
                        : entity instanceof ItemEntity ? 0xFFFFB45B : 0xFF9AAABD;
                graphics.fill(dotX - 2, dotY - 2, dotX + 2, dotY + 2, color);
            }
        }

        if (isEnabled(Module.TEXT_GUI)) {
            int textWidth = Math.min(screenWidth,
                    Math.max(120, Math.min(480, client.font.width(customHudText) + 16)));
            int x = hudXFor(Module.TEXT_GUI, 8, screenWidth, textWidth);
            int y = hudYFor(Module.TEXT_GUI, screenHeight - 27, screenHeight, 19);
            graphics.fill(x, y, x + textWidth, y + 19, 0xA0101726);
            graphics.text(client.font, Component.literal(customHudText), x + 8, y + 6,
                    hudAccentColor, true);
        }

        if (isEnabled(Module.SPEEDOMETER)) {
            int x = hudXFor(Module.SPEEDOMETER, screenWidth / 2 - 58, screenWidth, 116);
            int y = hudYFor(Module.SPEEDOMETER, screenHeight - 37, screenHeight, 25);
            graphics.fill(x, y, x + 116, y + 25, 0xC5101726);
            graphics.fill(x, y, x + 116, y + 2, hudAccentColor);
            graphics.text(client.font, Component.literal(String.format("SPEED  %.2f b/s", currentSpeedBps)),
                    x + 10, y + 10, 0xFFF1F6FB, true);
        }

        if (isEnabled(Module.CPS_HUD)) {
            int x = hudXFor(Module.CPS_HUD, screenWidth / 2 - 48, screenWidth, 96);
            int y = hudYFor(Module.CPS_HUD, screenHeight - 66, screenHeight, 24);
            graphics.fill(x, y, x + 96, y + 24, 0xC5101726);
            graphics.fill(x, y, x + 96, y + 2, hudAccentColor);
            graphics.text(client.font, Component.literal("CPS  " + currentCps),
                    x + 10, y + 9, 0xFFF1F6FB, true);
        }
    }

    private static void drawKey(GuiGraphicsExtractor graphics, int x, int y, String label, boolean pressed) {
        int background = pressed ? 0xFF24516A : 0xC5202B3D;
        int foreground = pressed ? 0xFFFFFFFF : 0xFFB9C8D8;
        graphics.fill(x, y, x + 20, y + 19, background);
        graphics.text(Minecraft.getInstance().font, Component.literal(label), x + 7, y + 5, foreground, true);
    }

    private static String trimText(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    private static void notify(Minecraft client, String module, boolean enabled) {
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal(
                    "[SkyOP] " + module + ": " + (enabled ? "ON" : "OFF")));
        }
    }

    public static void saveConfig() {
        Properties p = new Properties();
        for (Module module : Module.values()) {
            p.setProperty("module." + module.name(), Boolean.toString(isEnabled(module)));
        }
        p.setProperty("language", SkyOPLanguage.code());
        p.setProperty("targetPlayers", Boolean.toString(targetPlayers));
        p.setProperty("targetMobs", Boolean.toString(targetMobs));
        p.setProperty("auraRotate", Boolean.toString(auraRotate));
        p.setProperty("auraRequireLineOfSight", Boolean.toString(auraRequireLineOfSight));
        p.setProperty("auraTargetLowestHealth", Boolean.toString(auraTargetLowestHealth));
        p.setProperty("auraFov", Double.toString(auraFov));
        p.setProperty("auraRotationSpeed", Double.toString(auraRotationSpeed));
        p.setProperty("auraRange", Double.toString(auraRange));
        p.setProperty("auraDelayTicks", Integer.toString(auraDelayTicks));
        p.setProperty("triggerDelayTicks", Integer.toString(triggerDelayTicks));
        p.setProperty("autoClickerCps", Integer.toString(autoClickerCps));
        p.setProperty("speedMultiplier", Double.toString(speedMultiplier));
        p.setProperty("flightSpeed", Double.toString(flightSpeed));
        p.setProperty("autoJumpDelayTicks", Integer.toString(autoJumpDelayTicks));
        p.setProperty("spiderClimbSpeed", Double.toString(spiderClimbSpeed));
        p.setProperty("espRange", Double.toString(espRange));
        p.setProperty("espPlayers", Boolean.toString(espPlayers));
        p.setProperty("espMobs", Boolean.toString(espMobs));
        p.setProperty("espItems", Boolean.toString(espItems));
        p.setProperty("autoHuntRange", Double.toString(autoHuntRange));
        p.setProperty("jumpBoostMultiplier", Double.toString(jumpBoostMultiplier));
        p.setProperty("bridgeDelayTicks", Integer.toString(bridgeDelayTicks));
        p.setProperty("autoEatThreshold", Integer.toString(autoEatThreshold));
        p.setProperty("autoRefillThreshold", Integer.toString(autoRefillThreshold));
        p.setProperty("hudX", Integer.toString(hudX));
        p.setProperty("hudY", Integer.toString(hudY));
        for (Map.Entry<Module, HudPosition> entry : HUD_POSITIONS.entrySet()) {
            String prefix = "hud." + entry.getKey().name() + ".";
            p.setProperty(prefix + "x", Integer.toString(entry.getValue().x()));
            p.setProperty(prefix + "y", Integer.toString(entry.getValue().y()));
        }
        p.setProperty("hudAccentColor", Integer.toString(hudAccentColor));
        p.setProperty("customHudText", customHudText);
        for (Module module : FAVORITES) p.setProperty("favorite." + module.name(), "true");
        for (Map.Entry<Module, KeyMapping> entry : MODULE_KEYBINDS.entrySet()) {
            p.setProperty("keybind." + entry.getKey().name(),
                    Integer.toString(MODULE_KEY_CODES.getOrDefault(entry.getKey(), -1)));
        }

        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                p.store(writer, "SkyOP Client configuration");
            }
        } catch (IOException exception) {
            System.err.println("[SkyOP] Could not save config: " + exception.getMessage());
        }
    }

    private static void loadConfig() {
        if (!Files.exists(CONFIG_FILE)) return;
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            p.load(reader);
            SkyOPLanguage.set(p.getProperty("language", "id"));
            for (Module module : Module.values()) {
                String value = p.getProperty("module." + module.name());
                if (value != null) {
                    if (Boolean.parseBoolean(value)) ENABLED.add(module);
                    else ENABLED.remove(module);
                }
            }
            targetPlayers = bool(p, "targetPlayers", targetPlayers);
            targetMobs = bool(p, "targetMobs", targetMobs);
            auraRotate = bool(p, "auraRotate", auraRotate);
            auraRequireLineOfSight = bool(p, "auraRequireLineOfSight", auraRequireLineOfSight);
            auraTargetLowestHealth = bool(p, "auraTargetLowestHealth", auraTargetLowestHealth);
            auraFov = number(p, "auraFov", auraFov, 30.0, 180.0);
            auraRotationSpeed = number(p, "auraRotationSpeed", auraRotationSpeed, 0.10, 1.0);
            auraRange = number(p, "auraRange", auraRange, 2.5, 6.0);
            auraDelayTicks = integer(p, "auraDelayTicks", auraDelayTicks, 1, 20);
            triggerDelayTicks = integer(p, "triggerDelayTicks", triggerDelayTicks, 1, 12);
            autoClickerCps = integer(p, "autoClickerCps", autoClickerCps, 4, 20);
            speedMultiplier = number(p, "speedMultiplier", speedMultiplier, 1.0, 2.8);
            flightSpeed = number(p, "flightSpeed", flightSpeed, 0.12, 0.9);
            autoJumpDelayTicks = integer(p, "autoJumpDelayTicks", autoJumpDelayTicks, 2, 12);
            spiderClimbSpeed = number(p, "spiderClimbSpeed", spiderClimbSpeed, 0.08, 0.38);
            espRange = number(p, "espRange", espRange, 8.0, 96.0);
            espPlayers = bool(p, "espPlayers", espPlayers);
            espMobs = bool(p, "espMobs", espMobs);
            espItems = bool(p, "espItems", espItems);
            autoHuntRange = number(p, "autoHuntRange", autoHuntRange, 2.5, 6.0);
            jumpBoostMultiplier = number(p, "jumpBoostMultiplier", jumpBoostMultiplier, 1.0, 3.0);
            bridgeDelayTicks = integer(p, "bridgeDelayTicks", bridgeDelayTicks, 1, 10);
            autoEatThreshold = (int) SkyOPSettingsSchema.validate("autoEatThreshold",
                    integer(p, "autoEatThreshold", autoEatThreshold, 2, 18), autoEatThreshold);
            autoRefillThreshold = (int) SkyOPSettingsSchema.validate("autoRefillThreshold",
                    integer(p, "autoRefillThreshold", autoRefillThreshold, 1, 16), autoRefillThreshold);
            hudX = integer(p, "hudX", hudX, 0, 10000);
            hudY = integer(p, "hudY", hudY, 0, 10000);
            HUD_POSITIONS.clear();
            for (Module module : Module.values()) {
                String prefix = "hud." + module.name() + ".";
                if (p.getProperty(prefix + "x") != null && p.getProperty(prefix + "y") != null) {
                    int savedX = integer(p, prefix + "x", 0, 0, 10000);
                    int savedY = integer(p, prefix + "y", 0, 0, 10000);
                    HUD_POSITIONS.put(module, new HudPosition(savedX, savedY));
                }
            }
            hudAccentColor = 0xFF000000 | (integer(p, "hudAccentColor", hudAccentColor,
                    Integer.MIN_VALUE, Integer.MAX_VALUE) & 0x00FFFFFF);
            customHudText = p.getProperty("customHudText", customHudText);
            FAVORITES.clear();
            for (Module module : Module.values()) {
                if (bool(p, "favorite." + module.name(), false)) FAVORITES.add(module);
                String keybind = p.getProperty("keybind." + module.name());
                KeyMapping mapping = MODULE_KEYBINDS.get(module);
                if (keybind != null && mapping != null) {
                    try {
                        int parsedCode = Integer.parseInt(keybind);
                        int code = parsedCode > 0 && SkyOPSdlKeyboardIndex.isValidStateIndex(parsedCode) ? parsedCode : -1;
                        MODULE_KEY_CODES.put(module, code);
                        mapping.setKey(code < 0 ? InputConstants.UNKNOWN : InputConstants.Type.KEYBOARD.getOrCreate(code));
                    } catch (NumberFormatException ignored) { }
                }
            }
            KeyMapping.resetMapping();
        } catch (IOException exception) {
            System.err.println("[SkyOP] Could not load config: " + exception.getMessage());
        }
    }

    private static int getCurrentPing(Minecraft client) {
        try {
            if (client.getConnection() == null || client.player == null) return -1;
            var info = client.getConnection().getPlayerInfo(client.player.getUUID());
            return info == null ? -1 : info.getLatency();
        } catch (RuntimeException ignored) {
            return -1;
        }
    }

    private static String compassDirection(float yaw) {
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int index = Math.floorMod(Math.round(yaw / 45.0F), 8);
        return directions[index];
    }

    private static boolean bool(Properties p, String key, boolean fallback) {
        String value = p.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static int integer(Properties p, String key, int fallback, int min, int max) {
        try {
            return clamp(Integer.parseInt(p.getProperty(key, Integer.toString(fallback))), min, max);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static double number(Properties p, String key, double fallback, double min, double max) {
        try {
            return clamp(Double.parseDouble(p.getProperty(key, Double.toString(fallback))), min, max);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
