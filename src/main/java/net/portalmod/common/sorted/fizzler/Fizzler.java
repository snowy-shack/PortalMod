package net.portalmod.common.sorted.fizzler;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;

public interface Fizzler {
    boolean isInsideField(AxisAlignedBB box, BlockPos pos, BlockState state);
    boolean isActive(BlockState state);
    VoxelShape getFieldShape(BlockState state);

    static boolean isActiveFizzler(BlockState state) {
        return state.getBlock() instanceof Fizzler && ((Fizzler)state.getBlock()).isActive(state);
    }
}
