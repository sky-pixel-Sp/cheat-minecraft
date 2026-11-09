package id.skyop.client;

import java.util.List;

/** Inventory and utility module wrappers; inventory actions remain in SkyOPInventoryManager. */
public final class SkyOPInventoryModules {
    private SkyOPInventoryModules() { }

    public static final class AutoEat extends SkyOPModule {
        public AutoEat(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_EAT, settings); }
    }

    public static final class AutoArmor extends SkyOPModule {
        public AutoArmor(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_ARMOR, settings); }
    }

    public static final class AutoTool extends SkyOPModule {
        public AutoTool(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_TOOL, settings); }
    }

    public static final class AutoRefill extends SkyOPModule {
        public AutoRefill(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_REFILL, settings); }
    }

    public static final class AutoFish extends SkyOPModule {
        public AutoFish(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.AUTO_FISH, settings); }
    }

    public static final class InventoryCleaner extends SkyOPModule {
        public InventoryCleaner(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.INVENTORY_CLEANER, settings); }
    }

    public static final class HotbarPresets extends SkyOPModule {
        public HotbarPresets(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.HOTBAR_PRESETS, settings); }
    }

    public static final class ProfileManager extends SkyOPModule {
        public ProfileManager(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.PROFILE_MANAGER, settings); }
    }
}
