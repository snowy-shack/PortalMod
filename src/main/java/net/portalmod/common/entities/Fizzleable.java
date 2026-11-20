package net.portalmod.common.entities;

import net.portalmod.common.sorted.fizzler.Fizzler;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.util.ModUtil;

import java.util.stream.Stream;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface Fizzleable {
    boolean shouldCheckForFizzlers();
    void onTouchingFizzler();

    default void checkForFizzlers(Entity entity) {
        if (this.shouldCheckForFizzlers()) {
            this.checkTraversedBlocks(entity);
        }
    }

    default void checkTraversedBlocks(Entity entity) {
        AABB oldBox = this.boundingBoxAtPos(entity, ModUtil.getOldPos(entity));

        AABB movementBox = entity.getBoundingBox().minmax(oldBox);
        Stream<BlockPos> collidedPositions = BlockPos.betweenClosedStream(movementBox);

        collidedPositions.forEach(pos -> {
            BlockState state = entity.level().getBlockState(pos);
            if (this.isInsideFizzler(pos, state, movementBox)) {
                this.onTouchingFizzler();
            }
        });
    }

    default AABB boundingBoxAtPos(Entity entity, Vec3 position) {
        double h = entity.getBbHeight();
        double w = 0.5 * entity.getBbWidth();
        return new AABB(position.add(-w, 0, -w), position.add(w, h, w));
    }

    default boolean isInsideFizzler(BlockPos pos, BlockState state, AABB box) {
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
