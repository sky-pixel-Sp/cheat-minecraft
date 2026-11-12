package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Concrete movement modules. Each module owns its tick handler and transient state. */
public final class SkyOPMovementModules {
    private SkyOPMovementModules() { }

    private static int autoJumpCooldown;
    private static int stepAssistCooldown;
    private static int bridgeCooldown;
    private enum AutoMlgPhase { IDLE, AIM_PENDING, CONFIRM_PENDING }
    private static int autoMlgCooldown;
    private static int autoMlgOriginalSlot = -1;
    private static int autoMlgBucketSlot = -1;
    private static AutoMlgPhase autoMlgPhase = AutoMlgPhase.IDLE;
    private static LocalPlayer autoMlgPendingPlayer;
    private static BlockPos autoMlgPendingBlock;
    private static float autoMlgOriginalYaw;
    private static float autoMlgOriginalPitch;
    private static float autoMlgOriginalHeadYaw;
    private static float autoMlgTargetYaw;
    private static float autoMlgTargetPitch;
    private static int autoMlgPhaseTicks;
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

        if (autoMlgPhase == AutoMlgPhase.AIM_PENDING) {
            tickAutoMlgAimPending(client);
            return;
        }
        if (autoMlgPhase == AutoMlgPhase.CONFIRM_PENDING) {
            tickAutoMlgConfirmPending(client);
            return;
        }
        if (autoMlgCooldown > 0) {
            autoMlgCooldown--;
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.mlg_cooldown"));
            return;
        }

