package net.portalmod.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.BlockItem;
import net.minecraft.world.GrassColors;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.portalmod.core.init.BlockInit;

// https://forums.minecraftforge.net/topic/88237-11441152-solved-custom-leaf-help/?do=findComment&comment=412393
public class BlockColorHandler {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerBlockColorHandlers(final ColorHandlerEvent.Block event) {
        final IBlockColor blockColorHandler = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                return BiomeColors.getAverageFoliageColor(blockAccess, pos);
            }
            return GrassColors.get(.5D, 1D);
        };

        event.getBlockColors().register(blockColorHandler,
                BlockInit.ARBORED_BLACKPLATE.get(),
                BlockInit.ARBORED_BLACKPLATE_SLAB.get(),
                BlockInit.ARBORED_BLACKPLATE_STAIRS.get()
        );
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerItemColorHandlers(final ColorHandlerEvent.Item event) {
        final IItemColor blockItemColorHandler = (stack, tintIndex) -> {
            BlockState blockState = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            return event.getBlockColors().getColor(blockState, null, null, tintIndex);
        };

        event.getItemColors().register(blockItemColorHandler,
                BlockInit.ARBORED_BLACKPLATE.get(),
                BlockInit.ARBORED_BLACKPLATE_SLAB.get(),
                BlockInit.ARBORED_BLACKPLATE_STAIRS.get()
        );
    }
}
