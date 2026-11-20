package net.portalmod.common.sorted.fizzler;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface Fizzler {
    boolean isInsideField(AABB box, BlockPos pos, BlockState state);
    boolean isActive(BlockState state);
    VoxelShape getFieldShape(BlockState state);

    static boolean isActiveFizzler(BlockState state) {
        return state.getBlock() instanceof Fizzler && ((Fizzler)state.getBlock()).isActive(state);
    }
}
