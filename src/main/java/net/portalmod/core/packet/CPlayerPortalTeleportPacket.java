package net.portalmod.core.packet;

import net.portalmod.core.network.PacketContext;
import net.portalmod.common.sorted.portal.IClientTeleportable;
import net.portalmod.common.sorted.portal.PortalHandler;
import net.portalmod.core.interfaces.ITeleportLerpable;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

public class CPlayerPortalTeleportPacket implements AbstractPacket<CPlayerPortalTeleportPacket> {
    public CPlayerPortalTeleportPacket() {}

    @Override
    public void encode(FriendlyByteBuf buffer) {}

    @Override
    public CPlayerPortalTeleportPacket decode(FriendlyByteBuf buffer) {
        return new CPlayerPortalTeleportPacket();
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ((PortalHandler)context.get().getSender()).onTeleportPacket();
            ((IClientTeleportable)context.get().getSender()).setJustPortaled(true);
            ((ITeleportLerpable)context.get().getSender()).setHasUsedPortal(true);
        });
        context.get().setPacketHandled(true);
        return true;
    }
}