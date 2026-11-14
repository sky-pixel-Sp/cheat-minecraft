package id.skyop.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.awt.Desktop;
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
import java.util.List;
import java.util.Map;

public final class SkyOPDiagnostics {
    private static final long MAX_LOG_BYTES = 1_048_576L;
    private static final int TAIL_LINES = 20;
    private static final Map<SkyOPModuleId, ModuleState> MODULES = new EnumMap<>(SkyOPModuleId.class);

    private SkyOPDiagnostics() { }

    private static ModuleState state(SkyOPModuleId id) {
        synchronized (MODULES) {
            return MODULES.computeIfAbsent(id, ignored -> new ModuleState());
        }
    }

    public static Path logPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("skyop-client").resolve("skyop.log");
    }

    public static void recordReadiness(SkyOPModuleId id, boolean ready, String reason) {
        ModuleState s = state(id);
        synchronized (s) { s.readyKnown = true; s.ready = ready; }
        recordReason(id, ready ? "ready" : "not_ready",
                ready ? "Ready" : (reason == null || reason.isBlank() ? "Module dependency is not ready" : reason));
    }

    public static void recordReason(SkyOPModuleId id, String key, String message) {
        ModuleState s = state(id);
        long now = System.currentTimeMillis();
        boolean log;
        synchronized (s) {
            s.reasonKey = key == null ? "unknown" : key;
            s.reasonText = message == null ? "" : message;
            log = SkyOPDiagnosticRules.shouldWriteReasonLog(s.lastReasonLogMillis, now);
            if (log) s.lastReasonLogMillis = now;
        }
        if (log) write("INFO", id.name(), s.reasonKey + ": " + s.reasonText, null);
    }

    public static boolean recordError(SkyOPModuleId id, String phase, Throwable error) {
        String shortError = concise(error);
        ModuleState s = state(id);
        boolean disable;
        synchronized (s) {
            s.lastError = shortError;
            disable = s.health.recordError(shortError);
            s.reasonKey = "error";
            s.reasonText = phase + ": " + shortError;
        }
        write("ERROR", id.name() + " / " + phase, shortError, stack(error));
        return disable;
    }

    public static void recordSystemError(String context, Throwable error) {
        write("ERROR", context, concise(error), stack(error));
    }

    public static void recordSuccess(SkyOPModuleId id) { state(id).health.recordSuccess(); }

    public static void markAutoDisabled(SkyOPModuleId id) {
        ModuleState s = state(id);
        synchronized (s) {
            s.health.markAutoDisabled();
            s.reasonKey = "error_disabled";
            s.reasonText = "Auto-disabled after 3 consecutive callback errors";
        }
        write("WARN", id.name(), "Auto-disabled after 3 consecutive callback errors", null);
    }

    public static Boolean readinessSnapshot(SkyOPModuleId id) {
        ModuleState state = state(id);
        synchronized (state) {
            return state.readyKnown ? Boolean.valueOf(state.ready) : null;
        }
    }

    public static void retry(SkyOPModuleId id) {
        if (id == null) return;
        ModuleState s = state(id);
        synchronized (s) { s.health.retry(); }
        recordReason(id, "retry", "Manual retry started a new enable session");
    }

    public static String buildReport(Minecraft client) {
        String version = client == null ? "unknown" : client.getLaunchedVersion();
        String modVersion = FabricLoader.getInstance().getModContainer("skyop")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        StringBuilder b = new StringBuilder(12000);
        b.append("SkyOP Diagnostics\nGenerated: ").append(Instant.now()).append('\n');
        b.append("SkyOP version: ").append(version).append("\nMinecraft version: ").append(version).append('\n');
        b.append("Build commit: not embedded in this artifact\nRuntime side: CLIENT\n");
        b.append("World loaded: ").append(client != null && client.level != null).append('\n');

        String connection = "OFFLINE";
        if (client != null && client.player != null && client.level != null) {
            if (client.getSingleplayerServer() != null) connection = client.isLocalServer() ? "INTEGRATED SINGLEPLAYER / LAN" : "INTEGRATED SERVER";
            else if (client.getConnection() != null) connection = "REMOTE MULTIPLAYER";
            else connection = "CONNECTED / UNKNOWN";
        }
        b.append("Connection: ").append(connection).append('\n');
        b.append("Server component: UNKNOWN (no versioned handshake)\n");
        b.append("Usage Gate: NOT IMPLEMENTED\n");

        Player p = client == null ? null : client.player;
        if (p == null) b.append("Game mode/health/hunger/position: unavailable\n");
        else {
            String mode = p.isCreative() ? "CREATIVE" : p.isSpectator() ? "SPECTATOR" : "SURVIVAL_OR_ADVENTURE";
            b.append("Game mode: ").append(mode).append('\n');
            b.append("Health: ").append(p.getHealth()).append(" / ").append(p.getMaxHealth()).append('\n');
            b.append("Hunger: ").append(p.getFoodData().getFoodLevel()).append('\n');
            b.append("Position: ").append(String.format(java.util.Locale.ROOT, "%.2f, %.2f, %.2f", p.getX(), p.getY(), p.getZ())).append('\n');
        }

        b.append("\nModule health\n");
        for (SkyOPModuleId id : SkyOPModuleId.values()) {
            ModuleState s = state(id);
            SkyOPModuleHealth.Snapshot h;
            boolean known, ready;
            String reason, lastError;
            synchronized (s) {
                h = s.health.snapshot(); known = s.readyKnown; ready = s.ready;
                reason = s.reasonKey + ": " + s.reasonText; lastError = s.lastError;
            }
            b.append(id.name()).append(" | enabled=").append(SkyOPCore.isEnabled(id))
             .append(" | active=").append(SkyOPModuleManager.isActive(id))
             .append(" | ready=").append(known ? Boolean.toString(ready) : "unknown")
             .append(" | errors=").append(h.errorCount())
             .append(" | consecutiveErrors=").append(h.consecutiveErrors())
             .append(" | autoDisabled=").append(h.autoDisabled())
             .append(" | reason=").append(reason).append(" | lastError=").append(lastError).append('\n');
        }
        b.append("\nLast ").append(TAIL_LINES).append(" log lines\n");
        for (String line : recentLogLines()) b.append(line).append('\n');
        b.append("\nLog file: ").append(logPath()).append("\nReport remains local; no network transmission.\n");
        return b.toString();
    }

    public static void openLogDirectory() throws IOException {
        Path dir = logPath().getParent();
        Files.createDirectories(dir);
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
            throw new IOException("Opening a folder is not supported on this desktop.");
        Desktop.getDesktop().open(dir.toFile());
    }

    public static List<String> recentLogLines() {
        Path p = logPath();
        if (!Files.exists(p)) return List.of("(No log entries yet)");
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            return List.copyOf(lines.subList(Math.max(0, lines.size() - TAIL_LINES), lines.size()));
        } catch (IOException e) {
            return List.of("(Unable to read log: " + e.getMessage() + ")");
        }
    }

    private static String concise(Throwable e) {
        if (e == null) return "Unknown throwable";
        String m = e.getMessage();
        return e.getClass().getSimpleName() + (m == null || m.isBlank() ? "" : ": " + m);
    }

    private static String stack(Throwable e) {
        if (e == null) return "";
        StringWriter w = new StringWriter();
        e.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    private static synchronized void write(String level, String context, String message, String stack) {
        Path p = logPath();
        try {
            Files.createDirectories(p.getParent());
            String line = Instant.now() + " | " + level + " | " + context + " | " + message + System.lineSeparator();
            long size = Files.exists(p) ? Files.size(p) : 0L;
            long incoming = line.getBytes(StandardCharsets.UTF_8).length + (stack == null ? 0L : stack.getBytes(StandardCharsets.UTF_8).length);
            if (size + incoming > MAX_LOG_BYTES) {
                Path backup = p.resolveSibling("skyop.log.1");
                Files.deleteIfExists(backup);
                if (Files.exists(p)) Files.move(p, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.writeString(p, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (stack != null && !stack.isBlank())
                Files.writeString(p, stack + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[SkyOP] Could not write diagnostics log: " + e.getMessage());
        }
    }

    private static final class ModuleState {
        final SkyOPModuleHealth health = new SkyOPModuleHealth();
        boolean readyKnown, ready;
        String reasonKey = "not_checked", reasonText = "No runtime check recorded";
        long lastReasonLogMillis;
        String lastError = "";
    }
}
