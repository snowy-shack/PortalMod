package net.portalmod.common.sorted.portal;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;
import net.portalmod.core.packet.ClientPacketHandler;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;

public class SPortalShotPacket implements AbstractPacket<SPortalShotPacket> {
    public int id;

    public SPortalShotPacket() {}

    public SPortalShotPacket(int id) {
        this.id = id;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.id);
    }

    @Override
    public SPortalShotPacket decode(FriendlyByteBuf buffer) {
        return new SPortalShotPacket(buffer.readInt());
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> ClientOnly.run(() -> () -> ClientPacketHandler.handleSPortalShotPacket(this)));

        context.get().setPacketHandled(true);
        return true;
    }
}