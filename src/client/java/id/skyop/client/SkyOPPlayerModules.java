package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Player-focused utility modules. No server protocol or anti-cheat manipulation is performed. */
public final class SkyOPPlayerModules {
    private SkyOPPlayerModules() { }

    private static int respawnCooldown;
    private static boolean lowHealthNotified;
    private static boolean armorNotified;

    /**
     * Runs before the normal screen guard so the ordinary respawn request can
     * be sent while Minecraft is showing its death screen.
     */
    public static void tickAutoRespawn(Minecraft client) {
        if (!SkyOPCore.isEnabled(SkyOPModuleId.AUTO_RESPAWN)) {
            respawnCooldown = 0;
            return;
        }
        if (client == null || client.player == null || client.level == null
                || !client.player.isDeadOrDying()) {
            respawnCooldown = 0;
            return;
        }
        if (respawnCooldown > 0) {
            respawnCooldown--;
            return;
        }
        client.player.respawn();
        respawnCooldown = 20;
    }

    static void tickLowHealthAlert(Minecraft client) {
        if (client == null || client.player == null || !client.player.isAlive()) {
            lowHealthNotified = false;
            return;
        }

        float threshold = (float) SkyOPMath.clamp(
                SkyOPState.lowHealthAlertThreshold, 1, 20);
        float health = client.player.getHealth();
        if (health <= threshold) {
            if (!lowHealthNotified) {
                client.player.sendSystemMessage(Component.literal(String.format(
                        java.util.Locale.ROOT,
                        "[SkyOP] Low health: %.1f HP (threshold %.0f).",
                        health, threshold)));
                lowHealthNotified = true;
            }
        } else {
            lowHealthNotified = false;
        }
    }

    static void tickArmorAlert(Minecraft client) {
        if (client == null || client.player == null || !client.player.isAlive()) {
            armorNotified = false;
            return;
        }

        int threshold = (int) SkyOPMath.clamp(SkyOPState.armorAlertPercent, 1, 50);
        boolean lowDurability = false;
        for (ItemStack stack : client.player.getInventory().armor) {
            if (stack.isEmpty() || !stack.isDamageableItem()) continue;
            int maximum = Math.max(1, stack.getMaxDamage());
            int remaining = Math.max(0, maximum - stack.getDamageValue());
            int percentage = remaining * 100 / maximum;
            if (percentage <= threshold) {
                lowDurability = true;
                break;
            }
        }

        if (lowDurability && !armorNotified) {
            client.player.sendSystemMessage(Component.literal(
                    "[SkyOP] Armor durability is at or below " + threshold + "%."));
        }
        armorNotified = lowDurability;
    }

    static void cleanup() {
        respawnCooldown = 0;
        lowHealthNotified = false;
        armorNotified = false;
    }

    public static final class FastBreak extends SkyOPModule {
        public FastBreak(List<SkyOPSetting<?>> settings) {
            super(SkyOPModuleId.FAST_BREAK, settings);
        }
    }

    public static final class AutoRespawn extends SkyOPModule {
        public AutoRespawn(List<SkyOPSetting<?>> settings) {
            super(SkyOPModuleId.AUTO_RESPAWN, settings);
        }
        @Override public void onDisable(Minecraft client) { respawnCooldown = 0; }
    }

    public static final class LowHealthAlert extends SkyOPModule {
        public LowHealthAlert(List<SkyOPSetting<?>> settings) {
            super(SkyOPModuleId.LOW_HEALTH_ALERT, settings);
        }
        @Override public void onTick(Minecraft client) { tickLowHealthAlert(client); }
        @Override public void onDisable(Minecraft client) { lowHealthNotified = false; }
    }

    public static final class ArmorAlert extends SkyOPModule {
        public ArmorAlert(List<SkyOPSetting<?>> settings) {
            super(SkyOPModuleId.ARMOR_ALERT, settings);
        }
        @Override public void onTick(Minecraft client) { tickArmorAlert(client); }
        @Override public void onDisable(Minecraft client) { armorNotified = false; }
    }
}
