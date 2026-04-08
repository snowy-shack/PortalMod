package net.portalmod.common.sorted.faithplate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.blocks.DoubleBlock;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class FaithPlateBlock extends DoubleBlock {
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
    public BlockState getStateForPlacement(BlockItemUseContext context) {
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

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }
    
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityTypeInit.FAITHPLATE.get().create();
    }
    
    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (level.isClientSide)
            return ActionResultType.PASS;

        TileEntity tileEntity = level.getBlockEntity(this.getUpperPos(state, pos));

        if (!(tileEntity instanceof FaithPlateTileEntity)) return ActionResultType.PASS;

        FaithPlateTileEntity faithPlate = (FaithPlateTileEntity)tileEntity;

        if(WrenchItem.usedWrench(player, hand) && !FaithPlateTileEntity.isPlayerConfiguring(player)) {
            if(!faithPlate.isBeingConfigured()) {
                faithPlate.startConfiguration((ServerPlayerEntity)player);
                WrenchItem.playUseSound(level, rayTraceResult.getLocation());
            } else {
                WrenchItem.playFailSound(level, rayTraceResult.getLocation());
            }

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
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
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("faithplate", list);
    }
    
    public enum Face implements IStringSerializable {
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