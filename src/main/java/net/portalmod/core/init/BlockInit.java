package net.portalmod.core.init;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.blocks.*;
import net.portalmod.common.sorted.antline.AntlineBlock;
import net.portalmod.common.sorted.antline.AntlineIndicatorBlock;
import net.portalmod.common.sorted.button.StandingButtonBlock;
import net.portalmod.common.sorted.button.SuperButtonBlock;
import net.portalmod.common.sorted.cubedropper.CubeDropperBlock;
import net.portalmod.common.sorted.door.ChamberDoorBlock;
import net.portalmod.common.sorted.faithplate.FaithPlateBlock;
import net.portalmod.common.sorted.fizzler.FizzlerEmitterBlock;
import net.portalmod.common.sorted.fizzler.FizzlerFieldBlock;
import net.portalmod.common.sorted.gel.ConversionGelBlock;
import net.portalmod.common.sorted.gel.PropulsionGelBlock;
import net.portalmod.common.sorted.gel.RepulsionGelBlock;
import net.portalmod.common.sorted.panel.LargePanelBlock;
import net.portalmod.common.sorted.panel.SmallPanelBlock;
import net.portalmod.common.sorted.radio.RadioBlock;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PortalMod.MODID);
    
    private BlockInit() {}
    
    public static final RegistryObject<Block> LUNECAST = registerLunecast("lunecast");
    public static final RegistryObject<Block> BLACKPLATE = registerBlackplate("blackplate");
    public static final RegistryObject<Block> ARBORED_LUNECAST = registerLunecast("arbored_lunecast");
    public static final RegistryObject<Block> ARBORED_BLACKPLATE = registerBlackplate("arbored_blackplate");
    public static final RegistryObject<Block> ERODED_LUNECAST = registerLunecast("eroded_lunecast");
    public static final RegistryObject<Block> ERODED_BLACKPLATE = registerBlackplate("eroded_blackplate");
    public static final RegistryObject<Block> FRACTURED_LUNECAST = registerLunecast("fractured_lunecast");
    public static final RegistryObject<Block> FRACTURED_BLACKPLATE = registerBlackplate("fractured_blackplate");
    public static final RegistryObject<Block> VINTAGE_LUNECAST = registerLunecast("vintage_lunecast");
    public static final RegistryObject<Block> VINTAGE_BLACKPLATE = registerBlackplate("vintage_blackplate");

    public static final RegistryObject<Block> LUNECAST_SLAB = registerSlab("lunecast");
    public static final RegistryObject<Block> BLACKPLATE_SLAB = registerSlab("blackplate");
    public static final RegistryObject<Block> ARBORED_LUNECAST_SLAB = registerSlab("arbored_lunecast");
    public static final RegistryObject<Block> ARBORED_BLACKPLATE_SLAB = registerSlab("arbored_blackplate");
    public static final RegistryObject<Block> ERODED_LUNECAST_SLAB = registerSlab("eroded_lunecast");
    public static final RegistryObject<Block> ERODED_BLACKPLATE_SLAB = registerSlab("eroded_blackplate");
    public static final RegistryObject<Block> FRACTURED_LUNECAST_SLAB = registerSlab("fractured_lunecast");
    public static final RegistryObject<Block> FRACTURED_BLACKPLATE_SLAB = registerSlab("fractured_blackplate");
    public static final RegistryObject<Block> VINTAGE_LUNECAST_SLAB = registerSlab("vintage_lunecast");
    public static final RegistryObject<Block> VINTAGE_BLACKPLATE_SLAB = registerSlab("vintage_blackplate");

    public static final RegistryObject<Block> LUNECAST_STAIRS = registerStairs("lunecast");
    public static final RegistryObject<Block> BLACKPLATE_STAIRS = registerStairs("blackplate");
    public static final RegistryObject<Block> ARBORED_LUNECAST_STAIRS = registerStairs("arbored_lunecast");
    public static final RegistryObject<Block> ARBORED_BLACKPLATE_STAIRS = registerStairs("arbored_blackplate");
    public static final RegistryObject<Block> ERODED_LUNECAST_STAIRS = registerStairs("eroded_lunecast");
    public static final RegistryObject<Block> ERODED_BLACKPLATE_STAIRS = registerStairs("eroded_blackplate");
    public static final RegistryObject<Block> FRACTURED_LUNECAST_STAIRS = registerStairs("fractured_lunecast");
    public static final RegistryObject<Block> FRACTURED_BLACKPLATE_STAIRS = registerStairs("fractured_blackplate");
    public static final RegistryObject<Block> VINTAGE_LUNECAST_STAIRS = registerStairs("vintage_lunecast");
    public static final RegistryObject<Block> VINTAGE_BLACKPLATE_STAIRS = registerStairs("vintage_blackplate");
    
    public static final RegistryObject<Block> STEP_PILLAR = BLOCKS.register("step_pillar",
            () -> new StepPillarBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));

    public static final RegistryObject<Block> LUNECAST_STEP = BLOCKS.register("lunecast_step",
            () -> new StepBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));

    public static final RegistryObject<Block> RUSTY_LUNECAST_STEP = BLOCKS.register("rusty_lunecast_step",
            () -> new StepBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));

    public static final RegistryObject<Block> BLACKPLATE_STEP = BLOCKS.register("blackplate_step",
            () -> new StepBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));

    public static final RegistryObject<Block> RUSTY_BLACKPLATE_STEP = BLOCKS.register("rusty_blackplate_step",
            () -> new StepBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));

    public static final RegistryObject<Block> IRON_FRAME = BLOCKS.register("iron_frame",
            () -> new FrameBlock(AbstractBlock.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), false));

    public static final RegistryObject<Block> BARRED_IRON_FRAME = BLOCKS.register("barred_iron_frame",
            () -> new FrameBlock(AbstractBlock.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), true));

    public static final RegistryObject<Block> MESHED_IRON_FRAME = BLOCKS.register("meshed_iron_frame",
            () -> new FrameBlock(AbstractBlock.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), true));

    public static final RegistryObject<Block> RUSTY_IRON_FRAME = BLOCKS.register("rusty_iron_frame",
            () -> new FrameBlock(AbstractBlock.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), false));

    public static final RegistryObject<Block> RUSTY_BARRED_IRON_FRAME = BLOCKS.register("rusty_barred_iron_frame",
            () -> new FrameBlock(AbstractBlock.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), true));

    public static final RegistryObject<Block> RUSTY_MESHED_IRON_FRAME = BLOCKS.register("rusty_meshed_iron_frame",
            () -> new FrameBlock(AbstractBlock.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), true));
    public static final RegistryObject<Block> RADIO = BLOCKS.register("radio",
            () -> new RadioBlock(AbstractBlock.Properties.of(Material.DECORATION).strength(1.0F)));
    
    public static final RegistryObject<Block> FAITHPLATE = BLOCKS.register("faithplate",
            () -> new FaithPlateBlock(AbstractBlock.Properties.copy(Blocks.STONE)));
    
    public static final RegistryObject<Block> WIRE_MESH_BLOCK = BLOCKS.register("wire_mesh_block",
            //() -> new IronGratingBlock(AbstractBlock.Properties.copy(Blocks.CHAIN)));
            () -> new WireMeshBlock(AbstractBlock.Properties.copy(Blocks.CHAIN)));

    public static final RegistryObject<Block> WIRE_MESH = BLOCKS.register("wire_mesh",
            //() -> new IronGratingBlock(AbstractBlock.Properties.copy(Blocks.CHAIN)));
            () -> new PaneBlock(AbstractBlock.Properties.copy(Blocks.CHAIN)));
    
    public static final RegistryObject<Block> FOREST_CAKE = BLOCKS.register("forest_cake",
            () -> new ForestCakeBlock(AbstractBlock.Properties.copy(Blocks.CAKE)));

    public static final RegistryObject<Block> CHAMBER_DOOR = BLOCKS.register("chamber_door",
            () -> new ChamberDoorBlock(AbstractBlock.Properties.of(Material.STONE).sound(SoundType.STONE).noOcclusion()));

    public static final RegistryObject<Block> CUBE_DROPPER = BLOCKS.register("cube_dropper",
            () -> new CubeDropperBlock(AbstractBlock.Properties.of(Material.STONE).noOcclusion()));

    // todo rename
    public static final RegistryObject<Block> STANDING_BUTTON = BLOCKS.register("standing_button",
            () -> new StandingButtonBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));

    public static final RegistryObject<Block> SUPER_BUTTON = BLOCKS.register("super_button",
            () -> new SuperButtonBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));

    public static final RegistryObject<Block> CHAMBER_LIGHTS = BLOCKS.register("chamber_lights",
            () -> new ChamberLightsBlock(AbstractBlock.Properties.copy(Blocks.REDSTONE_LAMP).lightLevel(i -> 15)));

    public static final RegistryObject<Block> TEST_BLOCK = BLOCKS.register("test_block",
            () -> new Block(AbstractBlock.Properties.copy(Blocks.NETHERITE_BLOCK)));
    
    // ANTLINE
    
    public static final RegistryObject<Block> ANTLINE = BLOCKS.register("antline",
            () -> new AntlineBlock(AbstractBlock.Properties.copy(Blocks.REDSTONE_WIRE).lightLevel(i -> 4).emissiveRendering(($0, $1, $2) -> true)));  // todo: this emissive rendering stuff doesnt work properly because it does not have a simple block model
    
    public static final RegistryObject<Block> ANTLINE_INDICATOR = BLOCKS.register("antline_indicator",
            () -> new AntlineIndicatorBlock(AbstractBlock.Properties.of(Material.DECORATION).noOcclusion().strength(1.0F)));
    
