package net.portalmod.common.sorted.trigger;

import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class CTriggerAbortConfigPacket implements AbstractPacket<CTriggerAbortConfigPacket> {
    private BlockPos pos;

    public CTriggerAbortConfigPacket() {}

    public CTriggerAbortConfigPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public CTriggerAbortConfigPacket decode(FriendlyByteBuf buffer) {
        return new CTriggerAbortConfigPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender != null)
                TriggerSelectionServer.endConfiguration(sender);
        });

        context.get().setPacketHandled(true);
        return true;
    }
}