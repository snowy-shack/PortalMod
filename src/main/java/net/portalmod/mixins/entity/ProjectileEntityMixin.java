package net.portalmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.portalmod.common.sorted.portal.PortalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {

    @Inject(
            remap = false,
            method = "tick",
            at = @At("HEAD")
    )
    private void pmTeleport(CallbackInfo ci) {
        Entity thiss = (Entity)(Object)this;
        thiss.setDeltaMovement(PortalEntity.teleportEntity(thiss, PortalEntity.doFunneling(thiss, thiss.getDeltaMovement())));
    }

    @Inject(
            remap = false,
            method = "canHitEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmCantHitPortals(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(entity instanceof PortalEntity)
            cir.setReturnValue(false);
    }
}