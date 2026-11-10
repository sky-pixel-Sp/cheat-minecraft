package id.skyop.client;

/**
 * Pure geometry for the ClickGUI. Coordinates are Minecraft GUI-space pixels,
 * not framebuffer pixels; every rectangle is bounded by the current viewport.
 */
public final class SkyOPGuiLayout {
    public record Rect(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }
        public boolean contains(int px, int py) {
            return px >= x && px < right() && py >= y && py < bottom();
        }
    }

    public record Layout(
            int screenWidth, int screenHeight,
            int panelX, int panelY, int panelWidth, int panelHeight,
            int headerHeight, int footerHeight,
            Rect sidebar, Rect moduleList, Rect inspector,
            boolean compact, boolean iconSidebar, boolean inspectorOverlay,
            boolean foldedHeaderTools
    ) {
        public Rect panel() { return new Rect(panelX, panelY, panelWidth, panelHeight); }
        public int bodyTop() { return panelY + headerHeight; }
        public int bodyBottom() { return panelY + panelHeight - footerHeight; }
    }

    private SkyOPGuiLayout() { }

    public static Layout calculate(int screenWidth, int screenHeight) {
        int sw = Math.max(1, screenWidth);
        int sh = Math.max(1, screenHeight);
        int marginX = Math.min(12, Math.max(0, (sw - 1) / 2));
        int marginY = Math.min(10, Math.max(0, (sh - 1) / 2));
        int panelWidth = Math.max(1, Math.min(960, sw - marginX * 2));
        int panelHeight = Math.max(1, Math.min(590, sh - marginY * 2));
        int panelX = (sw - panelWidth) / 2;
        int panelY = (sh - panelHeight) / 2;
        boolean compact = panelWidth < 680 || panelHeight < 450;

        int headerHeight = compact
                ? Math.min(52, Math.max(24, panelHeight / 4))
                : 62;
        int footerHeight = compact
                ? Math.min(34, Math.max(16, panelHeight / 7))
                : 38;
        headerHeight = Math.min(headerHeight, panelHeight);
        footerHeight = Math.min(footerHeight, Math.max(0, panelHeight - headerHeight));
        int bodyTop = panelY + headerHeight;
        int bodyBottom = Math.max(bodyTop, panelY + panelHeight - footerHeight);
        int bodyHeight = Math.max(1, bodyBottom - bodyTop);

        Rect sidebar;
        Rect moduleList;
        Rect inspector;
        if (!compact) {
            sidebar = new Rect(panelX + 12, panelY + 62, 126, Math.max(1, panelHeight - 110));
            moduleList = new Rect(panelX + 151, panelY + 63,
                    Math.max(1, panelWidth - 430), Math.max(1, panelHeight - 121));
            inspector = new Rect(panelX + panelWidth - 263, panelY + 63,
                    248, Math.max(1, panelHeight - 121));
        } else {
            int inset = Math.min(8, Math.max(0, panelWidth - 1));
            int sidebarWidth = Math.min(Math.max(18, panelWidth / 12), Math.max(1, panelWidth - 1));
            int sidebarX = panelX + inset;
            int bodyY = Math.min(panelY + panelHeight - 1, bodyTop + 2);
            int compactBodyHeight = Math.max(1, Math.min(bodyHeight - 4,
                    panelY + panelHeight - footerHeight - bodyY));
            sidebar = new Rect(sidebarX, bodyY, sidebarWidth, compactBodyHeight);

            int listX = Math.min(panelX + panelWidth - 1, sidebar.right() + 6);
            int right = Math.max(listX + 1, panelX + panelWidth - inset);
            moduleList = new Rect(listX, bodyY, Math.max(1, right - listX), compactBodyHeight);

            int inspectorWidth = Math.min(248, Math.max(1, panelWidth - inset * 2));
            int inspectorX = Math.max(panelX, panelX + panelWidth - inset - inspectorWidth);
            inspector = new Rect(inspectorX, bodyY, inspectorWidth, compactBodyHeight);
        }

        return new Layout(sw, sh, panelX, panelY, panelWidth, panelHeight,
                headerHeight, footerHeight, sidebar, moduleList, inspector,
                compact, compact, compact, compact);
    }
}
