package net.portalmod.mixins.level;

import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalManager;
import net.portalmod.core.util.EntityTickWrapper;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow @Final private static Logger LOGGER;

    @Shadow protected abstract boolean isUUIDUsed(Entity p_217478_1_);

    @Redirect(
                        method = "tickNonPassenger",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;tick()V"
            )
    )
    private void pmWrapServerTickNonPassenger(Entity entity) {
        EntityTickWrapper.wrapTick(entity, Entity::tick);
    }

    @Redirect(
                        method = "tickPassenger",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;rideTick()V"
            )
    )
    private void pmWrapServerTickPassenger(Entity entity) {
        EntityTickWrapper.wrapTick(entity, Entity::rideTick);
    }

//    @Redirect(
//            //            method = "addEntity",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/level/chunk/ChunkAccess;addEntity(Lnet/minecraft/world/entity/Entity;)V"
//            )
//    )
//    private void pmDoNotAddPortalToChunk(ChunkAccess chunk, Entity entity) {
//        if(!(entity instanceof PortalEntity))
//            chunk.addEntity(entity);
//    }

    @Inject(
                        method = "updateChunkPos",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmDoNotUpdateChunkPosForPortal(Entity entity, CallbackInfo info) {
        if(entity instanceof PortalEntity)
            info.cancel();
    }

    @Redirect(
                        method = "unload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;onEntityRemoved(Lnet/minecraft/world/entity/Entity;)V"
            )
    )
    private void pmWrapUnloadEntity(ServerLevel level, Entity entity) {
        PortalManager.getInstance().unloadingChunk = true;
        level.onEntityRemoved(entity);
        PortalManager.getInstance().unloadingChunk = false;
    }
}