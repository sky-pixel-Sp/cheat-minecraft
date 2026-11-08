package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkyOPColorCodecTest {
    @Test
    void parsesSixDigitRgbWithOptionalPrefixes() {
        assertEquals(0xFF12ABEF, SkyOPColorCodec.parseRgb("#12aBeF"));
        assertEquals(0xFF12ABEF, SkyOPColorCodec.parseRgb("12aBeF"));
        assertEquals(0xFF12ABEF, SkyOPColorCodec.parseRgb("0x12aBeF"));
        assertEquals(0xFF12ABEF, SkyOPColorCodec.parseRgb("0X12aBeF"));
    }

    @Test
    void expandsThreeDigitShorthand() {
        assertEquals(0xFFAABBCC, SkyOPColorCodec.parseRgb("#abc"));
    }

    @Test
    void ignoresSurroundingWhitespace() {
        assertEquals(0xFF12ABEF, SkyOPColorCodec.parseRgb("  #12ABEF  "));
    }

    @Test
    void formatsOpaqueRgbWithoutAlpha() {
        assertEquals("12ABEF", SkyOPColorCodec.formatRgb(0x7F12ABEF));
    }

    @Test
    void rejectsMissingOrMalformedColors() {
        assertThrows(IllegalArgumentException.class, () -> SkyOPColorCodec.parseRgb(null));
        assertThrows(IllegalArgumentException.class, () -> SkyOPColorCodec.parseRgb(""));
        assertThrows(IllegalArgumentException.class, () -> SkyOPColorCodec.parseRgb("#12"));
        assertThrows(IllegalArgumentException.class, () -> SkyOPColorCodec.parseRgb("GG0000"));
    }
}
