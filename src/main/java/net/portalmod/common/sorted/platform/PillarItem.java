package net.portalmod.common.sorted.platform;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

/**
 * Used for checking for pillar items, but does not add functionality by itself.
 */
public class PillarItem extends BlockItem {
    public PillarItem(Block block, Properties properties) {
        super(block, properties);
    }
}
