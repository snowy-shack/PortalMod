package net.portalmod.common.sorted.gel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        for (Direction direction : Direction.values()) {
            // Has gel but no supporting block in that direction
            if (blockState.getValue(STATES.get(direction)) && !isValidFace(direction.getOpposite(), world, pos.relative(direction))) {
                blockState = removeSide(direction, blockState);
                world.setBlockAndUpdate(pos, blockState);
                world.playSound(null, pos, SoundInit.GEL_BREAK.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSlightSoundPitch());
            }
        }

        super.neighborChanged(blockState, world, pos, block, neighbor, b);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedDirection = context.getClickedFace().getOpposite();
        BlockState previousState = context.getLevel().getBlockState(clickedPos);

        if (!isValidFace(context.getClickedFace(), context.getLevel(), clickedPos.relative(clickedDirection))) {
            return previousState;
        }

        if (!previousState.getBlock().is(this)) {
            previousState = this.emptyBlockState;
        }

        return addSide(clickedDirection, previousState);
    }

    public static boolean isValidFace(Direction face, World world, BlockPos pos) {
        return Block.isFaceFull(world.getBlockState(pos).getCollisionShape(world, pos), face);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean skipRendering(BlockState self, BlockState other, Direction direction) {
        if(other.is(this)) {
            boolean match = true;
            for (Direction face : Direction.values()) {
                if (face.getAxis() != direction.getAxis()) {
                    match &= other.getValue(STATES.get(face)) || !self.getValue(STATES.get(face));
                }
            }
            return match;
        }
        return super.skipRendering(self, other, direction);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
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

    public BlockState rotate(BlockState state) {
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);

        return state.setValue(NORTH, west)
                .setValue(EAST, north)
                .setValue(SOUTH, east)
                .setValue(WEST, south);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        for (int i = 0; i < ModUtil.getRotationAmount(rotation); i++) {
            state = this.rotate(state);
        }
        return state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);

        switch (mirror) {
            case LEFT_RIGHT: return state.setValue(NORTH, south).setValue(SOUTH, north);
            case FRONT_BACK: return state.setValue(EAST, west).setValue(WEST, east);
        }

        return state;
    }

    public static BlockState addSide(Direction side, BlockState state) {
        return state.setValue(STATES.get(side), true);
    }

    public static BlockState removeSide(Direction side, BlockState state) {
        BlockState newState = state.setValue(STATES.get(side), false);
        if (STATES.values().stream().noneMatch(newState::getValue)) {
            return Blocks.AIR.defaultBlockState();
        }
        return newState;
    }
}