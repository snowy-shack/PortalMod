package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class AntlineTimerBlock extends AbstractAntlineIndicator {
    public static final IntegerProperty TIMER = IntegerProperty.create("timer", 0, 9);

    public AntlineTimerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(ACTIVATED, false)
                .setValue(AntlineTimerBlock.TIMER, 0)
                .setValue(REVERSED, false)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public boolean isActive(BlockState blockState) {
        boolean active = blockState.getValue(TIMER) < 9;
        boolean reversed = blockState.getValue(REVERSED);
        return active != reversed;
    }

    @Override
    public void setActive(boolean active, World world, BlockPos pos) {
        // Don't allow unpowering when already not activatd, this fixes a loop
        if (world.getBlockState(pos).getValue(ACTIVATED) == active) return;

        // Save antline power
        if (active != world.getBlockState(pos).getValue(ACTIVATED)) {
            world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ACTIVATED, active));
        }

        // If activated, reset timer
        if (active) {
            BlockState state = world.getBlockState(pos);
            if (state.getValue(TIMER) != 8) {
                world.setBlockAndUpdate(pos, state.setValue(TIMER, 8));

                this.playActivationSound(true, world, pos);
            }
            return;
        }

        // Upon deactivation, begin countdown.
        scheduleTick(world, pos);
    }

    @Override
    public void tick(BlockState state, ServerWorld level, BlockPos pos, Random random) {
        count(level, pos, state);
    }

    public void count(World level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // Reset if powered by antline
        if (state.getValue(ACTIVATED)) {
            level.setBlock(pos, state.setValue(TIMER, 1), 2);
            return;
        }

        // Done counting
        int count = state.getValue(TIMER);
        if (count <= 0) {
            level.setBlock(pos, state.setValue(TIMER, 9), 2);

            this.playActivationSound(false, level, pos);

            return;
        }

        level.setBlock(pos, state.setValue(TIMER, count - 1), 2);

        //todo play timer sound?

        scheduleTick(level, pos);
    }

    public static void scheduleTick(World level, BlockPos pos) {
        level.getBlockTicks().scheduleTick(pos, BlockInit.ANTLINE_TIMER.get(), 12);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TIMER);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline_timer", list);
    }
}