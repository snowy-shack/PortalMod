package net.portalmod.common.sorted.step;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

/**
 * Used for checking for pillar items, but does not add functionality by itself.
 */
public class StepPillarItem extends BlockItem {
    public StepPillarItem(Block block, Properties properties) {
        super(block, properties);
    }
}
