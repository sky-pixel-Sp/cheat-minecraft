package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsive, two-column SkyOP module manager for Minecraft 26.3.
 */
public final class SkyOPClickGui extends Screen {
    private static final int PANEL = 0xF0141B29;
    private static final int PANEL_LIGHT = 0xFF202B3D;
    private static final int PANEL_HOVER = 0xFF293A50;
    private static final int ACCENT = 0xFF61C7F2;
    private static final int TEXT = 0xFFF1F6FB;
    private static final int MUTED = 0xFF9AAABD;
    private static final int GREEN = 0xFF57D69A;
    private static final int RED = 0xFFFF7885;

    private SkyOPCategory selectedCategory = SkyOPCategory.COMBAT;
    private SkyOPModuleId selectedModule = SkyOPModuleId.KILL_AURA;
    private int panelX, panelY, panelWidth, panelHeight;
    private SkyOPGuiLayout.Layout layout;
    private boolean inspectorOpen, headerToolsOpen;
    private String searchQuery = "";
    private boolean searchFocused, waitingForKeybind, hudEditMode, draggingHud, colorPickerOpen, editingHudText;
    private boolean editingHudColor;
    private String hexColorInput = "";
    private SkyOPModuleId draggingHudModule;
    private int moduleScroll, settingsScroll, sidebarScroll, hudDragOffsetX, hudDragOffsetY;
    private record HudPreview(SkyOPModuleId module, int x, int y, int width, int height, String label) { }
    private long animationTicks;
    private String profileStatus = "";
    private int profileStatusTicks;

    public SkyOPClickGui() {
        super(Component.literal("SkyOP Client"));
    }

    @Override
    protected void init() {
        calculateLayout();
        selectFirstModule();
    }

    private void calculateLayout() {
        layout = SkyOPGuiLayout.calculate(width, height);
        panelX = layout.panelX();
        panelY = layout.panelY();
        panelWidth = layout.panelWidth();
        panelHeight = layout.panelHeight();
    }

