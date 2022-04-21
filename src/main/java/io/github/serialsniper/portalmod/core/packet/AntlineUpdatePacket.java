package io.github.serialsniper.portalmod.core.packet;

import io.github.serialsniper.portalmod.common.blockentities.AntlineTileEntity;
import io.github.serialsniper.portalmod.core.init.BlockInit;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AntlineUpdatePacket {
//    public static class Server implements AbstractPacket {
//        private final BlockPos pos;
//        private final CompoundNBT nbt;
//
//        public Server(BlockPos pos, CompoundNBT nbt) {
//            this.pos = pos;
//            this.nbt = nbt;
//        }
//
//        public Server(PacketBuffer buffer) {
//            this(buffer.readBlockPos(), buffer.readNbt());
//        }
//
//        @Override
//        public void encode(PacketBuffer buffer) {
//            buffer.writeBlockPos(pos)
//                    .writeNbt(nbt);
//        }
//
//        @Override
//        public boolean handle(Supplier<NetworkEvent.Context> context) {
//            context.get().enqueueWork(() -> {
//                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//                    PacketInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
//                            () -> context.get().getSender().level.getChunkAt(pos)),
//                            new AntlineUpdatePacket.Client(pos, nbt));
//                });
//            });
//
//            context.get().setPacketHandled(true);
//            return true;
//        }
//    }

    public static class Client implements AbstractPacket {
        private final BlockPos pos;
        private final CompoundNBT nbt;

        public Client(BlockPos pos, CompoundNBT nbt) {
            this.pos = pos;
            this.nbt = nbt;
        }

        public Client(PacketBuffer buffer) {
            this(buffer.readBlockPos(), buffer.readNbt());
        }

        @Override
        public void encode(PacketBuffer buffer) {
            buffer.writeBlockPos(pos)
                    .writeNbt(nbt);
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
}