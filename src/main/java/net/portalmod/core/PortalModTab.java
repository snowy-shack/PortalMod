package net.portalmod.core;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.portalmod.PortalMod;
import net.portalmod.core.init.ItemInit;

/**
 * The PortalMod creative tab. Since 1.19.3 tabs are registry entries and list their items explicitly,
 * so this shows every item in {@link ItemInit#ITEMS} in registration order.
 */
public final class PortalModTab {
    public static final ResourceKey<CreativeModeTab> KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(PortalMod.MODID, PortalMod.MODID));

    public static final CreativeModeTab INSTANCE = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + PortalMod.MODID))
            .icon(() -> new ItemStack(ItemInit.RADIO.get()))
            .displayItems((params, output) -> ItemInit.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build();

    private PortalModTab() {}

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, INSTANCE);
    }
}
