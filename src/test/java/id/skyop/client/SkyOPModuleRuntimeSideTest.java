package id.skyop.client;

import id.skyop.common.SkyOPRuntimeSide;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkyOPModuleRuntimeSideTest {
    @Test
    void everyCurrentlyImplementedModuleIsExplicitlyClientRuntime() {
        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            assertEquals(SkyOPRuntimeSide.CLIENT, module.runtimeSide(), module.name());
        }
    }
}
