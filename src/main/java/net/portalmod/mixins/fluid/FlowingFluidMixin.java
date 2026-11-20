package net.portalmod.mixins.fluid;

import net.portalmod.core.init.FluidTagInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {

    @Shadow public abstract Fluid getSource();
    @Shadow public abstract FluidState getFlowing(int p_207207_1_, boolean p_207207_2_);
    @Shadow protected abstract int getDropOff(LevelReader p_204528_1_);

    @Inject(
            method = "getNewLiquid(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;getDropOff(Lnet/minecraft/world/level/LevelReader;)I"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void pmSpread5Blocks(LevelReader p_205576_1_, BlockPos p_205576_2_, BlockState p_205576_3_, CallbackInfoReturnable<FluidState> cir, int i) {
        int k = i - this.getDropOff(p_205576_1_);
        if (this.getSource().is(FluidTagInit.GOO)) {
            if (i == 4 || i == 2) {
                k = i - 2;
            } else {
                k = i - 1;
            }
        }
        cir.setReturnValue(k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false));
    }
}
