package net.portalmod.common.sorted.faithplate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

//todo turn into MultiBlock
public class FaithPlateBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<Face> FACE = EnumProperty.create("face", Face.class);
    
    public FaithPlateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(FACE, Face.FLOOR));
    }
    
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, FACE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos();

        if(face == Direction.DOWN)
            return null;

        if(face.getAxis() == Direction.Axis.Y) {
            Direction direction = context.getHorizontalDirection();

            if(!context.getLevel().getBlockState(pos.relative(direction)).canBeReplaced(context))
                return null;

            return this.defaultBlockState()
                    .setValue(FACING, direction)
                    .setValue(FACE, Face.FLOOR);
        }

        if(!context.getLevel().getBlockState(pos.relative(Direction.UP)).canBeReplaced(context))
            return null;

        return this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(FACE, Face.WALL)
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    public void setPlacedBy(World level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack itemStack) {
        if(level.isClientSide)
            return;

        DoubleBlockHalf half = state.getValue(HALF) == DoubleBlockHalf.LOWER ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
        BlockPos blockpos = pos.relative(FaithPlateBlock.getOtherBlockDirection(state));
        level.setBlock(blockpos, state.setValue(HALF, half), 3);
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos targetPos, boolean b) {
        if(level.isClientSide)
            return;

        if(getOtherBlock(state, level, pos).getBlock() != BlockInit.FAITHPLATE.get())
            level.destroyBlock(pos, false, null, 0);
    }

    public static Direction getOtherBlockDirection(BlockState state) {
        Direction direction = state.getValue(FACE) == Face.FLOOR ? state.getValue(FACING) : Direction.UP;

        if(state.getValue(HALF) == DoubleBlockHalf.UPPER && state.getValue(FACE) == Face.WALL
        || state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(FACE) == Face.FLOOR)
            return direction.getOpposite();
        return direction;
    }

    public static Direction getNormal(BlockState state) {
        return state.getValue(FACE) == Face.FLOOR ? Direction.UP : state.getValue(FACING);
    }

    private BlockState getOtherBlock(BlockState state, World level, BlockPos pos) {
        return level.getBlockState(pos.relative(getOtherBlockDirection(state)));
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
        if(level.isClientSide
                && player.getItemInHand(hand).getItem() == ItemInit.WRENCH.get()
                && FaithPlateTER.selected == null
//                && level.getBlockEntity(pos) instanceof FaithPlateTileEntity
                && state.getBlock() == BlockInit.FAITHPLATE.get()) {
            if(!state.hasTileEntity())
                pos = pos.relative(FaithPlateBlock.getOtherBlockDirection(state));

            BlockPos finalPos = pos;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FaithPlateClient.setScreen(finalPos));
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.DESTROY;
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
    
    public static enum Face implements IStringSerializable {
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