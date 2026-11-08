package id.skyop.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import id.skyop.client.SkyOPSdlKeyboardIndex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Protects Minecraft's SDL keyboard-state lookup from invalid key mappings.
 *
 * Minecraft 26.3's InputConstants.UNKNOWN uses -1, but SDL keyboard state is
 * indexed by SDL scancodes. An invalid index must be treated as "not pressed".
 */
@Mixin(InputConstants.class)
public abstract class InputConstantsMixin {
    @Inject(method = "isKeyDown(JI)Z", at = @At("HEAD"), cancellable = true)
    private static void skyop$guardKeyboardStateIndex(long window, int key,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (!SkyOPSdlKeyboardIndex.isValidStateIndex(key)) {
            cir.setReturnValue(false);
        }
    }
}
