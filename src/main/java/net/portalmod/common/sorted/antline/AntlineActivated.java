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
    void onAntlineActivation(boolean active, BlockState state, World world, BlockPos pos);

    default boolean ignoreAntlineActivationFromBlock(BlockState state) {
        return false;
    }

    /**
     * Looks around to check whether we should be powered or not.
     */
    default void updateAntlineActivation(BlockState state, World world, BlockPos pos) {
        Direction horsedOn = this.getHorsedOn(state);

        for (Direction direction : Direction.values()) {
            if (!this.antlineConnectsInDirection(direction, state)) continue;

            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            Block neighborBlock = neighborState.getBlock();

            // Powered by element
            if (neighborBlock instanceof AntlineActivator
                    && ((AntlineActivator) neighborBlock).antlineConnectsInDirection(direction.getOpposite(), neighborState)
                    && ((AntlineActivator) neighborBlock).getHorsedOn(neighborState) == horsedOn
                    && ((AntlineActivator) neighborBlock).isAntlineActive(neighborState)
                    && !ignoreAntlineActivationFromBlock(neighborState)
            ) {
                this.onAntlineActivation(true, state, world, pos);
                return;
            }

            // Powered by antline
            if (neighborBlock instanceof AntlineBlock && !ignoreAntlineActivationFromBlock(neighborState)) {
                // First update antline
//                world.neighborChanged(neighborPos, state.getBlock(), pos);

                AntlineTileEntity tileEntity = (AntlineTileEntity) world.getBlockEntity(neighborPos);
                if (tileEntity == null) continue;

                AntlineTileEntity.Side side = tileEntity.getSideMap().get(horsedOn);
                if (side.hasConnection(direction.getOpposite()) && side.isActive()) {
                    this.onAntlineActivation(true, state, world, pos);
                    return;
                }
            }
        }

        this.onAntlineActivation(false, state, world, pos);
    }
}
