package id.skyop.client;

/**
 * Parses and formats opaque RGB values used by the SkyOP HUD accent editor.
 */
public final class SkyOPColorCodec {
    private SkyOPColorCodec() { }

    /**
     * Parses #RRGGBB, RRGGBB, 0xRRGGBB, or shorthand RGB such as #ABC.
     *
     * @return opaque ARGB color
     * @throws IllegalArgumentException when the input is null or malformed
     */
    public static int parseRgb(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Color is required");
        }

        String value = input.strip();
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }

        if (value.length() == 3) {
            value = "" + value.charAt(0) + value.charAt(0)
                    + value.charAt(1) + value.charAt(1)
                    + value.charAt(2) + value.charAt(2);
        }
        if (value.length() != 6) {
            throw new IllegalArgumentException("Use exactly 6 hexadecimal digits");
        }

        try {
            return 0xFF000000 | Integer.parseInt(value, 16);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Color contains a non-hexadecimal character", exception);
        }
    }

    /** Formats an ARGB value as six uppercase RGB digits, without the alpha channel. */
    public static String formatRgb(int argb) {
        return String.format(java.util.Locale.ROOT, "%06X", argb & 0x00FFFFFF);
    }
}
