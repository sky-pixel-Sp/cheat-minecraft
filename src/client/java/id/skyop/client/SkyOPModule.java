package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Runtime lifecycle contract for a SkyOP module.
 * Hooks that do not apply to a module use the documented default no-op implementation.
 */
public interface SkyOPModule {
    SkyOPClient.Module type();
    List<SkyOPSetting<?>> settings();

    default boolean isReady(Minecraft client) {
        return SkyOPModuleDependencies.isAvailable(client, type());
    }

    default void onEnable(Minecraft client) { }
    default void onDisable(Minecraft client) { }
    default void onTick(Minecraft client) { }
    default void onRender2D(Minecraft client, GuiGraphicsExtractor graphics) { }
    default void onRender3D(Minecraft client, Object renderContext) { }
    default void onWorldChange(Minecraft client) { }
}
