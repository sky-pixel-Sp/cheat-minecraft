package id.skyop.client;

import org.junit.jupiter.api.Test;
import org.lwjgl.sdl.SDLScancode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyOPSdlKeyboardIndexTest {
    @Test
    void sdlUnknownScancodeIsSafeAndUnbound() {
        assertEquals(0, SkyOPSdlKeyboardIndex.unboundScancode());
        assertTrue(SkyOPSdlKeyboardIndex.isValidStateIndex(SkyOPSdlKeyboardIndex.unboundScancode()));
    }

    @Test
    void acceptsTheFirstAndLastSdlStateIndexes() {
        assertTrue(SkyOPSdlKeyboardIndex.isValidStateIndex(0));
        assertTrue(SkyOPSdlKeyboardIndex.isValidStateIndex(SDLScancode.SDL_SCANCODE_COUNT - 1));
    }

    @Test
    void rejectsNegativeAndPastEndIndexes() {
        assertFalse(SkyOPSdlKeyboardIndex.isValidStateIndex(-1));
        assertFalse(SkyOPSdlKeyboardIndex.isValidStateIndex(SDLScancode.SDL_SCANCODE_COUNT));
        assertFalse(SkyOPSdlKeyboardIndex.isValidStateIndex(Integer.MAX_VALUE));
    }
}
