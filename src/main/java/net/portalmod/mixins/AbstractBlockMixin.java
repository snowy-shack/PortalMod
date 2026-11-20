package net.portalmod.mixins;

import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.portalmod.common.sorted.portal.PortalEntity;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

@Mixin(BlockBehaviour.AbstractBlockState.class)
public class AbstractBlockMixin {
    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private void pmGetCollisionShape(BlockGetter blockReader, BlockPos pos, CollisionContext selectionContext, CallbackInfoReturnable<VoxelShape> info) {
        if(PortalEntity.shouldSkipCollision(blockReader, pos, selectionContext))
            info.setReturnValue(Shapes.empty());
    }
}