package net.portalmod.common.sorted.antline.indicator;

import net.portalmod.common.sorted.antline.AntlineActivated;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

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
    public void onAntlineActivation(boolean active, BlockState state, Level world, BlockPos pos) {
        BlockState current = world.getBlockState(pos);
        if (current.getValue(ACTIVATED) == active) return;

        world.setBlockAndUpdate(pos, current.setValue(ACTIVATED, active));

        this.playActivationSound(active != current.getValue(REVERSED), world, pos);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("antline_indicator", list);
    }
}