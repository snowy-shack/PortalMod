package net.portalmod.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.portalmod.core.init.BlockTagInit;
import net.portalmod.core.init.GameRuleInit;

/**
 * Helper interface for working with portalable blocks. This interface does not make a block portalable, but it can provide handy information when the block is in the tag.
 */
public interface PortalableBlock {

    /**
     * Returns whether a face is portalable, when this block is in the tag {@code portalmod:portalable}.
     */
    boolean isPortalableOnFace(BlockState state, Direction face);

    static boolean isPortalable(BlockState state, Direction face, World world) {
        if (world.getGameRules().getBoolean(GameRuleInit.USE_PORTALABLE_BLACKLIST)) {
            return !state.is(BlockTagInit.UNPORTALABLE);
        }

        boolean inTag = state.is(BlockTagInit.PORTALABLE);

        if (state.getBlock() instanceof PortalableBlock) {
            return inTag && ((PortalableBlock) state.getBlock()).isPortalableOnFace(state, face);
        }

        return inTag;
    }
}
