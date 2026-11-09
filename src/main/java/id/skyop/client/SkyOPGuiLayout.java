package id.skyop.client;

/**
 * Pure layout calculation for the SkyOP ClickGUI.
 *
 * All coordinates are expressed in Minecraft GUI-scaled pixels. In compact
 * mode the category rail becomes icon-only and the inspector is displayed as
 * an overlay over the module browser.
 */
public record SkyOPGuiLayout(
        int screenWidth,
        int screenHeight,
        int panelX,
        int panelY,
        int panelWidth,
        int panelHeight,
        boolean compact,
        int headerHeight,
        int footerHeight,
        int sidebarX,
        int sidebarY,
        int sidebarWidth,
        int contentX,
        int contentY,
        int contentWidth,
        int listTop,
        int listBottom,
        int inspectorX,
        int inspectorY,
        int inspectorWidth,
        int inspectorHeight,
        int footerY
) {
    public static SkyOPGuiLayout calculate(int screenWidth, int screenHeight) {
        int safeScreenWidth = Math.max(1, screenWidth);
        int safeScreenHeight = Math.max(1, screenHeight);

        int panelWidth = Math.max(1, Math.min(960, safeScreenWidth - 16));
        int panelHeight = Math.max(1, Math.min(590, safeScreenHeight - 16));
        int panelX = Math.max(0, (safeScreenWidth - panelWidth) / 2);
        int panelY = Math.max(0, (safeScreenHeight - panelHeight) / 2);

        boolean compact = panelWidth < 700 || panelHeight < 430;
        int headerHeight = compact ? 42 : 56;
        int footerHeight = compact ? 32 : 42;
        int sidebarWidth = compact ? 32 : 126;
        int sidebarX = panelX + (compact ? 7 : 12);
        int sidebarY = panelY + (compact ? 47 : 93);
        int contentX = panelX + (compact ? 46 : 151);
        int contentY = panelY + (compact ? 43 : 63);
        int contentWidth = Math.max(1, panelWidth - (compact ? 54 : 430));
        int listTop = contentY + (compact ? 48 : 76);
        int footerY = panelY + panelHeight - footerHeight;
        int listBottom = Math.max(listTop + 1,
                panelY + panelHeight - (compact ? footerHeight + 6 : 58));

        int inspectorX = compact ? contentX : panelX + panelWidth - 263;
        int inspectorY = compact ? contentY : panelY + 63;
        int inspectorWidth = compact ? contentWidth : 248;
        int inspectorHeight = Math.max(1, compact
                ? footerY - inspectorY - 6
                : panelHeight - 121);

        return new SkyOPGuiLayout(
                safeScreenWidth, safeScreenHeight,
                panelX, panelY, panelWidth, panelHeight,
                compact, headerHeight, footerHeight,
                sidebarX, sidebarY, sidebarWidth,
                contentX, contentY, contentWidth,
                listTop, listBottom,
                inspectorX, inspectorY, inspectorWidth, inspectorHeight,
                footerY
        );
    }

    public boolean iconSidebar() {
        return compact;
    }

    public boolean inspectorOverlay() {
        return compact;
    }

    public int categoryRowY(int index) {
        return sidebarY + index * (compact ? 27 : 36);
    }

    public int categoryButtonWidth() {
        return compact ? sidebarWidth : 116;
    }

    public int categoryButtonHeight() {
        return compact ? 23 : 30;
    }

    public int moduleColumns() {
        return contentWidth >= 320 ? 2 : 1;
    }

    public int moduleCardWidth() {
        int columns = moduleColumns();
        return Math.max(1, (contentWidth - (columns - 1) * 8) / columns);
    }

    public int moduleCardHeight() {
        return compact ? 42 : 52;
    }

    public int moduleRowStep() {
        return compact ? 47 : 60;
    }

    public int settingRowStep() {
        return compact ? 36 : 42;
    }

    public int settingsTopOffset() {
        return compact ? 66 : 70;
    }

    public int colorButtonX() {
        return panelX + panelWidth - (compact ? 86 : 378);
    }

    public int hudButtonX() {
        return panelX + panelWidth - (compact ? 60 : 320);
    }

    public int closeButtonX() {
        return panelX + panelWidth - (compact ? 30 : 48);
    }

    public int colorPickerX() {
        return Math.max(panelX + 6, panelX + panelWidth - 174);
    }

    public int colorPickerY() {
        return panelY + (compact ? headerHeight + 2 : 42);
    }
}
