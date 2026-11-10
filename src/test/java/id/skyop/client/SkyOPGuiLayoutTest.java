package id.skyop.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkyOPGuiLayoutTest {
    @Test
    void compactLayoutFits480By270WithoutMinimumSizeClamping() {
        SkyOPGuiLayout.Layout layout = SkyOPGuiLayout.calculate(480, 270);

        assertEquals(456, layout.panelWidth());
        assertEquals(250, layout.panelHeight());
        assertTrue(layout.compact());
        assertTrue(layout.iconSidebar());
        assertTrue(layout.inspectorOverlay());
        assertTrue(layout.foldedHeaderTools());
        assertRectInside(layout.panel(), layout.sidebar());
        assertRectInside(layout.panel(), layout.moduleList());
        assertRectInside(layout.panel(), layout.inspector());
        assertTrue(layout.moduleList().width() > 0);
        assertTrue(layout.moduleList().height() > 0);
    }

    @Test
    void standardLayoutKeepsSeparateSidebarModuleListAndInspectorAt854By480() {
        SkyOPGuiLayout.Layout layout = SkyOPGuiLayout.calculate(854, 480);

        assertEquals(830, layout.panelWidth());
        assertEquals(460, layout.panelHeight());
        assertFalse(layout.compact());
        assertFalse(layout.iconSidebar());
        assertFalse(layout.inspectorOverlay());
        assertFalse(layout.foldedHeaderTools());
        assertTrue(layout.sidebar().right() < layout.moduleList().x());
        assertTrue(layout.moduleList().right() < layout.inspector().x());
        assertRectInside(layout.panel(), layout.sidebar());
        assertRectInside(layout.panel(), layout.moduleList());
        assertRectInside(layout.panel(), layout.inspector());
    }

    @Test
    void viewportBelowRequestedMinimumRemainsBoundedAndNonNegative() {
        SkyOPGuiLayout.Layout layout = SkyOPGuiLayout.calculate(160, 90);

        assertEquals(136, layout.panelWidth());
        assertEquals(70, layout.panelHeight());
        assertTrue(layout.compact());
        assertRectInside(layout.panel(), layout.sidebar());
        assertRectInside(layout.panel(), layout.moduleList());
        assertRectInside(layout.panel(), layout.inspector());
        assertTrue(layout.headerHeight() >= 0);
        assertTrue(layout.footerHeight() >= 0);
    }

    @Test
    void onePixelViewportDoesNotCreateNegativeGeometry() {
        SkyOPGuiLayout.Layout layout = SkyOPGuiLayout.calculate(1, 1);

        assertEquals(1, layout.panelWidth());
        assertEquals(1, layout.panelHeight());
        assertRectInside(layout.panel(), layout.sidebar());
        assertRectInside(layout.panel(), layout.moduleList());
        assertRectInside(layout.panel(), layout.inspector());
    }

    private static void assertRectInside(SkyOPGuiLayout.Rect bounds, SkyOPGuiLayout.Rect child) {
        assertTrue(child.x() >= bounds.x(), () -> "left edge outside: " + child);
        assertTrue(child.y() >= bounds.y(), () -> "top edge outside: " + child);
        assertTrue(child.right() <= bounds.right(), () -> "right edge outside: " + child);
        assertTrue(child.bottom() <= bounds.bottom(), () -> "bottom edge outside: " + child);
        assertTrue(child.width() > 0);
        assertTrue(child.height() > 0);
    }
}
