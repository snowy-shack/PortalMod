package net.portalmod.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.portalmod.core.init.BlockTagInit;

/**
 * Helper interface for working with portalable blocks. This interface does not make a block portalable, but it can provide handy information when the block is in the tag.
 */
public interface PortalableBlock {

    /**
     * Returns whether a face is not portalable, even when this block is in the tag {@code portalmod:portalable}.
     */
    boolean isPortalableOnFace(BlockState state, Direction face);

    static boolean isPortalable(BlockState state, Direction face) {
        //TODO gamerule for whether to whitelist or blacklist
        boolean useWhitelist = true;
        Block block = state.getBlock();

        boolean inTag = useWhitelist ? block.is(BlockTagInit.PORTALABLE) : !block.is(BlockTagInit.UNPORTALABLE);

        if (block instanceof PortalableBlock) {
            return inTag && ((PortalableBlock) block).isPortalableOnFace(state, face);
        }

        return inTag;
    }
}
