package net.portalmod.common.sorted.radio;

import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.state.*;
import net.minecraft.state.StateContainer.*;
import net.minecraft.state.properties.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import net.portalmod.core.init.TileEntityTypeInit;

public class RadioBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<RadioState> STATE = EnumProperty.create("state", RadioState.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    VoxelShape AABB = Block.box(3.5, 0, 6, 12.5, 6, 10);
    VoxelShape AABB_SIDE = Block.box(6, 0, 3.5, 10, 6, 12.5);
    
    public RadioBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATE, RadioState.OFF)
                .setValue(POWERED, false));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader level) {
        return TileEntityTypeInit.RADIO.get().create();
    }
    
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE, POWERED);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? AABB_SIDE : AABB;
    }
    
    @Override
    public boolean canSurvive(BlockState state, IWorldReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult rayTraceResult) {
        if(!level.isClientSide())
            ((RadioBlockTileEntity)level.getBlockEntity(pos)).switchManual();
        return ActionResultType.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos targetPos, boolean b) {
        if(level.isClientSide())
            return;

        if(!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true, null, 0);
            return;
        }

        RadioBlockTileEntity tile = (RadioBlockTileEntity) level.getBlockEntity(pos);
        if(level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(POWERED, true), 2);
            tile.play();
        } else {
            if(state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, false), 2);
                if(level.dimension() != World.END)
                    tile.stop();
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean isPowered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        RegistryKey<World> dimension = context.getLevel().dimension();

        BlockState state = this.stateDefinition.any()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(POWERED, isPowered);

        if(dimension == World.OVERWORLD)
            state = state.setValue(STATE, isPowered ? RadioState.ON : RadioState.OFF);
        else if(dimension == World.END)
            state = state.setValue(STATE, isPowered ? RadioState.ACTIVE : RadioState.INACTIVE);
        return state;
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.DESTROY;
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}