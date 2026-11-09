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
    void moduleAndSettingRowsUseCompactSpacing() {
        SkyOPGuiLayout layout = SkyOPGuiLayout.calculate(480, 270);

        assertEquals(47, layout.moduleRowStep());
        assertEquals(42, layout.moduleCardHeight());
        assertEquals(36, layout.settingRowStep());
        assertEquals(66, layout.settingsTopOffset());
        assertEquals(27, layout.categoryRowY(1) - layout.categoryRowY(0));
    }
}
