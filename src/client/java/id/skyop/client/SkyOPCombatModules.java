package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.component.SwingAnimation;

import java.util.List;

/** Concrete combat modules. Target selection and attack helpers live with combat. */
public final class SkyOPCombatModules {
    private SkyOPCombatModules() { }

    private static int auraCooldown;
    private static int triggerCooldown;
    private static int clickerCooldown;
    private static int autoHuntCooldown;
    private static final float ATTACK_STRENGTH_THRESHOLD = 0.90F;




    static void tickAutoHunt(Minecraft client) {
        if (autoHuntCooldown > 0) { autoHuntCooldown--; return; }
        Player player = client.player;
        double range = SkyOPMath.clamp(SkyOPState.autoHuntRange, 2.5, 6.0);
        List<Monster> candidates = client.level.getEntitiesOfClass(
                Monster.class, player.getBoundingBox().inflate(range),
                entity -> entity.isAlive() && !entity.isInvisible()
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



    static void tickKillAura(Minecraft client) {
        if (auraCooldown > 0) { auraCooldown--; return; }

        var player = client.player;
        double range = SkyOPMath.clamp(SkyOPState.auraRange, 2.5, 6.0);
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
            if (SkyOPState.auraRequireLineOfSight && !player.hasLineOfSight(candidate)) continue;

            Vec3 direction = candidate.getBoundingBox().getCenter().subtract(eye).normalize();
            double halfFovRadians = Math.toRadians(SkyOPMath.clamp(SkyOPState.auraFov, 30.0, 180.0) / 2.0);
            if (look.dot(direction) < Math.cos(halfFovRadians)) continue;

            if (SkyOPState.auraTargetLowestHealth) {
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
        if (SkyOPState.auraRotate) rotateToward(player, target);
        if (attack(client, target)) {
            auraCooldown = SkyOPMath.clamp(SkyOPState.auraDelayTicks, 1, 20);
        }
    }



    static void tickTriggerBot(Minecraft client) {
        if (triggerCooldown > 0) { triggerCooldown--; return; }
        if (!(client.hitResult instanceof EntityHitResult entityHit)) return;
        Entity entity = entityHit.getEntity();
        if (entity instanceof LivingEntity living && living != client.player
                && living.isAlive() && isCombatTarget(living)) {
            if (attack(client, living)) {
                triggerCooldown = SkyOPMath.clamp(SkyOPState.triggerDelayTicks, 1, 12);
            }
        }
    }



    static void tickAutoClicker(Minecraft client) {
        if (clickerCooldown > 0) { clickerCooldown--; return; }
        if (!client.options.keyAttack.isDown()) return;
        HitResult result = client.hitResult;
        if (result instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity living
                && living != client.player && living.isAlive() && isCombatTarget(living)) {
            if (attack(client, living)) {
                clickerCooldown = Math.max(1, 20 / SkyOPMath.clamp(SkyOPState.autoClickerCps, 4, 20));
            }
        }
    }

private static boolean isCombatTarget(LivingEntity entity) {
        if (entity instanceof Player) return SkyOPState.targetPlayers;
        return SkyOPState.targetMobs && entity instanceof Monster;
    }

private static void rotateToward(Player player, LivingEntity target) {
        Vec3 delta = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yawTarget = (float) (Mth.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0F;
        float pitchTarget = (float) (-(Mth.atan2(delta.y, horizontal) * (180.0 / Math.PI)));
        float rotationStep = (float) SkyOPMath.clamp(SkyOPState.auraRotationSpeed, 0.10, 1.0);
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
        SkyOPTelemetry.recordAttack();
        return true;
    }

    public static final class KillAura extends SkyOPModule {
        public KillAura(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.KILL_AURA, settings); }
        @Override public void onTick(Minecraft client) { tickKillAura(client); }
        @Override public void onDisable(Minecraft client) { auraCooldown = 0; }
    }

    public static final class AutoHunt extends SkyOPModule {
        public AutoHunt(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_HUNT, settings); }
        @Override public void onTick(Minecraft client) { tickAutoHunt(client); }
        @Override public void onDisable(Minecraft client) { autoHuntCooldown = 0; }
    }

    public static final class TriggerBot extends SkyOPModule {
        public TriggerBot(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.TRIGGER_BOT, settings); }
        @Override public void onTick(Minecraft client) { tickTriggerBot(client); }
        @Override public void onDisable(Minecraft client) { triggerCooldown = 0; }
    }

    public static final class AutoClicker extends SkyOPModule {
        public AutoClicker(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_CLICKER, settings); }
        @Override public void onTick(Minecraft client) { tickAutoClicker(client); }
        @Override public void onDisable(Minecraft client) { clickerCooldown = 0; }
    }
}
