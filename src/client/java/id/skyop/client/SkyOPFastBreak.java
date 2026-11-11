package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Haste-equivalent mining multiplier for the local player.
 *
 * Java Edition's Haste formula multiplies destroy speed by 1 + (level * 0.2).
 * This helper applies that same scale locally; it does not send an effect or
 * permission to a remote server.
 */
public final class SkyOPFastBreak {
    private SkyOPFastBreak() { }

    public static float multiplierForLevel(double hasteLevel) {
        SkyOPSettingsSchema.NumberRule rule = SkyOPSettingsSchema.rule("fastBreakLevel");
        double safeLevel = rule == null ? 7.0 : rule.validate(hasteLevel);
        return 1.0F + (float) (safeLevel * 0.2D);
    }

    public static float applyToLocalPlayer(Player player, float vanillaSpeed) {
        if (player == null || !SkyOPCore.isEnabled(SkyOPModuleId.FAST_BREAK)) {
            return vanillaSpeed;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player != player) {
            return vanillaSpeed;
        }
        return vanillaSpeed * multiplierForLevel(SkyOPState.fastBreakLevel);
    }
}
