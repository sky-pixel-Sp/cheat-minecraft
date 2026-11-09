package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Renders the HUD modules independently from the client initializer. */
public final class SkyOPHudRenderer {
    private SkyOPHudRenderer() { }



    static void render(Minecraft client, GuiGraphicsExtractor graphics) {
        if (client.player == null || client.gui.hud.isHidden()) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.HUD)) {
            int width = 184, lineHeight = 12;
            int activeCount = 0;
            for (SkyOPModuleId module : SkyOPModuleId.values()) {
                if (module != SkyOPModuleId.HUD && SkyOPCore.SkyOPCore.isEnabled(module)) activeCount++;
            }
            int height = 44 + activeCount * lineHeight;
            int fullHeight = height + 26;
            int x = SkyOPCore.hudXFor(SkyOPModuleId.HUD, SkyOPState.hudX, screenWidth, 190);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.HUD, SkyOPState.hudY, screenHeight, fullHeight);
            graphics.fill(x, y, x + width, y + height, 0xC5101726);
            graphics.fill(x, y, x + width, y + 2, SkyOPState.SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal("SKYOP  /  CLIENT"), x + 10, y + 8, SkyOPState.SkyOPState.hudAccentColor, true);
            graphics.text(client.font, Component.literal("26.3  •  " + activeCount + " " + SkyOPLanguage.text("gui.active")),
                    x + 10, y + 22, 0xFFB9C8D8, true);

            int row = 0;
            for (SkyOPModuleId module : SkyOPModuleId.values()) {
                if (module != SkyOPModuleId.HUD && SkyOPCore.SkyOPCore.isEnabled(module)) {
                    graphics.text(client.font, Component.literal("• " + module.title()),
                            x + 10, y + 40 + row * lineHeight, 0xFFE7F0F8, true);
                    row++;
                }
            }

            String coords = String.format("XYZ %.1f / %.1f / %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ());
            graphics.text(client.font, Component.literal(coords), x + 10, y + height + 7, 0xFFFFFFFF, true);
            graphics.text(client.font, Component.literal(client.getFps() + " FPS"),
                    x + 10, y + height + 19, 0xFF76D5FF, true);
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.TARGET_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.TARGET_HUD, screenWidth - 194, screenWidth, 186);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.TARGET_HUD, 8, screenHeight, 58);
            graphics.fill(x, y, x + 186, y + 58, 0xC5101726);
            graphics.fill(x, y, x + 186, y + 2, SkyOPState.SkyOPState.hudAccentColor);
            if (client.hitResult instanceof EntityHitResult hit
                    && hit.getEntity() instanceof LivingEntity target && target.isAlive()) {
                String targetName = target.getName().getString();
                graphics.text(client.font, Component.literal(trimText(targetName, 22)),
                        x + 10, y + 9, 0xFFF1F6FB, true);
                graphics.text(client.font, Component.literal(
                                String.format("HP %.1f / %.1f  •  %.1fm",
                                        target.getHealth(), target.getMaxHealth(), client.player.distanceTo(target))),
                        x + 10, y + 25, 0xFFB9C8D8, true);
                int barWidth = 164;
                graphics.fill(x + 10, y + 43, x + 10 + barWidth, y + 49, 0xFF36465A);
                float ratio = target.getMaxHealth() <= 0 ? 0
                        : Mth.SkyOPMath.clamp(target.getHealth() / target.getMaxHealth(), 0.0F, 1.0F);
                graphics.fill(x + 10, y + 43, x + 10 + (int) (barWidth * ratio), y + 49, 0xFF57D69A);
            } else {
                graphics.text(client.font, Component.literal(SkyOPLanguage.text("gui.no_target")), x + 10, y + 25, 0xFF9AAABD, true);
            }
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.KEYSTROKES_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.KEYSTROKES_HUD, 10, screenWidth, 86);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.KEYSTROKES_HUD, screenHeight - 62, screenHeight, 42);
            drawKey(graphics, x + 22, y, "W", client.options.keyUp.isDown());
            drawKey(graphics, x, y + 22, "A", client.options.keyLeft.isDown());
            drawKey(graphics, x + 22, y + 22, "S", client.options.keyDown.isDown());
            drawKey(graphics, x + 44, y + 22, "D", client.options.keyRight.isDown());
            drawKey(graphics, x + 66, y + 22, "J", client.options.keyJump.isDown());
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.ARMOR_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.ARMOR_HUD, screenWidth - 150, screenWidth, 142);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.ARMOR_HUD, screenHeight - 38, screenHeight, 28);
            graphics.fill(x, y, x + 142, y + 28, 0xC5101726);
            graphics.text(client.font, Component.literal(
                            String.format("HP %.0f/%.0f  •  ARMOR %d",
                                    client.player.getHealth(), client.player.getMaxHealth(),
                                    client.player.getArmorValue())),
                    x + 8, y + 10, 0xFFF1F6FB, true);
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.POTION_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.POTION_HUD, screenWidth - 198, screenWidth, 190);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.POTION_HUD, 76, screenHeight, 96);
            graphics.fill(x, y, x + 190, y + 8 + client.player.getActiveEffects().size() * 11, 0xC5101726);
            graphics.text(client.font, Component.literal("EFFECTS"), x + 8, y + 4, SkyOPState.SkyOPState.hudAccentColor, true);
            int row = 0;
            for (var effect : client.player.getActiveEffects()) {
                if (row >= 7) break;
                String name = effect.getEffect().value().getDisplayName().getString();
                graphics.text(client.font, Component.literal(trimText(name, 18) + "  " + effect.getDuration() / 20 + "s"),
                        x + 8, y + 17 + row * 11, 0xFFF1F6FB, true);
                row++;
            }
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.DURABILITY_HUD)) {
            var stack = client.player.getMainHandItem();
            int x = SkyOPCore.hudXFor(SkyOPModuleId.DURABILITY_HUD, screenWidth / 2 - 82, screenWidth, 164);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.DURABILITY_HUD, screenHeight - 94, screenHeight, 24);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
                int percent = (int) (100.0 * remaining / Math.max(1, stack.getMaxDamage()));
                graphics.fill(x, y, x + 164, y + 24, 0xC5101726);
                graphics.text(client.font, Component.literal("DURABILITY  " + percent + "%"),
                        x + 8, y + 8, SkyOPState.SkyOPState.hudAccentColor, true);
                graphics.fill(x + 8, y + 18, x + 156, y + 21, 0xFF36465A);
                graphics.fill(x + 8, y + 18, x + 8 + (int) (148 * percent / 100.0), y + 21, SkyOPState.SkyOPState.hudAccentColor);
            }
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.COMPASS_HUD) || SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.DIRECTION_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.COMPASS_HUD, screenWidth / 2 - 48, screenWidth, 96);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.COMPASS_HUD, 8, screenHeight, 23);
            String direction = SkyOPMath.compassDirection(client.player.getYRot());
            graphics.fill(x, y, x + 96, y + 23, 0xC5101726);
            graphics.text(client.font, Component.literal(direction + "  " + Math.round(client.player.getYRot()) + "°"),
                    x + 8, y + 8, SkyOPState.SkyOPState.hudAccentColor, true);
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.PING_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.PING_HUD, screenWidth - 100, screenWidth, 92);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.PING_HUD, 8, screenHeight, 22);
            int ping = SkyOPMath.getCurrentPing(client);
            graphics.fill(x, y, x + 92, y + 22, 0xC5101726);
            graphics.text(client.font, Component.literal("PING  " + (ping < 0 ? "--" : ping + " ms")),
                    x + 8, y + 7, SkyOPState.SkyOPState.hudAccentColor, true);
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.FPS_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.FPS_HUD, screenWidth - 100, screenWidth, 92);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.FPS_HUD, 34, screenHeight, 22);
            graphics.fill(x, y, x + 92, y + 22, 0xC5101726);
            graphics.text(client.font, Component.literal("FPS  " + client.getFps()),
                    x + 8, y + 7, SkyOPState.SkyOPState.hudAccentColor, true);
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.RADAR_HUD)) {
            List<LivingEntity> nearby = client.level.getEntitiesOfClass(LivingEntity.class,
                    client.player.getBoundingBox().inflate(32.0),
                    entity -> entity != client.player && entity.isAlive());
            nearby.sort(java.util.Comparator.comparingDouble(client.player::distanceToSqr));
            int count = Math.min(6, nearby.size());
            int x = SkyOPCore.hudXFor(SkyOPModuleId.RADAR_HUD, screenWidth - 202, screenWidth, 194);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.RADAR_HUD, screenHeight / 2 - 12, screenHeight, 90);
            graphics.fill(x, y, x + 194, y + 24 + count * 11, 0xC5101726);
            graphics.text(client.font, Component.literal("NEARBY / LOADED ENTITIES"),
                    x + 8, y + 5, SkyOPState.SkyOPState.hudAccentColor, true);
            for (int i = 0; i < count; i++) {
                LivingEntity entity = nearby.get(i);
                graphics.text(client.font, Component.literal(
                                trimText(entity.getName().getString(), 17) + "  " +
                                        String.format("%.1fm", client.player.distanceTo(entity))),
                        x + 8, y + 18 + i * 11, 0xFFF1F6FB, true);
            }
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.WAYPOINTS)) {
            List<SkyOPWaypointManager.Waypoint> points = SkyOPWaypointManager.nearest(client, 5);
            int x = SkyOPCore.hudXFor(SkyOPModuleId.WAYPOINTS, 8, screenWidth, 190);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.WAYPOINTS, screenHeight / 2 + 40, screenHeight, 22 + points.size() * 12);
            graphics.fill(x, y, x + 190, y + 22 + points.size() * 12, 0xC5101726);
            graphics.fill(x, y, x + 190, y + 2, SkyOPState.SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal("WAYPOINTS"), x + 8, y + 6, SkyOPState.SkyOPState.hudAccentColor, true);
            for (int i = 0; i < points.size(); i++) {
                SkyOPWaypointManager.Waypoint point = points.get(i);
                graphics.text(client.font, Component.literal(point.name() + "  "
                                + point.x() + " " + point.y() + " " + point.z() + "  "
                                + String.format("%.0fm", SkyOPWaypointManager.distance(client, point))),
                        x + 8, y + 18 + i * 12, 0xFFF1F6FB, true);
            }
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.MINIMAP)) {
            int size = 104;
            int x = SkyOPCore.hudXFor(SkyOPModuleId.MINIMAP, 8, screenWidth, size);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.MINIMAP, screenHeight - size - 8, screenHeight, size);
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            graphics.fill(x, y, x + size, y + size, 0xC5101726);
            graphics.fill(x, y, x + size, y + 2, SkyOPState.SkyOPState.hudAccentColor);
            graphics.fill(centerX - 1, y + 8, centerX + 1, y + size - 8, 0x664B6078);
            graphics.fill(x + 8, centerY - 1, x + size - 8, centerY + 1, 0x664B6078);
            graphics.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, 0xFFFFFFFF);
            double yaw = Math.toRadians(client.player.getYRot());
            for (Entity entity : client.level.getEntitiesOfClass(Entity.class,
                    client.player.getBoundingBox().inflate(48.0), candidate -> candidate != client.player)) {
                double dx = entity.getX() - client.player.getX();
                double dz = entity.getZ() - client.player.getZ();
                double localX = dx * Math.cos(yaw) + dz * Math.sin(yaw);
                double localZ = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
                int dotX = centerX + (int) (localX / 48.0 * 42.0);
                int dotY = centerY + (int) (localZ / 48.0 * 42.0);
                int color = entity instanceof Player ? 0xFF61C7F2
                        : entity instanceof Monster ? 0xFFFF7885
                        : entity instanceof ItemEntity ? 0xFFFFB45B : 0xFF9AAABD;
                graphics.fill(dotX - 2, dotY - 2, dotX + 2, dotY + 2, color);
            }
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.TEXT_GUI)) {
            int textWidth = Math.min(screenWidth,
                    Math.max(120, Math.min(480, client.font.width(SkyOPState.SkyOPState.customHudText) + 16)));
            int x = SkyOPCore.hudXFor(SkyOPModuleId.TEXT_GUI, 8, screenWidth, textWidth);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.TEXT_GUI, screenHeight - 27, screenHeight, 19);
            graphics.fill(x, y, x + textWidth, y + 19, 0xA0101726);
            graphics.text(client.font, Component.literal(SkyOPState.SkyOPState.customHudText), x + 8, y + 6,
                    SkyOPState.SkyOPState.hudAccentColor, true);
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.SPEEDOMETER)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.SPEEDOMETER, screenWidth / 2 - 58, screenWidth, 116);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.SPEEDOMETER, screenHeight - 37, screenHeight, 25);
            graphics.fill(x, y, x + 116, y + 25, 0xC5101726);
            graphics.fill(x, y, x + 116, y + 2, SkyOPState.SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal(String.format("SPEED  %.2f b/s", SkyOPState.SkyOPState.currentSpeedBps)),
                    x + 10, y + 10, 0xFFF1F6FB, true);
        }

        if (SkyOPCore.SkyOPCore.isEnabled(SkyOPModuleId.CPS_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.CPS_HUD, screenWidth / 2 - 48, screenWidth, 96);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.CPS_HUD, screenHeight - 66, screenHeight, 24);
            graphics.fill(x, y, x + 96, y + 24, 0xC5101726);
            graphics.fill(x, y, x + 96, y + 2, SkyOPState.SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal("CPS  " + SkyOPState.SkyOPState.currentCps),
                    x + 10, y + 9, 0xFFF1F6FB, true);
        }
    }

private static void drawKey(GuiGraphicsExtractor graphics, int x, int y, String label, boolean pressed) {
        int background = pressed ? 0xFF24516A : 0xC5202B3D;
        int foreground = pressed ? 0xFFFFFFFF : 0xFFB9C8D8;
        graphics.fill(x, y, x + 20, y + 19, background);
        graphics.text(Minecraft.getInstance().font, Component.literal(label), x + 7, y + 5, foreground, true);
    }

private static String trimText(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

private static int SkyOPMath.getCurrentPing(Minecraft client) {
        try {
            if (client.getConnection() == null || client.player == null) return -1;
            var info = client.getConnection().getPlayerInfo(client.player.getUUID());
            return info == null ? -1 : info.getLatency();
        } catch (RuntimeException ignored) {
            return -1;
        }
    }

private static String SkyOPMath.compassDirection(float yaw) {
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int index = Math.floorMod(Math.round(yaw / 45.0F), 8);
        return directions[index];
    }
}
