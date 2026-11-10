package id.skyop.client;

/** Editable bounded text value exposed through the module setting registry. */
public final class TextSetting extends SkyOPSetting<String> {
    private final String defaultText;
    private final int maximumLength;

    public TextSetting(String id, String title, String description,
                       String defaultText, int maximumLength,
                       java.util.function.Supplier<String> getter,
                       java.util.function.Consumer<String> setter) {
        super(id, title, description, getter, setter);
        if (maximumLength < 1) throw new IllegalArgumentException("maximumLength must be positive");
        this.defaultText = defaultText == null ? "" : defaultText;
        this.maximumLength = maximumLength;
    }

    public int maximumLength() { return maximumLength; }

    @Override protected String defaultValue() { return defaultText; }

    @Override
    protected String validate(String value) {
        String safe = value == null ? "" : value;
        return safe.length() <= maximumLength ? safe : safe.substring(0, maximumLength);
    }

    @Override protected String encode(String value) { return value; }
    @Override protected String decode(String serialized) { return serialized; }
}