//    public static final RegistryObject<Block> LASER_EMITTER = BLOCKS.register("laser_emitter",
//            () -> new LaserEmitterBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));
//
//    public static final RegistryObject<Block> LASER_CATCHER = BLOCKS.register("laser_catcher",
//            () -> new LaserCatcherBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));
//
//    public static final RegistryObject<Block> LASER_RELAY = BLOCKS.register("laser_relay",
//            () -> new LaserRelayBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));

    public static final RegistryObject<Block> FIZZLER_EMITTER = BLOCKS.register("fizzler_emitter",
            () -> new FizzlerEmitterBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion().lightLevel(blockState -> blockState.getValue(FizzlerEmitterBlock.ACTIVE) ? 10 : 0)));

    public static final RegistryObject<Block> FIZZLER_FIELD = BLOCKS.register("fizzler_field",
            () -> new FizzlerFieldBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion().strength(-1.0F,3600000.0F).noDrops().lightLevel(blockState -> 10)));

    public static final RegistryObject<FlowingFluidBlock> GOO = BLOCKS.register("goo",
            () -> new FlowingFluidBlock(() -> FluidInit.GOO_FLUID.get(), AbstractBlock.Properties.of(Material.WATER)));
    
    // GEL
    
    private static final AbstractBlock.Properties GEL_BASE = AbstractBlock.Properties.of(Material.CLOTH_DECORATION)
            .sound(SoundTypeInit.GEL).noOcclusion();
    
    public static final RegistryObject<Block> REPULSION_GEL = BLOCKS.register("repulsion_gel", () -> new RepulsionGelBlock(GEL_BASE));
    public static final RegistryObject<Block> PROPULSION_GEL = BLOCKS.register("propulsion_gel", () -> new PropulsionGelBlock(GEL_BASE));
    public static final RegistryObject<Block> CONVERSION_GEL = BLOCKS.register("conversion_gel", () -> new ConversionGelBlock(GEL_BASE));

    public static RegistryObject<Block> registerLunecast(String name) {
        return BLOCKS.register(name, () -> new SmallPanelBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }

    public static RegistryObject<Block> registerBlackplate(String name) {
        return BLOCKS.register(name, () -> new LargePanelBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }

    public static RegistryObject<Block> registerSlab(String name) {
        return BLOCKS.register(name + "_slab", () -> new SlabBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }

    public static RegistryObject<Block> registerStairs(String name) {
        return BLOCKS.register(name + "_stairs", () -> new StairsBlock(() -> LUNECAST.get().defaultBlockState(), AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }
}