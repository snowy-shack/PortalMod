package net.portalmod.common.sorted.fizzler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.blocks.DoubleBlock;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import java.util.HashMap;
import java.util.Map;

public class FizzlerFieldBlock extends DoubleBlock implements Fizzler {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public FizzlerFieldBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.X)
                .setValue(HALF, DoubleBlockHalf.LOWER));
        this.initAABBs();
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS, HALF);
    }


    private static final Map<Direction.Axis, VoxelShapeGroup> SHAPE = new HashMap<>();
    private static final VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
            .add(0,0,7,16,16,9)
            .build();

    private void initAABBs() {
        for(Direction.Axis axis : Direction.Axis.values()) {
            Mat4 matrix = Mat4.identity();
            matrix.translate(new Vec3(.5));

            matrix.rotateDeg(Vector3f.YP, (axis == Direction.Axis.X) ? 0 : 90);
            matrix.translate(new Vec3(-.5));

            SHAPE.put(axis, shape.clone().transform(matrix));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
//        return getFieldShape(state);
    }

    public VoxelShape getFieldShape(BlockState state) {
        return SHAPE.get(state.getValue(AXIS)).getShape();
    }

    @Override
    public boolean isInsideField(AxisAlignedBB box, BlockPos pos, BlockState state) {
        return this.getFieldShape(state).bounds().move(pos).intersects(box);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos pos1, boolean b) {
        super.neighborChanged(state, world, pos, block, pos1, b);

        Direction facing = Direction.fromAxisAndDirection(state.getValue(AXIS), Direction.AxisDirection.POSITIVE);
        Block leftBlock = world.getBlockState(pos.relative(facing)).getBlock();
        Block rightBlock = world.getBlockState(pos.relative(facing.getOpposite())).getBlock();

        if ((leftBlock instanceof FizzlerFieldBlock || leftBlock instanceof FizzlerEmitterBlock) && (rightBlock instanceof FizzlerFieldBlock || rightBlock instanceof FizzlerEmitterBlock)) {
            return;
        }

        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        world.updateNeighborsAt(pos, this);
    }

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
        if (entity instanceof TestElementEntity) {
            VoxelShape voxelshape = getFieldShape(state);
            VoxelShape movedBlockShape = voxelshape.move(pos.getX(), pos.getY(), pos.getZ());
            VoxelShape entityShape = VoxelShapes.create(entity.getBoundingBox());
            if (VoxelShapes.joinIsNotEmpty(movedBlockShape, entityShape, IBooleanFunction.AND)) {
                ((TestElementEntity) entity).startFizzling();
            }
        }

        if(level.isClientSide)
            return;

        if (entity instanceof PlayerEntity) {
            PortalGun.fizzleGun(level, (PlayerEntity) entity);
        }

        if (entity instanceof ItemEntity && ((ItemEntity) entity).getItem().getItem() instanceof PortalGun) {
            PortalGun.fizzleGunItem(((ItemEntity) entity).getItem());
        }
    }
}