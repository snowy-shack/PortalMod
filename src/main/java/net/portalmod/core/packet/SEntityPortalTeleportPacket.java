package net.portalmod.core.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.SEntityPacket;
import net.minecraft.network.play.server.SEntityTeleportPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalPair;
import net.portalmod.common.sorted.portal.PortalPairCache;
import net.portalmod.common.sorted.portal.PortalRenderer;
import net.portalmod.common.sorted.portal.ITeleportable;
import net.portalmod.core.math.Vec3;
import net.portalmod.mixins.accessors.AbstractMinecartAccessor;
import net.portalmod.mixins.accessors.LivingEntityAccessor;

import java.io.IOException;

public class SEntityPortalTeleportPacket implements IPacket<IClientPlayNetHandler> {
    private IPacket<IClientPlayNetHandler> wrapped;
    private int portalId;
    private double x;
    private double y;
    private double z;

    public SEntityPortalTeleportPacket() {}

    public SEntityPortalTeleportPacket(IPacket<IClientPlayNetHandler> wrapped, int portalId, double x, double y, double z) {
        this.wrapped = wrapped;
        this.portalId = portalId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void read(PacketBuffer packetBuffer) throws IOException {
        wrapped.read(packetBuffer);
        this.portalId = packetBuffer.readInt();
        this.x = packetBuffer.readDouble();
        this.y = packetBuffer.readDouble();
        this.z = packetBuffer.readDouble();
    }

    @Override
    public void write(PacketBuffer packetBuffer) throws IOException {
        wrapped.write(packetBuffer);
        packetBuffer.writeInt(this.portalId);
        packetBuffer.writeDouble(this.x);
        packetBuffer.writeDouble(this.y);
        packetBuffer.writeDouble(this.z);
    }

    @Override
    public void handle(IClientPlayNetHandler clientPlayNetHandler) {
        // todo dont use Minecraft

        PacketThreadUtil.ensureRunningOnSameThread(this, clientPlayNetHandler, Minecraft.getInstance());
        wrapped.handle(clientPlayNetHandler);

        Entity entity = null;

        if(wrapped instanceof SEntityTeleportPacket) {
            SEntityTeleportPacket packet = (SEntityTeleportPacket)wrapped;
            entity = Minecraft.getInstance().level.getEntity(packet.getId());
        }
        if(wrapped instanceof SEntityPacket) {
            SEntityPacket packet = (SEntityPacket)wrapped;
            entity = packet.getEntity(Minecraft.getInstance().level);
        }

        if(entity != null) {
            if(!entity.isControlledByLocalInstance()) {
//                Vector3d offset = entity.getPosition(0).subtract(entity.getPosition(1));
//                entity.setPos(x, y, z);
//                entity.xo = x + offset.x;
//                entity.yo = y + offset.y;
//                entity.zo = z + offset.z;
//                entity.xOld = x + offset.x;
//                entity.yOld = y + offset.y;
//                entity.zOld = z + offset.z;

                if(this.portalId != -1)
                    ((ITeleportable)entity).setLastUsedPortal(this.portalId);
                if(!((ITeleportable)entity).hasLastUsedPortal())
                    return;

//                if(!((ITeleportable)entity).hasLastUsedPortal() && this.portalId == -1)
//                    return;

//                ((ITeleportable)entity).setLastUsedPortal(this.portalId);
//
//                if(!((ITeleportable)entity).hasLastUsedPortal())
//                    return;

                PortalEntity portal = (PortalEntity)entity.level.getEntity(((ITeleportable)entity).getLastUsedPortal());
//                System.out.println(this.portalId);
                if(portal == null)
                    return;
                PortalPair pair = PortalPairCache.CLIENT.get(portal.getGunUUID());
                PortalEntity targetPortal = pair.get(portal.getEnd().other());

//                Vector3f normal = new Vec3(portal.getDirection().getNormal()).to3f();
//                Vector3f normal2 = new Vec3(portal.getDirection().getNormal()).to3f();
//                normal.mul(.5f);
//                normal2.mul(PortalRenderer.OFFSET * 10);
//                Vector3d portalPos = Vector3d.atCenterOf(portal.blockPosition())
//                        .subtract(new Vector3d(normal)).add(new Vector3d(normal2));

                Vec3 portalPos = portal.getPlanePos();

//                Vector3f targetnormal = new Vec3(targetPortal.getDirection().getNormal()).to3f();
//                Vector3f targetnormal2 = new Vec3(targetPortal.getDirection().getNormal()).to3f();
//                targetnormal.mul(.5f);
//                targetnormal2.mul(PortalRenderer.OFFSET * 10);
//                Vector3d targetPortalPos = Vector3d.atCenterOf(targetPortal.blockPosition())
//                        .subtract(new Vector3d(targetnormal)).add(new Vector3d(targetnormal2));

                Vec3 targetPortalPos = portal.getPlanePos();

//                Vector3d offset = targetPortalPos.subtract(portalPos);
//                Vector3d pos = new Vector3d(x, y, z).subtract(offset);

                Vec3 offset = targetPortalPos.sub(portalPos);
                Vec3 pos = new Vec3(x, y, z).sub(offset);

                if(entity instanceof AbstractMinecartEntity) {
                    ((AbstractMinecartAccessor)entity).pmSetLX(pos.x);
                    ((AbstractMinecartAccessor)entity).pmSetLY(pos.y);
                    ((AbstractMinecartAccessor)entity).pmSetLZ(pos.z);
                } else if(entity instanceof LivingEntity) {
                    ((LivingEntityAccessor)entity).pmSetLerpX(pos.x);
                    ((LivingEntityAccessor)entity).pmSetLerpY(pos.y);
                    ((LivingEntityAccessor)entity).pmSetLerpZ(pos.z);
                }
//                if(entity instanceof AbstractMinecartEntity)
//                    ((AbstractMinecartAccessor)entity).pmSetLSteps(0);
//                else
//                    ((LivingEntityAccessor)entity).pmSetLerpSteps(0);
            }
        }
    }
}