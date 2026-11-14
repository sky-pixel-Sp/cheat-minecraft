package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPAutomationRulesTest {
    @Test
    void playerInventorySlotsMapToVanillaMenuSlots() {
        assertEquals(36, SkyOPAutomationRules.playerInventorySlotToMenuSlot(0));
        assertEquals(37, SkyOPAutomationRules.playerInventorySlotToMenuSlot(1));
        assertEquals(44, SkyOPAutomationRules.playerInventorySlotToMenuSlot(8));
        assertEquals(9, SkyOPAutomationRules.playerInventorySlotToMenuSlot(9));
        assertEquals(35, SkyOPAutomationRules.playerInventorySlotToMenuSlot(35));
    }

    @Test
    void armorOffhandAndOutOfRangeInventorySlotsAreNotMappable() {
        for (int slot = 36; slot <= 40; slot++) {
            assertEquals(-1, SkyOPAutomationRules.playerInventorySlotToMenuSlot(slot));
        }
        assertEquals(-1, SkyOPAutomationRules.playerInventorySlotToMenuSlot(-1));
        assertEquals(-1, SkyOPAutomationRules.playerInventorySlotToMenuSlot(41));
    }

    @Test
    void singleplayerServerPlayerMustHaveTheLocalPlayersUuid() {
        var local = java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        var matching = java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        var different = java.util.UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        assertTrue(SkyOPAutomationRules.matchesSingleplayerPlayerUuid(local, matching));
        assertFalse(SkyOPAutomationRules.matchesSingleplayerPlayerUuid(local, different));
        assertFalse(SkyOPAutomationRules.matchesSingleplayerPlayerUuid(local, null));
        assertFalse(SkyOPAutomationRules.matchesSingleplayerPlayerUuid(null, matching));
    }

    @Test
    void autoTotemOnlyRequestsSwapWhenOffhandIsEmptyAndHealthIsLow() {
        assertTrue(SkyOPAutomationRules.shouldEquipTotem(false, 8.0F, 14));
        assertFalse(SkyOPAutomationRules.shouldEquipTotem(true, 2.0F, 14));
        assertFalse(SkyOPAutomationRules.shouldEquipTotem(false, 18.0F, 14));
    }

    @Test
    void autoTotemNeverReplacesAnExistingOffhandItem() {
        assertFalse(SkyOPAutomationRules.shouldEquipTotem(true, 1.0F, 20));
    }

    @Test
    void autoTotemThresholdIsClampedToValidHealthRange() {
        assertTrue(SkyOPAutomationRules.shouldEquipTotem(false, 1.0F, 99));
        assertFalse(SkyOPAutomationRules.shouldEquipTotem(false, 2.0F, 0));
    }

    @Test
    void repeatedMlgAttemptsKeepTheFirstOriginalSelectedSlot() {
        assertEquals(4, SkyOPAutomationRules.preserveOriginalSelectedSlot(-1, 4));
        assertEquals(4, SkyOPAutomationRules.preserveOriginalSelectedSlot(4, 1));
        assertEquals(4, SkyOPAutomationRules.preserveOriginalSelectedSlot(4, 7));
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
