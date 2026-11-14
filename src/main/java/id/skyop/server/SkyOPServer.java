package id.skyop.server;

import net.fabricmc.api.DedicatedServerModInitializer;

public final class SkyOPServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        SkyOPServerDiagnostics.startup();
    }
}
