package net.portalmod.common.sorted.fizzler;

import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

public class FizzlerEmitterTileEntity extends BlockEntity implements TickableBlockEntity, IndicatorActivated {

    public static final int MAX_DISTANCE = 16;

    public FizzlerEmitterTileEntity() {
        super(TileEntityTypeInit.FIZZLER_EMITTER.get());
    }

    @Override
    public void tick() {
        handleAntlineActivation();
    }

    private void handleAntlineActivation() {
        Level world = this.level;
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        boolean activated = state.getValue(FizzlerEmitterBlock.ACTIVE);
        Direction facing = state.getValue(FizzlerEmitterBlock.FACING);
        Direction upDirection = ((FizzlerEmitterBlock) state.getBlock()).getUpperDirection(state);

        int distance = this.distanceToOtherSide(facing, upDirection);

        // No other side or path is blocked
        if (distance == 0) {
            return;
        }

        // Indicator powered
        BlockPos otherFizzlerPos = pos.relative(facing, distance);
        List<BlockPos> indicatorPositions = getIndicatorPositions(state, world, pos);
        indicatorPositions.addAll(getIndicatorPositions(world.getBlockState(otherFizzlerPos), world, otherFizzlerPos));
        IndicatorInfo indicatorInfo = IndicatorActivated.checkPositions(world, indicatorPositions);
        if (indicatorInfo.hasIndicators) {
            if (indicatorInfo.allIndicatorsActivated != activated) {
                this.setActive(indicatorInfo.allIndicatorsActivated, distance, facing, upDirection);
            }
            return;
        }

        // No indicators
        if (!activated) {
            this.setActive(true, distance, facing, upDirection);
        }
    }

    // Activate both emitters and change field
    public void setActive(boolean active, int distance, Direction facing, Direction upDirection) {
        if (distance > 0) {
            setField(active, distance, facing, upDirection);

            BlockPos oppositeEmitterPos = this.getBlockPos().relative(facing, distance);
            BlockState oppositeEmitterState = this.level.getBlockState(oppositeEmitterPos);

            SoundEvent soundEvent = active ? SoundInit.FIZZLER_ACTIVATE.get() : SoundInit.FIZZLER_DEACTIVATE.get();
            float pitch = (ModUtil.randomSoundPitch() + 2) / 3; // 3x lower random pitch
            this.level.playSound(null, this.getBlockPos().relative(facing, distance / 2), soundEvent, SoundSource.BLOCKS, 1, pitch);

            ((FizzlerEmitterBlock) oppositeEmitterState.getBlock()).setBlockStateValue(FizzlerEmitterBlock.ACTIVE, active, oppositeEmitterState, this.level, oppositeEmitterPos);

            ((FizzlerEmitterBlock) this.getBlockState().getBlock()).setBlockStateValue(FizzlerEmitterBlock.ACTIVE, active, this.getBlockState(), this.level, this.getBlockPos());

            ((FizzlerEmitterBlock) oppositeEmitterState.getBlock()).updateAllNeighbors(this.level, oppositeEmitterPos, oppositeEmitterState);
            ((FizzlerEmitterBlock) this.getBlockState().getBlock()).updateAllNeighbors(this.level, this.getBlockPos(), this.getBlockState());
        }
    }

    public void setField(boolean active, int distance, Direction facing, Direction upDirection) {
        boolean rotated = this.getBlockState().getValue(FizzlerEmitterBlock.ROTATED);
        for (int i = 1; i < distance; i++) {
            if (active) {
                BlockState fizzlerField = BlockInit.FIZZLER_FIELD.get().defaultBlockState()
                        .setValue(FizzlerFieldBlock.ROTATED, rotated)
                        .setValue(FizzlerFieldBlock.AXIS, facing.getAxis());
                this.level.setBlock(this.getBlockPos().relative(facing, i), fizzlerField, 2);
                this.level.setBlock(this.getBlockPos().relative(facing, i).relative(upDirection), fizzlerField.setValue(FizzlerFieldBlock.HALF, DoubleBlockHalf.UPPER), 2);
            } else {
                this.level.setBlock(this.getBlockPos().relative(facing, i), Blocks.AIR.defaultBlockState(), 2);
                this.level.setBlock(this.getBlockPos().relative(facing, i).relative(upDirection), Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    public int distanceToOtherSide(Direction direction, Direction upDirection) {
        for (int i = 1; i <= MAX_DISTANCE; i++) {
            BlockState lowerState = this.level.getBlockState(this.getBlockPos().relative(direction, i));
            BlockState upperState = this.level.getBlockState(this.getBlockPos().relative(direction, i).relative(upDirection));

            if (this.isOtherSide(lowerState, direction, false) && this.isOtherSide(upperState, direction, true)) {
                return i;
            }

            if (this.isBlockingField(lowerState, direction, false) || this.isBlockingField(upperState, direction, true)) {
                return 0;
            }
        }

        return 0;
    }

    public boolean isBlockingField(BlockState state, Direction direction, boolean upper) {
        if (state.getBlock() instanceof FizzlerFieldBlock) {
            return state.getValue(FizzlerFieldBlock.AXIS) != direction.getAxis()
                    || upper != (state.getValue(FizzlerFieldBlock.HALF) == DoubleBlockHalf.UPPER);
        }

        return !state.getMaterial().isReplaceable();
    }

    public boolean isOtherSide(BlockState state, Direction direction, boolean upper) {
        if (state.getBlock() instanceof FizzlerEmitterBlock) {
            return state.getValue(FizzlerEmitterBlock.FACING) == direction.getOpposite()
                    && upper == (state.getValue(FizzlerEmitterBlock.HALF) == DoubleBlockHalf.UPPER);
        }

        return false;
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState state, Level world, BlockPos pos) {
        Direction up = ((FizzlerEmitterBlock) state.getBlock()).getUpperDirection(state);
        Direction backwards = state.getValue(FizzlerEmitterBlock.FACING).getOpposite();

        Vec3i perpendicular = up.getNormal().cross(backwards.getNormal());
        Direction side = Direction.fromNormal(perpendicular.getX(), perpendicular.getY(), perpendicular.getZ());

        return new ArrayList<>(Arrays.asList(
                pos.relative(side),
                pos.relative(side).relative(up),
                pos.relative(side.getOpposite()),
                pos.relative(side.getOpposite()).relative(up),
                pos.relative(backwards).relative(side),
                pos.relative(backwards).relative(side).relative(up),
                pos.relative(backwards).relative(side.getOpposite()),
                pos.relative(backwards).relative(side.getOpposite()).relative(up)
        ));
    }
}
