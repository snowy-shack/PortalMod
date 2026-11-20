package net.portalmod.common.sorted.faithplate;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.EntityBlock;
import net.portalmod.common.blocks.DoubleBlock;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class FaithPlateBlock extends DoubleBlock implements EntityBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    public static final EnumProperty<Face> FACE = EnumProperty.create("face", Face.class);
    
    public FaithPlateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(FACE, Face.FLOOR));
    }

    @Override
    public Direction getUpperDirection(BlockState state) {
        return state.getValue(FACE) == Face.FLOOR ? state.getValue(FACING).getOpposite() : Direction.UP;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, FACE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getNearestLookingDirection().getOpposite();
        BlockPos pos = context.getClickedPos();

        if(face == Direction.DOWN)
            return null;

        // Floor faithplate
        if(face.getAxis() == Direction.Axis.Y) {
            Direction facing = context.getHorizontalDirection();

            if (!ModUtil.canPlaceAt(context, pos.relative(facing)))
                return null;

            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(FACE, Face.FLOOR);

        }

        if(!ModUtil.canPlaceAt(context, pos.relative(Direction.UP))) {
            return null;
        }

        // Wall faithplate
        return this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(FACE, Face.WALL)
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    public Direction getNormal(BlockState state) {
        return state.getValue(FACE) == Face.FLOOR ? Direction.UP : state.getValue(FACING);
    }

    public BlockPos getUpperPos(BlockState state, BlockPos pos) {
        return this.getMainPosition(state, pos).relative(this.getUpperDirection(state));
    }

    public boolean hasTileEntity(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }
    
    @Nullable
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TileEntityTypeInit.FAITHPLATE.get().create(pos, state);
    }

    // 1.17+: block entities tick through a ticker the block hands out (was TickableBlockEntity).
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityTypeInit.FAITHPLATE.get() ? (lvl, pos, st, be) -> ((FaithPlateTileEntity) be).tick() : null;
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (level.isClientSide)
            return InteractionResult.PASS;

        BlockEntity tileEntity = level.getBlockEntity(this.getUpperPos(state, pos));

        if (!(tileEntity instanceof FaithPlateTileEntity)) return InteractionResult.PASS;

        FaithPlateTileEntity faithPlate = (FaithPlateTileEntity)tileEntity;

        if(WrenchItem.usedWrench(player, hand) && !FaithPlateTileEntity.isPlayerConfiguring(player)) {
            if(!faithPlate.isBeingConfigured()) {
                faithPlate.startConfiguration((ServerPlayer)player);
                WrenchItem.playUseSound(level, rayTraceResult.getLocation());
            } else {
                WrenchItem.playFailSound(level, rayTraceResult.getLocation());
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean b) {
        if(state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            FaithPlateTileEntity te = ((FaithPlateTileEntity) level.getBlockEntity(pos));
            if(te != null) {
                te.endConfiguration();
            }
        }

        super.onRemove(state, level, pos, newState, b);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.BLOCK;
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("faithplate", list);
    }
    
    public enum Face implements StringRepresentable {
        FLOOR("floor"),
        WALL("wall");
        
        private final String name;
        
        Face(String name) {
            this.name = name;
        }
        
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}