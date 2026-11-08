package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
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
    private boolean searchFocused, waitingForKeybind, hudEditMode, draggingHud, colorPickerOpen;
    private int moduleScroll, settingsScroll, hudDragOffsetX, hudDragOffsetY;
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
        drawHeader(graphics);
        drawSidebar(graphics, mouseX, mouseY);
        drawModuleList(graphics, mouseX, mouseY);
        drawInspector(graphics, mouseX, mouseY);
        drawFooter(graphics, mouseX, mouseY);
        drawColorPicker(graphics, mouseX, mouseY);
        drawHudEditorPreview(graphics);
    }

    @Override
    public void tick() {
        super.tick();
        animationTicks++;
        if (profileStatusTicks > 0) profileStatusTicks--;
    }

    private void drawHeader(GuiGraphicsExtractor g) {
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
            g.text(font, Component.literal(hudEditMode ? "HUD EDIT ON" : "HUD EDIT"),
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
        g.text(font, Component.literal(SkyOPLanguage.text("gui.left_toggle_right_inspect")),
                x, y + 24, MUTED, true);

        int cardWidth = (w - 8) / 2;
        int index = 0;
        for (SkyOPClient.Module module : modulesForCategory()) {
            int col = index % 2;
            int row = index / 2;
            int cardX = x + col * (cardWidth + 8);
            int cardY = y + 46 + row * 60;
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

            int pillX = cardX + cardWidth - 48;
            g.fill(pillX, cardY + 15, pillX + 40, cardY + 36,
                    enabled ? 0xFF174633 : 0xFF3B2631);
            g.text(font, Component.literal(enabled ? "ON" : "OFF"),
                    pillX + 10, cardY + 22, enabled ? GREEN : RED, true);
            index++;
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

        List<Setting> settings = settingsFor(selectedModule);
        int rowY = y + 78;
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

        if (settings.isEmpty()) {
            g.text(font, Component.literal(SkyOPLanguage.text("gui.no_extra_settings")), x + 12, y + 91, MUTED, true);
            g.text(font, Component.literal(selectedModule.description()), x + 12, y + 110, TEXT, true);
        }
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
            case AUTO_SPRINT, STEP_ASSIST, NO_FALL, HUD ->
                    List.of(info(SkyOPLanguage.text("setting.module_status"), SkyOPLanguage.text("setting.toggle_module_list")));
        };
    }

    private static Setting toggle(String label, String detail, boolean value) {
        return new Setting(label, detail, SettingKind.TOGGLE, value ? 1 : 0,
                0, 1, value, value ? "ON" : "OFF");
    }

    private static Setting slider(String label, String detail, double value,
                                  double min, double max, String format) {
        return new Setting(label, detail, SettingKind.SLIDER, value, min, max,
                false, String.format(format, value));
    }

    private static Setting info(String label, String detail) {
        return new Setting(label, detail, SettingKind.INFO, 0, 0, 1, false, "INFO");
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

        int moduleX = panelX + 151, moduleY = panelY + 109;
        int moduleW = panelWidth - 430, cardWidth = (moduleW - 8) / 2, moduleIndex = 0;
        for (SkyOPClient.Module module : modulesForCategory()) {
            int col = moduleIndex % 2;
            int row = moduleIndex / 2;
            int cardX = moduleX + col * (cardWidth + 8);
            int cardY = moduleY + row * 60;
            if (inside(mx, my, cardX, cardY, cardWidth, 52)) {
                selectedModule = module;
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
            int settingsX = panelX + panelWidth - 255;
            int settingsY = panelY + 141;
            int rowIndex = (my - settingsY) / 42;
            List<Setting> settings = settingsFor(selectedModule);
            if (mx >= settingsX && mx <= panelX + panelWidth - 15
                    && my >= settingsY && rowIndex >= 0 && rowIndex < settings.size()
                    && my < settingsY + settings.size() * 42) {
                applySettingClick(rowIndex, mouseX);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
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
        SkyOPClient.saveConfig();
        super.onClose();
    }
}
