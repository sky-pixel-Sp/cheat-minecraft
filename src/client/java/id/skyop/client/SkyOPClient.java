package id.skyop.client;

import net.fabricmc.api.ClientModInitializer;

/** Fabric entrypoint. All gameplay and client services are delegated to dedicated classes. */
public final class SkyOPClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkyOPCore.initialize();
    }
}
