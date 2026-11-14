package id.skyop.client;

import id.skyop.common.SkyOPDiagnostics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPDiagnosticsTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void reasonLoggingIsThrottledToOneWritePerModuleEveryTwoSeconds() {
        assertTrue(SkyOPDiagnostics.shouldWriteReasonLog(Long.MIN_VALUE, 1_000L));
        assertFalse(SkyOPDiagnostics.shouldWriteReasonLog(1_000L, 2_999L));
        assertTrue(SkyOPDiagnostics.shouldWriteReasonLog(1_000L, 3_000L));
        assertFalse(SkyOPDiagnostics.shouldWriteReasonLog(3_000L, 2_999L));
    }

    @Test
    void errorsAreCountedAndOneHundredSuccessfulCallbacksResetTheConsecutiveCounter() {
        SkyOPDiagnostics diagnostics = new SkyOPDiagnostics(temporaryDirectory.resolve("skyop.log"));
        assertEquals(1, diagnostics.recordError("AUTO_TOTEM", new IllegalStateException("first")));
        assertEquals(2, diagnostics.recordError("AUTO_TOTEM", new IllegalStateException("second")));
        for (int i = 0; i < 100; i++) diagnostics.recordSuccess("AUTO_TOTEM");
        SkyOPDiagnostics.ModuleSnapshot snapshot = diagnostics.snapshot("AUTO_TOTEM");
        assertEquals(2, snapshot.errorCount());
        assertEquals(0, snapshot.consecutiveErrors());
        assertEquals(100, snapshot.successfulCallbacks());
    }

    @Test
    void reasonsAreVisibleImmediatelyEvenWhenDiskLoggingIsThrottled() {
        SkyOPDiagnostics diagnostics = new SkyOPDiagnostics(temporaryDirectory.resolve("skyop.log"));
        diagnostics.recordReason("AUTO_MLG", "cooldown", "Waiting for cooldown.");
        diagnostics.recordReason("AUTO_MLG", "no_bucket", "No water bucket in hotbar.");
        SkyOPDiagnostics.ModuleSnapshot snapshot = diagnostics.snapshot("AUTO_MLG");
        assertEquals("no_bucket", snapshot.lastReasonKey());
        assertEquals("No water bucket in hotbar.", snapshot.lastReasonText());
        assertEquals(1, diagnostics.recentLines().size());
    }

    @Test
    void logRotatesAtOneMebibyteAndKeepsOneBackup() throws Exception {
        Path log = temporaryDirectory.resolve("skyop.log");
        Files.writeString(log, "x".repeat((int) SkyOPDiagnostics.MAX_LOG_BYTES));
        SkyOPDiagnostics diagnostics = new SkyOPDiagnostics(log);
        diagnostics.recordEvent("SERVER", "Server started.");
        assertTrue(Files.exists(temporaryDirectory.resolve("skyop.log.1")));
        assertEquals(SkyOPDiagnostics.MAX_LOG_BYTES, Files.size(temporaryDirectory.resolve("skyop.log.1")));
        assertTrue(Files.size(log) > 0 && Files.size(log) < 1_024);
    }
}
