package id.skyop.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkyOPModuleHealthTest {
    @Test void thirdConsecutiveErrorRequestsAutoDisable() {
        SkyOPModuleHealth h = new SkyOPModuleHealth();
        assertFalse(h.recordError("one"));
        assertFalse(h.recordError("two"));
        assertTrue(h.recordError("three"));
        assertEquals(3, h.snapshot().errorCount());
    }
    @Test void oneHundredSuccessesClearTheConsecutiveStreak() {
        SkyOPModuleHealth h = new SkyOPModuleHealth();
        h.recordError("temporary");
        for (int i = 0; i < 100; i++) h.recordSuccess();
        assertEquals(0, h.snapshot().consecutiveErrors());
        assertEquals(1, h.snapshot().errorCount());
    }
    @Test void manualRetryPreservesTheHistoricalErrorCount() {
        SkyOPModuleHealth h = new SkyOPModuleHealth();
        h.recordError("one"); h.recordError("two"); h.recordError("three");
        h.markAutoDisabled(); h.retry();
        assertFalse(h.snapshot().autoDisabled());
        assertEquals(3, h.snapshot().errorCount());
    }
}
