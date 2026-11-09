package id.skyop.client;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Typed, serializable setting bound to the live value owned by a module or client state.
 * The ClickGUI can be replaced without duplicating setting validation or persistence rules.
 */
public abstract class SkyOPSetting<T> {
    private final String id;
    private final String title;
    private final String description;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    protected SkyOPSetting(String id, String title, String description,
                           Supplier<T> getter, Consumer<T> setter) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Setting id is required.");
        this.id = id;
        this.title = Objects.requireNonNull(title, "title");
        this.description = Objects.requireNonNull(description, "description");
        this.getter = Objects.requireNonNull(getter, "getter");
        this.setter = Objects.requireNonNull(setter, "setter");
    }

    public final String id() { return id; }
    public final String title() { return resolveText(title); }
    public final String description() { return resolveText(description); }

    private static String resolveText(String value) {
        return value.startsWith("lang:") ? SkyOPLanguage.text(value.substring(5)) : value;
    }
    public final T value() { return getter.get(); }

    public final void set(T value) {
        setter.accept(validate(Objects.requireNonNull(value, "value")));
    }

    public final String serialize() {
        return encode(value());
    }

    public final void deserialize(String serialized) {
        if (serialized == null) {
            set(defaultValue());
            return;
        }
        try {
            set(decode(serialized));
        } catch (IllegalArgumentException invalid) {
            set(defaultValue());
        }
    }

    protected abstract T defaultValue();
    protected abstract T validate(T value);
    protected abstract String encode(T value);
    protected abstract T decode(String serialized);
}
