package id.skyop.client;

/**
 * Bounds checks for Minecraft 26.3's SDL keyboard-state array.
 *
 * SDL 3 exposes the keyboard state as SDL_SCANCODE_COUNT (512) entries.
 * The valid indexes are therefore 0 through 511; Minecraft's unbound key
 * sentinel (-1) must be intercepted before it reaches the DirectByteBuffer.
 */
public final class SkyOPSdlKeyboardIndex {
    public static final int SDL_SCANCODE_COUNT = 512;

    private SkyOPSdlKeyboardIndex() {
    }

    public static boolean isValidStateIndex(int key) {
        return key >= 0 && key < SDL_SCANCODE_COUNT;
    }
}
