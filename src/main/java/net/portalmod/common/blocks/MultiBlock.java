package net.portalmod.common.blocks;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public abstract class MultiBlock extends Block {

    private final StatePropertiesPredicate predicate = mainBlockPredicate().build();

    public MultiBlock(Properties properties) {
        super(properties);
    }

    /**
     * @return the main position of the multiblock (usually top-left), given a particular corner.
     */
    public abstract BlockPos getMainPosition(BlockState blockState, BlockPos pos);

    /**
     * @return all block positions of the multiblock, given the main corner.
     */
    public abstract List<BlockPos> getConnectedPositions(BlockState blockState, BlockPos mainPos);

    /**
     * Should call {@link net.minecraft.world.World#setBlockAndUpdate(net.minecraft.util.math.BlockPos, net.minecraft.block.BlockState)} for every block except for the main one.
     */
    public abstract void placeConnectedBlocks(World world, BlockState blockState, BlockPos pos);

    /**
     * @return a predicate for determining whether a blockstate is the main block.
     */
    public abstract StatePropertiesPredicate.Builder mainBlockPredicate();

    public boolean isMainBlock(BlockState blockState) {
        return predicate.matches(blockState);
    }

    public List<BlockPos> getAllPositions(BlockState blockState, BlockPos pos) {
        BlockPos mainPos = this.getMainPosition(blockState, pos);
        List<BlockPos> connectedPositions = this.getConnectedPositions(blockState, mainPos);
        connectedPositions.add(mainPos);
        return connectedPositions;
    }

    public <T extends Comparable<T>> void setBlockStateValue(Property<T> property, T value, BlockState blockState, World world, BlockPos pos) {
        for (BlockPos multiBlockPos : this.getAllPositions(blockState, pos)) {
            BlockState multiBlockState = world.getBlockState(multiBlockPos);
            if (multiBlockState.getBlock().is(this)) {
                world.setBlock(multiBlockPos, multiBlockState.setValue(property, value), 2);
            }
        }
    }

    public void updateAllNeighbors(World world, BlockPos pos, BlockState blockState) {
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

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState updateBlockState, IWorld world, BlockPos pos, BlockPos updatePos) {
        for (BlockPos connectedPos : this.getAllPositions(blockState, pos)) {
            if (!connectedPos.equals(pos) && !world.getBlockState(connectedPos).is(this)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return blockState;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity entity, ItemStack itemStack) {
        if (!world.isClientSide) {
            this.placeConnectedBlocks(world, blockState, pos);
        }
    }

    @Override
    public void playerWillDestroy(World world, BlockPos pos, BlockState blockState, PlayerEntity player) {
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
    public void playerDestroy(World world, PlayerEntity player, BlockPos pos, BlockState blockState, @Nullable TileEntity tileEntity, ItemStack itemStack) {
        super.playerDestroy(world, player, pos, Blocks.AIR.defaultBlockState(), tileEntity, itemStack);
    }

    public void preventCreativeDropFromMainPart(World world, BlockPos pos, BlockState blockState, PlayerEntity player) {
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
