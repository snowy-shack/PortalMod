package net.portalmod.common.sorted.faithplate;

import java.util.function.Supplier;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class SFaithPlateLaunchPacket implements AbstractPacket<SFaithPlateLaunchPacket> {
    protected BlockPos pos;

    public SFaithPlateLaunchPacket() {}

    public SFaithPlateLaunchPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public SFaithPlateLaunchPacket decode(FriendlyByteBuf buffer) {
        return new SFaithPlateLaunchPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> ClientOnly.run(() -> () ->
            FaithPlateClient.handleLaunch(this, context)
        ));
        
        context.get().setPacketHandled(true);
        return true;
    }
}