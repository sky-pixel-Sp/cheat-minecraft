package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyOPModuleRiskTest {
    @Test
    void highImpactAutomationIsMarkedDangerous() {
        assertTrue(SkyOPModuleId.KILL_AURA.isDangerous());
        assertTrue(SkyOPModuleId.TRIGGER_BOT.isDangerous());
        assertTrue(SkyOPModuleId.FLIGHT.isDangerous());
        assertTrue(SkyOPModuleId.AUTO_TOTEM.isDangerous());
    }

    @Test
    void informationalModulesAreMarkedSafe() {
        assertFalse(SkyOPModuleId.WORLD_INFO_HUD.isDangerous());
        assertFalse(SkyOPModuleId.CHUNK_INFO_HUD.isDangerous());
        assertFalse(SkyOPModuleId.PERFORMANCE_HUD.isDangerous());
        assertFalse(SkyOPModuleId.SESSION_TIMER_HUD.isDangerous());
        assertFalse(SkyOPModuleId.FOOD_HUD.isDangerous());
        assertFalse(SkyOPModuleId.AIR_HUD.isDangerous());
        assertFalse(SkyOPModuleId.EXPERIENCE_HUD.isDangerous());
        assertFalse(SkyOPModuleId.ITEM_INFO_HUD.isDangerous());
        assertFalse(SkyOPModuleId.OFFHAND_HUD.isDangerous());
        assertFalse(SkyOPModuleId.BLOCK_INFO_HUD.isDangerous());
        assertFalse(SkyOPModuleId.ARMOR_DETAIL_HUD.isDangerous());
    }

    @Test
    void riskFiltersAreNavigationFiltersRatherThanGameplayCategories() {
        assertTrue(SkyOPCategory.SAFE.isRiskFilter());
        assertTrue(SkyOPCategory.DANGEROUS.isRiskFilter());
        assertFalse(SkyOPCategory.WORLD.isRiskFilter());
    }
}
