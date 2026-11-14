package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SkyOPSettingsSchemaTest {
    @Test
    void validatesFullCircleAuraFieldOfView() {
        var rule = SkyOPSettingsSchema.rule("auraFov");
        assertNotNull(rule);
        assertEquals(1.0, rule.validate(0));
        assertEquals(360.0, rule.validate(720));
        assertEquals(360.0, rule.validate(Double.NaN));
        assertEquals(360.0, rule.defaultValue());
    }

    @Test
    void validatesAutoHuntRange() {
        var rule = SkyOPSettingsSchema.rule("autoHuntRange");
        assertNotNull(rule);
        assertEquals(2.5, rule.validate(0.1));
        assertEquals(6.0, rule.validate(12.0));
        assertEquals(4.5, rule.validate(Double.NaN));
    }

    @Test
    void validatesJumpBoostMultiplier() {
        var rule = SkyOPSettingsSchema.rule("jumpBoostMultiplier");
        assertNotNull(rule);
        assertEquals(1.0, rule.validate(0.0));
        assertEquals(3.0, rule.validate(9.0));
        assertEquals(1.35, rule.validate(Double.POSITIVE_INFINITY));
    }

    @Test
    void validatesBridgeDelayAsAnInteger() {
        var rule = SkyOPSettingsSchema.rule("bridgeDelayTicks");
        assertNotNull(rule);
        assertEquals(1.0, rule.validate(0.0));
        assertEquals(10.0, rule.validate(99.0));
        assertEquals(4.0, rule.validate(3.6));
    }

    @Test
    void validatesAutoMlgPredictionWindow() {
        var rule = SkyOPSettingsSchema.rule("autoMlgPredictionWindow");
        assertNotNull(rule);
        assertEquals(1.0, rule.validate(0.0));
        assertEquals(3.0, rule.validate(4.0));
        assertEquals(2.0, rule.validate(Double.NaN));
        assertEquals(2.0, rule.defaultValue());
    }

    @Test
    void validatesNewUtilityModuleSettings() {
        var zoom = SkyOPSettingsSchema.rule("zoomFov");
        var containers = SkyOPSettingsSchema.rule("containerFinderRange");
        var health = SkyOPSettingsSchema.rule("lowHealthAlertThreshold");
        var armor = SkyOPSettingsSchema.rule("armorAlertPercent");

        assertNotNull(zoom);
        assertNotNull(containers);
        assertNotNull(health);
        assertNotNull(armor);

        assertEquals(10.0, zoom.validate(2));
        assertEquals(70.0, zoom.validate(90));
        assertEquals(20.0, zoom.validate(Double.NaN));

        assertEquals(4.0, containers.validate(1));
        assertEquals(24.0, containers.validate(100));
        assertEquals(16.0, containers.validate(Double.POSITIVE_INFINITY));

        assertEquals(1.0, health.validate(0));
        assertEquals(20.0, health.validate(25));
        assertEquals(8.0, health.validate(Double.NaN));

        assertEquals(1.0, armor.validate(0));
        assertEquals(50.0, armor.validate(80));
        assertEquals(15.0, armor.validate(Double.NaN));
    }
}
