package net.portalmod.common.sorted.panel;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
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

        // Block is not this one or shift is not down
        if (!clickedBlock.getBlock().is(this) || player == null || !player.isShiftKeyDown()) {
            return this.defaultBlockState();
        }

        PanelState clickedPanelState = clickedBlock.getValue(STATE);
        boolean clickedOnSide = clickedFace.getAxis() != Direction.Axis.Y;

        // Top or bottom
        if (clickedPanelState.isSingle() && !clickedOnSide) {
            return this.defaultBlockState().setValue(STATE, PanelState.doubleState(clickedFace == Direction.DOWN));
        }

        if (clickedOnSide && areTwoBlocksInInventory(player, this)) {
            // Wall 2x2 panel
            if (clickedPanelState.isDouble() && world.getBlockState(context.getClickedPos().relative(clickedPanelState.getVerticalFacing())).canBeReplaced(context)) {
                removeBlockFromInventory(player, this);
                return this.defaultBlockState()
                        .setValue(STATE, wallStateFromDirection(clickedFace, clickedPanelState))
                        .setValue(AXIS, clickedFace.getAxis());
            }

            // Floor 2x2 panel
            if (clickedPanelState.isSingle()) {
                Direction left = clickedFace.getClockWise();
                Direction right = clickedFace.getCounterClockWise();

                BlockState leftBlockState = world.getBlockState(clickedOnPos.relative(left));
                BlockState rightBlockState = world.getBlockState(clickedOnPos.relative(right));
                boolean canPlaceOnRight = leftBlockState.is(this) && leftBlockState.getValue(STATE).isSingle()
                        && world.getBlockState(context.getClickedPos().relative(left)).canBeReplaced(context);
                boolean canPlaceOnLeft = rightBlockState.is(this) && rightBlockState.getValue(STATE).isSingle()
                        && world.getBlockState(context.getClickedPos().relative(right)).canBeReplaced(context);

                if (!canPlaceOnRight && !canPlaceOnLeft) return this.defaultBlockState();

                boolean prefersLeft = MultiBlock.clickedOnPositiveHalf(context, right);
                boolean willPlaceLeft = prefersLeft && canPlaceOnLeft || !canPlaceOnRight;

                Direction.Axis axis = clickedFace.getAxis();
                boolean getsBottomState = axis == Direction.Axis.Z ? clickedFace == Direction.SOUTH : willPlaceLeft != (clickedFace == Direction.WEST);
                boolean getsLeftState = axis == Direction.Axis.X ? clickedFace == Direction.WEST : willPlaceLeft != (clickedFace == Direction.NORTH);

                removeBlockFromInventory(player, this);
                return this.defaultBlockState()
                        .setValue(AXIS, axis)
                        .setValue(STATE, PanelState.floorState(getsBottomState, getsLeftState));
            }
        }

        return this.defaultBlockState();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
        PanelState panelState = blockState.getValue(STATE);

        // Place the other half
        if (panelState.isDouble()) {
            world.setBlockAndUpdate(pos.relative(panelState.getVerticalFacing()), blockState.setValue(STATE, panelState.mirrorUpDown()));
            return;
        }

        Direction.Axis axis = blockState.getValue(AXIS);

        // Place the other 3 wall blocks
        if (panelState.isWall()) {
            Direction direction = axis == Direction.Axis.X ? panelState.isLeft() ? Direction.EAST : Direction.WEST : panelState.isLeft() ? Direction.NORTH : Direction.SOUTH;

            // New block
            setBlock(world, pos.relative(panelState.getVerticalFacing()), wallStateFromDirection(direction.getOpposite(), panelState.mirrorUpDown()), axis);

            // Clicked block
            setBlock(world, pos.relative(direction), wallStateFromDirection(direction, panelState), axis);

            // Diagonal block
            setBlock(world, pos.relative(direction).relative(panelState.getVerticalFacing()), wallStateFromDirection(direction, panelState.mirrorUpDown()), axis);
        }

        // Place the other 3 floor blocks
        if (panelState.isFloor()) {
            Direction leftRightDir = getLeftRightFloorDir(panelState);
            Direction upDownDir = getUpDownFloorDir(panelState);

            // New block
            setBlock(world, pos.relative(leftRightDir), panelState.mirrorLeftRight(), axis);

            // Clicked block
            setBlock(world, pos.relative(upDownDir), panelState.mirrorUpDown(), axis);

            // Diagonal block
            setBlock(world, pos.relative(leftRightDir).relative(upDownDir), panelState.mirrorUpDown().mirrorLeftRight(), axis);
        }
    }

    public static Direction getUpDownFloorDir(PanelState panelState) {
        return panelState.isBottom() ? Direction.NORTH : Direction.SOUTH;
    }

    public static Direction getLeftRightFloorDir(PanelState panelState) {
        return panelState.isLeft() ? Direction.EAST : Direction.WEST;
    }

    public void setBlock(World world, BlockPos pos, PanelState state, Direction.Axis axis) {
        world.setBlockAndUpdate(pos, this.defaultBlockState().setValue(STATE, state).setValue(AXIS, axis));
    }

    public static PanelState wallStateFromDirection(Direction direction, PanelState placedState) {
        if (direction.getAxis() == Direction.Axis.Y) return PanelState.SINGLE;

        return PanelState.wallState(placedState.isBottom(), direction == Direction.WEST || direction == Direction.SOUTH);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        PanelState panelState = state.getValue(STATE);
        Direction.Axis axis = state.getValue(AXIS);

        // Is single or connection is not in that direction
        if (panelState.isSingle() || !getConnectedPositions(state, pos).contains(neighborPos)) {
            return state;
        }

        if (panelState.isFloor() && (!neighborState.is(this)
                || axis != neighborState.getValue(AXIS)
                || getLeftRightFloorDir(panelState) == direction && neighborState.getValue(STATE) != panelState.mirrorLeftRight()
                || getUpDownFloorDir(panelState) == direction && neighborState.getValue(STATE) != panelState.mirrorUpDown())) {
            return this.defaultBlockState();
        }

        // Incorrect sideways connection
        if (panelState.isWall() && direction.getAxis() != Direction.Axis.Y
                && (!neighborState.is(this) || axis != neighborState.getValue(AXIS) || panelState.mirrorLeftRight() != neighborState.getValue(STATE))) {
            return state.setValue(STATE, PanelState.doubleState(panelState.isBottom()));
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
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack itemStack = player.inventory.getItem(i);
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
        PlayerInventory inventory = player.inventory;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            Item item = itemStack.getItem();
            if (item instanceof BlockItem && ((BlockItem) item).getBlock().is(block)
                    // May not remove one from the selected hand if its count is 1, because it will already get removed by default
                    && !(i == inventory.selected && itemStack.getCount() == 1)) {
                itemStack.shrink(1);
                return;
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

        if (panelState.isWall()) {
            Direction direction = axis == Direction.Axis.X ? panelState.isLeft() ? Direction.EAST : Direction.WEST : panelState.isLeft() ? Direction.NORTH : Direction.SOUTH;
            set.add(pos.relative(panelState.getVerticalFacing()));
            set.add(pos.relative(panelState.getVerticalFacing()).relative(direction));
            set.add(pos.relative(direction));
        }

        if (panelState.isFloor()) {
            Direction leftRightDir = getLeftRightFloorDir(panelState);
            Direction upDownDir = getUpDownFloorDir(panelState);

            set.add(pos.relative(leftRightDir));
            set.add(pos.relative(upDownDir));
            set.add(pos.relative(upDownDir).relative(leftRightDir));
        }

        return set;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        PanelState panelState = state.getValue(STATE);

        if (panelState.isFloor()) {
            if (ModUtil.getRotationAmount(rotation) % 2 == 1) {
                state = state.cycle(AXIS);
            }

            for (int i = 0; i < ModUtil.getRotationAmount(rotation); i++) {
                state = state.setValue(STATE, state.getValue(STATE).rotate());
            }

            return state;
        }

        if (panelState.isWall()) {
            if (ModUtil.getRotationAmount(rotation) % 2 == 1) {
                state = state.cycle(AXIS);
            }

            Direction.Axis axis = state.getValue(AXIS);
            if (axis == Direction.Axis.Z && rotation == Rotation.CLOCKWISE_90
                    || axis == Direction.Axis.X && rotation == Rotation.COUNTERCLOCKWISE_90
                    || rotation == Rotation.CLOCKWISE_180) {
                return state.setValue(STATE, panelState.mirrorLeftRight());
            }
        }

        return state;

    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return state;
        }

        PanelState panelState = state.getValue(STATE);
        if (panelState.isFloor()) {
            return state.setValue(STATE, mirror == Mirror.FRONT_BACK ? panelState.mirrorLeftRight() : panelState.mirrorUpDown());
        }

        if (panelState.isWall()) {
            Direction.Axis axis = state.getValue(AXIS);
            if (axis == Direction.Axis.X && mirror == Mirror.FRONT_BACK || axis == Direction.Axis.Z && mirror == Mirror.LEFT_RIGHT) {
                return state.setValue(STATE, panelState.mirrorLeftRight());
            }
        }

        return state;

    }

    @Override
    public boolean containsBlock(BlockState state, BlockPos panelPos, BlockPos pos, World world) {
        return panelPos.equals(pos) || this.getConnectedPositions(state, panelPos).contains(pos);
    }

    @Override
    public boolean willHelpPortal(Direction face, Direction horizontalDirection, BlockState state, World world) {
        // Only front face of 2x2 panel
        PanelState panelState = state.getValue(STATE);

        if(panelState.isFloor()) {
            return face.getAxis().isVertical();
        } else if(panelState.isWall()) {
            return !face.getAxis().isVertical() && face.getClockWise().getAxis() == state.getValue(AXIS);
        }

        return false;
    }

    @Override
    public Pair<Vec3, Direction> helpPortal(Vec3 hitPos, Direction face, Direction horizontalDirection, Direction[] lookingDirections, BlockState state, World world) {
        if(!this.willHelpPortal(face, horizontalDirection, state, world))
            return new Pair<>(hitPos, horizontalDirection);

        PanelState panelState = state.getValue(STATE);
        double yPos = panelState.isBottom() ? Math.ceil(hitPos.y) : Math.floor(hitPos.y);

        Optional<Direction> upDirection = Arrays.stream(lookingDirections)
                .filter(direction -> direction.getAxis() == state.getValue(AXIS))
                .findFirst();

        if(!upDirection.isPresent())
            return new Pair<>(hitPos, lookingDirections[0]);

        if(face.getAxis().isVertical()) {
            if(state.getValue(AXIS) == Direction.Axis.X) {
                return new Pair<>(new Vec3(
                        !panelState.isLeft() ? Math.floor(hitPos.x) : Math.ceil(hitPos.x),
                        yPos,
                        panelState.isBottom() ? Math.floor(hitPos.z) : Math.ceil(hitPos.z)
                ), upDirection.get());
            }

            return new Pair<>(new Vec3(
                    !panelState.isLeft() ? Math.floor(hitPos.x) : Math.ceil(hitPos.x),
                    yPos,
                    panelState.isBottom() ? Math.floor(hitPos.z) : Math.ceil(hitPos.z)
            ), upDirection.get());

        } else {
            if(face.getAxis() == Direction.Axis.X) {
                return new Pair<>(new Vec3(
                        panelState.isBottom() ? Math.floor(hitPos.x) : Math.ceil(hitPos.x),
                        yPos,
                        panelState.isLeft() ? Math.floor(hitPos.z) : Math.ceil(hitPos.z)
                ), horizontalDirection);
            }

            return new Pair<>(new Vec3(
                    !panelState.isLeft() ? Math.floor(hitPos.x) : Math.ceil(hitPos.x),
                    yPos,
                    !panelState.isBottom() ? Math.floor(hitPos.z) : Math.ceil(hitPos.z)
            ), horizontalDirection);
        }
    }
}
