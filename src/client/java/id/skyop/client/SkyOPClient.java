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
        COMBAT("Combat"), MOVEMENT("Movement"), VISUAL("Visual"), PLAYER("Player");

        private final String title;
        Category(String title) { this.title = title; }
        public String title() { return title; }
    }

    public enum Preset { PVE, OP, VISUAL }

    public enum Module {
        KILL_AURA("KillAura", "Prioritises a nearby configured target.", Category.COMBAT),
        TRIGGER_BOT("TriggerBot", "Attacks a valid target under the crosshair.", Category.COMBAT),
        AUTO_CLICKER("AutoClicker", "Repeats attacks while the attack key is held.", Category.COMBAT),

        AUTO_SPRINT("Auto Sprint", "Sprints while moving forward.", Category.MOVEMENT),
        SPEED("Speed", "Applies a configurable horizontal movement boost.", Category.MOVEMENT),
        FLIGHT("Flight", "Local flight controls using jump and sneak.", Category.MOVEMENT),
        AUTO_JUMP("Auto Jump", "Automatically jumps while moving forward.", Category.MOVEMENT),
        STEP_ASSIST("Step Assist", "Jumps when forward movement meets a wall.", Category.MOVEMENT),
        SPIDER("Spider", "Climbs a wall while moving into it.", Category.MOVEMENT),

        ENTITY_ESP("Entity ESP", "Highlights nearby loaded living entities.", Category.VISUAL),
        GAMMA_BOOST("Gamma Boost", "Sets vanilla gamma to its maximum normal value.", Category.VISUAL),
        HUD("HUD", "Displays enabled modules, coordinates and FPS.", Category.VISUAL),
        TARGET_HUD("Target HUD", "Shows health and distance for the crosshair target.", Category.VISUAL),
        KEYSTROKES_HUD("Keystrokes", "Displays movement key states on screen.", Category.VISUAL),
        ARMOR_HUD("Armor HUD", "Displays health and armor status.", Category.VISUAL),

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
    public static boolean auraTargetLowestHealth = false;
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

    private static int auraCooldown;
    private static int triggerCooldown;
    private static int clickerCooldown;
    private static int autoJumpCooldown;
    private static int stepAssistCooldown;
    private static Double originalGamma;
    private static Boolean originalNoGravity;
    private static Player flightPlayer;
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
            restoreFlight(client);
            return;
        }

        if (client.screen != null) return;

        if (isEnabled(Module.AUTO_SPRINT)) tickAutoSprint(client);
        if (isEnabled(Module.SPEED)) tickSpeed(client);
        if (isEnabled(Module.FLIGHT)) tickFlight(client);
        if (isEnabled(Module.AUTO_JUMP)) tickAutoJump(client); else autoJumpCooldown = 0;
        if (isEnabled(Module.STEP_ASSIST)) tickStepAssist(client); else stepAssistCooldown = 0;
        if (isEnabled(Module.SPIDER)) tickSpider(client);

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
        List<LivingEntity> entities = client.level.getEntitiesOfClass(
                LivingEntity.class, area,
                entity -> entity != player && entity.isAlive() && shouldEsp(entity)
        );

        Set<UUID> seen = new HashSet<>();
        for (LivingEntity entity : entities) {
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
        float bestHealth = Float.MAX_VALUE;
        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition();

        for (LivingEntity candidate : candidates) {
            if (candidate.isInvisible()) continue;
            double distance = player.distanceToSqr(candidate);
            if (distance > range * range) continue;
            if (auraRequireLineOfSight && !player.hasLineOfSight(candidate)) continue;

            Vec3 direction = candidate.getBoundingBox().getCenter().subtract(eye).normalize();
            if (look.dot(direction) < 0.35) continue;

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
        if (client.player == null || client.gui.hud.isHidden()) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        if (isEnabled(Module.HUD)) {
            int x = 8, y = 8, width = 184, lineHeight = 12;
            int activeCount = 0;
            for (Module module : Module.values()) {
                if (module != Module.HUD && isEnabled(module)) activeCount++;
            }

            int height = 44 + activeCount * lineHeight;
            graphics.fill(x, y, x + width, y + height, 0xC5101726);
            graphics.fill(x, y, x + width, y + 2, 0xFF61C7F2);
            graphics.text(client.font, Component.literal("SKYOP  /  CLIENT"), x + 10, y + 8, 0xFF76D5FF, true);
            graphics.text(client.font, Component.literal("26.3  •  " + activeCount + " active"),
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
            int x = screenWidth - 194, y = 8;
            graphics.fill(x, y, x + 186, y + 58, 0xC5101726);
            graphics.fill(x, y, x + 186, y + 2, 0xFF61C7F2);
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
                graphics.text(client.font, Component.literal("NO TARGET"), x + 10, y + 25, 0xFF9AAABD, true);
            }
        }

        if (isEnabled(Module.KEYSTROKES_HUD)) {
            int x = 10, y = screenHeight - 62;
            drawKey(graphics, x + 22, y, "W", client.options.keyUp.isDown());
            drawKey(graphics, x, y + 22, "A", client.options.keyLeft.isDown());
            drawKey(graphics, x + 22, y + 22, "S", client.options.keyDown.isDown());
            drawKey(graphics, x + 44, y + 22, "D", client.options.keyRight.isDown());
            drawKey(graphics, x + 66, y + 22, "J", client.options.keyJump.isDown());
        }

        if (isEnabled(Module.ARMOR_HUD)) {
            int x = screenWidth - 150, y = screenHeight - 38;
            graphics.fill(x, y, x + 142, y + 28, 0xC5101726);
            graphics.text(client.font, Component.literal(
                            String.format("HP %.0f/%.0f  •  ARMOR %d",
                                    client.player.getHealth(), client.player.getMaxHealth(),
                                    client.player.getArmorValue())),
                    x + 8, y + 10, 0xFFF1F6FB, true);
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
        p.setProperty("targetPlayers", Boolean.toString(targetPlayers));
        p.setProperty("targetMobs", Boolean.toString(targetMobs));
        p.setProperty("auraRotate", Boolean.toString(auraRotate));
        p.setProperty("auraRequireLineOfSight", Boolean.toString(auraRequireLineOfSight));
        p.setProperty("auraTargetLowestHealth", Boolean.toString(auraTargetLowestHealth));
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
            auraTargetLowestHealth = bool(p, "auraTargetLowestHealth", auraTargetLowestHealth);
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
