package net.portalmod.common.sorted.antline;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class SAntlineUpdatePacket implements AbstractPacket<SAntlineUpdatePacket> {
    private BlockPos pos;
    private CompoundNBT nbt;

    public SAntlineUpdatePacket() {}

    public SAntlineUpdatePacket(BlockPos pos, CompoundNBT nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }
    
    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos)
                .writeNbt(nbt);
    }

    @Override
    public SAntlineUpdatePacket decode(PacketBuffer buffer) {
        return new SAntlineUpdatePacket(buffer.readBlockPos(), buffer.readNbt());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
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