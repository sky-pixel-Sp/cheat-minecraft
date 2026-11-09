package id.skyop.client;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPSettingsTest {
    @Test
    void booleanSettingSerializesAndRecoversInvalidValuesToDefault() {
        AtomicReference<Boolean> value = new AtomicReference<>(false);
        BooleanSetting setting = new BooleanSetting(
                "enabled", "Enabled", "Whether the module is enabled.", true,
                value::get, value::set
        );

        setting.deserialize("true");
        assertTrue(value.get());
        assertEquals("true", setting.serialize());

        setting.deserialize("not-a-boolean");
        assertTrue(value.get(), "invalid input must restore the declared default");
    }

    @Test
    void numberSettingUsesCentralSchemaForClampingAndFallback() {
        AtomicReference<Double> value = new AtomicReference<>(4.2);
        NumberSetting setting = new NumberSetting(
                "auraRange", "Attack range", "Maximum attack distance.",
                SkyOPSettingsSchema.rule("auraRange"), value::get, value::set
        );

        setting.deserialize("99");
        assertEquals(6.0, value.get());
        setting.deserialize("NaN");
        assertEquals(4.2, value.get());
        setting.set(1.0);
        assertEquals(2.5, value.get());
        assertEquals(2.5, setting.minimum());
        assertEquals(6.0, setting.maximum());
    }

    @Test
    void modeSettingRejectsUnsupportedValuesAndRestoresDefault() {
        AtomicReference<String> value = new AtomicReference<>("Nearest");
        ModeSetting setting = new ModeSetting(
                "priority", "Priority", "Target selection priority.",
                java.util.List.of("Nearest", "LowestHealth", "Visible"),
                "Nearest", value::get, value::set
        );

        setting.deserialize("Visible");
        assertEquals("Visible", value.get());
        setting.deserialize("UnknownMode");
        assertEquals("Nearest", value.get());
        assertThrows(IllegalArgumentException.class, () -> setting.set("UnknownMode"));
    }

    @Test
    void multiSettingFiltersUnknownOptionsAndRoundTripsSelection() {
        Set<String> options = Set.of("Players", "Hostile", "Passive");
        AtomicReference<Set<String>> value = new AtomicReference<>(Set.of("Hostile"));
        MultiSetting setting = new MultiSetting(
                "targets", "Targets", "Allowed entity categories.",
                options, Set.of("Hostile"), value::get, value::set
        );

        setting.deserialize("Players,Unknown,Passive");
        assertEquals(Set.of("Players", "Passive"), value.get());
        String serialized = setting.serialize();
        setting.deserialize(serialized);
        assertEquals(Set.of("Players", "Passive"), value.get());
        setting.deserialize("");
        assertEquals(Set.of(), value.get());
    }

    @Test
    void colorSettingUsesTheSharedRgbCodecAndRestoresDefaultOnInvalidInput() {
        AtomicInteger value = new AtomicInteger(0xFF61C7F2);
        ColorSetting setting = new ColorSetting(
                "accent", "Accent", "Interface accent color.", 0xFF61C7F2,
                value::get, value::set
        );

        setting.deserialize("#ABC");
        assertEquals(0xFFAABBCC, value.get());
        assertEquals("#AABBCC", setting.serialize());
        setting.deserialize("not-a-color");
        assertEquals(0xFF61C7F2, value.get());
    }

    @Test
    void settingLabelsResolveDynamicallyWhenLanguageChanges() {
        AtomicReference<Boolean> value = new AtomicReference<>(false);
        BooleanSetting setting = new BooleanSetting(
                "targetPlayers", "lang:setting.target_players", "lang:setting.include_other_players",
                false, value::get, value::set
        );

        SkyOPLanguage.set("id");
        String indonesianTitle = setting.title();
        String indonesianDescription = setting.description();
        SkyOPLanguage.set("en");

        assertNotEquals(indonesianTitle, setting.title());
        assertNotEquals(indonesianDescription, setting.description());
        assertEquals("Target players", setting.title());
        SkyOPLanguage.set("id");
    }

    @Test
    void keybindSettingAcceptsUnboundAndValidScancodesOnly() {
        AtomicInteger value = new AtomicInteger(61);
        KeybindSetting setting = new KeybindSetting(
                "keybind", "Keybind", "Module keyboard shortcut.",
                61, value::get, value::set
        );

        setting.deserialize("511");
        assertEquals(511, value.get());
        setting.deserialize("-1");
        assertEquals(KeybindSetting.UNBOUND, value.get());
        setting.deserialize("512");
        assertEquals(61, value.get(), "invalid key should restore the module's default key");
        assertThrows(IllegalArgumentException.class, () -> setting.set(512));
        assertEquals(61, value.get());
    }
}
