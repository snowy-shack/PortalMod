package net.portalmod.core.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.common.sorted.portal.IClientTeleportable;
import net.portalmod.common.sorted.portal.PortalHandler;
import net.portalmod.core.interfaces.ITeleportLerpable;

import java.util.function.Supplier;

public class CPlayerPortalTeleportPacket implements AbstractPacket<CPlayerPortalTeleportPacket> {
    public CPlayerPortalTeleportPacket() {}

    @Override
    public void encode(PacketBuffer buffer) {}

    @Override
    public CPlayerPortalTeleportPacket decode(PacketBuffer buffer) {
        return new CPlayerPortalTeleportPacket();
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ((PortalHandler)context.get().getSender()).onTeleportPacket();
            ((IClientTeleportable)context.get().getSender()).setJustPortaled(true);
            ((ITeleportLerpable)context.get().getSender()).setHasUsedPortal(true);
        });
        context.get().setPacketHandled(true);
        return true;
    }
}