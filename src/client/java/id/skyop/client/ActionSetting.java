package id.skyop.client;

/** A module-owned operation represented as a non-persistent registry setting. */
public final class ActionSetting extends SkyOPSetting<String> {
    @FunctionalInterface
    public interface Operation {
        void run() throws Exception;
    }

    private final String suffix;
    private final Operation operation;

    public ActionSetting(String id, String title, String description,
                         String suffix, Operation operation) {
        super(id, title, description, () -> suffix == null ? "" : suffix, ignored -> { });
        this.suffix = suffix == null ? "" : suffix;
        this.operation = java.util.Objects.requireNonNull(operation, "operation");
    }

    public void execute() throws Exception {
        operation.run();
    }

    @Override protected String defaultValue() { return suffix; }
    @Override protected String validate(String value) { return suffix; }
    @Override protected String encode(String value) { return suffix; }
    @Override protected String decode(String serialized) { return suffix; }
}
