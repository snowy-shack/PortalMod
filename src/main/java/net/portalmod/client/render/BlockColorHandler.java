package net.portalmod.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.portalmod.core.init.BlockInit;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.GrassColor;
import net.minecraft.client.renderer.BiomeColors;

// https://forums.minecraftforge.net/topic/88237-11441152-solved-custom-leaf-help/?do=findComment&comment=412393
@Environment(EnvType.CLIENT)
public class BlockColorHandler {
    public static void register() {
        final BlockColor blockColorHandler = (state, blockAccess, pos, tintIndex) -> {
            if (blockAccess != null && pos != null) {
                return BiomeColors.getAverageFoliageColor(blockAccess, pos);
            }
            return GrassColor.get(.5D, 1D);
        };

        ColorProviderRegistry.BLOCK.register(blockColorHandler,
                BlockInit.ARBORED_BLACKPLATE.get(),
                BlockInit.ARBORED_BLACKPLATE_SLAB.get(),
                BlockInit.ARBORED_BLACKPLATE_STAIRS.get(),
                BlockInit.ARBORED_BLACKPLATE_PLATFORM.get()
        );

        final ItemColor blockItemColorHandler = (stack, tintIndex) -> {
            BlockState blockState = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
            return Minecraft.getInstance().getBlockColors().getColor(blockState, null, null, tintIndex);
        };

        ColorProviderRegistry.ITEM.register(blockItemColorHandler,
                BlockInit.ARBORED_BLACKPLATE.get(),
                BlockInit.ARBORED_BLACKPLATE_SLAB.get(),
                BlockInit.ARBORED_BLACKPLATE_STAIRS.get(),
                BlockInit.ARBORED_BLACKPLATE_PLATFORM.get()
        );
    }
}
