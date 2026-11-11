package id.skyop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Haste-equivalent mining multiplier for the local player and its matching
 * integrated-server ServerPlayer in singleplayer.
 *
 * Java Edition's Haste formula multiplies destroy speed by 1 + (level * 0.2).
 * This does not modify remote multiplayer players or grant any server permission.
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
        if (client.player == null) return vanillaSpeed;

        if (client.player == player) {
            return vanillaSpeed * multiplierForLevel(SkyOPState.fastBreakLevel);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return vanillaSpeed;
        var integratedServer = client.getSingleplayerServer();
        if (integratedServer == null
                || !SkyOPAutomationRules.matchesSingleplayerPlayerUuid(
                        client.player.getUUID(), serverPlayer.getUUID())) {
            return vanillaSpeed;
        }

        // Resolve by UUID from the active integrated server and require the exact
        // ServerPlayer instance. Remote server players are never modified here.
        ServerPlayer integratedPlayer =
                integratedServer.getPlayerList().getPlayer(client.player.getUUID());
        if (integratedPlayer != serverPlayer) return vanillaSpeed;

        return vanillaSpeed * multiplierForLevel(SkyOPState.fastBreakLevel);
    }
}
