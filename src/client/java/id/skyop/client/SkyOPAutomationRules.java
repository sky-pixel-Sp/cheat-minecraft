package id.skyop.client;

/** Pure decision rules shared by automation modules and their unit tests. */
public final class SkyOPAutomationRules {
    private SkyOPAutomationRules() { }

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
