package net.portalmod.common.sorted.trigger;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;
import net.portalmod.core.packet.ClientPacketHandler;

import java.util.function.Supplier;

public class STriggerStartConfigPacket implements AbstractPacket<STriggerStartConfigPacket> {
    public BlockPos pos;

    public STriggerStartConfigPacket() {}

    public STriggerStartConfigPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public STriggerStartConfigPacket decode(PacketBuffer buffer) {
        return new STriggerStartConfigPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.handleSTriggerStartConfigPacket(this)));

        context.get().setPacketHandled(true);
        return true;
    }
}