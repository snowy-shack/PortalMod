package net.portalmod.common.sorted.antline.indicator;

import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineActivated;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class AntlineTimerBlock extends AntlineOutput implements AntlineActivated, TestElementActivator {
    public static final int MAX_DURATION = 20;

    public static final IntegerProperty TIMER = IntegerProperty.create("timer", 0, 9);
    public static final IntegerProperty DURATION = IntegerProperty.create("duration", 1, MAX_DURATION);

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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
    public void onAntlineActivation(boolean active, BlockState state, Level world, BlockPos pos) {
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
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {

        // Handle cycling duration
        if (player.isShiftKeyDown() && WrenchItem.usedWrench(player, hand)) {
            int newDuration = incrementDuration(blockState);
            world.setBlockAndUpdate(pos, blockState.setValue(DURATION, newDuration));
            player.displayClientMessage(Component.translatable("actionbar.portalmod.antline_timer.duration", newDuration), true);
            WrenchItem.playUseSound(world, result.getLocation());

            return InteractionResult.SUCCESS;
        }

        return super.use(blockState, world, pos, player, hand, result);
    }

    public int incrementDuration(BlockState blockState) {
        int currentDuration = blockState.getValue(DURATION);
        int increment = currentDuration >= 10 ? 2 : 1;
        return Math.max(1, (currentDuration + increment) % (MAX_DURATION + 1));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (level.isClientSide) return;

        count(level, pos, state);
    }

    public void count(Level level, BlockPos pos, BlockState state) {

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

        if (this.shouldPlaySound(state.getValue(DURATION), count)) {
            level.playSound(null, pos, SoundInit.ANTLINE_TIMER_TICK.get(), SoundSource.BLOCKS, 3, 1);
        }

        scheduleTick(state, level, pos);
    }

    public boolean shouldPlaySound(int duration, int count) {
        if (duration >= 5) return true;

        // Don't tick too fast
        switch (duration) {
            case 1: return count % 5 == 2;
            case 2: return count % 3 == 0;
            case 3:
            case 4: return count % 2 == 0;
        }
        return false;
    }

    public static void scheduleTick(BlockState state, Level level, BlockPos pos) {
        final float stepFactor = 20f / 9f;
        int currentSteps = 9 - state.getValue(TIMER);
        int totalSteps = state.getValue(DURATION);
        double timePassed = Math.round(currentSteps * totalSteps * stepFactor);
        double targetTime = Math.round((currentSteps+1) * totalSteps * stepFactor);
        int ticks = (int) (targetTime - timePassed);
        level.getBlockTicks().scheduleTick(pos, BlockInit.ANTLINE_TIMER.get(), ticks);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("antline_timer", list);
    }
}
