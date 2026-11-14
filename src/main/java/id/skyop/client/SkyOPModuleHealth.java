package id.skyop.client;

public final class SkyOPModuleHealth {
    public record Snapshot(int errorCount, int consecutiveErrors, boolean autoDisabled, String lastError) { }

    private int errorCount;
    private int consecutiveErrors;
    private int consecutiveSuccesses;
    private boolean autoDisabled;
    private String lastError = "";

    public synchronized boolean recordError(String message) {
        errorCount++;
        consecutiveErrors++;
        consecutiveSuccesses = 0;
        lastError = message == null ? "Unknown error" : message;
        return consecutiveErrors >= 3;
    }

    public synchronized void recordSuccess() {
        if (consecutiveErrors == 0 || autoDisabled) return;
        consecutiveSuccesses++;
        if (consecutiveSuccesses >= 100) {
            consecutiveErrors = 0;
            consecutiveSuccesses = 0;
        }
    }

    public synchronized void markAutoDisabled() { autoDisabled = true; }

    public synchronized void retry() {
        consecutiveErrors = 0;
        consecutiveSuccesses = 0;
        autoDisabled = false;
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(errorCount, consecutiveErrors, autoDisabled, lastError);
    }
}
