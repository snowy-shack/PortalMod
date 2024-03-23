package net.portalmod.common.sorted.panel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Objects;

public class PanelBlock extends Block {

    public final boolean canBeBig;
    public static final EnumProperty<PanelState> STATE = EnumProperty.create("state", PanelState.class);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public PanelBlock(boolean canBeBig, Properties properties) {
        super(properties);
        this.canBeBig = canBeBig;
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, PanelState.SINGLE).setValue(AXIS, Direction.Axis.X));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        Direction clickedFace = context.getClickedFace();
        BlockPos clickedOnPos = context.getClickedPos().relative(clickedFace.getOpposite());
        BlockState clickedBlock = world.getBlockState(clickedOnPos);

        // Block is not this one or shift is down
        if (!clickedBlock.getBlock().is(this) || Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
            return this.defaultBlockState();
        }

        PanelState clickedPanelState = clickedBlock.getValue(STATE);

        // Block is single and placing on top or bottom
        if (clickedPanelState.isSingle() && !isSide(clickedFace)) {
//            world.setBlockAndUpdate(clickedOnPos, clickedBlock.setValue(STATE, clickedFace == Direction.UP ? PanelState.BOTTOM : PanelState.TOP));
            return this.defaultBlockState().setValue(STATE, clickedFace == Direction.UP ? PanelState.TOP : PanelState.BOTTOM);
        }

        // Block is double and placing on side
        if (this.canBeBig && clickedPanelState.isDouble() && isSide(clickedFace) && isBlockInInventoryAndRemove(context.getPlayer(), this)) {
//
//            // New block
//            world.setBlock(context.getClickedPos().relative(clickedPanelState.getVerticalFacing()), clickedBlock
//                    .setValue(STATE, directionalState(clickedFace, clickedPanelState.opposite()))
//                    .setValue(AXIS, clickedFace.getAxis()), 0);
//
//            // Block under/above clicked block
//            world.setBlock(clickedOnPos.relative(clickedPanelState.getVerticalFacing()), clickedBlock
//                    .setValue(STATE, directionalState(clickedFace.getOpposite(), clickedPanelState.opposite()))
//                    .setValue(AXIS, clickedFace.getAxis()), 0);
//
//            // Clicked block
//            world.setBlock(clickedOnPos, clickedBlock
//                    .setValue(STATE, directionalState(clickedFace.getOpposite(), clickedPanelState))
//                    .setValue(AXIS, clickedFace.getAxis()), 0);

            return this.defaultBlockState()
                    .setValue(STATE, directionalState(clickedFace, clickedPanelState))
                    .setValue(AXIS, clickedFace.getAxis());
        }

        return this.defaultBlockState();
    }

    @Override
    public void onPlace(BlockState blockState, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
        PanelState panelState = blockState.getValue(STATE);

        // Place the other half
        if (panelState.isDouble()) {
            world.setBlockAndUpdate(pos.relative(panelState.getVerticalFacing()), blockState.setValue(STATE, panelState.oppositeVertical()));
            return;
        }

        Direction.Axis axis = blockState.getValue(AXIS);

        // Place the other 3 blocks
        if (panelState.isQuadruple()) {
            Direction direction = Direction.fromAxisAndDirection(axis, panelState.isLeft() ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);

            // Clicked block
            setBlockUnlessStateMatches(world, pos, directionalState(direction.getOpposite(), panelState), axis);

            // New block
            setBlockUnlessStateMatches(world, pos.relative(panelState.getVerticalFacing()), directionalState(direction, panelState.oppositeVertical()), axis);

            // Block under/above clicked block
            setBlockUnlessStateMatches(world, pos.relative(panelState.getVerticalFacing()), directionalState(direction.getOpposite(), panelState.oppositeVertical()), axis);
        }
    }

    public static boolean isSide(Direction direction) {
        return direction != Direction.UP && direction != Direction.DOWN;
    }

    public void setBlockUnlessStateMatches(World world, BlockPos pos, PanelState state, Direction.Axis axis) {
        BlockState checkingState = world.getBlockState(pos);
        if (checkingState.is(this) && checkingState.getValue(STATE) == state && checkingState.getValue(AXIS) == axis) {
            return;
        }
        world.setBlockAndUpdate(pos, this.defaultBlockState().setValue(STATE, state).setValue(STATE, state));
    }

    public static PanelState directionalState(Direction direction, PanelState placedState) {
        boolean isBottom = placedState.isBottom();
        switch (direction) {
            case EAST:
            case NORTH:
                return isBottom ? PanelState.BOTTOM_RIGHT : PanelState.TOP_RIGHT;

            case WEST:
            case SOUTH:
                return isBottom ? PanelState.BOTTOM_LEFT : PanelState.TOP_LEFT;
        }
        return PanelState.SINGLE;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction p_196271_2_, BlockState p_196271_3_, IWorld world, BlockPos blockPos, BlockPos p_196271_6_) {
        Direction checkingDirection = Direction.UP;
        PanelState correctState = PanelState.TOP;
        switch (blockState.getValue(STATE)) {
            case TOP:
                checkingDirection = Direction.DOWN;
                correctState = PanelState.BOTTOM;
                break;
            case TOP_LEFT:
                checkingDirection = Direction.DOWN;
                correctState = PanelState.BOTTOM_LEFT;
                break;
            case TOP_RIGHT:
                checkingDirection = Direction.DOWN;
                correctState = PanelState.BOTTOM_RIGHT;
                break;
            case BOTTOM_LEFT:
                correctState = PanelState.TOP_LEFT;
                break;
            case BOTTOM_RIGHT:
                correctState = PanelState.TOP_RIGHT;
                break;
        }

//        BlockState checkingBlockState = world.getBlockState(blockPos.relative(checkingDirection));
//        if (checkingBlockState.getBlock().is(this)) {
//            if (checkingBlockState.getValue(STATE) != correctState){
//                return this.defaultBlockState();
//            }
//        } else {
//            return this.defaultBlockState();
//        }

        return super.updateShape(blockState, p_196271_2_, p_196271_3_, world, blockPos, p_196271_6_);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE, AXIS);
    }

    public static boolean isBlockInInventoryAndRemove(PlayerEntity player, Block block) {
        if (player.isCreative()) {
            return true;
        }

        for (ItemStack itemStack : player.getAllSlots()) {
            Item item = itemStack.getItem();
            if (item instanceof BlockItem && ((BlockItem) item).getBlock().is(block)) {
                itemStack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
