package id.skyop.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accesses the client-visible bite flag on a fishing hook.
 *
 * Keep mixin classes in this dedicated package. Fabric Mixin reserves the
 * configured package for mixin classes and rejects normal mod entrypoints
 * loaded from the same package.
 */
@Mixin(FishingHook.class)
public interface SkyOPFishingHookAccessor {
    @Accessor("biting")
    boolean skyop$isBiting();
}
