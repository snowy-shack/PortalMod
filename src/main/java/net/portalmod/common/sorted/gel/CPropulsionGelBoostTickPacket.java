package net.portalmod.common.sorted.gel;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CPropulsionGelBoostTickPacket implements AbstractPacket<CPropulsionGelBoostTickPacket> {
    public CPropulsionGelBoostTickPacket() {}

    @Override
    public void encode(PacketBuffer buffer) {}

    @Override
    public CPropulsionGelBoostTickPacket decode(PacketBuffer buffer) {
        return new CPropulsionGelBoostTickPacket();
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(player == null)
                return;

            ((IGelAffected)player).setServerBoosting(true);
        });

        context.get().setPacketHandled(true);
        return true;
    }
}