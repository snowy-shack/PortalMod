package net.portalmod.common.sorted.faithplate;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.packet.AbstractPacket;

public class CFaithPlateUpdatedPacket implements AbstractPacket<CFaithPlateUpdatedPacket> {
    private BlockPos pos;
    private CompoundNBT nbt;

    public CFaithPlateUpdatedPacket() {}

    public CFaithPlateUpdatedPacket(BlockPos pos, CompoundNBT nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos)
                .writeNbt(nbt);
    }

    @Override
    public CFaithPlateUpdatedPacket decode(PacketBuffer buffer) {
        return new CFaithPlateUpdatedPacket(buffer.readBlockPos(), buffer.readNbt());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            FaithPlateTileEntity blockEntity = (FaithPlateTileEntity) context.get().getSender().level.getBlockEntity(pos);
            if(blockEntity == null)
                return;
            blockEntity.load(nbt);
            context.get().getSender().level.sendBlockUpdated(pos, BlockInit.FAITHPLATE.get().defaultBlockState(),
                    BlockInit.FAITHPLATE.get().defaultBlockState(), 3);
        });
        
        context.get().setPacketHandled(true);
        return true;
    }
}