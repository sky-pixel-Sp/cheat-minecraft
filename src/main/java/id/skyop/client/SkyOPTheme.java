package id.skyop.client;

import java.util.List;
import java.util.Locale;

/**
 * Persisted theme IDs keep their historical names for config compatibility.
 * Their palettes and display names follow the SkyOP Graphite UI specification.
 */
public final class SkyOPTheme {
    public record Palette(int canvas, int panel, int elevated, int hover,
                          int accent, int text, int muted, int success, int danger) { }
    public record Preset(String id, String displayName, Palette palette) { }

    private static final List<Preset> PRESETS = List.of(
            new Preset("CYAN", "Graphite", new Palette(
                    0xF00B0E14, 0xE6121722, 0xF51A2030, 0xFF212A3D,
                    0xFF4FC3F7, 0xFFF2F5FA, 0xFF9AA4B8, 0xFF3DDC97, 0xFFFF5C7A)),
            new Preset("AMETHYST", "Midnight", new Palette(
                    0xF0070B1A, 0xE60D1330, 0xF5141C44, 0xFF24315D,
                    0xFF82B1FF, 0xFFEEF2FF, 0xFFAAB5D4, 0xFF3DDC97, 0xFFFF5C7A)),
            new Preset("ROSE", "Crimson", new Palette(
                    0xF0120A0C, 0xE61B1013, 0xF5261619, 0xFF3A2028,
                    0xFFFF5C7A, 0xFFFFF1F3, 0xFFD5AAB3, 0xFF3DDC97, 0xFFFF5C7A)),
            new Preset("EMERALD", "Emerald", new Palette(
                    0xF007120E, 0xE60D1C16, 0xF514291F, 0xFF1D3A2C,
                    0xFF3DDC97, 0xFFEFFFF7, 0xFF9FC8B2, 0xFF3DDC97, 0xFFFF5C7A)),
            new Preset("ICE", "Mono", new Palette(
                    0xF00A0A0A, 0xE6141414, 0xF51E1E1E, 0xFF303030,
                    0xFFFFFFFF, 0xFFF5F5F5, 0xFF9AA4B8, 0xFF3DDC97, 0xFFFF5C7A)),
            new Preset("AMBER", "Sunset", new Palette(
                    0xF0120B14, 0xE61B1020, 0xF5281632, 0xFF3B2048,
                    0xFFFF8A65, 0xFFFFF4EE, 0xFFC7AEC7, 0xFF3DDC97, 0xFFFF5C7A))
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
