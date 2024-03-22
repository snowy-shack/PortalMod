package net.portalmod.common.sorted.radio;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class SRadioUpdatePacket implements AbstractPacket<SRadioUpdatePacket> {
    private BlockPos pos;
    private RadioState state;

    public SRadioUpdatePacket() {}

    public SRadioUpdatePacket(BlockPos pos, RadioState state) {
        this.pos = pos;
        this.state = state;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos)
                .writeEnum(state);
    }

    @Override
    public SRadioUpdatePacket decode(PacketBuffer buffer) {
        return new SRadioUpdatePacket(buffer.readBlockPos(), buffer.readEnum(RadioState.class));
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                RadioBlockTileEntity blockEntity = (RadioBlockTileEntity)Minecraft.getInstance().level.getBlockEntity(pos);
                if(blockEntity != null)
                    blockEntity.handlePacket(state);
            });
        });

        context.get().setPacketHandled(true);
        return true;
    }
}