package id.skyop.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class StringSettingTest {
    @Test
    void preservesLeadingAndTrailingSpacesWhenEditing() {
        AtomicReference<String> value = new AtomicReference<>("default");
        StringSetting setting = new StringSetting(
                "customHudText", "Custom text", "Visible text", 12,
                value::get, value::set
        );

        setting.set("  SkyOP  ");

        assertEquals("  SkyOP  ", value.get());
        assertEquals("  SkyOP  ", setting.value());
    }

    @Test
    void serializesAndRestoresTextIncludingSpacesAndUnicode() {
        AtomicReference<String> value = new AtomicReference<>("start");
        StringSetting setting = new StringSetting(
                "customHudText", "Custom text", "Visible text", 20,
                value::get, value::set
        );

        setting.set("  Halo 世界  ");
        String encoded = setting.serialize();
        value.set("overwritten");
        setting.deserialize(encoded);

        assertEquals("  Halo 世界  ", value.get());
    }

    @Test
    void rejectsTextLongerThanItsDeclaredLimit() {
        AtomicReference<String> value = new AtomicReference<>("ok");
        StringSetting setting = new StringSetting(
                "customHudText", "Custom text", "Visible text", 5,
                value::get, value::set
        );

        assertThrows(IllegalArgumentException.class, () -> setting.set("123456"));
        assertEquals("ok", value.get());
    }
}
