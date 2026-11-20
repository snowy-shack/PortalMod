package net.portalmod.common.sorted.faithplate;

import java.util.function.Supplier;

import net.portalmod.core.network.PacketContext;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.packet.AbstractPacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class CFaithPlateUpdatedPacket implements AbstractPacket<CFaithPlateUpdatedPacket> {
    private BlockPos pos;
    private CompoundTag nbt;

    public CFaithPlateUpdatedPacket() {}

    public CFaithPlateUpdatedPacket(BlockPos pos, CompoundTag nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos)
                .writeNbt(nbt);
    }

    @Override
    public CFaithPlateUpdatedPacket decode(FriendlyByteBuf buffer) {
        return new CFaithPlateUpdatedPacket(buffer.readBlockPos(), buffer.readNbt());
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            FaithPlateTileEntity blockEntity = (FaithPlateTileEntity) context.get().getSender().level().getBlockEntity(pos);
            if(blockEntity == null)
                return;
            blockEntity.load(nbt);
            context.get().getSender().level().sendBlockUpdated(pos, BlockInit.FAITHPLATE.get().defaultBlockState(),
                    BlockInit.FAITHPLATE.get().defaultBlockState(), 3);
        });
        
        context.get().setPacketHandled(true);
        return true;
    }
}