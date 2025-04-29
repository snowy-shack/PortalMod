package net.portalmod.common.sorted.fizzler;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface Fizzler {
    boolean isInsideField(AxisAlignedBB box, BlockPos pos, BlockState state);
}
