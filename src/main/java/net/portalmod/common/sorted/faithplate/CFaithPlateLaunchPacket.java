package net.portalmod.common.sorted.faithplate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.portalmod.core.init.BlockInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.packet.AbstractPacket;

import java.util.function.Supplier;

public class CFaithPlateLaunchPacket implements AbstractPacket<CFaithPlateLaunchPacket> {
    private BlockPos pos;

    public CFaithPlateLaunchPacket() {}

    public CFaithPlateLaunchPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public CFaithPlateLaunchPacket decode(PacketBuffer buffer) {
        return new CFaithPlateLaunchPacket(buffer.readBlockPos());
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            World level = context.get().getSender().level;
            FaithPlateTileEntity be = (FaithPlateTileEntity)level.getBlockEntity(pos);
            if(be == null)
                return;

            PacketInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(
                () -> context.get().getSender()), new SFaithPlateLaunchPacket(pos));

//            if(new Vector3d(pos.getX(), pos.getY(), pos.getZ())
//                    .distanceTo(context.get().getSender().getPosition(0)) > 3)
//                return;

//            FaithPlateTER ter = (FaithPlateTER)TileEntityRendererDispatcher.instance.getRenderer(be);
//            ((FaithPlatePlateModel)ter.getPlateModel()).startAnimation(be, "launch");
//            level.playSound(Minecraft.getInstance().player,
//                pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
//                    SoundInit.FAITHPLATE_LAUNCH.get(), SoundCategory.BLOCKS, .1f, 1);

        });

        context.get().setPacketHandled(true);
        return true;
    }
}