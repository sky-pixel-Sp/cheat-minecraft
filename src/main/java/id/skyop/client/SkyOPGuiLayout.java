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

        // Keep the normal 8px outer margin, but never create a panel larger
        // than a scaled viewport smaller than that margin.
        int marginX = Math.min(8, Math.max(0, (safeScreenWidth - 1) / 2));
        int marginY = Math.min(8, Math.max(0, (safeScreenHeight - 1) / 2));
        int panelWidth = Math.max(1, Math.min(960, safeScreenWidth - marginX * 2));
        int panelHeight = Math.max(1, Math.min(590, safeScreenHeight - marginY * 2));
        int panelX = (safeScreenWidth - panelWidth) / 2;
        int panelY = (safeScreenHeight - panelHeight) / 2;
        int panelRight = panelX + panelWidth;
        int panelBottom = panelY + panelHeight;

        boolean compact = panelWidth < 700 || panelHeight < 430;
        int headerHeight = compact
                ? Math.min(42, Math.max(1, panelHeight / 4))
                : 56;
        int footerHeight = compact
                ? Math.min(32, Math.max(0, panelHeight / 6))
                : 42;
        headerHeight = Math.min(headerHeight, panelHeight);
        footerHeight = Math.min(footerHeight, Math.max(0, panelHeight - headerHeight));
        int footerY = panelBottom - footerHeight;

        int sidebarX;
        int sidebarY;
        int sidebarWidth;
        int contentX;
        int contentY;
        int contentWidth;
        int listTop;
        int listBottom;
        int inspectorX;
        int inspectorY;
        int inspectorWidth;
        int inspectorHeight;

        if (!compact) {
            sidebarX = panelX + 12;
            sidebarY = panelY + 93;
            sidebarWidth = 126;
            contentX = panelX + 151;
            contentY = panelY + 63;
            contentWidth = Math.max(1, panelWidth - 430);
            listTop = contentY + 76;
            listBottom = Math.max(listTop + 1, panelBottom - 58);
            inspectorX = panelRight - 263;
            inspectorY = panelY + 63;
            inspectorWidth = 248;
            inspectorHeight = Math.max(1, panelHeight - 121);
        } else {
            int leftInset = Math.min(7, Math.max(0, panelWidth - 1));
            int rightInset = Math.min(8, Math.max(0, panelWidth - 1));
            int innerWidth = Math.max(1, panelWidth - leftInset - rightInset);
            sidebarX = panelX + leftInset;
            sidebarWidth = Math.min(32, innerWidth);
            int contentGap = innerWidth > sidebarWidth + 1 ? 7 : 0;
            contentX = Math.min(panelRight - 1, sidebarX + sidebarWidth + contentGap);
            int contentRight = panelRight - rightInset;
            contentWidth = Math.max(1, contentRight - contentX);

            // Shrink the header/footer on tiny viewports. The 480x270 GUI
            // still uses the original 42/32 pixel compact chrome.
            int lastPanelY = panelBottom - 1;
            contentY = panelY + Math.min(lastPanelY - panelY, headerHeight + 1);
            sidebarY = panelY + Math.min(lastPanelY - panelY, headerHeight + 5);

            int bodyHeight = Math.max(1, panelBottom - contentY);
            int listOffset = Math.min(48, Math.max(1, bodyHeight / 3));
            listTop = Math.min(lastPanelY, contentY + listOffset);
            listBottom = Math.min(panelBottom, Math.max(listTop + 1, footerY - 6));

            inspectorX = contentX;
            inspectorY = contentY;
            inspectorWidth = contentWidth;
            int inspectorAvailableHeight = Math.max(1, panelBottom - inspectorY);
            inspectorHeight = Math.min(inspectorAvailableHeight,
                    Math.max(1, footerY - inspectorY - 6));
        }

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
        int row = Math.max(0, index);
        if (!compact) return sidebarY + row * 36;
        int step = compactCategoryRowStep();
        int lastPanelY = panelY + panelHeight - 1;
        int lastButtonY = Math.max(panelY, lastPanelY - categoryButtonHeight() + 1);
        return Math.min(lastButtonY, sidebarY + row * step);
    }

    private int compactCategoryRowStep() {
        int available = Math.max(1, footerY - sidebarY);
        int categoryCount = SkyOPCategory.values().length;
        return available >= categoryCount * 27 ? 27 : Math.max(1, available / categoryCount);
    }

    public int categoryButtonWidth() {
        return compact ? sidebarWidth : 116;
    }

    public int categoryButtonHeight() {
        return compact ? Math.min(23, compactCategoryRowStep()) : 30;
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

    /** Returns true only inside the visible inspector rectangle. */
    public boolean containsInspector(int mouseX, int mouseY) {
        return mouseX >= inspectorX && mouseX < inspectorX + inspectorWidth
                && mouseY >= inspectorY && mouseY < inspectorY + inspectorHeight;
    }

    /** Visible module-card viewport; excludes search, header and footer. */
    public boolean containsModuleList(int mouseX, int mouseY) {
        return mouseX >= contentX && mouseX < contentX + contentWidth
                && mouseY >= listTop && mouseY < listBottom;
    }

    /**
     * Resolve a setting click against the clipped inspector viewport.
     * Returns -1 for padding, clipped rows, and clicks outside the inspector.
     */
    public int visibleSettingIndexAt(int mouseX, int mouseY, int settingCount, int scrollOffset) {
        if (settingCount <= 0 || scrollOffset < 0 || !containsInspector(mouseX, mouseY)
                || mouseX < inspectorX + 8 || mouseX >= inspectorX + inspectorWidth - 8) return -1;
        int settingsTop = inspectorY + settingsTopOffset();
        int settingsBottom = inspectorY + inspectorHeight - 7;
        int visibleHeight = settingsBottom - settingsTop;
        if (visibleHeight <= 0 || mouseY < settingsTop || mouseY >= settingsBottom) return -1;

        int rowStep = settingRowStep();
        int visibleRows = visibleHeight / rowStep;
        int visibleRow = (mouseY - settingsTop) / rowStep;
        if (visibleRow < 0 || visibleRow >= visibleRows) return -1;
        int settingIndex = scrollOffset + visibleRow;
        return settingIndex < settingCount ? settingIndex : -1;
    }

    public int compactHeaderButtonWidth() {
        return Math.max(1, Math.min(22, (panelWidth - 6) / 3));
    }

    public int compactHeaderButtonHeight() {
        return Math.max(1, Math.min(22, panelHeight));
    }

    public int compactHeaderButtonY() {
        return panelY + Math.min(8, Math.max(0, panelHeight - compactHeaderButtonHeight()));
    }

    private int compactHeaderGap() {
        int unused = panelWidth - compactHeaderButtonWidth() * 3;
        return unused >= 6 ? 3 : Math.max(0, unused / 2);
    }

    public int colorButtonX() {
        if (!compact) return panelX + panelWidth - 378;
        return Math.max(panelX, hudButtonX() - compactHeaderButtonWidth() - compactHeaderGap());
    }

    public int hudButtonX() {
        if (!compact) return panelX + panelWidth - 320;
        return Math.max(panelX, closeButtonX() - compactHeaderButtonWidth() - compactHeaderGap());
    }

    public int closeButtonX() {
        if (!compact) return panelX + panelWidth - 48;
        return panelX + panelWidth - compactHeaderButtonWidth();
    }

    public int colorPickerWidth() {
        return Math.min(166, panelWidth);
    }

    public int colorPickerHeight() {
        return Math.min(104, panelHeight);
    }

    public boolean colorPickerFits() {
        return panelWidth >= 166 && panelHeight >= 104;
    }

    public int colorPickerX() {
        return panelX + Math.max(0, panelWidth - Math.min(174, panelWidth));
    }

    public int colorPickerY() {
        int desiredOffset = compact ? headerHeight + 2 : 42;
        return panelY + Math.min(desiredOffset, Math.max(0, panelHeight - colorPickerHeight()));
    }


    public enum ScrollTarget {
        NONE, INSPECTOR, MODULE_LIST
    }

    /**
     * In compact mode the inspector covers the module list. Resolve that
     * overlap before module scrolling so the overlay always owns the wheel.
     */
    public ScrollTarget scrollTargetAt(int mouseX, int mouseY, boolean inspectorOpen) {
        if (inspectorOpen && inspectorOverlay() && containsInspector(mouseX, mouseY)) {
            return ScrollTarget.INSPECTOR;
        }
        if (containsModuleList(mouseX, mouseY)) return ScrollTarget.MODULE_LIST;
        if (!inspectorOverlay() && containsInspector(mouseX, mouseY)) {
            return ScrollTarget.INSPECTOR;
        }
        return ScrollTarget.NONE;
    }

    private double compactFooterScale() {
        return Math.min(1.0, Math.max(0.0, (panelWidth - 16) / 448.0));
    }

    private int scaledFooterValue(int value) {
        return Math.max(1, (int) Math.round(value * compactFooterScale()));
    }

    public int compactFooterButtonHeight() {
        return Math.max(1, Math.min(20, footerHeight - 4));
    }

    public int compactFooterButtonY() {
        int height = compactFooterButtonHeight();
        int maximumY = panelY + panelHeight - height;
        int desiredY = footerY + Math.max(0, (footerHeight - height) / 2);
        return Math.max(panelY, Math.min(maximumY, desiredY));
    }

    public int compactPresetX(int index) {
        int offset = switch (index) {
            case 0 -> scaledFooterValue(10);
            case 1 -> scaledFooterValue(41);
            case 2 -> scaledFooterValue(72);
            default -> throw new IllegalArgumentException("Preset index must be 0, 1 or 2.");
        };
        return Math.min(panelX + panelWidth - 1, panelX + offset);
    }

    public int compactPresetWidth(int index) {
        int requested = switch (index) {
            case 0, 1 -> scaledFooterValue(27);
            case 2 -> scaledFooterValue(39);
            default -> throw new IllegalArgumentException("Preset index must be 0, 1 or 2.");
        };
        return Math.max(1, Math.min(requested, panelX + panelWidth - compactPresetX(index)));
    }

    public int compactLanguageX() {
        return Math.min(panelX + panelWidth - 1, panelX + scaledFooterValue(122));
    }

    public int compactLanguageWidth() {
        return Math.max(1, Math.min(scaledFooterValue(82),
                panelX + panelWidth - compactLanguageX()));
    }

    public int compactDisableWidth() {
        return Math.max(1, Math.min(scaledFooterValue(90), panelWidth));
    }

    public int compactDisableX() {
        return Math.max(panelX, Math.min(panelX + panelWidth - compactDisableWidth(),
                panelX + panelWidth - scaledFooterValue(98)));
    }
}
