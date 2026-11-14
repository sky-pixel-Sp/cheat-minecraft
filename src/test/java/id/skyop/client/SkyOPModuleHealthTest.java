package id.skyop.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkyOPModuleHealthTest {
    @Test void thirdFailureInSameCallbackRequestsAutoDisable() {
        var health = new SkyOPModuleHealth();
        assertFalse(health.recordError("onTick", "one"));
        assertFalse(health.recordError("onTick", "two"));
        assertTrue(health.recordError("onTick", "three"));
        assertEquals(3, health.snapshot().errorCount());
        assertEquals(3, health.snapshot().consecutiveErrors());
        assertEquals("onTick", health.snapshot().lastFailingCallback());
    }

    @Test void successInAnotherCallbackCannotMaskRepeatedTickFailure() {
        var health = new SkyOPModuleHealth();
        assertFalse(health.recordError("onTick", "one"));
        health.recordSuccess("onRender2D");
        assertFalse(health.recordError("onTick", "two"));
        health.recordSuccess("isReady");
        assertTrue(health.recordError("onTick", "three"));
    }

    @Test void successInSameCallbackResetsOnlyItsOwnStreak() {
        var health = new SkyOPModuleHealth();
        health.recordError("onTick", "one");
        health.recordError("onTick", "two");
        health.recordSuccess("onTick");
        assertFalse(health.recordError("onTick", "after recovery"));
        assertEquals(3, health.snapshot().errorCount());
        assertEquals(1, health.snapshot().consecutiveErrors());
    }

    @Test void manualRetryPreservesHistoryAndClearsAllPhaseStreaks() {
        var health = new SkyOPModuleHealth();
        health.recordError("onTick", "one");
        health.recordError("onTick", "two");
        health.recordError("onTick", "three");
        health.markAutoDisabled();
        health.retry();
        assertFalse(health.snapshot().autoDisabled());
        assertEquals(3, health.snapshot().errorCount());
        assertEquals(0, health.snapshot().consecutiveErrors());
    }

    @Test void unrelatedSuccessCannotReviveAnAutoDisabledModule() {
        var health = new SkyOPModuleHealth();
        health.recordError("onTick", "one");
        health.recordError("onTick", "two");
        health.recordError("onTick", "three");
        health.markAutoDisabled();
        health.recordSuccess("onRender2D");
        assertTrue(health.snapshot().autoDisabled());
        assertEquals(3, health.snapshot().consecutiveErrors());
    }
}
