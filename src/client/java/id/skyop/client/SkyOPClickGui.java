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
    private SkyOPGuiLayout layout;
    private boolean inspectorOpen;
    private String searchQuery = "";
    private boolean searchFocused, waitingForKeybind, hudEditMode, draggingHud, colorPickerOpen;
    private StringSetting editingStringSetting;
    private boolean editingHudColor;
    private String hexColorInput = "";
    private SkyOPModuleId draggingHudModule;
    private int moduleScroll, settingsScroll, hudDragOffsetX, hudDragOffsetY;
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
        if (!layout.inspectorOverlay() || inspectorOpen) {
            drawInspector(graphics, mouseX, mouseY);
        }
        drawFooter(graphics, mouseX, mouseY);
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
            g.text(font, Component.literal(SkyOPLanguage.text("gui.brand")),
                    panelX + 10, panelY + 13, ACCENT, true);
            g.text(font, Component.literal("26.3"),
                    panelX + 66, panelY + 13, MUTED, true);

            int colorX = layout.colorButtonX();
            int hudX = layout.hudButtonX();
            int closeX = layout.closeButtonX();
            boolean colorHovered = inside(mouseX, mouseY, colorX, panelY + 8, 20, 22);
            g.fill(colorX, panelY + 8, colorX + 20, panelY + 30,
                    colorHovered ? PANEL_HOVER : PANEL_LIGHT);
            g.fill(colorX + 6, panelY + 14, colorX + 14, panelY + 22, SkyOPCore.hudAccentColor());

            boolean hudHovered = inside(mouseX, mouseY, hudX, panelY + 8, 22, 22);
            g.fill(hudX, panelY + 8, hudX + 22, panelY + 30,
                    hudHovered || hudEditMode ? 0xFF263D53 : PANEL_LIGHT);
            g.text(font, Component.literal("H"), hudX + 7, panelY + 15,
                    hudEditMode ? GREEN : ACCENT, true);

            g.fill(closeX, panelY + 8, closeX + 22, panelY + 30, PANEL_LIGHT);
            g.text(font, Component.literal("×"), closeX + 8, panelY + 12, TEXT, true);
            return;
        }

        g.text(font, Component.literal(SkyOPLanguage.text("gui.brand")),
                panelX + 20, panelY + 14, ACCENT, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.client")),
                panelX + 112, panelY + 14, MUTED, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.control_center")),
                panelX + 20, panelY + 34, TEXT, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.subtitle")),
                panelX + 210, panelY + 34, MUTED, true);

        int colorX = layout.colorButtonX();
        int hudEditX = layout.hudButtonX();
        boolean colorHovered = inside(mouseX, mouseY, colorX, panelY + 12, 48, 24);
        g.fill(colorX, panelY + 12, colorX + 48, panelY + 36,
                colorHovered ? PANEL_HOVER : PANEL_LIGHT);
        g.fill(colorX + 16, panelY + 20, colorX + 32, panelY + 28, SkyOPCore.hudAccentColor());
        boolean editHovered = inside(mouseX, mouseY, hudEditX, panelY + 12, 72, 24);
        g.fill(hudEditX, panelY + 12, hudEditX + 72, panelY + 36,
                editHovered || hudEditMode ? 0xFF263D53 : PANEL_LIGHT);
        g.text(font, Component.literal(SkyOPLanguage.text(hudEditMode ? "gui.hud_edit_on" : "gui.hud_edit")),
                hudEditX + 7, panelY + 20, hudEditMode ? GREEN : ACCENT, true);

        int statusX = panelX + panelWidth - 172;
        g.fill(statusX, panelY + 13, statusX + 104, panelY + 35, PANEL_LIGHT);
        g.fill(statusX + 9, panelY + 21, statusX + 15, panelY + 27, GREEN);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.ready")),
                statusX + 21, panelY + 20, TEXT, true);
        int closeX = layout.closeButtonX();
        g.fill(closeX, panelY + 12, closeX + 30, panelY + 35, PANEL_LIGHT);
        g.text(font, Component.literal("×"), closeX + 11, panelY + 16, TEXT, true);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = layout.sidebarX();
        int y = layout.sidebarY();
        if (layout.iconSidebar()) {
            for (int index = 0; index < SkyOPCategory.values().length; index++) {
                SkyOPCategory category = SkyOPCategory.values()[index];
                int rowY = layout.categoryRowY(index);
                int buttonWidth = layout.categoryButtonWidth();
                int buttonHeight = layout.categoryButtonHeight();
                boolean selected = category == selectedCategory;
                boolean hovered = inside(mouseX, mouseY, x, rowY, buttonWidth, buttonHeight);
                g.fill(x, rowY, x + buttonWidth, rowY + buttonHeight,
                        selected ? 0xFF263B50 : (hovered ? PANEL_HOVER : 0xFF111A27));
                if (selected) g.fill(x, rowY, x + 3, rowY + buttonHeight, ACCENT);
                String icon = category == SkyOPCategory.FAVORITES ? "★"
                        : category.name().substring(0, 1);
                int textX = x + (buttonWidth - font.width(icon)) / 2;
                g.text(font, Component.literal(icon), textX, rowY + 8,
                        selected ? TEXT : MUTED, true);
            }
            return;
        }

        int w = layout.sidebarWidth();
        g.fill(x, panelY + 62, x + w, panelY + panelHeight - 48, 0xFF111A27);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.categories")),
                x + 10, panelY + 72, MUTED, true);
        int index = 0;
        for (SkyOPCategory category : SkyOPCategory.values()) {
            int rowY = layout.categoryRowY(index);
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
        g.text(font, Component.literal(SkyOPLanguage.text("gui.active_modules")),
                x + 10, activeY + 12, MUTED, true);
        g.text(font, Component.literal(Integer.toString(SkyOPCore.enabledModuleCount())),
                x + 10, activeY + 30, ACCENT, true);
    }

    private void drawModuleList(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = layout.contentX();
        int y = layout.contentY();
        int w = layout.contentWidth();
        g.text(font, Component.literal(selectedCategory.title().toUpperCase()
                        + " " + SkyOPLanguage.text("gui.modules")),
                x, y + (layout.compact() ? 1 : 8), TEXT, true);

        int searchY = y + (layout.compact() ? 20 : 27);
        int searchHeight = layout.compact() ? 20 : 22;
        g.fill(x, searchY, x + w, searchY + searchHeight,
                searchFocused ? 0xFF263D53 : PANEL_LIGHT);
        g.text(font, Component.literal(searchQuery.isEmpty()
                        ? SkyOPLanguage.text("gui.search") + "  (" + SkyOPLanguage.text("gui.type_to_filter") + ")"
                        : SkyOPLanguage.text("gui.search_prefix") + ": " + searchQuery + (searchFocused ? "|" : "")),
                x + 8, searchY + 6, searchFocused ? TEXT : MUTED, true);
        if (!layout.compact()) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.left_toggle_right_inspect")),
                    x, y + 54, MUTED, true);
        }

        List<SkyOPModuleId> modules = modulesForCategory();
        int columns = layout.moduleColumns();
        int cardWidth = layout.moduleCardWidth();
        int cardHeight = layout.moduleCardHeight();
        int rowStep = layout.moduleRowStep();
        int listTop = layout.listTop();
        int listBottom = Math.max(listTop + 1, layout.listBottom());
        int visibleHeight = Math.max(1, listBottom - listTop);
        int rowCount = (modules.size() + columns - 1) / columns;
        int maxScroll = Math.max(0, rowCount * rowStep - visibleHeight + 8);
        moduleScroll = Math.min(moduleScroll, maxScroll);
        g.enableScissor(x, listTop, x + w, listBottom);
        int index = 0;
        SkyOPModuleId hoveredModule = null;
        for (SkyOPModuleId module : modules) {
            int col = index % columns;
            int row = index / columns;
            int cardX = x + col * (cardWidth + 8);
            int cardY = listTop + row * rowStep - moduleScroll;
            boolean enabled = SkyOPCore.isEnabled(module);
            boolean selected = module == selectedModule;
            boolean hovered = inside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight);
            g.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight,
                    selected ? 0xFF263D53 : (hovered ? PANEL_HOVER : PANEL_LIGHT));
            if (selected) g.fill(cardX, cardY, cardX + 3, cardY + cardHeight, ACCENT);

            int favoriteX = cardX + cardWidth - (layout.compact() ? 58 : 74);
            int pillX = cardX + cardWidth - 46;
            int titleMax = Math.max(8, (cardWidth - (layout.compact() ? 76 : 96)) / 6);
            int descriptionMax = Math.max(8, (cardWidth - 24) / 5);
            g.text(font, Component.literal(trim(module.title(), titleMax)),
                    cardX + 9, cardY + 6, TEXT, true);
            g.text(font, Component.literal(trim(module.description(), descriptionMax)),
                    cardX + 9, cardY + (layout.compact() ? 21 : 25), MUTED, true);
            g.text(font, Component.literal(SkyOPCore.isFavorite(module) ? "★" : "☆"),
                    favoriteX, cardY + 6, SkyOPCore.isFavorite(module) ? 0xFFFFC85A : MUTED, true);
            g.fill(pillX, cardY + 13, pillX + 38, cardY + 32,
                    enabled ? 0xFF174633 : 0xFF3B2631);
            g.text(font, Component.literal(enabled ? SkyOPLanguage.text("gui.on") : SkyOPLanguage.text("gui.off")),
                    pillX + 7, cardY + 19, enabled ? GREEN : RED, true);

            if (hovered) hoveredModule = module;
            index++;
        }
        g.disableScissor();
        if (modules.isEmpty()) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.no_matching_modules")),
                    x + 8, listTop + 12, MUTED, true);
        }
        if (hoveredModule != null && !layout.compact()) {
            int alpha = 190 + (int) (40 * Math.sin(animationTicks * 0.18));
            int tooltipColor = (Math.min(255, alpha) << 24) | 0x00101726;
            g.fill(x, listBottom + 2, x + w, listBottom + 28, tooltipColor);
            g.text(font, Component.literal(hoveredModule.description()),
                    x + 7, listBottom + 10, TEXT, true);
        }
    }

    private void drawInspector(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = layout.inspectorX();
        int y = layout.inspectorY();
        int w = layout.inspectorWidth();
        int h = layout.inspectorHeight();
        g.fill(x, y, x + w, y + h, 0xFF111A27);

        if (layout.compact()) {
            g.fill(x, y, x + w, y + 25, 0xFF182538);
            g.text(font, Component.literal(SkyOPLanguage.text("gui.inspector")),
                    x + 8, y + 8, MUTED, true);
            int closeX = x + w - 23;
            g.fill(closeX, y + 3, closeX + 19, y + 22, PANEL_LIGHT);
            g.text(font, Component.literal("×"), closeX + 7, y + 7, TEXT, true);
        } else {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.inspector")),
                    x + 12, y + 10, MUTED, true);
        }

        if (selectedModule == null) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.select_module")),
                    x + 12, y + 42, TEXT, true);
            return;
        }

        boolean enabled = SkyOPCore.isEnabled(selectedModule);
        g.text(font, Component.literal(trim(selectedModule.title(), Math.max(8, (w - 110) / 6))),
                x + 12, y + 31, TEXT, true);
        g.text(font, Component.literal(enabled ? "● " + SkyOPLanguage.text("gui.enabled")
                        : "○ " + SkyOPLanguage.text("gui.disabled")),
                x + 12, y + 47, enabled ? GREEN : MUTED, true);
        g.fill(x + 12, y + 59, x + w - 12, y + 60, 0xFF2B394A);

        List<SkyOPSetting<?>> settings = visibleSettings(selectedModule);
        int rowStep = layout.settingRowStep();
        int settingsTop = layout.settingsTopOffset();
        int rowY = y + settingsTop - settingsScroll * rowStep;
        int settingsBottom = Math.max(y + settingsTop + 1, y + h - 7);
        g.enableScissor(x + 8, y + settingsTop, x + w - 8, settingsBottom);
        for (SkyOPSetting<?> setting : settings) {
            int rowH = layout.compact() ? 33 : 39;
            boolean hovered = inside(mouseX, mouseY, x + 8, rowY, w - 16, rowH);
            g.fill(x + 8, rowY, x + w - 8, rowY + rowH - 2,
                    hovered ? PANEL_HOVER : PANEL_LIGHT);

            if (setting instanceof ActionSetting action) {
                String actionLabel = action.title()
                        + (action.value().isBlank() ? "" : " " + action.value());
                g.text(font, Component.literal(trim(actionLabel, Math.max(8, (w - 32) / 6))),
                        x + 15, rowY + (layout.compact() ? 6 : 8), ACCENT, true);
                g.text(font, Component.literal(trim(action.description(), Math.max(8, (w - 24) / 5))),
                        x + 15, rowY + (layout.compact() ? 19 : 22), MUTED, false);
            } else {
                String displayedValue = setting instanceof StringSetting string
                        ? (editingStringSetting == string ? string.value() + "|" : string.value())
                        : settingValue(setting);
                g.text(font, Component.literal(trim(setting.title(), Math.max(8, (w - 106) / 6))),
                        x + 15, rowY + 4, TEXT, true);
                g.text(font, Component.literal(trim(displayedValue, 15)),
                        x + w - 80, rowY + 4,
                        editingStringSetting == setting ? 0xFFFFC85A : settingValueColor(setting), true);
                g.text(font, Component.literal(trim(setting.description(), Math.max(8, (w - 24) / 5))),
                        x + 15, rowY + 17, MUTED, true);
            }

            if (setting instanceof NumberSetting number) {
                int trackX = x + 15;
                int trackY = rowY + (layout.compact() ? 28 : 32);
                int trackW = Math.max(20, w - 30);
                g.fill(trackX, trackY, trackX + trackW, trackY + 3, 0xFF36465A);
                double range = number.maximum() - number.minimum();
                double fraction = range == 0.0 ? 0.0 : (number.value() - number.minimum()) / range;
                int fillW = (int) (trackW * Math.max(0.0, Math.min(1.0, fraction)));
                g.fill(trackX, trackY, trackX + fillW, trackY + 3, ACCENT);
            } else if (setting instanceof MultiSetting multi) {
                int chipX = x + 15;
                for (String option : multi.options().stream().sorted().toList()) {
                    boolean chosen = multi.value().contains(option);
                    int chipWidth = layout.compact() ? 54 : 64;
                    int chipY = rowY + (layout.compact() ? 25 : 29);
                    g.fill(chipX, chipY, chipX + chipWidth - 3, chipY + 8,
                            chosen ? 0xFF285B70 : 0xFF36465A);
                    g.text(font, Component.literal(trim(option, 7)), chipX + 3, chipY,
                            chosen ? TEXT : MUTED, false);
                    chipX += chipWidth;
                    if (chipX > x + w - 60) break;
                }
            } else if (setting instanceof ColorSetting color) {
                g.fill(x + w - 22, rowY + 19, x + w - 12, rowY + 29, color.value());
            }
            rowY += rowStep;
        }
        g.disableScissor();

        if (settings.isEmpty()) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.no_extra_settings")),
                    x + 12, y + settingsTop + 12, MUTED, true);
            g.text(font, Component.literal(selectedModule.description()),
                    x + 12, y + settingsTop + 30, TEXT, true);
        }
        String missing = SkyOPModuleDependencies.missingRequirement(Minecraft.getInstance(), selectedModule);
        if (!missing.isEmpty() && !layout.compact()) {
            g.text(font, Component.literal(trim(SkyOPLanguage.text("gui.requires") + ": " + missing, 32)),
                    x + 12, y + h - 18, 0xFFFFC85A, true);
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
        if (setting instanceof ActionSetting action) return action.value();
        if (setting instanceof StringSetting string) return string.value();
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
                                   int inspectorX, int rowY) {
        if (setting instanceof ActionSetting action) {
            try {
                action.execute();
                setProfileStatus(SkyOPLanguage.text("gui.profile_action_completed"));
            } catch (Exception exception) {
                setProfileStatus(SkyOPLanguage.text("gui.profile_error") + ": " + exception.getMessage());
            }
        } else if (setting instanceof StringSetting string) {
            editingStringSetting = string;
        } else if (setting instanceof BooleanSetting bool) {
            bool.set(!bool.value());
        } else if (setting instanceof NumberSetting number) {
            int sliderX = inspectorX + 15;
            int sliderWidth = Math.max(20, layout.inspectorWidth() - 30);
            double fraction = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / sliderWidth));
            number.set(number.minimum() + fraction * (number.maximum() - number.minimum()));
        } else if (setting instanceof ModeSetting mode) {
            List<String> options = mode.options();
            int index = options.indexOf(mode.value());
            mode.set(options.get((index + 1) % options.size()));
        } else if (setting instanceof MultiSetting multi) {
            int localX = (int) mouseX - (inspectorX + 15);
            int chipWidth = layout.compact() ? 54 : 64;
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
        int mx = (int) mouseX;
        int my = (int) mouseY;

        int closeX = layout.closeButtonX();
        int closeY = panelY + (layout.compact() ? 8 : 12);
        if (button == InputConstants.MOUSE_BUTTON_LEFT
                && inside(mx, my, closeX, closeY, layout.compact() ? 22 : 30, layout.compact() ? 22 : 23)) {
            onClose();
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            int colorX = layout.colorButtonX();
            int hudX = layout.hudButtonX();
            int headerY = panelY + (layout.compact() ? 8 : 12);
            if (inside(mx, my, colorX, headerY, layout.compact() ? 20 : 48, layout.compact() ? 22 : 24)) {
                colorPickerOpen = !colorPickerOpen;
                editingHudColor = false;
                hexColorInput = SkyOPColorCodec.formatRgb(SkyOPCore.hudAccentColor());
                return true;
            }
            if (inside(mx, my, hudX, headerY, layout.compact() ? 22 : 72, layout.compact() ? 22 : 24)) {
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

        int footerY = layout.footerY();
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (layout.compact()) {
                if (inside(mx, my, panelX + 10, footerY + 6, 27, 20)) {
                    SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.PVE);
                    return true;
                }
                if (inside(mx, my, panelX + 41, footerY + 6, 27, 20)) {
                    SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.OP);
                    return true;
                }
                if (inside(mx, my, panelX + 72, footerY + 6, 39, 20)) {
                    SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.VISUAL);
                    return true;
                }
                if (inside(mx, my, panelX + 122, footerY + 6, 82, 20)) {
                    SkyOPCore.cycleLanguage();
                    return true;
                }
                int disableX = panelX + panelWidth - 98;
                if (inside(mx, my, disableX, footerY + 6, 90, 20)) {
                    SkyOPCore.disableAll(Minecraft.getInstance());
                    return true;
                }
            } else {
                int languageX = panelX + 320;
                if (inside(mx, my, languageX, footerY + 7, 150, 24)) {
                    SkyOPCore.cycleLanguage();
                    return true;
                }
                if (inside(mx, my, panelX + 130, footerY + 7, 48, 24)) {
                    SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.PVE);
                    return true;
                }
                if (inside(mx, my, panelX + 184, footerY + 7, 48, 24)) {
                    SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.OP);
                    return true;
                }
                if (inside(mx, my, panelX + 238, footerY + 7, 66, 24)) {
                    SkyOPCore.applyPreset(Minecraft.getInstance(), SkyOPPreset.VISUAL);
                    return true;
                }
                int disableX = panelX + panelWidth - 144;
                if (inside(mx, my, disableX, footerY + 7, 124, 24)) {
                    SkyOPCore.disableAll(Minecraft.getInstance());
                    return true;
                }
            }
        }

        if (layout.inspectorOverlay() && inspectorOpen && button == InputConstants.MOUSE_BUTTON_LEFT) {
            int inspectorCloseX = layout.inspectorX() + layout.inspectorWidth() - 23;
            if (inside(mx, my, inspectorCloseX, layout.inspectorY() + 3, 19, 19)) {
                inspectorOpen = false;
                editingStringSetting = null;
                waitingForKeybind = false;
                return true;
            }
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            for (int index = 0; index < SkyOPCategory.values().length; index++) {
                int categoryX = layout.sidebarX();
                int categoryY = layout.categoryRowY(index);
                if (inside(mx, my, categoryX, categoryY,
                        layout.categoryButtonWidth(), layout.categoryButtonHeight())) {
                    selectedCategory = SkyOPCategory.values()[index];
                    selectFirstModule();
                    moduleScroll = 0;
                    settingsScroll = 0;
                    inspectorOpen = false;
                    return true;
                }
            }
        }

        int moduleX = layout.contentX();
        int searchY = layout.contentY() + (layout.compact() ? 20 : 27);
        int moduleWidth = layout.contentWidth();
        if (button == InputConstants.MOUSE_BUTTON_LEFT
                && inside(mx, my, moduleX, searchY, moduleWidth, layout.compact() ? 20 : 22)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        int columns = layout.moduleColumns();
        int cardWidth = layout.moduleCardWidth();
        int cardHeight = layout.moduleCardHeight();
        int rowStep = layout.moduleRowStep();
        int moduleTop = layout.listTop();
        int moduleIndex = 0;
        for (SkyOPModuleId module : modulesForCategory()) {
            int col = moduleIndex % columns;
            int row = moduleIndex / columns;
            int cardX = moduleX + col * (cardWidth + 8);
            int cardY = moduleTop + row * rowStep - moduleScroll;
            int favoriteX = cardX + cardWidth - (layout.compact() ? 58 : 74);
            if (button == InputConstants.MOUSE_BUTTON_LEFT
                    && inside(mx, my, favoriteX, cardY + 3, 18, 18)) {
                SkyOPCore.toggleFavorite(module);
                return true;
            }
            if (inside(mx, my, cardX, cardY, cardWidth, cardHeight)) {
                selectedModule = module;
                settingsScroll = 0;
                if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                    inspectorOpen = true;
                    return true;
                }
                if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                    SkyOPCore.toggle(Minecraft.getInstance(), module);
                    return true;
                }
            }
            moduleIndex++;
        }

        if ((layout.inspectorOverlay() && inspectorOpen || !layout.inspectorOverlay())
                && selectedModule != null && button == InputConstants.MOUSE_BUTTON_LEFT) {
            int inspectorX = layout.inspectorX();
            int inspectorY = layout.inspectorY();
            int inspectorW = layout.inspectorWidth();

            int settingsX = inspectorX + 8;
            int settingsY = inspectorY + layout.settingsTopOffset();
            int visibleRow = (my - settingsY) / layout.settingRowStep();
            int rowIndex = visibleRow + settingsScroll;
            List<SkyOPSetting<?>> settings = visibleSettings(selectedModule);
            if (mx >= settingsX && mx <= inspectorX + inspectorW - 8
                    && my >= settingsY && visibleRow >= 0 && rowIndex < settings.size()) {
                SkyOPSetting<?> setting = settings.get(rowIndex);
                if (setting instanceof StringSetting string) {
                    editingStringSetting = string;
                } else {
                    applySettingClick(setting, mouseX, inspectorX + 8,
                            settingsY + visibleRow * layout.settingRowStep());
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void setProfileStatus(String value) {
        profileStatus = value == null ? SkyOPLanguage.text("gui.profile_action_completed") : value;
        profileStatusTicks = 100;
    }

    private boolean handleColorPickerClick(int mx, int my) {
        int x = layout.colorPickerX(), y = layout.colorPickerY();
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
        int x = layout.colorPickerX(), y = layout.colorPickerY();
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
        if (editingStringSetting != null) {
            if (event.isAllowedChatCharacter()
                    && editingStringSetting.value().length() < editingStringSetting.maximumLength()) {
                editingStringSetting.set(editingStringSetting.value() + event.codepointAsString());
            }
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
        if (editingStringSetting != null) {
            if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_RETURN) {
                editingStringSetting = null;
                return true;
            }
            if (event.key() == InputConstants.KEY_BACKSPACE && !editingStringSetting.value().isEmpty()) {
                String value = editingStringSetting.value();
                editingStringSetting.set(value.substring(0, value.length() - 1));
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
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (layout.inspectorOverlay() && inspectorOpen
                && inside(mx, my, layout.inspectorX(), layout.inspectorY(),
                layout.inspectorWidth(), layout.inspectorHeight())) {
            settingsScroll = Math.max(0, Math.min(120,
                    settingsScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        if (inside(mx, my, layout.contentX(), layout.listTop(),
                layout.contentWidth(), Math.max(1, layout.listBottom() - layout.listTop()))) {
            moduleScroll = Math.max(0, Math.min(1200,
                    moduleScroll - (int) Math.signum(verticalAmount) * 60));
            return true;
        }
        if (!layout.inspectorOverlay()
                && inside(mx, my, layout.inspectorX(), layout.inspectorY(),
                layout.inspectorWidth(), layout.inspectorHeight())) {
            settingsScroll = Math.max(0, Math.min(120,
                    settingsScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
