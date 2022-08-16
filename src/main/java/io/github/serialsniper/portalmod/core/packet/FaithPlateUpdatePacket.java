package io.github.serialsniper.portalmod.core.packet;

import java.util.function.Supplier;

import io.github.serialsniper.portalmod.common.blockentities.FaithPlateTileEntity;
import io.github.serialsniper.portalmod.core.init.BlockInit;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class FaithPlateUpdatePacket {
    public static class Server implements AbstractPacket {
        private final BlockPos pos;
        private final CompoundNBT nbt;

        public Server(BlockPos pos, CompoundNBT nbt) {
            this.pos = pos;
            this.nbt = nbt;
        }

        public Server(PacketBuffer buffer) {
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
                FaithPlateTileEntity blockEntity = (FaithPlateTileEntity) context.get().getSender().level.getBlockEntity(pos);
                if(blockEntity == null) return;
                blockEntity.load(nbt);
                context.get().getSender().level.sendBlockUpdated(pos, BlockInit.FAITHPLATE.get().defaultBlockState(),
                        BlockInit.FAITHPLATE.get().defaultBlockState(), 3);
            });

            context.get().setPacketHandled(true);
            return true;
        }
    }

//    public static class Client implements AbstractPacket {
//        private final BlockPos pos;
//        private final CompoundNBT nbt;
//
//        public Client(BlockPos pos, CompoundNBT nbt) {
//            this.pos = pos;
//            this.nbt = nbt;
//        }
//
//        public Client(PacketBuffer buffer) {
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
//                    FaithPlateTileEntity blockEntity = (FaithPlateTileEntity) Minecraft.getInstance().level.getBlockEntity(pos);
//                    if(blockEntity == null) return;
////                    blockEntity.load(nbt);
//                    Minecraft.getInstance().level.sendBlockUpdated(pos, BlockInit.FAITHPLATE.get().defaultBlockState(),
//                            BlockInit.FAITHPLATE.get().defaultBlockState(), 0);
////                    blockEntity.requestModelDataUpdate();
//                });
//            });
//
//            context.get().setPacketHandled(true);
//            return true;
//        }
//    }
}