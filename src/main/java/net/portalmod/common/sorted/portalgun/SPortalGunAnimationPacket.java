package net.portalmod.common.sorted.portalgun;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.UUID;
import java.util.function.Supplier;

public class SPortalGunAnimationPacket implements AbstractPacket<SPortalGunAnimationPacket> {
    protected UUID gunUUID;
    protected PortalGunAnimation animation;

    public SPortalGunAnimationPacket() {}

    public SPortalGunAnimationPacket(UUID gunUUID, PortalGunAnimation animation) {
        this.gunUUID = gunUUID;
        this.animation = animation;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeUUID(gunUUID);
        buffer.writeEnum(animation);
    }

    @Override
    public SPortalGunAnimationPacket decode(PacketBuffer buffer) {
        return new SPortalGunAnimationPacket(
                buffer.readUUID(),
                buffer.readEnum(PortalGunAnimation.class));
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            switch(this.animation) {
                case SHOOT:
                    PortalGunISTER.startShootAnimation(this.gunUUID);
                    break;

                case FIZZLE:
                    PortalGunISTER.startFizzleAnimation();
                    break;

                case DROP:
                    PortalGunISTER.stopLiftAnimation(this.gunUUID);
                    break;

                case LIFT:
                    PortalGunISTER.startLiftAnimation(this.gunUUID);
                    break;
            }
        }));

        context.get().setPacketHandled(true);
        return true;
    }
}