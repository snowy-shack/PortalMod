package net.portalmod.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.portalmod.core.init.BlockInit;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.GrassColor;
import net.minecraft.client.renderer.BiomeColors;

// https://forums.minecraftforge.net/topic/88237-11441152-solved-custom-leaf-help/?do=findComment&comment=412393
public class BlockColorHandler {
    @Environment(EnvType.CLIENT)
    @SubscribeEvent
    public void registerBlockColorHandlers(final ColorHandlerEvent.Block event) {
        final BlockColor blockColorHandler = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                return BiomeColors.getAverageFoliageColor(blockAccess, pos);
            }
            return GrassColor.get(.5D, 1D);
        };

        event.getBlockColors().register(blockColorHandler,
                BlockInit.ARBORED_BLACKPLATE.get(),
                BlockInit.ARBORED_BLACKPLATE_SLAB.get(),
                BlockInit.ARBORED_BLACKPLATE_STAIRS.get(),
                BlockInit.ARBORED_BLACKPLATE_PLATFORM.get()
        );
    }

    @Environment(EnvType.CLIENT)
    @SubscribeEvent
    public void registerItemColorHandlers(final ColorHandlerEvent.Item event) {
        final ItemColor blockItemColorHandler = (stack, tintIndex) -> {
            BlockState blockState = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            return event.getBlockColors().getColor(blockState, null, null, tintIndex);
        };

        event.getItemColors().register(blockItemColorHandler,
                BlockInit.ARBORED_BLACKPLATE.get(),
                BlockInit.ARBORED_BLACKPLATE_SLAB.get(),
                BlockInit.ARBORED_BLACKPLATE_STAIRS.get(),
                BlockInit.ARBORED_BLACKPLATE_PLATFORM.get()
        );
    }
}
