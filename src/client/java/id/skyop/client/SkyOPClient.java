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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * SkyOP Client modules for Minecraft Java 26.3.
 * Client-side movement/combat changes remain subject to server authority.
 */
public final class SkyOPClient implements ClientModInitializer {
    public enum Category {
        COMBAT("Combat"), MOVEMENT("Movement"), VISUAL("Visual"), PLAYER("Player");
        private final String title;
        Category(String title) { this.title = title; }
        public String title() { return title; }
    }

    public enum Module {
        KILL_AURA("KillAura", "Rotates toward and attacks selected nearby targets.", Category.COMBAT),
        TRIGGER_BOT("TriggerBot", "Attacks a valid target under the crosshair.", Category.COMBAT),
        AUTO_CLICKER("AutoClicker", "Repeats attacks while the attack key is held.", Category.COMBAT),
        AUTO_SPRINT("Auto Sprint", "Automatically sprints while moving forward.", Category.MOVEMENT),
        SPEED("Speed", "Applies a configurable horizontal movement boost.", Category.MOVEMENT),
        FLIGHT("Flight", "Client-side flight controls using jump and sneak.", Category.MOVEMENT),
        ENTITY_ESP("Entity ESP", "Highlights nearby loaded living entities.", Category.VISUAL),
        GAMMA_BOOST("Gamma Boost", "Sets Minecraft's vanilla gamma option to maximum.", Category.VISUAL),
        HUD("HUD", "Displays module status, FPS and coordinates.", Category.VISUAL),
        NO_FALL("NoFall", "Clears the local fall-distance accumulator.", Category.PLAYER);

        private final String title;
        private final String description;
        private final Category category;
        Module(String title, String description, Category category) {
            this.title = title;
            this.description = description;
            this.category = category;
        }
        public String title() { return title; }
        public String description() { return description; }
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

    private static final EnumSet<Module> ENABLED = EnumSet.of(Module.HUD);
    private static final Map<UUID, GlowState> ORIGINAL_GLOW = new HashMap<>();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("skyop-client.properties");

    public static boolean targetPlayers = false;
    public static boolean targetMobs = true;
    public static boolean auraRotate = true;
    public static boolean auraRequireLineOfSight = true;
    public static double auraRange = 4.2;
    public static int auraDelayTicks = 8;
    public static int triggerDelayTicks = 3;
    public static int autoClickerCps = 12;
    public static double speedMultiplier = 1.7;
    public static double flightSpeed = 0.38;
    public static double espRange = 48.0;
    public static boolean espPlayers = true;
    public static boolean espMobs = true;

    private static int auraCooldown;
    private static int triggerCooldown;
    private static int clickerCooldown;
    private static Double originalGamma;
    private static Boolean originalNoGravity;
    private static final float ATTACK_STRENGTH_THRESHOLD = 0.90F;

    private record GlowState(Entity entity, boolean wasGlowing) { }

