package net.portalmod.common.sorted.antline;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface for things that are powered by antlines, such as indicators.
 */
public interface AntlineActivated extends AntlineConnector {
    void setActive(boolean active, BlockState state, World world, BlockPos pos);

    default boolean ignoreActivationFromBlock(BlockState state) {
        return false;
    }

    /**
     * Looks around to check whether we should be powered or not.
     */
    default void updatePower(BlockState state, World world, BlockPos pos) {
        Direction horsedOn = this.getHorsedOn(state);

        for (Direction direction : Direction.values()) {
            if (!this.connectsInDirection(direction, state)) continue;

            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            Block neighborBlock = neighborState.getBlock();

            // Powered by element
            if (neighborBlock instanceof AntlineActivator
                    && ((AntlineActivator) neighborBlock).connectsInDirection(direction.getOpposite(), neighborState)
                    && ((AntlineActivator) neighborBlock).getHorsedOn(neighborState) == horsedOn
                    && ((AntlineActivator) neighborBlock).isActive(neighborState)
                    && !ignoreActivationFromBlock(neighborState)
            ) {
                this.setActive(true, state, world, pos);
                return;
            }

            // Powered by antline
            if (neighborBlock instanceof AntlineBlock && !ignoreActivationFromBlock(neighborState)) {
                // First update antline
//                world.neighborChanged(neighborPos, state.getBlock(), pos);

                AntlineTileEntity tileEntity = (AntlineTileEntity) world.getBlockEntity(neighborPos);
                if (tileEntity == null) continue;

                AntlineTileEntity.Side side = tileEntity.getSideMap().get(horsedOn);
                if (side.hasConnection(direction.getOpposite()) && side.isActive()) {
                    this.setActive(true, state, world, pos);
                    return;
                }
            }
        }

        this.setActive(false, state, world, pos);
    }
}
