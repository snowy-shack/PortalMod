package net.portalmod.common.sorted.antline;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class SAntlineUpdatePacket implements AbstractPacket<SAntlineUpdatePacket> {
    private BlockPos pos;
    private CompoundTag nbt;

    public SAntlineUpdatePacket() {}

    public SAntlineUpdatePacket(BlockPos pos, CompoundTag nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }
    
    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos)
                .writeNbt(nbt);
    }

    @Override
    public SAntlineUpdatePacket decode(FriendlyByteBuf buffer) {
        return new SAntlineUpdatePacket(buffer.readBlockPos(), buffer.readNbt());
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            ClientOnly.run(() -> () -> {
                AntlineTileEntity blockEntity = (AntlineTileEntity) Minecraft.getInstance().level.getBlockEntity(pos);
                if(blockEntity == null) return;
                blockEntity.load(nbt, false);
                Minecraft.getInstance().level.sendBlockUpdated(pos, BlockInit.ANTLINE.get().defaultBlockState(),
                        BlockInit.ANTLINE.get().defaultBlockState(), 0);
                blockEntity.requestModelDataUpdate();
            });
        });

        context.get().setPacketHandled(true);
        return true;
    }
}