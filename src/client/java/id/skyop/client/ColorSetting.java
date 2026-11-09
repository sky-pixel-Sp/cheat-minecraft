package id.skyop.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ColorSetting extends SkyOPSetting<Integer> {
    private final int defaultValue;

    public ColorSetting(String id, String title, String description, int defaultValue,
                        Supplier<Integer> getter, Consumer<Integer> setter) {
        super(id, title, description, getter, setter);
        this.defaultValue = 0xFF000000 | (defaultValue & 0x00FFFFFF);
    }

    @Override protected Integer defaultValue() { return defaultValue; }
    @Override protected Integer validate(Integer value) { return 0xFF000000 | (value & 0x00FFFFFF); }
    @Override protected String encode(Integer value) { return "#" + SkyOPColorCodec.formatRgb(value); }

    @Override
    protected Integer decode(String serialized) {
        try {
            return SkyOPColorCodec.parseRgb(serialized);
        } catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException("Invalid color setting: " + id(), invalid);
        }
    }
}
