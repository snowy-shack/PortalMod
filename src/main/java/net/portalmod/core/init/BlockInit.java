package net.portalmod.core.init;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.portalmod.PortalMod;
import net.portalmod.common.blocks.ChamberLightsBlock;
import net.portalmod.common.blocks.ForestCakeBlock;
import net.portalmod.common.blocks.FrameBlock;
import net.portalmod.common.blocks.WireMeshBlock;
import net.portalmod.common.sorted.antline.AntlineBlock;
import net.portalmod.common.sorted.antline.AntlineIndicatorBlock;
import net.portalmod.common.sorted.chamber_door.ChamberDoorBlock;
import net.portalmod.common.sorted.faithplate.FaithPlateBlock;
import net.portalmod.common.sorted.fizzler.FizzlerEmitterBlock;
import net.portalmod.common.sorted.fizzler.FizzlerFieldBlock;
import net.portalmod.common.sorted.gel.AdhesionGelBlock;
import net.portalmod.common.sorted.gel.ConversionGelBlock;
import net.portalmod.common.sorted.gel.PropulsionGelBlock;
import net.portalmod.common.sorted.gel.RepulsionGelBlock;
import net.portalmod.common.sorted.laser.LaserCatcherBlock;
import net.portalmod.common.sorted.laser.LaserEmitterBlock;
import net.portalmod.common.sorted.laser.LaserRelayBlock;
import net.portalmod.common.sorted.panel.PanelBlock;
import net.portalmod.common.sorted.radio.RadioBlock;
import net.portalmod.common.sorted.superbutton.SuperButtonBlock;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PortalMod.MODID);
    
    private BlockInit() {}
    
    public static final RegistryObject<Block> LUNECAST = registerLunecast("");
    public static final RegistryObject<Block> BLACKPLATE = registerBlackplate("");
    public static final RegistryObject<Block> ARBORED_LUNECAST = registerLunecast("arbored_");
    public static final RegistryObject<Block> ARBORED_BLACKPLATE = registerBlackplate("arbored_");
    public static final RegistryObject<Block> ERODED_LUNECAST = registerLunecast("eroded_");
    public static final RegistryObject<Block> ERODED_BLACKPLATE = registerBlackplate("eroded_");
    public static final RegistryObject<Block> FRACTURED_LUNECAST = registerLunecast("fractured_");
    public static final RegistryObject<Block> FRACTURED_BLACKPLATE = registerBlackplate("fractured_");
    public static final RegistryObject<Block> VINTAGE_LUNECAST = registerLunecast("vintage_");
    public static final RegistryObject<Block> VINTAGE_BLACKPLATE = registerBlackplate("vintage_");
    
    public static final RegistryObject<Block> IRON_FRAME = BLOCKS.register("iron_frame",
            () -> new FrameBlock(AbstractBlock.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), false));

    public static final RegistryObject<Block> BARRED_IRON_FRAME = BLOCKS.register("barred_iron_frame",
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
    
    // ANTLINE
    
    public static final RegistryObject<Block> ANTLINE = BLOCKS.register("antline",
            () -> new AntlineBlock(AbstractBlock.Properties.copy(Blocks.REDSTONE_WIRE).lightLevel(i -> 7).emissiveRendering(($0, $1, $2) -> true)));
    
    public static final RegistryObject<Block> SUPER_BUTTON = BLOCKS.register("super_button",
            () -> new SuperButtonBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));
    
    public static final RegistryObject<Block> ANTLINE_INDICATOR = BLOCKS.register("antline_indicator",
            () -> new AntlineIndicatorBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));
    
    public static final RegistryObject<Block> LASER_EMITTER = BLOCKS.register("laser_emitter",
            () -> new LaserEmitterBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));
    
    public static final RegistryObject<Block> LASER_CATCHER = BLOCKS.register("laser_catcher",
            () -> new LaserCatcherBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));
    
    public static final RegistryObject<Block> LASER_RELAY = BLOCKS.register("laser_relay",
            () -> new LaserRelayBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));

    public static final RegistryObject<Block> FIZZLER_EMITTER = BLOCKS.register("fizzler_emitter",
            () -> new FizzlerEmitterBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));

    public static final RegistryObject<Block> FIZZLER_FIELD = BLOCKS.register("fizzler_field",
            () -> new FizzlerFieldBlock(AbstractBlock.Properties.copy(Blocks.STONE).noOcclusion()));

    public static final RegistryObject<FlowingFluidBlock> GOO = BLOCKS.register("goo",
            () -> new FlowingFluidBlock(() -> FluidInit.GOO_FLUID.get(), AbstractBlock.Properties.of(Material.WATER)));
    
    // GEL
    
    private static final AbstractBlock.Properties GEL_BASE = AbstractBlock.Properties.of(Material.CLOTH_DECORATION)
            .sound(SoundTypeInit.GEL).noOcclusion();
    
    public static final RegistryObject<Block> REPULSION_GEL = BLOCKS.register("repulsion_gel", () -> new RepulsionGelBlock(GEL_BASE));
    public static final RegistryObject<Block> PROPULSION_GEL = BLOCKS.register("propulsion_gel", () -> new PropulsionGelBlock(GEL_BASE));
    public static final RegistryObject<Block> ADHESION_GEL = BLOCKS.register("adhesion_gel", () -> new AdhesionGelBlock(GEL_BASE));
    public static final RegistryObject<Block> CONVERSION_GEL = BLOCKS.register("conversion_gel", () -> new ConversionGelBlock(GEL_BASE));
    
    public static final RegistryObject<Block> CHAMBER_LIGHTS = BLOCKS.register("chamber_lights",
            () -> new ChamberLightsBlock(AbstractBlock.Properties.copy(Blocks.REDSTONE_LAMP).lightLevel(i -> 15).noOcclusion()));

    public static RegistryObject<Block> registerLunecast(String name) {
        return BLOCKS.register(name + "lunecast", () -> new PanelBlock(false, AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }

    public static RegistryObject<Block> registerBlackplate(String name) {
        return BLOCKS.register(name + "blackplate", () -> new PanelBlock(true, AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }
}