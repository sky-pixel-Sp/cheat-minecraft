package id.skyop.client;

import java.util.HashMap;
import java.util.Map;

public final class SkyOPModuleHealth {
    public record Snapshot(int errorCount, int consecutiveErrors,
                           boolean autoDisabled, String lastError, String lastFailingCallback) { }

    private final Map<String, Integer> failuresByCallback = new HashMap<>();
    private int errorCount;
    private boolean autoDisabled;
    private String lastError = "";
    private String lastFailingCallback = "";

    /** The third consecutive failure of the same callback phase requests module auto-disable. */
    public synchronized boolean recordError(String callback, String message) {
        String key = normalize(callback);
        int failures = failuresByCallback.merge(key, 1, Integer::sum);
        errorCount++;
        lastFailingCallback = key;
        lastError = message == null ? "Unknown error" : message;
        return failures >= 3;
    }

    /** A successful callback resets only its own consecutive failure streak. */
    public synchronized void recordSuccess(String callback) {
        if (!autoDisabled) failuresByCallback.remove(normalize(callback));
    }

    public synchronized void markAutoDisabled() { autoDisabled = true; }

    /** Manual retry starts a new session and preserves the historical error count. */
    public synchronized void retry() {
        failuresByCallback.clear();
        autoDisabled = false;
    }

    public synchronized Snapshot snapshot() {
        int maximumStreak = failuresByCallback.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0);
        return new Snapshot(errorCount, maximumStreak, autoDisabled, lastError, lastFailingCallback);
    }

    private static String normalize(String callback) {
        return callback == null || callback.isBlank() ? "unknown" : callback;
    }
}
