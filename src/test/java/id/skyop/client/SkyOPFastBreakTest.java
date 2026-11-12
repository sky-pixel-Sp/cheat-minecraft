package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkyOPFastBreakTest {
    @Test
    void hasteFiftyMatchesElevenTimesVanillaMiningSpeed() {
        assertEquals(11.0F, SkyOPFastBreak.multiplierForLevel(50), 0.0001F);
    }

    @Test
    void hasteLevelsFollowVanillaTwentyPercentPerLevelFormula() {
        assertEquals(1.2F, SkyOPFastBreak.multiplierForLevel(1), 0.0001F);
        assertEquals(1.8F, SkyOPFastBreak.multiplierForLevel(4), 0.0001F);
    }

    @Test
    void hasteLevelIsClampedToTheSupportedRange() {
        assertEquals(1.2F, SkyOPFastBreak.multiplierForLevel(0), 0.0001F);
        assertEquals(11.0F, SkyOPFastBreak.multiplierForLevel(99), 0.0001F);
        assertEquals(2.4F, SkyOPFastBreak.multiplierForLevel(Double.NaN), 0.0001F);
    }
}
