package id.skyop.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BooleanSetting extends SkyOPSetting<Boolean> {
    private final boolean defaultValue;

    public BooleanSetting(String id, String title, String description, boolean defaultValue,
                          Supplier<Boolean> getter, Consumer<Boolean> setter) {
        super(id, title, description, getter, setter);
        this.defaultValue = defaultValue;
    }

    @Override protected Boolean defaultValue() { return defaultValue; }
    @Override protected Boolean validate(Boolean value) { return value; }
    @Override protected String encode(Boolean value) { return Boolean.toString(value); }

    @Override
    protected Boolean decode(String serialized) {
        if ("true".equalsIgnoreCase(serialized)) return true;
        if ("false".equalsIgnoreCase(serialized)) return false;
        throw new IllegalArgumentException("Invalid boolean setting: " + id());
    }
}
