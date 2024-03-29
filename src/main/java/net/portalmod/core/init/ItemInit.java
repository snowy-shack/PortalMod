package net.portalmod.core.init;

import net.minecraft.block.Block;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.blocks.ISTERWrapper;
import net.portalmod.common.items.ModSpawnEggItem;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineBlockItem;
import net.portalmod.common.sorted.gel.container.EmptyGelContainer;
import net.portalmod.common.sorted.gel.container.GelContainer;
import net.portalmod.common.sorted.longfallboots.LongFallBoots;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunISTER;
import net.portalmod.common.sorted.radio.RadioBlockItem;
import net.portalmod.core.PortalModTab;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PortalMod.MODID);
//    public static final Registry<SpawnEggItem> SPAWN_EGGS = new Registry<>();

    private ItemInit() {}
    
    public static final RegistryObject<Item> PORTALGUN = ITEMS.register("portalgun",
            () -> new PortalGun(new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)
                    .setISTER(() -> PortalGunISTER::new)));
    
    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));

//    public static final RegistryObject<Item> MOON_ROCK = ITEMS.register("moon_rock",
//            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    
    public static final RegistryObject<Item> LONGFALL_BOOTS = ITEMS.register("longfall_boots",
            () -> new LongFallBoots(ArmorMaterialInit.LONGFALL_BOOTS, EquipmentSlotType.FEET,
                    new Item.Properties().tab(PortalModTab.INSTANCE).fireResistant()));
    
    // BLOCK ITEMS

    public static final RegistryObject<BlockItem> LUNECAST = registerBlockItem("lunecast", BlockInit.LUNECAST);
    public static final RegistryObject<BlockItem> BLACKPLATE = registerBlockItem("blackplate", BlockInit.BLACKPLATE);
    public static final RegistryObject<BlockItem> ARBORED_LUNECAST = registerBlockItem("arbored_lunecast", BlockInit.ARBORED_LUNECAST);
    public static final RegistryObject<BlockItem> ARBORED_BLACKPLATE = registerBlockItem("arbored_blackplate", BlockInit.ARBORED_BLACKPLATE);
    public static final RegistryObject<BlockItem> ERODED_LUNECAST = registerBlockItem("eroded_lunecast", BlockInit.ERODED_LUNECAST);
    public static final RegistryObject<BlockItem> ERODED_BLACKPLATE = registerBlockItem("eroded_blackplate", BlockInit.ERODED_BLACKPLATE);
    public static final RegistryObject<BlockItem> FRACTURED_LUNECAST = registerBlockItem("fractured_lunecast", BlockInit.FRACTURED_LUNECAST);
    public static final RegistryObject<BlockItem> FRACTURED_BLACKPLATE = registerBlockItem("fractured_blackplate", BlockInit.FRACTURED_BLACKPLATE);
    public static final RegistryObject<BlockItem> VINTAGE_LUNECAST = registerBlockItem("vintage_lunecast", BlockInit.VINTAGE_LUNECAST);
    public static final RegistryObject<BlockItem> VINTAGE_BLACKPLATE = registerBlockItem("vintage_blackplate", BlockInit.VINTAGE_BLACKPLATE);

    public static final RegistryObject<BlockItem> RADIO = ITEMS.register("radio",
            () -> new RadioBlockItem(BlockInit.RADIO.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
    
    public static final RegistryObject<BlockItem> WIRE_MESH_BLOCK = ITEMS.register("wire_mesh_block",
            () -> new BlockItem(BlockInit.WIRE_MESH_BLOCK.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<BlockItem> WIRE_MESH = ITEMS.register("wire_mesh",
            () -> new BlockItem(BlockInit.WIRE_MESH.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<BlockItem> IRON_FRAME = registerBlockItem("iron_frame", BlockInit.IRON_FRAME);
    public static final RegistryObject<BlockItem> BARRED_IRON_FRAME = registerBlockItem("barred_iron_frame", BlockInit.BARRED_IRON_FRAME);

    public static final RegistryObject<BlockItem> FOREST_CAKE = ITEMS.register("forest_cake",
            () -> new BlockItem(BlockInit.FOREST_CAKE.get(), new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));
    
    public static final RegistryObject<BlockItem> FAITHPLATE = ITEMS.register("faithplate",
            () -> new BlockItem(BlockInit.FAITHPLATE.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<BlockItem> CHAMBER_DOOR = ITEMS.register("chamber_door",
            () -> new BlockItem(BlockInit.CHAMBER_DOOR.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    // SPAWN EGGS
    
    public static final RegistryObject<ModSpawnEggItem> COMPANION_CUBE = ITEMS.register("companion_cube",
            () -> new ModSpawnEggItem(EntityInit.COMPANION_CUBE, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<ModSpawnEggItem> STORAGE_CUBE = ITEMS.register("storage_cube",
            () -> new ModSpawnEggItem(EntityInit.STORAGE_CUBE, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<ModSpawnEggItem> VINTAGE_CUBE = ITEMS.register("vintage_cube",
            () -> new ModSpawnEggItem(EntityInit.VINTAGE_CUBE, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<ModSpawnEggItem> TURRET = ITEMS.register("turret",
            () -> new ModSpawnEggItem(EntityInit.TURRET, 0xFFFFFF, 0xFFFFFF,
                    new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));
    
    // ANTLINE
    
    public static final RegistryObject<BlockItem> ANTLINE = ITEMS.register("antline",
            () -> new AntlineBlockItem(BlockInit.ANTLINE.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
    
    public static final RegistryObject<BlockItem> SUPER_BUTTON = ITEMS.register("super_button",
            () -> new BlockItem(BlockInit.SUPER_BUTTON.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
    
    public static final RegistryObject<BlockItem> ANTLINE_INDICATOR = ITEMS.register("antline_indicator",
            () -> new BlockItem(BlockInit.ANTLINE_INDICATOR.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
    
//    public static final RegistryObject<BlockItem> LASER_EMITTER = ITEMS.register("laser_emitter",
//            () -> new BlockItem(BlockInit.LASER_EMITTER.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
//
//    public static final RegistryObject<BlockItem> LASER_CATCHER = ITEMS.register("laser_catcher",
//            () -> new BlockItem(BlockInit.LASER_CATCHER.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
//
//    public static final RegistryObject<BlockItem> LASER_RELAY = ITEMS.register("laser_relay",
//            () -> new BlockItem(BlockInit.LASER_RELAY.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<BlockItem> FIZZLER_EMITTER = ITEMS.register("fizzler_emitter",
            () -> new BlockItem(BlockInit.FIZZLER_EMITTER.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<BlockItem> FIZZLER_FIELD = ITEMS.register("fizzler_field",
            () -> new BlockItem(BlockInit.FIZZLER_FIELD.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<BucketItem> GOO_BUCKET = ITEMS.register("goo_bucket",
            () -> new BucketItem(() -> FluidInit.GOO_FLUID.get(), new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));

    // GEL

    private static final Item.Properties GEL_BASE = new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE);

    // 0x29DD4D
    public static final RegistryObject<BlockItem> REPULSION_GEL = ITEMS.register("repulsion_gel",
            () -> new GelContainer(BlockInit.REPULSION_GEL.get(), GEL_BASE, 0x3776F1));
    
    public static final RegistryObject<BlockItem> PROPULSION_GEL = ITEMS.register("propulsion_gel",
            () -> new GelContainer(BlockInit.PROPULSION_GEL.get(), GEL_BASE, 0xE3834A));
    
//    public static final RegistryObject<BlockItem> ADHESION_GEL = ITEMS.register("adhesion_gel",
//            () -> new GelContainer(BlockInit.ADHESION_GEL.get(), GEL_BASE, 0x954CC0));
    
    public static final RegistryObject<BlockItem> CONVERSION_GEL = ITEMS.register("conversion_gel",
            () -> new GelContainer(BlockInit.CONVERSION_GEL.get(), GEL_BASE, 0xF7F7F8));
    
    public static final RegistryObject<Item> CONTAINER = ITEMS.register("container",
            () -> new EmptyGelContainer(new Item.Properties().stacksTo(16).tab(PortalModTab.INSTANCE)));

    // todo remove ister

    public static final RegistryObject<BlockItem> CHAMBER_LIGHTS = ITEMS.register("chamber_lights",
            () -> new BlockItem(BlockInit.CHAMBER_LIGHTS.get(),
                    new Item.Properties().tab(PortalModTab.INSTANCE)
                            .setISTER(() -> ISTERWrapper::new)));

//    public static final RegistryObject<Item> ADHESION_GEL = ITEMS.register("adhesion_gel",
//            () -> new Item(new Item.Properties().tab(PortalTab.INSTANCE)));
    
//    public static final RegistryObject<Item> STORAGE_CUBE = ITEMS.register("storage_cube",
//            () -> new Item(new Item.Properties().tab(PortalModTab.INSTANCE)));
    
//    public static final RegistryObject<Item> TURRET = ITEMS.register("turret",
//            () -> new Item(new Item.Properties().tab(PortalModTab.INSTANCE)));
    
    public static final RegistryObject<Item> BULLETS = ITEMS.register("bullets",
            () -> new Item(new Item.Properties().tab(PortalModTab.INSTANCE)));

    public static RegistryObject<BlockItem> registerBlockItem(String name, RegistryObject<Block> block) {
        return ItemInit.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
    }
}