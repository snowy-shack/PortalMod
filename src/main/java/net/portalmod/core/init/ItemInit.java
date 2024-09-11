package net.portalmod.core.init;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
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
import net.portalmod.common.items.TooltipItem;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.common.sorted.antline.AntlineBlockItem;
import net.portalmod.common.sorted.gel.container.EmptyGelContainer;
import net.portalmod.common.sorted.gel.container.GelContainer;
import net.portalmod.common.sorted.longfallboots.LongFallBoots;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunISTER;
import net.portalmod.core.PortalModTab;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PortalMod.MODID);
//    public static final Registry<SpawnEggItem> SPAWN_EGGS = new Registry<>();

    private ItemInit() {}
    private static final Item.Properties GEL_BASE = new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE);

    // Fundamental Items
    public static final RegistryObject<Item> PORTALGUN = ITEMS.register("portalgun",
            () -> new PortalGun(new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)
                    .setISTER(() -> PortalGunISTER::new)));
    public static final RegistryObject<Item> LONGFALL_BOOTS = ITEMS.register("longfall_boots",
            () -> new LongFallBoots(ArmorMaterialInit.LONGFALL_BOOTS, EquipmentSlotType.FEET,
                    new Item.Properties().tab(PortalModTab.INSTANCE).fireResistant()));
    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));

    // Cubes
    public static final RegistryObject<Item> COMPANION_CUBE = registerSpawnEgg("companion_cube", EntityInit.COMPANION_CUBE, "cube");
    public static final RegistryObject<Item> STORAGE_CUBE = registerSpawnEgg("storage_cube", EntityInit.STORAGE_CUBE, "cube");
    public static final RegistryObject<Item> VINTAGE_CUBE = registerSpawnEgg("vintage_cube", EntityInit.VINTAGE_CUBE, "cube");
    public static final RegistryObject<Item> CUBE_DROPPER = registerBlockItem("cube_dropper", BlockInit.CUBE_DROPPER);

    // Test Elements
    public static final RegistryObject<Item> SUPER_BUTTON = registerBlockItem("super_button", BlockInit.SUPER_BUTTON);
    public static final RegistryObject<Item> STANDING_BUTTON = registerBlockItem("standing_button", BlockInit.STANDING_BUTTON);

    public static final RegistryObject<Item> ANTLINE = ITEMS.register("antline",
            () -> new AntlineBlockItem(BlockInit.ANTLINE.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<Item> ANTLINE_INDICATOR = registerBlockItem("antline_indicator", BlockInit.ANTLINE_INDICATOR);
    public static final RegistryObject<Item> CHAMBER_DOOR = registerBlockItem("chamber_door", BlockInit.CHAMBER_DOOR);
    public static final RegistryObject<Item> FIZZLER_EMITTER = registerBlockItem("fizzler_emitter", BlockInit.FIZZLER_EMITTER);
    public static final RegistryObject<Item> FAITHPLATE = registerBlockItem("faithplate", BlockInit.FAITHPLATE);

    public static final RegistryObject<Item> CHAMBER_LIGHTS = ITEMS.register("chamber_lights",
            () -> new BlockItem(BlockInit.CHAMBER_LIGHTS.get(),
                    new Item.Properties().tab(PortalModTab.INSTANCE)
                            .setISTER(() -> ISTERWrapper::new))); // todo remove ister

    public static final RegistryObject<Item> GOO_BUCKET = ITEMS.register("goo_bucket",
            () -> new BucketItem(() -> FluidInit.GOO_FLUID.get(), new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<Item> CONTAINER = ITEMS.register("container",
            () -> new EmptyGelContainer(new Item.Properties().stacksTo(16).tab(PortalModTab.INSTANCE)));

    public static final RegistryObject<Item> REPULSION_GEL = ITEMS.register("repulsion_gel",
            () -> new GelContainer(BlockInit.REPULSION_GEL.get(), GEL_BASE, 0x3776F1));

    public static final RegistryObject<Item> PROPULSION_GEL = ITEMS.register("propulsion_gel",
            () -> new GelContainer(BlockInit.PROPULSION_GEL.get(), GEL_BASE, 0xE3834A));

    public static final RegistryObject<Item> CONVERSION_GEL = ITEMS.register("conversion_gel",
            () -> new GelContainer(BlockInit.CONVERSION_GEL.get(), GEL_BASE, 0xF7F7F8));

    public static final RegistryObject<Item> TURRET = registerSpawnEgg("turret", EntityInit.TURRET, "turret");

    public static final RegistryObject<Item> BULLETS = ITEMS.register("bullets",
            () -> new TooltipItem(new Item.Properties().tab(PortalModTab.INSTANCE), "bullets"));

    public static final RegistryObject<Item> RADIO = registerBlockItem("radio", BlockInit.RADIO);

    public static final RegistryObject<Item> FOREST_CAKE = ITEMS.register("forest_cake",
            () -> new BlockItem(BlockInit.FOREST_CAKE.get(), new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE)));

    // Potential Future Items
//    public static final RegistryObject<BlockItem> ADHESION_GEL = ITEMS.register("adhesion_gel",
//            () -> new GelContainer(BlockInit.ADHESION_GEL.get(), GEL_BASE, 0x954CC0));
//    public static final RegistryObject<BlockItem> LASER_EMITTER = ITEMS.register("laser_emitter",
//            () -> new BlockItem(BlockInit.LASER_EMITTER.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
//
//    public static final RegistryObject<BlockItem> LASER_CATCHER = ITEMS.register("laser_catcher",
//            () -> new BlockItem(BlockInit.LASER_CATCHER.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
//
//    public static final RegistryObject<BlockItem> LASER_RELAY = ITEMS.register("laser_relay",
//            () -> new BlockItem(BlockInit.LASER_RELAY.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));

    // Blocks
    public static final RegistryObject<Item> LUNECAST = registerBlockItem("lunecast", BlockInit.LUNECAST);
    public static final RegistryObject<Item> LUNECAST_SLAB = registerBlockItem("lunecast_slab", BlockInit.LUNECAST_SLAB);
    public static final RegistryObject<Item> LUNECAST_STAIRS = registerBlockItem("lunecast_stairs", BlockInit.LUNECAST_STAIRS);

    public static final RegistryObject<Item> ARBORED_LUNECAST = registerBlockItem("arbored_lunecast", BlockInit.ARBORED_LUNECAST);
    public static final RegistryObject<Item> ARBORED_LUNECAST_SLAB = registerBlockItem("arbored_lunecast_slab", BlockInit.ARBORED_LUNECAST_SLAB);
    public static final RegistryObject<Item> ARBORED_LUNECAST_STAIRS = registerBlockItem("arbored_lunecast_stairs", BlockInit.ARBORED_LUNECAST_STAIRS);

    public static final RegistryObject<Item> ERODED_LUNECAST = registerBlockItem("eroded_lunecast", BlockInit.ERODED_LUNECAST);
    public static final RegistryObject<Item> ERODED_LUNECAST_SLAB = registerBlockItem("eroded_lunecast_slab", BlockInit.ERODED_LUNECAST_SLAB);
    public static final RegistryObject<Item> ERODED_LUNECAST_STAIRS = registerBlockItem("eroded_lunecast_stairs", BlockInit.ERODED_LUNECAST_STAIRS);

    public static final RegistryObject<Item> FRACTURED_LUNECAST = registerBlockItem("fractured_lunecast", BlockInit.FRACTURED_LUNECAST);
    public static final RegistryObject<Item> FRACTURED_LUNECAST_SLAB = registerBlockItem("fractured_lunecast_slab", BlockInit.FRACTURED_LUNECAST_SLAB);
    public static final RegistryObject<Item> FRACTURED_LUNECAST_STAIRS = registerBlockItem("fractured_lunecast_stairs", BlockInit.FRACTURED_LUNECAST_STAIRS);

    public static final RegistryObject<Item> VINTAGE_LUNECAST = registerBlockItem("vintage_lunecast", BlockInit.VINTAGE_LUNECAST);
    public static final RegistryObject<Item> VINTAGE_LUNECAST_SLAB = registerBlockItem("vintage_lunecast_slab", BlockInit.VINTAGE_LUNECAST_SLAB);
    public static final RegistryObject<Item> VINTAGE_LUNECAST_STAIRS = registerBlockItem("vintage_lunecast_stairs", BlockInit.VINTAGE_LUNECAST_STAIRS);

    public static final RegistryObject<Item> BLACKPLATE = registerBlockItem("blackplate", BlockInit.BLACKPLATE);
    public static final RegistryObject<Item> BLACKPLATE_SLAB = registerBlockItem("blackplate_slab", BlockInit.BLACKPLATE_SLAB);
    public static final RegistryObject<Item> BLACKPLATE_STAIRS = registerBlockItem("blackplate_stairs", BlockInit.BLACKPLATE_STAIRS);

    public static final RegistryObject<Item> ARBORED_BLACKPLATE = registerBlockItem("arbored_blackplate", BlockInit.ARBORED_BLACKPLATE);
    public static final RegistryObject<Item> ARBORED_BLACKPLATE_SLAB = registerBlockItem("arbored_blackplate_slab", BlockInit.ARBORED_BLACKPLATE_SLAB);
    public static final RegistryObject<Item> ARBORED_BLACKPLATE_STAIRS = registerBlockItem("arbored_blackplate_stairs", BlockInit.ARBORED_BLACKPLATE_STAIRS);

    public static final RegistryObject<Item> ERODED_BLACKPLATE = registerBlockItem("eroded_blackplate", BlockInit.ERODED_BLACKPLATE);
    public static final RegistryObject<Item> ERODED_BLACKPLATE_SLAB = registerBlockItem("eroded_blackplate_slab", BlockInit.ERODED_BLACKPLATE_SLAB);
    public static final RegistryObject<Item> ERODED_BLACKPLATE_STAIRS = registerBlockItem("eroded_blackplate_stairs", BlockInit.ERODED_BLACKPLATE_STAIRS);

    public static final RegistryObject<Item> FRACTURED_BLACKPLATE = registerBlockItem("fractured_blackplate", BlockInit.FRACTURED_BLACKPLATE);
    public static final RegistryObject<Item> FRACTURED_BLACKPLATE_SLAB = registerBlockItem("fractured_blackplate_slab", BlockInit.FRACTURED_BLACKPLATE_SLAB);
    public static final RegistryObject<Item> FRACTURED_BLACKPLATE_STAIRS = registerBlockItem("fractured_blackplate_stairs", BlockInit.FRACTURED_BLACKPLATE_STAIRS);

    public static final RegistryObject<Item> VINTAGE_BLACKPLATE = registerBlockItem("vintage_blackplate", BlockInit.VINTAGE_BLACKPLATE);
    public static final RegistryObject<Item> VINTAGE_BLACKPLATE_SLAB = registerBlockItem("vintage_blackplate_slab", BlockInit.VINTAGE_BLACKPLATE_SLAB);
    public static final RegistryObject<Item> VINTAGE_BLACKPLATE_STAIRS = registerBlockItem("vintage_blackplate_stairs", BlockInit.VINTAGE_BLACKPLATE_STAIRS);

    // Decoration blocks
    public static final RegistryObject<Item> STEP_PILLAR = registerBlockItem("step_pillar", BlockInit.STEP_PILLAR);
    public static final RegistryObject<Item> LUNECAST_STEP = registerBlockItem("lunecast_step", BlockInit.LUNECAST_STEP);
    public static final RegistryObject<Item> RUSTY_LUNECAST_STEP = registerBlockItem("rusty_lunecast_step", BlockInit.RUSTY_LUNECAST_STEP);
    public static final RegistryObject<Item> BLACKPLATE_STEP = registerBlockItem("blackplate_step", BlockInit.BLACKPLATE_STEP);
    public static final RegistryObject<Item> RUSTY_BLACKPLATE_STEP = registerBlockItem("rusty_blackplate_step", BlockInit.RUSTY_BLACKPLATE_STEP);

    public static final RegistryObject<Item> WIRE_MESH_BLOCK = registerBlockItem("wire_mesh_block", BlockInit.WIRE_MESH_BLOCK);
    public static final RegistryObject<Item> WIRE_MESH = registerBlockItem("wire_mesh", BlockInit.WIRE_MESH);
    public static final RegistryObject<Item> IRON_FRAME = registerBlockItem("iron_frame", BlockInit.IRON_FRAME);
    public static final RegistryObject<Item> BARRED_IRON_FRAME = registerBlockItem("barred_iron_frame", BlockInit.BARRED_IRON_FRAME);
    public static final RegistryObject<Item> MESHED_IRON_FRAME = registerBlockItem("meshed_iron_frame", BlockInit.MESHED_IRON_FRAME);
    public static final RegistryObject<Item> RUSTY_IRON_FRAME = registerBlockItem("rusty_iron_frame", BlockInit.RUSTY_IRON_FRAME);
    public static final RegistryObject<Item> RUSTY_BARRED_IRON_FRAME = registerBlockItem("rusty_barred_iron_frame", BlockInit.RUSTY_BARRED_IRON_FRAME);
    public static final RegistryObject<Item> RUSTY_MESHED_IRON_FRAME = registerBlockItem("rusty_meshed_iron_frame", BlockInit.RUSTY_MESHED_IRON_FRAME);

    // Test Items, remove later
    public static final RegistryObject<Item> TEST_BLOCK = registerBlockItem("test_block", BlockInit.TEST_BLOCK);
    public static final RegistryObject<Item> GABE = registerSpawnEgg("gabe", EntityInit.GABE);
    public static final RegistryObject<Item> FIZZLER_FIELD = registerBlockItem("fizzler_field", BlockInit.FIZZLER_FIELD);

    public static RegistryObject<Item> registerBlockItem(String name, RegistryObject<Block> block) {
        return ItemInit.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(PortalModTab.INSTANCE)));
    }

    public static RegistryObject<Item> registerSpawnEgg(String name, RegistryObject<? extends EntityType<?>> entity, String tooltip) {
        return ITEMS.register(name, () -> new ModSpawnEggItem(entity,new Item.Properties().stacksTo(1).tab(PortalModTab.INSTANCE), tooltip));
    }

    public static RegistryObject<Item> registerSpawnEgg(String name, RegistryObject<? extends EntityType<?>> entity) {
        return registerSpawnEgg(name, entity, null);
    }

}