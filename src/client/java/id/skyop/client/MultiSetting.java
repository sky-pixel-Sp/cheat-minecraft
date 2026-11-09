package id.skyop.client;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MultiSetting extends SkyOPSetting<Set<String>> {
    private final Set<String> options;
    private final Set<String> defaultValue;

    public MultiSetting(String id, String title, String description, Set<String> options,
                        Set<String> defaultValue, Supplier<Set<String>> getter,
                        Consumer<Set<String>> setter) {
        super(id, title, description, getter, setter);
        this.options = Set.copyOf(options);
        this.defaultValue = validateSet(defaultValue);
    }

    public Set<String> options() { return options; }
    @Override protected Set<String> defaultValue() { return defaultValue; }

    @Override
    protected Set<String> validate(Set<String> value) {
        return validateSet(value);
    }

    private Set<String> validateSet(Set<String> value) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String option : value) {
            if (options.contains(option)) result.add(option);
        }
        return Set.copyOf(result);
    }

    @Override
    protected String encode(Set<String> value) {
        return String.join(",", value);
    }

    @Override
    protected Set<String> decode(String serialized) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (!serialized.isBlank()) {
            for (String token : serialized.split(",")) {
                if (options.contains(token)) result.add(token);
            }
        }
        return Set.copyOf(result);
    }
}
