package id.skyop.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local-only diagnostics shared by client and dedicated-server runtimes. */
public final class SkyOPDiagnostics {
    public static final long REASON_LOG_THROTTLE_MILLIS = 2_000L;
    public static final int ERROR_DISABLE_THRESHOLD = 3;
    public static final int SUCCESS_RESET_THRESHOLD = 100;
    public static final long MAX_LOG_BYTES = 1_048_576L;

    private final Path logFile;
    private final Map<String, MutableModuleState> modules = new LinkedHashMap<>();
    private final ArrayDeque<String> recentLines = new ArrayDeque<>(20);

    public SkyOPDiagnostics(Path logFile) {
        if (logFile == null) throw new IllegalArgumentException("logFile must not be null");
        this.logFile = logFile.toAbsolutePath().normalize();
    }

    public Path logFile() { return logFile; }

    public synchronized void recordReason(String module, String key, String text) {
        String safeModule = safeName(module);
        MutableModuleState state = state(safeModule);
        state.lastReasonKey = safeText(key, "unknown");
        state.lastReasonText = safeText(text, "No additional detail.");
        long now = System.currentTimeMillis();
        if (shouldWriteReasonLog(state.lastReasonLogMillis, now)) {
            state.lastReasonLogMillis = now;
            append("INFO", safeModule, state.lastReasonKey + ": " + state.lastReasonText);
        }
    }

    public synchronized int recordError(String module, Throwable throwable) {
        String safeModule = safeName(module);
        MutableModuleState state = state(safeModule);
        state.errorCount++;
        state.consecutiveErrors++;
        state.successfulCallbacks = 0;
        String type = throwable == null ? "Unknown error" : throwable.getClass().getName();
        String message = throwable == null || throwable.getMessage() == null
                ? "" : ": " + throwable.getMessage();
        state.lastError = type + message;
        append("ERROR", safeModule, state.lastError);
        if (throwable != null) {
            StringWriter buffer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(buffer));
            for (String line : buffer.toString().split("\\R")) {
                if (!line.isBlank()) append("ERROR", safeModule, line);
            }
        }
        return state.consecutiveErrors;
    }

    public synchronized void recordSuccess(String module) {
        MutableModuleState state = state(safeName(module));
        state.successfulCallbacks++;
        if (state.successfulCallbacks >= SUCCESS_RESET_THRESHOLD) {
            state.consecutiveErrors = 0;
            state.successfulCallbacks = SUCCESS_RESET_THRESHOLD;
        }
    }

    public synchronized void resetConsecutiveErrors(String module) {
        MutableModuleState state = state(safeName(module));
        state.consecutiveErrors = 0;
        state.successfulCallbacks = 0;
    }

    public synchronized void recordEvent(String module, String message) {
        append("INFO", safeName(module), safeText(message, ""));
    }

    public synchronized ModuleSnapshot snapshot(String module) {
        MutableModuleState state = state(safeName(module));
        return new ModuleSnapshot(state.lastReasonKey, state.lastReasonText, state.errorCount,
                state.consecutiveErrors, state.successfulCallbacks, state.lastError);
    }

    public synchronized Map<String, ModuleSnapshot> snapshots() {
        Map<String, ModuleSnapshot> result = new LinkedHashMap<>();
        for (Map.Entry<String, MutableModuleState> entry : modules.entrySet()) {
            MutableModuleState state = entry.getValue();
            result.put(entry.getKey(), new ModuleSnapshot(state.lastReasonKey, state.lastReasonText,
                    state.errorCount, state.consecutiveErrors, state.successfulCallbacks, state.lastError));
        }
        return Map.copyOf(result);
    }

    public synchronized List<String> recentLines() { return List.copyOf(recentLines); }

    public synchronized String buildReport(String runtimeSummary, List<String> moduleRows) {
        StringBuilder report = new StringBuilder(4_096);
        report.append("SkyOP Diagnostics Report\n");
        report.append("Generated: ").append(Instant.now()).append('\n');
        report.append(runtimeSummary == null ? "" : runtimeSummary).append('\n');
        report.append("\nModule status\n");
        if (moduleRows != null) for (String row : moduleRows) report.append(row).append('\n');
        report.append("\nRecent local log (last 20 lines)\n");
        for (String line : recentLines) report.append(line).append('\n');
        report.append("\nLog file: ").append(logFile).append('\n');
        report.append("Privacy: this report is generated locally and is not transmitted.\n");
        return report.toString();
    }

    public static boolean shouldWriteReasonLog(long lastMillis, long nowMillis) {
        if (lastMillis == Long.MIN_VALUE) return true;
        return nowMillis >= lastMillis && nowMillis - lastMillis >= REASON_LOG_THROTTLE_MILLIS;
    }

    private MutableModuleState state(String module) {
        return modules.computeIfAbsent(module, ignored -> new MutableModuleState());
    }

    private void append(String level, String module, String message) {
        String flattened = message == null ? "" : message.replace('\n', ' ').replace('\r', ' ');
        String line = Instant.now() + " | " + level + " | " + module + " | " + flattened;
        if (recentLines.size() == 20) recentLines.removeFirst();
        recentLines.addLast(line);
        byte[] bytes = (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
        try {
            Path parent = logFile.getParent();
            if (parent != null) Files.createDirectories(parent);
            long existing = Files.exists(logFile) ? Files.size(logFile) : 0L;
            if (existing + bytes.length > MAX_LOG_BYTES) {
                Path backup = logFile.resolveSibling(logFile.getFileName() + ".1");
                Files.move(logFile, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.write(logFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Logging failure must never interrupt the game runtime.
        }
    }

    private static String safeName(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value.replace('|', '/');
    }

    private static String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.replace('|', '/').replace('\n', ' ').replace('\r', ' ');
    }

    public record ModuleSnapshot(String lastReasonKey, String lastReasonText, int errorCount,
                                 int consecutiveErrors, int successfulCallbacks, String lastError) { }

    private static final class MutableModuleState {
        private String lastReasonKey = "unknown";
        private String lastReasonText = "No diagnostic reason recorded yet.";
        private String lastError = "";
        private int errorCount;
        private int consecutiveErrors;
        private int successfulCallbacks;
        private long lastReasonLogMillis = Long.MIN_VALUE;
    }
}
