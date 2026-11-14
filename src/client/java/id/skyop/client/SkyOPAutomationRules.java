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

    public static boolean shouldEquipTotem(boolean offhandOccupied,
                                           float health, double healthThreshold) {
        double threshold = SkyOPSettingsSchema.rule("autoTotemHealth").validate(healthThreshold);
        return !offhandOccupied && Float.isFinite(health) && health <= threshold;
    }

    /**
     * Keep the first selected slot captured for an MLG attempt session. A retry
     * while the bucket is still full must not replace it with the bucket slot.
     */
    public static int preserveOriginalSelectedSlot(int savedOriginalSlot, int currentSelectedSlot) {
        return savedOriginalSlot >= 0 ? savedOriginalSlot : currentSelectedSlot;
    }

    public static boolean shouldTriggerMlg(boolean onGround, double fallDistance,
                                           double verticalVelocity, double triggerDistance) {
        double threshold = SkyOPSettingsSchema.rule("autoMlgFallDistance").validate(triggerDistance);
        return !onGround && Double.isFinite(fallDistance) && fallDistance >= threshold
                && Double.isFinite(verticalVelocity) && verticalVelocity < -0.55D;
    }
}
