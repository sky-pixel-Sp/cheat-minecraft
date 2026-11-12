package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPGuiLayoutTest {
    @Test
    void compactLayoutFits480By270GuiScaleFourViewport() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(480, 270);

        assertTrue(layout.compact());
        assertTrue(layout.iconSidebar());
        assertTrue(layout.inspectorOverlay());
        assertEquals(464, layout.panelWidth());
        assertEquals(254, layout.panelHeight());
        assertTrue(layout.panelX() >= 0);
        assertTrue(layout.panelY() >= 0);
        assertTrue(layout.panelX() + layout.panelWidth() <= 480);
        assertTrue(layout.panelY() + layout.panelHeight() <= 270);
        assertTrue(layout.contentWidth() > 0);
        assertTrue(layout.listBottom() > layout.listTop());
        assertTrue(layout.inspectorWidth() > 0);
        assertTrue(layout.inspectorHeight() > 0);
        assertTrue(layout.inspectorX() + layout.inspectorWidth()
                <= layout.panelX() + layout.panelWidth());
        assertTrue(layout.footerY() >= layout.listBottom());
        assertEquals(2, layout.moduleColumns());
        assertTrue(layout.moduleCardWidth() >= 150);
    }

    @Test
    void mediumLayoutFits854By480ViewportWithoutForcingMinimumPanelSize() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(854, 480);

        assertFalse(layout.compact());
        assertFalse(layout.iconSidebar());
        assertFalse(layout.inspectorOverlay());
        assertEquals(838, layout.panelWidth());
        assertEquals(464, layout.panelHeight());
        assertTrue(layout.panelX() + layout.panelWidth() <= 854);
        assertTrue(layout.panelY() + layout.panelHeight() <= 480);
        assertTrue(layout.contentX() + layout.contentWidth() < layout.inspectorX());
        assertTrue(layout.inspectorX() + layout.inspectorWidth()
                <= layout.panelX() + layout.panelWidth());
        assertTrue(layout.listBottom() > layout.listTop());
        assertTrue(layout.inspectorHeight() > 0);
        assertEquals(2, layout.moduleColumns());
    }

    @Test
    void largeViewportKeepsWideTwoPanelLayout() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(1920, 1080);

        assertFalse(layout.compact());
        assertEquals(960, layout.panelWidth());
        assertEquals(590, layout.panelHeight());
        assertEquals(248, layout.inspectorWidth());
        assertEquals(126, layout.sidebarWidth());
        assertTrue(layout.panelX() >= 0);
        assertTrue(layout.panelY() >= 0);
        assertTrue(layout.listBottom() < layout.footerY());
    }

    @Test
    void compactHeaderControlsCategoryRailAndColorPickerStayInside480By270Panel() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(480, 270);
        int headerWidth = layout.compactHeaderButtonWidth();

        assertTrue(layout.colorButtonX() >= layout.panelX());
        assertTrue(layout.colorButtonX() + headerWidth <= layout.hudButtonX());
        assertTrue(layout.hudButtonX() + headerWidth <= layout.closeButtonX());
        assertTrue(layout.closeButtonX() + headerWidth
                <= layout.panelX() + layout.panelWidth());
        assertTrue(layout.compactHeaderButtonY() + layout.compactHeaderButtonHeight()
                <= layout.panelY() + layout.panelHeight());

        assertTrue(layout.categoryRowY(5) + layout.categoryButtonHeight()
                <= layout.footerY());
        assertTrue(layout.colorPickerX() >= layout.panelX());
        assertTrue(layout.colorPickerX() + layout.colorPickerWidth()
                <= layout.panelX() + layout.panelWidth());
        assertTrue(layout.colorPickerY() >= layout.panelY());
        assertTrue(layout.colorPickerY() + layout.colorPickerHeight()
                <= layout.panelY() + layout.panelHeight());

        assertCompactFooterInsidePanel(layout);
    }

    @Test
    void wideHeaderControlsStayInside854By480Panel() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(854, 480);

        assertFalse(layout.compact());
        assertTrue(layout.colorButtonX() + 48 <= layout.hudButtonX());
        assertTrue(layout.hudButtonX() + 72 <= layout.panelX() + layout.panelWidth() - 172);
        assertTrue(layout.closeButtonX() + 30 <= layout.panelX() + layout.panelWidth());
        assertTrue(layout.colorPickerX() + layout.colorPickerWidth()
                <= layout.panelX() + layout.panelWidth());
        assertTrue(layout.colorPickerY() + layout.colorPickerHeight()
                <= layout.panelY() + layout.panelHeight());
    }

    @Test
    void verySmallViewportNeverProducesNegativeDimensionsOrOutOfBoundsPanel() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(320, 200);

        assertTrue(layout.compact());
        assertTrue(layout.panelWidth() >= 1);
        assertTrue(layout.panelHeight() >= 1);
        assertTrue(layout.contentWidth() >= 1);
        assertTrue(layout.inspectorWidth() >= 1);
        assertTrue(layout.inspectorHeight() >= 1);
        assertTrue(layout.panelX() + layout.panelWidth() <= 320);
        assertTrue(layout.panelY() + layout.panelHeight() <= 200);
        assertTrue(layout.moduleCardWidth() >= 1);
    }

    @Test
    void extremeViewportsKeepEveryLayoutRegionInsideTheActualPanel() {
        int[][] viewports = {
                {1, 1}, {2, 3}, {16, 16}, {80, 40},
                {160, 90}, {320, 200}, {480, 270}, {854, 480}
        };
        for (int[] viewport : viewports) {
            SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(viewport[0], viewport[1]);
            String label = viewport[0] + "x" + viewport[1];
            int right = layout.panelX() + layout.panelWidth();
            int bottom = layout.panelY() + layout.panelHeight();

            assertTrue(layout.panelX() >= 0, label);
            assertTrue(layout.panelY() >= 0, label);
            assertTrue(right <= viewport[0], label);
            assertTrue(bottom <= viewport[1], label);
            assertTrue(layout.headerHeight() >= 0 && layout.headerHeight() <= layout.panelHeight(), label);
            assertTrue(layout.footerHeight() >= 0
                    && layout.headerHeight() + layout.footerHeight() <= layout.panelHeight(), label);

            assertRectInside(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(),
                    layout.sidebarX(), layout.sidebarY(), layout.sidebarWidth(),
                    Math.max(1, Math.min(bottom, layout.footerY()) - layout.sidebarY()), label + " sidebar");
            assertRectInside(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(),
                    layout.contentX(), layout.contentY(), layout.contentWidth(),
                    Math.max(1, layout.listBottom() - layout.contentY()), label + " content");
            assertRectInside(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(),
                    layout.contentX(), layout.listTop(), layout.contentWidth(),
                    layout.listBottom() - layout.listTop(), label + " list");
            assertRectInside(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(),
                    layout.inspectorX(), layout.inspectorY(), layout.inspectorWidth(),
                    layout.inspectorHeight(), label + " inspector");

            if (layout.compact()) {
                int headerX = layout.colorButtonX();
                int headerY = layout.compactHeaderButtonY();
                int headerSize = layout.compactHeaderButtonWidth();
                int headerHeight = layout.compactHeaderButtonHeight();
                assertRectInside(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(),
                        headerX, headerY, headerSize, headerHeight, label + " color button");
                assertRectInside(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(),
                        layout.hudButtonX(), headerY, headerSize, headerHeight, label + " HUD button");
                assertRectInside(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(),
                        layout.closeButtonX(), headerY, headerSize, headerHeight, label + " close button");
                assertCompactFooterInsidePanel(layout, label);
            }
            assertTrue(layout.colorPickerWidth() >= 1 && layout.colorPickerWidth() <= layout.panelWidth(), label);
            assertTrue(layout.colorPickerHeight() >= 1 && layout.colorPickerHeight() <= layout.panelHeight(), label);
            assertRectInside(layout.panelX(), layout.panelY(), layout.panelWidth(), layout.panelHeight(),
                    layout.colorPickerX(), layout.colorPickerY(),
                    layout.colorPickerWidth(), layout.colorPickerHeight(), label + " color picker");
        }
    }

    @Test
    void compactInspectorHasScrollPriorityOverOverlappingModuleList() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(480, 270);
        int mouseX = layout.contentX() + 20;
        int mouseY = layout.listTop() + 2;

        assertTrue(layout.containsInspector(mouseX, mouseY));
        assertTrue(layout.containsModuleList(mouseX, mouseY));
        assertEquals(SkyOPGuiLayout.ScrollTarget.INSPECTOR,
                layout.scrollTargetAt(mouseX, mouseY, true));
        assertEquals(SkyOPGuiLayout.ScrollTarget.MODULE_LIST,
                layout.scrollTargetAt(mouseX, mouseY, false));
    }

    @Test
    void settingHitboxesRespectVisibleRowsAndInspectorClipping() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(480, 270);
        int x = layout.inspectorX() + 12;
        int firstRowY = layout.inspectorY() + layout.settingsTopOffset() + 1;
        int secondRowY = firstRowY + layout.settingRowStep();

        assertEquals(0, layout.visibleSettingIndexAt(x, firstRowY, 8, 0));
        assertEquals(1, layout.visibleSettingIndexAt(x, secondRowY, 8, 0));
        assertEquals(1, layout.visibleSettingIndexAt(x, firstRowY, 8, 1));
        assertEquals(-1, layout.visibleSettingIndexAt(layout.inspectorX() + 2, firstRowY, 8, 0));
        assertEquals(-1, layout.visibleSettingIndexAt(x,
                layout.inspectorY() + layout.inspectorHeight() - 1, 8, 0));
        assertEquals(-1, layout.visibleSettingIndexAt(x, firstRowY, 0, 0));
    }

    @Test
    void compactFooterButtonsAreClampedForNarrowPanels() {
        for (int[] viewport : new int[][]{{1, 1}, {16, 16}, {80, 40}, {160, 90}, {480, 270}}) {
            SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(viewport[0], viewport[1]);
            if (!layout.compact()) continue;
            assertCompactFooterInsidePanel(layout, viewport[0] + "x" + viewport[1]);
        }
    }

    @Test
    void compactCategoryRailFitsAllCategoriesAndRiskFilters() {
        for (int[] viewport : new int[][]{{320, 200}, {480, 270}}) {
            SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(viewport[0], viewport[1]);
            assertTrue(layout.compact());
            int previousBottom = layout.sidebarY();
            for (int index = 0; index < SkyOPCategory.values().length; index++) {
                int y = layout.categoryRowY(index);
                int height = layout.categoryButtonHeight();
                assertTrue(y >= previousBottom, viewport[0] + "x" + viewport[1] + " category " + index);
                assertTrue(y + height <= layout.footerY(),
                        viewport[0] + "x" + viewport[1] + " category " + index + " must fit above footer");
                previousBottom = y + height;
            }
        }
        assertTrue(SkyOPCategory.SAFE.isRiskFilter());
        assertTrue(SkyOPCategory.DANGEROUS.isRiskFilter());
        assertTrue(!SkyOPCategory.WORLD.isRiskFilter());
    }

    @Test
    void moduleAndSettingRowsUseCompactSpacing() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(480, 270);

        assertEquals(47, layout.moduleRowStep());
        assertEquals(42, layout.moduleCardHeight());
        assertEquals(36, layout.settingRowStep());
        assertEquals(66, layout.settingsTopOffset());
        assertEquals(layout.categoryButtonHeight(), layout.categoryRowY(1) - layout.categoryRowY(0));
    }

    private static void assertCompactFooterInsidePanel(SkyOPGuiLayout layout) {
        assertCompactFooterInsidePanel(layout, "480x270");
    }

    private static void assertCompactFooterInsidePanel(SkyOPGuiLayout layout, String label) {
        int top = layout.compactFooterButtonY();
        int height = layout.compactFooterButtonHeight();
        int left = layout.panelX();
        int right = left + layout.panelWidth();
        int bottom = layout.panelY() + layout.panelHeight();

        assertTrue(top >= layout.panelY() && top + height <= bottom, label + " footer y");
        for (int index = 0; index < 3; index++) {
            int x = layout.compactPresetX(index);
            int width = layout.compactPresetWidth(index);
            assertTrue(x >= left && x + width <= right, label + " preset " + index);
        }
        assertTrue(layout.compactLanguageX() >= left
                && layout.compactLanguageX() + layout.compactLanguageWidth() <= right, label + " language");
        assertTrue(layout.compactDisableX() >= left
                && layout.compactDisableX() + layout.compactDisableWidth() <= right, label + " disable");
    }

    private static void assertRectInside(int panelX, int panelY, int panelWidth, int panelHeight,
                                         int x, int y, int width, int height, String label) {
        assertTrue(width >= 1, label + " width");
        assertTrue(height >= 1, label + " height");
        assertTrue(x >= panelX, label + " left");
        assertTrue(y >= panelY, label + " top");
        assertTrue(x + width <= panelX + panelWidth, label + " right");
        assertTrue(y + height <= panelY + panelHeight, label + " bottom");
    }
}
