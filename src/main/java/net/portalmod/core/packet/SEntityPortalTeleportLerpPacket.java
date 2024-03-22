package net.portalmod.core.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.interfaces.ITeleportLerpable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public class SEntityPortalTeleportLerpPacket implements AbstractPacket<SEntityPortalTeleportLerpPacket> {
    private int entityId;
    private byte xRot;
    private byte yRot;
    private boolean isOnGround;
    private Deque<Tuple<Vector3d, Vector3d>> lerpPositions;

    public SEntityPortalTeleportLerpPacket() {}

    public SEntityPortalTeleportLerpPacket(int entityId, byte xRot, byte yRot, boolean isOnGround, Deque<Tuple<Vector3d, Vector3d>> lerpPositions) {
        this.entityId = entityId;
        this.xRot = xRot;
        this.yRot = yRot;
        this.isOnGround = isOnGround;
        this.lerpPositions = lerpPositions;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeByte(this.xRot);
        buffer.writeByte(this.yRot);
        buffer.writeBoolean(this.isOnGround);
        buffer.writeInt(this.lerpPositions.size());

        for(Tuple<Vector3d, Vector3d> lerpPos : this.lerpPositions) {
            Vector3d oldPos = lerpPos.getA();
            Vector3d newPos = lerpPos.getB();
            buffer.writeDouble(oldPos.x);
            buffer.writeDouble(oldPos.y);
            buffer.writeDouble(oldPos.z);
            buffer.writeDouble(newPos.x);
            buffer.writeDouble(newPos.y);
            buffer.writeDouble(newPos.z);
        }
    }

    @Override
    public SEntityPortalTeleportLerpPacket decode(PacketBuffer buffer) {
        int entityId = buffer.readInt();
        byte xRot = buffer.readByte();
        byte yRot = buffer.readByte();
        boolean isOnGround = buffer.readBoolean();
        int size = buffer.readInt();
        Deque<Tuple<Vector3d, Vector3d>> lerpPositions = new ArrayDeque<>();

        for(int i = 0; i < size; i++) {
            Vector3d oldPos = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            Vector3d newPos = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            lerpPositions.add(new Tuple<>(oldPos, newPos));
        }

        return new SEntityPortalTeleportLerpPacket(entityId, xRot, yRot, isOnGround, lerpPositions);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
                if(entity != null && entity != Minecraft.getInstance().player) {
                    Vector3d lastPos = this.lerpPositions.peekLast().getB();
                    entity.setPacketCoordinates(lastPos.x, lastPos.y, lastPos.z);

                    float yRot = (float)(this.yRot * 360) / 256.0F;
                    float xRot = (float)(this.xRot * 360) / 256.0F;
                    // todo replace with proper invocation of setRot
                    entity.yRot = xRot % 360.0F;
                    entity.xRot = yRot % 360.0F;
                    entity.setOnGround(this.isOnGround);

                    Deque<Tuple<Vector3d, Vector3d>> lerpPositions =
                            ((ITeleportLerpable)entity).getLerpPositions();
                    if(!lerpPositions.isEmpty()) {
                        Vector3d pos = lerpPositions.peekLast().getB();
                        entity.setPosAndOldPos(pos.x, pos.y, pos.z);
                        lerpPositions.clear();
                    }
                    lerpPositions.addAll(this.lerpPositions);
                }
            });
        });

        context.get().setPacketHandled(true);
        return true;
    }
}