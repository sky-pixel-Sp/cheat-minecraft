package id.skyop.client;

/** Pure decision rules shared by automation modules and their unit tests. */
public final class SkyOPAutomationRules {
    private SkyOPAutomationRules() { }

    public static boolean shouldEquipTotem(boolean offhandAlreadyHasTotem,
                                           float health, double healthThreshold) {
        double threshold = SkyOPSettingsSchema.rule("autoTotemHealth").validate(healthThreshold);
        return !offhandAlreadyHasTotem && Float.isFinite(health) && health <= threshold;
    }

    public static boolean shouldTriggerMlg(boolean onGround, float fallDistance,
                                           double verticalVelocity, double triggerDistance) {
        double threshold = SkyOPSettingsSchema.rule("autoMlgFallDistance").validate(triggerDistance);
        return !onGround && Float.isFinite(fallDistance) && fallDistance >= threshold
                && Double.isFinite(verticalVelocity) && verticalVelocity < -0.55D;
    }
}
