package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Renders the HUD modules independently from the client initializer. */
public final class SkyOPHudRenderer {
    private static long sessionStartedAtMillis = System.currentTimeMillis();

    private SkyOPHudRenderer() { }

    public static void resetSessionTimer() {
        sessionStartedAtMillis = System.currentTimeMillis();
    }



    static void render(Minecraft client, GuiGraphicsExtractor graphics) {
        if (client.player == null || client.gui.hud.isHidden()) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        if (SkyOPCore.isEnabled(SkyOPModuleId.HUD)) {
            int width = 190, lineHeight = 12;
            int activeCount = 0;
            for (SkyOPModuleId module : SkyOPModuleId.values()) {
                if (module != SkyOPModuleId.HUD && SkyOPCore.isEnabled(module)) activeCount++;
            }
            int height = 44 + activeCount * lineHeight;
            int fullHeight = height + 26;
            int x = SkyOPCore.hudXFor(SkyOPModuleId.HUD, SkyOPState.hudX, screenWidth, width);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.HUD, SkyOPState.hudY, screenHeight, fullHeight);
            graphics.fill(x, y, x + width, y + fullHeight, 0xC5101726);
            graphics.fill(x, y, x + width, y + 2, SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal("SKYOP  /  CLIENT"), x + 10, y + 8, SkyOPState.hudAccentColor, true);
            graphics.text(client.font, Component.literal("26.3  •  " + activeCount + " " + SkyOPLanguage.text("gui.active")),
                    x + 10, y + 22, 0xFFB9C8D8, true);

            int row = 0;
            for (SkyOPModuleId module : SkyOPModuleId.values()) {
                if (module != SkyOPModuleId.HUD && SkyOPCore.isEnabled(module)) {
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

        if (SkyOPCore.isEnabled(SkyOPModuleId.TARGET_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.TARGET_HUD, screenWidth - 194, screenWidth, 186);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.TARGET_HUD, 8, screenHeight, 58);
            graphics.fill(x, y, x + 186, y + 58, 0xC5101726);
            graphics.fill(x, y, x + 186, y + 2, SkyOPState.hudAccentColor);
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
                        : Mth.clamp(target.getHealth() / target.getMaxHealth(), 0.0F, 1.0F);
                graphics.fill(x + 10, y + 43, x + 10 + (int) (barWidth * ratio), y + 49, 0xFF57D69A);
            } else {
                graphics.text(client.font, Component.literal(SkyOPLanguage.text("gui.no_target")), x + 10, y + 25, 0xFF9AAABD, true);
            }
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.KEYSTROKES_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.KEYSTROKES_HUD, 10, screenWidth, 86);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.KEYSTROKES_HUD, screenHeight - 62, screenHeight, 42);
            drawKey(graphics, x + 22, y, "W", client.options.keyUp.isDown());
            drawKey(graphics, x, y + 22, "A", client.options.keyLeft.isDown());
            drawKey(graphics, x + 22, y + 22, "S", client.options.keyDown.isDown());
            drawKey(graphics, x + 44, y + 22, "D", client.options.keyRight.isDown());
            drawKey(graphics, x + 66, y + 22, "J", client.options.keyJump.isDown());
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.ARMOR_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.ARMOR_HUD, screenWidth - 150, screenWidth, 142);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.ARMOR_HUD, screenHeight - 38, screenHeight, 28);
            graphics.fill(x, y, x + 142, y + 28, 0xC5101726);
            graphics.text(client.font, Component.literal(
                            String.format("HP %.0f/%.0f  •  ARMOR %d",
                                    client.player.getHealth(), client.player.getMaxHealth(),
                                    client.player.getArmorValue())),
                    x + 8, y + 10, 0xFFF1F6FB, true);
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.POTION_HUD)) {
            int visibleEffects = Math.min(7, client.player.getActiveEffects().size());
            int panelHeight = 8 + visibleEffects * 11;
            int x = SkyOPCore.hudXFor(SkyOPModuleId.POTION_HUD, screenWidth - 198, screenWidth, 190);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.POTION_HUD, 76, screenHeight, panelHeight);
            graphics.fill(x, y, x + 190, y + panelHeight, 0xC5101726);
            graphics.text(client.font, Component.literal("EFFECTS"), x + 8, y + 4, SkyOPState.hudAccentColor, true);
            int row = 0;
            for (var effect : client.player.getActiveEffects()) {
                if (row >= 7) break;
                String name = effect.getEffect().value().getDisplayName().getString();
                graphics.text(client.font, Component.literal(trimText(name, 18) + "  " + effect.getDuration() / 20 + "s"),
                        x + 8, y + 17 + row * 11, 0xFFF1F6FB, true);
                row++;
            }
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.DURABILITY_HUD)) {
            var stack = client.player.getMainHandItem();
            int x = SkyOPCore.hudXFor(SkyOPModuleId.DURABILITY_HUD, screenWidth / 2 - 82, screenWidth, 164);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.DURABILITY_HUD, screenHeight - 94, screenHeight, 24);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
                int percent = (int) (100.0 * remaining / Math.max(1, stack.getMaxDamage()));
                graphics.fill(x, y, x + 164, y + 24, 0xC5101726);
                graphics.text(client.font, Component.literal("DURABILITY  " + percent + "%"),
                        x + 8, y + 8, SkyOPState.hudAccentColor, true);
                graphics.fill(x + 8, y + 18, x + 156, y + 21, 0xFF36465A);
                graphics.fill(x + 8, y + 18, x + 8 + (int) (148 * percent / 100.0), y + 21, SkyOPState.hudAccentColor);
            }
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.COMPASS_HUD)) {
            int widgetWidth = 104;
            int x = SkyOPCore.hudXFor(SkyOPModuleId.COMPASS_HUD, screenWidth / 2 - 52, screenWidth, widgetWidth);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.COMPASS_HUD, 8, screenHeight, 23);
            String direction = SkyOPMath.compassDirection(client.player.getYRot());
            graphics.fill(x, y, x + widgetWidth, y + 23, 0xC5101726);
            graphics.fill(x, y, x + widgetWidth, y + 2, SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal(direction + "  " + Math.round(client.player.getYRot()) + "°"),
                    x + 8, y + 8, SkyOPState.hudAccentColor, true);
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.DIRECTION_HUD)) {
            int widgetWidth = 164;
            int x = SkyOPCore.hudXFor(SkyOPModuleId.DIRECTION_HUD, screenWidth / 2 - 82, screenWidth, widgetWidth);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.DIRECTION_HUD, 36, screenHeight, 24);
            String direction = SkyOPMath.compassDirection(client.player.getYRot());
            String coordinates = "X " + client.player.blockPosition().getX()
                    + "  Z " + client.player.blockPosition().getZ();
            graphics.fill(x, y, x + widgetWidth, y + 24, 0xC5101726);
            graphics.fill(x, y, x + widgetWidth, y + 2, SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal(direction + "  •  " + coordinates),
                    x + 8, y + 9, 0xFFF1F6FB, true);
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.PING_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.PING_HUD, screenWidth - 100, screenWidth, 92);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.PING_HUD, 8, screenHeight, 22);
            int ping = SkyOPMath.getCurrentPing(client);
            graphics.fill(x, y, x + 92, y + 22, 0xC5101726);
            graphics.text(client.font, Component.literal("PING  " + (ping < 0 ? "--" : ping + " ms")),
                    x + 8, y + 7, SkyOPState.hudAccentColor, true);
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.FPS_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.FPS_HUD, screenWidth - 100, screenWidth, 92);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.FPS_HUD, 34, screenHeight, 22);
            graphics.fill(x, y, x + 92, y + 22, 0xC5101726);
            graphics.text(client.font, Component.literal("FPS  " + client.getFps()),
                    x + 8, y + 7, SkyOPState.hudAccentColor, true);
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.RADAR_HUD)) {
            List<LivingEntity> nearby = client.level.getEntitiesOfClass(LivingEntity.class,
                    client.player.getBoundingBox().inflate(32.0),
                    entity -> entity != client.player && entity.isAlive());
            nearby.sort(java.util.Comparator.comparingDouble(client.player::distanceToSqr));
            int count = Math.min(6, nearby.size());
            int x = SkyOPCore.hudXFor(SkyOPModuleId.RADAR_HUD, screenWidth - 202, screenWidth, 194);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.RADAR_HUD, screenHeight / 2 - 12, screenHeight, 90);
            graphics.fill(x, y, x + 194, y + 24 + count * 11, 0xC5101726);
            graphics.text(client.font, Component.literal("NEARBY / LOADED ENTITIES"),
                    x + 8, y + 5, SkyOPState.hudAccentColor, true);
            for (int i = 0; i < count; i++) {
                LivingEntity entity = nearby.get(i);
                graphics.text(client.font, Component.literal(
                                trimText(entity.getName().getString(), 17) + "  " +
                                        String.format("%.1fm", client.player.distanceTo(entity))),
                        x + 8, y + 18 + i * 11, 0xFFF1F6FB, true);
            }
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.WAYPOINTS)) {
            List<SkyOPWaypointManager.Waypoint> points = SkyOPWaypointManager.nearest(client, 5);
            int x = SkyOPCore.hudXFor(SkyOPModuleId.WAYPOINTS, 8, screenWidth, 190);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.WAYPOINTS, screenHeight / 2 + 40, screenHeight, 22 + points.size() * 12);
            graphics.fill(x, y, x + 190, y + 22 + points.size() * 12, 0xC5101726);
            graphics.fill(x, y, x + 190, y + 2, SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal("WAYPOINTS"), x + 8, y + 6, SkyOPState.hudAccentColor, true);
            for (int i = 0; i < points.size(); i++) {
                SkyOPWaypointManager.Waypoint point = points.get(i);
                graphics.text(client.font, Component.literal(point.name() + "  "
                                + point.x() + " " + point.y() + " " + point.z() + "  "
                                + String.format("%.0fm", SkyOPWaypointManager.distance(client, point))),
                        x + 8, y + 18 + i * 12, 0xFFF1F6FB, true);
            }
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.MINIMAP)) {
            int size = 104;
            int x = SkyOPCore.hudXFor(SkyOPModuleId.MINIMAP, 8, screenWidth, size);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.MINIMAP, screenHeight - size - 8, screenHeight, size);
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            graphics.fill(x, y, x + size, y + size, 0xC5101726);
            graphics.fill(x, y, x + size, y + 2, SkyOPState.hudAccentColor);
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

        if (SkyOPCore.isEnabled(SkyOPModuleId.TEXT_GUI)) {
            int textWidth = Math.min(screenWidth,
                    Math.max(120, Math.min(480, client.font.width(SkyOPState.customHudText) + 16)));
            int x = SkyOPCore.hudXFor(SkyOPModuleId.TEXT_GUI, 8, screenWidth, textWidth);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.TEXT_GUI, screenHeight - 27, screenHeight, 19);
            graphics.fill(x, y, x + textWidth, y + 19, 0xA0101726);
            graphics.text(client.font, Component.literal(SkyOPState.customHudText), x + 8, y + 6,
                    SkyOPState.hudAccentColor, true);
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.SPEEDOMETER)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.SPEEDOMETER, screenWidth / 2 - 58, screenWidth, 116);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.SPEEDOMETER, screenHeight - 37, screenHeight, 25);
            graphics.fill(x, y, x + 116, y + 25, 0xC5101726);
            graphics.fill(x, y, x + 116, y + 2, SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal(String.format("SPEED  %.2f b/s", SkyOPState.currentSpeedBps)),
                    x + 10, y + 10, 0xFFF1F6FB, true);
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.CPS_HUD)) {
            int x = SkyOPCore.hudXFor(SkyOPModuleId.CPS_HUD, screenWidth / 2 - 48, screenWidth, 96);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.CPS_HUD, screenHeight - 66, screenHeight, 24);
            graphics.fill(x, y, x + 96, y + 24, 0xC5101726);
            graphics.fill(x, y, x + 96, y + 2, SkyOPState.hudAccentColor);
            graphics.text(client.font, Component.literal("CPS  " + SkyOPState.currentCps),
                    x + 10, y + 9, 0xFFF1F6FB, true);
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.WORLD_INFO_HUD)) {
            var position = client.player.blockPosition();
            String dimension = client.level.dimension().toString();
            String biome = client.level.getBiome(position).unwrapKey()
                    .map(key -> key.toString()).orElse("unknown");
            String weather = client.level.isThundering() ? "Storm"
                    : client.level.isRaining() ? "Rain" : "Clear";
            int x = SkyOPCore.hudXFor(SkyOPModuleId.WORLD_INFO_HUD, 8, screenWidth, 220);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.WORLD_INFO_HUD, 112, screenHeight, 40);
            drawInfoPanel(client, graphics, x, y, 220, "WORLD INTEL",
                    List.of(trimText(dimension, 30), trimText(biome, 30), "Weather: " + weather));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.CHUNK_INFO_HUD)) {
            var position = client.player.blockPosition();
            int chunkX = Math.floorDiv(position.getX(), 16);
            int chunkZ = Math.floorDiv(position.getZ(), 16);
            int localX = Math.floorMod(position.getX(), 16);
            int localZ = Math.floorMod(position.getZ(), 16);
            int x = SkyOPCore.hudXFor(SkyOPModuleId.CHUNK_INFO_HUD, 8, screenWidth, 184);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.CHUNK_INFO_HUD, 166, screenHeight, 40);
            drawInfoPanel(client, graphics, x, y, 184, "CHUNK DATA",
                    List.of("Chunk " + chunkX + " / " + chunkZ,
                            "Local " + localX + " / " + position.getY() + " / " + localZ));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.PERFORMANCE_HUD)) {
            Runtime runtime = Runtime.getRuntime();
            long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            long maxMb = runtime.maxMemory() / (1024L * 1024L);
            int percent = maxMb <= 0 ? 0 : (int) Math.min(100L, usedMb * 100L / maxMb);
            int x = SkyOPCore.hudXFor(SkyOPModuleId.PERFORMANCE_HUD, screenWidth - 158, screenWidth, 150);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.PERFORMANCE_HUD, 62, screenHeight, 42);
            drawInfoPanel(client, graphics, x, y, 150, "PERFORMANCE",
                    List.of(client.getFps() + " FPS", usedMb + " / " + maxMb + " MB  •  " + percent + "%"));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.SESSION_TIMER_HUD)) {
            long elapsed = Math.max(0L, (System.currentTimeMillis() - sessionStartedAtMillis) / 1000L);
            long hours = elapsed / 3600L;
            long minutes = (elapsed % 3600L) / 60L;
            long seconds = elapsed % 60L;
            int x = SkyOPCore.hudXFor(SkyOPModuleId.SESSION_TIMER_HUD, screenWidth / 2 - 75, screenWidth, 150);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.SESSION_TIMER_HUD, screenHeight - 114, screenHeight, 29);
            drawInfoPanel(client, graphics, x, y, 150, "WORLD SESSION",
                    List.of(String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.FOOD_HUD)) {
            var food = client.player.getFoodData();
            int x = SkyOPCore.hudXFor(SkyOPModuleId.FOOD_HUD, 8, screenWidth, 140);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.FOOD_HUD, screenHeight - 82, screenHeight, 29);
            drawInfoPanel(client, graphics, x, y, 140, "FOOD",
                    List.of("Hunger " + food.getFoodLevel() + "/20  •  Sat "
                            + String.format(java.util.Locale.ROOT, "%.1f", food.getSaturationLevel())));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.AIR_HUD)) {
            int air = Math.max(0, client.player.getAirSupply());
            int maxAir = Math.max(1, client.player.getMaxAirSupply());
            int x = SkyOPCore.hudXFor(SkyOPModuleId.AIR_HUD, 8, screenWidth, 140);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.AIR_HUD, screenHeight - 48, screenHeight, 29);
            drawInfoPanel(client, graphics, x, y, 140, "AIR SUPPLY",
                    List.of(air + " / " + maxAir + " ticks"));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.EXPERIENCE_HUD)) {
            int percent = Math.max(0, Math.min(100, Math.round(client.player.experienceProgress * 100.0F)));
            int x = SkyOPCore.hudXFor(SkyOPModuleId.EXPERIENCE_HUD, screenWidth / 2 - 75, screenWidth, 150);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.EXPERIENCE_HUD, screenHeight - 150, screenHeight, 29);
            drawInfoPanel(client, graphics, x, y, 150, "EXPERIENCE",
                    List.of("Level " + client.player.experienceLevel + "  •  " + percent + "%"));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.ITEM_INFO_HUD)) {
            var stack = client.player.getMainHandItem();
            String name = stack.isEmpty() ? "Empty" : trimText(stack.getHoverName().getString(), 25);
            String durability = stack.isDamageableItem()
                    ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) + " / " + stack.getMaxDamage()
                    : "Not damageable";
            int x = SkyOPCore.hudXFor(SkyOPModuleId.ITEM_INFO_HUD, screenWidth / 2 - 110, screenWidth, 204);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.ITEM_INFO_HUD, screenHeight - 112, screenHeight, 40);
            drawInfoPanel(client, graphics, x, y, 204, "MAIN HAND",
                    List.of(name + (stack.isEmpty() ? "" : "  x" + stack.getCount()), "Durability: " + durability));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.OFFHAND_HUD)) {
            var stack = client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            String name = stack.isEmpty() ? "Empty" : trimText(stack.getHoverName().getString(), 22);
            int x = SkyOPCore.hudXFor(SkyOPModuleId.OFFHAND_HUD, screenWidth - 190, screenWidth, 182);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.OFFHAND_HUD, 190, screenHeight, 29);
            drawInfoPanel(client, graphics, x, y, 182, "OFFHAND",
                    List.of(name + (stack.isEmpty() ? "" : "  x" + stack.getCount())));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.BLOCK_INFO_HUD)) {
            String blockName = "No block targeted";
            String coordinates = "Aim at a block to inspect";
            if (client.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                var position = blockHit.getBlockPos();
                blockName = trimText(client.level.getBlockState(position).getBlock().getName().getString(), 28);
                coordinates = position.getX() + " / " + position.getY() + " / " + position.getZ();
            }
            int x = SkyOPCore.hudXFor(SkyOPModuleId.BLOCK_INFO_HUD, screenWidth - 232, screenWidth, 224);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.BLOCK_INFO_HUD, 112, screenHeight, 40);
            drawInfoPanel(client, graphics, x, y, 224, "CROSSHAIR BLOCK",
                    List.of(blockName, coordinates));
        }

        if (SkyOPCore.isEnabled(SkyOPModuleId.ARMOR_DETAIL_HUD)) {
            var head = client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
            var chest = client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            var legs = client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
            var feet = client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
            int x = SkyOPCore.hudXFor(SkyOPModuleId.ARMOR_DETAIL_HUD, screenWidth - 218, screenWidth, 210);
            int y = SkyOPCore.hudYFor(SkyOPModuleId.ARMOR_DETAIL_HUD, screenHeight - 144, screenHeight, 62);
            drawInfoPanel(client, graphics, x, y, 210, "ARMOR DETAIL",
                    List.of("HEAD  " + armorStatus(head), "CHEST  " + armorStatus(chest),
                            "LEGS  " + armorStatus(legs), "FEET  " + armorStatus(feet)));
        }
    }

    private static String armorStatus(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return "Empty";
        if (!stack.isDamageableItem()) return trimText(stack.getHoverName().getString(), 16);
        int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
        int percent = (int) Math.round(remaining * 100.0 / Math.max(1, stack.getMaxDamage()));
        return percent + "%";
    }

    private static void drawInfoPanel(Minecraft client, GuiGraphicsExtractor graphics,
                                      int x, int y, int width, String heading, List<String> lines) {
        int height = 18 + lines.size() * 11;
        graphics.fill(x, y, x + width, y + height, 0xD5101726);
        graphics.fill(x, y, x + width, y + 2, SkyOPState.hudAccentColor);
        graphics.text(client.font, Component.literal(heading), x + 7, y + 5,
                SkyOPState.hudAccentColor, true);
        for (int index = 0; index < lines.size(); index++) {
            graphics.text(client.font, Component.literal(trimText(lines.get(index), Math.max(8, (width - 14) / 6))),
                    x + 7, y + 17 + index * 11, 0xFFF1F6FB, false);
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

}