        LocalPlayer player = client.player;
        if (player.getAbilities().instabuild || player.isSpectator()
                || player.isPassenger() || player.isFallFlying()
                || player.isInWaterOrBubble() || player.isInLava() || player.isNoGravity()) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.creative_or_unsupported"));
            return;
        }
        if (!SkyOPAutomationRules.shouldTriggerMlg(
                player.onGround(), player.fallDistance, player.getDeltaMovement().y,
                SkyOPState.autoMlgFallDistance)) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.not_falling"));
            return;
        }
        if (player.containerMenu != player.inventoryMenu) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.container_open"));
            return;
        }

        var inventory = player.getInventory();
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

        BlockHitResult landing = predictLandingSurface(client, player);
        if (landing == null) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.no_landing"));
            return;
        }
        double reachSquared = player.getEyePosition().distanceToSqr(landing.getLocation());
        if (reachSquared > 4.5D * 4.5D) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.out_of_reach")
                            + " (" + String.format(java.util.Locale.ROOT, "%.2f", Math.sqrt(reachSquared)) + " blocks)");
            return;
        }

        beginAutoMlgAim(client, player, landing, bucketSlot);
    }

    private static void beginAutoMlgAim(Minecraft client, LocalPlayer player,
                                        BlockHitResult landing, int bucketSlot) {
        Vec3 direction = landing.getLocation().subtract(player.getEyePosition());
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        autoMlgTargetYaw = horizontal < 1.0E-5D
                ? player.getYRot()
                : (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0D);
        autoMlgTargetPitch = (float) -Math.toDegrees(Math.atan2(direction.y, horizontal));

        autoMlgOriginalSlot = player.getInventory().getSelectedSlot();
        autoMlgBucketSlot = bucketSlot;
        autoMlgOriginalYaw = player.getYRot();
        autoMlgOriginalPitch = player.getXRot();
        autoMlgOriginalHeadYaw = player.getYHeadRot();
        autoMlgPendingPlayer = player;
        autoMlgPendingBlock = landing.getBlockPos();
        autoMlgPhase = AutoMlgPhase.AIM_PENDING;
        // One complete client tick lets the ordinary movement update carry this view
        // before the single normal use-item interaction is submitted.
        autoMlgPhaseTicks = 1;

        player.getInventory().setSelectedSlot(bucketSlot);
        player.setYRot(autoMlgTargetYaw);
        player.setXRot(autoMlgTargetPitch);
        player.setYHeadRot(autoMlgTargetYaw);
        SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                SkyOPLanguage.text("diagnostics.reason.aim_sync_pending"));
    }

    private static void tickAutoMlgAimPending(Minecraft client) {
        LocalPlayer player = client.player;
        if (player != autoMlgPendingPlayer || !player.isAlive()
                || client.level == null || client.gameMode == null) {
            abortAutoMlg(client, "diagnostics.reason.action_cancelled", 0);
            return;
        }
        if (player.containerMenu != player.inventoryMenu
                || player.getInventory().getSelectedSlot() != autoMlgBucketSlot) {
            abortAutoMlg(client, "diagnostics.reason.action_cancelled", 0);
            return;
        }
        // Do not fight the player's own input while aim synchronization is pending.
        if (Math.abs(angleDifference(player.getYRot(), autoMlgTargetYaw)) > 1.5F
                || Math.abs(player.getXRot() - autoMlgTargetPitch) > 1.5F) {
            abortAutoMlg(client, "diagnostics.reason.aim_cancelled", 0);
            return;
        }
        if (autoMlgPhaseTicks > 0) {
            autoMlgPhaseTicks--;
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.aim_sync_pending"));
            return;
        }

        HitResult result = player.pick(4.5D, 1.0F, false);
        if (!(result instanceof BlockHitResult hit)
                || result.getType() != HitResult.Type.BLOCK
                || autoMlgPendingBlock == null
                || !hit.getBlockPos().equals(autoMlgPendingBlock)
                || player.getEyePosition().distanceToSqr(hit.getLocation()) > 4.5D * 4.5D
                || !player.getInventory().getItem(autoMlgBucketSlot)
                        .is(net.minecraft.world.item.Items.WATER_BUCKET)) {
            abortAutoMlg(client, "diagnostics.reason.aim_mismatch", 8);
            return;
        }

        // Exactly one normal vanilla item-use request. No packet construction or
        // synthetic reach is used; the remote server remains authoritative.
        client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        restoreAutoMlgView(player);
        autoMlgPhase = AutoMlgPhase.CONFIRM_PENDING;
        autoMlgPhaseTicks = 6;
        SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                SkyOPLanguage.text("diagnostics.reason.mlg_interaction_sent"));
    }

    private static void tickAutoMlgConfirmPending(Minecraft client) {
        LocalPlayer player = client.player;
        if (player != autoMlgPendingPlayer) {
            clearAutoMlgState();
            return;
        }
        if (player.getInventory().getSelectedSlot() != autoMlgBucketSlot) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.action_cancelled"));
            clearAutoMlgState();
            autoMlgCooldown = 8;
            return;
        }

        if (player.getInventory().getItem(autoMlgBucketSlot).is(net.minecraft.world.item.Items.BUCKET)) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.mlg_confirmed"));
            restoreAutoMlgSlot(client);
            autoMlgCooldown = 15;
            return;
        }

        if (--autoMlgPhaseTicks <= 0) {
            SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG,
                    SkyOPLanguage.text("diagnostics.reason.mlg_not_confirmed"));
            restoreAutoMlgSlot(client);
            autoMlgCooldown = 15;
        }
    }

    /**
     * Predicts the first solid surface crossed by a simplified local falling trajectory.
     * It only clips through chunks that are already loaded; no chunk is requested or forced.
     */
    static BlockHitResult predictLandingSurface(Minecraft client, LocalPlayer player) {
        if (client == null || client.level == null || player == null) return null;
        Vec3 position = player.position();
        Vec3 velocity = player.getDeltaMovement();

        for (int tick = 0; tick < 80; tick++) {
            SkyOPAutomationRules.FallMotion next = SkyOPAutomationRules.advanceFall(
                    position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            Vec3 nextPosition = new Vec3(next.x(), next.y(), next.z());

            if (next.velocityY() < 0.0D && nextPosition.y < position.y) {
                Vec3 from = position.add(0.0D, 0.05D, 0.0D);
                Vec3 to = nextPosition.add(0.0D, -0.05D, 0.0D);
                if (!isLoadedSegment(client, from, to)) return null;
                if (from.distanceToSqr(to) > 1.0E-8D) {
                    HitResult collision = client.level.clip(new ClipContext(
                            from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                    if (collision instanceof BlockHitResult blockHit
                            && collision.getType() == HitResult.Type.BLOCK
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

    private static boolean isLoadedSegment(Minecraft client, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double length = Math.sqrt(delta.lengthSqr());
        int samples = Math.max(1, (int) Math.ceil(length * 2.0D));
        for (int index = 0; index <= samples; index++) {
            double fraction = index / (double) samples;
            Vec3 sample = from.add(delta.scale(fraction));
            if (!client.level.hasChunkAt(BlockPos.containing(sample.x, sample.y, sample.z))) return false;
        }
        return true;
    }

    private static float angleDifference(float first, float second) {
        float difference = (first - second) % 360.0F;
        if (difference >= 180.0F) difference -= 360.0F;
        if (difference < -180.0F) difference += 360.0F;
        return difference;
    }

    private static void abortAutoMlg(Minecraft client, String reasonKey, int cooldown) {
        SkyOPDiagnostics.recordReason(SkyOPModuleId.AUTO_MLG, SkyOPLanguage.text(reasonKey));
        restoreAutoMlgSlot(client);
        autoMlgCooldown = Math.max(0, cooldown);
    }

    private static void restoreAutoMlgView(LocalPlayer player) {
        if (player == null || player != autoMlgPendingPlayer) return;
        player.setYRot(autoMlgOriginalYaw);
        player.setXRot(autoMlgOriginalPitch);
        player.setYHeadRot(autoMlgOriginalHeadYaw);
    }

    private static void restoreAutoMlgSlot(Minecraft client) {
        LocalPlayer player = client == null ? null : client.player;
        if (player != null && player == autoMlgPendingPlayer) {
            if (autoMlgPhase == AutoMlgPhase.AIM_PENDING) restoreAutoMlgView(player);
            if (autoMlgOriginalSlot >= 0
                    && player.getInventory().getSelectedSlot() == autoMlgBucketSlot) {
                player.getInventory().setSelectedSlot(autoMlgOriginalSlot);
            }
        }
        clearAutoMlgState();
        autoMlgCooldown = 0;
    }

    private static void clearAutoMlgState() {
        autoMlgOriginalSlot = -1;
        autoMlgBucketSlot = -1;
        autoMlgPhase = AutoMlgPhase.IDLE;
        autoMlgPendingPlayer = null;
        autoMlgPendingBlock = null;
        autoMlgPhaseTicks = 0;
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

    private static Boolean autoWalkOriginalKeyState;

    static void restoreAutoWalk(Minecraft client) {
        if (client != null && client.options != null && autoWalkOriginalKeyState != null) {
            client.options.keyUp.setDown(autoWalkOriginalKeyState);
        }
        autoWalkOriginalKeyState = null;
    }

    static void tickAutoWalk(Minecraft client) {
        if (client.player == null || !client.player.isAlive() || client.player.isPassenger()
                || client.player.isSpectator() || client.gui.screen() != null) {
            restoreAutoWalk(client);
            return;
        }
        if (autoWalkOriginalKeyState == null) {
            autoWalkOriginalKeyState = client.options.keyUp.isDown();
        }
        client.options.keyUp.setDown(true);
    }

    static void cleanup(Minecraft client) {
        restoreFlight(client);
        restoreAutoWalk(client);
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

    public static final class AutoWalk extends SkyOPModule {
        public AutoWalk(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_WALK, settings); }
        @Override public void onTick(Minecraft client) { tickAutoWalk(client); }
        @Override public void onDisable(Minecraft client) { restoreAutoWalk(client); }
        @Override public void onWorldChange(Minecraft client) { restoreAutoWalk(client); }
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
