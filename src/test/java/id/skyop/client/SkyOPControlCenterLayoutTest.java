package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPControlCenterLayoutTest {
    @Test
    void standardViewportKeepsAReadableSidebarAndPositiveContent() {
        SkyOPControlCenterLayout layout = SkyOPControlCenterLayout.calculate(1920, 1080);
        assertEquals(172, layout.sidebarWidth());
        assertFalse(layout.compact());
        assertFalse(layout.micro());
        assertTrue(layout.contentWidth() > 500);
        assertTrue(layout.contentHeight() > 300);
    }

    @Test
    void compactViewportUsesSmallerNavigation() {
        SkyOPControlCenterLayout layout = SkyOPControlCenterLayout.calculate(854, 480);
        assertTrue(layout.compact());
        assertEquals(54, layout.sidebarWidth());
        assertTrue(layout.contentWidth() > 0);
        assertTrue(layout.contentHeight() > 0);
    }

    @Test
    void microViewportNeverProducesNegativeOrOutOfScreenPanelBounds() {
        for (int width = 1; width <= 500; width += 13) {
            for (int height = 1; height <= 320; height += 11) {
                SkyOPControlCenterLayout layout = SkyOPControlCenterLayout.calculate(width, height);
                assertTrue(layout.panelX() >= 0);
                assertTrue(layout.panelY() >= 0);
                assertTrue(layout.panelWidth() >= 1);
                assertTrue(layout.panelHeight() >= 1);
                assertTrue(layout.panelX() + layout.panelWidth() <= width);
                assertTrue(layout.panelY() + layout.panelHeight() <= height);
                assertTrue(layout.sidebarWidth() > 0);
                assertTrue(layout.contentWidth() > 0);
                assertTrue(layout.contentHeight() > 0);
            }
        }
    }

    @Test
    void hitTestingUsesHalfOpenBounds() {
        SkyOPControlCenterLayout layout = SkyOPControlCenterLayout.calculate(854, 480);
        assertTrue(layout.contains(10, 10, 10, 10, 20, 20));
        assertFalse(layout.contains(30, 20, 10, 10, 20, 20));
        assertFalse(layout.contains(9, 10, 10, 10, 20, 20));
    }
}
