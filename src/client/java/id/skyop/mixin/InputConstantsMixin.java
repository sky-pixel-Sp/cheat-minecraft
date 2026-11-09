package id.skyop.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import id.skyop.client.SkyOPSdlKeyboardIndex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Protects Minecraft 26.3's SDL keyboard-state lookup from invalid indexes.
 *
 * Minecraft 26.3 exposes InputConstants.isKeyDown(int), which reads the SDL
 * keyboard-state ByteBuffer directly. Reject invalid indexes before get(int)
 * can throw; vanilla UNKNOWN is represented by SDL scancode 0.
 */
@Mixin(InputConstants.class)
public abstract class InputConstantsMixin {
    @Inject(method = "isKeyDown(I)Z", at = @At("HEAD"), cancellable = true)
    private static void skyop$guardKeyboardStateIndex(int key,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (!SkyOPSdlKeyboardIndex.isValidStateIndex(key)) {
            cir.setReturnValue(false);
        }
    }
}
