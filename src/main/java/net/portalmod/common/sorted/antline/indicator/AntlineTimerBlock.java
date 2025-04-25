package net.portalmod.common.sorted.antline.indicator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineActivated;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class AntlineTimerBlock extends AntlineOutput implements AntlineActivated, TestElementActivator {
    public static final int MAX_DURATION = 20;

    public static final IntegerProperty TIMER = IntegerProperty.create("timer", 0, 9);
    public static final IntegerProperty DURATION = IntegerProperty.create("duration", 1, MAX_DURATION + 1);

    public AntlineTimerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVATED, false)
                .setValue(REVERSED, false)
                .setValue(TIMER, 9)
                .setValue(DURATION, 5)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TIMER, DURATION);
    }

    @Override
    public boolean isActive(BlockState state) {
        boolean active = state.getValue(TIMER) < 9;
        boolean reversed = state.getValue(REVERSED);
        return active != reversed;
    }

    @Override
    public void setActive(boolean active, BlockState state, World world, BlockPos pos) {
        if (state.getValue(ACTIVATED) == active) return;

        // Save antline power
        if (active != state.getValue(ACTIVATED)) {
            world.setBlockAndUpdate(pos, state.setValue(ACTIVATED, active));
        }

        // If activated, reset timer
        if (active) {
            if (state.getValue(TIMER) != 8) {
                world.setBlockAndUpdate(pos, state.setValue(TIMER, 8));

                this.playActivationSound(!state.getValue(REVERSED), world, pos);
            }
            return;
        }

        // Upon deactivation, begin countdown.
        scheduleTick(state, world, pos);
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {

        // Handle cycling duration
        if (player.isShiftKeyDown() && WrenchItem.usedWrench(player, hand)) {
            int newDuration = incrementDuration(blockState);
            world.setBlockAndUpdate(pos, blockState.setValue(DURATION, newDuration));
            player.displayClientMessage(new TranslationTextComponent("actionbar.portalmod.antline_timer.duration", newDuration), true);
            WrenchItem.playUseSound(world, result.getLocation());

            return ActionResultType.SUCCESS;
        }

        return super.use(blockState, world, pos, player, hand, result);
    }

    public int incrementDuration(BlockState blockState) {
        int currentDuration = blockState.getValue(DURATION);
        int increment = currentDuration >= 10 ? 2 : 1;
        return Math.max(1, (currentDuration + increment) % (MAX_DURATION + 1));
    }

    @Override
    public void tick(BlockState state, ServerWorld level, BlockPos pos, Random random) {
        if (level.isClientSide) return;

        count(level, pos, state);
    }

    public void count(World level, BlockPos pos, BlockState state) {

        // Reset if powered by antline
        if (state.getValue(ACTIVATED)) {
            level.setBlock(pos, state.setValue(TIMER, 8), 2);
            return;
        }

        // Done counting
        int count = state.getValue(TIMER);
        if (count <= 0) {
            level.setBlock(pos, state.setValue(TIMER, 9), 2);

            this.playActivationSound(state.getValue(REVERSED), level, pos);

            return;
        }

        level.setBlock(pos, state.setValue(TIMER, count - 1), 2);

        //todo play timer sound?

        scheduleTick(state, level, pos);
    }

    public static void scheduleTick(BlockState state, World level, BlockPos pos) {
        int ticks = Math.round(state.getValue(DURATION) * 20f / 9f);
        level.getBlockTicks().scheduleTick(pos, BlockInit.ANTLINE_TIMER.get(), ticks);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable IBlockReader blockReader, List<ITextComponent> list, ITooltipFlag flag) {
        ModUtil.addTooltip("antline_timer", list);
    }
}
