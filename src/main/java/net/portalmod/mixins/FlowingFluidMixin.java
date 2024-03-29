package net.portalmod.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.portalmod.core.init.FluidTagInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {

    @Shadow public abstract Fluid getSource();
    @Shadow public abstract FluidState getFlowing(int p_207207_1_, boolean p_207207_2_);
    @Shadow protected abstract int getDropOff(IWorldReader p_204528_1_);

    @Inject(method = "getNewLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FlowingFluid;getDropOff(Lnet/minecraft/world/IWorldReader;)I"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, remap = false)
    public void pmMake5(IWorldReader p_205576_1_, BlockPos p_205576_2_, BlockState p_205576_3_, CallbackInfoReturnable<FluidState> cir, int i) {
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
