package net.portalmod.common.sorted.trigger;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CTriggerAbortConfigPacket implements AbstractPacket<CTriggerAbortConfigPacket> {
    private BlockPos pos;

    public CTriggerAbortConfigPacket() {}

    public CTriggerAbortConfigPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public CTriggerAbortConfigPacket decode(PacketBuffer buffer) {
        return new CTriggerAbortConfigPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity sender = context.get().getSender();
            if (sender != null)
                TriggerSelectionServer.endConfiguration(sender);
        });

        context.get().setPacketHandled(true);
        return true;
    }
}