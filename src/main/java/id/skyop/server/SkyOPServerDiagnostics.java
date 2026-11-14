package id.skyop.server;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class SkyOPServerDiagnostics {
    private static final long MAX_LOG_BYTES = 1_048_576L;
    private SkyOPServerDiagnostics() { }

    public static Path logPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("skyop-server").resolve("skyop-server.log");
    }
    public static synchronized void startup() {
        write("INFO", "SERVER", "SkyOP dedicated-server entrypoint initialized");
    }
    public static String buildReport() {
        return "SkyOP Server Diagnostics\nGenerated: " + Instant.now()
                + "\nRuntime side: DEDICATED_SERVER\nJava: " + System.getProperty("java.version")
                + "\nOS: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch")
                + "\nServer module registry: NOT IMPLEMENTED\nUsage Gate: NOT IMPLEMENTED"
                + "\nClient configuration: not read\nServer log: " + logPath() + "\n";
    }
    private static void write(String level, String context, String message) {
        Path p = logPath();
        try {
            Files.createDirectories(p.getParent());
            String line = Instant.now() + " | " + level + " | " + context + " | " + message + System.lineSeparator();
            if ((Files.exists(p) ? Files.size(p) : 0L) + line.getBytes(StandardCharsets.UTF_8).length > MAX_LOG_BYTES) {
                Path backup = p.resolveSibling("skyop-server.log.1");
                Files.deleteIfExists(backup);
                if (Files.exists(p)) Files.move(p, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.writeString(p, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[SkyOP Server] Log write failed: " + e.getMessage());
        }
    }
}
