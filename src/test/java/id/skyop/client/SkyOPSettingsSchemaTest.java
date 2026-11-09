package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SkyOPSettingsSchemaTest {
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
}
