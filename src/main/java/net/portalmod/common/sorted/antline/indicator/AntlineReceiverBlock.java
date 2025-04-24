package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.AntlineActivator;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class AntlineReceiverBlock extends AntlineDevice implements AntlineActivator, TestElementActivator {

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public AntlineReceiverBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(REVERSED, false)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean b) {
        super.neighborChanged(state, world, pos, block, neighborPos, b);

        boolean powered = world.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            world.setBlockAndUpdate(pos, state.setValue(POWERED, powered));
        }
    }

    @Override
    public boolean isActive(BlockState state) {
        return state.getValue(POWERED) != state.getValue(REVERSED);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline_receiver", list);
    }
}
