package net.portalmod.common.sorted.trigger;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CTriggerEndConfigPacket implements AbstractPacket<CTriggerEndConfigPacket> {
    private BlockPos pos;
    private BlockPos start;
    private BlockPos end;

    public CTriggerEndConfigPacket() {}

    public CTriggerEndConfigPacket(BlockPos pos, BlockPos start, BlockPos end) {
        this.pos = pos;
        this.start = start;
        this.end = end;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeBlockPos(this.start);
        buffer.writeBlockPos(this.end);
    }

    @Override
    public CTriggerEndConfigPacket decode(PacketBuffer buffer) {
        return new CTriggerEndConfigPacket(buffer.readBlockPos(), buffer.readBlockPos(), buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity sender = context.get().getSender();
            if(sender == null)
                return;

            TriggerTileEntity blockEntity = (TriggerTileEntity)sender.level.getBlockEntity(pos);
            if(blockEntity == null)
                return;

            blockEntity.setField(this.start, this.end);
            sender.level.sendBlockUpdated(pos, BlockInit.TRIGGER.get().defaultBlockState(),
                    BlockInit.TRIGGER.get().defaultBlockState(), 3);

            TriggerSelectionServer.endConfiguration(sender);
        });
        
        context.get().setPacketHandled(true);
        return true;
    }
}