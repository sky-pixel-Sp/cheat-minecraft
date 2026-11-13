package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Multi-page control center for the existing SkyOP client services.
 *
 * Every action in this screen delegates to an existing manager or core action.
 * Pages that do not have a working backend are intentionally not represented
 * by fake controls.
 */
public final class SkyOPControlCenterScreen extends Screen {
    private enum Page {
        OVERVIEW, MODULES, PROFILES, WAYPOINTS, APPEARANCE, DIAGNOSTICS, CONNECTION
    }

    private record Action(String id, String label, int x, int y, int width, int height, boolean danger) { }

    private static final int HEADER_HEIGHT = 44;
    private static final int FOOTER_HEIGHT = 28;
    private static final int BUTTON_HEIGHT = 25;

    private final Screen parent;
    private Page page = Page.OVERVIEW;
    private SkyOPControlCenterLayout layout;
    private List<Action> actions = List.of();
    private String status = "";
    private int statusTicks;
    private int waypointScroll;

    public SkyOPControlCenterScreen() {
        this(null);
    }

    public SkyOPControlCenterScreen(Screen parent) {
        super(Component.literal("SkyOP Control Center"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        calculateLayout();
    }

    private void calculateLayout() {
        layout = SkyOPControlCenterLayout.calculate(width, height);
    }

    @Override
    public void tick() {
        super.tick();
        if (statusTicks > 0 && --statusTicks == 0) status = "";
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        calculateLayout();

        SkyOPTheme.Palette palette = SkyOPTheme.palette(SkyOPState.uiTheme);
        g.fill(0, 0, width, height, 0xC9000712);
        g.fill(layout.panelX() - 1, layout.panelY() - 1,
                layout.panelX() + layout.panelWidth() + 1,
                layout.panelY() + layout.panelHeight() + 1, palette.hover());
        g.fill(layout.panelX(), layout.panelY(),
                layout.panelX() + layout.panelWidth(),
                layout.panelY() + layout.panelHeight(), palette.panel());
        g.fill(layout.panelX(), layout.panelY(),
                layout.panelX() + layout.panelWidth(), layout.panelY() + 3, palette.accent());

        drawHeader(g, mouseX, mouseY, palette);
        drawNavigation(g, mouseX, mouseY, palette);
        drawPage(g, mouseX, mouseY, palette);
        drawFooter(g, palette);
    }

    private void drawHeader(GuiGraphicsExtractor g, int mouseX, int mouseY, SkyOPTheme.Palette p) {
        int x = layout.panelX();
        int y = layout.panelY();
        g.text(font, Component.literal("SKYOP"), x + 16, y + 10, p.accent(), true);
        if (!layout.micro()) {
            g.text(font, Component.literal(tr("CONTROL CENTER  /  4.1.0", "PUSAT KONTROL  /  4.1.0")), x + 74, y + 10, p.text(), true);
            g.text(font, Component.literal(tr("Minecraft Java 26.3  •  Fabric", "Minecraft Java 26.3  •  Fabric")),
                    x + 16, y + 27, p.muted(), false);
        } else {
            g.text(font, Component.literal("4.1"), x + 74, y + 10, p.muted(), false);
        }

        Minecraft client = Minecraft.getInstance();
        boolean connected = client.player != null && client.level != null;
        String connection = connected ? "IN WORLD" : "MENU / OFFLINE";
        int statusX = x + layout.panelWidth() - (layout.micro() ? 104 : 224);
        int statusW = layout.micro() ? 58 : 112;
        g.fill(statusX, y + 9, statusX + statusW, y + 32, p.elevated());
        g.fill(statusX + 7, y + 17, statusX + 12, y + 22,
                connected ? p.success() : p.muted());
        if (!layout.micro()) {
            g.text(font, Component.literal(connection), statusX + 18, y + 17,
                    connected ? p.success() : p.muted(), true);
        }

        int panicW = layout.micro() ? 38 : 92;
        int panicX = x + layout.panelWidth() - panicW - 8;
        boolean hover = layout.contains(mouseX, mouseY, panicX, y + 9, panicW, 23);
        g.fill(panicX, y + 9, panicX + panicW, y + 32,
                hover ? 0xFF713D48 : 0xFF492832);
        String panicLabel = layout.micro() ? "!" : "DISABLE ALL";
        g.text(font, Component.literal(panicLabel),
                panicX + Math.max(3, (panicW - font.width(panicLabel)) / 2),
                y + 17, 0xFFFFC2C8, true);
    }

    private void drawNavigation(GuiGraphicsExtractor g, int mouseX, int mouseY, SkyOPTheme.Palette p) {
        int x = layout.panelX() + (layout.micro() ? 3 : 8);
        int top = layout.panelY() + (layout.micro() ? 43 : 58);
        int rowHeight = layout.micro() ? 21 : layout.compact() ? 29 : 36;
        int gap = layout.micro() ? 2 : 4;
        Page[] pages = Page.values();

        for (int i = 0; i < pages.length; i++) {
            Page item = pages[i];
            int y = top + i * (rowHeight + gap);
            boolean selected = item == page;
            boolean hovered = layout.contains(mouseX, mouseY, x, y, layout.sidebarWidth() - 8, rowHeight);
            g.fill(x, y, x + layout.sidebarWidth() - 8, y + rowHeight,
                    selected ? p.hover() : hovered ? p.elevated() : p.canvas());
            if (selected) g.fill(x, y, x + 3, y + rowHeight, p.accent());
            String label = navigationLabel(item);
            if (layout.micro()) label = label.substring(0, 1);
            else if (layout.compact()) label = trim(label, 8);
            g.text(font, Component.literal(label), x + (layout.micro() ? 10 : 9),
                    y + Math.max(3, (rowHeight - 8) / 2), selected ? p.text() : p.muted(), selected);
        }
    }

    private String tr(String english, String indonesian) {
        return "id".equals(SkyOPLanguage.code()) ? indonesian : english;
    }

    private String navigationLabel(Page value) {
        boolean id = "id".equals(SkyOPLanguage.code());
        return switch (value) {
            case OVERVIEW -> id ? "Ringkasan" : "Overview";
            case MODULES -> id ? "Modul" : "Modules";
            case PROFILES -> id ? "Profil" : "Profiles";
            case WAYPOINTS -> id ? "Waypoint" : "Waypoints";
            case APPEARANCE -> id ? "Tampilan" : "Appearance";
            case DIAGNOSTICS -> id ? "Diagnostik" : "Diagnostics";
            case CONNECTION -> id ? "Koneksi" : "Connection";
        };
    }

    private void drawPage(GuiGraphicsExtractor g, int mouseX, int mouseY, SkyOPTheme.Palette p) {
        int x = layout.contentX();
        int y = layout.contentY();
        int w = layout.contentWidth();
        String title = navigationLabel(page).toUpperCase(Locale.ROOT);
        g.text(font, Component.literal(title), x, y, p.text(), true);
        if (!layout.micro()) {
            String subtitle = switch (page) {
                case OVERVIEW -> tr("Live client state and shortcuts to implemented tools", "Status klien langsung dan pintasan ke fitur yang tersedia");
                case MODULES -> tr("Open the real module browser and dynamic setting inspector", "Buka daftar modul dan pengaturan dinamis");
                case PROFILES -> tr("Save, load, import and export the current local configuration", "Simpan, muat, impor, dan ekspor konfigurasi lokal");
                case WAYPOINTS -> tr("Manage coordinates stored on this client", "Kelola koordinat yang tersimpan di klien ini");
                case APPEARANCE -> tr("Change the active palette and interface language", "Ganti tema warna dan bahasa antarmuka");
                case DIAGNOSTICS -> tr("Runtime health, module errors and local diagnostic report", "Kesehatan runtime, error modul, dan laporan diagnostik");
                case CONNECTION -> tr("Connection state and observed latency; server policy remains authoritative", "Status koneksi dan latensi; keputusan akses tetap milik server");
            };
            g.text(font, Component.literal(subtitle), x, y + 17, p.muted(), false);
        }

        actions = buildActions();
        switch (page) {
            case OVERVIEW -> drawOverview(g, p, x, y, w);
            case MODULES -> drawModules(g, p, x, y, w);
            case PROFILES -> drawProfiles(g, p, x, y, w);
            case WAYPOINTS -> drawWaypoints(g, p, x, y, w);
            case APPEARANCE -> drawAppearance(g, p, x, y, w);
            case DIAGNOSTICS -> drawDiagnostics(g, p, x, y, w);
            case CONNECTION -> drawConnection(g, p, x, y, w);
        }

        for (Action action : actions) drawAction(g, mouseX, mouseY, p, action);
    }

    private List<Action> buildActions() {
        List<Action> result = new ArrayList<>();
        int x = layout.contentX();
        int y = layout.contentY() + (layout.micro() ? 35 : 55);
        int width = layout.contentWidth();
        int gap = layout.micro() ? 4 : 8;
        int buttonWidth = Math.max(48, (width - gap) / 2);

        switch (page) {
            case OVERVIEW -> {
                int top = layout.contentY() + (layout.micro() ? 118 : layout.compact() ? 150 : 232);
                if (layout.micro()) {
                    add(result, "open_modules", "MODULES", x, top, width, false);
                } else {
                    add(result, "open_modules", tr("OPEN MODULES", "BUKA MODUL"), x, top, buttonWidth, false);
                    add(result, "open_profiles", tr("PROFILES", "PROFIL"), x + buttonWidth + gap, top, buttonWidth, false);
                    add(result, "open_waypoints", tr("WAYPOINTS", "TITIK KOORDINAT"), x, top + 32, buttonWidth, false);
                    add(result, "open_appearance", tr("APPEARANCE", "TAMPILAN"), x + buttonWidth + gap, top + 32, buttonWidth, false);
                    add(result, "open_diagnostics", tr("DIAGNOSTICS", "DIAGNOSTIK"), x, top + 64, buttonWidth, false);
                }
            }
            case MODULES -> add(result, "open_module_gui", tr("OPEN FULL MODULE BROWSER", "BUKA DAFTAR MODUL"),
                    x, y + 64, Math.min(width, 300), false);
            case PROFILES -> {
                int half = Math.max(48, (width - gap) / 2);
                int profileTop = layout.contentY() + 72;
                int rowStep = layout.compact() ? 26 : 30;
                for (int slot = 1; slot <= 3; slot++) {
                    int rowY = profileTop + (slot - 1) * rowStep;
                    add(result, "save_profile_" + slot, tr("SAVE PROFILE ", "SIMPAN PROFIL ") + slot, x, rowY, half, false);
                    add(result, "load_profile_" + slot, tr("LOAD PROFILE ", "MUAT PROFIL ") + slot,
                            x + half + gap, rowY, half, false);
                }
                int hotbarTop = profileTop + 3 * rowStep + (layout.compact() ? 30 : 44);
                for (int slot = 1; slot <= 3; slot++) {
                    int rowY = hotbarTop + (slot - 1) * rowStep;
                    add(result, "save_hotbar_" + slot, tr("SAVE HOTBAR ", "SIMPAN HOTBAR ") + slot, x, rowY, half, false);
                    add(result, "load_hotbar_" + slot, tr("LOAD HOTBAR ", "MUAT HOTBAR ") + slot,
                            x + half + gap, rowY, half, false);
                }
                int fileTop = hotbarTop + 3 * rowStep + (layout.compact() ? 24 : 34);
                add(result, "export_profile", tr("EXPORT JSON", "EKSPOR JSON"), x, fileTop, half, false);
                add(result, "import_profile", tr("IMPORT JSON", "IMPOR JSON"), x + half + gap, fileTop, half, false);
                add(result, "backup_profile", tr("BACKUP NOW", "CADANGKAN"), x, fileTop + 30, width, false);
            }
            case WAYPOINTS -> {
                int rowY = y + 29;
                add(result, "add_waypoint", tr("ADD CURRENT POSITION", "TAMBAH POSISI SAAT INI"), x, rowY, buttonWidth, false);
                add(result, "clear_waypoints", tr("CLEAR ALL", "HAPUS SEMUA"), x + buttonWidth + gap, rowY, buttonWidth, true);
                List<SkyOPWaypointManager.Waypoint> points = SkyOPWaypointManager.all();
                int visible = Math.max(1, (layout.contentHeight() - 175) / 27);
                int first = Math.min(waypointScroll, Math.max(0, points.size() - visible));
                int end = Math.min(points.size(), first + visible);
                int itemsTop = layout.contentY() + 118;
                for (int index = first; index < end; index++) {
                    int itemY = itemsTop + (index - first) * 27;
                    add(result, "copy_waypoint_" + index, tr("COPY COORDS", "SALIN KOORDINAT"),
                            x + Math.max(0, width - (layout.micro() ? 70 : 112)),
                            itemY, Math.min(width, layout.micro() ? 70 : 112), false);
                }
            }
            case APPEARANCE -> {
                add(result, "cycle_theme", tr("NEXT THEME", "TEMA BERIKUTNYA"), x, y + 45, buttonWidth, false);
                add(result, "cycle_language", tr("SWITCH LANGUAGE", "GANTI BAHASA"),
                        x + buttonWidth + gap, y + 45, buttonWidth, false);
                add(result, "open_hud_editor", tr("OPEN HUD EDITOR", "BUKA EDITOR HUD"),
                        x, y + 104, Math.min(width, 300), false);
            }
            case DIAGNOSTICS -> add(result, "open_diagnostics_screen", tr("OPEN DIAGNOSTICS", "BUKA DIAGNOSTIK"),
                    x, y + 95, Math.min(width, 300), false);
            case CONNECTION -> {
                // Intentionally no reconnect/anti-kick control. A server-issued
                // kick or ban is an access decision and must not be bypassed.
            }
        }
        return List.copyOf(result);
    }

    private void add(List<Action> target, String id, String label,
                     int x, int y, int w, boolean danger) {
        int h = Math.min(BUTTON_HEIGHT, Math.max(16, layout.contentHeight() / 8));
        target.add(new Action(id, label, x, y, Math.max(1, w), h, danger));
    }

    private void drawOverview(GuiGraphicsExtractor g, SkyOPTheme.Palette p,
                              int x, int y, int w) {
        Minecraft client = Minecraft.getInstance();
        int top = y + (layout.micro() ? 27 : layout.compact() ? 32 : 43);
        int gap = layout.micro() ? 4 : 8;
        int columns = layout.micro() || layout.compact() ? 2 : 4;
        int cardWidth = Math.max(1, (w - gap * (columns - 1)) / columns);
        int cardHeight = layout.micro() ? 34 : layout.compact() ? 42 : 57;
        String[] labels = "id".equals(SkyOPLanguage.code())
                ? new String[]{"TERDAFTAR", "DIKONFIGURASI", "AKTIF", "FPS"}
                : new String[]{"REGISTERED", "ENABLED", "ACTIVE", "FPS"};
        String[] values = {
                Integer.toString(SkyOPModuleId.values().length),
                Integer.toString(SkyOPCore.enabledModuleCount()),
                Integer.toString(SkyOPModuleManager.activeCount()),
                Integer.toString(client.getFps())
        };
        for (int i = 0; i < values.length; i++) {
            int col = i % columns;
            int row = i / columns;
            drawMetric(g, p, x + col * (cardWidth + gap), top + row * (cardHeight + gap),
                    cardWidth, cardHeight, labels[i], values[i]);
        }
        if (!layout.micro() && !layout.compact()) {
            int ping = SkyOPMath.getCurrentPing(client);
            String pingText = ping < 0 ? "N/A" : ping + " ms";
            int lowerTop = top + cardHeight + 14;
            g.fill(x, lowerTop, x + w, lowerTop + 42, p.canvas());
            g.text(font, Component.literal("CURRENT SESSION"), x + 10, lowerTop + 7, p.muted(), true);
            String player = client.player == null ? "No player loaded" : client.player.getName().getString();
            String world = client.level == null ? "No world loaded" : "World loaded";
            g.text(font, Component.literal(trim(player + "  •  " + world + "  •  Ping " + pingText,
                    Math.max(12, (w - 22) / 6))), x + 10, lowerTop + 23, p.text(), false);
        }
    }

    private void drawMetric(GuiGraphicsExtractor g, SkyOPTheme.Palette p,
                            int x, int y, int w, int h, String label, String value) {
        g.fill(x, y, x + w, y + h, p.elevated());
        g.fill(x, y, x + 3, y + h, p.accent());
        if (!layout.micro()) g.text(font, Component.literal(label), x + 10, y + 8, p.muted(), true);
        g.text(font, Component.literal(value), x + 10,
                y + (layout.micro() ? 12 : 27), p.text(), true);
    }

    private void drawModules(GuiGraphicsExtractor g, SkyOPTheme.Palette p, int x, int y, int w) {
        drawInfoCard(g, p, x, y + 35, w, 52,
                tr("REGISTRY MODUL", "REGISTRY MODUL"),
                SkyOPModuleId.values().length + tr(" registered module IDs are available in the current client build.",
                        " ID modul tersedia pada build klien ini."));
        if (!layout.micro()) {
            g.text(font, Component.literal(tr("The full browser contains live toggle states, module filters, Favorites and registered settings.",
                    "Browser lengkap memiliki status toggle langsung, filter modul, Favorit, dan pengaturan yang terdaftar.")),
                    x, y + 98, p.muted(), false);
        }
    }

    private void drawProfiles(GuiGraphicsExtractor g, SkyOPTheme.Palette p, int x, int y, int w) {
        if (!layout.micro()) {
            int profileTop = layout.contentY() + 72;
            int rowStep = layout.compact() ? 26 : 30;
            int hotbarTop = profileTop + 3 * rowStep + (layout.compact() ? 30 : 44);
            int fileTop = hotbarTop + 3 * rowStep + (layout.compact() ? 24 : 34);

            g.text(font, Component.literal(tr("CONFIGURATION PROFILES  •  3 SLOTS", "PROFIL KONFIGURASI  •  3 SLOT")),
                    x, profileTop - 26, p.muted(), true);
            g.text(font, Component.literal(tr("Save or load the active client configuration.", "Simpan atau muat konfigurasi klien yang sedang aktif.")),
                    x, profileTop - 12, p.muted(), false);

            g.fill(x, hotbarTop - 22, x + w, hotbarTop - 21, p.hover());
            g.text(font, Component.literal(tr("HOTBAR LAYOUTS  •  3 SLOTS", "SUSUNAN HOTBAR  •  3 SLOT")),
                    x, hotbarTop - 18, p.muted(), true);

            g.fill(x, fileTop - 22, x + w, fileTop - 21, p.hover());
            g.text(font, Component.literal(tr("FILES AND BACKUPS", "FILE DAN CADANGAN")),
                    x, fileTop - 18, p.muted(), true);
        }
    }

    private void drawWaypoints(GuiGraphicsExtractor g, SkyOPTheme.Palette p, int x, int y, int w) {
        List<SkyOPWaypointManager.Waypoint> points = SkyOPWaypointManager.all();
        int top = y + 30;
        g.text(font, Component.literal(points.size() + tr(" / 32 saved waypoints", " / 32 waypoint tersimpan")), x, top, p.muted(), true);
        int rowTop = top + 88;
        int visible = Math.max(1, (layout.contentHeight() - 175) / 27);
        int first = Math.min(waypointScroll, Math.max(0, points.size() - visible));
        int end = Math.min(points.size(), first + visible);
        Minecraft client = Minecraft.getInstance();
        for (int index = first; index < end; index++) {
            SkyOPWaypointManager.Waypoint point = points.get(index);
            int rowY = rowTop + (index - first) * 27;
            g.fill(x, rowY, x + w, rowY + 23, p.elevated());
            String value = point.name() + "  [" + point.x() + ", " + point.y() + ", " + point.z() + "]";
            if (client.player != null) {
                value += "  •  " + String.format(Locale.ROOT, "%.1f", SkyOPWaypointManager.distance(client, point)) + " m";
            }
            g.text(font, Component.literal(trim(value, Math.max(8, (w - 130) / 6))),
                    x + 7, rowY + 8, p.text(), false);
        }
        if (points.isEmpty()) {
            g.text(font, Component.literal(tr("No waypoints saved yet.", "Belum ada waypoint tersimpan.")), x, rowTop + 8, p.muted(), false);
        }
        if (!layout.micro()) {
            g.text(font, Component.literal(tr("Click COPY COORDS on a row to copy its position.",
                    "Klik SALIN KOORDINAT pada baris untuk menyalin posisi.")),
                    x, y + layout.contentHeight() - 24, p.muted(), false);
        }
    }

    private void drawAppearance(GuiGraphicsExtractor g, SkyOPTheme.Palette p, int x, int y, int w) {
        int top = y + 38;
        drawInfoCard(g, p, x, top, w, 52, tr("ACTIVE THEME", "TEMA AKTIF"), SkyOPTheme.displayName(SkyOPState.uiTheme));
        if (!layout.micro()) {
            g.text(font, Component.literal(tr("The palette change is saved to the local config immediately.", "Perubahan tema langsung disimpan ke konfigurasi lokal.")),
                    x, top + 61, p.muted(), false);
            g.text(font, Component.literal(tr("LANGUAGE", "BAHASA")), x, top + 92, p.muted(), true);
            g.text(font, Component.literal(SkyOPLanguage.current().displayName()),
                    x, top + 109, p.text(), true);
            g.text(font, Component.literal(tr("HUD position and text editing remain available in the existing ClickGUI.", "Pengaturan posisi HUD dan teks tersedia melalui ClickGUI.")),
                    x, top + 152, p.muted(), false);
        }
    }

    private void drawDiagnostics(GuiGraphicsExtractor g, SkyOPTheme.Palette p, int x, int y, int w) {
        Minecraft client = Minecraft.getInstance();
        int top = y + 36;
        String connection = client.player != null && client.level != null ? "In world" : "Not in a world";
        String ping = SkyOPMath.getCurrentPing(client) < 0 ? "Unavailable"
                : SkyOPMath.getCurrentPing(client) + " ms";
        drawInfoCard(g, p, x, top, w, 50, tr("CLIENT STATE", "STATUS KLIEN"), connection + "  •  Ping " + ping);
        if (!layout.micro()) {
            g.text(font, Component.literal("Open the diagnostics dashboard to inspect module health, recent reasons and error counters."),
                    x, top + 63, p.muted(), false);
            g.text(font, Component.literal("Copy Report and the local skyop-client log are provided by that screen."),
                    x, top + 80, p.muted(), false);
        }
    }

    private void drawConnection(GuiGraphicsExtractor g, SkyOPTheme.Palette p, int x, int y, int w) {
        Minecraft client = Minecraft.getInstance();
        boolean inWorld = client.player != null && client.level != null;
        int ping = SkyOPMath.getCurrentPing(client);
        int top = y + 36;
        String state = inWorld ? "Client is in a loaded world." : "Client is not currently in a world.";
        String latency = ping < 0 ? "Latency unavailable" : "Observed latency: " + ping + " ms";
        drawInfoCard(g, p, x, top, w, 56, tr("CONNECTION SNAPSHOT", "RINGKASAN KONEKSI"), state + "  " + latency);
        if (!layout.micro()) {
            g.text(font, Component.literal(tr("Reconnect is not triggered after a server-issued kick or ban.", "Reconnect tidak dijalankan setelah server mengirim kick atau ban.")),
                    x, top + 70, p.text(), true);
            g.text(font, Component.literal(tr("This page does not bypass server access rules, anti-cheat, or moderation.", "Halaman ini tidak melewati aturan akses server, anti-cheat, atau moderasi.")),
                    x, top + 88, p.muted(), false);
            g.text(font, Component.literal(tr("Unexpected transport-loss recovery can be added as a separate, opt-in feature.", "Pemulihan koneksi yang terputus tanpa sengaja dapat dibuat sebagai fitur terpisah dan opsional.")),
                    x, top + 106, p.muted(), false);
        }
    }

    private void drawInfoCard(GuiGraphicsExtractor g, SkyOPTheme.Palette p,
                              int x, int y, int w, int h, String label, String value) {
        g.fill(x, y, x + w, y + h, p.elevated());
        g.fill(x, y, x + 3, y + h, p.accent());
        g.text(font, Component.literal(label), x + 10, y + 8, p.muted(), true);
        g.text(font, Component.literal(trim(value, Math.max(8, (w - 22) / 6))),
                x + 10, y + 26, p.text(), false);
    }

    private void drawAction(GuiGraphicsExtractor g, int mouseX, int mouseY,
                            SkyOPTheme.Palette p, Action action) {
        boolean hovered = layout.contains(mouseX, mouseY,
                action.x(), action.y(), action.width(), action.height());
        int fill = action.danger()
                ? (hovered ? 0xFF713D48 : 0xFF492832)
                : (hovered ? p.hover() : p.elevated());
        g.fill(action.x(), action.y(), action.x() + action.width(),
                action.y() + action.height(), fill);
        String label = trim(action.label(), Math.max(4, (action.width() - 10) / 6));
        int color = action.danger() ? 0xFFFFC2C8 : hovered ? p.text() : p.accent();
        g.text(font, Component.literal(label),
                action.x() + Math.max(4, (action.width() - font.width(label)) / 2),
                action.y() + Math.max(3, (action.height() - 8) / 2), color, true);
    }

    private void drawFooter(GuiGraphicsExtractor g, SkyOPTheme.Palette p) {
        int y = layout.panelY() + layout.panelHeight() - FOOTER_HEIGHT;
        g.fill(layout.panelX() + 8, y, layout.panelX() + layout.panelWidth() - 8, y + 1, p.hover());
        String footer = status.isBlank()
                ? tr("Right Shift: Control Center  •  Escape: close", "Right Shift: Pusat Kontrol  •  Escape: tutup")
                : status;
        g.text(font, Component.literal(trim(footer, Math.max(6, (layout.panelWidth() - 24) / 6))),
                layout.panelX() + 12, y + 11, status.isBlank() ? p.muted() : p.success(), false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        calculateLayout();
        int mx = (int) event.x();
        int my = (int) event.y();
        if (event.input() != InputConstants.MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        int panicWidth = layout.micro() ? 38 : 92;
        int panicX = layout.panelX() + layout.panelWidth() - panicWidth - 8;
        if (layout.contains(mx, my, panicX, layout.panelY() + 9, panicWidth, 23)) {
            runAction("disable_all");
            return true;
        }

        int navX = layout.panelX() + (layout.micro() ? 3 : 8);
        int navTop = layout.panelY() + (layout.micro() ? 43 : 58);
        int navHeight = layout.micro() ? 21 : layout.compact() ? 29 : 36;
        int navGap = layout.micro() ? 2 : 4;
        for (int i = 0; i < Page.values().length; i++) {
            int navY = navTop + i * (navHeight + navGap);
            if (layout.contains(mx, my, navX, navY, layout.sidebarWidth() - 8, navHeight)) {
                page = Page.values()[i];
                return true;
            }
        }

        for (Action action : buildActions()) {
            if (layout.contains(mx, my, action.x(), action.y(), action.width(), action.height())) {
                runAction(action.id());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void runAction(String id) {
        Minecraft client = Minecraft.getInstance();
        try {
            if ("disable_all".equals(id)) {
                SkyOPCore.disableAll(client);
                setStatus("All modules disabled; temporary client state restored.");
                return;
            }
            switch (id) {
                case "open_modules", "open_module_gui" ->
                        client.gui.setScreen(new SkyOPClickGui());
                case "open_profiles" -> page = Page.PROFILES;
                case "open_waypoints" -> page = Page.WAYPOINTS;
                case "open_appearance" -> page = Page.APPEARANCE;
                case "open_diagnostics" -> page = Page.DIAGNOSTICS;
                case "open_diagnostics_screen" ->
                        client.gui.setScreen(new SkyOPDiagnosticsScreen(this));
                case "open_hud_editor" -> {
                    client.gui.setScreen(new SkyOPClickGui());
                    setStatus(tr("Use HUD EDIT in the ClickGUI header to move HUD panels.", "Gunakan HUD EDIT pada header ClickGUI untuk memindahkan panel HUD."));
                }
                case "cycle_theme" -> {
                    SkyOPCore.cycleUiTheme();
                    setStatus(tr("Theme: ", "Tema: ") + SkyOPTheme.displayName(SkyOPState.uiTheme));
                }
                case "cycle_language" -> {
                    SkyOPCore.cycleLanguage();
                    setStatus(tr("Language: ", "Bahasa: ") + SkyOPLanguage.current().displayName());
                }
                case "add_waypoint" -> {
                    boolean added = SkyOPWaypointManager.addCurrent(client);
                    setStatus(added ? tr("Current position saved as a waypoint.", "Posisi saat ini disimpan sebagai waypoint.")
                            : client.player == null ? tr("Join a world before adding a waypoint.", "Masuk ke dunia sebelum menambahkan waypoint.")
                            : tr("Waypoint limit reached (32).", "Batas waypoint tercapai (32)."));
                }
                case "clear_waypoints" -> {
                    SkyOPWaypointManager.clear();
                    waypointScroll = 0;
                    setStatus(tr("All local waypoints cleared.", "Semua waypoint lokal dihapus."));
                }
                case "export_profile" -> {
                    SkyOPProfileManager.exportCurrent();
                    setStatus(tr("Profile exported: ", "Profil diekspor: ") + SkyOPProfileManager.exportPath());
                }
                case "import_profile" -> {
                    SkyOPProfileManager.importProfile();
                    setStatus(tr("Profile imported from: ", "Profil diimpor dari: ") + SkyOPProfileManager.importPath());
                }
                case "backup_profile" -> {
                    SkyOPProfileManager.backupCurrent();
                    setStatus(tr("Configuration backup created.", "Cadangan konfigurasi berhasil dibuat."));
                }
                default -> {
                    if (id.startsWith("save_profile_")) {
                        int slot = parseSlot(id);
                        SkyOPProfileManager.saveSlot(slot);
                        setStatus(tr("Configuration profile ", "Profil konfigurasi ") + slot + tr(" saved.", " disimpan."));
                    } else if (id.startsWith("load_profile_")) {
                        int slot = parseSlot(id);
                        SkyOPProfileManager.loadSlot(slot);
                        setStatus(tr("Configuration profile ", "Profil konfigurasi ") + slot + tr(" loaded.", " dimuat."));
                    } else if (id.startsWith("save_hotbar_")) {
                        int slot = parseSlot(id);
                        SkyOPProfileManager.saveHotbarPreset(slot);
                        setStatus(tr("Hotbar layout ", "Susunan hotbar ") + slot + tr(" saved.", " disimpan."));
                    } else if (id.startsWith("load_hotbar_")) {
                        int slot = parseSlot(id);
                        SkyOPProfileManager.loadHotbarPreset(slot);
                        setStatus(tr("Hotbar layout ", "Susunan hotbar ") + slot + tr(" loaded.", " dimuat."));
                    } else if (id.startsWith("copy_waypoint_")) {
                        int index = Integer.parseInt(id.substring("copy_waypoint_".length()));
                        List<SkyOPWaypointManager.Waypoint> points = SkyOPWaypointManager.all();
                        if (index < 0 || index >= points.size()) {
                            setStatus(tr("Waypoint no longer exists.", "Waypoint tidak lagi tersedia."));
                        } else {
                            SkyOPWaypointManager.Waypoint point = points.get(index);
                            client.keyboardHandler.setClipboard(point.x() + " " + point.y() + " " + point.z());
                            setStatus(tr("Waypoint coordinates copied.", "Koordinat waypoint disalin."));
                        }
                    }
                }
            }
        } catch (IOException | IllegalArgumentException exception) {
            setStatus(tr("Action failed: ", "Aksi gagal: ") + safeError(exception));
        }
    }

    private int parseSlot(String id) {
        int underscore = id.lastIndexOf('_');
        return Integer.parseInt(id.substring(underscore + 1));
    }

    private void setStatus(String message) {
        status = message == null || message.isBlank() ? "Action completed." : message;
        statusTicks = 180;
    }

    private String safeError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) return exception.getClass().getSimpleName();
        return trim(message, 100);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (page == Page.WAYPOINTS && layout != null
                && layout.contains((int) mouseX, (int) mouseY, layout.contentX(), layout.contentY(),
                layout.contentWidth(), layout.contentHeight())) {
            int count = SkyOPWaypointManager.all().size();
            int visible = Math.max(1, (layout.contentHeight() - 175) / 27);
            int max = Math.max(0, count - visible);
            waypointScroll = Math.max(0, Math.min(max, waypointScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        SkyOPCore.saveConfig();
        if (parent != null) {
            Minecraft.getInstance().gui.setScreen(parent);
        } else {
            super.onClose();
        }
    }

    private String trim(String value, int maxLength) {
        if (value == null || maxLength <= 0) return "";
        if (value.length() <= maxLength) return value;
        if (maxLength <= 3) return value.substring(0, maxLength);
        return value.substring(0, maxLength - 3) + "...";
    }
}
