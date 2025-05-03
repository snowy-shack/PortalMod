package net.portalmod.common.blocks;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link MultiBlock} for two-high blocks
 */
public class DoubleBlock extends MultiBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public DoubleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockPos getMainPosition(BlockState blockState, BlockPos pos) {
        return blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }

    @Override
    public List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos) {
        return new ArrayList<>(Arrays.asList(
                mainPos.above()
        ));
    }

    @Override
    public void placeConnectedBlocks(World world, BlockState blockState, BlockPos pos) {
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        world.setBlockAndUpdate(pos.relative(isLower ? Direction.UP : Direction.DOWN), blockState.setValue(HALF, isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER));
    }

    @Override
    public StatePropertiesPredicate.Builder mainBlockPredicate() {
        return StatePropertiesPredicate.Builder.properties().hasProperty(HALF, DoubleBlockHalf.LOWER);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean placedOnLowerSide = context.getClickLocation().y - pos.getY() < 0.5;
        boolean canBeLower = world.getBlockState(pos.above()).canBeReplaced(context);
        boolean canBeUpper = world.getBlockState(pos.below()).canBeReplaced(context);

        // Placement preference

        if (placedOnLowerSide) {
            if (canBeUpper) {
                return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER);
            }
        } else {
            if (canBeLower) {
                return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER);
            }
        }

        // Placement fallback

        if (canBeLower) {
            return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER);
        }

        if (canBeUpper) {
            return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER);
        }

        return null;
    }
}