        private void selectFirstModule() {
        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            if (module.category() == selectedCategory) {
                selectedModule = module;
                return;
            }
        }
        selectedModule = null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        calculateLayout();
        graphics.fill(0, 0, width, height, 0xB8000712);
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFF31445A);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 3, ACCENT);
        drawHeader(graphics, mouseX, mouseY);
        drawSidebar(graphics, mouseX, mouseY);
        drawModuleList(graphics, mouseX, mouseY);
        drawInspector(graphics, mouseX, mouseY);
        drawFooter(graphics, mouseX, mouseY);
        if (layout.compact()) drawCompactToolsOverlay(graphics);
        drawColorPicker(graphics, mouseX, mouseY);
        drawHudEditorPreview(graphics, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        animationTicks++;
        if (profileStatusTicks > 0) profileStatusTicks--;
    }

    private void drawHeader(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (layout.compact()) {
            drawCompactHeader(g, mouseX, mouseY);
            return;
        }
        g.text(font, Component.literal(SkyOPLanguage.text("gui.brand")), panelX + 20, panelY + 14, ACCENT, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.client")), panelX + 112, panelY + 14, MUTED, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.control_center")), panelX + 20, panelY + 34, TEXT, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.subtitle")), panelX + 210, panelY + 34, MUTED, true);

        if (panelWidth >= 800) {
            int colorX = panelX + panelWidth - 378;
            int hudEditX = panelX + panelWidth - 320;
            boolean colorHovered = inside(mouseX, mouseY, colorX, panelY + 12, 48, 24);
            g.fill(colorX, panelY + 12, colorX + 48, panelY + 36, colorHovered ? PANEL_HOVER : PANEL_LIGHT);
            g.fill(colorX + 16, panelY + 20, colorX + 32, panelY + 28, SkyOPCore.hudAccentColor());
            boolean editHovered = inside(mouseX, mouseY, hudEditX, panelY + 12, 72, 24);
            g.fill(hudEditX, panelY + 12, hudEditX + 72, panelY + 36, editHovered || hudEditMode ? 0xFF263D53 : PANEL_LIGHT);
            g.text(font, Component.literal(SkyOPLanguage.text(hudEditMode ? "gui.hud_edit_on" : "gui.hud_edit")),
                    hudEditX + 7, panelY + 20, hudEditMode ? GREEN : ACCENT, true);
        }

        int statusX = panelX + panelWidth - 172;
        g.fill(statusX, panelY + 13, statusX + 104, panelY + 35, PANEL_LIGHT);
        g.fill(statusX + 9, panelY + 21, statusX + 15, panelY + 27, GREEN);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.ready")), statusX + 21, panelY + 20, TEXT, true);
        int closeX = panelX + panelWidth - 48;
        g.fill(closeX, panelY + 12, closeX + 30, panelY + 35, PANEL_LIGHT);
        g.text(font, Component.literal("×"), closeX + 11, panelY + 16, TEXT, true);
    }

    private int compactHeaderButtonWidth() {
        return Math.min(20, Math.max(8, (panelWidth - 11) / 2));
    }

    private int compactCloseX() {
        return panelX + panelWidth - compactHeaderButtonWidth() - 4;
    }

    private int compactToolsX() {
        return compactCloseX() - compactHeaderButtonWidth() - 3;
    }

    private void drawCompactHeader(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int y = panelY + 7, buttonWidth = compactHeaderButtonWidth();
        g.text(font, Component.literal(SkyOPLanguage.text("gui.brand")), panelX + 3, y, ACCENT, true);
        if (panelWidth >= 250) g.text(font, Component.literal("SKYOP CLIENT"), panelX + 67, y, MUTED, true);
        int moreX = compactToolsX(), closeX = compactCloseX();
        g.fill(moreX, panelY + 5, moreX + buttonWidth, panelY + 24, headerToolsOpen ? PANEL_HOVER : PANEL_LIGHT);
        g.text(font, Component.literal("⋯"), moreX + Math.max(1,(buttonWidth-10)/2), panelY + 8, ACCENT, true);
        g.fill(closeX, panelY + 5, closeX + buttonWidth, panelY + 24, PANEL_LIGHT);
        g.text(font, Component.literal("×"), closeX + Math.max(1,(buttonWidth-8)/2), panelY + 8, TEXT, true);
    }

    private void drawCompactToolsOverlay(GuiGraphicsExtractor g) {
        if (!headerToolsOpen) return;
        int px = Math.max(panelX, panelX + panelWidth - 116), py = panelY + layout.headerHeight() - 2;
        int pw = Math.min(108, panelWidth - (px - panelX));
        g.fill(px, py, px + pw, py + 48, 0xF5101726);
        g.fill(px, py, px + pw, py + 1, 0xFF40546B);
        g.fill(px, py + 47, px + pw, py + 48, 0xFF40546B);
        g.fill(px, py, px + 1, py + 48, 0xFF40546B);
        g.fill(px + pw - 1, py, px + pw, py + 48, 0xFF40546B);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.hud_edit")), px + 7, py + 8, hudEditMode ? GREEN : TEXT, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.hud_color")), px + 7, py + 29, ACCENT, true);
    }

    private String categoryIcon(SkyOPCategory category) {
        return switch (category) {
            case COMBAT -> "C";
            case MOVEMENT -> "M";
            case VISUAL -> "V";
            case PLAYER -> "P";
            case INVENTORY -> "I";
            case FAVORITES -> "★";
        };
    }

    private int compactCategoryRowHeight() {
        return Math.max(14, Math.min(25, layout.sidebar().height() / Math.max(1, SkyOPCategory.values().length)));
    }

    private void drawCompactSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        SkyOPGuiLayout.Rect rect = layout.sidebar();
        g.fill(rect.x(), rect.y(), rect.right(), rect.bottom(), 0xFF111A27);
        int rowH = compactCategoryRowHeight(), index = 0;
        for (SkyOPCategory category : SkyOPCategory.values()) {
            int rowY = rect.y() + index * rowH - sidebarScroll * rowH;
            if (rowY + rowH > rect.y() && rowY < rect.bottom()) {
                boolean selected = category == selectedCategory;
                boolean hovered = inside(mouseX, mouseY, rect.x(), rowY, rect.width(), rowH);
                g.fill(rect.x() + 2, rowY + 1, rect.right() - 2, rowY + rowH - 1,
                        selected ? 0xFF263B50 : (hovered ? PANEL_HOVER : 0xFF111A27));
                if (selected) g.fill(rect.x() + 1, rowY + 1, rect.x() + 3, rowY + rowH - 1, ACCENT);
                String icon = categoryIcon(category);
                int tx = rect.x() + Math.max(3, (rect.width() - font.width(icon)) / 2);
                g.text(font, Component.literal(icon), tx, rowY + Math.max(2, (rowH - 8) / 2), selected ? TEXT : MUTED, true);
            }
            index++;
        }
    }

    private void drawCompactModuleList(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        SkyOPGuiLayout.Rect rect = layout.moduleList();
        int x = rect.x(), y = rect.y(), w = rect.width();
        g.text(font, Component.literal(trim(selectedCategory.title().toUpperCase(), 18)), x + 2, y + 2, TEXT, true);
        if (rect.height() <= 20 || w <= 8) return;
        int searchY = y + Math.min(17, Math.max(2, rect.height() / 3));
        int searchHeight = Math.min(18, Math.max(10, rect.height() - (searchY - y) - 1));
        g.fill(x, searchY, x + w, searchY + searchHeight, searchFocused ? 0xFF263D53 : PANEL_LIGHT);
        String search = searchQuery.isEmpty() ? SkyOPLanguage.text("gui.search")
                : SkyOPLanguage.text("gui.search_prefix") + ": " + searchQuery + (searchFocused ? "|" : "");
        g.text(font, Component.literal(trim(search, Math.max(8, w / 6))), x + 5, searchY + Math.max(1, (searchHeight - 8) / 2), searchFocused ? TEXT : MUTED, true);
        int listTop = y + (rect.height() >= 56 ? 41 : searchY - y + searchHeight + 3);
        int listBottom = rect.bottom() - 2;
        if (listBottom <= listTop || w <= 8) return;
        List<SkyOPModuleId> modules = modulesForCategory();
        int columns = w >= 270 ? 2 : 1, gap = 5;
        int cardWidth = Math.max(1, (w - (columns - 1) * gap) / columns);
        int cardHeight = rect.height() >= 80 ? 38 : Math.max(10, Math.min(24, listBottom - listTop - 1));
        int rowStep = cardHeight + 4;
        int visibleHeight = listBottom - listTop;
        int rows = (modules.size() + columns - 1) / columns;
        moduleScroll = Math.min(moduleScroll, Math.max(0, rows * rowStep - visibleHeight + 2));
        g.enableScissor(x, listTop, rect.right(), listBottom);
        int index = 0;
        for (SkyOPModuleId module : modules) {
            int col = index % columns, row = index / columns;
            int cardX = x + col * (cardWidth + gap), cardY = listTop + row * rowStep - moduleScroll;
            boolean enabled = SkyOPCore.isEnabled(module), selected = module == selectedModule;
            boolean hovered = inside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight);
            g.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, selected ? 0xFF263D53 : (hovered ? PANEL_HOVER : PANEL_LIGHT));
            if (selected) g.fill(cardX, cardY, cardX + 2, cardY + cardHeight, ACCENT);
            g.text(font, Component.literal(trim(module.title(), Math.max(8, cardWidth / 6))), cardX + 5, cardY + 5, TEXT, true);
            int pillW = Math.min(30, Math.max(18, cardWidth / 4)), pillX = cardX + cardWidth - pillW - 4;
            int pillY = cardY + Math.max(1, cardHeight - 15);
            g.fill(pillX, pillY, pillX + pillW, Math.min(listBottom, pillY + 13), enabled ? 0xFF174633 : 0xFF3B2631);
            g.text(font, Component.literal(enabled ? "ON" : "OFF"), pillX + 2, pillY + 3, enabled ? GREEN : RED, false);
            int favoriteX = cardX + cardWidth - pillW - 21;
            g.text(font, Component.literal(SkyOPCore.isFavorite(module) ? "★" : "☆"), favoriteX, cardY + 2,
                    SkyOPCore.isFavorite(module) ? 0xFFFFC85A : MUTED, true);
            index++;
        }
        g.disableScissor();
        if (modules.isEmpty()) g.text(font, Component.literal(SkyOPLanguage.text("gui.no_matching_modules")), x + 4, listTop + 6, MUTED, true);
    }

    private int compactFooterButtonHeight() {
        return Math.max(1, Math.min(20, layout.footerHeight() - 4));
    }

    private void drawCompactPresetButton(GuiGraphicsExtractor g, int mouseX, int mouseY,
                                         int x, int y, int w, int h, String label) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, h);
        g.fill(x, y, x + w, y + h, hovered ? PANEL_HOVER : PANEL_LIGHT);
        g.text(font, Component.literal(label), x + Math.max(1, (w - font.width(label)) / 2),
                y + Math.max(0, (h - 8) / 2), hovered ? TEXT : ACCENT, true);
    }

    private void drawCompactFooter(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int y = panelY + panelHeight - layout.footerHeight();
        g.fill(panelX + 5, y, panelX + panelWidth - 5, y + 1, 0xFF2B394A);
        boolean labels = panelWidth >= 280;
        int buttonHeight = compactFooterButtonHeight();
        int start = panelX + 7, btnY = y + Math.max(0, (layout.footerHeight() - buttonHeight) / 2);
        String[] names = labels ? new String[]{"PVE", "OP", "VIS"} : new String[]{"P", "O", "V"};
        int[] widths = labels ? new int[]{34, 30, 38} : new int[]{18, 18, 18};
        int cursor = start;
        for (int n = 0; n < 3; n++) {
            drawCompactPresetButton(g, mouseX, mouseY, cursor, btnY, widths[n], buttonHeight, names[n]);
            cursor += widths[n] + 3;
        }
        int langWidth = labels ? 82 : 22, langX = cursor + 2;
        if (langX + langWidth > panelX + panelWidth - 24) langX = panelX + panelWidth - langWidth - 25;
        boolean langHover = inside(mouseX, mouseY, langX, btnY, langWidth, buttonHeight);
        g.fill(langX, btnY, langX + langWidth, btnY + buttonHeight, langHover ? PANEL_HOVER : PANEL_LIGHT);
        String lang = labels ? SkyOPLanguage.current().code().toUpperCase(java.util.Locale.ROOT) : "L";
        g.text(font, Component.literal(lang), langX + Math.max(3, (langWidth - font.width(lang)) / 2),
                btnY + Math.max(0,(buttonHeight - 8) / 2), langHover ? TEXT : ACCENT, true);
        int disableWidth = labels ? 54 : 19, disableX = panelX + panelWidth - disableWidth - 7;
        boolean disableHover = inside(mouseX, mouseY, disableX, btnY, disableWidth, buttonHeight);
        g.fill(disableX, btnY, disableX + disableWidth, btnY + buttonHeight, disableHover ? 0xFF693542 : 0xFF482B35);
        String disableLabel = labels ? "OFF" : "×";
        g.text(font, Component.literal(disableLabel), disableX + Math.max(3, (disableWidth - font.width(disableLabel)) / 2),
                btnY + Math.max(0,(buttonHeight - 8) / 2), 0xFFFFB8C0, true);
        if (!profileStatus.isEmpty() && profileStatusTicks > 0 && panelWidth >= 320)
            g.text(font, Component.literal(trim(profileStatus, 24)), panelX + panelWidth / 2 - 36, btnY + 6, TEXT, true);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (layout.compact()) {
            drawCompactSidebar(g, mouseX, mouseY);
            return;
        }
        int x = layout.sidebar().x(), y = layout.sidebar().y(), w = layout.sidebar().width();
        g.fill(x, y, x + w, panelY + panelHeight - 48, 0xFF111A27);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.categories")), x + 10, y + 10, MUTED, true);
        int index = 0;
        for (SkyOPCategory category : SkyOPCategory.values()) {
            int rowY = y + 31 + index * 36;
            boolean selected = category == selectedCategory;
            boolean hovered = inside(mouseX, mouseY, x + 5, rowY, w - 10, 30);
            int background = selected ? 0xFF263B50 : (hovered ? PANEL_HOVER : 0xFF111A27);
            g.fill(x + 5, rowY, x + w - 5, rowY + 30, background);
            if (selected) g.fill(x + 5, rowY, x + 8, rowY + 30, ACCENT);
            g.text(font, Component.literal(category.title()), x + 15, rowY + 10,
                    selected ? TEXT : MUTED, true);
            g.text(font, Component.literal(Integer.toString(countModules(category))),
                    x + w - 25, rowY + 10, selected ? ACCENT : MUTED, true);
            index++;
        }

        int activeY = panelY + panelHeight - 98;
        g.fill(x + 5, activeY, x + w - 5, activeY + 1, 0xFF2B394A);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.active_modules")), x + 10, activeY + 12, MUTED, true);
        g.text(font, Component.literal(Integer.toString(SkyOPCore.enabledModuleCount())),
                x + 10, activeY + 30, ACCENT, true);
    }

    private void drawModuleList(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (layout.compact()) {
            drawCompactModuleList(g, mouseX, mouseY);
            return;
        }
        int x = layout.moduleList().x(), y = layout.moduleList().y(), w = layout.moduleList().width();
        g.text(font, Component.literal(selectedCategory.title().toUpperCase() + " " + SkyOPLanguage.text("gui.modules")),
                x, y + 8, TEXT, true);
        int searchY = y + 27;
        g.fill(x, searchY, x + w, searchY + 22, searchFocused ? 0xFF263D53 : PANEL_LIGHT);
        g.text(font, Component.literal(searchQuery.isEmpty()
                        ? SkyOPLanguage.text("gui.search") + "  (" + SkyOPLanguage.text("gui.type_to_filter") + ")"
                        : SkyOPLanguage.text("gui.search_prefix") + ": " + searchQuery + (searchFocused ? "|" : "")),
                x + 8, searchY + 7, searchFocused ? TEXT : MUTED, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.left_toggle_right_inspect")),
                x, y + 54, MUTED, true);

        List<SkyOPModuleId> modules = modulesForCategory();
        int cardWidth = (w - 8) / 2;
        int listTop = y + 76;
        int listBottom = panelY + panelHeight - 58;
        int visibleHeight = Math.max(80, listBottom - listTop);
        int rowCount = (modules.size() + 1) / 2;
        int maxScroll = Math.max(0, rowCount * 60 - visibleHeight + 10);
        moduleScroll = Math.min(moduleScroll, maxScroll);
        g.enableScissor(x, listTop, x + w, listBottom);
        int index = 0;
        SkyOPModuleId hoveredModule = null;
        for (SkyOPModuleId module : modules) {
            int col = index % 2;
            int row = index / 2;
            int cardX = x + col * (cardWidth + 8);
            int cardY = listTop + row * 60 - moduleScroll;
            boolean enabled = SkyOPCore.isEnabled(module);
            boolean selected = module == selectedModule;
            boolean hovered = inside(mouseX, mouseY, cardX, cardY, cardWidth, 52);
            g.fill(cardX, cardY, cardX + cardWidth, cardY + 52,
                    selected ? 0xFF263D53 : (hovered ? PANEL_HOVER : PANEL_LIGHT));
            if (selected) g.fill(cardX, cardY, cardX + 3, cardY + 52, ACCENT);
            g.text(font, Component.literal(trim(module.title(), 19)),
                    cardX + 10, cardY + 8, TEXT, true);
            g.text(font, Component.literal(trim(module.description(), 24)),
                    cardX + 10, cardY + 25, MUTED, true);

            int favoriteX = cardX + cardWidth - 74;
            g.text(font, Component.literal(SkyOPCore.isFavorite(module) ? "★" : "☆"),
                    favoriteX, cardY + 8, SkyOPCore.isFavorite(module) ? 0xFFFFC85A : MUTED, true);
            int pillX = cardX + cardWidth - 48;
            g.fill(pillX, cardY + 15, pillX + 40, cardY + 36,
                    enabled ? 0xFF174633 : 0xFF3B2631);
            g.text(font, Component.literal(enabled ? SkyOPLanguage.text("gui.on") : SkyOPLanguage.text("gui.off")),
                    pillX + 10, cardY + 22, enabled ? GREEN : RED, true);
            if (hovered) hoveredModule = module;
            index++;
        }
        g.disableScissor();
        if (modules.isEmpty()) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.no_matching_modules")), x + 8, listTop + 12, MUTED, true);
        }
        if (hoveredModule != null) {
            int alpha = 190 + (int) (40 * Math.sin(animationTicks * 0.18));
            int tooltipColor = (Math.min(255, alpha) << 24) | 0x00101726;
            g.fill(x, listBottom + 2, x + w, listBottom + 28, tooltipColor);
            g.text(font, Component.literal(hoveredModule.description()), x + 7, listBottom + 10, TEXT, true);
        }
    }

    private void drawInspector(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (layout.compact()) {
            if (inspectorOpen) {
                SkyOPGuiLayout.Rect rect = layout.inspector();
                drawInspectorPanel(g, mouseX, mouseY, rect.x(), rect.y(), rect.width(), rect.height(), true);
            }
            return;
        }
        SkyOPGuiLayout.Rect rect = layout.inspector();
        drawInspectorPanel(g, mouseX, mouseY, rect.x(), rect.y(), rect.width(), rect.height(), false);
    }

    /** Every module inspector row is rendered from that module's registered typed settings. */
    private void drawInspectorPanel(GuiGraphicsExtractor g, int mouseX, int mouseY,
                                    int x, int y, int w, int h, boolean compact) {
        int pad = compact ? 7 : 12;
        g.fill(x, y, x + w, y + h, compact ? 0xF5111A27 : 0xFF111A27);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.inspector")), x + pad, y + (compact ? 6 : 10), MUTED, true);
        if (compact) {
            g.fill(x + w - 23, y + 4, x + w - 5, y + 22, PANEL_LIGHT);
            g.text(font, Component.literal("×"), x + w - 17, y + 8, TEXT, true);
        }
        if (selectedModule == null) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.select_module")), x + pad, y + (compact ? 29 : 42), TEXT, true);
            return;
        }
        boolean enabled = SkyOPCore.isEnabled(selectedModule);
        int titleY = y + (compact ? 24 : 31);
        g.text(font, Component.literal(trim(selectedModule.title(), compact ? 20 : 28)), x + pad, titleY, TEXT, true);
        if (!compact) {
            g.text(font, Component.literal(enabled ? "● " + SkyOPLanguage.text("gui.enabled") : "○ " + SkyOPLanguage.text("gui.disabled")),
                    x + pad, y + 47, enabled ? GREEN : MUTED, true);
            g.fill(x + pad, y + 66, x + w - pad, y + 67, 0xFF2B394A);
        } else {
            g.text(font, Component.literal(enabled ? "●" : "○"), x + w - 28, titleY, enabled ? GREEN : MUTED, true);
        }
        List<SkyOPSetting<?>> settings = visibleSettings(selectedModule);
        int settingsTop = y + (compact ? 43 : 72);
        int rowHeight = compact ? 31 : 39, rowStep = compact ? 35 : 42;
        int settingsBottom = y + h - (compact ? 5 : 8);
        int visibleRows = Math.max(1, (settingsBottom - settingsTop + 2) / rowStep);
        settingsScroll = Math.max(0, Math.min(settingsScroll, Math.max(0, settings.size() - visibleRows)));
        int rowY = settingsTop - settingsScroll * rowStep;
        if (w <= 8 || settingsBottom <= settingsTop) {
            if (settings.isEmpty())
                g.text(font, Component.literal(SkyOPLanguage.text("gui.no_extra_settings")), x + pad, settingsTop, MUTED, true);
            return;
        }
        g.enableScissor(x + 4, settingsTop, x + w - 4, settingsBottom);
        for (SkyOPSetting<?> setting : settings) {
            boolean hovered = inside(mouseX, mouseY, x + 4, rowY, w - 8, rowHeight);
            g.fill(x + 4, rowY, x + w - 4, rowY + rowHeight - 2, hovered ? PANEL_HOVER : PANEL_LIGHT);
            if (setting instanceof ActionSetting actionSetting) {
                g.text(font, Component.literal(trim(actionSetting.title(), compact ? 12 : 18)), x + pad, rowY + 4, MUTED, true);
                g.text(font, Component.literal(trim(actionSetting.buttonLabel(), compact ? 20 : 30)),
                        x + pad, rowY + (compact ? 16 : 19), ACCENT, true);
            } else {
                g.text(font, Component.literal(trim(setting.title(), compact ? 15 : 20)), x + pad, rowY + 4, TEXT, true);
                g.text(font, Component.literal(trim(settingValue(setting), compact ? 12 : 15)),
                        x + w - (compact ? 76 : 80), rowY + 4, settingValueColor(setting), true);
                if (!compact || (!(setting instanceof KeybindSetting) && !(setting instanceof TextSetting) && !(setting instanceof MultiSetting)))
                    g.text(font, Component.literal(trim(setting.description(), compact ? 25 : 31)),
                            x + pad, rowY + (compact ? 16 : 19), MUTED, true);
                if (setting instanceof NumberSetting number) {
                    int trackX = x + pad, trackY = rowY + rowHeight - 5, trackW = Math.max(1, w - pad * 2);
                    g.fill(trackX, trackY, trackX + trackW, trackY + 2, 0xFF36465A);
                    double range = number.maximum() - number.minimum();
                    double fraction = range <= 0 ? 0 : (number.value() - number.minimum()) / range;
                    int fillW = (int) (trackW * Math.max(0.0, Math.min(1.0, fraction)));
                    g.fill(trackX, trackY, trackX + fillW, trackY + 2, ACCENT);
                } else if (setting instanceof MultiSetting multi) {
                    int chipX = x + pad, chipY = rowY + rowHeight - 11, chipWidth = compact ? 44 : 64;
                    for (String option : multi.options().stream().sorted().toList()) {
                        boolean chosen = multi.value().contains(option);
                        g.fill(chipX, chipY, chipX + chipWidth - 3, chipY + 9, chosen ? 0xFF285B70 : 0xFF36465A);
                        g.text(font, Component.literal(trim(option, compact ? 6 : 8)), chipX + 3, chipY + 1, chosen ? TEXT : MUTED, false);
                        chipX += chipWidth;
                        if (chipX > x + w - 50) break;
                    }
                } else if (setting instanceof ColorSetting color) {
                    g.fill(x + w - 22, rowY + rowHeight - 14, x + w - 12, rowY + rowHeight - 4, color.value());
                } else if (setting instanceof TextSetting textSetting) {
                    g.fill(x + pad, rowY + rowHeight - 12, x + w - pad, rowY + rowHeight - 3,
                            editingHudText ? 0xFF263D53 : 0xFF36465A);
                    g.text(font, Component.literal(trim(textSetting.value(), compact ? 24 : 34)),
                            x + pad + 3, rowY + rowHeight - 10, editingHudText ? 0xFFFFC85A : ACCENT, false);
                }
            }
            rowY += rowStep;
        }
        g.disableScissor();
        if (settings.isEmpty())
            g.text(font, Component.literal(SkyOPLanguage.text("gui.no_extra_settings")), x + pad, settingsTop + 8, MUTED, true);
        String missing = SkyOPModuleDependencies.missingRequirement(Minecraft.getInstance(), selectedModule);
        if (!missing.isEmpty() && !compact && h > 160)
            g.text(font, Component.literal(trim(SkyOPLanguage.text("gui.requires") + ": " + missing, 32)),
                    x + pad, y + h - 18, 0xFFFFC85A, true);
    }

    private boolean handleInspectorClick(int mx, int my, int x, int y, int w, int h, boolean compact) {
        if (compact && inside(mx, my, x + w - 23, y + 4, 18, 18)) {
            inspectorOpen = false;
            return true;
        }
        if (selectedModule == null) return false;
        int settingsTop = y + (compact ? 43 : 72), rowStep = compact ? 35 : 42;
        List<SkyOPSetting<?>> settings = visibleSettings(selectedModule);
        int visibleRows = Math.max(1, (h - (compact ? 48 : 80)) / rowStep);
        int visibleRow = (my - settingsTop) / rowStep, rowIndex = visibleRow + settingsScroll;
        if (mx < x + 4 || mx >= x + w - 4 || my < settingsTop || visibleRow < 0
                || rowIndex >= settings.size() || visibleRow >= visibleRows || my >= y + h - (compact ? 5 : 8)) return false;
        SkyOPSetting<?> setting = settings.get(rowIndex);
        int rowY = settingsTop + visibleRow * rowStep;
        if (setting instanceof ActionSetting action) {
            try {
                action.activate();
                setProfileStatus(SkyOPLanguage.text("gui.profile_action_completed"));
            } catch (Exception exception) {
                setProfileStatus(SkyOPLanguage.text("gui.profile_error") + ": " + exception.getMessage());
            }
            return true;
        }
        if (setting instanceof TextSetting) {
            editingHudText = true;
            return true;
        }
        applySettingClick(setting, mx, x, w, rowY);
        return true;
    }

        private void drawFooter(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (layout.compact()) {
            drawCompactFooter(g, mouseX, mouseY);
            return;
        }
        int y = panelY + panelHeight - layout.footerHeight();
        g.fill(panelX + 12, y, panelX + panelWidth - 12, y + 1, 0xFF2B394A);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.quick_profiles")), panelX + 20, y + 15, MUTED, true);

        drawProfileButton(g, mouseX, mouseY, panelX + 130, y + 7, 48, "PVE");
        drawProfileButton(g, mouseX, mouseY, panelX + 184, y + 7, 48, "OP");
        drawProfileButton(g, mouseX, mouseY, panelX + 238, y + 7, 66, "VISUAL");

        int languageX = panelX + 320;
        boolean languageHovered = inside(mouseX, mouseY, languageX, y + 7, 150, 24);
        g.fill(languageX, y + 7, languageX + 150, y + 31, languageHovered ? 0xFF314C65 : PANEL_LIGHT);
        String languageLabel = SkyOPLanguage.text("gui.language") + ": " + SkyOPLanguage.current().displayName();
        g.text(font, Component.literal(trim(languageLabel, 24)), languageX + 9, y + 15, languageHovered ? TEXT : ACCENT, true);

        int bx = panelX + panelWidth - 144;
        boolean hovered = inside(mouseX, mouseY, bx, y + 7, 124, 24);
        g.fill(bx, y + 7, bx + 124, y + 31, hovered ? 0xFF693542 : 0xFF482B35);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.disable_all")), bx + 18, y + 15, 0xFFFFB8C0, true);
        if (!profileStatus.isEmpty() && profileStatusTicks > 0) {
            int alpha = 190 + (int) (35 * Math.sin(animationTicks * 0.25));
            int color = (Math.min(255, alpha) << 24) | 0x00FFFFFF;
            g.text(font, Component.literal(trim(profileStatus, 30)), panelX + 410, y + 15, color, true);
        }
    }

    private void drawProfileButton(GuiGraphicsExtractor g, int mouseX, int mouseY,
                                   int x, int y, int w, String label) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, 24);
        g.fill(x, y, x + w, y + 24, hovered ? 0xFF314C65 : PANEL_LIGHT);
        g.text(font, Component.literal(label), x + (w - font.width(label)) / 2,
                y + 8, hovered ? TEXT : ACCENT, true);
    }

    private List<SkyOPModuleId> modulesForCategory() {
        List<SkyOPModuleId> modules = new ArrayList<>();
        String query = searchQuery.toLowerCase(java.util.Locale.ROOT);
        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            boolean inCategory = selectedCategory == SkyOPCategory.FAVORITES
                    ? SkyOPCore.isFavorite(module) : module.category() == selectedCategory;
            boolean matches = query.isEmpty()
                    || module.title().toLowerCase(java.util.Locale.ROOT).contains(query)
                    || module.description().toLowerCase(java.util.Locale.ROOT).contains(query);
            if ((inCategory || !query.isEmpty()) && matches) modules.add(module);
        }
        return modules;
    }

    private int countModules(SkyOPCategory category) {
        int count = 0;
        for (SkyOPModuleId module : SkyOPModuleId.values()) {
            if (category == SkyOPCategory.FAVORITES ? SkyOPCore.isFavorite(module)
                    : module.category() == category) count++;
        }
        return count;
    }

    private List<SkyOPSetting<?>> visibleSettings(SkyOPModuleId module) {
        return SkyOPModuleManager.settings(module);
    }

    private String settingValue(SkyOPSetting<?> setting) {
        if (setting instanceof BooleanSetting bool) {
            return Boolean.TRUE.equals(bool.value()) ? SkyOPLanguage.text("gui.on") : SkyOPLanguage.text("gui.off");
        }
        if (setting instanceof NumberSetting number) {
            double value = number.value();
            return number.integer() ? Long.toString(Math.round(value))
                    : String.format(java.util.Locale.ROOT, "%.2f", value);
        }
        if (setting instanceof ModeSetting mode) return mode.value();
        if (setting instanceof MultiSetting multi) {
            return multi.value().size() + " " + SkyOPLanguage.text("gui.selected");
        }
        if (setting instanceof ColorSetting color) return "#" + SkyOPColorCodec.formatRgb(color.value());
        if (setting instanceof KeybindSetting) {
            KeyMapping mapping = SkyOPCore.keybindFor(selectedModule);
            return waitingForKeybind ? SkyOPLanguage.text("gui.press_a_key")
                    : mapping == null || SkyOPCore.isModuleKeybindUnbound(selectedModule)
                    ? SkyOPLanguage.text("gui.unbound")
                    : mapping.getTranslatedKeyMessage().getString();
        }
        return String.valueOf(setting.value());
    }

    private int settingValueColor(SkyOPSetting<?> setting) {
        if (setting instanceof BooleanSetting bool) return bool.value() ? GREEN : RED;
        return ACCENT;
    }

    private void applySettingClick(SkyOPSetting<?> setting, double mouseX,
                                   int inspectorX, int inspectorWidth, int rowY) {
        int settingInset = layout.compact() ? 7 : 12;
        if (setting instanceof BooleanSetting bool) {
            bool.set(!bool.value());
        } else if (setting instanceof NumberSetting number) {
            int sliderX = inspectorX + settingInset;
            int sliderWidth = Math.max(1, inspectorWidth - settingInset * 2);
            double fraction = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / sliderWidth));
            number.set(number.minimum() + fraction * (number.maximum() - number.minimum()));
        } else if (setting instanceof ModeSetting mode) {
            List<String> options = mode.options();
            if (!options.isEmpty()) {
                int index = options.indexOf(mode.value());
                mode.set(options.get((index + 1) % options.size()));
            }
        } else if (setting instanceof MultiSetting multi) {
            int chipWidth = layout.compact() ? 44 : 64;
            int localX = (int) mouseX - (inspectorX + settingInset);
            int index = Math.max(0, localX / chipWidth);
            List<String> options = multi.options().stream().sorted().toList();
            if (index < options.size()) {
                java.util.Set<String> selected = new java.util.LinkedHashSet<>(multi.value());
                String option = options.get(index);
                if (!selected.add(option)) selected.remove(option);
                multi.set(selected);
            }
        } else if (setting instanceof ColorSetting color) {
            colorPickerOpen = true;
            editingHudColor = false;
            hexColorInput = SkyOPColorCodec.formatRgb(color.value());
        } else if (setting instanceof KeybindSetting) {
            waitingForKeybind = true;
        }
        SkyOPCore.saveConfig();
    }

        @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        calculateLayout();
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.input();
        int mx = (int) mouseX, my = (int) mouseY;
        if (layout.compact()) return compactMouseClicked(event, doubleClick, mx, my, button);

        int closeX = panelX + panelWidth - 48;
        if (button == InputConstants.MOUSE_BUTTON_LEFT && inside(mx, my, closeX, panelY + 12, 30, 23)) {
            onClose();
            return true;
        }

        int footerY = panelY + panelHeight - 38;
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (panelWidth >= 800) {
                int colorX = panelX + panelWidth - 378;
                int editX = panelX + panelWidth - 320;
                if (inside(mx, my, colorX, panelY + 12, 48, 24)) {
                    colorPickerOpen = !colorPickerOpen;
                    editingHudColor = false;
                    hexColorInput = SkyOPColorCodec.formatRgb(SkyOPCore.hudAccentColor());
                    return true;
                }
                if (inside(mx, my, editX, panelY + 12, 72, 24)) {
                    hudEditMode = !hudEditMode;
                    draggingHud = false;
                    draggingHudModule = null;
                    return true;
                }
            }
            if (colorPickerOpen && handleColorPickerClick(mx, my)) return true;
            if (hudEditMode) {
                HudPreview preview = hudPreviewAt(mx, my);
                if (preview != null) {
                    draggingHud = true;
                    draggingHudModule = preview.module();
                    hudDragOffsetX = mx - preview.x();
                    hudDragOffsetY = my - preview.y();
                    return true;
                }
            }
            int languageX = panelX + 320;
            if (inside(mx, my, languageX, footerY + 7, 150, 24)) {
                SkyOPCore.cycleLanguage();
                return true;
            }
            int presetY = footerY + 7;
            if (inside(mx, my, panelX + 130, presetY, 48, 24)) {
                SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.PVE);
                return true;
            }
            if (inside(mx, my, panelX + 184, presetY, 48, 24)) {
                SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.OP);
                return true;
            }
            if (inside(mx, my, panelX + 238, presetY, 66, 24)) {
                SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.VISUAL);
                return true;
            }

            int disableX = panelX + panelWidth - 144;
            if (inside(mx, my, disableX, footerY + 7, 124, 24)) {
                SkyOPCore.disableAll(Minecraft.getInstance());
                return true;
            }
        }

        int moduleX = layout.moduleList().x();
        int moduleTop = layout.moduleList().y() + 76;
        int moduleWidth = layout.moduleList().width();
        int cardWidth = (moduleWidth - 8) / 2;
        int searchX = moduleX, searchY = layout.moduleList().y() + 27;
        if (button == InputConstants.MOUSE_BUTTON_LEFT && inside(mx, my, searchX, searchY, moduleWidth, 22)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        int navX = layout.sidebar().x() + 5, navY = layout.sidebar().y() + 31, navIndex = 0;
        for (SkyOPCategory category : SkyOPCategory.values()) {
            int rowY = navY + navIndex * 36;
            if (button == InputConstants.MOUSE_BUTTON_LEFT && inside(mx, my, navX, rowY, layout.sidebar().width() - 10, 30)) {
                selectedCategory = category;
                selectFirstModule();
                return true;
            }
            navIndex++;
        }

        int moduleIndex = 0;
        for (SkyOPModuleId module : modulesForCategory()) {
            int col = moduleIndex % 2;
            int row = moduleIndex / 2;
            int cardX = moduleX + col * (cardWidth + 8);
            int cardY = moduleTop + row * 60 - moduleScroll;
            int favoriteX = cardX + cardWidth - 74;
            if (button == InputConstants.MOUSE_BUTTON_LEFT
                    && inside(mx, my, favoriteX, cardY + 3, 18, 18)) {
                SkyOPCore.toggleFavorite(module);
                return true;
            }
            if (inside(mx, my, cardX, cardY, cardWidth, 52)) {
                selectedModule = module;
                settingsScroll = 0;
                if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                    SkyOPCore.toggle(Minecraft.getInstance(), module);
                } else if (button != InputConstants.MOUSE_BUTTON_RIGHT) {
                    return super.mouseClicked(event, doubleClick);
                }
                return true;
            }
            moduleIndex++;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && selectedModule != null) {
            SkyOPGuiLayout.Rect inspector = layout.inspector();
            if (handleInspectorClick(mx, my, inspector.x(), inspector.y(),
                    inspector.width(), inspector.height(), false)) return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean compactMouseClicked(MouseButtonEvent event, boolean doubleClick,
                                         int mx, int my, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT && inside(mx, my, compactCloseX(), panelY + 5,
                compactHeaderButtonWidth(), 19)) {
            onClose();
            return true;
        }
        if (button == InputConstants.MOUSE_BUTTON_LEFT && inside(mx, my, compactToolsX(), panelY + 5,
                compactHeaderButtonWidth(), 19)) {
            headerToolsOpen = !headerToolsOpen;
            return true;
        }
        if (headerToolsOpen && button == InputConstants.MOUSE_BUTTON_LEFT) {
            int px = Math.max(panelX, panelX + panelWidth - 116), py = panelY + layout.headerHeight() - 2;
            int pw = Math.min(108, panelWidth - (px - panelX));
            if (inside(mx, my, px, py, pw, 23)) {
                hudEditMode = !hudEditMode;
                headerToolsOpen = false;
                draggingHud = false;
                return true;
            }
            if (inside(mx, my, px, py + 23, pw, 24)) {
                colorPickerOpen = !colorPickerOpen;
                editingHudColor = false;
                hexColorInput = SkyOPColorCodec.formatRgb(SkyOPCore.hudAccentColor());
                headerToolsOpen = false;
                return true;
            }
        }
        if (colorPickerOpen && button == InputConstants.MOUSE_BUTTON_LEFT && handleColorPickerClick(mx, my)) return true;
        if (hudEditMode && button == InputConstants.MOUSE_BUTTON_LEFT) {
            HudPreview preview = hudPreviewAt(mx, my);
            if (preview != null) {
                draggingHud = true; draggingHudModule = preview.module();
                hudDragOffsetX = mx - preview.x(); hudDragOffsetY = my - preview.y();
                return true;
            }
        }
        int footerY = panelY + panelHeight - layout.footerHeight();
        boolean labels = panelWidth >= 280;
        int buttonHeight = compactFooterButtonHeight();
        int btnY = footerY + Math.max(0, (layout.footerHeight() - buttonHeight) / 2);
        int cursor = panelX + 7;
        int[] widths = labels ? new int[]{34,30,38} : new int[]{18,18,18};
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            SkyOPPreset[] presets = {SkyOPPreset.PVE, SkyOPPreset.OP, SkyOPPreset.VISUAL};
            for (int n=0;n<3;n++) {
                if (inside(mx,my,cursor,btnY,widths[n],buttonHeight)) {
                    SkyOPCore.applyPreset(Minecraft.getInstance(),presets[n]); return true;
                }
                cursor += widths[n] + 3;
            }
            int langWidth=labels?82:22,langX=cursor+2;
            if (langX+langWidth>panelX+panelWidth-24) langX=panelX+panelWidth-langWidth-25;
            if (inside(mx,my,langX,btnY,langWidth,buttonHeight)) { SkyOPCore.cycleLanguage(); return true; }
            int disableWidth=labels?54:19,disableX=panelX+panelWidth-disableWidth-7;
            if (inside(mx,my,disableX,btnY,disableWidth,buttonHeight)) { SkyOPCore.disableAll(Minecraft.getInstance()); return true; }
        }
        if (inspectorOpen && button == InputConstants.MOUSE_BUTTON_LEFT) {
            SkyOPGuiLayout.Rect inspector = layout.inspector();
            if (inspector.contains(mx, my)) {
                handleInspectorClick(mx, my, inspector.x(), inspector.y(),
                        inspector.width(), inspector.height(), true);
                return true; // A modal overlay must not click through to module cards.
            }
        }
        SkyOPGuiLayout.Rect sidebar=layout.sidebar();
        int rowH=compactCategoryRowHeight(),navIndex=0;
        for (SkyOPCategory category:SkyOPCategory.values()) {
            int rowY=sidebar.y()+navIndex*rowH-sidebarScroll*rowH;
            if (button==InputConstants.MOUSE_BUTTON_LEFT&&inside(mx,my,sidebar.x(),rowY,sidebar.width(),rowH)) {
                selectedCategory=category;selectFirstModule();settingsScroll=0;inspectorOpen=false;return true;
            }
            navIndex++;
        }
        SkyOPGuiLayout.Rect list=layout.moduleList();
        if (list.height() <= 20 || list.width() <= 8) return super.mouseClicked(event, doubleClick);
        int searchY=list.y()+Math.min(17,Math.max(2,list.height()/3));
        int searchH=Math.min(18,Math.max(10,list.height()-(searchY-list.y())-1));
        if (button==InputConstants.MOUSE_BUTTON_LEFT&&inside(mx,my,list.x(),searchY,list.width(),searchH)) {searchFocused=true;return true;}
        if (button==InputConstants.MOUSE_BUTTON_LEFT&&searchFocused) searchFocused=false;
        int listTop=list.y()+(list.height()>=56?41:(searchY-list.y())+searchH+3);
        int listBottom=list.bottom()-2;
        if(listBottom<=listTop)return super.mouseClicked(event,doubleClick);
        List<SkyOPModuleId> modules=modulesForCategory();
        int columns=list.width()>=270?2:1,gap=5;
        int cardWidth=Math.max(1,(list.width()-(columns-1)*gap)/columns);
        int cardHeight=list.height()>=80?38:Math.max(10,Math.min(24,listBottom-listTop-1));
        int rowStep=cardHeight+4,index=0;
        for (SkyOPModuleId module:modules) {
            int col=index%columns,row=index/columns;
            int cardX=list.x()+col*(cardWidth+gap),cardY=listTop+row*rowStep-moduleScroll;
            if(cardY+cardHeight<=listTop||cardY>=listBottom){index++;continue;}
            int pillW=Math.min(30,Math.max(18,cardWidth/4));
            int favoriteX=cardX+cardWidth-pillW-21;
            if (button==InputConstants.MOUSE_BUTTON_LEFT&&inside(mx,my,favoriteX,cardY+1,17,18)) {
                SkyOPCore.toggleFavorite(module);return true;
            }
            if (inside(mx,my,cardX,cardY,cardWidth,cardHeight)) {
                selectedModule=module;settingsScroll=0;
                if (button==InputConstants.MOUSE_BUTTON_RIGHT) {inspectorOpen=true;return true;}
                if (button==InputConstants.MOUSE_BUTTON_LEFT) {SkyOPCore.toggle(Minecraft.getInstance(),module);return true;}
            }
            index++;
        }
        if (button==InputConstants.MOUSE_BUTTON_RIGHT) return true;
        return super.mouseClicked(event,doubleClick);
    }

    private void setProfileStatus(String value) {
        profileStatus = value == null ? SkyOPLanguage.text("gui.profile_action_completed") : value;
        profileStatusTicks = 100;
    }

    private boolean handleColorPickerClick(int mx, int my) {
        int x = colorPickerX(), y = colorPickerY();
        int[][] colors = {{0xFF61C7F2, 0xFF8B7CFF, 0xFFFFB45B},
                {0xFF57D69A, 0xFFFF7885, 0xFFF1F6FB}};
        for (int row = 0; row < 2; row++) for (int col = 0; col < 3; col++) {
            int px = x + 8 + col * 28, py = y + 20 + row * 27;
            if (inside(mx, my, px, py, 22, 22)) {
                SkyOPCore.setHudAccentColor(colors[row][col]);
                hexColorInput = SkyOPColorCodec.formatRgb(colors[row][col]);
                editingHudColor = false;
                colorPickerOpen = false;
                return true;
            }
        }
        if (inside(mx, my, x + 8, y + 76, 96, 22)) {
            editingHudColor = true;
            hexColorInput = "";
            return true;
        }
        if (inside(mx, my, x + 110, y + 76, 48, 22)) {
            applyHexColor();
            return true;
        }
        return inside(mx, my, x, y, 166, 104);
    }

    private int colorPickerX() {
        return Math.max(panelX, panelX + panelWidth - (layout.compact() ? 174 : 230));
    }

    private int colorPickerY() {
        return panelY + (layout.compact() ? layout.headerHeight() - 3 : 42);
    }

    private void applyHexColor() {
        try {
            SkyOPCore.setHudAccentColor(SkyOPColorCodec.parseRgb(hexColorInput));
            hexColorInput = SkyOPColorCodec.formatRgb(SkyOPCore.hudAccentColor());
            editingHudColor = false;
            colorPickerOpen = false;
            setProfileStatus(SkyOPLanguage.text("gui.hud_color_updated"));
        } catch (IllegalArgumentException exception) {
            setProfileStatus(SkyOPLanguage.text("gui.enter_hex_digits"));
            editingHudColor = true;
        }
    }

    private void drawColorPicker(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!colorPickerOpen) return;
        int x = colorPickerX(), y = colorPickerY();
        int[][] colors = {{0xFF61C7F2, 0xFF8B7CFF, 0xFFFFB45B},
                {0xFF57D69A, 0xFFFF7885, 0xFFF1F6FB}};
        g.fill(x, y, x + 166, y + 104, 0xF0101726);
        g.outline(x, y, 166, 104, 0xFF40546B);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.hud_color")), x + 8, y + 6, TEXT, true);
        for (int row = 0; row < 2; row++) for (int col = 0; col < 3; col++) {
            int px = x + 8 + col * 28, py = y + 20 + row * 27;
            g.fill(px, py, px + 22, py + 22, colors[row][col]);
            if (colors[row][col] == SkyOPCore.hudAccentColor()) g.outline(px - 2, py - 2, 26, 26, TEXT);
        }
        int fieldX = x + 8, fieldY = y + 76;
        g.fill(fieldX, fieldY, fieldX + 96, fieldY + 22, editingHudColor ? 0xFF263D53 : PANEL_LIGHT);
        String typed = hexColorInput.isEmpty() ? "RRGGBB" : hexColorInput;
        g.text(font, Component.literal("#" + typed + (editingHudColor ? "|" : "")),
                fieldX + 6, fieldY + 7, editingHudColor ? TEXT : MUTED, true);
        int applyX = x + 110;
        boolean hovered = inside(mouseX, mouseY, applyX, fieldY, 48, 22);
        g.fill(applyX, fieldY, applyX + 48, fieldY + 22, hovered ? 0xFF314C65 : PANEL_LIGHT);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.apply")),
                applyX + 6, fieldY + 7, ACCENT, true);
    }

    private List<HudPreview> buildHudPreviews() {
        List<HudPreview> previews = new ArrayList<>();
        int sw = width, sh = height;
        if (SkyOPCore.isEnabled(SkyOPModuleId.HUD)) {
            int mainHudHeight = Math.min(sh, 70 + Math.max(0, SkyOPCore.enabledModuleCount() - 1) * 12);
            previews.add(hudPreview(SkyOPModuleId.HUD, 8, 8, 190,
                    mainHudHeight, SkyOPLanguage.text("gui.main_hud"), sw, sh));
        }
        if (SkyOPCore.isEnabled(SkyOPModuleId.TARGET_HUD))
            previews.add(hudPreview(SkyOPModuleId.TARGET_HUD, sw - 194, 8, 186, 58, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.KEYSTROKES_HUD))
            previews.add(hudPreview(SkyOPModuleId.KEYSTROKES_HUD, 10, sh - 62, 86, 42, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.ARMOR_HUD))
            previews.add(hudPreview(SkyOPModuleId.ARMOR_HUD, sw - 150, sh - 38, 142, 28, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.POTION_HUD))
            previews.add(hudPreview(SkyOPModuleId.POTION_HUD, sw - 198, 76, 190, 96, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.DURABILITY_HUD))
            previews.add(hudPreview(SkyOPModuleId.DURABILITY_HUD, sw / 2 - 82, sh - 94, 164, 24, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.COMPASS_HUD)
                || SkyOPCore.isEnabled(SkyOPModuleId.DIRECTION_HUD)) {
            String label = SkyOPCore.isEnabled(SkyOPModuleId.COMPASS_HUD)
                    ? SkyOPLanguage.text("gui.compass_direction") : SkyOPModuleId.DIRECTION_HUD.title();
            previews.add(hudPreview(SkyOPModuleId.COMPASS_HUD, sw / 2 - 48, 8, 96, 23, label, sw, sh));
        }
        if (SkyOPCore.isEnabled(SkyOPModuleId.PING_HUD))
            previews.add(hudPreview(SkyOPModuleId.PING_HUD, sw - 100, 8, 92, 22, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.FPS_HUD))
            previews.add(hudPreview(SkyOPModuleId.FPS_HUD, sw - 100, 34, 92, 22, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.RADAR_HUD))
            previews.add(hudPreview(SkyOPModuleId.RADAR_HUD, sw - 202, sh / 2 - 12, 194, 90, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.TEXT_GUI)) {
            int textWidth = Math.min(sw, Math.max(120, Math.min(480, font.width(SkyOPState.customHudText) + 16)));
            previews.add(hudPreview(SkyOPModuleId.TEXT_GUI, 8, sh - 27, textWidth, 19, null, sw, sh));
        }
        if (SkyOPCore.isEnabled(SkyOPModuleId.SPEEDOMETER))
            previews.add(hudPreview(SkyOPModuleId.SPEEDOMETER, sw / 2 - 58, sh - 37, 116, 25, null, sw, sh));
        if (SkyOPCore.isEnabled(SkyOPModuleId.CPS_HUD))
            previews.add(hudPreview(SkyOPModuleId.CPS_HUD, sw / 2 - 48, sh - 66, 96, 24, null, sw, sh));
        return previews;
    }

    private HudPreview hudPreview(SkyOPModuleId module, int defaultX, int defaultY,
                                  int previewWidth, int previewHeight, String label,
                                  int screenWidth, int screenHeight) {
        int x = SkyOPCore.hudXFor(module, defaultX, screenWidth, previewWidth);
        int y = SkyOPCore.hudYFor(module, defaultY, screenHeight, previewHeight);
        return new HudPreview(module, x, y, previewWidth, previewHeight,
                label == null ? module.title() : label);
    }

    private HudPreview hudPreviewAt(int mouseX, int mouseY) {
        List<HudPreview> previews = buildHudPreviews();
        // Hit-test in reverse drawing order so the visually topmost overlapping panel is draggable.
        for (int index = previews.size() - 1; index >= 0; index--) {
            HudPreview preview = previews.get(index);
            if (inside(mouseX, mouseY, preview.x(), preview.y(), preview.width(), preview.height())) return preview;
        }
        return null;
    }

    private void drawHudEditorPreview(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!hudEditMode) return;
        g.text(font, Component.literal(SkyOPLanguage.text("gui.hud_layout") + " • "
                + SkyOPLanguage.text("gui.drag_hud")), 12, 12, SkyOPCore.hudAccentColor(), true);
        for (HudPreview preview : buildHudPreviews()) {
            boolean hovered = inside(mouseX, mouseY, preview.x(), preview.y(), preview.width(), preview.height());
            int border = hovered ? TEXT : SkyOPCore.hudAccentColor();
            g.fill(preview.x(), preview.y(), preview.x() + preview.width(), preview.y() + preview.height(),
                    hovered ? 0xE51C2A3B : 0xD5101726);
            g.outline(preview.x(), preview.y(), preview.width(), preview.height(), border);
            g.fill(preview.x(), preview.y(), preview.x() + preview.width(), preview.y() + 2,
                    SkyOPCore.hudAccentColor());
            g.text(font, Component.literal(trim(preview.label(), 22)),
                    preview.x() + 6, preview.y() + 7, TEXT, true);
            if (preview.height() > 34) {
                g.text(font, Component.literal(preview.x() + ", " + preview.y()),
                        preview.x() + 6, preview.y() + 21, MUTED, true);
                g.text(font, Component.literal(SkyOPLanguage.text("gui.drag_to_move")),
                        preview.x() + 6, preview.y() + preview.height() - 12,
                        SkyOPCore.hudAccentColor(), true);
            }
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (editingHudColor) {
            String typed = event.codepointAsString().toUpperCase(java.util.Locale.ROOT);
            if (typed.length() == 1 && "0123456789ABCDEF".indexOf(typed.charAt(0)) >= 0
                    && hexColorInput.length() < 6) {
                hexColorInput += typed;
            }
            return true;
        }
        if (editingHudText && selectedModule != null) {
            TextSetting textSetting = visibleSettings(selectedModule).stream()
                    .filter(TextSetting.class::isInstance).map(TextSetting.class::cast).findFirst().orElse(null);
            if (textSetting == null) {
                editingHudText = false;
                return false;
            }
            if (event.isAllowedChatCharacter()) textSetting.set(textSetting.value() + event.codepointAsString());
            return true;
        }
        if (searchFocused && !waitingForKeybind && event.isAllowedChatCharacter() && searchQuery.length() < 32) {
            searchQuery += event.codepointAsString();
            moduleScroll = 0;
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (editingHudColor) {
            if (event.key() == InputConstants.KEY_ESCAPE) {
                editingHudColor = false;
                return true;
            }
            if (event.key() == InputConstants.KEY_RETURN) {
                applyHexColor();
                return true;
            }
            if (event.key() == InputConstants.KEY_BACKSPACE && !hexColorInput.isEmpty()) {
                hexColorInput = hexColorInput.substring(0, hexColorInput.length() - 1);
                return true;
            }
            return true;
        }
        if (waitingForKeybind && selectedModule != null) {
            if (event.key() == InputConstants.KEY_ESCAPE) {
                waitingForKeybind = false;
                return true;
            }
            SkyOPCore.setModuleKeybind(selectedModule, event.key());
            waitingForKeybind = false;
            setProfileStatus(SkyOPLanguage.text("gui.keybind_updated"));
            return true;
        }
        if (editingHudText) {
            if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_RETURN) {
                editingHudText = false;
                return true;
            }
            if (event.key() == InputConstants.KEY_BACKSPACE && selectedModule != null) {
                TextSetting textSetting = visibleSettings(selectedModule).stream()
                        .filter(TextSetting.class::isInstance).map(TextSetting.class::cast).findFirst().orElse(null);
                if (textSetting != null && !textSetting.value().isEmpty())
                    textSetting.set(textSetting.value().substring(0, textSetting.value().length() - 1));
                return true;
            }
        }
        if (searchFocused) {
            if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_RETURN) {
                searchFocused = false;
                return true;
            }
            if (event.key() == InputConstants.KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                moduleScroll = 0;
                return true;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (draggingHud && hudEditMode && draggingHudModule != null) {
            HudPreview preview = buildHudPreviews().stream()
                    .filter(value -> value.module() == draggingHudModule)
                    .findFirst().orElse(null);
            if (preview != null) {
                SkyOPCore.setHudPosition(draggingHudModule,
                        (int) event.x() - hudDragOffsetX, (int) event.y() - hudDragOffsetY,
                        width, height, preview.width(), preview.height());
            }
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingHud) {
            draggingHud = false;
            draggingHudModule = null;
            SkyOPCore.saveConfig();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        calculateLayout();
        int mx=(int)mouseX,my=(int)mouseY,direction=(int)Math.signum(verticalAmount);
        if(direction==0)return super.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
        if(layout.compact()) {
            if(inspectorOpen&&layout.inspector().contains(mx,my)){
                settingsScroll=Math.max(0,Math.min(120,settingsScroll-direction));return true;
            }
            if(layout.sidebar().contains(mx,my)){
                sidebarScroll=Math.max(0,Math.min(SkyOPCategory.values().length-1,sidebarScroll-direction));return true;
            }
            if(layout.moduleList().contains(mx,my)){
                moduleScroll=Math.max(0,Math.min(1200,moduleScroll-direction*36));return true;
            }
            return super.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
        }
        SkyOPGuiLayout.Rect modules=layout.moduleList(),inspector=layout.inspector();
        if(inside(mx,my,modules.x(),modules.y()+76,modules.width(),Math.max(1,modules.height()-76))){
            moduleScroll=Math.max(0,Math.min(1200,moduleScroll-direction*60));return true;
        }
        if(inside(mx,my,inspector.x(),inspector.y()+70,inspector.width(),Math.max(1,inspector.height()-70))){
            settingsScroll=Math.max(0,Math.min(120,settingsScroll-direction));return true;
        }
        return super.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
    }

        private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private String trim(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        draggingHud = false;
        draggingHudModule = null;
        waitingForKeybind = false;
        editingHudColor = false;
        editingHudText = false;
        searchFocused = false;
        SkyOPCore.saveConfig();
        super.onClose();
    }
}
