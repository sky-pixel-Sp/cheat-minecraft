package id.skyop.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPSpecialSettingTest {
    @Test
    void textSettingWritesBoundedTextAndRestoresItsDeclaredDefault() {
        AtomicReference<String> value = new AtomicReference<>("SkyOP");
        TextSetting setting = new TextSetting(
                "label", "Label", "Visible HUD label.", "SkyOP", 8, value::get, value::set
        );

        setting.set("SkyOP Client - Expanded");
        assertEquals("SkyOP Cl", value.get());
        assertEquals("SkyOP Cl", setting.serialize());
        assertEquals(8, setting.maximumLength());

        setting.deserialize(null);
        assertEquals("SkyOP", value.get());
    }

    @Test
    void actionSettingInvokesRegisteredCallbackWithoutBecomingPersistentState() throws Exception {
        AtomicInteger invocations = new AtomicInteger();
        ActionSetting setting = new ActionSetting(
                "profile.save.1", "Save", "Save configuration slot one.",
                () -> "Save 1", invocations::incrementAndGet
        );

        assertFalse(setting.value());
        assertEquals("Save 1", setting.buttonLabel());
        setting.activate();
        assertEquals(1, invocations.get());
        assertEquals("false", setting.serialize());
    }
}
