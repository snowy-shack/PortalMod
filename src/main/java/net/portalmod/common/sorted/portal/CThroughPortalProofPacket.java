package net.portalmod.common.sorted.portal;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CThroughPortalProofPacket implements AbstractPacket<CThroughPortalProofPacket> {
    private int[] portalChain;

    public CThroughPortalProofPacket() {}

    public CThroughPortalProofPacket(int[] portalChain) {
        this.portalChain = portalChain;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        int length = this.portalChain.length;
        buffer.writeInt(length);

        for(int i = 0; i < length; i++) {
            buffer.writeInt(this.portalChain[i]);
        }
    }

    @Override
    public CThroughPortalProofPacket decode(PacketBuffer buffer) {
        int length = buffer.readInt();
        int[] portalChain = new int[length];

        for(int i = 0; i < length; i++) {
            portalChain[i] = buffer.readInt();
        }

        return new CThroughPortalProofPacket(portalChain);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(player == null)
                return;

            MinecraftServer server = player.getServer();
            if(server == null)
                return;

            PortalServerProofManager.getInstance().setProof(player, server.getTickCount(), this.portalChain);
        });
        context.get().setPacketHandled(true);
        return true;
    }
}