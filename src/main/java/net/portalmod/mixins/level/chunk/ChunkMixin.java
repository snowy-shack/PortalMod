package net.portalmod.mixins.level.chunk;

import net.portalmod.common.sorted.portal.PortalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(LevelChunk.class)
public class ChunkMixin {

//    @Inject(
//            //            method = "addEntity",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void pmExcludePortalsFromChunk(Entity entity, CallbackInfo info) {
//        if(entity instanceof PortalEntity)
//            info.cancel();
//    }
}