package net.portalmod.common.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.common.sorted.fizzler.Fizzler;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.util.ModUtil;

import java.util.stream.Stream;

public interface Fizzleable {
    boolean shouldCheckForFizzlers();
    void onTouchingFizzler();

    default void checkForFizzlers(Entity entity) {
        if (this.shouldCheckForFizzlers()) {
            this.checkTraversedBlocks(entity);
        }
    }

    default void checkTraversedBlocks(Entity entity) {
        AxisAlignedBB oldBox = this.boundingBoxAtPos(entity, ModUtil.getOldPos(entity));

        AxisAlignedBB movementBox = entity.getBoundingBox().minmax(oldBox);
        Stream<BlockPos> collidedPositions = BlockPos.betweenClosedStream(movementBox);

        collidedPositions.forEach(pos -> {
            BlockState state = entity.level.getBlockState(pos);
            if (this.isInsideFizzler(pos, state, movementBox)) {
                this.onTouchingFizzler();
            }
        });
    }

    default AxisAlignedBB boundingBoxAtPos(Entity entity, Vector3d position) {
        double h = entity.getBbHeight();
        double w = 0.5 * entity.getBbWidth();
        return new AxisAlignedBB(position.add(-w, 0, -w), position.add(w, h, w));
    }

    default boolean isInsideFizzler(BlockPos pos, BlockState state, AxisAlignedBB box) {
        Block block = state.getBlock();
        if (block instanceof Fizzler) {
            return ((Fizzler) block).isInsideField(box, pos, state);
        }

        return false;
    }

    static boolean isFizzleableItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof PortalGun;
    }
}
