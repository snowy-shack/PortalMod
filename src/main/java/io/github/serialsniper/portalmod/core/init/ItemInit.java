package io.github.serialsniper.portalmod.core.init;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.items.armor.LongFallBoots;
import io.github.serialsniper.portalmod.core.PortalTab;
import io.github.serialsniper.portalmod.common.items.AntlineBlockItem;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.common.items.RadioBlockItem;
import io.github.serialsniper.portalmod.common.items.WrenchItem;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.registries.*;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PortalMod.MODID);
    public static final RegistryObject<PortalGun> PORTALGUN = ITEMS.register("portalgun",
            () -> new PortalGun(new Item.Properties().stacksTo(1).tab(PortalTab.INSTANCE)));
    public static final RegistryObject<BlockItem> PORTALABLE_BLOCK = ITEMS.register("portalable_block",
            () -> new BlockItem(BlockInit.PORTALABLE_BLOCK.get(), new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<BlockItem> UNPORTALABLE_BLOCK = ITEMS.register("unportalable_block",
            () -> new BlockItem(BlockInit.UNPORTALABLE_BLOCK.get(), new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<RadioBlockItem> RADIO = ITEMS.register("radio",
            () -> new RadioBlockItem(BlockInit.RADIO.get(), new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<AntlineBlockItem> ANTLINE = ITEMS.register("antline",
            () -> new AntlineBlockItem(BlockInit.ANTLINE.get(), new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<BlockItem> FAITHPLATE = ITEMS.register("faithplate",
            () -> new BlockItem(BlockInit.FAITHPLATE.get(), new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<WrenchItem> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().stacksTo(1).tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> MOON_ROCK = ITEMS.register("moon_rock",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<LongFallBoots> LONGFALL_BOOTS = ITEMS.register("longfall_boots",
            () -> new LongFallBoots(ArmorMaterialInit.LONGFALL_BOOTS, EquipmentSlotType.FEET, new Item.Properties().tab(PortalTab.INSTANCE).fireResistant()));

    // todo change to blockItem
    public static final RegistryObject<Item> CAKE_LIE = ITEMS.register("cake_lie",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> CONTAINER = ITEMS.register("container",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> REPULSION_GEL = ITEMS.register("repulsion_gel",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> PROPULSION_GEL = ITEMS.register("propulsion_gel",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> ADHESION_GEL = ITEMS.register("adhesion_gel",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> CONVERSION_GEL = ITEMS.register("conversion_gel",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> STORAGE_CUBE = ITEMS.register("storage_cube",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> TURRET = ITEMS.register("turret",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> BULLETS = ITEMS.register("bullets",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    public static final RegistryObject<Item> FLOOR_BUTTON = ITEMS.register("floor_button",
            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
}