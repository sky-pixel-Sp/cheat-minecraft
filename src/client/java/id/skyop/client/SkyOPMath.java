package id.skyop.client;

import net.minecraft.client.Minecraft;

/** Pure and client-safe helpers shared by modules. */
public final class SkyOPMath {
    private SkyOPMath() { }
    public static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    public static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }

    public static int getCurrentPing(Minecraft client) {
        try {
            if (client == null || client.getConnection() == null || client.player == null) return -1;
            var info = client.getConnection().getPlayerInfo(client.player.getUUID());
            return info == null ? -1 : info.getLatency();
        } catch (RuntimeException ignored) {
            return -1;
        }
    }

    public static String compassDirection(float yaw) {
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int index = Math.floorMod(Math.round(yaw / 45.0F), 8);
        return directions[index];
    }
}
