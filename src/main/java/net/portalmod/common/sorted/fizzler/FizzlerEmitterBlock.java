package net.portalmod.common.sorted.fizzler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class FizzlerEmitterBlock extends Block {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public FizzlerEmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        // todo actually have a bounding box
        return VoxelShapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, HALF);
    }
}