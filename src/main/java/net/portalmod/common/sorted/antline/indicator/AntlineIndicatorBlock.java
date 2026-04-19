package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.AntlineActivated;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class AntlineIndicatorBlock extends AntlineOutput implements AntlineActivated, TestElementActivator {

    public AntlineIndicatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVATED, false)
                .setValue(REVERSED, false)
        );
    }

    @Override
    public boolean isActive(BlockState state) {
        boolean active = state.getValue(ACTIVATED);
        boolean reversed = state.getValue(REVERSED);
        return reversed != active;
    }

    @Override
    public void onAntlineActivation(boolean active, BlockState state, World world, BlockPos pos) {
        BlockState current = world.getBlockState(pos);
        if (current.getValue(ACTIVATED) == active) return;

        world.setBlockAndUpdate(pos, current.setValue(ACTIVATED, active));

        this.playActivationSound(active != current.getValue(REVERSED), world, pos);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline_indicator", list);
    }
}