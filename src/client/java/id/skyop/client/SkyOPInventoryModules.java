package id.skyop.client;

import java.util.List;

/** Inventory and utility module wrappers; inventory actions remain in SkyOPInventoryManager. */
public final class SkyOPInventoryModules {
    private SkyOPInventoryModules() { }

    public static final class AutoEat extends SkyOPModule {
        public AutoEat(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_EAT, settings); }
        @Override public void onTick(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.tickAutoEat(client); }
        @Override public void onDisable(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.onModuleDisabled(client, type()); }
    }

    public static final class AutoTotem extends SkyOPModule {
        public AutoTotem(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_TOTEM, settings); }
        @Override public void onTick(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.tickAutoTotem(client); }
        @Override public void onDisable(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.onModuleDisabled(client, type()); }
    }

    public static final class AutoArmor extends SkyOPModule {
        public AutoArmor(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_ARMOR, settings); }
        @Override public void onTick(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.tickAutoArmor(client); }
        @Override public void onDisable(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.onModuleDisabled(client, type()); }
    }

    public static final class AutoTool extends SkyOPModule {
        public AutoTool(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_TOOL, settings); }
        @Override public void onTick(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.tickAutoTool(client); }
        @Override public void onDisable(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.onModuleDisabled(client, type()); }
    }

    public static final class AutoRefill extends SkyOPModule {
        public AutoRefill(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_REFILL, settings); }
        @Override public void onTick(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.tickAutoRefill(client); }
        @Override public void onDisable(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.onModuleDisabled(client, type()); }
    }

    public static final class AutoFish extends SkyOPModule {
        public AutoFish(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_FISH, settings); }
        @Override public void onTick(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.tickAutoFish(client); }
        @Override public void onDisable(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.onModuleDisabled(client, type()); }
    }

    public static final class InventoryCleaner extends SkyOPModule {
        public InventoryCleaner(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.INVENTORY_CLEANER, settings); }
        @Override public void onTick(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.tickInventoryCleaner(client); }
        @Override public void onDisable(net.minecraft.client.Minecraft client) { SkyOPInventoryManager.onModuleDisabled(client, type()); }
    }

    public static final class HotbarPresets extends SkyOPModule {
        public HotbarPresets(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.HOTBAR_PRESETS, settings); }
    }

    public static final class ProfileManager extends SkyOPModule {
        public ProfileManager(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.PROFILE_MANAGER, settings); }
    }
}
