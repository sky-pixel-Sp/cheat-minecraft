package id.skyop.client;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class SkyOPThemeTest {
    @Test
    void presetsHaveDistinctAccentsAndOpaqueReadableColors() {
        Set<Integer> accents = new HashSet<>();
        for (SkyOPTheme.Preset preset : SkyOPTheme.presets()) {
            assertTrue((preset.palette().accent() >>> 24) > 0, preset.id());
            assertTrue((preset.palette().text() >>> 24) > 0, preset.id());
            assertTrue(accents.add(preset.palette().accent()), "Distinct accent expected for " + preset.id());
        }
        assertTrue(SkyOPTheme.presets().size() >= 5);
    }

    @Test
    void unknownAndMissingThemesFallBackToCyberNeon() {
        assertEquals("CYAN", SkyOPTheme.normalize(null));
        assertEquals("CYAN", SkyOPTheme.normalize("unknown"));
        assertEquals(SkyOPTheme.palette("CYAN"), SkyOPTheme.palette("bad-value"));
    }

    @Test
    void cyclesForwardAndWraps() {
        String last = SkyOPTheme.presets().get(SkyOPTheme.presets().size() - 1).id();
        assertEquals("AMETHYST", SkyOPTheme.next("CYAN"));
        assertEquals("CYAN", SkyOPTheme.next(last));
        assertEquals("AMETHYST", SkyOPTheme.next("broken"));
    }

    @Test
    void normalizesIdsWithoutCaseOrWhitespaceSensitivity() {
        assertEquals("EMERALD", SkyOPTheme.normalize(" emerald "));
        assertEquals("Emerald", SkyOPTheme.displayName("emerald"));
    }
}
