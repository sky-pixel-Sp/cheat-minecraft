package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import id.skyop.common.SkyOPDiagnostics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Runtime diagnostics and module-health dashboard.
 *
 * The screen intentionally reads the live client and module registry instead
 * of displaying placeholder telemetry. Module cards can be toggled directly.
 */
public final class SkyOPDiagnosticsScreen extends Screen {
    private static final int OUTER_MARGIN = 12;
    private static final int FOOTER_HEIGHT = 36;
    private static final int PAGE_SIZE = 12;

    private final Screen parent;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int page;

    public SkyOPDiagnosticsScreen(Screen parent) {
        super(Component.literal("SkyOP Diagnostics"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        calculateBounds();
    }

    private void calculateBounds() {
        panelWidth = Math.max(1, Math.min(980, width - OUTER_MARGIN * 2));
        panelHeight = Math.max(1, Math.min(640, height - OUTER_MARGIN * 2));
        panelX = Math.max(0, (width - panelWidth) / 2);
        panelY = Math.max(0, (height - panelHeight) / 2);
        int pageCount = Math.max(1, (SkyOPModuleId.values().length + pageSize() - 1) / pageSize());
        page = SkyOPMath.clamp(page, 0, pageCount - 1);
    }

    private int pageSize() {
        return panelHeight < 455 ? 8 : PAGE_SIZE;
    }

    private boolean compact() {
        return panelWidth < 690 || panelHeight < 430;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        calculateBounds();

        Minecraft client = Minecraft.getInstance();
        SkyOPTheme.Palette palette = SkyOPTheme.palette(SkyOPState.uiTheme);
        boolean online = client.player != null && client.level != null;
        boolean idLanguage = "id".equals(SkyOPLanguage.code());

        graphics.fill(0, 0, width, height, 0xC9000712);
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1,
                palette.hover());
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, palette.panel());
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 3, palette.accent());

