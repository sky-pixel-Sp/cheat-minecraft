package id.skyop.client;

import id.skyop.common.SkyOPDiagnostics;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Client-runtime diagnostics; dedicated server owns a separate instance and log. */
public final class SkyOPClientDiagnostics {
    private static final Path LOG_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("skyop-client").resolve("skyop.log");
    private static final SkyOPDiagnostics INSTANCE = new SkyOPDiagnostics(LOG_FILE);

    private SkyOPClientDiagnostics() { }

    public static SkyOPDiagnostics get() { return INSTANCE; }

    public static String buildReport() {
        Minecraft client = Minecraft.getInstance();
        String connection = client.getSingleplayerServer() != null
                ? "integrated/single-player"
                : client.player == null ? "not connected"
                : "remote connection (server type not distinguishable here)";
        String playerState = client.player == null ? "not loaded"
                : client.player.isSpectator() ? "spectator"
                : client.player.isCreative() ? "creative"
                : "survival/adventure (not distinguished by this report)";
        String world = client.level == null ? "not loaded" : "loaded";
        String health = client.player == null ? "N/A"
                : String.format(java.util.Locale.ROOT, "%.1f / %.1f",
                client.player.getHealth(), client.player.getMaxHealth());
        String runtime = "SkyOP: 4.0.0 (repository version)\n"
                + "Git commit: unavailable in this runtime build\n"
                + "Minecraft target: 26.3\n"
                + "Runtime side: CLIENT\n"
                + "World: " + world + "\n"
                + "Connection: " + connection + "\n"
                + "Server component: UNKNOWN (version handshake not implemented)\n"
                + "Player mode: " + playerState + "\n"
                + "Health: " + health;

        List<String> rows = new ArrayList<>();
        for (SkyOPModuleId id : SkyOPModuleId.values()) {
            SkyOPModule module = SkyOPModuleManager.get(id);
            boolean enabled = SkyOPCore.isEnabled(id);
            boolean ready = module != null && SkyOPModuleManager.isReadyForDiagnostics(client, id);
            SkyOPDiagnostics.ModuleSnapshot status = INSTANCE.snapshot(id.name());
            rows.add(id.name() + " | side=" + id.runtimeSide()
                    + " | enabled=" + enabled
                    + " | ready=" + ready
                    + " | errors=" + status.errorCount()
                    + " | consecutiveErrors=" + status.consecutiveErrors()
                    + " | reason=" + status.lastReasonKey() + ": " + status.lastReasonText()
                    + (status.lastError().isBlank() ? "" : " | lastError=" + status.lastError()));
        }
        return INSTANCE.buildReport(runtime, rows);
    }
}
