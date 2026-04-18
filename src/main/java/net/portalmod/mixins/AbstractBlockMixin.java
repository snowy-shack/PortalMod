package net.portalmod.mixins;

import net.minecraft.util.math.shapes.VoxelShapes;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.portalmod.common.sorted.portal.PortalEntity;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockMixin {
    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private void pmGetCollisionShape(IBlockReader blockReader, BlockPos pos, ISelectionContext selectionContext, CallbackInfoReturnable<VoxelShape> info) {
        if(PortalEntity.shouldSkipCollision(blockReader, pos, selectionContext))
            info.setReturnValue(VoxelShapes.empty());
    }
}