        drawHeader(graphics, mouseX, mouseY, palette, online, idLanguage);
        drawMetrics(graphics, client, palette, idLanguage);
        drawRuntimePanel(graphics, client, palette, idLanguage);
        drawFooter(graphics, mouseX, mouseY, palette, idLanguage);
    }

    private void drawHeader(GuiGraphicsExtractor g, int mouseX, int mouseY,
                            SkyOPTheme.Palette p, boolean online, boolean idLanguage) {
        int y = panelY + 13;
        g.text(font, Component.literal("SKYOP"), panelX + 18, y, p.accent(), true);
        g.text(font, Component.literal(idLanguage ? "DIAGNOSTIK" : "DIAGNOSTICS"),
                panelX + 78, y, p.text(), true);
        g.text(font, Component.literal(idLanguage
                        ? "Status runtime dan kesehatan modul"
                        : "Runtime status and module health"),
                panelX + 18, y + 20, p.muted(), false);

        int closeX = panelX + panelWidth - 42;
        boolean closeHover = inside(mouseX, mouseY, closeX, panelY + 10, 28, 24);
        g.fill(closeX, panelY + 10, closeX + 28, panelY + 34,
                closeHover ? p.hover() : p.elevated());
        g.text(font, Component.literal("×"), closeX + 10, panelY + 17, p.text(), true);

        int statusWidth = compact() ? 88 : 126;
        int statusX = closeX - statusWidth - 10;
        g.fill(statusX, panelY + 10, statusX + statusWidth, panelY + 34, p.elevated());
        g.fill(statusX + 9, panelY + 19, statusX + 15, panelY + 25,
                online ? p.success() : p.muted());
        String status = online
                ? (idLanguage ? "DALAM DUNIA" : "IN WORLD")
                : (idLanguage ? "MENU / OFFLINE" : "MENU / OFFLINE");
        g.text(font, Component.literal(status), statusX + 21, panelY + 18,
                online ? p.success() : p.muted(), true);
    }

    private void drawMetrics(GuiGraphicsExtractor g, Minecraft client,
                             SkyOPTheme.Palette p, boolean idLanguage) {
        int top = panelY + 58;
        int height = compact() ? 49 : 58;
        int gap = 8;
        int available = Math.max(4, panelWidth - 32 - gap * 3);
        int cardWidth = available / 4;
        String[] labels = idLanguage
                ? new String[]{"DIKTIFKAN", "AKTIF", "FPS KLIEN", "PING"}
                : new String[]{"ENABLED", "ACTIVE", "CLIENT FPS", "PING"};
        String[] values = {
                Integer.toString(SkyOPCore.enabledModuleCount()),
                Integer.toString(SkyOPModuleManager.activeCount()),
                Integer.toString(client.getFps()),
                formatPing(SkyOPMath.getCurrentPing(client))
        };
        for (int i = 0; i < 4; i++) {
            int x = panelX + 16 + i * (cardWidth + gap);
            g.fill(x, top, x + cardWidth, top + height, p.elevated());
            g.fill(x, top, x + 3, top + height, i == 1 ? p.success() : p.accent());
            g.text(font, Component.literal(labels[i]), x + 11, top + 9, p.muted(), true);
            g.text(font, Component.literal(values[i]), x + 11, top + 26, p.text(), true);
        }
    }

    private void drawRuntimePanel(GuiGraphicsExtractor g, Minecraft client,
                                  SkyOPTheme.Palette p, boolean idLanguage) {
        int top = panelY + (compact() ? 122 : 134);
        int contentBottom = panelY + panelHeight - FOOTER_HEIGHT;
        int bodyHeight = Math.max(1, contentBottom - top - 9);
        int leftWidth = compact() ? Math.max(116, Math.min(174, panelWidth / 3))
                : Math.min(238, Math.max(190, panelWidth / 4));
        int x = panelX + 16;
        g.fill(x, top, x + leftWidth, top + bodyHeight, p.canvas());
        g.outline(x, top, leftWidth, bodyHeight, p.hover());

        g.text(font, Component.literal(idLanguage ? "RINGKASAN" : "RUNTIME OVERVIEW"),
                x + 11, top + 10, p.text(), true);

        String player = client.player == null
                ? (idLanguage ? "Tidak ada pemain" : "No player")
                : trim(client.player.getName().getString(), compact() ? 17 : 26);
        String world = client.level == null
                ? (idLanguage ? "Belum dimuat" : "Not loaded")
                : (idLanguage ? "Terhubung" : "Loaded");
        String health = client.player == null ? "--"
                : String.format(Locale.ROOT, "%.1f / %.1f", client.player.getHealth(),
                client.player.getMaxHealth());
        String food = client.player == null ? "--"
                : Integer.toString(client.player.getFoodData().getFoodLevel());
        Runtime runtime = Runtime.getRuntime();
        long usedMb = Math.max(0L, (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L));
        long maxMb = Math.max(1L, runtime.maxMemory() / (1024L * 1024L));
        String memory = usedMb + " / " + maxMb + " MB";
        String theme = SkyOPTheme.displayName(SkyOPState.uiTheme);

        int rowY = top + 35;
        int rowStep = compact() ? 29 : 34;
        drawRuntimeRow(g, x, rowY, leftWidth, idLanguage ? "PEMAIN" : "PLAYER",
                player, p);
        drawRuntimeRow(g, x, rowY + rowStep, leftWidth, idLanguage ? "DUNIA" : "WORLD",
                world, p);
        drawRuntimeRow(g, x, rowY + rowStep * 2, leftWidth, idLanguage ? "KESEHATAN" : "HEALTH",
                health, p);
        drawRuntimeRow(g, x, rowY + rowStep * 3, leftWidth, idLanguage ? "LAPAR" : "HUNGER",
                food, p);
        drawRuntimeRow(g, x, rowY + rowStep * 4, leftWidth, idLanguage ? "MEMORI JVM" : "JVM MEMORY",
                memory, p);
        if (bodyHeight > rowStep * 5 + 49) {
            drawRuntimeRow(g, x, rowY + rowStep * 5, leftWidth,
                    idLanguage ? "TEMA AKTIF" : "ACTIVE THEME", theme, p);
        }

        int rightX = x + leftWidth + 12;
        int rightWidth = Math.max(1, panelX + panelWidth - 16 - rightX);
        g.fill(rightX, top, rightX + rightWidth, top + bodyHeight, p.canvas());
        g.outline(rightX, top, rightWidth, bodyHeight, p.hover());
        g.text(font, Component.literal(idLanguage ? "KESEHATAN MODUL" : "MODULE HEALTH"),
                rightX + 11, top + 10, p.text(), true);
        int waiting = waitingModuleCount(client);
        String summary = idLanguage
                ? SkyOPModuleManager.activeCount() + " aktif  •  " + waiting + " menunggu"
                : SkyOPModuleManager.activeCount() + " active  •  " + waiting + " waiting";
        g.text(font, Component.literal(trim(summary, Math.max(10, (rightWidth - 24) / 6))),
                rightX + 11, top + 24, p.muted(), false);

        drawModuleCards(g, client, p, idLanguage, rightX + 10, top + 42,
                rightWidth - 20, Math.max(1, bodyHeight - 74));
    }

    private void drawRuntimeRow(GuiGraphicsExtractor g, int x, int y, int width,
                                String label, String value, SkyOPTheme.Palette p) {
        g.text(font, Component.literal(label), x + 11, y, p.muted(), false);
        int maxChars = Math.max(6, (width - 24) / 6);
        g.text(font, Component.literal(trim(value, maxChars)), x + 11, y + 11, p.text(), true);
    }

    private void drawModuleCards(GuiGraphicsExtractor g, Minecraft client,
                                 SkyOPTheme.Palette p, boolean idLanguage,
                                 int x, int y, int width, int height) {
        SkyOPModuleId[] modules = SkyOPModuleId.values();
        int perPage = pageSize();
        int first = page * perPage;
        int end = Math.min(modules.length, first + perPage);
        int count = end - first;
        int columns = width >= 330 ? 2 : 1;
        int gap = 6;
        int cardWidth = Math.max(1, (width - gap * (columns - 1)) / columns);
        int rows = Math.max(1, (count + columns - 1) / columns);
        int rowStep = Math.max(26, Math.min(37, height / Math.max(1, rows)));
        int cardHeight = Math.max(24, Math.min(33, rowStep - 3));

        for (int index = first; index < end; index++) {
            int local = index - first;
            int col = local % columns;
            int row = local / columns;
            int cardX = x + col * (cardWidth + gap);
            int cardY = y + row * rowStep;
            SkyOPModuleId id = modules[index];
            boolean enabled = SkyOPCore.isEnabled(id);
            boolean active = SkyOPModuleManager.isActive(id);
            SkyOPModule module = SkyOPModuleManager.get(id);
            boolean ready = module != null && SkyOPModuleManager.isReadyForDiagnostics(client, id);
            String state = active
                    ? (idLanguage ? "AKTIF" : "ACTIVE")
                    : (enabled && !ready ? (idLanguage ? "MENUNGGU" : "WAITING")
                    : enabled ? (idLanguage ? "SINKRON" : "SYNCING")
                    : ready ? (idLanguage ? "SIAP" : "STANDBY")
                    : (idLanguage ? "KONTEKS" : "CONTEXT"));
            SkyOPDiagnostics.ModuleSnapshot diagnostics = SkyOPClientDiagnostics.get().snapshot(id.name());
            String stateLabel = diagnostics.errorCount() > 0
                    ? state + " / ERR " + diagnostics.errorCount() : state;
            int stateColor = diagnostics.consecutiveErrors() > 0 ? p.danger()
                    : active ? p.success() : enabled && !ready ? p.danger() : p.muted();

            g.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight,
                    p.elevated());
            g.fill(cardX, cardY, cardX + 2, cardY + cardHeight,
                    active ? p.success() : enabled ? p.accent() : p.muted());
            int labelWidth = Math.max(8, (cardWidth - 92) / 6);
            g.text(font, Component.literal(trim(id.title(), labelWidth)),
                    cardX + 8, cardY + 5, p.text(), true);
            g.text(font, Component.literal(stateLabel),
                    cardX + cardWidth - font.width(stateLabel) - 7, cardY + 5,
                    stateColor, true);
            if (!compact() && cardHeight >= 30) {
                String detail = diagnostics.lastReasonText().isBlank()
                        ? id.category().title() : diagnostics.lastReasonText();
                g.text(font, Component.literal(trim(detail, Math.max(8, (cardWidth - 16) / 5))),
                        cardX + 8, cardY + 18, p.muted(), false);
            }
        }
    }

    private void drawFooter(GuiGraphicsExtractor g, int mouseX, int mouseY,
                            SkyOPTheme.Palette p, boolean idLanguage) {
        int y = panelY + panelHeight - FOOTER_HEIGHT + 6;
        int backX = panelX + 16;
        int backWidth = compact() ? 68 : 174;
        String backLabel = compact() ? (idLanguage ? "KEMBALI" : "BACK")
                : (idLanguage ? "KEMBALI KE CLICKGUI" : "BACK TO CLICKGUI");
        drawButton(g, mouseX, mouseY, backX, y, backWidth, 24, backLabel, p);

        int copyX = panelX + (compact() ? 90 : 206);
        int copyWidth = compact() ? 88 : 132;
        String copyLabel = compact() ? (idLanguage ? "SALIN" : "COPY")
                : (idLanguage ? "SALIN LAPORAN" : "COPY REPORT");
        drawButton(g, mouseX, mouseY, copyX, y, copyWidth, 24, copyLabel, p);

        int perPage = pageSize();
        int pageCount = Math.max(1, (SkyOPModuleId.values().length + perPage - 1) / perPage);
        String pageLabel = (page + 1) + " / " + pageCount;
        if (compact()) {
            int pagerX = panelX + panelWidth - 92;
            drawButton(g, mouseX, mouseY, pagerX, y, 24, 24,
                    idLanguage ? "<" : "<", p, page == 0);
            g.text(font, Component.literal(pageLabel), pagerX + 27, y + 8, p.muted(), true);
            drawButton(g, mouseX, mouseY, pagerX + 62, y, 24, 24,
                    ">", p, page >= pageCount - 1);
        } else {
            int pagerX = panelX + panelWidth - 138;
            g.text(font, Component.literal(pageLabel), pagerX + 48, y + 8, p.muted(), true);
            drawButton(g, mouseX, mouseY, pagerX, y, 40, 24,
                    "PREV", p, page == 0);
            drawButton(g, mouseX, mouseY, pagerX + 88, y, 40, 24,
                    "NEXT", p, page >= pageCount - 1);
        }
    }

    private void drawButton(GuiGraphicsExtractor g, int mouseX, int mouseY,
                            int x, int y, int w, int h, String label, SkyOPTheme.Palette p) {
        drawButton(g, mouseX, mouseY, x, y, w, h, label, p, false);
    }

    private void drawButton(GuiGraphicsExtractor g, int mouseX, int mouseY,
                            int x, int y, int w, int h, String label,
                            SkyOPTheme.Palette p, boolean disabled) {
        boolean hovered = !disabled && inside(mouseX, mouseY, x, y, w, h);
        g.fill(x, y, x + w, y + h, disabled ? p.canvas() : hovered ? p.hover() : p.elevated());
        g.outline(x, y, w, h, disabled ? p.hover() : hovered ? p.accent() : p.muted());
        int textColor = disabled ? p.muted() : hovered ? p.text() : p.accent();
        g.text(font, Component.literal(label), x + Math.max(3, (w - font.width(label)) / 2),
                y + Math.max(1, (h - 8) / 2), textColor, true);
    }

    private int waitingModuleCount(Minecraft client) {
        int waiting = 0;
        for (SkyOPModuleId id : SkyOPModuleId.values()) {
            if (!SkyOPCore.isEnabled(id)) continue;
            SkyOPModule module = SkyOPModuleManager.get(id);
            if (module == null || !SkyOPModuleManager.isReadyForDiagnostics(client, id)) waiting++;
        }
        return waiting;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        calculateBounds();
        if (event.input() != InputConstants.MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        int mx = (int) event.x();
        int my = (int) event.y();
        int closeX = panelX + panelWidth - 42;
        int footerY = panelY + panelHeight - FOOTER_HEIGHT + 6;
        int backWidth = compact() ? 68 : 174;
        if (inside(mx, my, closeX, panelY + 10, 28, 24)
                || inside(mx, my, panelX + 16, footerY, backWidth, 24)) {
            returnToParent();
            return true;
        }

        int copyX = panelX + (compact() ? 90 : 206);
        int copyWidth = compact() ? 88 : 132;
        if (inside(mx, my, copyX, footerY, copyWidth, 24)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(SkyOPClientDiagnostics.buildReport());
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal(
                        "SkyOP diagnostics report copied locally; no data was transmitted."));
            }
            return true;
        }

        int perPage = pageSize();
        int pageCount = Math.max(1, (SkyOPModuleId.values().length + perPage - 1) / perPage);
        int pagerY = footerY;
        if (compact()) {
            int pagerX = panelX + panelWidth - 92;
            if (inside(mx, my, pagerX, pagerY, 24, 24) && page > 0) {
                page--;
                return true;
            }
            if (inside(mx, my, pagerX + 62, pagerY, 24, 24) && page < pageCount - 1) {
                page++;
                return true;
            }
        } else {
            int pagerX = panelX + panelWidth - 138;
            if (inside(mx, my, pagerX, pagerY, 40, 24) && page > 0) {
                page--;
                return true;
            }
            if (inside(mx, my, pagerX + 88, pagerY, 40, 24) && page < pageCount - 1) {
                page++;
                return true;
            }
        }

        int top = panelY + (compact() ? 122 : 134);
        int contentBottom = panelY + panelHeight - FOOTER_HEIGHT;
        int bodyHeight = Math.max(1, contentBottom - top - 9);
        int leftWidth = compact() ? Math.max(116, Math.min(174, panelWidth / 3))
                : Math.min(238, Math.max(190, panelWidth / 4));
        int rightX = panelX + 16 + leftWidth + 12;
        int rightWidth = Math.max(1, panelX + panelWidth - 16 - rightX);
        int moduleX = rightX + 10;
        int moduleY = top + 42;
        int moduleWidth = rightWidth - 20;
        int moduleHeight = Math.max(1, bodyHeight - 74);

        SkyOPModuleId[] modules = SkyOPModuleId.values();
        int first = page * perPage;
        int end = Math.min(modules.length, first + perPage);
        int columns = moduleWidth >= 330 ? 2 : 1;
        int gap = 6;
        int cardWidth = Math.max(1, (moduleWidth - gap * (columns - 1)) / columns);
        int rows = Math.max(1, (end - first + columns - 1) / columns);
        int rowStep = Math.max(26, Math.min(37, moduleHeight / Math.max(1, rows)));
        int cardHeight = Math.max(24, Math.min(33, rowStep - 3));
        for (int index = first; index < end; index++) {
            int local = index - first;
            int cardX = moduleX + (local % columns) * (cardWidth + gap);
            int cardY = moduleY + (local / columns) * rowStep;
            if (inside(mx, my, cardX, cardY, cardWidth, cardHeight)) {
                SkyOPCore.toggle(Minecraft.getInstance(), modules[index]);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE
                || event.key() == InputConstants.KEY_BACKSPACE) {
            returnToParent();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    private void returnToParent() {
        Minecraft.getInstance().gui.setScreen(parent);
    }

    private static String formatPing(int ping) {
        return ping < 0 ? "--" : ping + " ms";
    }

    private static String trim(String value, int maxLength) {
        if (value == null || maxLength <= 0) return "";
        if (value.length() <= maxLength) return value;
        if (maxLength <= 3) return value.substring(0, maxLength);
        return value.substring(0, maxLength - 3) + "...";
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return w > 0 && h > 0 && mouseX >= x && mouseX < x + w
                && mouseY >= y && mouseY < y + h;
    }
}
