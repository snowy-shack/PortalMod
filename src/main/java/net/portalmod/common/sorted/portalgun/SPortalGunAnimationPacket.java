package net.portalmod.common.sorted.portalgun;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

public class SPortalGunAnimationPacket implements AbstractPacket<SPortalGunAnimationPacket> {
    protected UUID gunUUID;
    protected PortalGunAnimation animation;

    public SPortalGunAnimationPacket() {}

    public SPortalGunAnimationPacket(UUID gunUUID, PortalGunAnimation animation) {
        this.gunUUID = gunUUID;
        this.animation = animation;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(gunUUID);
        buffer.writeEnum(animation);
    }

    @Override
    public SPortalGunAnimationPacket decode(FriendlyByteBuf buffer) {
        return new SPortalGunAnimationPacket(
                buffer.readUUID(),
                buffer.readEnum(PortalGunAnimation.class));
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> ClientOnly.run(() -> () -> {
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