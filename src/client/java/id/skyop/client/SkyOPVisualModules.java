package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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

    static void tickShared(Minecraft client) {
        if (SkyOPCore.isEnabled(SkyOPModuleId.ENTITY_ESP) || SkyOPCore.isEnabled(SkyOPModuleId.ITEM_ESP)) tickEsp(client);
        else restoreGlow();

        if (SkyOPCore.isEnabled(SkyOPModuleId.GAMMA_BOOST) || SkyOPCore.isEnabled(SkyOPModuleId.NIGHT_VISION)) tickGamma(client);
        else restoreGamma(client);
    }

    static void cleanup(Minecraft client) {
        restoreGlow();
        restoreGamma(client);
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
}
