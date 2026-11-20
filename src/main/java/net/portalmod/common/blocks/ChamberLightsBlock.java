package net.portalmod.common.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class ChamberLightsBlock extends DoubleBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");
    public static final BooleanProperty ACTIVE =  BooleanProperty.create("active");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ChamberLightsBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(ACTIVE, true)
                .setValue(POWERED, false)
                .setValue(ROTATED, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(AXIS, ACTIVE, POWERED, HALF, ROTATED);
    }

    @Override
    public Direction getUpperDirection(BlockState state) {
        return Direction.fromAxisAndDirection(state.getValue(AXIS), Direction.AxisDirection.POSITIVE);
    }

    @Override
    public boolean isSamePart(BlockState one, BlockState two) {
        return super.isSamePart(one, two)
                && one.getValue(AXIS) == two.getValue(AXIS)
                && one.getValue(ROTATED) == two.getValue(ROTATED);
    }

    @Override
    public boolean lookDirectionInfluencesLocation() {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos nPos, boolean moved) {
        Boolean wasPowered = state.getValue(POWERED);
        boolean isPowered = this.getAllPositions(state, pos).stream().anyMatch(world::hasNeighborSignal);
        if (wasPowered == isPowered) {
            return;
        }

        this.setBlockStateValue(POWERED, isPowered, state, world, pos);

        if (isPowered) {
            this.setBlockStateValue(ACTIVE, false, state, world, pos);
        } else {
            this.setBlockStateValue(ACTIVE, new Random().nextBoolean(), state, world, pos);
            this.blink(world.getBlockState(pos), world, pos);
        }
    }

    public void blink(BlockState state, Level world, BlockPos pos) {
        Random random = new Random();
        boolean oldActive = state.getValue(ACTIVE);

        this.setBlockStateValue(ACTIVE, !oldActive, state, world, pos);

        if (!oldActive) {
            this.playBlinkSound(world, pos);
        }

        // Cancel the sequence 50% of the time, but only when the state is what it should be
        if (random.nextDouble() < 0.5 && state.getValue(POWERED) == oldActive) {
            return;
        }

        // Gaussian distribution starting at 1 with standard deviation of 10 (to make the chance for a short blink higher)
        double ticks = Math.abs(random.nextGaussian()) * 5 + 1;
        world.getBlockTicks().scheduleTick(pos, this, (int) ticks);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
        this.blink(state, world, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        if (context.getPlayer() == null) return null;
        Direction.Axis axis = context.getPlayer().getDirection().getAxis() == Direction.Axis.X
                ? Direction.Axis.Z
                : Direction.Axis.X;

        boolean prefersHorizontal = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();

        // Check what placements are possible
        Optional<DoubleBlockHalf> verticalTopHalf = getPlacementHalf(context, Direction.UP);
        Optional<DoubleBlockHalf> horizontalTopHalf = getPlacementHalf(context, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));

        if (!verticalTopHalf.isPresent() && !horizontalTopHalf.isPresent()) {
            // Neither is possible
            return null;
        }

        boolean willBeHorizontal = prefersHorizontal && horizontalTopHalf.isPresent() || !verticalTopHalf.isPresent();

        BlockState blockstate = this.defaultBlockState()
                .setValue(HALF, willBeHorizontal ? horizontalTopHalf.get() : verticalTopHalf.get())
                .setValue(ACTIVE, new Random().nextBoolean());

        if (willBeHorizontal) {
            return blockstate.setValue(AXIS, axis)
                    .setValue(ROTATED, context.getNearestLookingDirection().getAxis() == Direction.Axis.Y);
        }

        return blockstate.setValue(ROTATED, context.getHorizontalDirection().getAxis() == Direction.Axis.X);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
        super.setPlacedBy(world, pos, blockState, entity, itemStack);

        this.blink(blockState, world, pos);
    }

    public void playBlinkSound(Level world, BlockPos pos) {
        world.playSound(
                null, pos, SoundInit.CHAMBER_LIGHTS_FLICKER.get(),
                SoundSource.BLOCKS, new Random().nextFloat() * 0.3f + 0.1f, ModUtil.randomSlightSoundPitch()
        );
    }

    // Copied from RespawnAnchorBlock
    @Environment(EnvType.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if (!state.getValue(POWERED) && random.nextInt(24) == 0) {
            level.playLocalSound( // just .playSound didn't work for me, no matter what I tried
                    (double) pos.getX() + 0.5D,
                    (double) pos.getY() + 0.5D,
                    (double) pos.getZ() + 0.5D,
                    SoundInit.CHAMBER_LIGHTS_AMBIENT.get(),
                    SoundSource.AMBIENT,
                    0.3f, 1, false
            );
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction.Axis axis = state.getValue(AXIS);

        if (axis == Direction.Axis.Y) {
            if (rotation == Rotation.NONE || rotation == Rotation.CLOCKWISE_180) {
                return state;
            }
            return state.cycle(ROTATED);
        }

        if (rotation == Rotation.NONE) {
            return state;
        }

        if (rotation == Rotation.CLOCKWISE_180) {
            return state.cycle(HALF);
        }

        BlockState rotated = state.setValue(AXIS, axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);

        if (rotation == Rotation.CLOCKWISE_90 && axis == Direction.Axis.Z || rotation == Rotation.COUNTERCLOCKWISE_90 && axis == Direction.Axis.X) {
            return rotated.cycle(HALF);
        }

        return rotated;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction.Axis axis = state.getValue(AXIS);
        if (axis == Direction.Axis.X && mirror == Mirror.FRONT_BACK || axis == Direction.Axis.Z && mirror == Mirror.LEFT_RIGHT) {
            return state.cycle(HALF);
        }
        return state;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockReader, List<Component> list, TooltipFlag flag) {
        ModUtil.addTooltip("chamber_lights", list);
    }
}