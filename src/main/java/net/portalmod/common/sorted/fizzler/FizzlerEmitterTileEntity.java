package net.portalmod.common.sorted.fizzler;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FizzlerEmitterTileEntity extends TileEntity implements ITickableTileEntity, IndicatorActivated {
    public FizzlerEmitterTileEntity() {
        super(TileEntityTypeInit.FIZZLER_EMITTER.get());
    }

    @Override
    public void tick() {
        handleAntlineActivation();
    }

    private void handleAntlineActivation() {
        World world = this.level;
        BlockState blockState = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        boolean activated = blockState.getValue(FizzlerEmitterBlock.ACTIVE);
        Direction facing = blockState.getValue(FizzlerEmitterBlock.FACING);
        int distance = this.distanceToOtherSide(facing);

        // Redstone powered
        if (blockState.getValue(FizzlerEmitterBlock.POWERED)) {
            if (activated) {
                this.setActive(false, distance, facing);
            }
            return;
        }

        // Indicator powered
        IndicatorInfo indicatorInfo = this.checkIndicators(blockState, world, pos);
        if (indicatorInfo.hasIndicators) {
            if (indicatorInfo.allIndicatorsActivated != activated) {
                this.setActive(indicatorInfo.allIndicatorsActivated, distance, facing);
            }
            return;
        }

        // No indicators
        if (!activated) {
            this.setActive(true, distance, facing);
        }
    }

    // Activate both emitters and change field
    public void setActive(boolean active, int distance, Direction facing) {
        if (distance > 0) {
            setField(active, distance, facing);

            BlockPos oppositeEmitterPos = this.getBlockPos().relative(facing, distance);
            BlockState oppositeEmitterState = this.level.getBlockState(oppositeEmitterPos);

            SoundEvent soundEvent = active ? SoundInit.FIZZLER_ACTIVATE.get() : SoundInit.FIZZLER_DEACTIVATE.get();
            float pitch = (ModUtil.randomSoundPitch() + 2) / 3; // 3x lower random pitch

            this.level.playSound(null, this.getBlockPos(), soundEvent, SoundCategory.BLOCKS, 1, pitch);
            this.level.playSound(null, oppositeEmitterPos, soundEvent, SoundCategory.BLOCKS, 1, pitch);

            ((FizzlerEmitterBlock) oppositeEmitterState.getBlock()).setBlockStateValue(FizzlerEmitterBlock.ACTIVE, active, oppositeEmitterState, this.level, oppositeEmitterPos);

            ((FizzlerEmitterBlock) this.getBlockState().getBlock()).setBlockStateValue(FizzlerEmitterBlock.ACTIVE, active, this.getBlockState(), this.level, this.getBlockPos());
        }
    }

    public void setField(boolean active, int distance, Direction facing) {
        for (int i = 1; i < distance; i++) {
            if (active) {
                BlockState fizzlerField = BlockInit.FIZZLER_FIELD.get().defaultBlockState().setValue(FizzlerFieldBlock.AXIS, facing.getAxis());
                this.level.setBlock(this.getBlockPos().relative(facing, i), fizzlerField, 2);
                this.level.setBlock(this.getBlockPos().relative(facing, i).above(), fizzlerField.setValue(FizzlerFieldBlock.HALF, DoubleBlockHalf.UPPER), 2);
            } else {
                this.level.setBlock(this.getBlockPos().relative(facing, i), Blocks.AIR.defaultBlockState(), 2);
                this.level.setBlock(this.getBlockPos().relative(facing, i).above(), Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    public int distanceToOtherSide(Direction direction) {
        for (int i = 1; i <= 16; i++) {
            for (int j = 0; j <= 1; j++) {
                BlockState state = this.level.getBlockState(this.getBlockPos().relative(direction, i).above(j));
                if (state.getBlock().is(Blocks.AIR) || state.getBlock() instanceof FizzlerFieldBlock) {
                    continue;
                }
                if (j == 0 && state.getBlock() instanceof FizzlerEmitterBlock && state.getValue(FizzlerEmitterBlock.HALF) == DoubleBlockHalf.LOWER && state.getValue(FizzlerEmitterBlock.FACING) == direction.getOpposite()) {
                    return i;
                }
                return 0;
            }
        }
        return 0;
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, World world, BlockPos pos) {
        Direction facing = blockState.getValue(FizzlerEmitterBlock.FACING);

        return new ArrayList<>(Arrays.asList(
                pos.relative(facing.getClockWise()),
                pos.relative(facing.getClockWise()).relative(facing.getOpposite()),
                pos.relative(facing.getCounterClockWise()),
                pos.relative(facing.getCounterClockWise()).relative(facing.getOpposite()),
                pos.above().relative(facing.getClockWise()),
                pos.above().relative(facing.getClockWise()).relative(facing.getOpposite()),
                pos.above().relative(facing.getCounterClockWise()),
                pos.above().relative(facing.getCounterClockWise()).relative(facing.getOpposite())
        ));
    }
}
