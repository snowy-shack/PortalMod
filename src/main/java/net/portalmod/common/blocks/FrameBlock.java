package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FrameBlock extends Block implements IWaterLoggable {
    public static Set<Block> FRAME_BLOCKS = new HashSet<>();

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public boolean isFilled;

    public FrameBlock(Properties properties, boolean isFilled) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, false));
        this.initAABBs();
        this.isFilled = isFilled;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    private static final Map<Direction, VoxelShapeGroup> FILLED_SHAPE = new HashMap<>();
    private static final Map<Direction, VoxelShapeGroup> HOLLOW_SHAPE = new HashMap<>();

    private void initAABBs() {
        VoxelShapeGroup filledShape = new VoxelShapeGroup.Builder()
                .add(0, 0, 0, 2, 16, 16)
                .build();
        VoxelShapeGroup hollowShape = new VoxelShapeGroup.Builder()
                .add(0,0, 0, 2, 1,16)
                .add(0,15,0, 2,16,16)
                .add(0,1, 0, 2,15, 1)
                .add(0,1, 15,2,15,16)
                .build();

        for(Direction facing : Direction.values()) {
            Mat4 matrix = Mat4.identity();
            matrix.translate(new Vec3(.5));

            if(facing.getAxis() == Direction.Axis.Y) {
                int angle = (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) ? 90 : -90;
                matrix.rotateDeg(Vector3f.ZP, angle);
            } else {
                int angle = facing.get2DDataValue() * -90 + 90;
                matrix.rotateDeg(Vector3f.YP, angle);
            }

            matrix.translate(new Vec3(-.5));

            FILLED_SHAPE.put(facing, filledShape.clone().transform(matrix));
            HOLLOW_SHAPE.put(facing, hollowShape.clone().transform(matrix));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        if (FRAME_BLOCKS.isEmpty()) {
            DefaultedRegistry.BLOCK.stream().filter(block -> block instanceof FrameBlock).forEach(block -> FRAME_BLOCKS.add(block));
        }

        boolean holdingFrame = FRAME_BLOCKS.stream().anyMatch(block -> context.isHoldingItem(block.asItem()));

        return (this.isFilled || holdingFrame ? FILLED_SHAPE : HOLLOW_SHAPE).get(state.getValue(FACING)).getShape();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return this.isFilled ? this.getShape(state, level, pos, context) : HOLLOW_SHAPE.get(state.getValue(FACING)).getShape();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection();
        return this.stateDefinition.any()
                .setValue(FACING, context.getPlayer().isShiftKeyDown() ? nearestLookingDirection.getOpposite() : nearestLookingDirection)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState p_196271_1_, Direction p_196271_2_, BlockState p_196271_3_, IWorld p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_) {
        if (p_196271_1_.getValue(WATERLOGGED)) {
            p_196271_4_.getLiquidTicks().scheduleTick(p_196271_5_, Fluids.WATER, Fluids.WATER.getTickDelay(p_196271_4_));
        }
        return p_196271_1_;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
