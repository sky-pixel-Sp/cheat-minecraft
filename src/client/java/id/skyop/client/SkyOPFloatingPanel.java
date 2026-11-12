package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Responsive in-world quick switcher. Every control maps to a real SkyOP action:
 * module toggles, theme cycling, opening the full ClickGUI, and disabling all modules.
 */
public final class SkyOPFloatingPanel extends Screen {
    private static final int MAX_PANEL_WIDTH = 310;
    private static final int MAX_PANEL_HEIGHT = 292;
    private static final int HEADER_HEIGHT = 38;
    private static final int CARD_TOP = 46;
    private static final int CARD_HEIGHT = 40;
    private static final int CARD_STEP = 44;
    private static final int FOOTER_HEIGHT = 30;

    private static final List<SkyOPModuleId> QUICK_MODULES = List.of(
            SkyOPModuleId.AUTO_TOTEM,
            SkyOPModuleId.AUTO_MLG,
            SkyOPModuleId.FAST_BREAK,
            SkyOPModuleId.AUTO_SPRINT,
            SkyOPModuleId.ENTITY_ESP,
            SkyOPModuleId.AUTO_EAT,
            SkyOPModuleId.ZOOM,
            SkyOPModuleId.ARMOR_ALERT
    );

    private int panelX, panelY, panelWidth, panelHeight, columns, visibleCount, cardWidth;
    private int dragOffsetX, dragOffsetY;
    private boolean dragging;

    public SkyOPFloatingPanel() {
        super(Component.literal("SkyOP Quick Panel"));
    }

    @Override
    protected void init() {
        calculatePanelSize();
        panelX = SkyOPState.quickPanelX < 0
                ? Math.max(8, width - panelWidth - 16) : SkyOPState.quickPanelX;
        panelY = SkyOPState.quickPanelY < 0
                ? Math.max(8, (height - panelHeight) / 2) : SkyOPState.quickPanelY;
        clampPanel();
    }

    private void calculatePanelSize() {
        panelWidth = Math.max(1, Math.min(MAX_PANEL_WIDTH, width - 16));
        panelHeight = Math.max(1, Math.min(MAX_PANEL_HEIGHT, height - 16));
        columns = panelWidth >= 270 ? 2 : 1;
        cardWidth = Math.max(1, (panelWidth - 24 - (columns - 1) * 8) / columns);
        int availableCardHeight = Math.max(0, panelHeight - CARD_TOP - FOOTER_HEIGHT - 8);
        int visibleRows = Math.max(1, availableCardHeight / CARD_STEP);
        visibleCount = Math.min(QUICK_MODULES.size(), visibleRows * columns);
    }

    private void clampPanel() {
        panelX = SkyOPMath.clamp(panelX, 0, Math.max(0, width - panelWidth));
        panelY = SkyOPMath.clamp(panelY, 0, Math.max(0, height - panelHeight));
    }

    private int footerY() {
        return panelY + panelHeight - FOOTER_HEIGHT;
    }

    private int cardX(int index) {
        return panelX + 12 + (index % columns) * (cardWidth + 8);
    }

