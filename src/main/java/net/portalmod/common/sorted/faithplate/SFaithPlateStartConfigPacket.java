package net.portalmod.common.sorted.faithplate;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class SFaithPlateStartConfigPacket implements AbstractPacket<SFaithPlateStartConfigPacket> {
    protected BlockPos pos;

    public SFaithPlateStartConfigPacket() {}

    public SFaithPlateStartConfigPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public SFaithPlateStartConfigPacket decode(PacketBuffer buffer) {
        return new SFaithPlateStartConfigPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                FaithPlateClient.setScreen(pos)
        ));

        context.get().setPacketHandled(true);
        return true;
    }
}