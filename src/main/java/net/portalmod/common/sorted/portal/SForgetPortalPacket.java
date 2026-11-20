package net.portalmod.common.sorted.portal;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

public class SForgetPortalPacket implements AbstractPacket<SForgetPortalPacket> {
    private UUID uuid;
    private PortalEnd end;

    public SForgetPortalPacket() {}

    public SForgetPortalPacket(UUID uuid, PortalEnd end) {
        this.uuid = uuid;
        this.end = end;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer
        .writeUUID(uuid)
        .writeBoolean(end == PortalEnd.SECONDARY);
    }

    @Override
    public SForgetPortalPacket decode(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        PortalEnd end = buffer.readBoolean() ? PortalEnd.SECONDARY : PortalEnd.PRIMARY;
        return new SForgetPortalPacket(uuid, end);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ClientOnly.run(() -> () -> {
                PortalEntity oldPortal = ClientPortalManager.getInstance().get(this.uuid, this.end);
                ClientPortalManager.getInstance().forgetPortal(this.uuid, this.end);

                if(oldPortal != null)
                    PortalPhotonParticle.createClosingParticles(oldPortal);

                PortalEntity otherPortal = ClientPortalManager.getInstance().get(this.uuid, this.end.other());
                if(otherPortal != null)
                    otherPortal.pushEntities();
            });
        });

        context.get().setPacketHandled(true);
        return true;
    }
}