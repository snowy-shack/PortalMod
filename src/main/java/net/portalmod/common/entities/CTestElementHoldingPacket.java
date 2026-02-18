package net.portalmod.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

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
    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.id);
        buffer.writeFloat((float)this.oldPosition.x);
        buffer.writeFloat((float)this.oldPosition.y);
        buffer.writeFloat((float)this.oldPosition.z);
        buffer.writeFloat((float)this.position.x);
        buffer.writeFloat((float)this.position.y);
        buffer.writeFloat((float)this.position.z);
        buffer.writeFloat(this.oldRotation);
        buffer.writeFloat(this.rotation);
    }

    @Override
    public CTestElementHoldingPacket decode(PacketBuffer buffer) {
        int id = buffer.readInt();
        Vec3 oldPosition = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        Vec3 position = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        float oldRotation = buffer.readFloat();
        float rotation = buffer.readFloat();

        return new CTestElementHoldingPacket(id, oldPosition, position, oldRotation, rotation);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity sender = context.get().getSender();
            if(sender == null)
                return;

            Entity entity = sender.level.getEntity(this.id);
            if(!(entity instanceof TestElementEntity))
                return;

            TestElementEntity tee = (TestElementEntity)entity;
            if(entity.getVehicle() != sender)
                return;

            Vector3d position = entity.position();
            tee.serverOldPos = oldPosition;
            tee.setBoundingBox(entity.getBoundingBox().move(position.reverse()).move(this.position.to3d()));
            tee.setLocationFromBoundingbox();

            tee.yRot = this.rotation;
            tee.yRotO = this.oldRotation;
            tee.yBodyRot = this.rotation;
            tee.yBodyRotO = this.oldRotation;
        });
        context.get().setPacketHandled(true);
        return true;
    }
}