    private static KeyMapping key(String translationKey, int keyCode) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translationKey, InputConstants.Type.KEYBOARD, keyCode, KEY_CATEGORY
        ));
    }

    @Override
    public void onInitializeClient() {
        loadConfig();
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("skyop", "overlay"),
                (graphics, deltaTracker) -> renderHud(Minecraft.getInstance(), graphics)
        );
        ClientTickEvents.END_CLIENT_TICK.register(SkyOPClient::onClientTick);
    }

    public static boolean isEnabled(Module module) { return ENABLED.contains(module); }
    public static int enabledModuleCount() { return ENABLED.size(); }

    public static void setEnabled(Minecraft client, Module module, boolean enabled) {
        if (enabled) ENABLED.add(module); else ENABLED.remove(module);

        if (module == Module.ENTITY_ESP && !enabled) restoreGlow();
        if (module == Module.FLIGHT && !enabled) restoreFlight(client);
        if (module == Module.GAMMA_BOOST && !enabled) restoreGamma(client);

        saveConfig();
        notify(client, module.title(), enabled);
    }

    public static void toggle(Minecraft client, Module module) {
        setEnabled(client, module, !isEnabled(module));
    }

    public static void disableAll(Minecraft client) {
        ENABLED.clear();
        auraCooldown = 0;
        triggerCooldown = 0;
        clickerCooldown = 0;
        restoreGlow();
        restoreGamma(client);
        restoreFlight(client);
        saveConfig();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("[SkyOP] All modules disabled."));
        }
    }

    private static void onClientTick(Minecraft client) {
        handleKeybinds(client);
        if (client.player == null || client.level == null) {
            restoreGlow();
            restoreGamma(client);
            originalNoGravity = null;
            return;
        }

        if (isEnabled(Module.AUTO_SPRINT)) tickAutoSprint(client);
        if (isEnabled(Module.SPEED)) tickSpeed(client);
        if (isEnabled(Module.FLIGHT)) tickFlight(client);
        if (isEnabled(Module.NO_FALL) && client.player.fallDistance > 1.0F) {
            client.player.fallDistance = 0.0F;
        }
        if (isEnabled(Module.ENTITY_ESP)) tickEsp(client);
        if (isEnabled(Module.GAMMA_BOOST)) tickGamma(client);

        if (isEnabled(Module.KILL_AURA)) tickKillAura(client); else auraCooldown = 0;
        if (isEnabled(Module.TRIGGER_BOT)) tickTriggerBot(client); else triggerCooldown = 0;
        if (isEnabled(Module.AUTO_CLICKER)) tickAutoClicker(client); else clickerCooldown = 0;
    }

    private static void handleKeybinds(Minecraft client) {
        if (KEY_GUI.consumeClick()) client.gui.setScreen(new SkyOPClickGui());
        if (KEY_HUD.consumeClick()) toggle(client, Module.HUD);
        if (KEY_AUTO_CLICKER.consumeClick()) toggle(client, Module.AUTO_CLICKER);
        if (KEY_SPRINT.consumeClick()) toggle(client, Module.AUTO_SPRINT);
        if (KEY_SPEED.consumeClick()) toggle(client, Module.SPEED);
        if (KEY_AURA.consumeClick()) toggle(client, Module.KILL_AURA);
        if (KEY_ESP.consumeClick()) toggle(client, Module.ENTITY_ESP);
        if (KEY_NO_FALL.consumeClick()) toggle(client, Module.NO_FALL);
        if (KEY_FLIGHT.consumeClick()) toggle(client, Module.FLIGHT);
        if (KEY_DISABLE_ALL.consumeClick()) disableAll(client);
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
        if (originalNoGravity == null) originalNoGravity = player.isNoGravity();
        player.setNoGravity(true);

        double vertical = 0.0;
        if (client.options.keyJump.isDown()) vertical += clamp(flightSpeed, 0.12, 0.9);
        if (client.options.keyShift.isDown()) vertical -= clamp(flightSpeed, 0.12, 0.9);
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, vertical, motion.z);
        player.fallDistance = 0.0F;
    }

    private static void restoreFlight(Minecraft client) {
        if (client.player != null && originalNoGravity != null) {
            client.player.setNoGravity(originalNoGravity);
        }
        originalNoGravity = null;
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
        List<LivingEntity> entities = client.level.getEntitiesOfClass(
                LivingEntity.class, area,
                entity -> entity != player && entity.isAlive() && shouldEsp(entity)
        );
        for (LivingEntity entity : entities) {
            ORIGINAL_GLOW.putIfAbsent(entity.getUUID(),
                    new GlowState(entity, entity.isCurrentlyGlowing()));
            entity.setGlowingTag(true);
        }
    }

    private static boolean shouldEsp(LivingEntity entity) {
        return entity instanceof Player ? espPlayers : espMobs;
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
        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition();

        for (LivingEntity candidate : candidates) {
            if (candidate.isInvisible()) continue;
            if (auraRequireLineOfSight && !player.hasLineOfSight(candidate)) continue;

            double distance = player.distanceToSqr(candidate);
            if (distance > bestDistance) continue;

            Vec3 direction = candidate.getBoundingBox().getCenter().subtract(eye).normalize();
            if (look.dot(direction) < 0.35) continue;
            bestDistance = distance;
            target = candidate;
        }

        if (target == null) return;
        if (auraRotate) rotateToward(player, target);
        attack(client, target);
        auraCooldown = clamp(auraDelayTicks, 1, 20);
    }

    private static void tickTriggerBot(Minecraft client) {
        if (triggerCooldown > 0) { triggerCooldown--; return; }
        if (!(client.hitResult instanceof EntityHitResult entityHit)) return;
        Entity entity = entityHit.getEntity();
        if (entity instanceof LivingEntity living && living != client.player
                && living.isAlive() && isCombatTarget(living)) {
            attack(client, living);
            triggerCooldown = clamp(triggerDelayTicks, 1, 12);
        }
    }

    private static void tickAutoClicker(Minecraft client) {
        if (clickerCooldown > 0) { clickerCooldown--; return; }
        if (!client.options.keyAttack.isDown()) return;
        HitResult result = client.hitResult;
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity living
                && living != client.player && living.isAlive() && isCombatTarget(living)) {
            attack(client, living);
            clickerCooldown = Math.max(1, 20 / clamp(autoClickerCps, 4, 20));
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
        float yaw = player.getYRot() + Mth.wrapDegrees(yawTarget - player.getYRot()) * 0.42F;
        float pitch = player.getXRot() + Mth.wrapDegrees(pitchTarget - player.getXRot()) * 0.42F;
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);
    }

    private static void attack(Minecraft client, LivingEntity target) {
        if (client.player == null || client.gameMode == null
                || client.player.getAttackStrengthScale(0.0F) < ATTACK_STRENGTH_THRESHOLD) return;
        client.gameMode.attack(client.player, target);
        client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
    }

    private static void renderHud(Minecraft client, GuiGraphicsExtractor graphics) {
        if (!isEnabled(Module.HUD) || client.player == null || client.gui.hud.isHidden()) return;

        int x = 8, y = 8, width = 164, lineHeight = 12;
        int activeCount = 0;
        for (Module module : Module.values()) {
            if (module != Module.HUD && isEnabled(module)) activeCount++;
        }

        int height = 44 + activeCount * lineHeight;
        graphics.fill(x, y, x + width, y + height, 0xB5101726);
        graphics.fill(x, y, x + width, y + 2, 0xFF65C8FF);
        graphics.text(client.font, Component.literal("SKYOP  /  CLIENT"), x + 9, y + 8, 0xFF76D5FF, true);
        graphics.text(client.font, Component.literal("26.3  •  " + activeCount + " active"),
                x + 9, y + 22, 0xFFB9C8D8, true);

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
        graphics.text(client.font, Component.literal(coords), x + 9, y + height + 7, 0xFFFFFFFF, true);
        graphics.text(client.font, Component.literal(client.getFps() + " FPS"),
                x + 9, y + height + 19, 0xFF76D5FF, true);
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
        p.setProperty("targetPlayers", Boolean.toString(targetPlayers));
        p.setProperty("targetMobs", Boolean.toString(targetMobs));
        p.setProperty("auraRotate", Boolean.toString(auraRotate));
        p.setProperty("auraRequireLineOfSight", Boolean.toString(auraRequireLineOfSight));
        p.setProperty("auraRange", Double.toString(auraRange));
        p.setProperty("auraDelayTicks", Integer.toString(auraDelayTicks));
        p.setProperty("triggerDelayTicks", Integer.toString(triggerDelayTicks));
        p.setProperty("autoClickerCps", Integer.toString(autoClickerCps));
        p.setProperty("speedMultiplier", Double.toString(speedMultiplier));
        p.setProperty("flightSpeed", Double.toString(flightSpeed));
        p.setProperty("espRange", Double.toString(espRange));
        p.setProperty("espPlayers", Boolean.toString(espPlayers));
        p.setProperty("espMobs", Boolean.toString(espMobs));

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
            auraRange = number(p, "auraRange", auraRange, 2.5, 6.0);
            auraDelayTicks = integer(p, "auraDelayTicks", auraDelayTicks, 1, 20);
            triggerDelayTicks = integer(p, "triggerDelayTicks", triggerDelayTicks, 1, 12);
            autoClickerCps = integer(p, "autoClickerCps", autoClickerCps, 4, 20);
            speedMultiplier = number(p, "speedMultiplier", speedMultiplier, 1.0, 2.8);
            flightSpeed = number(p, "flightSpeed", flightSpeed, 0.12, 0.9);
            espRange = number(p, "espRange", espRange, 8.0, 96.0);
            espPlayers = bool(p, "espPlayers", espPlayers);
            espMobs = bool(p, "espMobs", espMobs);
        } catch (IOException exception) {
            System.err.println("[SkyOP] Could not load config: " + exception.getMessage());
        }
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
