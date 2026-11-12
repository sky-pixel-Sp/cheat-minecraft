package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

import java.util.List;

/** Concrete movement modules. Each module owns its tick handler and transient state. */
public final class SkyOPMovementModules {
    private SkyOPMovementModules() { }

    private static int autoJumpCooldown;
    private static int stepAssistCooldown;
    private static int bridgeCooldown;
    private static int autoMlgCooldown;
    private static int autoMlgOriginalSlot = -1;
    private static int autoMlgBucketSlot = -1;
    private static boolean jumpKeyWasDown;
    private static boolean jumpBoostKeyWasDown;
    private static Boolean originalNoGravity;
    private static net.minecraft.world.entity.player.Player flightPlayer;
    private static final float UNUSED = 0.0F;




    static void tickAutoSprint(Minecraft client) {
        var player = client.player;
        if (client.options.keyUp.isDown() && !client.options.keyShift.isDown()
                && player.isAlive() && !player.isPassenger()) {
            player.setSprinting(true);
        }
    }



    static void tickNoFall(Minecraft client) {
        if (client != null && client.player != null && client.player.fallDistance > 1.0F) {
            client.player.fallDistance = 0.0F;
        }
    }



    static void tickSpeed(Minecraft client) {
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
        double boostedSpeed = 0.28 * SkyOPMath.clamp(SkyOPState.speedMultiplier, 1.0, 2.8);
        player.setDeltaMovement(dx / length * boostedSpeed, current.y, dz / length * boostedSpeed);
    }



