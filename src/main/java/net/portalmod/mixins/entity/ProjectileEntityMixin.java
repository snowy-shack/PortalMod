package net.portalmod.mixins.entity;

import net.portalmod.common.sorted.portal.PortalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

@Mixin(Projectile.class)
public class ProjectileEntityMixin {

    @Inject(
                        method = "tick",
            at = @At("HEAD")
    )
    private void pmTeleport(CallbackInfo ci) {
        Entity thiss = (Entity)(Object)this;
        thiss.setDeltaMovement(PortalEntity.teleportEntity(thiss, PortalEntity.doFunneling(thiss, thiss.getDeltaMovement())));
    }

    @Inject(
                        method = "canHitEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmCantHitPortals(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(entity instanceof PortalEntity)
            cir.setReturnValue(false);
    }
}