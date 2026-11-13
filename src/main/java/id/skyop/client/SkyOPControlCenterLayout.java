package id.skyop.client;

/**
 * Responsive bounds for the SkyOP Control Center.
 *
 * This class is deliberately independent from Minecraft so the sizing rules
 * can be tested at several GUI scales without booting a game client.
 */
public record SkyOPControlCenterLayout(
        int panelX,
        int panelY,
        int panelWidth,
        int panelHeight,
        int sidebarWidth,
        int contentX,
        int contentY,
        int contentWidth,
        int contentHeight,
        boolean compact,
        boolean micro
) {
    public static SkyOPControlCenterLayout calculate(int screenWidth, int screenHeight) {
        int safeWidth = Math.max(1, screenWidth);
        int safeHeight = Math.max(1, screenHeight);
        int marginX = Math.min(8, Math.max(0, (safeWidth - 1) / 2));
        int marginY = Math.min(8, Math.max(0, (safeHeight - 1) / 2));
        int width = Math.max(1, Math.min(1_040, safeWidth - marginX * 2));
        int height = Math.max(1, Math.min(700, safeHeight - marginY * 2));
        int x = Math.max(0, (safeWidth - width) / 2);
        int y = Math.max(0, (safeHeight - height) / 2);

        boolean compact = width < 760 || height < 510;
        boolean micro = width < 460 || height < 290;
        int sidebar = micro ? 34 : compact ? 54 : 172;
        int contentX = x + sidebar + (micro ? 7 : compact ? 12 : 22);
        int contentY = y + (micro ? 38 : 58);
        int contentWidth = Math.max(1, x + width - contentX - (micro ? 7 : 18));
        int contentHeight = Math.max(1, y + height - contentY - (micro ? 36 : 52));

        return new SkyOPControlCenterLayout(
                x, y, width, height, sidebar, contentX, contentY,
                contentWidth, contentHeight, compact, micro
        );
    }

    public boolean contains(int mouseX, int mouseY, int x, int y, int width, int height) {
        return width > 0 && height > 0
                && mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }
}
