package io.github.serialsniper.portalmod.core.packet;

import io.github.serialsniper.portalmod.common.blockentities.RadioBlockTileEntity;
import io.github.serialsniper.portalmod.core.enums.RadioState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RadioUpdateClientPacket implements AbstractPacket {
    private final BlockPos pos;
    private final RadioState state;

    public RadioUpdateClientPacket(BlockPos pos, RadioState state) {
        this.pos = pos;
        this.state = state;
    }

    public RadioUpdateClientPacket(PacketBuffer buffer) {
        this(buffer.readBlockPos(), buffer.readEnum(RadioState.class));
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos)
                .writeEnum(state);
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