package id.skyop.client;

import java.util.List;

/** Player-focused modules whose work is applied by the client event/mixin layer. */
public final class SkyOPPlayerModules {
    private SkyOPPlayerModules() { }

    public static final class FastBreak extends SkyOPModule {
        public FastBreak(List<SkyOPSetting<?>> settings) {
            super(SkyOPModuleId.FAST_BREAK, settings);
        }
    }
}
