package net.portalmod.core.init;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
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
import net.portalmod.common.sorted.gel.PropulsionGelBlock;
import net.portalmod.common.sorted.gel.RepulsionGelBlock;
import net.portalmod.common.sorted.goo.GooBlock;
import net.portalmod.common.sorted.panel.LargePanelBlock;
import net.portalmod.common.sorted.panel.SmallPanelBlock;
import net.portalmod.common.sorted.pellet.PelletLauncherBlock;
import net.portalmod.common.sorted.radio.RadioBlock;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PortalMod.MODID);
    
    private BlockInit() {}

    // Material for small things that would use Material.DECORATION. This one is the same but does not let water break it.
    public static final Material TESTING_ELEMENT = new Material(
            MaterialColor.COLOR_GRAY,
            false,
            false,
            true,
            false,
            false,
            false,
            PushReaction.DESTROY);
    
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

    public static final RegistryObject<Block> LUNECAST_SLAB = registerLunecastSlab("lunecast");
    public static final RegistryObject<Block> BLACKPLATE_SLAB = registerBlackplateSlab("blackplate");
    public static final RegistryObject<Block> ARBORED_LUNECAST_SLAB = registerLunecastSlab("arbored_lunecast");
    public static final RegistryObject<Block> ARBORED_BLACKPLATE_SLAB = registerBlackplateSlab("arbored_blackplate");
    public static final RegistryObject<Block> ERODED_LUNECAST_SLAB = registerLunecastSlab("eroded_lunecast");
    public static final RegistryObject<Block> ERODED_BLACKPLATE_SLAB = registerBlackplateSlab("eroded_blackplate");
    public static final RegistryObject<Block> FRACTURED_LUNECAST_SLAB = registerLunecastSlab("fractured_lunecast");
    public static final RegistryObject<Block> FRACTURED_BLACKPLATE_SLAB = registerBlackplateSlab("fractured_blackplate");
    public static final RegistryObject<Block> VINTAGE_LUNECAST_SLAB = registerLunecastSlab("vintage_lunecast");
    public static final RegistryObject<Block> VINTAGE_BLACKPLATE_SLAB = registerBlackplateSlab("vintage_blackplate");

    public static final RegistryObject<Block> LUNECAST_STAIRS = registerLunecastStairs("lunecast");
    public static final RegistryObject<Block> BLACKPLATE_STAIRS = registerBlackplateStairs("blackplate");
    public static final RegistryObject<Block> ARBORED_LUNECAST_STAIRS = registerLunecastStairs("arbored_lunecast");
    public static final RegistryObject<Block> ARBORED_BLACKPLATE_STAIRS = registerBlackplateStairs("arbored_blackplate");
    public static final RegistryObject<Block> ERODED_LUNECAST_STAIRS = registerLunecastStairs("eroded_lunecast");
    public static final RegistryObject<Block> ERODED_BLACKPLATE_STAIRS = registerBlackplateStairs("eroded_blackplate");
    public static final RegistryObject<Block> FRACTURED_LUNECAST_STAIRS = registerLunecastStairs("fractured_lunecast");
    public static final RegistryObject<Block> FRACTURED_BLACKPLATE_STAIRS = registerBlackplateStairs("fractured_blackplate");
    public static final RegistryObject<Block> VINTAGE_LUNECAST_STAIRS = registerLunecastStairs("vintage_lunecast");
    public static final RegistryObject<Block> VINTAGE_BLACKPLATE_STAIRS = registerBlackplateStairs("vintage_blackplate");



    public static final RegistryObject<Block> STEP_PILLAR = BLOCKS.register("step_pillar",
            () -> new StepPillarBlock(AbstractBlock.Properties.copy(Blocks.LIGHT_GRAY_CONCRETE)));

    public static final RegistryObject<Block> LUNECAST_STEP = BLOCKS.register("lunecast_step",
            () -> new StepBlock(AbstractBlock.Properties.copy(Blocks.WHITE_CONCRETE)));

    public static final RegistryObject<Block> RUSTY_LUNECAST_STEP = BLOCKS.register("rusty_lunecast_step",
            () -> new StepBlock(AbstractBlock.Properties.copy(Blocks.WHITE_CONCRETE)));

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
            () -> new RadioBlock(AbstractBlock.Properties.of(TESTING_ELEMENT).strength(1.0F)));
    
    public static final RegistryObject<Block> FAITHPLATE = BLOCKS.register("faithplate",
            () -> new FaithPlateBlock(stoneCopy(MaterialColor.COLOR_BLACK)));
    
    public static final RegistryObject<Block> WIRE_MESH_BLOCK = BLOCKS.register("wire_mesh_block",
            () -> new WireMeshBlock(AbstractBlock.Properties.copy(Blocks.CHAIN)));

    public static final RegistryObject<Block> WIRE_MESH = BLOCKS.register("wire_mesh",
            () -> new PaneBlock(AbstractBlock.Properties.copy(Blocks.CHAIN)));
    
    public static final RegistryObject<Block> FOREST_CAKE = BLOCKS.register("forest_cake",
            () -> new ForestCakeBlock(AbstractBlock.Properties.copy(Blocks.CAKE)));

    public static final RegistryObject<Block> CHAMBER_DOOR = BLOCKS.register("chamber_door",
            () -> new ChamberDoorBlock(stoneCopy(MaterialColor.COLOR_BLACK).sound(SoundType.STONE).noOcclusion()));

    public static final RegistryObject<Block> CUBE_DROPPER = BLOCKS.register("cube_dropper",
            () -> new CubeDropperBlock(AbstractBlock.Properties.of(Material.STONE).noOcclusion()));

    public static final RegistryObject<Block> PUSH_DOOR = BLOCKS.register("push_door",
            () -> new PushDoorBlock(AbstractBlock.Properties.copy(Blocks.IRON_DOOR)));

    public static final RegistryObject<Block> PELLET_LAUNCHER = BLOCKS.register("pellet_launcher",
            () -> new PelletLauncherBlock(AbstractBlock.Properties.of(Material.STONE)));

    public static final RegistryObject<Block> STANDING_BUTTON = BLOCKS.register("standing_button",
            () -> new StandingButtonBlock(stoneCopy(MaterialColor.COLOR_RED).noOcclusion()));

    public static final RegistryObject<Block> SUPER_BUTTON = BLOCKS.register("super_button",
            () -> new SuperButtonBlock(stoneCopy(MaterialColor.COLOR_RED).noOcclusion()));

    public static final RegistryObject<Block> CHAMBER_LIGHTS = BLOCKS.register("chamber_lights",
            () -> new ChamberLightsBlock(AbstractBlock.Properties.copy(Blocks.REDSTONE_LAMP).lightLevel(i -> 15)));

    public static final RegistryObject<Block> ANTLINE = BLOCKS.register("antline",
            () -> new AntlineBlock(AbstractBlock.Properties.of(TESTING_ELEMENT, MaterialColor.COLOR_LIGHT_BLUE).lightLevel(i -> 7).noCollission().instabreak()/*.hasPostProcess(BlockInit::always).emissiveRendering(BlockInit::always)*/));
    // todo: this emissive rendering stuff likely doesnt work properly because it does not have a simple block model
    
    public static final RegistryObject<Block> ANTLINE_INDICATOR = BLOCKS.register("antline_indicator",
            () -> new AntlineIndicatorBlock(AbstractBlock.Properties.of(TESTING_ELEMENT).noOcclusion().strength(1.0F).lightLevel(i -> 7)/*.hasPostProcess(BlockInit::always).emissiveRendering(BlockInit::always)*/));

    public static final RegistryObject<Block> FIZZLER_EMITTER = BLOCKS.register("fizzler_emitter",
            () -> new FizzlerEmitterBlock(stoneCopy(MaterialColor.COLOR_BLACK).noOcclusion().lightLevel(blockState -> blockState.getValue(FizzlerEmitterBlock.ACTIVE) ? 10 : 0)));

    public static final RegistryObject<Block> FIZZLER_FIELD = BLOCKS.register("fizzler_field",
            () -> new FizzlerFieldBlock(AbstractBlock.Properties.copy(Blocks.AIR).noOcclusion().strength(-1.0F,3600000.0F).noDrops().lightLevel(blockState -> 10)));

    public static final RegistryObject<FlowingFluidBlock> GOO = BLOCKS.register("goo",
            () -> new GooBlock(FluidInit.GOO_FLUID, AbstractBlock.Properties.of(Material.WATER, MaterialColor.PODZOL)));



    // GEL
    
    public static final RegistryObject<Block> REPULSION_GEL = BLOCKS.register("repulsion_gel", () -> new RepulsionGelBlock(
            AbstractBlock.Properties.of(TESTING_ELEMENT, MaterialColor.COLOR_BLUE).sound(SoundTypeInit.GEL).noOcclusion().noCollission()));

    public static final RegistryObject<Block> PROPULSION_GEL = BLOCKS.register("propulsion_gel", () -> new PropulsionGelBlock(
            AbstractBlock.Properties.of(TESTING_ELEMENT, MaterialColor.COLOR_ORANGE).sound(SoundTypeInit.GEL).noOcclusion().noCollission()));



    public static final RegistryObject<Block> TEST_BLOCK = BLOCKS.register("test_block",
            () -> new Block(AbstractBlock.Properties.copy(Blocks.NETHERITE_BLOCK)));



    public static RegistryObject<Block> registerLunecast(String name) {
        return BLOCKS.register(name, () -> new SmallPanelBlock(AbstractBlock.Properties.copy(Blocks.WHITE_CONCRETE)));
    }

    public static RegistryObject<Block> registerBlackplate(String name) {
        return BLOCKS.register(name, () -> new LargePanelBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }

    public static RegistryObject<Block> registerLunecastSlab(String name) {
        return BLOCKS.register(name + "_slab", () -> new SlabBlock(AbstractBlock.Properties.copy(Blocks.WHITE_CONCRETE)));
    }

    public static RegistryObject<Block> registerBlackplateSlab(String name) {
        return BLOCKS.register(name + "_slab", () -> new SlabBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }

    public static RegistryObject<Block> registerLunecastStairs(String name) {
        return BLOCKS.register(name + "_stairs", () -> new StairsBlock(() -> LUNECAST.get().defaultBlockState(), AbstractBlock.Properties.copy(Blocks.WHITE_CONCRETE)));
    }

    public static RegistryObject<Block> registerBlackplateStairs(String name) {
        return BLOCKS.register(name + "_stairs", () -> new StairsBlock(() -> BLACKPLATE.get().defaultBlockState(), AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    }

    public static AbstractBlock.Properties stoneCopy(MaterialColor color) {
        return AbstractBlock.Properties.of(Material.STONE, color).requiresCorrectToolForDrops().strength(1.5F, 6.0F);
    }

    public static boolean always(BlockState p_235426_0_, IBlockReader p_235426_1_, BlockPos p_235426_2_) {
        return true;
    }
}