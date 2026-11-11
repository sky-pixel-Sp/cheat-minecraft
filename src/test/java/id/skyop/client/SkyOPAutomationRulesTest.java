package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPAutomationRulesTest {
    @Test
    void autoTotemOnlyRequestsSwapWhenOffhandIsEmptyAndHealthIsLow() {
        assertTrue(SkyOPAutomationRules.shouldEquipTotem(false, 8.0F, 14));
        assertFalse(SkyOPAutomationRules.shouldEquipTotem(true, 2.0F, 14));
        assertFalse(SkyOPAutomationRules.shouldEquipTotem(false, 18.0F, 14));
    }

    @Test
    void autoTotemThresholdIsClampedToValidHealthRange() {
        assertTrue(SkyOPAutomationRules.shouldEquipTotem(false, 1.0F, 99));
        assertFalse(SkyOPAutomationRules.shouldEquipTotem(false, 2.0F, 0));
    }

    @Test
    void autoMlgRequiresFallingAndAConfiguredFallDistance() {
        assertTrue(SkyOPAutomationRules.shouldTriggerMlg(false, 7.0F, -0.8, 6));
        assertFalse(SkyOPAutomationRules.shouldTriggerMlg(true, 7.0F, -0.8, 6));
        assertFalse(SkyOPAutomationRules.shouldTriggerMlg(false, 5.9F, -0.8, 6));
        assertFalse(SkyOPAutomationRules.shouldTriggerMlg(false, 7.0F, -0.2, 6));
    }

    @Test
    void autoMlgThresholdCannotBeSetBelowThreeBlocksOrAboveEighteen() {
        assertFalse(SkyOPAutomationRules.shouldTriggerMlg(false, 2.9F, -0.8, 0));
        assertTrue(SkyOPAutomationRules.shouldTriggerMlg(false, 18.0F, -0.8, 99));
    }
}
