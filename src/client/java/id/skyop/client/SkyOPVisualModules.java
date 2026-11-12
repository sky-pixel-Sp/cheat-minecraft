package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Visual gameplay modules and shared temporary-state restoration. */
public final class SkyOPVisualModules {
    private SkyOPVisualModules() { }
    private static Double originalGamma;
    private static int containerScanCooldown;
    private static List<ContainerMark> nearbyContainers = List.of();

    private record ContainerMark(BlockPos position, String name, double distance) { }



    static void tickGamma(Minecraft client) {
        if (originalGamma == null) originalGamma = client.options.gamma().get();
        client.options.gamma().set(1.0);
    }



    static void restoreGamma(Minecraft client) {
        if (originalGamma != null) {
            client.options.gamma().set(originalGamma);
            originalGamma = null;
        }
    }



    static void tickEsp(Minecraft client) {
        var player = client.player;
        AABB area = player.getBoundingBox().inflate(SkyOPMath.clamp(SkyOPState.espRange, 8.0, 96.0));
        List<Entity> entities = client.level.getEntitiesOfClass(
                Entity.class, area,
                entity -> entity != player && entity.isAlive() && shouldEsp(entity)
        );

        Set<UUID> seen = new HashSet<>();
        for (Entity entity : entities) {
            UUID id = entity.getUUID();
            seen.add(id);
            SkyOPState.ORIGINAL_GLOW.putIfAbsent(id, new SkyOPState.GlowState(entity, entity.isCurrentlyGlowing()));
            entity.setGlowingTag(true);
        }

        Iterator<Map.Entry<UUID, SkyOPState.GlowState>> iterator = SkyOPState.ORIGINAL_GLOW.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SkyOPState.GlowState> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
                SkyOPState.GlowState state = entry.getValue();
                state.entity().setGlowingTag(state.wasGlowing());
                iterator.remove();
            }
        }
    }

