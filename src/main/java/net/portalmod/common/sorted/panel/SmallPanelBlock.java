package net.portalmod.common.sorted.panel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Objects;

public class SmallPanelBlock extends Block {

    public static final EnumProperty<PanelState> STATE = EnumProperty.create("state", PanelState.class, PanelState.SINGLE, PanelState.TOP, PanelState.BOTTOM);

    public SmallPanelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, PanelState.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE);
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

        return this.defaultBlockState();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
        PanelState panelState = blockState.getValue(STATE);

        // Place the other half
        if (panelState.isDouble()) {
            world.setBlockAndUpdate(pos.relative(panelState.getVerticalFacing()), blockState.setValue(STATE, panelState.oppositeVertical()));
        }
    }

    public static boolean isSide(Direction direction) {
        return direction != Direction.UP && direction != Direction.DOWN;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction p_196271_2_, BlockState p_196271_3_, IWorld world, BlockPos blockPos, BlockPos p_196271_6_) {
        for (BlockPos connectedPos : getConnectedPositions(blockState, blockPos)) {
            if (world.getBlockState(connectedPos).is(this)) {
                continue;
            }
            PanelState panelState = blockState.getValue(STATE);
            if (panelState.isQuadruple() && !(connectedPos.getX() == blockPos.getX() && connectedPos.getZ() == blockPos.getZ())) {
                return defaultBlockState().setValue(STATE, panelState.isBottom() ? PanelState.BOTTOM : PanelState.TOP);
            }
            return defaultBlockState();
        }
        return blockState;
    }

    public BlockPos[] getConnectedPositions(BlockState state, BlockPos pos) {
        PanelState panelState = state.getValue(STATE);
        if (panelState.isDouble()) {
            return new BlockPos[]{pos.relative(panelState.getVerticalFacing())};
        }
        return new BlockPos[]{pos};
    }
}
