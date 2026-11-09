package id.skyop.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Stores an SDL keyboard scancode; -1 is SkyOP's explicit Unbound sentinel. */
public final class KeybindSetting extends SkyOPSetting<Integer> {
    public static final int UNBOUND = -1;

    public KeybindSetting(String id, String title, String description,
                          Supplier<Integer> getter, Consumer<Integer> setter) {
        super(id, title, description, getter, setter);
    }

    @Override protected Integer defaultValue() { return UNBOUND; }

    @Override
    protected Integer validate(Integer value) {
        if (value == UNBOUND) return UNBOUND;
        if (value <= 0 || !SkyOPSdlKeyboardIndex.isValidStateIndex(value)) {
            throw new IllegalArgumentException("Keybind must be Unbound or a valid SDL scancode.");
        }
        return value;
    }

    @Override protected String encode(Integer value) { return Integer.toString(value); }

    @Override
    protected Integer decode(String serialized) {
        try {
            return validate(Integer.parseInt(serialized));
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("Invalid keybind setting: " + id(), invalid);
        }
    }
}
