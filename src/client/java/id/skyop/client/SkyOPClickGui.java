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
    private String searchQuery = "";
    private boolean searchFocused, waitingForKeybind, hudEditMode, draggingHud, colorPickerOpen, editingHudText;
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
        panelWidth = Math.min(960, Math.max(680, width - 24));
        panelHeight = Math.min(590, Math.max(450, height - 20));
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
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
        g.text(font, Component.literal(SkyOPLanguage.text("gui.brand")), panelX + 20, panelY + 14, ACCENT, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.client")), panelX + 112, panelY + 14, MUTED, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.control_center")), panelX + 20, panelY + 34, TEXT, true);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.subtitle")), panelX + 210, panelY + 34, MUTED, true);

        if (panelWidth >= 800) {
            int colorX = panelX + panelWidth - 378;
            int hudEditX = panelX + panelWidth - 320;
            boolean colorHovered = inside(mouseX, mouseY, colorX, panelY + 12, 48, 24);
            g.fill(colorX, panelY + 12, colorX + 48, panelY + 36, colorHovered ? PANEL_HOVER : PANEL_LIGHT);
            g.fill(colorX + 16, panelY + 20, colorX + 32, panelY + 28, SkyOPState.hudAccentColor());
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

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = panelX + 12, y = panelY + 62, w = 126;
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
        int x = panelX + 151, y = panelY + 63, w = panelWidth - 430;
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
        int x = panelX + panelWidth - 263, y = panelY + 63, w = 248, h = panelHeight - 121;
        g.fill(x, y, x + w, y + h, 0xFF111A27);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.inspector")), x + 12, y + 10, MUTED, true);
        if (selectedModule == null) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.select_module")), x + 12, y + 42, TEXT, true);
            return;
        }

        boolean enabled = SkyOPCore.isEnabled(selectedModule);
        g.text(font, Component.literal(selectedModule.title()), x + 12, y + 31, TEXT, true);
        g.text(font, Component.literal(enabled ? "● " + SkyOPLanguage.text("gui.enabled") : "○ " + SkyOPLanguage.text("gui.disabled")),
                x + 12, y + 47, enabled ? GREEN : MUTED, true);
        g.fill(x + 12, y + 66, x + w - 12, y + 67, 0xFF2B394A);

        if (selectedModule == SkyOPModuleId.PROFILE_MANAGER) {
            drawProfileControls(g, mouseX, mouseY, x, y);
            return;
        }
        if (selectedModule == SkyOPModuleId.HOTBAR_PRESETS) {
            drawHotbarControls(g, mouseX, mouseY, x, y);
            return;
        }
        if (selectedModule == SkyOPModuleId.TEXT_GUI) {
            g.text(font, Component.literal(editingHudText ? SkyOPLanguage.text("gui.edit_text") + ": " + SkyOPState.customHudText + "|" : SkyOPLanguage.text("gui.custom_hud_text")),
                    x + 12, y + 125, editingHudText ? 0xFFFFC85A : TEXT, true);
            g.text(font, Component.literal(SkyOPLanguage.text("gui.edit_text_help")),
                    x + 12, y + 145, MUTED, true);
            return;
        }

        List<SkyOPSetting<?>> settings = visibleSettings(selectedModule);
        int rowY = y + 72 - settingsScroll * 42;
        int settingsBottom = y + h - 8;
        g.enableScissor(x + 8, y + 70, x + w - 8, settingsBottom);
        for (SkyOPSetting<?> setting : settings) {
            int rowH = 39;
            boolean hovered = inside(mouseX, mouseY, x + 8, rowY, w - 16, rowH);
            g.fill(x + 8, rowY, x + w - 8, rowY + rowH - 2,
                    hovered ? PANEL_HOVER : PANEL_LIGHT);
            g.text(font, Component.literal(trim(setting.title(), 20)),
                    x + 15, rowY + 5, TEXT, true);
            g.text(font, Component.literal(trim(settingValue(setting), 15)),
                    x + w - 80, rowY + 5, settingValueColor(setting), true);
            g.text(font, Component.literal(trim(setting.description(), 31)),
                    x + 15, rowY + 19, MUTED, true);

            if (setting instanceof NumberSetting number) {
                int trackX = x + 15, trackY = rowY + 32, trackW = w - 30;
                g.fill(trackX, trackY, trackX + trackW, trackY + 3, 0xFF36465A);
                double fraction = (number.value() - number.minimum()) /
                        (number.maximum() - number.minimum());
                int fillW = (int) (trackW * Math.max(0.0, Math.min(1.0, fraction)));
                g.fill(trackX, trackY, trackX + fillW, trackY + 3, ACCENT);
            } else if (setting instanceof MultiSetting multi) {
                int chipX = x + 15;
                for (String option : multi.options().stream().sorted().toList()) {
                    boolean selected = multi.value().contains(option);
                    int chipWidth = 64;
                    g.fill(chipX, rowY + 29, chipX + chipWidth - 3, rowY + 38,
                            selected ? 0xFF285B70 : 0xFF36465A);
                    g.text(font, Component.literal(trim(option, 8)), chipX + 3, rowY + 29,
                            selected ? TEXT : MUTED, false);
                    chipX += chipWidth;
                    if (chipX > x + w - 60) break;
                }
            } else if (setting instanceof ColorSetting color) {
                g.fill(x + w - 22, rowY + 20, x + w - 12, rowY + 30, color.value());
            }
            rowY += 42;
        }
        g.disableScissor();

        if (settings.isEmpty()) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.no_extra_settings")), x + 12, y + 130, MUTED, true);
            g.text(font, Component.literal(selectedModule.description()), x + 12, y + 148, TEXT, true);
        }
        String missing = SkyOPModuleDependencies.missingRequirement(Minecraft.getInstance(), selectedModule);
        if (!missing.isEmpty()) {
            g.text(font, Component.literal(trim(SkyOPLanguage.text("gui.requires") + ": " + missing, 32)),
                    x + 12, y + h - 18, 0xFFFFC85A, true);
        }
    }

    private void drawProfileControls(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y) {
        g.text(font, Component.literal(SkyOPLanguage.text("gui.configuration_profiles")), x + 12, y + 112, TEXT, true);
        drawControl(g, mouseX, mouseY, x + 12, y + 138, 66, SkyOPLanguage.text("gui.save") + " 1");
        drawControl(g, mouseX, mouseY, x + 84, y + 138, 66, SkyOPLanguage.text("gui.load") + " 1");
        drawControl(g, mouseX, mouseY, x + 12, y + 168, 66, SkyOPLanguage.text("gui.save") + " 2");
        drawControl(g, mouseX, mouseY, x + 84, y + 168, 66, SkyOPLanguage.text("gui.load") + " 2");
        drawControl(g, mouseX, mouseY, x + 12, y + 198, 66, SkyOPLanguage.text("gui.save") + " 3");
        drawControl(g, mouseX, mouseY, x + 84, y + 198, 66, SkyOPLanguage.text("gui.load") + " 3");
        drawControl(g, mouseX, mouseY, x + 12, y + 234, 100, SkyOPLanguage.text("gui.export_json"));
        drawControl(g, mouseX, mouseY, x + 118, y + 234, 100, SkyOPLanguage.text("gui.import_json"));
        drawControl(g, mouseX, mouseY, x + 12, y + 264, 100, SkyOPLanguage.text("gui.backup_now"));
    }

    private void drawHotbarControls(GuiGraphicsExtractor g, int mouseX, int mouseY, int x, int y) {
        g.text(font, Component.literal(SkyOPLanguage.text("gui.hotbar_layout_slots")), x + 12, y + 112, TEXT, true);
        for (int slot = 1; slot <= 3; slot++) {
            int rowY = y + 140 + (slot - 1) * 34;
            drawControl(g, mouseX, mouseY, x + 12, rowY, 100, SkyOPLanguage.text("gui.save_hotbar") + " " + slot);
            drawControl(g, mouseX, mouseY, x + 118, rowY, 100, SkyOPLanguage.text("gui.load_hotbar") + " " + slot);
        }
    }

    private void drawControl(GuiGraphicsExtractor g, int mouseX, int mouseY,
                             int x, int y, int w, String label) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, 23);
        g.fill(x, y, x + w, y + 23, hovered ? 0xFF314C65 : PANEL_LIGHT);
        g.text(font, Component.literal(label), x + 6, y + 8, hovered ? TEXT : ACCENT, true);
    }

    private void drawFooter(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int y = panelY + panelHeight - 38;
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
                                   int inspectorX, int rowY) {
        if (setting instanceof BooleanSetting bool) {
            bool.set(!bool.value());
        } else if (setting instanceof NumberSetting number) {
            int sliderX = inspectorX + 15;
            int sliderWidth = 218;
            double fraction = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / sliderWidth));
            number.set(number.minimum() + fraction * (number.maximum() - number.minimum()));
        } else if (setting instanceof ModeSetting mode) {
            List<String> options = mode.options();
            int index = options.indexOf(mode.value());
            mode.set(options.get((index + 1) % options.size()));
        } else if (setting instanceof MultiSetting multi) {
            int localX = (int) mouseX - (inspectorX + 15);
            int chipWidth = 64;
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
                    hexColorInput = SkyOPColorCodec.formatRgb(SkyOPState.hudAccentColor());
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

        int moduleX = panelX + 151;
        int moduleTop = panelY + 139;
        int moduleWidth = panelWidth - 430;
        int cardWidth = (moduleWidth - 8) / 2;
        int searchX = moduleX, searchY = panelY + 90;
        if (button == InputConstants.MOUSE_BUTTON_LEFT && inside(mx, my, searchX, searchY, moduleWidth, 22)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        int navX = panelX + 17, navY = panelY + 93, navIndex = 0;
        for (SkyOPCategory category : SkyOPCategory.values()) {
            int rowY = navY + navIndex * 36;
            if (button == InputConstants.MOUSE_BUTTON_LEFT && inside(mx, my, navX, rowY, 116, 30)) {
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

        if (selectedModule != null && button == InputConstants.MOUSE_BUTTON_LEFT) {
            int inspectorX = panelX + panelWidth - 263;
            int inspectorY = panelY + 63;

            if (selectedModule == SkyOPModuleId.TEXT_GUI
                    && inside(mx, my, inspectorX + 8, inspectorY + 115, 232, 40)) {
                editingHudText = true;
                return true;
            }

            if (selectedModule == SkyOPModuleId.PROFILE_MANAGER) {
                int localX = mx - (inspectorX + 12);
                int localY = my - (inspectorY + 138);
                int row = localY >= 0 ? localY / 30 : -1;
                try {
                    if (row == 0 && localX >= 0 && localX < 66) SkyOPProfileManager.saveSlot(1);
                    else if (row == 0 && localX >= 72 && localX < 150) SkyOPProfileManager.loadSlot(1);
                    else if (row == 1 && localX >= 0 && localX < 66) SkyOPProfileManager.saveSlot(2);
                    else if (row == 1 && localX >= 72 && localX < 150) SkyOPProfileManager.loadSlot(2);
                    else if (row == 2 && localX >= 0 && localX < 66) SkyOPProfileManager.saveSlot(3);
                    else if (row == 2 && localX >= 72 && localX < 150) SkyOPProfileManager.loadSlot(3);
                    else if (inside(mx, my, inspectorX + 12, inspectorY + 234, 100, 23)) SkyOPProfileManager.exportCurrent();
                    else if (inside(mx, my, inspectorX + 118, inspectorY + 234, 100, 23)) SkyOPProfileManager.importProfile();
                    else if (inside(mx, my, inspectorX + 12, inspectorY + 264, 100, 23)) SkyOPProfileManager.backupCurrent();
                    else return super.mouseClicked(event, doubleClick);
                    setProfileStatus(SkyOPLanguage.text("gui.profile_action_completed"));
                } catch (Exception exception) {
                    setProfileStatus(SkyOPLanguage.text("gui.profile_error") + ": " + exception.getMessage());
                }
                return true;
            }

            if (selectedModule == SkyOPModuleId.HOTBAR_PRESETS) {
                int row = (my - (inspectorY + 140)) / 34;
                int localX = mx - (inspectorX + 12);
                int slot = row + 1;
                try {
                    if (row >= 0 && row < 3 && localX >= 0 && localX < 100) {
                        SkyOPProfileManager.saveHotbarPreset(slot);
                        setProfileStatus(SkyOPLanguage.text("gui.saved_hotbar") + " " + slot);
                        return true;
                    }
                    if (row >= 0 && row < 3 && localX >= 106 && localX < 212) {
                        SkyOPProfileManager.loadHotbarPreset(slot);
                        setProfileStatus(SkyOPLanguage.text("gui.loaded_hotbar") + " " + slot);
                        return true;
                    }
                } catch (Exception exception) {
                    setProfileStatus(SkyOPLanguage.text("gui.hotbar_error") + ": " + exception.getMessage());
                    return true;
                }
            }

            int settingsX = inspectorX + 8;
            int settingsY = inspectorY + 70;
            int visibleRow = (my - settingsY) / 42;
            int rowIndex = visibleRow + settingsScroll;
            List<SkyOPSetting<?>> settings = visibleSettings(selectedModule);
            if (mx >= settingsX && mx <= inspectorX + 240
                    && my >= settingsY && visibleRow >= 0 && rowIndex < settings.size()) {
                applySettingClick(settings.get(rowIndex), mouseX, inspectorX + 8,
                        settingsY + visibleRow * 42);
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
        int x = panelX + panelWidth - 230, y = panelY + 42;
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
            hexColorInput = SkyOPColorCodec.formatRgb(SkyOPState.hudAccentColor());
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
        int x = panelX + panelWidth - 230, y = panelY + 42;
        int[][] colors = {{0xFF61C7F2, 0xFF8B7CFF, 0xFFFFB45B},
                {0xFF57D69A, 0xFFFF7885, 0xFFF1F6FB}};
        g.fill(x, y, x + 166, y + 104, 0xF0101726);
        g.outline(x, y, 166, 104, 0xFF40546B);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.hud_color")), x + 8, y + 6, TEXT, true);
        for (int row = 0; row < 2; row++) for (int col = 0; col < 3; col++) {
            int px = x + 8 + col * 28, py = y + 20 + row * 27;
            g.fill(px, py, px + 22, py + 22, colors[row][col]);
            if (colors[row][col] == SkyOPState.hudAccentColor()) g.outline(px - 2, py - 2, 26, 26, TEXT);
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
        int x = SkyOPState.hudXFor(module, defaultX, screenWidth, previewWidth);
        int y = SkyOPState.hudYFor(module, defaultY, screenHeight, previewHeight);
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
                + SkyOPLanguage.text("gui.drag_hud")), 12, 12, SkyOPState.hudAccentColor(), true);
        for (HudPreview preview : buildHudPreviews()) {
            boolean hovered = inside(mouseX, mouseY, preview.x(), preview.y(), preview.width(), preview.height());
            int border = hovered ? TEXT : SkyOPState.hudAccentColor();
            g.fill(preview.x(), preview.y(), preview.x() + preview.width(), preview.y() + preview.height(),
                    hovered ? 0xE51C2A3B : 0xD5101726);
            g.outline(preview.x(), preview.y(), preview.width(), preview.height(), border);
            g.fill(preview.x(), preview.y(), preview.x() + preview.width(), preview.y() + 2,
                    SkyOPState.hudAccentColor());
            g.text(font, Component.literal(trim(preview.label(), 22)),
                    preview.x() + 6, preview.y() + 7, TEXT, true);
            if (preview.height() > 34) {
                g.text(font, Component.literal(preview.x() + ", " + preview.y()),
                        preview.x() + 6, preview.y() + 21, MUTED, true);
                g.text(font, Component.literal(SkyOPLanguage.text("gui.drag_to_move")),
                        preview.x() + 6, preview.y() + preview.height() - 12,
                        SkyOPState.hudAccentColor(), true);
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
        if (editingHudText && selectedModule == SkyOPModuleId.TEXT_GUI) {
            if (event.isAllowedChatCharacter() && SkyOPState.customHudText.length() < 80) {
                SkyOPCore.setCustomHudText(SkyOPState.customHudText + event.codepointAsString());
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
        if (editingHudText) {
            if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_RETURN) {
                editingHudText = false;
                return true;
            }
            if (event.key() == InputConstants.KEY_BACKSPACE && !SkyOPState.customHudText.isEmpty()) {
                SkyOPCore.setCustomHudText(SkyOPState.customHudText.substring(0, SkyOPState.customHudText.length() - 1));
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
        int moduleX = panelX + 151, moduleY = panelY + 63, moduleW = panelWidth - 430;
        int inspectorX = panelX + panelWidth - 263, inspectorY = panelY + 63;
        if (inside((int) mouseX, (int) mouseY, moduleX, moduleY + 76, moduleW, panelHeight - 135)) {
            moduleScroll = Math.max(0, Math.min(1200, moduleScroll - (int) Math.signum(verticalAmount) * 60));
            return true;
        }
        if (inside((int) mouseX, (int) mouseY, inspectorX, inspectorY + 100, 248, panelHeight - 140)) {
            settingsScroll = Math.max(0, Math.min(20, settingsScroll - (int) Math.signum(verticalAmount)));
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
