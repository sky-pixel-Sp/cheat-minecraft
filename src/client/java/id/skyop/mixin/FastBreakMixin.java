package id.skyop.mixin;

import id.skyop.client.SkyOPFastBreak;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the configured Haste-equivalent multiplier to local block breaking. */
@Mixin(Player.class)
public abstract class FastBreakMixin {
    @Inject(
            method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F",
            at = @At("RETURN"),
            cancellable = true
    )
    private void skyop$applyFastBreak(BlockState state, CallbackInfoReturnable<Float> callback) {
        Player player = (Player) (Object) this;
        callback.setReturnValue(SkyOPFastBreak.applyToLocalPlayer(player, callback.getReturnValue()));
    }
}
