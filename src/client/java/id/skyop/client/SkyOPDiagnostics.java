package id.skyop.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * Local-only diagnostics for module lifecycle and automation decisions.
 * The log is restricted to the SkyOP config directory; no network reporting is used.
 */
public final class SkyOPDiagnostics {
    private static final long MAX_LOG_BYTES = 1_048_576L;
    private static final EnumMap<SkyOPModuleId, MutableStatus> MODULE_STATUS =
            new EnumMap<>(SkyOPModuleId.class);

    public record Snapshot(int errorCount, String lastReason, String lastError,
                           Instant lastReasonAt, Instant lastErrorAt) { }

    private static final class MutableStatus {
        private int errorCount;
        private String lastReason = "";
        private String lastError = "";
        private Instant lastReasonAt;
        private Instant lastErrorAt;
        private long lastLogMillis;
    }

    private SkyOPDiagnostics() { }

    private static MutableStatus state(SkyOPModuleId module) {
        return MODULE_STATUS.computeIfAbsent(module, ignored -> new MutableStatus());
    }

    public static synchronized Snapshot snapshot(SkyOPModuleId module) {
        MutableStatus state = state(module);
        return new Snapshot(state.errorCount, state.lastReason, state.lastError,
                state.lastReasonAt, state.lastErrorAt);
    }

    /** Reset the threshold counter only when a user explicitly starts a new enable session. */
    public static synchronized void beginModuleSession(SkyOPModuleId module) {
        state(module).errorCount = 0;
    }

    public static boolean shouldDisableAfterErrors(int errorCount) {
        return SkyOPAutomationRules.shouldDisableAfterErrors(errorCount);
    }

    /** Records the latest reason an enabled automation did not perform an action. */
    public static synchronized void recordReason(SkyOPModuleId module, String reason) {
        if (module == null || reason == null || reason.isBlank()) return;
        MutableStatus state = state(module);
        if (reason.equals(state.lastReason)) return;
        state.lastReason = reason;
        state.lastReasonAt = Instant.now();
        long now = System.currentTimeMillis();
        if (SkyOPAutomationRules.shouldWriteReasonLog(state.lastLogMillis, now)) {
            state.lastLogMillis = now;
            appendLog("INFO", module.name() + " / decision", reason);
        }
    }

    /** Returns the number of failures observed for this module in its current enable session. */
    public static synchronized int recordError(SkyOPModuleId module, String phase, Throwable error) {
        if (module == null) {
            recordServiceError(phase, error);
            return 0;
        }
        MutableStatus state = state(module);
        state.errorCount++;
        state.lastErrorAt = Instant.now();
        String summary = error == null ? "Unknown error"
                : error.getClass().getSimpleName() + ": " + String.valueOf(error.getMessage());
        state.lastError = phase + " — " + summary;
        appendLog("ERROR", module.name() + " / " + phase, throwableDetails(error));
        return state.errorCount;
    }

    public static synchronized void recordServiceError(String phase, Throwable error) {
        appendLog("ERROR", "SkyOP service / " + phase, throwableDetails(error));
    }

    public static Path logPath() {
        return FabricLoader.getInstance().getConfigDir()
                .resolve("skyop-client").resolve("skyop.log").normalize();
    }

    public static synchronized String buildReport() {
        StringBuilder report = new StringBuilder(4096);
        report.append("SkyOP Client diagnostic report").append(System.lineSeparator());
        report.append("Created: ").append(Instant.now()).append(System.lineSeparator());
        report.append("SkyOP: ").append(modVersion("skyop")).append(System.lineSeparator());
        report.append("Minecraft: ").append(modVersion("minecraft")).append(System.lineSeparator());
        Minecraft client = Minecraft.getInstance();
        appendGameState(report, client);
        report.append("Java: ").append(System.getProperty("java.version", "unknown")).append(System.lineSeparator());
        report.append("OS: ").append(System.getProperty("os.name", "unknown")).append(" ")
                .append(System.getProperty("os.version", "unknown")).append(System.lineSeparator());
        report.append("Log: ").append(logPath()).append(System.lineSeparator());
        report.append(System.lineSeparator());
        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            Snapshot snapshot = snapshot(module);
            report.append(module.name())
                    .append(" enabled=").append(SkyOPCore.isEnabled(module))
                    .append(" ready=").append(readyOrUnknown(client, module))
                    .append(" errors=").append(snapshot.errorCount()).append("/3")
                    .append(System.lineSeparator());
            report.append("  reason: ").append(emptyAsDash(snapshot.lastReason())).append(System.lineSeparator());
            report.append("  error: ").append(emptyAsDash(snapshot.lastError())).append(System.lineSeparator());
        }
        return report.toString();
    }

    private static String modVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static void appendGameState(StringBuilder report, Minecraft client) {
        report.append("World loaded: ").append(client != null && client.level != null)
                .append(System.lineSeparator());
        report.append("Singleplayer server: ")
                .append(client != null && client.getSingleplayerServer() != null)
                .append(System.lineSeparator());
        if (client != null && client.player != null) {
            var player = client.player;
            report.append("Creative (instabuild): ").append(player.getAbilities().instabuild)
                    .append(System.lineSeparator());
            report.append("Spectator: ").append(player.isSpectator()).append(System.lineSeparator());
            report.append("Health: ").append(player.getHealth()).append(System.lineSeparator());
        } else {
            report.append("Player: not in a world").append(System.lineSeparator());
        }
    }

    private static String readyOrUnknown(Minecraft client, SkyOPModuleId module) {
        if (client == null || client.player == null) return "n/a";
        try {
            return String.valueOf(SkyOPModuleManager.isReadyForDiagnostics(client, module));
        } catch (RuntimeException error) {
            return "error";
        }
    }

    private static String emptyAsDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String throwableDetails(Throwable error) {
        if (error == null) return "Unknown error";
        StringWriter buffer = new StringWriter();
        error.printStackTrace(new PrintWriter(buffer));
        return buffer.toString();
    }

    private static void appendLog(String level, String source, String details) {
        try {
            Path path = logPath();
            Path parent = path.getParent();
            if (parent == null) return;
            Files.createDirectories(parent);
            if (Files.exists(path) && Files.size(path) > MAX_LOG_BYTES) {
                Path previous = path.resolveSibling("skyop.log.1");
                Files.move(path, previous, StandardCopyOption.REPLACE_EXISTING);
            }
            String line = Instant.now() + " [" + level + "] " + source + System.lineSeparator()
                    + String.valueOf(details) + System.lineSeparator();
            Files.writeString(path, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException | SecurityException exception) {
            System.err.println("[SkyOP] Could not write diagnostics log: " + exception.getMessage());
        }
    }
}
