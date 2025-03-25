package net.portalmod.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// Pretty much all copied over from the Carpet mod

/**
 * Opt-in Interface that allows for more control on a blocks interaction within the {@link net.minecraft.block.PistonBlockStructureHelper} via {@link net.portalmod.mixins.PistonBlockStructureHelperMixin}
 */
public interface CustomPushBehavior {

    /**
     * @return whether the neighboring block is pulled along if this block is moved by pistons
     */
    boolean isStickyToNeighbor(World level, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction dir, Direction moveDir);
}
