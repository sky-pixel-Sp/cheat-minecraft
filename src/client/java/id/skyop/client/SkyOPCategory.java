package id.skyop.client;

/** Navigation groups and risk filters for the ClickGUI. SAFE and DANGEROUS are filters, not module types. */
public enum SkyOPCategory {
    COMBAT("Combat"), MOVEMENT("Movement"), VISUAL("Visual"), WORLD("World"),
    PLAYER("Player"), INVENTORY("Inventory"), FAVORITES("Favorites"),
    SAFE("Safe"), DANGEROUS("Dangerous");

    private final String fallbackTitle;

    SkyOPCategory(String fallbackTitle) {
        this.fallbackTitle = fallbackTitle;
    }

    public boolean isRiskFilter() {
        return this == SAFE || this == DANGEROUS;
    }

    /** Shared inclusion logic for module categories and UI filters. */
    public boolean includes(SkyOPModuleId module, boolean favorite) {
        if (module == null) return false;
        return switch (this) {
            case FAVORITES -> favorite;
            case SAFE -> !module.isDangerous();
            case DANGEROUS -> module.isDangerous();
            default -> module.category() == this;
        };
    }

    public String title() {
        String key = "category." + name().toLowerCase(java.util.Locale.ROOT);
        String translated = SkyOPLanguage.text(key);
        return translated.equals(key) ? fallbackTitle : translated;
    }
}
