package id.skyop.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class NumberSetting extends SkyOPSetting<Double> {
    private final SkyOPSettingsSchema.NumberRule rule;

    public NumberSetting(String id, String title, String description,
                         SkyOPSettingsSchema.NumberRule rule,
                         Supplier<Double> getter, Consumer<Double> setter) {
        super(id, title, description, getter, setter);
        if (rule == null) throw new IllegalArgumentException("A numeric schema rule is required for " + id);
        this.rule = rule;
    }

    public double minimum() { return rule.minimum(); }
    public double maximum() { return rule.maximum(); }
    public boolean integer() { return rule.integer(); }

    @Override protected Double defaultValue() { return rule.defaultValue(); }
    @Override protected Double validate(Double value) { return rule.validate(value); }
    @Override protected String encode(Double value) { return Double.toString(value); }

    @Override
    protected Double decode(String serialized) {
        try {
            double value = Double.parseDouble(serialized);
            return Double.isFinite(value) ? rule.validate(value) : rule.defaultValue();
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("Invalid number setting: " + id(), invalid);
        }
    }
}
