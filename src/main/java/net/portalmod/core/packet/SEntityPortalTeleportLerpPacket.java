package net.portalmod.core.packet;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.interfaces.ITeleportLerpable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

public class SEntityPortalTeleportLerpPacket implements AbstractPacket<SEntityPortalTeleportLerpPacket> {
    private int entityId;
    private byte xRot;
    private byte yRot;
    private boolean isOnGround;
    private Deque<Tuple<Vec3, Vec3>> lerpPositions;

    public SEntityPortalTeleportLerpPacket() {}

    public SEntityPortalTeleportLerpPacket(int entityId, byte xRot, byte yRot, boolean isOnGround, Deque<Tuple<Vec3, Vec3>> lerpPositions) {
        this.entityId = entityId;
        this.xRot = xRot;
        this.yRot = yRot;
        this.isOnGround = isOnGround;
        this.lerpPositions = lerpPositions;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeByte(this.xRot);
        buffer.writeByte(this.yRot);
        buffer.writeBoolean(this.isOnGround);
        buffer.writeInt(this.lerpPositions.size());

        for(Tuple<Vec3, Vec3> lerpPos : this.lerpPositions) {
            Vec3 oldPos = lerpPos.getA();
            Vec3 newPos = lerpPos.getB();
            buffer.writeDouble(oldPos.x);
            buffer.writeDouble(oldPos.y);
            buffer.writeDouble(oldPos.z);
            buffer.writeDouble(newPos.x);
            buffer.writeDouble(newPos.y);
            buffer.writeDouble(newPos.z);
        }
    }

    @Override
    public SEntityPortalTeleportLerpPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        byte xRot = buffer.readByte();
        byte yRot = buffer.readByte();
        boolean isOnGround = buffer.readBoolean();
        int size = buffer.readInt();
        Deque<Tuple<Vec3, Vec3>> lerpPositions = new ArrayDeque<>();

        for(int i = 0; i < size; i++) {
            Vec3 oldPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            Vec3 newPos = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            lerpPositions.add(new Tuple<>(oldPos, newPos));
        }

        return new SEntityPortalTeleportLerpPacket(entityId, xRot, yRot, isOnGround, lerpPositions);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ClientOnly.run(() -> () -> {
                Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
                if(entity != null && entity != Minecraft.getInstance().player) {
                    Vec3 lastPos = this.lerpPositions.peekLast().getB();
                    entity.setPacketCoordinates(lastPos.x, lastPos.y, lastPos.z);

                    float yRot = (float)(this.yRot * 360) / 256.0F;
                    float xRot = (float)(this.xRot * 360) / 256.0F;
                    // todo replace with proper invocation of setRot
                    entity.setYRot(xRot % 360.0F);
                    entity.setXRot(yRot % 360.0F);
                    entity.setOnGround(this.isOnGround);

                    Deque<Tuple<Vec3, Vec3>> lerpPositions =
                            ((ITeleportLerpable)entity).getLerpPositions();
                    if(!lerpPositions.isEmpty()) {
                        Vec3 pos = lerpPositions.peekLast().getB();
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