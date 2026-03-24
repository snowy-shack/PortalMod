package net.portalmod.common.sorted.button;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.common.blocks.DoubleBlock;
import net.portalmod.common.blocks.InteractKeyInteractable;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineActivator;
import net.portalmod.common.sorted.portalgun.CPortalGunInteractionPacket;
import net.portalmod.common.sorted.portalgun.PortalGunInteraction;
import net.portalmod.core.init.AttributeInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class StandingButtonBlock extends DoubleBlock implements AntlineActivator, InteractKeyInteractable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty PRESSED = BooleanProperty.create("pressed");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<ButtonMode> MODE = EnumProperty.create("mode", ButtonMode.class);
    public static final int BUTTON_DELAY = 20;

    public StandingButtonBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(PRESSED, false)
                .setValue(ACTIVE, false)
                .setValue(MODE, ButtonMode.NORMAL)
        );
        this.initAABBs();
    }

    private static final BiHashMap<Direction, DoubleBlockHalf, VoxelShapeGroup> SHAPE = new BiHashMap<>();

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
        for (Direction facing : Direction.values()) {
            Vec3 normal = new Vec3(facing.getNormal());

            Mat4 matrix = Mat4.identity();
            matrix.translate(new Vec3(.5));

            if (facing == Direction.DOWN) {
                matrix.scale(normal.mul(2).add(1));
            }

            if (facing.getAxis() != Direction.Axis.Y) {
                matrix.rotateDeg(normal.cross(Direction.UP.getNormal()).to3f(), -90);
            }

            matrix.translate(new Vec3(-.5));

            for(DoubleBlockHalf half : DoubleBlockHalf.values()) {
                VoxelShapeGroup shape = (half == DoubleBlockHalf.UPPER) ? UPPER : LOWER;
                SHAPE.put(facing, half, shape.clone().transform(matrix));
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext context) {
        return SHAPE.get(state.getValue(FACING), state.getValue(HALF)).getVariant(state.getValue(PRESSED) ? "on" : "off");
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, PRESSED, ACTIVE, MODE);
    }

    @Override
    public Direction getUpperDirection(BlockState state) {
        return state.getValue(FACING);
    }

    public boolean canPress(BlockState blockState) {
        return !blockState.getValue(PRESSED);
    }

    public void press(BlockState blockState, World world, BlockPos pos) {
        ButtonMode mode = blockState.getValue(MODE);
        Boolean wasActive = blockState.getValue(ACTIVE);

        this.setBlockStateValue(PRESSED, true, blockState, world, pos);
        world.getBlockTicks().scheduleTick(pos, this, BUTTON_DELAY);

        if (mode == ButtonMode.NORMAL || mode == ButtonMode.PERSISTENT && !wasActive) {
            this.setActivated(true, blockState, world, pos);
        }
        else if (mode == ButtonMode.TOGGLE) {
            this.setActivated(!wasActive, blockState, world, pos);
        }

        this.updateAllNeighbors(world, pos, blockState);
    }

    private void setActivated(boolean activated, BlockState blockState, World world, BlockPos pos) {
        this.setBlockStateValue(ACTIVE, activated, blockState, world, pos);
        world.playSound(null, pos, activated ? SoundInit.BUTTON_ACTIVATE.get() : SoundInit.BUTTON_DEACTIVATE.get(), SoundCategory.BLOCKS, 1, 1);
    }

    @Override
    public void tick(BlockState blockState, ServerWorld world, BlockPos pos, Random random) {
        if (blockState.getValue(PRESSED)) {
            this.setBlockStateValue(PRESSED, false, blockState, world, pos);
            world.updateNeighborsAt(pos, this);
            if (blockState.getValue(MODE) == ButtonMode.NORMAL) {
                this.setActivated(false, blockState, world, pos);
            }
            this.updateAllNeighbors(world, pos, world.getBlockState(pos));
        }
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (WrenchItem.usedWrench(player, hand)) {

            // Don't cycle when persistent/toggle and active
            boolean shouldCycle = blockState.getValue(MODE) == ButtonMode.NORMAL || !blockState.getValue(ACTIVE);

            if (shouldCycle) {
                ButtonMode newMode = cycleMode(blockState, world, pos);
                player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.button_mode." + newMode.getSerializedName()), true);
            }

            this.setBlockStateValue(ACTIVE, false, blockState, world, pos);

            WrenchItem.playUseSound(world, rayTraceResult.getLocation());

            this.updateAllNeighbors(world, pos, world.getBlockState(pos));
            return ActionResultType.sidedSuccess(world.isClientSide);
        }

        double rayLength = rayTraceResult.getLocation().subtract(player.getEyePosition(1)).length();
        double reach = player.getAttributeValue(AttributeInit.BUTTON_REACH.get());
        if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER && this.canPress(blockState)) {
            if (rayLength > reach) {
                player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.standing_button.too_far_away"), true);
                return ActionResultType.CONSUME;
            }

            this.press(blockState, world, pos);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }

        return ActionResultType.PASS;
    }

    @Override
    public boolean interactKeyInteract(PlayerEntity player, BlockRayTraceResult rayHit) {
        double buttonReach = player.getAttributeValue(AttributeInit.BUTTON_REACH.get());
        if (rayHit.getLocation().subtract(player.getEyePosition(1)).length() < buttonReach) {
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.PRESS_BUTTON).blockHit(rayHit).build());
            return true;
        }

        return false;
    }

    public ButtonMode cycleMode(BlockState blockState, World world, BlockPos pos) {
        ButtonMode currentMode = blockState.getValue(MODE);
        ButtonMode newMode = currentMode.cycle();
        this.setBlockStateValue(MODE, newMode, blockState, world, pos);
        return newMode;
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos pos2, boolean b) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(FACING);

        // Reset when powered from below
        if (state.getValue(ACTIVE)
                && state.getValue(HALF) == DoubleBlockHalf.LOWER
                && level.hasSignal(pos.relative(facing.getOpposite()), facing)
        ) {
            this.setActivated(false, state, level, pos);
            this.updateAllNeighbors(level, pos, level.getBlockState(pos));
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction facing = context.getClickedFace();

        BlockPos upperPos = context.getClickedPos().relative(facing);
        if (ModUtil.canPlaceAt(context, upperPos)) {
            return this.defaultBlockState().setValue(FACING, facing);
        }

        return null;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("standing_button", list);
    }

    @Override
    public boolean isAntlineActive(BlockState state) {
        return state.getValue(ACTIVE);
    }

    @Override
    public Direction getHorsedOn(BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return null;
        }

        return state.getValue(FACING).getOpposite();
    }

    @Override
    public boolean antlineConnectsInDirection(Direction direction, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && direction.getAxis() != state.getValue(FACING).getAxis();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
