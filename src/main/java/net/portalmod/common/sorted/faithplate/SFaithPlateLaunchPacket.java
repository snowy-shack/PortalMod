package net.portalmod.common.sorted.faithplate;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

public class SFaithPlateLaunchPacket implements AbstractPacket<SFaithPlateLaunchPacket> {
    protected BlockPos pos;

    public SFaithPlateLaunchPacket() {}

    public SFaithPlateLaunchPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public SFaithPlateLaunchPacket decode(PacketBuffer buffer) {
        return new SFaithPlateLaunchPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
            FaithPlateClient.handleLaunch(this, context)
        ));
        
        context.get().setPacketHandled(true);
        return true;
    }
}