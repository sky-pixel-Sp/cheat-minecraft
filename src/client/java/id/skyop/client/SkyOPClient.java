package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SkyOP client entrypoint for Minecraft Java 26.3.
 * All modules are client-side. Server-authoritative mechanics can be rejected
 * or corrected by multiplayer servers.
 */
public final class SkyOPClient implements ClientModInitializer {
    private static final Identifier CATEGORY_ID = Identifier.fromNamespaceAndPath("skyop", "main");
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(CATEGORY_ID);

    private static final KeyMapping KEY_SPRINT = key("key.skyop.toggle_sprint", 295); // F6
    private static final KeyMapping KEY_SPEED = key("key.skyop.toggle_speed", 296);  // F7
    private static final KeyMapping KEY_AURA = key("key.skyop.toggle_aura", 297);   // F8
    private static final KeyMapping KEY_ESP = key("key.skyop.toggle_esp", 298);     // F9
    private static final KeyMapping KEY_NOFALL = key("key.skyop.toggle_nofall", 299); // F10
    private static final KeyMapping KEY_HUD = key("key.skyop.toggle_hud", 293);     // F4
    private static final KeyMapping KEY_DISABLE_ALL = key("key.skyop.disable_all", 301); // F12

    private static boolean autoSprint;
    private static boolean speedBoost;
    private static boolean killAura;
    private static boolean entityEsp;
    private static boolean noFall;
    private static boolean hudEnabled = true;
    private static int auraDelay;

    // Preserve each entity's original glow state so disabling ESP does not
    // blindly turn off glow that another mod or the server had already applied.
    private static final Map<UUID, GlowState> ORIGINAL_GLOW = new HashMap<>();

    private record GlowState(Entity entity, boolean wasGlowing) { }

