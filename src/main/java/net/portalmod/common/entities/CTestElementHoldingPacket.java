package net.portalmod.common.entities;

import net.portalmod.core.network.PacketContext;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

public class CTestElementHoldingPacket implements AbstractPacket<CTestElementHoldingPacket> {
    private int id;
    private Vec3 oldPosition;
    private Vec3 position;
    private float oldRotation;
    private float rotation;

    public CTestElementHoldingPacket() {}

    public CTestElementHoldingPacket(int id, Vec3 oldPosition, Vec3 position, float oldRotation, float rotation) {
        this.id = id;
        this.oldPosition = oldPosition;
        this.position = position;
        this.oldRotation = oldRotation;
        this.rotation = rotation;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.id);
        buffer.writeDouble(this.oldPosition.x);
        buffer.writeDouble(this.oldPosition.y);
        buffer.writeDouble(this.oldPosition.z);
        buffer.writeDouble(this.position.x);
        buffer.writeDouble(this.position.y);
        buffer.writeDouble(this.position.z);
        buffer.writeFloat(this.oldRotation);
        buffer.writeFloat(this.rotation);
    }

    @Override
    public CTestElementHoldingPacket decode(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        Vec3 oldPosition = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        Vec3 position = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        float oldRotation = buffer.readFloat();
        float rotation = buffer.readFloat();

        return new CTestElementHoldingPacket(id, oldPosition, position, oldRotation, rotation);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if(sender == null)
                return;

            Entity entity = sender.level.getEntity(this.id);
            if(!(entity instanceof TestElementEntity))
                return;

            TestElementEntity tee = (TestElementEntity)entity;
            if(entity.getVehicle() != sender)
                return;

            tee.serverOldPos = oldPosition;
            tee.setPos(this.position.x, this.position.y, this.position.z);

            tee.yRot = this.rotation;
            tee.yRotO = this.oldRotation;
            tee.yBodyRot = this.rotation;
            tee.yBodyRotO = this.oldRotation;
        });
        context.get().setPacketHandled(true);
        return true;
    }
}