package id.skyop.client;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central range schema for persisted numeric settings. Values are validated on load and before
 * they are applied by the GUI; unknown properties are ignored by the feature code.
 */
public final class SkyOPSettingsSchema {
    public record NumberRule(double defaultValue, double minimum, double maximum, boolean integer) {
        public double validate(double value) {
            if (!Double.isFinite(value)) return defaultValue;
            double clamped = Math.max(minimum, Math.min(maximum, value));
            return integer ? Math.rint(clamped) : clamped;
        }
    }

    private static final Map<String, NumberRule> NUMBERS = new LinkedHashMap<>();

    static {
        number("auraRange", 4.2, 2.5, 6.0, false);
        number("auraFov", 360, 1, 360, true);
        number("auraRotationSpeed", 0.42, 0.10, 1.0, false);
        number("auraDelayTicks", 8, 1, 20, true);
        number("triggerDelayTicks", 3, 1, 12, true);
        number("autoClickerCps", 12, 4, 20, true);
        number("speedMultiplier", 1.7, 1.0, 2.8, false);
        number("flightSpeed", 0.38, 0.12, 0.9, false);
        number("autoJumpDelayTicks", 4, 2, 12, true);
        number("spiderClimbSpeed", 0.22, 0.08, 0.38, false);
        number("espRange", 48, 8, 96, false);
        number("autoEatThreshold", 14, 2, 18, true);
        number("autoRefillThreshold", 2, 1, 16, true);
        number("autoHuntRange", 4.5, 2.5, 6.0, false);
        number("jumpBoostMultiplier", 1.35, 1.0, 3.0, false);
        number("fastBreakLevel", 7, 1, 7, true);
        number("autoMlgFallDistance", 6, 3, 18, false);
        number("autoTotemHealth", 14, 1, 20, false);
        number("bridgeDelayTicks", 2, 1, 10, true);
        number("zoomFov", 20, 10, 70, true);
        number("containerFinderRange", 16, 4, 24, true);
        number("lowHealthAlertThreshold", 8, 1, 20, true);
        number("armorAlertPercent", 15, 1, 50, true);
    }

    private SkyOPSettingsSchema() { }

    private static void number(String id, double value, double min, double max, boolean integer) {
        NUMBERS.put(id, new NumberRule(value, min, max, integer));
    }

    public static NumberRule rule(String id) { return NUMBERS.get(id); }

    public static double validate(String id, double value, double fallback) {
        NumberRule rule = NUMBERS.get(id);
        return rule == null ? fallback : rule.validate(value);
    }

    public static Map<String, NumberRule> numbers() { return Map.copyOf(NUMBERS); }
}
