package id.skyop.client;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPThemeTest {
    @Test
    void allSixSpecificationThemesHaveDistinctAccentsAndOpaqueReadableColors() {
        Set<Integer> accents = new HashSet<>();
        assertEquals(6, SkyOPTheme.presets().size());
        for (SkyOPTheme.Preset preset : SkyOPTheme.presets()) {
            assertTrue((preset.palette().accent() >>> 24) > 0, preset.id());
            assertTrue((preset.palette().text() >>> 24) > 0, preset.id());
            assertTrue(accents.add(preset.palette().accent()), "Distinct accent expected for " + preset.id());
        }
    }

    @Test
    void historicalConfigThemeIdsNowResolveToSpecificationThemeNames() {
        assertEquals("Graphite", SkyOPTheme.displayName("CYAN"));
        assertEquals("Midnight", SkyOPTheme.displayName("AMETHYST"));
        assertEquals("Crimson", SkyOPTheme.displayName("ROSE"));
        assertEquals("Emerald", SkyOPTheme.displayName("EMERALD"));
        assertEquals("Mono", SkyOPTheme.displayName("ICE"));
        assertEquals("Sunset", SkyOPTheme.displayName("AMBER"));
    }

    @Test
    void unknownAndMissingThemesFallBackToGraphiteWithoutBreakingLegacyIds() {
        assertEquals("CYAN", SkyOPTheme.normalize(null));
        assertEquals("CYAN", SkyOPTheme.normalize("unknown"));
        assertEquals("CYAN", SkyOPTheme.normalize(" cyan "));
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
