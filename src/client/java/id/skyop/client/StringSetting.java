package id.skyop.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A bounded, persistent text setting edited by the generic module inspector. */
public final class StringSetting extends SkyOPSetting<String> {
    private final String defaultValue;
    private final int maximumLength;

    public StringSetting(String id, String title, String description, int maximumLength,
                         Supplier<String> getter, Consumer<String> setter) {
        super(id, title, description, getter, setter);
        if (maximumLength < 1) throw new IllegalArgumentException("maximumLength must be positive");
        this.maximumLength = maximumLength;
        this.defaultValue = validate(getter.get());
    }

    public int maximumLength() { return maximumLength; }

    @Override protected String defaultValue() { return defaultValue; }

    @Override
    protected String validate(String value) {
        if (value == null) throw new IllegalArgumentException("Text setting cannot be null.");
        if (value.length() > maximumLength) {
            throw new IllegalArgumentException("Text setting exceeds " + maximumLength + " characters.");
        }
        return value;
    }

    @Override
    protected String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected String decode(String serialized) {
        try {
            return validate(new String(Base64.getUrlDecoder().decode(serialized), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException("Invalid text setting: " + id(), invalid);
        }
    }
}
