package net.portalmod.mixins.hooks.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces Forge's EntityEvent.Size (client only, like the original handler). */
@Mixin(Entity.class)
public abstract class EntityHooksMixin {
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void pmOnGetDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        EntityDimensions size = ClientEvents.entitySize((Entity) (Object) this, cir.getReturnValue());
        if (size != cir.getReturnValue())
            cir.setReturnValue(size);
    }
}
