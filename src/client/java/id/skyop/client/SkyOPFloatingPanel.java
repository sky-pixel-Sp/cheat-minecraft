package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Non-pausing, draggable in-world quick switcher for six frequently used modules. */
public final class SkyOPFloatingPanel extends Screen {
    private static final int PANEL_WIDTH = 286;
    private static final int PANEL_HEIGHT = 246;
    private static final List<SkyOPModuleId> QUICK_MODULES = List.of(
            SkyOPModuleId.AUTO_TOTEM, SkyOPModuleId.AUTO_MLG, SkyOPModuleId.FAST_BREAK,
            SkyOPModuleId.AUTO_SPRINT, SkyOPModuleId.ENTITY_ESP, SkyOPModuleId.AUTO_EAT
    );

    private int panelX, panelY, dragOffsetX, dragOffsetY;
    private boolean dragging;

    public SkyOPFloatingPanel() {
        super(Component.literal("SkyOP Quick Panel"));
    }

    @Override
    protected void init() {
        panelX = SkyOPState.quickPanelX < 0 ? Math.max(8, width - PANEL_WIDTH - 24) : SkyOPState.quickPanelX;
        panelY = SkyOPState.quickPanelY < 0 ? Math.max(8, (height - PANEL_HEIGHT) / 2) : SkyOPState.quickPanelY;
        clampPanel();
    }

    private void clampPanel() {
        panelX = SkyOPMath.clamp(panelX, 0, Math.max(0, width - PANEL_WIDTH));
        panelY = SkyOPMath.clamp(panelY, 0, Math.max(0, height - PANEL_HEIGHT));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        SkyOPTheme.Palette palette = SkyOPTheme.palette(SkyOPState.uiTheme);
        boolean idLanguage = "id".equals(SkyOPLanguage.code());
        graphics.fill(0, 0, width, height, 0x48000000);
        graphics.fill(panelX - 2, panelY - 2, panelX + PANEL_WIDTH + 2, panelY + PANEL_HEIGHT + 2, palette.hover());
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, palette.panel());
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 3, palette.accent());
        graphics.fill(panelX, panelY + 3, panelX + PANEL_WIDTH, panelY + 38, palette.elevated());

        String title = idLanguage ? "SKYOP  /  PANEL CEPAT" : "SKYOP  /  QUICK PANEL";
        graphics.text(font, Component.literal(title), panelX + 14, panelY + 14, palette.text(), true);
        int closeX = panelX + PANEL_WIDTH - 27;
        boolean closeHovered = inside(mouseX, mouseY, closeX, panelY + 8, 19, 19);
        graphics.fill(closeX, panelY + 8, closeX + 19, panelY + 27, closeHovered ? palette.hover() : palette.panel());
        graphics.text(font, Component.literal("×"), closeX + 6, panelY + 12, palette.text(), true);

        for (int index = 0; index < QUICK_MODULES.size(); index++) {
            SkyOPModuleId module = QUICK_MODULES.get(index);
            int x = panelX + 12 + (index % 2) * 131;
            int y = panelY + 48 + (index / 2) * 54;
            boolean enabled = SkyOPCore.isEnabled(module);
            boolean hovered = inside(mouseX, mouseY, x, y, 125, 46);
            graphics.fill(x, y, x + 125, y + 46, hovered ? palette.hover() : palette.elevated());
            graphics.fill(x, y, x + 3, y + 46, enabled ? palette.accent() : palette.muted());
            graphics.text(font, Component.literal(trim(module.title(), 17)), x + 10, y + 9, palette.text(), true);
            String status = enabled ? (idLanguage ? "AKTIF" : "ON") : (idLanguage ? "MATI" : "OFF");
            graphics.text(font, Component.literal(status), x + 10, y + 27,
                    enabled ? palette.success() : palette.muted(), true);
        }

        String hint = idLanguage ? "Geser judul untuk memindahkan  •  ESC untuk menutup"
                : "Drag the title to move  •  ESC to close";
        graphics.text(font, Component.literal(hint), panelX + 12,
                panelY + PANEL_HEIGHT - 17, palette.muted(), true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.input() != InputConstants.MOUSE_BUTTON_LEFT) return super.mouseClicked(event, doubleClick);
        int mx = (int) event.x(), my = (int) event.y();
        int closeX = panelX + PANEL_WIDTH - 27;
        if (inside(mx, my, closeX, panelY + 8, 19, 19)) {
            onClose();
            return true;
        }
        if (inside(mx, my, panelX, panelY, PANEL_WIDTH, 38)) {
            dragging = true;
            dragOffsetX = mx - panelX;
            dragOffsetY = my - panelY;
            return true;
        }
        for (int index = 0; index < QUICK_MODULES.size(); index++) {
            int x = panelX + 12 + (index % 2) * 131;
            int y = panelY + 48 + (index / 2) * 54;
            if (inside(mx, my, x, y, 125, 46)) {
                SkyOPCore.toggle(Minecraft.getInstance(), QUICK_MODULES.get(index));
                return true;
            }
        }
        return inside(mx, my, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT)
                || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (!dragging) return super.mouseDragged(event, deltaX, deltaY);
        panelX = (int) event.x() - dragOffsetX;
        panelY = (int) event.y() - dragOffsetY;
        clampPanel();
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!dragging) return super.mouseReleased(event);
        dragging = false;
        savePosition();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        dragging = false;
        savePosition();
        super.onClose();
    }

    private void savePosition() {
        SkyOPState.quickPanelX = panelX;
        SkyOPState.quickPanelY = panelY;
        SkyOPCore.saveConfig();
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return w > 0 && h > 0 && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static String trim(String value, int maxLength) {
        if (value == null || maxLength <= 0) return "";
        if (value.length() <= maxLength) return value;
        if (maxLength <= 3) return value.substring(0, maxLength);
        return value.substring(0, maxLength - 3) + "...";
    }
}
