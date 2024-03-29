package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import java.util.HashMap;
import java.util.Map;

public class FrameBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public boolean isFilled;

    public FrameBlock(Properties properties, boolean isFilled) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP));
        this.initAABBs();
        this.isFilled = isFilled;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader level, BlockPos pos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static final Map<Direction, VoxelShapeGroup> SHAPE = new HashMap<>();
    private static final Map<Direction, VoxelShapeGroup> COLLISIONSHAPE = new HashMap<>();

    private void initAABBs() {
        VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
                .add(0, 0, 0, 2, 16, 16)
                .build();
        VoxelShapeGroup collisionShape = new VoxelShapeGroup.Builder()
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

            SHAPE.put(facing, shape.clone().transform(matrix));
            COLLISIONSHAPE.put(facing, collisionShape.clone().transform(matrix));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return SHAPE.get(state.getValue(FACING)).getShape();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return isFilled ? this.getShape(state, level, pos, context) : COLLISIONSHAPE.get(state.getValue(FACING)).getShape();
    }
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if(context.getPlayer().isCrouching()) {
            return this.stateDefinition.any()
                    .setValue(FACING, context.getNearestLookingDirection());
        } else {
            return this.stateDefinition.any()
                    .setValue(FACING, context.getNearestLookingDirection().getOpposite());
        }
    }
}
