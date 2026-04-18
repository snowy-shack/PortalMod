package net.portalmod.mixins.fluid;

import net.minecraft.block.*;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.portalmod.core.init.FluidTagInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluidBlock.class)
public abstract class FlowingFluidBlockMixin extends Block implements IBucketPickupHandler {

    @Shadow protected abstract void fizz(IWorld p_180688_1_, BlockPos p_180688_2_);

    @Shadow public abstract FluidState getFluidState(BlockState p_204507_1_);

    @Shadow public abstract FlowingFluid getFluid();

    public FlowingFluidBlockMixin(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Inject(
            method = "shouldSpreadLiquid(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void pmHandleHorizontalGooReactions(World world, BlockPos pos, BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        for(Direction direction : Direction.values()) {
            if (direction != Direction.UP) {
                BlockPos relativePos = pos.relative(direction);
                if (getFluid().is(FluidTagInit.GOO)) {
                    if (portalMod$handleFluidInteraction(FluidTags.WATER, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, world, relativePos) ||
                            portalMod$handleFluidInteraction(FluidTags.LAVA, Blocks.NETHERRACK, Blocks.OBSIDIAN, world, relativePos)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                if (getFluid().is(FluidTags.WATER)) {
                    if (portalMod$handleFluidInteraction(FluidTagInit.GOO, Blocks.SOUL_SAND, Blocks.SOUL_SAND, world, relativePos)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                if (getFluid().is(FluidTags.LAVA)) {
                    if (portalMod$handleFluidInteraction(FluidTagInit.GOO, Blocks.NETHERRACK, Blocks.NETHERRACK, world, relativePos)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
    }

    @Unique
    public boolean portalMod$handleFluidInteraction(ITag<Fluid> fluid1, Block flowingBlock, Block sourceBlock, World world, BlockPos pos) {
        if (world.getFluidState(pos).is(fluid1)) {
            world.setBlockAndUpdate(pos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(world, pos, pos, (world.getFluidState(pos).isSource() ? sourceBlock : flowingBlock).defaultBlockState()));
            this.fizz(world, pos);
            return true;
        }
        return false;
    }
}
