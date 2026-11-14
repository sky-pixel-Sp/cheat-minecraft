package id.skyop.client;

public final class SkyOPDiagnosticRules {
    private SkyOPDiagnosticRules() { }

    public static boolean shouldWriteReasonLog(long lastMillis, long nowMillis) {
        return lastMillis <= 0L || nowMillis < lastMillis || nowMillis - lastMillis >= 2_000L;
    }
}