    private static KeyMapping key(String translationKey, int glfwKeyCode) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translationKey,
                InputConstants.Type.KEYBOARD,
                glfwKeyCode,
                CATEGORY
        ));
    }

    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("skyop", "status_hud"),
                (graphics, deltaTracker) -> renderHud(Minecraft.getInstance(), graphics)
        );

        ClientTickEvents.END_CLIENT_TICK.register(SkyOPClient::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        handleKeybinds(client);

        if (client.player == null || client.level == null) {
            return;
        }

        if (autoSprint) {
            tickAutoSprint(client);
        }
        if (speedBoost) {
            tickSpeed(client);
        }
        if (killAura) {
            tickKillAura(client);
        }
        if (entityEsp) {
            tickEsp(client);
        }
        if (noFall) {
            // This only changes the client-side fall accumulator. A remote
            // server remains authoritative and may still apply fall damage.
            if (client.player.fallDistance > 1.5F) {
                client.player.fallDistance = 0.0F;
            }
        }
    }

    private static void handleKeybinds(Minecraft client) {
        if (KEY_SPRINT.consumeClick()) {
            autoSprint = !autoSprint;
            notify(client, "Auto Sprint", autoSprint);
        }
        if (KEY_SPEED.consumeClick()) {
            speedBoost = !speedBoost;
            notify(client, "Speed Boost", speedBoost);
        }
        if (KEY_AURA.consumeClick()) {
            killAura = !killAura;
            auraDelay = 0;
            notify(client, "KillAura (hostile mobs only)", killAura);
        }
        if (KEY_ESP.consumeClick()) {
            entityEsp = !entityEsp;
            if (!entityEsp) {
                restoreGlow(client);
            }
            notify(client, "Entity ESP", entityEsp);
        }
        if (KEY_NOFALL.consumeClick()) {
            noFall = !noFall;
            notify(client, "NoFall", noFall);
        }
        if (KEY_HUD.consumeClick()) {
            hudEnabled = !hudEnabled;
            notify(client, "SkyOP HUD", hudEnabled);
        }
        if (KEY_DISABLE_ALL.consumeClick()) {
            disableAll(client);
        }
    }

    private static void tickAutoSprint(Minecraft client) {
        var player = client.player;
        boolean movingForward = client.options.keyUp.isDown();
        boolean sneaking = client.options.keyShift.isDown();
        if (movingForward && !sneaking && player.isAlive()) {
            player.setSprinting(true);
        }
    }

    private static void tickSpeed(Minecraft client) {
        var player = client.player;
        if (!player.isAlive() || player.isPassenger()) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0, look.z);
        if (forward.lengthSqr() < 1.0E-5) {
            return;
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);

        double forwardInput = (client.options.keyUp.isDown() ? 1.0 : 0.0)
                - (client.options.keyDown.isDown() ? 1.0 : 0.0);
        double sideInput = (client.options.keyRight.isDown() ? 1.0 : 0.0)
                - (client.options.keyLeft.isDown() ? 1.0 : 0.0);
        Vec3 direction = forward.scale(forwardInput).add(right.scale(sideInput));
        if (direction.lengthSqr() < 1.0E-4) {
            return;
        }

        direction = direction.normalize();
        Vec3 current = player.getDeltaMovement();
        // Roughly three times ordinary sprint speed; vertical velocity is preserved.
        player.setDeltaMovement(direction.x * 0.78, current.y, direction.z * 0.78);
    }

    private static void tickKillAura(Minecraft client) {
        if (client.gameMode == null || auraDelay > 0) {
            auraDelay = Math.max(0, auraDelay - 1);
            return;
        }

        var player = client.player;
        AABB searchBox = player.getBoundingBox().inflate(4.5);
        List<Monster> candidates = client.level.getEntitiesOfClass(
                Monster.class,
                searchBox,
                Monster::isAlive
        );

        Monster bestTarget = null;
        double bestDistance = 4.5 * 4.5;
        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition();

        for (Monster candidate : candidates) {
            if (candidate.isInvisible() || !player.hasLineOfSight(candidate)) {
                continue;
            }
            double distance = player.distanceToSqr(candidate);
            if (distance > bestDistance) {
                continue;
            }
            Vec3 toTarget = candidate.getBoundingBox().getCenter().subtract(eye).normalize();
            if (look.dot(toTarget) < 0.72) {
                continue;
            }
            bestDistance = distance;
            bestTarget = candidate;
        }

        if (bestTarget != null && player.getAttackStrengthScale(0.0F) >= 0.90F) {
            client.gameMode.attack(player, bestTarget);
            player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
            auraDelay = 8;
        }
    }

    private static void tickEsp(Minecraft client) {
        var player = client.player;
        AABB area = player.getBoundingBox().inflate(48.0);
        List<LivingEntity> entities = client.level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity != player && entity.isAlive()
        );

        for (LivingEntity entity : entities) {
            ORIGINAL_GLOW.putIfAbsent(
                    entity.getUUID(),
                    new GlowState(entity, entity.isCurrentlyGlowing())
            );
            entity.setGlowingTag(true);
        }
    }

    private static void restoreGlow(Minecraft client) {
        for (GlowState state : ORIGINAL_GLOW.values()) {
            state.entity().setGlowingTag(state.wasGlowing());
        }
        ORIGINAL_GLOW.clear();
    }

    private static void disableAll(Minecraft client) {
        autoSprint = false;
        speedBoost = false;
        killAura = false;
        entityEsp = false;
        noFall = false;
        auraDelay = 0;
        restoreGlow(client);
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal("[SkyOP] All modules disabled."));
        }
    }

    private static void notify(Minecraft client, String module, boolean enabled) {
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal(
                    "[SkyOP] " + module + ": " + (enabled ? "ON" : "OFF")
            ));
        }
    }

    private static void renderHud(Minecraft client, GuiGraphicsExtractor graphics) {
        if (!hudEnabled || client.player == null || client.gui.hud.isHidden()) {
            return;
        }

        String[] lines = {
                "SKYOP CLIENT  |  26.3",
                "F6  Auto Sprint: " + state(autoSprint),
                "F7  Speed Boost: " + state(speedBoost),
                "F8  KillAura: " + state(killAura),
                "F9  Entity ESP: " + state(entityEsp),
                "F10 NoFall: " + state(noFall),
                "F12 Disable all"
        };

        int panelHeight = 8 + lines.length * 11;
        graphics.fill(5, 5, 154, panelHeight, 0xA9000000);
        for (int i = 0; i < lines.length; i++) {
            int color = i == 0 ? 0xFF74D7FF : 0xFFFFFFFF;
            graphics.text(client.font, Component.literal(lines[i]), 10, 9 + i * 11, color, true);
        }
    }

    private static String state(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }
}
