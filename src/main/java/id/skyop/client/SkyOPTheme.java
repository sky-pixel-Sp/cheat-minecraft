package id.skyop.client;

import java.util.List;
import java.util.Locale;

/** Named, persisted color palettes for SkyOP's ClickGUI and floating quick panel. */
public final class SkyOPTheme {
    public record Palette(int canvas, int panel, int elevated, int hover,
                          int accent, int text, int muted, int success, int danger) { }
    public record Preset(String id, String displayName, Palette palette) { }

    private static final List<Preset> PRESETS = List.of(
            new Preset("CYAN", "Cyber Neon", new Palette(
                    0xFF101925, 0xF010141C, 0xFF1A202B, 0xFF263343,
                    0xFF55B8F4, 0xFFF3F6FA, 0xFF98A6B8, 0xFF64D6A4, 0xFFE07B83)),
            new Preset("AMETHYST", "Amethyst", new Palette(
                    0xFF171226, 0xF0141020, 0xFF241E36, 0xFF382B4D,
                    0xFFB69AFF, 0xFFF7F2FF, 0xFFB4A8C8, 0xFF70D6B0, 0xFFFF879B)),
            new Preset("EMERALD", "Emerald", new Palette(
                    0xFF0E1B17, 0xF00C1712, 0xFF192A22, 0xFF294338,
                    0xFF62D7A0, 0xFFF0FAF4, 0xFF9CB9A8, 0xFF62D7A0, 0xFFE9828B)),
            new Preset("AMBER", "Amber", new Palette(
                    0xFF1B160D, 0xF0171209, 0xFF2A2113, 0xFF46371F,
                    0xFFFFC86B, 0xFFFFF8E9, 0xFFC3B18E, 0xFF78D7A7, 0xFFF17D8A)),
            new Preset("ROSE", "Rose", new Palette(
                    0xFF1B1017, 0xF0180E14, 0xFF2B1823, 0xFF492638,
                    0xFFFF88AE, 0xFFFFF3F7, 0xFFC6A5B2, 0xFF72D8B1, 0xFFFF88AE)),
            new Preset("ICE", "Arctic", new Palette(
                    0xFF0D1820, 0xF00B141C, 0xFF172631, 0xFF263D4D,
                    0xFF8ADCF5, 0xFFF2FAFF, 0xFFA6BAC8, 0xFF70D9B0, 0xFFEF8790))
    );

    private SkyOPTheme() { }

    public static List<Preset> presets() { return PRESETS; }

    public static String normalize(String id) {
        if (id == null) return PRESETS.get(0).id();
        String normalized = id.trim().toUpperCase(Locale.ROOT);
        return PRESETS.stream().anyMatch(preset -> preset.id().equals(normalized))
                ? normalized : PRESETS.get(0).id();
    }

    public static Palette palette(String id) {
        String normalized = normalize(id);
        return PRESETS.stream().filter(preset -> preset.id().equals(normalized))
                .findFirst().orElse(PRESETS.get(0)).palette();
    }

    public static String displayName(String id) {
        String normalized = normalize(id);
        return PRESETS.stream().filter(preset -> preset.id().equals(normalized))
                .findFirst().orElse(PRESETS.get(0)).displayName();
    }

    public static String next(String id) {
        String normalized = normalize(id);
        for (int index = 0; index < PRESETS.size(); index++) {
            if (PRESETS.get(index).id().equals(normalized)) {
                return PRESETS.get((index + 1) % PRESETS.size()).id();
            }
        }
        return PRESETS.get(0).id();
    }
}
