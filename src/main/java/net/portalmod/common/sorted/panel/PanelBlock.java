package net.portalmod.common.sorted.panel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PanelBlock extends Block implements PortalHelper {
    public static final EnumProperty<PanelState> STATE = EnumProperty.create("state", PanelState.class);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public PanelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, PanelState.SINGLE).setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE, AXIS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        Direction clickedFace = context.getClickedFace();
        BlockPos clickedOnPos = context.getClickedPos().relative(clickedFace.getOpposite());
        BlockState clickedBlock = world.getBlockState(clickedOnPos);
        PlayerEntity player = context.getPlayer();

        // Block is not this one or shift is down
        if (!clickedBlock.getBlock().is(this) || Objects.requireNonNull(player).isShiftKeyDown()) {
            return this.defaultBlockState();
        }

        PanelState clickedPanelState = clickedBlock.getValue(STATE);

        // Block is single and placing on top or bottom
        if (clickedPanelState.isSingle() && !isSide(clickedFace)) {
            return this.defaultBlockState().setValue(STATE, clickedFace == Direction.UP ? PanelState.TOP : PanelState.BOTTOM);
        }

        // Block is double and placing on side
        if (clickedPanelState.isDouble() && isSide(clickedFace) && areTwoBlocksInInventory(player, this) && world.getBlockState(context.getClickedPos().relative(clickedPanelState.getVerticalFacing())).canBeReplaced(context)) {
            removeBlockFromInventory(player, this);
            return this.defaultBlockState()
                    .setValue(STATE, getStateFromDirection(clickedFace, clickedPanelState))
                    .setValue(AXIS, clickedFace.getAxis());
        }

        return this.defaultBlockState();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
        PanelState panelState = blockState.getValue(STATE);

        // Place the other half
        if (panelState.isDouble()) {
            world.setBlockAndUpdate(pos.relative(panelState.getVerticalFacing()), blockState.setValue(STATE, panelState.oppositeVertical()));
            return;
        }

        Direction.Axis axis = blockState.getValue(AXIS);

        // Place the other 3 blocks
        if (panelState.isQuadruple()) {
            Direction direction = axis == Direction.Axis.X ? panelState.isLeft() ? Direction.EAST : Direction.WEST : panelState.isLeft() ? Direction.NORTH : Direction.SOUTH;

            // New block
            setBlock(world, pos.relative(panelState.getVerticalFacing()), getStateFromDirection(direction.getOpposite(), panelState.oppositeVertical()), axis);

            // Clicked block
            setBlock(world, pos.relative(direction), getStateFromDirection(direction, panelState), axis);

            // Block under/above clicked block
            setBlock(world, pos.relative(direction).relative(panelState.getVerticalFacing()), getStateFromDirection(direction, panelState.oppositeVertical()), axis);
        }
    }

    public static boolean isSide(Direction direction) {
        return direction != Direction.UP && direction != Direction.DOWN;
    }

    public void setBlock(World world, BlockPos pos, PanelState state, Direction.Axis axis) {
        world.setBlockAndUpdate(pos, this.defaultBlockState().setValue(STATE, state).setValue(AXIS, axis));
    }

    public static PanelState getStateFromDirection(Direction direction, PanelState placedState) {
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
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        PanelState panelState = state.getValue(STATE);
        Direction.Axis axis = state.getValue(AXIS);

        // Is single or connection is not in that direction
        if (panelState.isSingle() || !getConnectedPositions(state, pos).contains(neighborPos)) {
            return state;
        }

        // Incorrect sideways connection
        if (panelState.isQuadruple() && direction.getAxis() != Direction.Axis.Y
                && (!neighborState.is(this) || axis != neighborState.getValue(AXIS) || panelState.oppositeHorizontal() != neighborState.getValue(STATE))) {
            return state.setValue(STATE, panelState.isBottom() ? PanelState.BOTTOM : PanelState.TOP);
        }

        // Incorrect vertical connection
        if (direction.getAxis() == Direction.Axis.Y
                && (!neighborState.is(this) || panelState.isBottom() == neighborState.getValue(STATE).isBottom())) {
            return state.setValue(STATE, PanelState.SINGLE);
        }

        return state;
    }

    public static boolean areTwoBlocksInInventory(PlayerEntity player, Block block) {
        if (player.isCreative()) {
            return true;
        }

        int total = 0;
        for (ItemStack itemStack : player.getAllSlots()) {
            Item item = itemStack.getItem();
            if (item instanceof BlockItem && ((BlockItem) item).getBlock().is(block)) {
                total += itemStack.getCount();
                if (total >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeBlockFromInventory(PlayerEntity player, Block block) {
        for (ItemStack itemStack : player.getAllSlots()) {
            Item item = itemStack.getItem();
            if (item instanceof BlockItem && ((BlockItem) item).getBlock().is(block)) {
                itemStack.shrink(1);
            }
        }
    }

    public Set<BlockPos> getConnectedPositions(BlockState state, BlockPos pos) {
        Set<BlockPos> set = new HashSet<>();
        PanelState panelState = state.getValue(STATE);
        Direction.Axis axis = state.getValue(AXIS);

        if (panelState.isDouble()) {
            set.add(pos.relative(panelState.getVerticalFacing()));
        }

        if (panelState.isQuadruple()) {
            Direction direction = axis == Direction.Axis.X ? panelState.isLeft() ? Direction.EAST : Direction.WEST : panelState.isLeft() ? Direction.NORTH : Direction.SOUTH;
            set.add(pos.relative(panelState.getVerticalFacing()));
            set.add(pos.relative(panelState.getVerticalFacing()).relative(direction));
            set.add(pos.relative(direction));
        }

        return set;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (!state.getValue(STATE).isQuadruple()) {
            return state;
        }

        if (ModUtil.getRotationAmount(rotation) % 2 == 1) {
            state = state.cycle(AXIS);
        }

        Direction.Axis axis = state.getValue(AXIS);
        if (axis == Direction.Axis.Z && rotation == Rotation.CLOCKWISE_90
                || axis == Direction.Axis.X && rotation == Rotation.COUNTERCLOCKWISE_90
                || rotation == Rotation.CLOCKWISE_180) {
            return state.setValue(STATE, state.getValue(STATE).oppositeHorizontal());
        }

        return state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (!state.getValue(STATE).isQuadruple()) {
            return state;
        }

        Direction.Axis axis = state.getValue(AXIS);
        if (axis == Direction.Axis.X && mirror == Mirror.FRONT_BACK || axis == Direction.Axis.Z && mirror == Mirror.LEFT_RIGHT) {
            return state.setValue(STATE, state.getValue(STATE).oppositeHorizontal());
        }

        return state;
    }

    @Override
    public Vec3 helpPortal(Vec3 hitPos, Direction face, BlockState state, World world) {
        PanelState panelState = state.getValue(STATE);
        if (!panelState.isQuadruple() || face.getClockWise().getAxis() != state.getValue(AXIS)) {
            // Not front face of 2x2 panel
            return hitPos;
        }

        double yPos = panelState.isBottom() ? Math.ceil(hitPos.y) : Math.floor(hitPos.y);

        if (face.getAxis() == Direction.Axis.X) {
            return new Vec3(
                    hitPos.x,
                    yPos,
                    panelState.isLeft() ? Math.floor(hitPos.z) : Math.ceil(hitPos.z)
            );
        }

        return new Vec3(
                !panelState.isLeft() ? Math.floor(hitPos.x) : Math.ceil(hitPos.x),
                yPos,
                hitPos.z
        );
    }
}
