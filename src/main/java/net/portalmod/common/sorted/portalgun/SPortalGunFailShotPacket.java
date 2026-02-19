package net.portalmod.common.sorted.portalgun;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.common.sorted.portal.PortalPhotonParticle;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.packet.AbstractPacket;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SPortalGunFailShotPacket implements AbstractPacket<SPortalGunFailShotPacket> {
    private Vec3 position;
    private Vec3 normal;
    private Vec3 upVector;
    private String dyeColor;

    public SPortalGunFailShotPacket() {}

    public SPortalGunFailShotPacket(Vec3 position, Vec3 normal, Vec3 upVector, String dyeColor) {
        this.position = position;
        this.normal = normal;
        this.upVector = upVector;
        this.dyeColor = dyeColor;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeFloat((float)this.position.x);
        buffer.writeFloat((float)this.position.y);
        buffer.writeFloat((float)this.position.z);
        buffer.writeFloat((float)this.normal.x);
        buffer.writeFloat((float)this.normal.y);
        buffer.writeFloat((float)this.normal.z);
        buffer.writeFloat((float)this.upVector.x);
        buffer.writeFloat((float)this.upVector.y);
        buffer.writeFloat((float)this.upVector.z);
        buffer.writeInt(this.dyeColor.length());
        buffer.writeCharSequence(dyeColor, StandardCharsets.UTF_8);
    }

    @Override
    public SPortalGunFailShotPacket decode(PacketBuffer buffer) {
        Vec3 position = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        Vec3 normal = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        Vec3 upVector = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        int length = buffer.readInt();
        String dyeColor = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString();
        return new SPortalGunFailShotPacket(position, normal, upVector, dyeColor);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            PortalPhotonParticle.createFailParticles(Minecraft.getInstance().level, position, normal, upVector, dyeColor);
        }));

        context.get().setPacketHandled(true);
        return true;
    }
}