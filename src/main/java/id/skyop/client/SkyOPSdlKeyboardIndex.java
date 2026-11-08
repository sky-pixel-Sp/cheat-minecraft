package id.skyop.client;

import org.lwjgl.sdl.SDLScancode;

/**
 * Validates indexes used by Minecraft 26.3's SDL keyboard-state buffer.
 *
 * SDL's unassigned scancode is 0; Minecraft's InputConstants.UNKNOWN is -1
 * and must never be used as an index into SDL_GetKeyboardState().
 */
public final class SkyOPSdlKeyboardIndex {
    private SkyOPSdlKeyboardIndex() {
    }

    public static boolean isValidStateIndex(int key) {
        return key >= 0 && key < SDLScancode.SDL_SCANCODE_COUNT;
    }

    public static int unboundScancode() {
        return SDLScancode.SDL_SCANCODE_UNKNOWN;
    }
}
