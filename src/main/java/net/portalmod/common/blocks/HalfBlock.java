package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public class HalfBlock extends Block {

    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    public HalfBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis() == Direction.Axis.Y) {
            return this.defaultBlockState().setValue(HALF, clickedFace.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Half.BOTTOM : Half.TOP);
        } else {
            boolean placedOnLowerSide = context.getClickLocation().y - context.getClickedPos().getY() < 0.5;
            return this.defaultBlockState().setValue(HALF, placedOnLowerSide ? Half.BOTTOM : Half.TOP);
        }
    }
}
