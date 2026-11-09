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
            ORIGINAL_GLOW.putIfAbsent(id, new GlowState(entity, entity.isCurrentlyGlowing()));
            entity.setGlowingTag(true);
        }

        Iterator<Map.Entry<UUID, GlowState>> iterator = ORIGINAL_GLOW.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, GlowState> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
                GlowState state = entry.getValue();
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
        for (GlowState state : ORIGINAL_GLOW.values()) {
            state.entity().setGlowingTag(state.wasGlowing());
        }
        ORIGINAL_GLOW.clear();
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
}
