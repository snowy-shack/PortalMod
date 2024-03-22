package net.portalmod.common.sorted.chamber_door;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

public class ChamberDoorBlock extends Block {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<Side> SIDE = EnumProperty.create("side", Side.class);

    private static final VoxelShapeGroup UPPER = new VoxelShapeGroup.Builder()
            .add(0, 0, 0, 2, 16, 16)
            .add(0, 14, 0, 16, 16, 16)
            .addPart("closed", 0, 0, 2, 16, 16, 14)
            .addPart("open", 0, 0, 2, 5, 16, 14)
            .build();

    private static final VoxelShapeGroup LOWER = new VoxelShapeGroup.Builder()
            .add(0, 0, 0, 2, 16, 16)
            .addPart("closed", 0, 0, 2, 16, 16, 14)
            .addPart("open", 0, 0, 2, 5, 16, 14)
            .build();

    public ChamberDoorBlock(AbstractBlock.Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(SIDE, Side.LEFT));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF, SIDE);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        int facing = state.getValue(FACING).get2DDataValue() * 90;
        int side = state.getValue(SIDE) == Side.LEFT ? 0 : 180;
        VoxelShapeGroup shapeGroup = state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER : LOWER;

        Mat4 matrix = Mat4.identity();
        matrix.translate(new Vec3(.5));
        matrix.rotateDeg(Vector3f.YN, facing + side);
        matrix.translate(new Vec3(-.5));

        return shapeGroup.clone()
                .transform(matrix)
                .getVariant(state.getValue(OPEN) ? "open" : "closed");
    }

    private enum Side implements IStringSerializable {
        LEFT,
        RIGHT;

        public String toString() {
            return this.getSerializedName();
        }

        public String getSerializedName() {
            return this == LEFT ? "left" : "right";
        }
    }
}