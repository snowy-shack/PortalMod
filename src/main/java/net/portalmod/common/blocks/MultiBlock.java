package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
     * @return whether the blockstate is the main block.
     */
    public abstract boolean isMainBlock(BlockState blockState);

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
}
