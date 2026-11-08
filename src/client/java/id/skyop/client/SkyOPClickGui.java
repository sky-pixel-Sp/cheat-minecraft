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

    private SkyOPClient.Category selectedCategory = SkyOPClient.Category.COMBAT;
    private SkyOPClient.Module selectedModule = SkyOPClient.Module.KILL_AURA;
    private int panelX, panelY, panelWidth, panelHeight;
    private String searchQuery = "";
    private boolean searchFocused, waitingForKeybind, hudEditMode, draggingHud, colorPickerOpen, editingHudText;
    private boolean editingHudColor;
    private String hexColorInput = "";
    private SkyOPClient.Module draggingHudModule;
    private int moduleScroll, settingsScroll, hudDragOffsetX, hudDragOffsetY;
    private record HudPreview(SkyOPClient.Module module, int x, int y, int width, int height, String label) { }
    private long animationTicks;
    private String profileStatus = "";
    private int profileStatusTicks;

    private enum SettingKind { TOGGLE, SLIDER, INFO }
    private record Setting(String label, String detail, SettingKind kind,
                           double value, double min, double max, boolean enabled,
                           String displayValue) { }

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
        for (SkyOPClient.Module module : SkyOPClient.Module.values()) {
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
            g.fill(colorX + 16, panelY + 20, colorX + 32, panelY + 28, SkyOPClient.hudAccentColor());
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
        for (SkyOPClient.Category category : SkyOPClient.Category.values()) {
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
        g.text(font, Component.literal(Integer.toString(SkyOPClient.enabledModuleCount())),
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

        List<SkyOPClient.Module> modules = modulesForCategory();
        int cardWidth = (w - 8) / 2;
        int listTop = y + 76;
        int listBottom = panelY + panelHeight - 58;
        int visibleHeight = Math.max(80, listBottom - listTop);
        int rowCount = (modules.size() + 1) / 2;
        int maxScroll = Math.max(0, rowCount * 60 - visibleHeight + 10);
        moduleScroll = Math.min(moduleScroll, maxScroll);
        g.enableScissor(x, listTop, x + w, listBottom);
        int index = 0;
        SkyOPClient.Module hoveredModule = null;
        for (SkyOPClient.Module module : modules) {
            int col = index % 2;
            int row = index / 2;
            int cardX = x + col * (cardWidth + 8);
            int cardY = listTop + row * 60 - moduleScroll;
            boolean enabled = SkyOPClient.isEnabled(module);
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
            g.text(font, Component.literal(SkyOPClient.isFavorite(module) ? "★" : "☆"),
                    favoriteX, cardY + 8, SkyOPClient.isFavorite(module) ? 0xFFFFC85A : MUTED, true);
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

        boolean enabled = SkyOPClient.isEnabled(selectedModule);
        g.text(font, Component.literal(selectedModule.title()), x + 12, y + 31, TEXT, true);
        g.text(font, Component.literal(enabled ? "● " + SkyOPLanguage.text("gui.enabled") : "○ " + SkyOPLanguage.text("gui.disabled")),
                x + 12, y + 47, enabled ? GREEN : MUTED, true);
        g.fill(x + 12, y + 66, x + w - 12, y + 67, 0xFF2B394A);

        KeyMapping mapping = SkyOPClient.keybindFor(selectedModule);
        String keyName = waitingForKeybind ? SkyOPLanguage.text("gui.press_a_key") :
                (mapping == null ? SkyOPLanguage.text("gui.unbound") : mapping.getTranslatedKeyMessage().getString());
        int keyY = y + 72;
        boolean keyHovered = inside(mouseX, mouseY, x + 8, keyY, w - 16, 24);
        g.fill(x + 8, keyY, x + w - 8, keyY + 23,
                keyHovered || waitingForKeybind ? 0xFF263D53 : PANEL_LIGHT);
        g.text(font, Component.literal(SkyOPLanguage.text("gui.keybind") + "  " + trim(keyName, 20)),
                x + 15, keyY + 8, waitingForKeybind ? 0xFFFFC85A : ACCENT, true);

        if (selectedModule == SkyOPClient.Module.PROFILE_MANAGER) {
            drawProfileControls(g, mouseX, mouseY, x, y);
            return;
        }
        if (selectedModule == SkyOPClient.Module.HOTBAR_PRESETS) {
            drawHotbarControls(g, mouseX, mouseY, x, y);
            return;
        }
        if (selectedModule == SkyOPClient.Module.TEXT_GUI) {
            g.text(font, Component.literal(editingHudText ? SkyOPLanguage.text("gui.edit_text") + ": " + SkyOPClient.customHudText + "|" : SkyOPLanguage.text("gui.custom_hud_text")),
                    x + 12, y + 125, editingHudText ? 0xFFFFC85A : TEXT, true);
            g.text(font, Component.literal(SkyOPLanguage.text("gui.edit_text_help")),
                    x + 12, y + 145, MUTED, true);
            return;
        }

        List<Setting> settings = settingsFor(selectedModule);
        int rowY = y + 104 - settingsScroll * 42;
        int settingsBottom = y + h - 8;
        g.enableScissor(x + 8, y + 100, x + w - 8, settingsBottom);
        for (Setting setting : settings) {
            int rowH = 39;
            boolean hovered = inside(mouseX, mouseY, x + 8, rowY, w - 16, rowH);
            g.fill(x + 8, rowY, x + w - 8, rowY + rowH - 2,
                    hovered ? PANEL_HOVER : PANEL_LIGHT);
            g.text(font, Component.literal(trim(setting.label(), 21)),
                    x + 15, rowY + 5, TEXT, true);

            int valueColor = setting.kind() == SettingKind.TOGGLE
                    ? (setting.enabled() ? GREEN : RED) : ACCENT;
            g.text(font, Component.literal(setting.displayValue()),
                    x + w - 68, rowY + 5, valueColor, true);
            g.text(font, Component.literal(trim(setting.detail(), 31)),
                    x + 15, rowY + 19, MUTED, true);

            if (setting.kind() == SettingKind.SLIDER) {
                int trackX = x + 15, trackY = rowY + 32, trackW = w - 30;
                g.fill(trackX, trackY, trackX + trackW, trackY + 3, 0xFF36465A);
                double fraction = (setting.value() - setting.min()) / (setting.max() - setting.min());
                int fillW = (int) (trackW * Math.max(0.0, Math.min(1.0, fraction)));
                g.fill(trackX, trackY, trackX + fillW, trackY + 3, ACCENT);
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

    private List<SkyOPClient.Module> modulesForCategory() {
        List<SkyOPClient.Module> modules = new ArrayList<>();
        String query = searchQuery.toLowerCase(java.util.Locale.ROOT);
        for (SkyOPClient.Module module : SkyOPClient.Module.values()) {
            boolean inCategory = selectedCategory == SkyOPClient.Category.FAVORITES
                    ? SkyOPClient.isFavorite(module) : module.category() == selectedCategory;
            boolean matches = query.isEmpty()
                    || module.title().toLowerCase(java.util.Locale.ROOT).contains(query)
                    || module.description().toLowerCase(java.util.Locale.ROOT).contains(query);
            if ((inCategory || !query.isEmpty()) && matches) modules.add(module);
        }
        return modules;
    }

    private int countModules(SkyOPClient.Category category) {
        int count = 0;
        for (SkyOPClient.Module module : SkyOPClient.Module.values()) {
            if (category == SkyOPClient.Category.FAVORITES ? SkyOPClient.isFavorite(module)
                    : module.category() == category) count++;
        }
        return count;
    }

    private List<Setting> settingsFor(SkyOPClient.Module module) {
        return switch (module) {
            case KILL_AURA -> List.of(
                    toggle(SkyOPLanguage.text("setting.target_players"), SkyOPLanguage.text("setting.include_other_players"), SkyOPClient.targetPlayers),
                    toggle(SkyOPLanguage.text("setting.target_mobs"), SkyOPLanguage.text("setting.include_hostile_mobs"), SkyOPClient.targetMobs),
                    toggle(SkyOPLanguage.text("setting.smooth_rotation"), SkyOPLanguage.text("setting.rotate_target"), SkyOPClient.auraRotate),
                    toggle(SkyOPLanguage.text("setting.line_of_sight"), SkyOPLanguage.text("setting.clear_view"), SkyOPClient.auraRequireLineOfSight),
                    slider(SkyOPLanguage.text("setting.attack_range"), SkyOPLanguage.text("setting.distance_blocks"), SkyOPClient.auraRange, 2.5, 6.0, "%.1f"),
                    toggle(SkyOPLanguage.text("setting.lowest_health"), SkyOPLanguage.text("setting.weakest_target"), SkyOPClient.auraTargetLowestHealth),
                    slider(SkyOPLanguage.text("setting.aim_fov"), SkyOPLanguage.text("setting.target_cone"), SkyOPClient.auraFov, 30, 180, "%.0f°"),
                    slider(SkyOPLanguage.text("setting.rotation_speed"), SkyOPLanguage.text("setting.rotation_interpolation"), SkyOPClient.auraRotationSpeed, 0.10, 1.0, "%.2f")
            );
            case TRIGGER_BOT -> List.of(
                    toggle(SkyOPLanguage.text("setting.target_players"), SkyOPLanguage.text("setting.include_other_players"), SkyOPClient.targetPlayers),
                    toggle(SkyOPLanguage.text("setting.target_mobs"), SkyOPLanguage.text("setting.include_hostile_mobs"), SkyOPClient.targetMobs),
                    slider(SkyOPLanguage.text("setting.trigger_delay"), SkyOPLanguage.text("setting.delay_between"), SkyOPClient.triggerDelayTicks, 1, 12, "%.0f")
            );
            case AUTO_CLICKER -> List.of(
                    slider(SkyOPLanguage.text("setting.clicks_per_second"), SkyOPLanguage.text("setting.vanilla_cooldown"), SkyOPClient.autoClickerCps, 4, 20, "%.0f CPS")
            );
            case SPEED -> List.of(
                    slider(SkyOPLanguage.text("setting.movement_multiplier"), SkyOPLanguage.text("setting.horizontal_multiplier"), SkyOPClient.speedMultiplier, 1.0, 2.8, "%.2fx")
            );
            case FLIGHT -> List.of(
                    slider(SkyOPLanguage.text("setting.flight_speed"), SkyOPLanguage.text("setting.vertical_speed"), SkyOPClient.flightSpeed, 0.12, 0.9, "%.2f")
            );
            case AUTO_JUMP -> List.of(
                    slider(SkyOPLanguage.text("setting.jump_interval"), SkyOPLanguage.text("setting.ticks_between_jumps"), SkyOPClient.autoJumpDelayTicks, 2, 12, "%.0f ticks")
            );
            case SPIDER -> List.of(
                    slider(SkyOPLanguage.text("setting.climb_speed"), SkyOPLanguage.text("setting.wall_vertical_speed"), SkyOPClient.spiderClimbSpeed, 0.08, 0.38, "%.2f")
            );
            case ENTITY_ESP -> List.of(
                    toggle(SkyOPLanguage.text("setting.show_players"), SkyOPLanguage.text("setting.highlight_players"), SkyOPClient.espPlayers),
                    toggle(SkyOPLanguage.text("setting.show_mobs"), SkyOPLanguage.text("setting.highlight_mobs"), SkyOPClient.espMobs),
                    slider(SkyOPLanguage.text("setting.esp_range"), SkyOPLanguage.text("setting.scan_radius"), SkyOPClient.espRange, 8, 96, "%.0f blocks")
            );
            case GAMMA_BOOST -> List.of(info(SkyOPLanguage.text("setting.vanilla_gamma"), SkyOPLanguage.text("setting.max_gamma")));
            case TARGET_HUD -> List.of(info(SkyOPLanguage.text("setting.target_card"), SkyOPLanguage.text("setting.crosshair_target")));
            case KEYSTROKES_HUD -> List.of(info(SkyOPLanguage.text("setting.key_display"), SkyOPLanguage.text("setting.wasd_jump")));
            case ARMOR_HUD -> List.of(info(SkyOPLanguage.text("setting.player_status"), SkyOPLanguage.text("setting.health_armor")));
            case SPEEDOMETER -> List.of(info(SkyOPLanguage.text("setting.movement_speed"), SkyOPLanguage.text("setting.blocks_second")));
            case CPS_HUD -> List.of(info(SkyOPLanguage.text("setting.attack_rate"), SkyOPLanguage.text("setting.successful_attacks")));
            case POTION_HUD -> List.of(info(SkyOPLanguage.text("gui.potion_effects"), SkyOPLanguage.text("gui.active_effects_duration")));
            case DURABILITY_HUD -> List.of(info(SkyOPLanguage.text("gui.main_hand_item"), SkyOPLanguage.text("gui.remaining_durability")));
            case COMPASS_HUD, DIRECTION_HUD -> List.of(info(SkyOPLanguage.text("gui.direction"), SkyOPLanguage.text("gui.cardinal_heading")));
            case PING_HUD -> List.of(info(SkyOPLanguage.text("gui.latency"), SkyOPLanguage.text("gui.current_multiplayer_ping")));
            case FPS_HUD -> List.of(info(SkyOPLanguage.text("gui.frame_rate"), SkyOPLanguage.text("gui.current_fps")));
            case RADAR_HUD -> List.of(info(SkyOPLanguage.text("gui.loaded_entities"), SkyOPLanguage.text("gui.nearby_loaded_entities")));
            case TEXT_GUI -> List.of(info(SkyOPLanguage.text("gui.custom_hud_text"), SkyOPLanguage.text("gui.edit_text_help")));
            case AUTO_EAT -> List.of(slider(SkyOPLanguage.text("setting.hunger_threshold"),
                    SkyOPLanguage.text("setting.eat_below"), SkyOPClient.autoEatThreshold, 2, 18, "%.0f"));
            case AUTO_REFILL -> List.of(slider(SkyOPLanguage.text("setting.refill_threshold"),
                    SkyOPLanguage.text("setting.refill_low"), SkyOPClient.autoRefillThreshold, 1, 16, "%.0f"));
            case AUTO_ARMOR -> List.of(info(SkyOPLanguage.text("gui.armor_policy"), SkyOPLanguage.text("gui.only_empty_armor_slots")));
            case AUTO_TOOL -> List.of(info(SkyOPLanguage.text("gui.tool_selection"), SkyOPLanguage.text("gui.fastest_tool")));
            case AUTO_FISH -> List.of(info(SkyOPLanguage.text("gui.fishing_automation"), SkyOPLanguage.text("gui.reel_and_recast")));
            case INVENTORY_CLEANER -> List.of(info(SkyOPLanguage.text("gui.safe_cleanup"), SkyOPLanguage.text("gui.stack_consolidation")));
            case HOTBAR_PRESETS -> List.of(info(SkyOPLanguage.text("gui.three_layouts"), SkyOPLanguage.text("gui.save_restore_hotbar")));
            case PROFILE_MANAGER -> List.of(info(SkyOPLanguage.text("gui.json_profiles"), SkyOPLanguage.text("gui.profile_slots_backup_restore")));
            case AUTO_SPRINT, STEP_ASSIST, NO_FALL, HUD ->
                    List.of(info(SkyOPLanguage.text("setting.module_status"), SkyOPLanguage.text("setting.toggle_module_list")));
        };
    }

    private static Setting toggle(String label, String detail, boolean value) {
        return new Setting(label, detail, SettingKind.TOGGLE, value ? 1 : 0,
                0, 1, value, SkyOPLanguage.text(value ? "gui.on" : "gui.off"));
    }

    private static Setting slider(String label, String detail, double value,
                                  double min, double max, String format) {
        return new Setting(label, detail, SettingKind.SLIDER, value, min, max,
                false, String.format(format, value));
    }

    private static Setting info(String label, String detail) {
        return new Setting(label, detail, SettingKind.INFO, 0, 0, 1, false, SkyOPLanguage.text("gui.info"));
    }

    private void applySettingClick(int index, double mouseX) {
        List<Setting> settings = settingsFor(selectedModule);
        if (index < 0 || index >= settings.size()) return;
        Setting setting = settings.get(index);
        if (setting.kind() == SettingKind.SLIDER) {
            int sliderX = panelX + panelWidth - 248;
            int sliderWidth = 216;
            double fraction = Math.max(0.0, Math.min(1.0, (mouseX - sliderX) / sliderWidth));
            setSliderValue(selectedModule, index, setting.min() + fraction * (setting.max() - setting.min()));
        } else if (setting.kind() == SettingKind.TOGGLE) {
            toggleSetting(selectedModule, index);
        }
        SkyOPClient.saveConfig();
    }

    private void toggleSetting(SkyOPClient.Module module, int index) {
        switch (module) {
            case KILL_AURA -> {
                switch (index) {
                    case 0 -> SkyOPClient.targetPlayers = !SkyOPClient.targetPlayers;
                    case 1 -> SkyOPClient.targetMobs = !SkyOPClient.targetMobs;
                    case 2 -> SkyOPClient.auraRotate = !SkyOPClient.auraRotate;
                    case 3 -> SkyOPClient.auraRequireLineOfSight = !SkyOPClient.auraRequireLineOfSight;
                    case 5 -> SkyOPClient.auraTargetLowestHealth = !SkyOPClient.auraTargetLowestHealth;
                }
            }
            case TRIGGER_BOT -> {
                if (index == 0) SkyOPClient.targetPlayers = !SkyOPClient.targetPlayers;
                if (index == 1) SkyOPClient.targetMobs = !SkyOPClient.targetMobs;
            }
            case ENTITY_ESP -> {
                if (index == 0) SkyOPClient.espPlayers = !SkyOPClient.espPlayers;
                if (index == 1) SkyOPClient.espMobs = !SkyOPClient.espMobs;
            }
            default -> { }
        }
    }

    private void setSliderValue(SkyOPClient.Module module, int index, double value) {
        switch (module) {
            case KILL_AURA -> {
                if (index == 4) SkyOPClient.auraRange = round(value, 1);
                if (index == 6) SkyOPClient.auraFov = Math.round(value);
                if (index == 7) SkyOPClient.auraRotationSpeed = round(value, 2);
            }
            case TRIGGER_BOT -> {
                if (index == 2) SkyOPClient.triggerDelayTicks = SkyOPClient.clamp((int) Math.round(value), 1, 12);
            }
            case AUTO_CLICKER -> SkyOPClient.autoClickerCps = SkyOPClient.clamp((int) Math.round(value), 4, 20);
            case SPEED -> SkyOPClient.speedMultiplier = round(value, 2);
            case FLIGHT -> SkyOPClient.flightSpeed = round(value, 2);
            case AUTO_JUMP -> SkyOPClient.autoJumpDelayTicks = SkyOPClient.clamp((int) Math.round(value), 2, 12);
            case SPIDER -> SkyOPClient.spiderClimbSpeed = round(value, 2);
            case ENTITY_ESP -> {
                if (index == 2) SkyOPClient.espRange = Math.round(value);
            }
            case AUTO_EAT -> SkyOPClient.autoEatThreshold = SkyOPClient.clamp((int) Math.round(value), 2, 18);
            case AUTO_REFILL -> SkyOPClient.autoRefillThreshold = SkyOPClient.clamp((int) Math.round(value), 1, 16);
            default -> { }
        }
    }

    private static double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
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
                    hexColorInput = SkyOPColorCodec.formatRgb(SkyOPClient.hudAccentColor());
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
                SkyOPClient.cycleLanguage();
                return true;
            }
            int presetY = footerY + 7;
            if (inside(mx, my, panelX + 130, presetY, 48, 24)) {
                SkyOPClient.applyPreset(Minecraft.getInstance(), SkyOPClient.Preset.PVE);
                return true;
            }
            if (inside(mx, my, panelX + 184, presetY, 48, 24)) {
                SkyOPClient.applyPreset(Minecraft.getInstance(), SkyOPClient.Preset.OP);
                return true;
            }
            if (inside(mx, my, panelX + 238, presetY, 66, 24)) {
                SkyOPClient.applyPreset(Minecraft.getInstance(), SkyOPClient.Preset.VISUAL);
                return true;
            }

            int disableX = panelX + panelWidth - 144;
            if (inside(mx, my, disableX, footerY + 7, 124, 24)) {
                SkyOPClient.disableAll(Minecraft.getInstance());
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
        for (SkyOPClient.Category category : SkyOPClient.Category.values()) {
            int rowY = navY + navIndex * 36;
            if (button == InputConstants.MOUSE_BUTTON_LEFT && inside(mx, my, navX, rowY, 116, 30)) {
                selectedCategory = category;
                selectFirstModule();
                return true;
            }
            navIndex++;
        }

        int moduleIndex = 0;
        for (SkyOPClient.Module module : modulesForCategory()) {
            int col = moduleIndex % 2;
            int row = moduleIndex / 2;
            int cardX = moduleX + col * (cardWidth + 8);
            int cardY = moduleTop + row * 60 - moduleScroll;
            int favoriteX = cardX + cardWidth - 74;
            if (button == InputConstants.MOUSE_BUTTON_LEFT
                    && inside(mx, my, favoriteX, cardY + 3, 18, 18)) {
                SkyOPClient.toggleFavorite(module);
                return true;
            }
            if (inside(mx, my, cardX, cardY, cardWidth, 52)) {
                selectedModule = module;
                settingsScroll = 0;
                if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                    SkyOPClient.toggle(Minecraft.getInstance(), module);
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

            if (inside(mx, my, inspectorX + 8, inspectorY + 72, 232, 24)) {
                if (SkyOPClient.keybindFor(selectedModule) != null) waitingForKeybind = true;
                return true;
            }

            if (selectedModule == SkyOPClient.Module.TEXT_GUI
                    && inside(mx, my, inspectorX + 8, inspectorY + 115, 232, 40)) {
                editingHudText = true;
                return true;
            }

            if (selectedModule == SkyOPClient.Module.PROFILE_MANAGER) {
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

            if (selectedModule == SkyOPClient.Module.HOTBAR_PRESETS) {
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
            int settingsY = inspectorY + 104;
            int visibleRow = (my - settingsY) / 42;
            int rowIndex = visibleRow + settingsScroll;
            List<Setting> settings = settingsFor(selectedModule);
            if (mx >= settingsX && mx <= inspectorX + 240
                    && my >= settingsY && visibleRow >= 0 && rowIndex < settings.size()) {
                applySettingClick(rowIndex, mouseX);
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
                SkyOPClient.setHudAccentColor(colors[row][col]);
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
            SkyOPClient.setHudAccentColor(SkyOPColorCodec.parseRgb(hexColorInput));
            hexColorInput = SkyOPColorCodec.formatRgb(SkyOPClient.hudAccentColor());
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
            if (colors[row][col] == SkyOPClient.hudAccentColor()) g.outline(px - 2, py - 2, 26, 26, TEXT);
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
        if (SkyOPClient.isEnabled(SkyOPClient.Module.HUD)) {
            int mainHudHeight = Math.min(sh, 70 + Math.max(0, SkyOPClient.enabledModuleCount() - 1) * 12);
            previews.add(hudPreview(SkyOPClient.Module.HUD, 8, 8, 190,
                    mainHudHeight, SkyOPLanguage.text("gui.main_hud"), sw, sh));
        }
        if (SkyOPClient.isEnabled(SkyOPClient.Module.TARGET_HUD))
            previews.add(hudPreview(SkyOPClient.Module.TARGET_HUD, sw - 194, 8, 186, 58, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.KEYSTROKES_HUD))
            previews.add(hudPreview(SkyOPClient.Module.KEYSTROKES_HUD, 10, sh - 62, 86, 42, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.ARMOR_HUD))
            previews.add(hudPreview(SkyOPClient.Module.ARMOR_HUD, sw - 150, sh - 38, 142, 28, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.POTION_HUD))
            previews.add(hudPreview(SkyOPClient.Module.POTION_HUD, sw - 198, 76, 190, 96, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.DURABILITY_HUD))
            previews.add(hudPreview(SkyOPClient.Module.DURABILITY_HUD, sw / 2 - 82, sh - 94, 164, 24, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.COMPASS_HUD)
                || SkyOPClient.isEnabled(SkyOPClient.Module.DIRECTION_HUD)) {
            String label = SkyOPClient.isEnabled(SkyOPClient.Module.COMPASS_HUD)
                    ? SkyOPLanguage.text("gui.compass_direction") : SkyOPClient.Module.DIRECTION_HUD.title();
            previews.add(hudPreview(SkyOPClient.Module.COMPASS_HUD, sw / 2 - 48, 8, 96, 23, label, sw, sh));
        }
        if (SkyOPClient.isEnabled(SkyOPClient.Module.PING_HUD))
            previews.add(hudPreview(SkyOPClient.Module.PING_HUD, sw - 100, 8, 92, 22, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.FPS_HUD))
            previews.add(hudPreview(SkyOPClient.Module.FPS_HUD, sw - 100, 34, 92, 22, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.RADAR_HUD))
            previews.add(hudPreview(SkyOPClient.Module.RADAR_HUD, sw - 202, sh / 2 - 12, 194, 90, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.TEXT_GUI)) {
            int textWidth = Math.min(sw, Math.max(120, Math.min(480, font.width(SkyOPClient.customHudText) + 16)));
            previews.add(hudPreview(SkyOPClient.Module.TEXT_GUI, 8, sh - 27, textWidth, 19, null, sw, sh));
        }
        if (SkyOPClient.isEnabled(SkyOPClient.Module.SPEEDOMETER))
            previews.add(hudPreview(SkyOPClient.Module.SPEEDOMETER, sw / 2 - 58, sh - 37, 116, 25, null, sw, sh));
        if (SkyOPClient.isEnabled(SkyOPClient.Module.CPS_HUD))
            previews.add(hudPreview(SkyOPClient.Module.CPS_HUD, sw / 2 - 48, sh - 66, 96, 24, null, sw, sh));
        return previews;
    }

    private HudPreview hudPreview(SkyOPClient.Module module, int defaultX, int defaultY,
                                  int previewWidth, int previewHeight, String label,
                                  int screenWidth, int screenHeight) {
        int x = SkyOPClient.hudXFor(module, defaultX, screenWidth, previewWidth);
        int y = SkyOPClient.hudYFor(module, defaultY, screenHeight, previewHeight);
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
                + SkyOPLanguage.text("gui.drag_hud")), 12, 12, SkyOPClient.hudAccentColor(), true);
        for (HudPreview preview : buildHudPreviews()) {
            boolean hovered = inside(mouseX, mouseY, preview.x(), preview.y(), preview.width(), preview.height());
            int border = hovered ? TEXT : SkyOPClient.hudAccentColor();
            g.fill(preview.x(), preview.y(), preview.x() + preview.width(), preview.y() + preview.height(),
                    hovered ? 0xE51C2A3B : 0xD5101726);
            g.outline(preview.x(), preview.y(), preview.width(), preview.height(), border);
            g.fill(preview.x(), preview.y(), preview.x() + preview.width(), preview.y() + 2,
                    SkyOPClient.hudAccentColor());
            g.text(font, Component.literal(trim(preview.label(), 22)),
                    preview.x() + 6, preview.y() + 7, TEXT, true);
            if (preview.height() > 34) {
                g.text(font, Component.literal(preview.x() + ", " + preview.y()),
                        preview.x() + 6, preview.y() + 21, MUTED, true);
                g.text(font, Component.literal(SkyOPLanguage.text("gui.drag_to_move")),
                        preview.x() + 6, preview.y() + preview.height() - 12,
                        SkyOPClient.hudAccentColor(), true);
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
        if (editingHudText && selectedModule == SkyOPClient.Module.TEXT_GUI) {
            if (event.isAllowedChatCharacter() && SkyOPClient.customHudText.length() < 80) {
                SkyOPClient.setCustomHudText(SkyOPClient.customHudText + event.codepointAsString());
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
            SkyOPClient.setModuleKeybind(selectedModule, event.key());
            waitingForKeybind = false;
            setProfileStatus(SkyOPLanguage.text("gui.keybind_updated"));
            return true;
        }
        if (editingHudText) {
            if (event.key() == InputConstants.KEY_ESCAPE || event.key() == InputConstants.KEY_RETURN) {
                editingHudText = false;
                return true;
            }
            if (event.key() == InputConstants.KEY_BACKSPACE && !SkyOPClient.customHudText.isEmpty()) {
                SkyOPClient.setCustomHudText(SkyOPClient.customHudText.substring(0, SkyOPClient.customHudText.length() - 1));
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
                SkyOPClient.setHudPosition(draggingHudModule,
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
            SkyOPClient.saveConfig();
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
        SkyOPClient.saveConfig();
        super.onClose();
    }
}
