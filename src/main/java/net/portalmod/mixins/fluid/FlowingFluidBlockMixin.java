package net.portalmod.mixins.fluid;

import net.portalmod.core.init.FluidTagInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

@Mixin(LiquidBlock.class)
public abstract class FlowingFluidBlockMixin extends Block implements BucketPickup {

    @Shadow protected abstract void fizz(LevelAccessor p_180688_1_, BlockPos p_180688_2_);

    @Shadow public abstract FluidState getFluidState(BlockState p_204507_1_);

    @Shadow public abstract FlowingFluid getFluid();

    public FlowingFluidBlockMixin(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Inject(
            method = "shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void pmHandleHorizontalGooReactions(Level world, BlockPos pos, BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        for(Direction direction : Direction.values()) {
            if (direction != Direction.UP) {
                BlockPos relativePos = pos.relative(direction);
                if (this.getFluid().is(FluidTagInit.GOO)) {
                    if (portalMod$handleFluidInteraction(FluidTags.WATER, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, world, relativePos) ||
                            portalMod$handleFluidInteraction(FluidTags.LAVA, Blocks.NETHERRACK, Blocks.OBSIDIAN, world, relativePos)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                if (this.getFluid().is(FluidTags.WATER)) {
                    if (portalMod$handleFluidInteraction(FluidTagInit.GOO, Blocks.SOUL_SAND, Blocks.SOUL_SAND, world, relativePos)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                if (this.getFluid().is(FluidTags.LAVA)) {
                    if (portalMod$handleFluidInteraction(FluidTagInit.GOO, Blocks.NETHERRACK, Blocks.NETHERRACK, world, relativePos)) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
    }

    @Unique
    public boolean portalMod$handleFluidInteraction(TagKey<Fluid> fluid1, Block flowingBlock, Block sourceBlock, Level world, BlockPos pos) {
        // Check both the type of fluid and whether it's not just a waterlogged block
        if (world.getFluidState(pos).is(fluid1) && world.getBlockState(pos).getBlock() instanceof LiquidBlock) {
            world.setBlockAndUpdate(pos, (world.getFluidState(pos).isSource() ? sourceBlock : flowingBlock).defaultBlockState());
            this.fizz(world, pos);
            return true;
        }
        return false;
    }
}
