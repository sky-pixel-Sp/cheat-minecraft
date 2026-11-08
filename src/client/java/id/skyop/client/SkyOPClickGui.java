package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
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
    }

    private void drawHeader(GuiGraphicsExtractor g) {
        g.text(font, Component.literal("S K Y O P"), panelX + 20, panelY + 14, ACCENT, true);
        g.text(font, Component.literal("CLIENT  /  26.3"), panelX + 112, panelY + 14, MUTED, true);
        g.text(font, Component.literal("MODULE CONTROL CENTER"), panelX + 20, panelY + 34, TEXT, true);
        g.text(font, Component.literal("Profiles • Modules • Live settings"), panelX + 210, panelY + 34, MUTED, true);

        int statusX = panelX + panelWidth - 172;
        g.fill(statusX, panelY + 13, statusX + 104, panelY + 35, PANEL_LIGHT);
        g.fill(statusX + 9, panelY + 21, statusX + 15, panelY + 27, GREEN);
        g.text(font, Component.literal("CLIENT READY"), statusX + 21, panelY + 20, TEXT, true);
        int closeX = panelX + panelWidth - 48;
        g.fill(closeX, panelY + 12, closeX + 30, panelY + 35, PANEL_LIGHT);
        g.text(font, Component.literal("×"), closeX + 11, panelY + 16, TEXT, true);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = panelX + 12, y = panelY + 62, w = 126;
        g.fill(x, y, x + w, panelY + panelHeight - 48, 0xFF111A27);
        g.text(font, Component.literal("CATEGORIES"), x + 10, y + 10, MUTED, true);
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
        g.text(font, Component.literal("ACTIVE MODULES"), x + 10, activeY + 12, MUTED, true);
        g.text(font, Component.literal(Integer.toString(SkyOPClient.enabledModuleCount())),
                x + 10, activeY + 30, ACCENT, true);
    }

    private void drawModuleList(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = panelX + 151, y = panelY + 63, w = panelWidth - 430;
        g.text(font, Component.literal(selectedCategory.title().toUpperCase() + " MODULES"),
                x, y + 8, TEXT, true);
        g.text(font, Component.literal("Left click: toggle  •  Right click: inspect"),
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
        g.text(font, Component.literal("MODULE INSPECTOR"), x + 12, y + 10, MUTED, true);
        if (selectedModule == null) {
            g.text(font, Component.literal("Select a module"), x + 12, y + 42, TEXT, true);
            return;
        }

        boolean enabled = SkyOPClient.isEnabled(selectedModule);
        g.text(font, Component.literal(selectedModule.title()), x + 12, y + 31, TEXT, true);
        g.text(font, Component.literal(enabled ? "● ENABLED" : "○ DISABLED"),
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
            g.text(font, Component.literal("No extra settings"), x + 12, y + 91, MUTED, true);
            g.text(font, Component.literal(selectedModule.description()), x + 12, y + 110, TEXT, true);
        }
    }

    private void drawFooter(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int y = panelY + panelHeight - 38;
        g.fill(panelX + 12, y, panelX + panelWidth - 12, y + 1, 0xFF2B394A);
        g.text(font, Component.literal("QUICK PROFILES"), panelX + 20, y + 15, MUTED, true);

        drawProfileButton(g, mouseX, mouseY, panelX + 130, y + 7, 48, "PVE");
        drawProfileButton(g, mouseX, mouseY, panelX + 184, y + 7, 48, "OP");
        drawProfileButton(g, mouseX, mouseY, panelX + 238, y + 7, 66, "VISUAL");

        int bx = panelX + panelWidth - 144;
        boolean hovered = inside(mouseX, mouseY, bx, y + 7, 124, 24);
        g.fill(bx, y + 7, bx + 124, y + 31, hovered ? 0xFF693542 : 0xFF482B35);
        g.text(font, Component.literal("DISABLE ALL"), bx + 31, y + 15, 0xFFFFB8C0, true);
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
        for (SkyOPClient.Module module : SkyOPClient.Module.values()) {
            if (module.category() == selectedCategory) modules.add(module);
        }
        return modules;
    }

    private int countModules(SkyOPClient.Category category) {
        int count = 0;
        for (SkyOPClient.Module module : SkyOPClient.Module.values()) {
            if (module.category() == category) count++;
        }
        return count;
    }

    private List<Setting> settingsFor(SkyOPClient.Module module) {
        return switch (module) {
            case KILL_AURA -> List.of(
                    toggle("Target players", "Include other players", SkyOPClient.targetPlayers),
                    toggle("Target hostile mobs", "Include hostile mobs", SkyOPClient.targetMobs),
                    toggle("Smooth rotation", "Rotate toward selected target", SkyOPClient.auraRotate),
                    toggle("Line of sight", "Require a clear view", SkyOPClient.auraRequireLineOfSight),
                    slider("Attack range", "Target distance in blocks", SkyOPClient.auraRange, 2.5, 6.0, "%.1f"),
                    toggle("Lowest health first", "Prioritise the weakest target", SkyOPClient.auraTargetLowestHealth),
                    slider("Aim field of view", "Target cone width in degrees", SkyOPClient.auraFov, 30, 180, "%.0f°"),
                    slider("Rotation speed", "Aim interpolation from slow to instant", SkyOPClient.auraRotationSpeed, 0.10, 1.0, "%.2f")
            );
            case TRIGGER_BOT -> List.of(
                    toggle("Target players", "Use the combat target filter", SkyOPClient.targetPlayers),
                    toggle("Target hostile mobs", "Use the combat target filter", SkyOPClient.targetMobs),
                    slider("Trigger delay", "Ticks between attack attempts", SkyOPClient.triggerDelayTicks, 1, 12, "%.0f")
            );
            case AUTO_CLICKER -> List.of(
                    slider("Clicks per second", "Respects vanilla attack cooldown", SkyOPClient.autoClickerCps, 4, 20, "%.0f CPS")
            );
            case SPEED -> List.of(
                    slider("Movement multiplier", "Horizontal movement multiplier", SkyOPClient.speedMultiplier, 1.0, 2.8, "%.2fx")
            );
            case FLIGHT -> List.of(
                    slider("Flight speed", "Vertical speed per tick", SkyOPClient.flightSpeed, 0.12, 0.9, "%.2f")
            );
            case AUTO_JUMP -> List.of(
                    slider("Jump interval", "Ticks between automatic jumps", SkyOPClient.autoJumpDelayTicks, 2, 12, "%.0f ticks")
            );
            case SPIDER -> List.of(
                    slider("Climb speed", "Vertical motion while touching a wall", SkyOPClient.spiderClimbSpeed, 0.08, 0.38, "%.2f")
            );
            case ENTITY_ESP -> List.of(
                    toggle("Show players", "Highlight loaded player entities", SkyOPClient.espPlayers),
                    toggle("Show mobs", "Highlight loaded mob entities", SkyOPClient.espMobs),
                    slider("ESP range", "Entity scan radius in blocks", SkyOPClient.espRange, 8, 96, "%.0f blocks")
            );
            case GAMMA_BOOST -> List.of(info("Vanilla gamma", "Uses the maximum normal gamma value"));
            case TARGET_HUD -> List.of(info("Target card", "Shows the entity under your crosshair"));
            case KEYSTROKES_HUD -> List.of(info("Key display", "Shows W A S D and jump input"));
            case ARMOR_HUD -> List.of(info("Player status", "Shows current health and armor points"));
            case SPEEDOMETER -> List.of(info("Movement speed", "Horizontal blocks per second"));
            case CPS_HUD -> List.of(info("Attack rate", "Successful attack actions per second"));
            case AUTO_SPRINT, STEP_ASSIST, NO_FALL, HUD ->
                    List.of(info("Module status", "Toggle this module from the module list"));
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