    static void tickFlight(Minecraft client) {
        var player = client.player;
        if (originalNoGravity == null) {
            originalNoGravity = player.isNoGravity();
            flightPlayer = player;
        }
        player.setNoGravity(true);

        double vertical = 0.0;
        if (client.options.keyJump.isDown()) vertical += SkyOPMath.clamp(SkyOPState.flightSpeed, 0.12, 0.9);
        if (client.options.keyShift.isDown()) vertical -= SkyOPMath.clamp(SkyOPState.flightSpeed, 0.12, 0.9);
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, vertical, motion.z);
        player.fallDistance = 0.0F;
    }



    static void tickAutoJump(Minecraft client) {
        if (autoJumpCooldown > 0) { autoJumpCooldown--; return; }
        var player = client.player;
        if (!client.options.keyUp.isDown() || client.options.keyShift.isDown()
                || !player.isAlive() || player.isPassenger() || !player.onGround()
                || player.isInWater() || player.isInLava()) return;
        player.jumpFromGround();
        autoJumpCooldown = SkyOPMath.clamp(SkyOPState.autoJumpDelayTicks, 2, 12);
    }



    static void tickStepAssist(Minecraft client) {
        if (stepAssistCooldown > 0) { stepAssistCooldown--; return; }
        var player = client.player;
        if (player.horizontalCollision && player.onGround()
                && client.options.keyUp.isDown() && !client.options.keyShift.isDown()
                && player.isAlive() && !player.isPassenger()) {
            player.jumpFromGround();
            stepAssistCooldown = 5;
        }
    }



    static void tickSpider(Minecraft client) {
        var player = client.player;
        if (!player.horizontalCollision || !client.options.keyUp.isDown()
                || client.options.keyShift.isDown() || !player.isAlive() || player.isPassenger()) return;

        Vec3 motion = player.getDeltaMovement();
        double climb = SkyOPMath.clamp(SkyOPState.spiderClimbSpeed, 0.08, 0.38);
        if (motion.y < climb) {
            player.setDeltaMovement(motion.x, climb, motion.z);
            player.fallDistance = 0.0F;
        }
    }



    static void tickAutoBridge(Minecraft client) {
        if (bridgeCooldown > 0) { bridgeCooldown--; return; }
        if (client.gameMode == null) return;
        LocalPlayer player = client.player;
        boolean moving = client.options.keyUp.isDown() || client.options.keyDown.isDown()
                || client.options.keyLeft.isDown() || client.options.keyRight.isDown();
        if (!moving || !player.isAlive() || player.isPassenger() || player.isSpectator()) return;

        BlockPos target = player.blockPosition().below();
        if (!client.level.getBlockState(target).isAir()) return;
        BlockPos support = null;
        Direction placeFace = null;
        for (Direction direction : List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
            BlockPos candidate = target.relative(direction);
            if (!client.level.getBlockState(candidate).isAir()) {
                support = candidate;
                placeFace = direction.getOpposite();
                break;
            }
        }
        if (support == null || placeFace == null) return;

        int previousSlot = player.getInventory().getSelectedSlot();
        int blockSlot = -1;
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof BlockItem) {
                blockSlot = slot;
                break;
            }
        }
        if (blockSlot < 0) return;

        player.getInventory().setSelectedSlot(blockSlot);
        Vec3 hitLocation = new Vec3(
                support.getX() + 0.5 + placeFace.getStepX() * 0.5,
                support.getY() + 0.5,
                support.getZ() + 0.5 + placeFace.getStepZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(hitLocation, placeFace, support, false);
        client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        player.getInventory().setSelectedSlot(previousSlot);
        bridgeCooldown = SkyOPMath.clamp(SkyOPState.bridgeDelayTicks, 1, 10);
    }



    static void tickJumpBoost(Minecraft client) {
        Player player = client.player;
        boolean jumpDown = client.options.keyJump.isDown();
        if (jumpDown && !jumpBoostKeyWasDown && player.onGround()
                && player.isAlive() && !player.isPassenger()) {
            double boost = 0.42 * SkyOPMath.clamp(SkyOPState.jumpBoostMultiplier, 1.0, 3.0);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, Math.max(motion.y, boost), motion.z);
        }
        jumpBoostKeyWasDown = jumpDown;
    }



    static void tickAutoMlg(Minecraft client) {
        if (client == null || client.player == null || client.level == null || client.gameMode == null) return;
        if (autoMlgCooldown > 0) {
            autoMlgCooldown--;
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.mlg_cooldown"));
            return;
        }
        LocalPlayer player = client.player;
        var inventory = player.getInventory();

        if (autoMlgOriginalSlot >= 0) {
            boolean waterUsed = autoMlgBucketSlot >= 0
                    && !inventory.getItem(autoMlgBucketSlot).is(net.minecraft.world.item.Items.WATER_BUCKET);
            if (player.onGround() || waterUsed) {
                restoreAutoMlgSlot(client);
                return;
            }
        }

        if (player.getAbilities().instabuild) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.creative"));
            return;
        }

        if (!SkyOPAutomationRules.shouldTriggerMlg(
                player.onGround(), player.fallDistance, player.getDeltaMovement().y,
                SkyOPState.autoMlgFallDistance)) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.not_falling"));
            return;
        }

        int bucketSlot = -1;
        for (int slot = 0; slot < net.minecraft.world.entity.player.Inventory.SELECTION_SIZE; slot++) {
            if (inventory.getItem(slot).is(net.minecraft.world.item.Items.WATER_BUCKET)) {
                bucketSlot = slot;
                break;
            }
        }
        if (bucketSlot < 0) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.no_bucket"));
            return;
        }

        BlockHitResult predictedLanding = predictLandingSurface(client, player);
        if (predictedLanding == null) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.no_landing"));
            return;
        }

        double reachSquared = player.getEyePosition().distanceToSqr(predictedLanding.getLocation());
        if (reachSquared > 4.5D * 4.5D) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.out_of_reach")
                            + " (" + String.format(java.util.Locale.ROOT, "%.2f", Math.sqrt(reachSquared)) + " blocks)");
            return;
        }

        float originalPitch = player.getXRot();
        try {
            // Aim downward temporarily and obtain a fresh vanilla-reach block hit.
            player.setXRot(90.0F);
            HitResult rayHit = player.pick(4.5D, 1.0F, false);
            if (rayHit.getType() != HitResult.Type.BLOCK || !(rayHit instanceof BlockHitResult hit)
                    || hit.getBlockPos().getY() >= Math.floor(player.getY())) {
                SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                        SkyOPLanguage.text("diagnostics.reason.no_landing"));
                return;
            }

            // Do not send the bucket action if the predicted surface and current aim diverge.
            if (Math.abs(hit.getBlockPos().getX() - predictedLanding.getBlockPos().getX()) > 1
                    || Math.abs(hit.getBlockPos().getZ() - predictedLanding.getBlockPos().getZ()) > 1) {
                SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                        SkyOPLanguage.text("diagnostics.reason.aim_mismatch"));
                return;
            }

            autoMlgOriginalSlot = inventory.getSelectedSlot();
            autoMlgBucketSlot = bucketSlot;
            inventory.setSelectedSlot(bucketSlot);

            // Use only vanilla client interaction paths. Server-side validation remains authoritative.
            client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
            player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
            client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
            autoMlgCooldown = 5;
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.mlg_attempt"));
        } finally {
            player.setXRot(originalPitch);
        }
    }

    /**
     * Predicts the first solid surface intersected by the player's falling trajectory.
     * Uses local air-motion integration and clips only against chunks already loaded by the client.
     */
    static BlockHitResult predictLandingSurface(Minecraft client, LocalPlayer player) {
        if (client.level == null || player == null) return null;

        Vec3 position = player.position();
        Vec3 velocity = player.getDeltaMovement();
        for (int tick = 0; tick < 80; tick++) {
            SkyOPAutomationRules.FallMotion next = SkyOPAutomationRules.advanceFall(
                    position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            Vec3 nextPosition = new Vec3(next.x(), next.y(), next.z());

            if (next.velocityY() < 0.0D && nextPosition.y < position.y) {
                BlockPos sample = BlockPos.containing(nextPosition.x, nextPosition.y - 0.05D, nextPosition.z);
                // Never force-load a chunk just to predict a landing point.
                if (!client.level.hasChunkAt(sample)) return null;

                Vec3 from = position.add(0.0D, 0.05D, 0.0D);
                Vec3 to = nextPosition.add(0.0D, -0.05D, 0.0D);
                if (from.distanceToSqr(to) > 1.0E-8D) {
                    HitResult collision = client.level.clip(new ClipContext(
                            from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                    if (collision.getType() == HitResult.Type.BLOCK
                            && collision instanceof BlockHitResult blockHit
                            && blockHit.getBlockPos().getY() < Math.floor(player.getY())) {
                        return blockHit;
                    }
                }
            }

            position = nextPosition;
            velocity = new Vec3(next.velocityX(), next.velocityY(), next.velocityZ());
        }
        return null;
    }

    private static void restoreAutoMlgSlot(Minecraft client) {
        if (client.player != null && autoMlgOriginalSlot >= 0
                && client.player.getInventory().getSelectedSlot() == autoMlgBucketSlot) {
            client.player.getInventory().setSelectedSlot(autoMlgOriginalSlot);
        }
        autoMlgOriginalSlot = -1;
        autoMlgBucketSlot = -1;
        autoMlgCooldown = 0;
    }

    static void tickInfiniteJump(Minecraft client) {
        Player player = client.player;
        boolean jumpDown = client.options.keyJump.isDown();
        if (jumpDown && !jumpKeyWasDown && !player.onGround()
                && player.isAlive() && !player.isPassenger()) {
            double boost = 0.42 * SkyOPMath.clamp(SkyOPState.jumpBoostMultiplier, 1.0, 3.0);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, Math.max(motion.y, boost), motion.z);
            player.fallDistance = 0.0F;
        }
        jumpKeyWasDown = jumpDown;
    }

static void restoreFlight(Minecraft client) {
        Player target = flightPlayer != null ? flightPlayer : client.player;
        if (target != null && originalNoGravity != null) {
            target.setNoGravity(originalNoGravity);
        }
        originalNoGravity = null;
        flightPlayer = null;
    }

    static void trackDisabledJumpInputs(Minecraft client) {
        if (client == null || client.options == null) return;
        if (!SkyOPCore.isEnabled(SkyOPModuleId.JUMP_BOOST)) jumpBoostKeyWasDown = client.options.keyJump.isDown();
        if (!SkyOPCore.isEnabled(SkyOPModuleId.INFINITE_JUMP)) jumpKeyWasDown = client.options.keyJump.isDown();
    }

    static void cleanup(Minecraft client) {
        restoreFlight(client);
        autoJumpCooldown = 0;
        stepAssistCooldown = 0;
        bridgeCooldown = 0;
        restoreAutoMlgSlot(client);
        jumpKeyWasDown = false;
        jumpBoostKeyWasDown = false;
    }

    public static final class AutoSprint extends SkyOPModule {
        public AutoSprint(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_SPRINT, settings); }
        @Override public void onTick(Minecraft client) { tickAutoSprint(client); }
        
    }

    public static final class Speed extends SkyOPModule {
        public Speed(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.SPEED, settings); }
        @Override public void onTick(Minecraft client) { tickSpeed(client); }
        
    }

    public static final class Flight extends SkyOPModule {
        public Flight(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.FLIGHT, settings); }
        @Override public void onTick(Minecraft client) { tickFlight(client); }
        @Override public void onDisable(Minecraft client) { restoreFlight(client); }
    }

    public static final class AutoJump extends SkyOPModule {
        public AutoJump(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_JUMP, settings); }
        @Override public void onTick(Minecraft client) { tickAutoJump(client); }
        @Override public void onDisable(Minecraft client) { autoJumpCooldown = 0; }
    }

    public static final class StepAssist extends SkyOPModule {
        public StepAssist(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.STEP_ASSIST, settings); }
        @Override public void onTick(Minecraft client) { tickStepAssist(client); }
        @Override public void onDisable(Minecraft client) { stepAssistCooldown = 0; }
    }

    public static final class Spider extends SkyOPModule {
        public Spider(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.SPIDER, settings); }
        @Override public void onTick(Minecraft client) { tickSpider(client); }
        
    }

    public static final class AutoBridge extends SkyOPModule {
        public AutoBridge(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_BRIDGE, settings); }
        @Override public void onTick(Minecraft client) { tickAutoBridge(client); }
        @Override public void onDisable(Minecraft client) { bridgeCooldown = 0; }
    }

    public static final class AutoMlg extends SkyOPModule {
        public AutoMlg(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_MLG, settings); }
        @Override public void onTick(Minecraft client) { tickAutoMlg(client); }
        @Override public void onDisable(Minecraft client) { restoreAutoMlgSlot(client); }
    }

    public static final class JumpBoost extends SkyOPModule {
        public JumpBoost(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.JUMP_BOOST, settings); }
        @Override public void onTick(Minecraft client) { tickJumpBoost(client); }
        @Override public void onDisable(Minecraft client) { jumpBoostKeyWasDown = client != null && client.options.keyJump.isDown(); }
    }

    public static final class InfiniteJump extends SkyOPModule {
        public InfiniteJump(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.INFINITE_JUMP, settings); }
        @Override public void onTick(Minecraft client) { tickInfiniteJump(client); }
        @Override public void onDisable(Minecraft client) { jumpKeyWasDown = client != null && client.options.keyJump.isDown(); }
    }

    public static final class NoFall extends SkyOPModule {
        public NoFall(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.NO_FALL, settings); }
        @Override public void onTick(Minecraft client) { tickNoFall(client); }
        
    }
}