    private int cardY(int index) {
        return panelY + CARD_TOP + (index / columns) * CARD_STEP;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        calculatePanelSize();
        clampPanel();

        SkyOPTheme.Palette palette = SkyOPTheme.palette(SkyOPState.uiTheme);
        boolean idLanguage = "id".equals(SkyOPLanguage.code());
        graphics.fill(0, 0, width, height, 0x48000000);
        graphics.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2,
                palette.hover());
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, palette.panel());
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 3, palette.accent());
        graphics.fill(panelX, panelY + 3, panelX + panelWidth, panelY + HEADER_HEIGHT,
                palette.elevated());

        String title = idLanguage ? "SKYOP  /  PANEL CEPAT" : "SKYOP  /  QUICK ACCESS";
        graphics.text(font, Component.literal(title), panelX + 12, panelY + 14, palette.text(), true);
        String count = visibleCount + "/" + QUICK_MODULES.size();
        int closeX = panelX + panelWidth - 25;
        int themeX = closeX - 34;
        graphics.text(font, Component.literal(count), Math.max(panelX + 104, themeX - font.width(count) - 7),
                panelY + 15, palette.muted(), false);

        boolean themeHovered = inside(mouseX, mouseY, themeX, panelY + 8, 28, 20);
        graphics.fill(themeX, panelY + 8, themeX + 28, panelY + 28,
                themeHovered ? palette.hover() : palette.panel());
        graphics.text(font, Component.literal("◐"), themeX + 9, panelY + 13, palette.accent(), true);

        boolean closeHovered = inside(mouseX, mouseY, closeX, panelY + 8, 19, 20);
        graphics.fill(closeX, panelY + 8, closeX + 19, panelY + 28,
                closeHovered ? palette.hover() : palette.panel());
        graphics.text(font, Component.literal("×"), closeX + 6, panelY + 12, palette.text(), true);

        for (int index = 0; index < visibleCount; index++) {
            SkyOPModuleId module = QUICK_MODULES.get(index);
            int x = cardX(index), y = cardY(index);
            boolean enabled = SkyOPCore.isEnabled(module);
            boolean hovered = inside(mouseX, mouseY, x, y, cardWidth, CARD_HEIGHT);
            graphics.fill(x, y, x + cardWidth, y + CARD_HEIGHT,
                    hovered ? palette.hover() : palette.elevated());
            graphics.fill(x, y, x + 3, y + CARD_HEIGHT,
                    enabled ? palette.accent() : palette.muted());
            String label = trim(module.title(), Math.max(9, (cardWidth - 52) / 6));
            graphics.text(font, Component.literal(label), x + 9, y + 8, palette.text(), true);
            String status = enabled ? (idLanguage ? "AKTIF" : "ON") : (idLanguage ? "MATI" : "OFF");
            int statusX = x + 9;
            graphics.text(font, Component.literal(status), statusX, y + 24,
                    enabled ? palette.success() : palette.muted(), true);
            int indicatorX = x + cardWidth - 18;
            graphics.fill(indicatorX, y + 14, indicatorX + 8, y + 22,
                    enabled ? palette.success() : palette.muted());
        }

        int footer = footerY();
        graphics.fill(panelX + 10, footer - 7, panelX + panelWidth - 10, footer - 7 + 1,
                palette.hover());
        int buttonGap = 8;
        int buttonWidth = Math.max(1, (panelWidth - 32 - buttonGap) / 2);
        int leftX = panelX + 12;
        int rightX = leftX + buttonWidth + buttonGap;
        boolean guiHovered = inside(mouseX, mouseY, leftX, footer, buttonWidth, 22);
        boolean disableHovered = inside(mouseX, mouseY, rightX, footer, buttonWidth, 22);
        graphics.fill(leftX, footer, leftX + buttonWidth, footer + 22,
                guiHovered ? palette.hover() : palette.elevated());
        graphics.fill(rightX, footer, rightX + buttonWidth, footer + 22,
                disableHovered ? palette.danger() : palette.elevated());
        String fullGui = idLanguage ? "BUKA GUI" : "FULL GUI";
        String disable = idLanguage ? "MATIKAN SEMUA" : "DISABLE ALL";
        graphics.text(font, Component.literal(trim(fullGui, Math.max(6, (buttonWidth - 8) / 6))),
                leftX + 5, footer + 7, palette.text(), true);
        graphics.text(font, Component.literal(trim(disable, Math.max(6, (buttonWidth - 8) / 6))),
                rightX + 5, footer + 7, disableHovered ? 0xFFFFFFFF : palette.danger(), true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.input() != InputConstants.MOUSE_BUTTON_LEFT) return super.mouseClicked(event, doubleClick);
        int mx = (int) event.x(), my = (int) event.y();

        int closeX = panelX + panelWidth - 25;
        int themeX = closeX - 34;
        if (inside(mx, my, closeX, panelY + 8, 19, 20)) {
            onClose();
            return true;
        }
        if (inside(mx, my, themeX, panelY + 8, 28, 20)) {
            SkyOPCore.cycleUiTheme();
            return true;
        }

        int footer = footerY();
        int buttonGap = 8;
        int buttonWidth = Math.max(1, (panelWidth - 32 - buttonGap) / 2);
        int leftX = panelX + 12;
        int rightX = leftX + buttonWidth + buttonGap;
        if (inside(mx, my, leftX, footer, buttonWidth, 22)) {
            Minecraft.getInstance().gui.setScreen(new SkyOPClickGui());
            return true;
        }
        if (inside(mx, my, rightX, footer, buttonWidth, 22)) {
            SkyOPCore.disableAll(Minecraft.getInstance());
            return true;
        }

        for (int index = 0; index < visibleCount; index++) {
            int x = cardX(index), y = cardY(index);
            if (inside(mx, my, x, y, cardWidth, CARD_HEIGHT)) {
                SkyOPCore.toggle(Minecraft.getInstance(), QUICK_MODULES.get(index));
                return true;
            }
        }

        if (inside(mx, my, panelX, panelY, panelWidth, HEADER_HEIGHT)) {
            dragging = true;
            dragOffsetX = mx - panelX;
            dragOffsetY = my - panelY;
            return true;
        }
        return inside(mx, my, panelX, panelY, panelWidth, panelHeight)
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
