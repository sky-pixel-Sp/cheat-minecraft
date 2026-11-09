package id.skyop.client;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModeSetting extends SkyOPSetting<String> {
    private final List<String> options;
    private final String defaultValue;

    public ModeSetting(String id, String title, String description, List<String> options,
                       String defaultValue, Supplier<String> getter, Consumer<String> setter) {
        super(id, title, description, getter, setter);
        this.options = List.copyOf(options);
        if (this.options.isEmpty() || !this.options.contains(defaultValue)) {
            throw new IllegalArgumentException("Mode default must be one of its options.");
        }
        this.defaultValue = defaultValue;
    }

    public List<String> options() { return options; }
    @Override protected String defaultValue() { return defaultValue; }

    @Override
    protected String validate(String value) {
        if (!options.contains(value)) throw new IllegalArgumentException("Unsupported mode: " + value);
        return value;
    }

    @Override protected String encode(String value) { return value; }

    @Override
    protected String decode(String serialized) {
        return options.contains(serialized) ? serialized : defaultValue;
    }
}
