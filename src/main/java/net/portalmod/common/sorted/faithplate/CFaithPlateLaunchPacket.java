package net.portalmod.common.sorted.faithplate;

import net.portalmod.core.util.ClientOnly;
import net.portalmod.core.network.PacketContext;
import net.portalmod.core.network.PacketDistributor;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class CFaithPlateLaunchPacket implements AbstractPacket<CFaithPlateLaunchPacket> {
    private BlockPos pos;

    public CFaithPlateLaunchPacket() {}

    public CFaithPlateLaunchPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public CFaithPlateLaunchPacket decode(FriendlyByteBuf buffer) {
        return new CFaithPlateLaunchPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            Level level = context.get().getSender().level();
            FaithPlateTileEntity be = (FaithPlateTileEntity)level.getBlockEntity(pos);
            if(be == null)
                return;

            PacketInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(
                () -> context.get().getSender()), new SFaithPlateLaunchPacket(pos));

//            if(new Vec3(pos.getX(), pos.getY(), pos.getZ())
//                    .distanceTo(context.get().getSender().getPosition(0)) > 3)
//                return;

//            FaithPlateTER ter = (FaithPlateTER)Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
//            ((FaithPlatePlateModel)ter.getPlateModel()).startAnimation(be, "launch");
//            level.playSound(Minecraft.getInstance().player,
//                pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
//                    SoundInit.FAITHPLATE_LAUNCH.get(), SoundSource.BLOCKS, .1f, 1);

        });

        context.get().setPacketHandled(true);
        return true;
    }
}