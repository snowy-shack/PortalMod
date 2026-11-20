package net.portalmod.mixins.level;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.mixins.accessors.ChunkManagerAccessor;
import net.portalmod.mixins.accessors.ChunkManagerAccessor2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Set;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$EntityTracker")
public abstract class EntityTrackerMixin {
//    @Shadow protected abstract void updateChunkTracking(ServerPlayer player, ChunkPos chunkPos, IPacket<?>[] packets, boolean before, boolean after);

//    @Inject(
//            //            method = "move",
//            at = @At("TAIL")
//    )
//    private void pmLoadChunksInsidePortal(ServerPlayer player, CallbackInfo info) {
//        this.updateChunkTracking(player, new ChunkPos(5, -1), new IPacket[2], false, true);
//    }

    @Shadow @Final private Entity entity;

    @Shadow @Final private ServerEntity serverEntity;

    @Shadow protected abstract int getEffectiveRange();

//    @ModifyVariable(
//            //            method = "updatePlayer",
//            at = @At("STORE"),
//            ordinal = 0
//    )
//    private boolean pmKeepOtherPortalLoaded(boolean flag) {
//        ChunkMap chunkManagerSuper = ((ServerChunkCache)this.entity.level.getChunkSource()).chunkMap;
//        Entity entity = this.entity;
//
//        if(entity instanceof PortalEntity && ((PortalEntity)entity).isOpen()) {
//            Optional<PortalEntity> otherPortalOptional = ((PortalEntity)entity).getOtherPortal();
//            if(!otherPortalOptional.isPresent())
//                return flag;
//            PortalEntity otherPortal = otherPortalOptional.get();
//
//
//
//
//
//
//            Vec3 vector3d = player.position().subtract(this.serverEntity.sentPos());
//            int i = Math.min(this.getEffectiveRange(), (((ChunkManagerAccessor)chunkManagerSuper).pmGetViewDistance() - 1) * 16);
//            boolean flag = vector3d.x >= (double)(-i) && vector3d.x <= (double)i && vector3d.z >= (double)(-i) && vector3d.z <= (double)i && this.entity.broadcastToPlayer(player);
//
//
//
//
//
//
//
//
//            EntityTrackerMixin otherEntityTracker = ((EntityTrackerMixin)(((ChunkManagerAccessor)chunkManagerSuper)
//                    .pmGetEntityMap().get(otherPortal.getId())));
//
//            ServerEntity trackedOtherPortal = otherEntityTracker.serverEntity;
//
//            Vec3 otherDistance = distance.add(this.serverEntity.sentPos()).subtract(trackedOtherPortal.sentPos());
//            if(Vec3.ZERO.distanceTo(otherDistance) < Vec3.ZERO.distanceTo(distance))
//                return otherDistance;
//        }
//        return distance;
//    }

    @Shadow @Final private Set<ServerPlayer> seenBy;

    @Inject(
                        method = "updatePlayer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmLoadEntitiesThroughPortal(ServerPlayer player, CallbackInfo info) {
        info.cancel();
        ChunkMap chunkManagerSuper = ((ServerChunkCache)this.entity.level().getChunkSource()).chunkMap;
        int viewDistance = ((ChunkManagerAccessor)chunkManagerSuper).pmGetViewDistance();
        Int2ObjectMap<EntityTrackerMixin> entityMap = (Int2ObjectMap<EntityTrackerMixin>)
                ((ChunkManagerAccessor)chunkManagerSuper).pmGetEntityMap();

        if(player == this.entity)
            return;

        Vec3 distance = player.position().subtract(this.serverEntity.sentPos());
        int radius = Math.min(this.getEffectiveRange(), (viewDistance - 1) * 16);
        boolean isInRadius = pmIsInRadius(distance, radius);

        if(this.entity.broadcastToPlayer(player)) {
            for(EntityTrackerMixin portalTracker : entityMap.values()) {
                if(!(portalTracker.entity instanceof PortalEntity) || !((PortalEntity)portalTracker.entity).isOpen())
                    continue;

                PortalEntity portal = (PortalEntity)portalTracker.entity;
                Vec3 distanceToPortal = player.position().subtract(portalTracker.serverEntity.sentPos());
                int playerRadius = (viewDistance - 1) * 16;
                if(!pmIsInRadius(distanceToPortal, playerRadius))
                    continue;

                Optional<PortalEntity> otherPortalOptional = portal.getOtherPortal();
                if(!otherPortalOptional.isPresent())
                    continue;
                PortalEntity otherPortal = otherPortalOptional.get();

                Vec3 teleportedPos = player.position().subtract(portal.position()).add(otherPortal.position());
                Vec3 teleportedDistance = teleportedPos.subtract(this.serverEntity.sentPos());
                if(pmIsInRadius(teleportedDistance, radius)) {
                    isInRadius = true;
                    break;
                }
            }
        } else {
            isInRadius = false;
        }

        if(isInRadius) {
            boolean isInCheckerboard = this.entity.forcedLoading;
            if(!isInCheckerboard) {
                ChunkPos chunkpos = new ChunkPos(this.entity.xChunk, this.entity.zChunk);
                ChunkHolder chunkholder = ((ChunkManagerAccessor)chunkManagerSuper).pmGetVisibleChunkIfPresent(chunkpos.toLong());
                if(chunkholder != null && chunkholder.getTickingChunk() != null) {
                    isInCheckerboard = ChunkManagerAccessor.pmCheckerboardDistance(chunkpos, player, false) <= viewDistance;

                    if(!isInCheckerboard) {
                        for(EntityTrackerMixin portalTracker : entityMap.values()) {
                            if(!(portalTracker.entity instanceof PortalEntity) || !((PortalEntity) portalTracker.entity).isOpen())
                                continue;

                            PortalEntity portal = (PortalEntity) portalTracker.entity;
                            Vec3 distanceToPortal = player.position().subtract(portalTracker.serverEntity.sentPos());
                            int playerRadius = (viewDistance - 1) * 16;
                            if(!pmIsInRadius(distanceToPortal, playerRadius))
                                continue;

                            Optional<PortalEntity> otherPortalOptional = portal.getOtherPortal();
                            if(!otherPortalOptional.isPresent())
                                continue;
                            PortalEntity otherPortal = otherPortalOptional.get();

                            Vec3 teleportedPos = player.position().subtract(portal.position()).add(otherPortal.position());
                            int x = Mth.floor(teleportedPos.x / 16.0D);
                            int z = Mth.floor(teleportedPos.z / 16.0D);

                            if(ChunkManagerAccessor2.pmCheckerboardDistance(chunkpos, x, z) <= viewDistance) {
                                isInCheckerboard = true;
                                break;
                            }
                        }
                    }
                }
            }

            if(isInCheckerboard && this.seenBy.add(player)) {
                this.serverEntity.addPairing(player);
            }
        } else if(this.seenBy.remove(player)) {
            this.serverEntity.removePairing(player);
        }
    }

    private boolean pmIsInRadius(Vec3 vector, int radius) {
        return vector.x >= -radius
            && vector.x <=  radius
            && vector.z >= -radius
            && vector.z <=  radius;
    }
}