package id.skyop.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkyOPDiagnosticRulesTest {
    @Test void reasonLogIsLimitedToOneEntryPerTwoSeconds() {
        assertTrue(SkyOPDiagnosticRules.shouldWriteReasonLog(0, 1000));
        assertFalse(SkyOPDiagnosticRules.shouldWriteReasonLog(1000, 2999));
        assertTrue(SkyOPDiagnosticRules.shouldWriteReasonLog(1000, 3000));
    }
    @Test void clockRollbackDoesNotSuppressLogging() {
        assertTrue(SkyOPDiagnosticRules.shouldWriteReasonLog(5000, 4000));
    }
}
