package net.portalmod.common.sorted.gel;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CRepulsionGelBouncePacket implements AbstractPacket<CRepulsionGelBouncePacket> {
    private boolean bounced;

    public CRepulsionGelBouncePacket() {}

    public CRepulsionGelBouncePacket(boolean bounced) {
        this.bounced = bounced;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBoolean(this.bounced);
    }

    @Override
    public CRepulsionGelBouncePacket decode(PacketBuffer buffer) {
        return new CRepulsionGelBouncePacket(buffer.readBoolean());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(player == null)
                return;

            ((IGelAffected)player).setBounced(this.bounced);
        });

        context.get().setPacketHandled(true);
        return true;
    }
}