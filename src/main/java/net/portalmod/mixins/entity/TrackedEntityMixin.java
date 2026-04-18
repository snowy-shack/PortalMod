package net.portalmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.TrackedEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.interfaces.ITeleportLerpable;
import net.portalmod.core.packet.SEntityPortalTeleportLerpPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(TrackedEntity.class)
public class TrackedEntityMixin {
    @Shadow @Final private Entity entity;

    @Redirect(
                        method = "sendChanges",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 3
            )
    )
    private void pmHijackPositionPacket(Consumer<IPacket<?>> instance, Object packetObject) {
        IPacket<?> packet = (IPacket<?>)packetObject;
        ITeleportLerpable victim = ((ITeleportLerpable)this.entity);

        if(victim.hasUsedPortal() && (this.entity instanceof LivingEntity)) {
            byte xRot = (byte) MathHelper.floor(this.entity.yRot * 256.0F / 360.0F);
            byte yRot = (byte) MathHelper.floor(this.entity.xRot * 256.0F / 360.0F);
            PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(), new SEntityPortalTeleportLerpPacket(
                    this.entity.getId(), xRot, yRot, this.entity.isOnGround(), victim.getLerpPositions()));
        } else {
            instance.accept(packet);
        }

        victim.setHasUsedPortal(false);
        victim.getLerpPositions().clear();
    }

//    @Inject(
//            //            method = "sendChanges",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
//                    shift = At.Shift.AFTER,
//                    ordinal = 3
//            )
//    )
//    private void pmWrapMovePacket(CallbackInfo info) {
////        if(((ITeleportable)this.entity).hasLastUsedPortal()) {
////            int lastUsedPortal = ((ITeleportable)this.entity).getLastUsedPortal();
////            PortalEntity portal = (PortalEntity)this.entity.level.getEntity(lastUsedPortal);
////            PortalPair pair = PortalPairManager.CLIENT.get(portal.getGunUUID());
////            PortalEntity targetPortal = pair.get(portal.getEnd().other());
////
////            Vector3f normal = portal.getDirection().step();
////            Vector3f normal2 = portal.getDirection().step();
////            normal.mul(.5f);
////            normal2.mul(PortalRenderer.OFFSET * 10);
////            Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
////                    .subtract(new Vector3d(normal)).add(new Vector3d(normal2));
////
////            Vector3f targetnormal = targetPortal.getDirection().step();
////            Vector3f targetnormal2 = targetPortal.getDirection().step();
////            targetnormal.mul(.5f);
////            targetnormal2.mul(PortalRenderer.OFFSET * 10);
////            Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
////                    .subtract(new Vector3d(targetnormal)).add(new Vector3d(targetnormal2));
////
////            Vector3d offset = targetPortalPos.subtract(portalPos);
////            Vector3d pos = this.entity.position().subtract(offset);
//
//        if(this.entity.level.isClientSide)
//            return;
//
//        Deque<DiscontinuousLerpPos> lerpPosQueue = ((IDiscontinuouslyLerpable)entity).getLerpPosQueue();
//
//        if(lerpPosQueue.isEmpty())
//            return;
//
//        if(lerpPosQueue.peekLast().isIncomplete())
//            lerpPosQueue.peekLast().extendLerp(this.entity.position());
//
//        PacketInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
//                new SEntityPortalTeleportPacketNew(this.entity.getId(), lerpPosQueue));
//
//        lerpPosQueue.clear();
//
//
////            Vector3d pos = this.entity.position();
////
////            instance.accept(new SEntityPortalTeleportPacket((IPacket<IClientPlayNetHandler>)packet,
////                    lastUsedPortal, pos.x, pos.y, pos.z));
////
////            ((ITeleportable)this.entity).removeLastUsedPortal();
////        } else {
////            instance.accept((IPacket<IClientPlayNetHandler>)packet);
////        }
//    }
}