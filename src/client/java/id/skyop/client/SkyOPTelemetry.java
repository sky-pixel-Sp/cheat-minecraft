package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Low-cost client telemetry shared by combat counters and HUD modules. */
public final class SkyOPTelemetry {
    private SkyOPTelemetry() { }

    public static void recordAttack() {
        SkyOPState.attacksThisWindow++;
    }

    public static void update(Minecraft client) {
        Player player = client.player;
        if (player == null) {
            SkyOPState.previousPosition = null;
            SkyOPState.currentSpeedBps = 0.0;
            return;
        }

        Vec3 position = player.position();
        if (SkyOPState.previousPosition != null) {
            double dx = position.x - SkyOPState.previousPosition.x;
            double dz = position.z - SkyOPState.previousPosition.z;
            double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
            SkyOPState.currentSpeedBps = speed <= 100.0 ? speed : 0.0;
        }
        SkyOPState.previousPosition = position;

        SkyOPState.cpsWindowTicks++;
        if (SkyOPState.cpsWindowTicks >= 20) {
            SkyOPState.currentCps = SkyOPState.attacksThisWindow;
            SkyOPState.attacksThisWindow = 0;
            SkyOPState.cpsWindowTicks = 0;
        }
    }

    public static void reset() {
        SkyOPState.previousPosition = null;
        SkyOPState.currentSpeedBps = 0.0;
        SkyOPState.attacksThisWindow = 0;
        SkyOPState.currentCps = 0;
        SkyOPState.cpsWindowTicks = 0;
    }
}
