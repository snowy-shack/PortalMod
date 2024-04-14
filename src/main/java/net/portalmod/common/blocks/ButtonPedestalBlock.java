package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ButtonPedestalBlock extends DoubleBlock {

    public static final BooleanProperty PRESSED = BooleanProperty.create("pressed");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<ButtonMode> MODE = EnumProperty.create("mode", ButtonMode.class);
    public static final int BUTTON_DELAY = 20;

    public ButtonPedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(PRESSED, false)
                .setValue(ACTIVE, false)
                .setValue(MODE, ButtonMode.NORMAL)
        );
        this.initAABBs();
    }

    private static final Map<DoubleBlockHalf, VoxelShapeGroup> SHAPE = new HashMap<>();

    private static final VoxelShapeGroup LOWER = new VoxelShapeGroup.Builder()
        .add(4, 0, 4, 12, 2, 12)
        .add(5.5, 2.95, 5.5, 10.5, 16, 10.5)
        .add(6, 2, 6, 10, 16, 10)
        .build();

    private static final VoxelShapeGroup UPPER = new VoxelShapeGroup.Builder()
            .add(6, 0, 6, 10, 3, 10)
            .add(5.5, 0, 5.5, 10.5, 3, 10.5)
            .addPart("off", 6, 3, 6, 10, 5, 10)
            .addPart("on", 6, 3, 6, 10, 3.5, 10)
            .build();

    private void initAABBs() {
        for(DoubleBlockHalf half : DoubleBlockHalf.values()) {
            SHAPE.put(half, (half == DoubleBlockHalf.UPPER) ? UPPER.clone() : LOWER.clone());
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return SHAPE.get(state.getValue(HALF)).getVariant(state.getValue(PRESSED) ? "on" : "off");
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF, PRESSED, ACTIVE, MODE);
    }

    public boolean canActivate(BlockState blockState) {
//        if (blockState.getValue(MODE) == ButtonMode.PERSISTENT) {
//            return !blockState.getValue(PRESSED) &&  !blockState.getValue(ACTIVE);
//        }
        return !blockState.getValue(PRESSED);
    }

    public void activate(BlockState blockState, World world, BlockPos pos) {
        ButtonMode mode = blockState.getValue(MODE);
        Boolean wasActive = blockState.getValue(ACTIVE);
        this.setBlockStateValue(PRESSED, true, blockState, world, pos);
        world.getBlockTicks().scheduleTick(pos, this, BUTTON_DELAY);
        world.updateNeighborsAt(pos, this);
        this.playSound(world, pos, true);

        if (mode == ButtonMode.NORMAL) {
            this.setBlockStateValue(ACTIVE, true, blockState, world, pos);
        }
        else if (mode == ButtonMode.PERSISTENT && !wasActive) {
            this.setBlockStateValue(ACTIVE, true, blockState, world, pos);
        }
        else if (mode == ButtonMode.TOGGLE) {
            this.setBlockStateValue(ACTIVE, !wasActive, blockState, world, pos);
        }
    }

    @Override
    public void tick(BlockState blockState, ServerWorld world, BlockPos pos, Random random) {
        if (blockState.getValue(PRESSED)) {
            this.setBlockStateValue(PRESSED, false, blockState, world, pos);
            world.updateNeighborsAt(pos, this);
            this.playSound(world, pos, false);
            if (blockState.getValue(MODE) == ButtonMode.NORMAL) {
                this.setBlockStateValue(ACTIVE, false, blockState, world, pos);
            }
        }
    }

    public void playSound(World world, BlockPos pos, boolean activated) {
        world.playSound(null, pos, activated ? SoundInit.SUPER_BUTTON_ACTIVATE.get() : SoundInit.SUPER_BUTTON_DEACTIVATE.get(), SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {
            ButtonMode newMode = cycleMode(blockState, world, pos);
            this.setBlockStateValue(ACTIVE, false, blockState, world, pos);
            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.button_mode." + newMode.getSerializedName()), true);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        else if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER && this.canActivate(blockState)) {
            this.activate(blockState, world, pos);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.FAIL;
    }

    public ButtonMode cycleMode(BlockState blockState, World world, BlockPos pos) {
        ButtonMode currentMode = blockState.getValue(MODE);
        ButtonMode newMode = currentMode.cycle();
        this.setBlockStateValue(MODE, newMode, blockState, world, pos);
        return newMode;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (world.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState();
        }

        if (world.getBlockState(pos.below()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER);
        }

        return null;
    }

    @Override
    public boolean canSurvive(BlockState blockState, IWorldReader world, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);
        return blockState.getValue(HALF) == DoubleBlockHalf.LOWER ? belowState.isFaceSturdy(world, belowPos, Direction.UP) : belowState.is(this);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState updateBlockState, IWorld world, BlockPos pos, BlockPos updatePos) {
        if (blockState.canSurvive(world, pos)) {
            return super.updateShape(blockState, direction, updateBlockState, world, pos, updatePos);
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("button_pedestal", list);
    }
}
