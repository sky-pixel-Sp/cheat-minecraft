package id.skyop.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Read-only local diagnostics screen with a one-click report copy action. */
public final class SkyOPDiagnosticsScreen extends Screen {
    private static final int PANEL = 0xF010141C;
    private static final int PANEL_ROW = 0xFF1A202B;
    private static final int ACCENT = 0xFF55B8F4;
    private static final int TEXT = 0xFFF3F6FA;
    private static final int MUTED = 0xFF98A6B8;
    private static final int GREEN = 0xFF64D6A4;
    private static final int RED = 0xFFE07B83;

    private final Screen parent;
    private int scrollRows;
    private int copiedTicks;

    public SkyOPDiagnosticsScreen(Screen parent) {
        super(Component.literal("SkyOP Diagnostics"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.fill(0, 0, width, height, 0xD0000712);
        graphics.fill(8, 8, width - 8, height - 8, 0xFF31445A);
        graphics.fill(9, 9, width - 9, height - 9, PANEL);
        graphics.fill(9, 9, width - 9, 12, ACCENT);

        graphics.text(font, Component.literal(SkyOPLanguage.text("diagnostics.title")),
                20, 20, ACCENT, true);
        graphics.text(font, Component.literal(SkyOPLanguage.text("diagnostics.subtitle")),
                20, 36, MUTED, false);
        graphics.text(font, Component.literal("Log: " + SkyOPDiagnostics.logPath()),
                20, 49, MUTED, false);

        Minecraft client = Minecraft.getInstance();
        List<SkyOPModule> modules = SkyOPModuleManager.all();
        int top = 66;
        int bottom = height - 48;
        int rowHeight = 58;
        int visibleRows = Math.max(1, (bottom - top) / rowHeight);
        int maxScroll = Math.max(0, modules.size() - visibleRows);
        scrollRows = Math.max(0, Math.min(scrollRows, maxScroll));

        for (int index = scrollRows; index < Math.min(modules.size(), scrollRows + visibleRows); index++) {
            SkyOPModule module = modules.get(index);
            SkyOPModuleId id = module.type();
            SkyOPDiagnostics.Snapshot status = SkyOPDiagnostics.snapshot(id);
            boolean ready = SkyOPModuleManager.isReadyForDiagnostics(client, id);
            boolean enabled = SkyOPCore.isEnabled(id);
            int rowY = top + (index - scrollRows) * rowHeight;
            graphics.fill(18, rowY, width - 18, rowY + rowHeight - 4, PANEL_ROW);
            graphics.fill(18, rowY, 21, rowY + rowHeight - 4,
                    status.errorCount() > 0 ? RED : (ready ? GREEN : MUTED));

            String state = (ready ? SkyOPLanguage.text("diagnostics.ready")
                    : SkyOPLanguage.text("diagnostics.not_ready"))
                    + "  •  " + (enabled ? SkyOPLanguage.text("gui.on") : SkyOPLanguage.text("gui.off"))
                    + "  •  " + SkyOPLanguage.text("diagnostics.errors") + ": " + status.errorCount() + "/3";
            graphics.text(font, Component.literal(fit(module.title() + "  |  " + state, width - 54)),
                    28, rowY + 5, ready ? TEXT : RED, true);
            String reason = status.lastReason().isBlank()
                    ? (enabled ? "—" : SkyOPLanguage.text("diagnostics.module_disabled"))
                    : status.lastReason();
            graphics.text(font, Component.literal(fit(
                    SkyOPLanguage.text("diagnostics.last_reason") + ": " + reason, width - 54)),
                    28, rowY + 21, MUTED, false);
            String error = status.lastError().isBlank() ? "—" : status.lastError();
            graphics.text(font, Component.literal(fit(
                    SkyOPLanguage.text("diagnostics.last_error") + ": " + error, width - 54)),
                    28, rowY + 36, status.errorCount() > 0 ? RED : MUTED, false);
        }

        int buttonY = height - 36;
        int copyX = 18;
        int copyW = Math.min(168, Math.max(100, width / 2 - 24));
        int backW = 92;
        int backX = width - backW - 18;
        boolean copyHover = inside(mouseX, mouseY, copyX, buttonY, copyW, 22);
        boolean backHover = inside(mouseX, mouseY, backX, buttonY, backW, 22);
        graphics.fill(copyX, buttonY, copyX + copyW, buttonY + 22,
                copyHover ? 0xFF24506A : 0xFF1B3A50);
        graphics.text(font, Component.literal(SkyOPLanguage.text(
                        copiedTicks > 0 ? "diagnostics.copied" : "diagnostics.copy")),
                copyX + 10, buttonY + 7, TEXT, true);
        graphics.fill(backX, buttonY, backX + backW, buttonY + 22,
                backHover ? 0xFF314C65 : PANEL_ROW);
        graphics.text(font, Component.literal(SkyOPLanguage.text("diagnostics.back")),
                backX + 23, buttonY + 7, TEXT, true);
        if (copiedTicks > 0) copiedTicks--;
    }

    private String fit(String value, int maxWidth) {
        String safe = value == null ? "—" : value;
        if (font.width(safe) <= maxWidth) return safe;
        while (safe.length() > 2 && font.width(safe + "…") > maxWidth) {
            safe = safe.substring(0, safe.length() - 1);
        }
        return safe + "…";
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.input() == InputConstants.MOUSE_BUTTON_LEFT) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();
            int buttonY = height - 36;
            int copyX = 18;
            int copyW = Math.min(168, Math.max(100, width / 2 - 24));
            int backX = width - 92 - 18;
            if (inside(mouseX, mouseY, copyX, buttonY, copyW, 22)) {
                Minecraft client = Minecraft.getInstance();
                client.keyboardHandler.setClipboard(SkyOPDiagnostics.buildReport());
                copiedTicks = 50;
                return true;
            }
            if (inside(mouseX, mouseY, backX, buttonY, 92, 22)) {
                Minecraft.getInstance().gui.setScreen(parent);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        int direction = (int) Math.signum(verticalAmount);
        scrollRows = Math.max(0, Math.min(SkyOPModuleManager.all().size(), scrollRows - direction));
        return true;
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
