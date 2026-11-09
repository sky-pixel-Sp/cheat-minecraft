package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/** Base class for every independently registered SkyOP module. */
public abstract class SkyOPModule {
    private final SkyOPModuleId type;
    private final List<SkyOPSetting<?>> settings;

    protected SkyOPModule(SkyOPModuleId type, List<SkyOPSetting<?>> settings) {
        this.type = type;
        this.settings = List.copyOf(settings);
    }

    public final SkyOPModuleId type() { return type; }
    public final List<SkyOPSetting<?>> settings() { return settings; }

    public boolean isReady(Minecraft client) {
        return SkyOPModuleDependencies.isAvailable(client, type);
    }

    public void onEnable(Minecraft client) { }
    public void onDisable(Minecraft client) { }
    public void onTick(Minecraft client) { }
    public void onRender2D(Minecraft client, GuiGraphicsExtractor graphics) { }
    public void onRender3D(Minecraft client, Object renderContext) { }
    public void onWorldChange(Minecraft client) { }
}
