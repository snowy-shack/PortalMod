package net.portalmod.mixins;

import net.portalmod.common.sorted.platform.PlatformBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(PistonMovingBlockEntity.class)
public class PistonTileEntityMixin {
    @Shadow private BlockState movedState;

    @Inject(method = "isStickyForEntities", at = @At("HEAD"), cancellable = true)
    private void makePlatformSticky(CallbackInfoReturnable<Boolean> cir) {
        if (movedState.getBlock() instanceof PlatformBlock) {
            cir.setReturnValue(true);
        }
    }
}
