package id.skyop.server;

import id.skyop.common.SkyOPDiagnostics;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Dedicated-server bootstrap. This entrypoint deliberately has no
 * client, renderer, input, GLFW or Minecraft-client references.
 */
public final class SkyOPServer implements DedicatedServerModInitializer {
    private static final Path LOG_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("skyop-server").resolve("skyop.log");
    private static final SkyOPDiagnostics DIAGNOSTICS = new SkyOPDiagnostics(LOG_FILE);

    @Override
    public void onInitializeServer() {
        DIAGNOSTICS.recordEvent("SERVER", "SkyOP server entrypoint initialized.");
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                DIAGNOSTICS.recordEvent("SERVER", "Minecraft server started; SkyOP server component is loaded."));
        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
                DIAGNOSTICS.recordEvent("SERVER", "Minecraft server is stopping."));
    }
}
