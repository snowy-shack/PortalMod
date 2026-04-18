package net.portalmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalPair;
import net.portalmod.common.sorted.portal.PortalPairCache;
import net.portalmod.common.sorted.portal.PortalEntityRenderer;
import net.portalmod.core.math.Vec3;
import net.portalmod.common.sorted.portal.ITeleportable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 0))
    private void pmTeleportMinecart(CallbackInfo info) {
        PortalEntity.teleportEntity((Entity)(Object)this, ((Entity)(Object)this).getDeltaMovement());
    }


    @Shadow private double lx;
    @Shadow private double ly;
    @Shadow private double lz;

    // has to redirect setPos after lerping
    @Redirect(
                        method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;setPos(DDD)V",
                    ordinal = 0
            )
    )
    private void pmClientTeleport(AbstractMinecartEntity instance, double x, double y, double z) {
//        if(true) return;

//        AbstractMinecartEntity entity = (AbstractMinecartEntity)(Object)this;
//
//        Deque<DiscontinuousLerpPos> lerpPosQueue = ((IDiscontinuouslyLerpable)entity).getLerpPosQueue();
//        if(lerpPosQueue.isEmpty())
//            return;
//
//        DiscontinuousLerpPos lerpPos = lerpPosQueue.peek();
//        if(lerpPos.isIncomplete())
//            return;
//
//        if(lerpPos.isExtended()) {
//            lerpPos.apply(entity);
//            if(lerpPos.isDone()) {
//                lerpPosQueue.poll();
//            } else {
//                lerpPos.consume();
//            }
//        } else {
//            lerpPos.apply(entity);
//            lerpPosQueue.poll();
//        }

        if(!((ITeleportable)instance).hasLastUsedPortal()) {
            instance.setPos(x, y, z);
            return;
        }

        PortalEntity portal = (PortalEntity)instance.level.getEntity(((ITeleportable)instance).getLastUsedPortal());

        PortalPair pair = PortalPairCache.CLIENT.get(portal.getGunUUID());
        if(pair == null) {
            instance.setPos(x, y, z);
            return;
        }

        PortalEntity targetPortal = pair.get(portal.getEnd().other());
        if(targetPortal == null) {
            instance.setPos(x, y, z);
            return;
        }

        Vector3f normal = new Vec3(portal.getDirection().getNormal()).to3f();
        Vector3f normal2 = new Vec3(portal.getDirection().getNormal()).to3f();
        normal.mul(.5f);
        normal2.mul(PortalEntityRenderer.OFFSET * 10);
        Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
                .subtract(new Vector3d(normal)).add(new Vector3d(normal2));

        Vector3f targetnormal = new Vec3(targetPortal.getDirection().getNormal()).to3f();
        Vector3f targetnormal2 = new Vec3(targetPortal.getDirection().getNormal()).to3f();
        targetnormal.mul(.5f);
        targetnormal2.mul(PortalEntityRenderer.OFFSET * 10);
        Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
                .subtract(new Vector3d(targetnormal)).add(new Vector3d(targetnormal2));

        Vector3d offset = targetPortalPos.subtract(portalPos);
        Vector3d portalToEntity = new Vector3d(x, y, z).subtract(portalPos);
        if(portalToEntity.dot(new Vec3(portal.getDirection().getNormal()).to3d()) >= 0) {
            instance.setPos(x, y, z);
            return;
        }

        AxisAlignedBB bb = instance.getBoundingBox();
        Vector3d delta = new Vector3d(x, y, z).subtract(instance.xo, instance.yo, instance.zo);
        AxisAlignedBB travelAABB = bb.expandTowards(delta);
        Vec3 projected = portal.projectPointOnPortalSurface(new Vec3(bb.getCenter().add(delta)));

        boolean isInside = projected.x > -.5
                && projected.x < .5
                && projected.y > -1
                && projected.y < 1;

        if(!portal.getBoundingBox().intersects(travelAABB) || !isInside) {
            instance.setPos(x, y, z);
            return;
        }

        instance.setPos(x + offset.x, y + offset.y, z + offset.z);
        instance.xo += offset.x;
        instance.yo += offset.y;
        instance.zo += offset.z;
        instance.xOld += offset.x;
        instance.yOld += offset.y;
        instance.zOld += offset.z;
        this.lx += offset.x;
        this.ly += offset.y;
        this.lz += offset.z;
        ((ITeleportable)instance).removeLastUsedPortal();
    }
}