package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyOPSdlKeyboardIndexTest {
    @Test
    void acceptsFirstAndLastSdlStateIndexes() {
        assertTrue(SkyOPSdlKeyboardIndex.isValidStateIndex(0));
        assertTrue(SkyOPSdlKeyboardIndex.isValidStateIndex(SkyOPSdlKeyboardIndex.SDL_SCANCODE_COUNT - 1));
    }

    @Test
    void rejectsNegativeAndPastEndIndexes() {
        assertFalse(SkyOPSdlKeyboardIndex.isValidStateIndex(-1));
        assertFalse(SkyOPSdlKeyboardIndex.isValidStateIndex(SkyOPSdlKeyboardIndex.SDL_SCANCODE_COUNT));
        assertFalse(SkyOPSdlKeyboardIndex.isValidStateIndex(Integer.MAX_VALUE));
    }
}
