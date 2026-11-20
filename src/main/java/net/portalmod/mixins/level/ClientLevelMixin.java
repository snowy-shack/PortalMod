package net.portalmod.mixins.level;

import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.core.util.EntityTickWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Redirect(
                        method = "tickNonPassenger",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;tick()V"
            )
    )
    private void pmWrapClientTickNonPassenger(Entity entity) {
        EntityTickWrapper.wrapTick(entity, Entity::tick);
    }

    @Redirect(
                        method = "tickPassenger",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;rideTick()V"
            )
    )
    private void pmWrapClientTickPassenger(Entity entity) {
        EntityTickWrapper.wrapTick(entity, Entity::rideTick);
    }

    @Inject(
                        method = "updateChunkPos",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmDoNotUpdateChunkPosForPortal(Entity entity, CallbackInfo info) {
        if(entity instanceof PortalEntity)
            info.cancel();
    }
}