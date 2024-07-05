package net.portalmod.common.sorted.faithplate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import java.util.function.Supplier;

public class FaithPlateClient {
    protected static void handleLaunch(SFaithPlateLaunchPacket packet, Supplier<NetworkEvent.Context> context) {
        World level = Minecraft.getInstance().level;
        FaithPlateTileEntity be = (FaithPlateTileEntity)level.getBlockEntity(packet.pos);
        if(be == null)
            return;

        // todo animate only if successful

//                be.animStart = System.currentTimeMillis();
        FaithPlateTER ter = (FaithPlateTER) TileEntityRendererDispatcher.instance.getRenderer(be);
        ((FaithPlatePlateModel)ter.getPlateModel()).startAnimation(be, "launch");
        level.playSound(Minecraft.getInstance().player,
                packet.pos.getX() + .5, packet.pos.getY() + .5, packet.pos.getZ() + .5,
                SoundInit.FAITHPLATE_LAUNCH.get(), SoundCategory.BLOCKS, 1, ModUtil.randomSoundPitch());
    }

    protected static void setScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new FaithPlateConfigScreen(pos));
    }
}