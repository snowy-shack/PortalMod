package net.portalmod.common.sorted.radio;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.portalmod.core.init.TileEntityTypeInit;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

public class RadioBlock extends Block implements EntityBlock {
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

    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TileEntityTypeInit.RADIO.get().create(pos, state);
    }

    // 1.17+: block entities tick through a ticker the block hands out (was TickableBlockEntity).
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityTypeInit.RADIO.get() ? (lvl, pos, st, be) -> ((RadioBlockTileEntity) be).tick() : null;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE, POWERED);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext selectionContext) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? AABB_SIDE : AABB;
    }
    
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult rayTraceResult) {
        if(!level.isClientSide())
            ((RadioBlockTileEntity)level.getBlockEntity(pos)).switchManual();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos targetPos, boolean b) {
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
                if(level.dimension() != Level.END)
                    tile.stop();
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean isPowered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        ResourceKey<Level> dimension = context.getLevel().dimension();

        BlockState state = this.stateDefinition.any()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(POWERED, isPowered);

        if(dimension == Level.OVERWORLD)
            state = state.setValue(STATE, isPowered ? RadioState.ON : RadioState.OFF);
        else if(dimension == Level.END)
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