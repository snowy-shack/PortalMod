package net.portalmod.common.sorted.portal;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;
import net.portalmod.core.packet.ClientPacketHandler;

import java.util.function.Supplier;

public class SPortalShotPacket implements AbstractPacket<SPortalShotPacket> {
    public int id;

    public SPortalShotPacket() {}

    public SPortalShotPacket(int id) {
        this.id = id;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.id);
    }

    @Override
    public SPortalShotPacket decode(PacketBuffer buffer) {
        return new SPortalShotPacket(buffer.readInt());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.handleSPortalShotPacket(this)));

        context.get().setPacketHandled(true);
        return true;
    }
}