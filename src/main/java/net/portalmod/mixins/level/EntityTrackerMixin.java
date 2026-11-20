package net.portalmod.mixins.level;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.mixins.accessors.ChunkManagerAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Set;

/**
 * Lets players see (track) entities that are only visible through a portal.
 *
 * 1.20.1 note: vanilla tracking is now a circular distance check and the 1.16 "checkerboard"
 * chunk test is gone, so this keeps the portal logic and drops the chunk test.
 * TODO(port): check in game that entities seen through portals in chunks the client has not
 * loaded still render.
 */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class EntityTrackerMixin {
    @Shadow @Final Entity entity;
    @Shadow @Final ServerEntity serverEntity;
    @Shadow @Final private Set<ServerPlayerConnection> seenBy;

    @Shadow protected abstract int getEffectiveRange();

    @Inject(method = "updatePlayer", at = @At("HEAD"), cancellable = true)
    private void pmLoadEntitiesThroughPortal(ServerPlayer player, CallbackInfo info) {
        info.cancel();
        if (player == this.entity)
            return;

        ChunkMap chunkMap = ((ServerChunkCache) this.entity.level().getChunkSource()).chunkMap;
        int viewDistance = ((ChunkManagerAccessor) chunkMap).pmGetViewDistance();
        @SuppressWarnings("unchecked")
        Int2ObjectMap<EntityTrackerMixin> entityMap = (Int2ObjectMap<EntityTrackerMixin>) ((ChunkManagerAccessor) chunkMap).pmGetEntityMap();

        double radius = Math.min(this.getEffectiveRange(), viewDistance * 16);
        boolean visible = false;

        if (this.entity.broadcastToPlayer(player)) {
            visible = pmIsInRadius(player.position().subtract(this.entity.position()), radius);

            if (!visible) {
                double playerRadius = viewDistance * 16;
                for (EntityTrackerMixin portalTracker : entityMap.values()) {
                    if (!(portalTracker.entity instanceof PortalEntity portal) || !portal.isOpen())
                        continue;
                    if (!pmIsInRadius(player.position().subtract(portal.position()), playerRadius))
                        continue;

                    Optional<PortalEntity> otherPortal = portal.getOtherPortal();
                    if (otherPortal.isEmpty())
                        continue;

                    Vec3 teleportedPos = player.position().subtract(portal.position()).add(otherPortal.get().position());
                    if (pmIsInRadius(teleportedPos.subtract(this.entity.position()), radius)) {
                        visible = true;
                        break;
                    }
                }
            }
        }

        if (visible) {
            if (this.seenBy.add(player.connection))
                this.serverEntity.addPairing(player);
        } else if (this.seenBy.remove(player.connection)) {
            this.serverEntity.removePairing(player);
        }
    }

    private static boolean pmIsInRadius(Vec3 offset, double radius) {
        return offset.x * offset.x + offset.z * offset.z <= radius * radius;
    }
}
