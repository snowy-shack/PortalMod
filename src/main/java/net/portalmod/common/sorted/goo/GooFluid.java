package net.portalmod.common.sorted.goo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.FluidInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.SoundInit;

import java.util.Optional;

/**
 * The goo fluid. On Forge this was a ForgeFlowingFluid built from properties;
 * on Fabric it is a plain vanilla-style FlowingFluid with the same settings
 * (tick rate 20, level decrease 2, can form sources).
 */
public abstract class GooFluid extends FlowingFluid {
    @Override
    public Fluid getFlowing() {
        return FluidInit.GOO_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return FluidInit.GOO_FLUID.get();
    }

    @Override
    public Item getBucket() {
        return ItemInit.GOO_BUCKET.get();
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return true;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 2;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 20;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluid);
    }

    @Override
    protected float getExplosionResistance() {
        return 1.0F;
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return BlockInit.GOO.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundInit.BUCKET_FILL_GOO.get());
    }

    public static class Flowing extends GooFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends GooFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
