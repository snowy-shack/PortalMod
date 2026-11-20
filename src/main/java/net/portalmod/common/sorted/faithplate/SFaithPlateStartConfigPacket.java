package net.portalmod.common.sorted.faithplate;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class SFaithPlateStartConfigPacket implements AbstractPacket<SFaithPlateStartConfigPacket> {
    protected BlockPos pos;

    public SFaithPlateStartConfigPacket() {}

    public SFaithPlateStartConfigPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public SFaithPlateStartConfigPacket decode(FriendlyByteBuf buffer) {
        return new SFaithPlateStartConfigPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> ClientOnly.run(() -> () ->
                FaithPlateClient.setScreen(pos)
        ));

        context.get().setPacketHandled(true);
        return true;
    }
}