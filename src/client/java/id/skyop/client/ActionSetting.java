package id.skyop.client;

import java.util.Objects;
import java.util.function.Supplier;

/** A button-like action exposed through a module's registered setting list. */
public final class ActionSetting extends SkyOPSetting<Boolean> {
    @FunctionalInterface
    public interface Action {
        void execute() throws Exception;
    }

    private final Supplier<String> buttonLabel;
    private final Action action;

    public ActionSetting(String id, String title, String description,
                         Supplier<String> buttonLabel, Action action) {
        super(id, title, description, () -> Boolean.FALSE, ignored -> { });
        this.buttonLabel = Objects.requireNonNull(buttonLabel, "buttonLabel");
        this.action = Objects.requireNonNull(action, "action");
    }

    public String buttonLabel() { return buttonLabel.get(); }
    public void activate() throws Exception { action.execute(); }

    @Override protected Boolean defaultValue() { return Boolean.FALSE; }
    @Override protected Boolean validate(Boolean value) { return Boolean.FALSE; }
    @Override protected String encode(Boolean value) { return "false"; }
    @Override protected Boolean decode(String serialized) { return Boolean.FALSE; }
}
