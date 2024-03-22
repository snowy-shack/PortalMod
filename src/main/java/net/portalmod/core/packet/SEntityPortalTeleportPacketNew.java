package net.portalmod.core.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.core.interfaces.IDiscontinuouslyLerpable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public class SEntityPortalTeleportPacketNew implements AbstractPacket<SEntityPortalTeleportPacketNew> {
    private int entityId;
    private Deque<DiscontinuousLerpPos> lerpPosQueue;

    public SEntityPortalTeleportPacketNew() {}

    public SEntityPortalTeleportPacketNew(int entityId, Deque<DiscontinuousLerpPos> lerpPosQueue) {
        this.entityId = entityId;
        this.lerpPosQueue = lerpPosQueue;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeInt(lerpPosQueue.size());

        for(DiscontinuousLerpPos lerpPos : lerpPosQueue) {
            Vector3d from = lerpPos.getFrom();
            Vector3d to = lerpPos.getFrom();
            int ticks = lerpPos.getTicks();
            buffer.writeDouble(from.x);
            buffer.writeDouble(from.y);
            buffer.writeDouble(from.z);
            buffer.writeDouble(to.x);
            buffer.writeDouble(to.y);
            buffer.writeDouble(to.z);
            buffer.writeInt(ticks);
        }
    }

    @Override
    public SEntityPortalTeleportPacketNew decode(PacketBuffer buffer) {
        int entityId = buffer.readInt();
        int size = buffer.readInt();
        Deque<DiscontinuousLerpPos> lerpPosQueue = new ArrayDeque<>();

        for(int i = 0; i < size; i++) {
            Vector3d from = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            Vector3d to = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            int ticks = buffer.readInt();
            lerpPosQueue.add(new DiscontinuousLerpPos(from, to, ticks));
        }

        return new SEntityPortalTeleportPacketNew(entityId, lerpPosQueue);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
                if(entity != null) {
                    if(!entity.isControlledByLocalInstance()) {
                        Deque<DiscontinuousLerpPos> lerpPosQueue =
                                ((IDiscontinuouslyLerpable)entity).getLerpPosQueue();
                        lerpPosQueue.clear();
                        lerpPosQueue.addAll(this.lerpPosQueue);
                        // todo not needed
//                        entity.setPacketCoordinates(this.lerpPosQueue.peekLast().getTo());
                    }
                }
            });
        });

        context.get().setPacketHandled(true);
        return true;
    }
}