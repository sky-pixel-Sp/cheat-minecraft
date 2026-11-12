package id.skyop.client;

import java.util.UUID;

/** Pure decision rules shared by automation modules and their unit tests. */
public final class SkyOPAutomationRules {
    private SkyOPAutomationRules() { }

    /**
     * Maps Player inventory indices to AbstractContainerMenu player-inventory slot ids.
     * Hotbar 0..8 is menu 36..44; main inventory 9..35 retains its index.
     * Armor 36..39 and offhand 40 are deliberately not mapped.
     */
    public static int playerInventorySlotToMenuSlot(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot <= 8) return inventorySlot + 36;
        if (inventorySlot >= 9 && inventorySlot <= 35) return inventorySlot;
        return -1;
    }

    public static boolean matchesSingleplayerPlayerUuid(UUID localPlayerUuid, UUID serverPlayerUuid) {
        return localPlayerUuid != null && serverPlayerUuid != null
                && localPlayerUuid.equals(serverPlayerUuid);
    }

    /** One simplified vanilla air-movement step used for local landing prediction. */
    public record FallMotion(double x, double y, double z,
                             double velocityX, double velocityY, double velocityZ) { }

    public static FallMotion advanceFall(double x, double y, double z,
                                         double velocityX, double velocityY, double velocityZ) {
        return new FallMotion(
                x + velocityX, y + velocityY, z + velocityZ,
                velocityX * 0.91D,
                (velocityY - 0.08D) * 0.98D,
                velocityZ * 0.91D
        );
    }

    public static boolean shouldWriteReasonLog(long lastLogMillis, long nowMillis) {
        return lastLogMillis <= 0L || nowMillis >= lastLogMillis + 2000L;
    }

    public static boolean shouldDisableAfterErrors(int errorCount) {
        return errorCount >= 3;
    }

    public static boolean shouldEquipTotem(boolean offhandOccupied,
                                           float health, double healthThreshold) {
        double threshold = SkyOPSettingsSchema.rule("autoTotemHealth").validate(healthThreshold);
        return !offhandOccupied && Float.isFinite(health) && health <= threshold;
    }

    public static boolean shouldTriggerMlg(boolean onGround, double fallDistance,
                                           double verticalVelocity, double triggerDistance) {
        double threshold = SkyOPSettingsSchema.rule("autoMlgFallDistance").validate(triggerDistance);
        return !onGround && Double.isFinite(fallDistance) && fallDistance >= threshold
                && Double.isFinite(verticalVelocity) && verticalVelocity < -0.55D;
    }
}
