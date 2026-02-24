package net.portalmod.common.sorted.faithplate;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CFaithPlateEndConfigPacket implements AbstractPacket<CFaithPlateEndConfigPacket> {
    private BlockPos pos;

    public CFaithPlateEndConfigPacket() {}

    public CFaithPlateEndConfigPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public CFaithPlateEndConfigPacket decode(PacketBuffer buffer) {
        return new CFaithPlateEndConfigPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity sender = context.get().getSender();
            if(sender == null)
                return;

            FaithPlateTileEntity blockEntity = (FaithPlateTileEntity)sender.level.getBlockEntity(pos);
            if(blockEntity == null)
                return;

            blockEntity.endConfiguration();
        });
        
        context.get().setPacketHandled(true);
        return true;
    }
}