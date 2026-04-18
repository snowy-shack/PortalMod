package net.portalmod.mixins.level.chunk;

import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.ChunkPrimer;
import net.portalmod.common.sorted.portal.PortalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkPrimer.class)
public class ChunkPrimerMixin {

    @Inject(
                        method = "addEntity(Lnet/minecraft/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmExcludePortalsFromChunkPrimer(Entity entity, CallbackInfo info) {
        if(entity instanceof PortalEntity)
            info.cancel();
    }
}