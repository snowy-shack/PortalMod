package net.portalmod.common.sorted.gel;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CPropulsionGelBoostTickPacket implements AbstractPacket<CPropulsionGelBoostTickPacket> {
    private int ticks;

    public CPropulsionGelBoostTickPacket() {}

    public CPropulsionGelBoostTickPacket(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.ticks);
    }

    @Override
    public CPropulsionGelBoostTickPacket decode(PacketBuffer buffer) {
        return new CPropulsionGelBoostTickPacket(buffer.readInt());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if(player == null)
                return;

            ((IGelAffected)player).setPropulsionTicks(this.ticks);
        });

        context.get().setPacketHandled(true);
        return true;
    }
}