package io.github.serialsniper.portalmod.mixins;

import io.github.serialsniper.portalmod.common.entities.PortalEntity;
import io.github.serialsniper.portalmod.core.init.EntityInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ConcurrentModificationException;
import java.util.List;

@Mixin(Block.class)
public class BlockMixin {
    // todo delete mixin
    @Inject(at = @At(value = "RETURN"), method = "shouldRenderFace(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)Z", cancellable = true)
    private static void portalmod_shouldRenderFace(BlockState state, IBlockReader level, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        BlockPos p = pos.relative(direction);

        AxisAlignedBB aabb = new AxisAlignedBB(
                p.getX(), p.getY() - 1, p.getZ(),
                p.getX() + 1, p.getY() + 1, p.getZ() + 1
        );

        try {
            List<PortalEntity> portals = Minecraft.getInstance().level.getEntities(EntityInit.PORTAL.get(), aabb, portal -> {
                BlockPos portalPos = portal.getPos();
                return portal.getDirection() == direction && (portalPos.equals(p) || portalPos.relative(Direction.UP).equals(p));
            });

            if(!portals.isEmpty())
                info.setReturnValue(false);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}