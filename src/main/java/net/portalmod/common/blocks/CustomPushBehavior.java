package net.portalmod.common.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

// Pretty much all copied over from the Carpet mod

/**
 * Opt-in Interface that allows for more control on a blocks interaction within the {@link net.minecraft.world.level.block.piston.PistonStructureResolver} via {@link net.portalmod.mixins.PistonBlockStructureHelperMixin}
 */
public interface CustomPushBehavior {

    /**
     * @return whether the neighboring block is pulled along if this block is moved by pistons
     */
    boolean isStickyToNeighbor(Level level, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction dir, Direction moveDir);
}
