package net.portalmod.common.sorted.radio;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class SRadioUpdatePacket implements AbstractPacket<SRadioUpdatePacket> {
    private BlockPos pos;
    private RadioState state;

    public SRadioUpdatePacket() {}

    public SRadioUpdatePacket(BlockPos pos, RadioState state) {
        this.pos = pos;
        this.state = state;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos)
                .writeEnum(state);
    }

    @Override
    public SRadioUpdatePacket decode(FriendlyByteBuf buffer) {
        return new SRadioUpdatePacket(buffer.readBlockPos(), buffer.readEnum(RadioState.class));
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ClientOnly.run(() -> () -> {
                RadioBlockTileEntity blockEntity = (RadioBlockTileEntity)Minecraft.getInstance().level.getBlockEntity(pos);
                if(blockEntity != null)
                    blockEntity.handlePacket(state);
            });
        });

        context.get().setPacketHandled(true);
        return true;
    }
}