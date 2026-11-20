package net.portalmod.common.sorted.trigger;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;
import net.portalmod.core.packet.ClientPacketHandler;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class STriggerStartConfigPacket implements AbstractPacket<STriggerStartConfigPacket> {
    public BlockPos pos;

    public STriggerStartConfigPacket() {}

    public STriggerStartConfigPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public STriggerStartConfigPacket decode(FriendlyByteBuf buffer) {
        return new STriggerStartConfigPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> ClientOnly.run(() -> () -> ClientPacketHandler.handleSTriggerStartConfigPacket(this)));

        context.get().setPacketHandled(true);
        return true;
    }
}