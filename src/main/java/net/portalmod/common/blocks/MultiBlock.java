package net.portalmod.common.blocks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public abstract class MultiBlock extends Block {

    private final Map<Property<?>, Comparable<?>> mainProperties = new HashMap<>();

    public MultiBlock(Properties blockProperties) {
        super(blockProperties);

        this.addMainBlockProperties(this.mainProperties);
    }

    /**
     * Defines the main position of the multiblock (usually top-left), given a particular block.
     */
    public abstract BlockPos getMainPosition(BlockState blockState, BlockPos pos);

    /**
     * Provides all block positions of the multiblock, given the main block.
     */
    public abstract List<BlockPos> getConnectedPositions(BlockState mainState, BlockPos mainPos);

    /**
     * Provides a map of {@link BlockPos} to {@link BlockState} of each additional block that would need to be placed to complete the structure.
     * Keep in mind that the provided block may NOT be the main block.
     */
    public abstract Map<BlockPos, BlockState> getOtherParts(BlockState blockState, BlockPos pos);

    /**
     * @return whether two blockstates are the same exact part of the structure, including rotations, but excluding any extra states like 'active'.
     */
    public abstract boolean isSamePart(BlockState one, BlockState two);

    /**
     * Adds the needed properties and values to a map for determining whether a blockstate is the main block.
     */
    public abstract void addMainBlockProperties(Map<Property<?>, Comparable<?>> map);

    /**
     * Defines whether the world positions of extra placed blocks depend on where the player is looking.
     * If this is the case, placement will not be instantaneous to prevent a desync.
     */
    public boolean lookDirectionInfluencesLocation() {
        return false;
    }


    // ----- Utility methods -----

    public boolean isMainBlock(BlockState blockState) {
        return this.mainProperties.entrySet().stream()
                .allMatch(entry ->
                        blockState.getValue(entry.getKey()).equals(entry.getValue()));
    }

    public List<BlockPos> getAllPositions(BlockState blockState, BlockPos pos) {
        BlockPos mainPos = this.getMainPosition(blockState, pos);
        List<BlockPos> connectedPositions = this.getConnectedPositions(blockState, mainPos);
        connectedPositions.add(mainPos);
        return connectedPositions;
    }

    public <T extends Comparable<T>> void setBlockStateValue(Property<T> property, T value, BlockState blockState, Level world, BlockPos pos) {
        for (BlockPos multiBlockPos : this.getAllPositions(blockState, pos)) {
            BlockState multiBlockState = world.getBlockState(multiBlockPos);
            if (multiBlockState.getBlock().is(this)) {
                world.setBlock(multiBlockPos, multiBlockState.setValue(property, value), 2);
            }
        }
    }

    public void updateAllNeighbors(Level world, BlockPos pos, BlockState blockState) {
        if (world.isClientSide) return;

        List<BlockPos> positions = this.getAllPositions(blockState, pos);
        for (BlockPos connected : positions) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = connected.relative(direction);
                if (!positions.contains(neighbor)) {
                    world.blockUpdated(neighbor, this);
                }
            }
        }
    }

    public static boolean clickedOnPositiveHalf(BlockPlaceContext context, Direction direction) {
        boolean isPositiveDirection = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        return clickedOnPositiveHalf(context, direction.getAxis()) == isPositiveDirection;
    }

    public static boolean clickedOnPositiveHalf(BlockPlaceContext context, Direction.Axis axis) {
        BlockPos pos = context.getClickedPos();

        if (axis == Direction.Axis.X) return context.getClickLocation().x - pos.getX() > 0.5;
        if (axis == Direction.Axis.Y) return context.getClickLocation().y - pos.getY() > 0.5;
        return context.getClickLocation().z - pos.getZ() > 0.5;
    }


    // ----- Overrides -----

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (this.getAllPositions(state, pos).contains(neighborPos)) {
            if (neighborState.is(this) && this.isSamePart(neighborState, this.getOtherParts(state, pos).get(neighborPos))) {
                // In the mojang code, this is where they sync their blockstate with the other part,
                // but we don't have to do that because they are already in sync due to using
                // setBlockStateValue() instead of only setting the state of one part
                return state;
            }
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
        if (world.isClientSide && this.lookDirectionInfluencesLocation()) return;

        this.getOtherParts(blockState, pos)
                .forEach(world::setBlockAndUpdate);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState blockState, Player player) {
        if (!world.isClientSide) {
            if (player.isCreative()) {
                preventCreativeDropFromMainPart(world, pos, blockState, player);
            } else {
                dropResources(blockState, world, pos, null, player, player.getMainHandItem());
            }
        }

        super.playerWillDestroy(world, pos, blockState, player);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState blockState, @Nullable BlockEntity tileEntity, ItemStack itemStack) {
        super.playerDestroy(world, player, pos, Blocks.AIR.defaultBlockState(), tileEntity, itemStack);
    }

    public void preventCreativeDropFromMainPart(Level world, BlockPos pos, BlockState blockState, Player player) {
        BlockPos mainPos = this.getMainPosition(blockState, pos);
        if (!pos.equals(mainPos)) {
            BlockState mainBlockState = world.getBlockState(mainPos);
            if (mainBlockState.getBlock().is(this)) {
                world.setBlock(mainPos, Blocks.AIR.defaultBlockState(), 35);
                world.levelEvent(player, 2001, mainPos, Block.getId(mainBlockState));
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.BLOCK;
    }
}
