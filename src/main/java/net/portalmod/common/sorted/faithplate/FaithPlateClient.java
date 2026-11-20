package net.portalmod.common.sorted.faithplate;

import net.portalmod.core.network.PacketContext;
import net.portalmod.core.init.SoundInit;
import net.portalmod.core.util.ModUtil;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class FaithPlateClient {
    protected static void handleLaunch(SFaithPlateLaunchPacket packet, Supplier<PacketContext> context) {
        Level level = Minecraft.getInstance().level;
        FaithPlateTileEntity be = (FaithPlateTileEntity)level.getBlockEntity(packet.pos);
        if(be == null)
            return;

        // todo animate only if successful

//                be.animStart = System.currentTimeMillis();
        FaithPlateTER ter = (FaithPlateTER) Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
        ((FaithPlatePlateModel)ter.getPlateModel()).startAnimation(be, "launch");
        level.playSound(Minecraft.getInstance().player,
                packet.pos.getX() + .5, packet.pos.getY() + .5, packet.pos.getZ() + .5,
                SoundInit.FAITHPLATE_LAUNCH.get(), SoundSource.BLOCKS, 1, ModUtil.randomSoundPitch());
    }

    protected static void setScreen(BlockPos pos) {
        Minecraft.getInstance().setScreen(new FaithPlateConfigScreen(pos));
    }
}