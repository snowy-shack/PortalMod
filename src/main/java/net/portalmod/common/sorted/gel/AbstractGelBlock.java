package net.portalmod.common.sorted.gel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.portalmod.common.sorted.gel.container.GelContainer;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import java.util.HashMap;

public class AbstractGelBlock extends BreakableBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST =  BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST =  BlockStateProperties.WEST;
    public static final BooleanProperty UP =    BlockStateProperties.UP;
    public static final BooleanProperty DOWN =  BlockStateProperties.DOWN;
    
    public static final HashMap<Direction, BooleanProperty> STATES = new HashMap<>();
    private static final HashMap<Direction, VoxelShapeGroup> SHAPES = new HashMap<>();
    
    static {
        STATES.put(Direction.NORTH, NORTH);
        STATES.put(Direction.SOUTH, SOUTH);
        STATES.put(Direction.EAST,  EAST);
        STATES.put(Direction.WEST,  WEST);
        STATES.put(Direction.UP,    UP);
        STATES.put(Direction.DOWN,  DOWN);
        genAABBs();
    }

    private BlockState emptyBlockState;

    public AbstractGelBlock(Properties properties) {
        super(properties);
        BlockState test = this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST,  false)
                .setValue(SOUTH, false)
                .setValue(WEST,  false)
                .setValue(UP,    false);
        this.emptyBlockState = (test.setValue(DOWN,  false));
        this.registerDefaultState(test.setValue(DOWN,  true));
    }
    
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }
    
    private static void genAABBs() {
        VoxelShapeGroup shape = new VoxelShapeGroup.Builder(0, 0, 0, 16, 1, 16).build();
        
        for(Direction facing : Direction.values()) {
            Mat4 matrix = Mat4.identity();
            matrix.translate(new Vec3(.5));
            
            if(facing.getAxisDirection() == AxisDirection.POSITIVE)
                matrix.scale(new Vec3(facing.getOpposite().getNormal()).mul(2).add(1));

            if(facing.getAxis() == Axis.X)
                matrix.rotateDeg(Vector3f.ZP, -90);
            if(facing.getAxis() == Axis.Z)
                matrix.rotateDeg(Vector3f.XP, 90);

            matrix.translate(new Vec3(-.5));
            SHAPES.put(facing, shape.clone().transform(matrix, true));
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, World world, BlockPos pos, Block block, BlockPos neighbor, boolean b) {
        Direction direction = Direction.getNearest(
                neighbor.getX() - pos.getX(),
                neighbor.getY() - pos.getY(),
                neighbor.getZ() - pos.getZ()
        );

        BlockState supporting = world.getBlockState(neighbor);
        if (!supporting.isFaceSturdy(world, pos, direction)) {
            world.setBlock(pos, blockState.setValue(STATES.get(direction), false), Constants.BlockFlags.DEFAULT);
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundInit.GEL_BREAK.get(), SoundCategory.BLOCKS, 1, 1);
        }

        super.neighborChanged(blockState, world, pos, block, neighbor, b);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState previousState = context.getLevel().getBlockState(context.getClickedPos());
        if(previousState.getBlock() != this)
            previousState = this.emptyBlockState;
        
        BlockRayTraceResult ray = ModUtil.rayTraceBlock(context.getPlayer(), context.getLevel(), 10);
        if(ray.getBlockPos().equals(context.getClickedPos()))
            return null;
        return previousState.setValue(STATES.get(context.getClickedFace().getOpposite()), true);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        genAABBs();
        VoxelShape shape = VoxelShapes.empty();
        for(Direction facing : Direction.values())
            if(state.getValue(STATES.get(facing)))
                shape = VoxelShapes.or(shape, SHAPES.get(facing).getShape());
        return shape;
    }
    
    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);
        GelContainer.setAmount(stack, 16);
        return stack;
    }
    
    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> stacks) {
        ItemStack stack = new ItemStack(this);
        GelContainer.setAmount(stack, 16);
        stacks.add(stack);
    }
    
    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
//        return context.getItemInHand().getItem() == this.asItem();
        return true;
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
       return PushReaction.DESTROY;
    }
}