private static boolean shouldEsp(Entity entity) {
        if (entity instanceof Player) return SkyOPState.espPlayers;
        if (entity instanceof ItemEntity) return SkyOPState.espItems || SkyOPCore.isEnabled(SkyOPModuleId.ITEM_ESP);
        return entity instanceof LivingEntity && SkyOPState.espMobs;
    }



    static void restoreGlow() {
        for (SkyOPState.GlowState state : SkyOPState.ORIGINAL_GLOW.values()) {
            state.entity().setGlowingTag(state.wasGlowing());
        }
        SkyOPState.ORIGINAL_GLOW.clear();
    }

    static void tickContainerFinder(Minecraft client) {
        if (containerScanCooldown > 0) {
            containerScanCooldown--;
            return;
        }
        if (client == null || client.player == null || client.level == null) {
            nearbyContainers = List.of();
            return;
        }

        int range = (int) SkyOPMath.clamp(SkyOPState.containerFinderRange, 4, 24);
        BlockPos origin = client.player.blockPosition();
        double maxDistanceSquared = (double) range * range;
        List<ContainerMark> found = new ArrayList<>();

        // Scan only block states in chunks the client already has loaded.
        // This never requests hidden chunks or additional data from the server.
        for (int x = origin.getX() - range; x <= origin.getX() + range; x++) {
            for (int z = origin.getZ() - range; z <= origin.getZ() + range; z++) {
                for (int y = origin.getY() - 4; y <= origin.getY() + 4; y++) {
                    BlockPos position = new BlockPos(x, y, z);
                    if (!client.level.hasChunkAt(position)) continue;

                    double dx = x + 0.5 - client.player.getX();
                    double dy = y + 0.5 - client.player.getY();
                    double dz = z + 0.5 - client.player.getZ();
                    double distanceSquared = dx * dx + dy * dy + dz * dz;
                    if (distanceSquared > maxDistanceSquared) continue;

                    var block = client.level.getBlockState(position).getBlock();
                    String name = block == Blocks.CHEST ? "Chest"
                            : block == Blocks.TRAPPED_CHEST ? "Trapped Chest"
                            : block == Blocks.BARREL ? "Barrel"
                            : block == Blocks.ENDER_CHEST ? "Ender Chest" : null;
                    if (name != null) {
                        found.add(new ContainerMark(position.immutable(), name,
                                Math.sqrt(distanceSquared)));
                    }
                }
            }
        }

        found.sort(Comparator.comparingDouble(ContainerMark::distance));
        nearbyContainers = List.copyOf(found.subList(0, Math.min(8, found.size())));
        containerScanCooldown = 40;
    }

    static void renderContainerFinder(Minecraft client, GuiGraphicsExtractor graphics) {
        if (client == null || client.player == null || client.gui.hud.isHidden()) return;
        SkyOPTheme.Palette palette = SkyOPTheme.palette(SkyOPState.uiTheme);
        int rows = Math.min(6, nearbyContainers.size());
        int panelWidth = 190;
        int panelHeight = 22 + rows * 12;
        int x = Math.max(4, client.getWindow().getGuiScaledWidth() - panelWidth - 10);
        int y = 52;

        graphics.fill(x, y, x + panelWidth, y + panelHeight, palette.panel());
        graphics.fill(x, y, x + panelWidth, y + 2, palette.accent());
        graphics.text(client.font, Component.literal("CONTAINER RADAR  /  " + nearbyContainers.size()),
                x + 8, y + 7, palette.accent(), true);

        if (rows == 0) {
            graphics.text(client.font, Component.literal("No nearby containers in loaded chunks"),
                    x + 8, y + 17, palette.muted(), false);
            return;
        }

        for (int index = 0; index < rows; index++) {
            ContainerMark mark = nearbyContainers.get(index);
            BlockPos pos = mark.position();
            String label = String.format(java.util.Locale.ROOT, "%s  %.0fm  %d %d %d",
                    mark.name(), mark.distance(), pos.getX(), pos.getY(), pos.getZ());
            graphics.text(client.font, Component.literal(label), x + 8,
                    y + 18 + index * 12, palette.text(), false);
        }
    }

    static void clearContainerFinder() {
        containerScanCooldown = 0;
        nearbyContainers = List.of();
    }

    static void tickShared(Minecraft client) {
        if (SkyOPCore.isEnabled(SkyOPModuleId.ENTITY_ESP) || SkyOPCore.isEnabled(SkyOPModuleId.ITEM_ESP)) tickEsp(client);
        else restoreGlow();

        if (SkyOPCore.isEnabled(SkyOPModuleId.GAMMA_BOOST) || SkyOPCore.isEnabled(SkyOPModuleId.NIGHT_VISION)) tickGamma(client);
        else restoreGamma(client);
    }

    static void cleanup(Minecraft client) {
        restoreGlow();
        restoreGamma(client);
        clearContainerFinder();
    }

    static void disable(Minecraft client, SkyOPModuleId module) {
        if ((module == SkyOPModuleId.ENTITY_ESP || module == SkyOPModuleId.ITEM_ESP)
                && !SkyOPCore.isEnabled(SkyOPModuleId.ENTITY_ESP)
                && !SkyOPCore.isEnabled(SkyOPModuleId.ITEM_ESP)) restoreGlow();
        if ((module == SkyOPModuleId.GAMMA_BOOST || module == SkyOPModuleId.NIGHT_VISION)
                && !SkyOPCore.isEnabled(SkyOPModuleId.GAMMA_BOOST)
                && !SkyOPCore.isEnabled(SkyOPModuleId.NIGHT_VISION)) restoreGamma(client);
    }

    public static final class EntityEsp extends SkyOPModule {
        public EntityEsp(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.ENTITY_ESP, settings); }
        @Override public void onDisable(Minecraft client) { disable(client, type()); }
    }
    public static final class ItemEsp extends SkyOPModule {
        public ItemEsp(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.ITEM_ESP, settings); }
        @Override public void onDisable(Minecraft client) { disable(client, type()); }
    }
    public static final class GammaBoost extends SkyOPModule {
        public GammaBoost(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.GAMMA_BOOST, settings); }
        @Override public void onDisable(Minecraft client) { disable(client, type()); }
    }
    public static final class NightVision extends SkyOPModule {
        public NightVision(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.NIGHT_VISION, settings); }
        @Override public void onDisable(Minecraft client) { disable(client, type()); }
    }

    public static final class Zoom extends SkyOPModule {
        private int originalFov = -1;

        public Zoom(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.ZOOM, settings); }

        @Override
        public void onEnable(Minecraft client) {
            originalFov = client.options.fov().get();
        }

        @Override
        public void onTick(Minecraft client) {
            client.options.fov().set((int) SkyOPMath.clamp(SkyOPState.zoomFov, 10, 70));
        }

        private void restore(Minecraft client) {
            if (client != null && originalFov >= 0) {
                client.options.fov().set(originalFov);
            }
            originalFov = -1;
        }

        @Override public void onDisable(Minecraft client) { restore(client); }
        @Override public void onWorldChange(Minecraft client) { restore(client); }
    }

    public static final class ContainerFinder extends SkyOPModule {
        public ContainerFinder(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.CONTAINER_FINDER, settings); }
        @Override public void onTick(Minecraft client) { tickContainerFinder(client); }
        @Override public void onRender2D(Minecraft client, GuiGraphicsExtractor graphics) {
            renderContainerFinder(client, graphics);
        }
        @Override public void onDisable(Minecraft client) { clearContainerFinder(); }
        @Override public void onWorldChange(Minecraft client) { clearContainerFinder(); }
    }

    public static final class Waypoints extends SkyOPModule {
        public Waypoints(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.WAYPOINTS, settings); }
    }

    public static final class MiniMap extends SkyOPModule {
        public MiniMap(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.MINIMAP, settings); }
    }

    public static final class Hud extends SkyOPModule {
        public Hud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.HUD, settings); }
    }

    public static final class TargetHud extends SkyOPModule {
        public TargetHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.TARGET_HUD, settings); }
    }

    public static final class KeystrokesHud extends SkyOPModule {
        public KeystrokesHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.KEYSTROKES_HUD, settings); }
    }

    public static final class ArmorHud extends SkyOPModule {
        public ArmorHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.ARMOR_HUD, settings); }
    }

    public static final class Speedometer extends SkyOPModule {
        public Speedometer(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.SPEEDOMETER, settings); }
    }

    public static final class CpsHud extends SkyOPModule {
        public CpsHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.CPS_HUD, settings); }
    }

    public static final class PotionHud extends SkyOPModule {
        public PotionHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.POTION_HUD, settings); }
    }

    public static final class DurabilityHud extends SkyOPModule {
        public DurabilityHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.DURABILITY_HUD, settings); }
    }

    public static final class CompassHud extends SkyOPModule {
        public CompassHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.COMPASS_HUD, settings); }
    }

    public static final class DirectionHud extends SkyOPModule {
        public DirectionHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.DIRECTION_HUD, settings); }
    }

    public static final class PingHud extends SkyOPModule {
        public PingHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.PING_HUD, settings); }
    }

    public static final class FpsHud extends SkyOPModule {
        public FpsHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.FPS_HUD, settings); }
    }

    public static final class RadarHud extends SkyOPModule {
        public RadarHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.RADAR_HUD, settings); }
    }

    public static final class TextHud extends SkyOPModule {
        public TextHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.TEXT_GUI, settings); }
    }

    public static final class WorldInfoHud extends SkyOPModule {
        public WorldInfoHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.WORLD_INFO_HUD, settings); }
    }

    public static final class ChunkInfoHud extends SkyOPModule {
        public ChunkInfoHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.CHUNK_INFO_HUD, settings); }
    }

    public static final class PerformanceHud extends SkyOPModule {
        public PerformanceHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.PERFORMANCE_HUD, settings); }
    }

    public static final class SessionTimerHud extends SkyOPModule {
        public SessionTimerHud(List<SkyOPSetting<?>> settings) { super(SkyOPModuleId.SESSION_TIMER_HUD, settings); }
    }
}
