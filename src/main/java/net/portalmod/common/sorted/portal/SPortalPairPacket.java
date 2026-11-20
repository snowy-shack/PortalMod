package net.portalmod.common.sorted.portal;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

public class SPortalPairPacket implements AbstractPacket<SPortalPairPacket> {
    private UUID uuid;
    private PartialPortalPair ppp;

    public SPortalPairPacket() {}

    public SPortalPairPacket(UUID uuid, PartialPortalPair ppp) {
        this.uuid = uuid;
        this.ppp = ppp;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer
        .writeUUID(uuid)
        .writeBoolean(ppp.has(PortalEnd.PRIMARY))
        .writeBoolean(ppp.has(PortalEnd.SECONDARY));

        if(ppp.has(PortalEnd.PRIMARY))
            ppp.get(PortalEnd.PRIMARY).write(buffer);
        if(ppp.has(PortalEnd.SECONDARY))
            ppp.get(PortalEnd.SECONDARY).write(buffer);
    }

    @Override
    public SPortalPairPacket decode(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        boolean hasPrimary = buffer.readBoolean();
        boolean hasSecondary = buffer.readBoolean();
        PartialPortalPair ppp = new PartialPortalPair();

        if(hasPrimary)
            ppp.set(PortalEnd.PRIMARY, PartialPortal.read(buffer));
        if(hasSecondary)
            ppp.set(PortalEnd.SECONDARY, PartialPortal.read(buffer));

        return new SPortalPairPacket(uuid, ppp);
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ClientOnly.run(() -> () -> {
                ClientPortalManager.getInstance().getPartialMap().put(this.uuid, this.ppp);
            });
        });

        context.get().setPacketHandled(true);
        return true;
    }
}