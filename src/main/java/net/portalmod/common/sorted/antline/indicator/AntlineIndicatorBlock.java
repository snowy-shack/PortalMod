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
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

public class AntlineIndicatorBlock extends AbstractAntlineIndicator {

    public AntlineIndicatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(ACTIVATED, false)
                .setValue(REVERSED, false)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public boolean isActive(BlockState blockState) {
        boolean active = blockState.getValue(ACTIVATED);
        boolean reversed = blockState.getValue(REVERSED);
        return reversed != active;
    }

    @Override
    public void setActive(boolean active, World world, BlockPos pos) {
        if (world.getBlockState(pos).getValue(ACTIVATED) == active) return;

        world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ACTIVATED, active));

        this.playActivationSound(active, world, pos);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline_indicator", list);
    }
}