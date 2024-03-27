package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ChamberLightsBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    
    public ChamberLightsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }
    
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos clickedPos = context.getClickedPos();
        World world = context.getLevel();
        if (world.getBlockState(clickedPos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
        }
        else if (world.getBlockState(clickedPos.below()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis()).setValue(HALF, DoubleBlockHalf.UPPER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity p_180633_4_, ItemStack p_180633_5_) {
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        world.setBlockAndUpdate(pos.relative(isLower ? Direction.UP : Direction.DOWN), blockState.setValue(HALF, isLower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER));
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction p_196271_2_, BlockState otherState, IWorld world, BlockPos pos, BlockPos p_196271_6_) {
        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
        if (!world.getBlockState(pos.relative(isLower ? Direction.UP : Direction.DOWN)).is(this)) {
            return Blocks.AIR.defaultBlockState();
        }
        return blockState;
    }

//    @Override
//    public void playerWillDestroy(World world, BlockPos pos, BlockState blockState, PlayerEntity p_176208_4_) {
//        breakOtherHaldIfNeeded(world, blockState, pos);
//        super.playerWillDestroy(world, pos, blockState, p_176208_4_);
//    }
//
//    public void breakOtherHaldIfNeeded(World world, BlockState blockState, BlockPos pos) {
//        boolean isLower = blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
//        world.setBlockAndUpdate(pos.relative(isLower ? Direction.UP : Direction.DOWN), Blocks.AIR.defaultBlockState());
//    }
}