package id.skyop.client;

/** UI module categories. */
public enum SkyOPCategory {
    COMBAT("Combat"), MOVEMENT("Movement"), VISUAL("Visual"), PLAYER("Player"),
    INVENTORY("Inventory"), FAVORITES("Favorites");

    private final String fallbackTitle;

    SkyOPCategory(String fallbackTitle) {
        this.fallbackTitle = fallbackTitle;
    }

    public String title() {
        String key = "category." + name().toLowerCase(java.util.Locale.ROOT);
        String translated = SkyOPLanguage.text(key);
        return translated.equals(key) ? fallbackTitle : translated;
    }
}
