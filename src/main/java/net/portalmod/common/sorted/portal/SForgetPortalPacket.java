package net.portalmod.common.sorted.portal;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.UUID;
import java.util.function.Supplier;

public class SForgetPortalPacket implements AbstractPacket<SForgetPortalPacket> {
    private UUID uuid;
    private PortalEnd end;

    public SForgetPortalPacket() {}

    public SForgetPortalPacket(UUID uuid, PortalEnd end) {
        this.uuid = uuid;
        this.end = end;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer
        .writeUUID(uuid)
        .writeBoolean(end == PortalEnd.SECONDARY);
    }

    @Override
    public SForgetPortalPacket decode(PacketBuffer buffer) {
        UUID uuid = buffer.readUUID();
        PortalEnd end = buffer.readBoolean() ? PortalEnd.SECONDARY : PortalEnd.PRIMARY;
        return new SForgetPortalPacket(uuid, end);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                PortalEntity oldPortal = ClientPortalManager.getInstance().get(this.uuid, this.end);
                ClientPortalManager.getInstance().forgetPortal(this.uuid, this.end);

                if(oldPortal != null)
                    PortalPhotonParticle.createClosingParticles(oldPortal);
            });
        });

        context.get().setPacketHandled(true);
        return true;
    }
}