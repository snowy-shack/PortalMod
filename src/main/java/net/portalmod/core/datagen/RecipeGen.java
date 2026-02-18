package net.portalmod.core.datagen;

import net.minecraft.advancements.criterion.*;
import net.minecraft.block.Blocks;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.ItemTagInit;
import net.portalmod.core.init.RecipeInit;

import java.util.function.Consumer;

public class RecipeGen extends RecipeProvider {
    public static final CriterionInstance ROOT_CRITERION = RightClickBlockWithItemTrigger.Instance.itemUsedOnBlock(
            LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.CRAFTING_TABLE).build()),
            ItemPredicate.Builder.item());
    public static final CriterionInstance CAKE_CRITERION = KilledTrigger.Instance.playerKilledEntity();

    public static final CriterionInstance HAS_CUBE = RecipeProvider.has(ItemTagInit.CUBES);
    public static final CriterionInstance HAS_INDICATOR = RecipeProvider.has(ItemTagInit.INDICATORS);
    public static final CriterionInstance HAS_LUNECAST = RecipeProvider.has(ItemTagInit.LUNECAST);
    public static final CriterionInstance HAS_BLACKPLATE = RecipeProvider.has(ItemTagInit.BLACKPLATE);
    public static final CriterionInstance HAS_CHAMBER_DECORATION = RecipeProvider.has(ItemTagInit.CHAMBER_DECORATION);
    public static final CriterionInstance HAS_ELEMENT = RecipeProvider.has(ItemTagInit.TESTING_ELEMENTS);

    public RecipeGen(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> c) {

        // --- Basic Items --- //

        ShapedRecipeBuilder.shaped(ItemInit.PORTALGUN.get())
                .pattern("ii ")
                .pattern("ron")
                .define('i', Items.IRON_INGOT)
                .define('r', Items.REDSTONE)
                .define('o', Items.ENDER_PEARL)
                .define('n', Items.NETHERITE_INGOT)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        CustomRecipeBuilder.special(RecipeInit.PORTAL_GUN.get()).save(c, "portalmod:portalgun_edit");

        ShapedRecipeBuilder.shaped(ItemInit.LONGFALL_BOOTS.get())
                .pattern("i i").define('i', Items.IRON_INGOT)
                .pattern("n n").define('n', Items.NETHERITE_SCRAP)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.WRENCH.get())
                .pattern(" i ").define('i', Items.IRON_INGOT)
                .pattern(" ii").define('/', Items.STICK)
                .pattern("/  ")
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.RADIO.get())
                .pattern(" i ").define('i', Items.IRON_INGOT)
                .pattern("ioi").define('o', Items.JUKEBOX)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.FOREST_CAKE.get())
                .pattern("oio")
                .pattern("cfc")
                .pattern("www")
                .define('i', Items.TORCH)
                .define('o', Items.SWEET_BERRIES)
                .define('c', Items.COCOA_BEANS)
                .define('f', Items.ROTTEN_FLESH)
                .define('w', Items.WHEAT)
                .unlockedBy("kill_entity", CAKE_CRITERION)
                .save(c);



        // --- Cubes --- //

        ShapedRecipeBuilder.shaped(ItemInit.STORAGE_CUBE.get())
                .pattern("IiI").define('I', Items.IRON_INGOT)
                .pattern("i i").define('i', Items.IRON_NUGGET)
                .pattern("IiI")
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.COMPANION_CUBE.get())
                .pattern("IiI").define('I', Items.IRON_INGOT)
                .pattern("iai").define('i', Items.IRON_NUGGET)
                .pattern("IiI").define('a', Items.GOLDEN_APPLE)
                .unlockedBy("has_item", HAS_CUBE)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.VINTAGE_CUBE.get())
                .pattern("IiI").define('I', Items.IRON_INGOT)
                .pattern("ipi").define('i', Items.IRON_NUGGET)
                .pattern("IiI").define('p', ItemTags.PLANKS)
                .unlockedBy("has_item", HAS_CUBE)
                .save(c);



        // --- Antline --- //

        ShapelessRecipeBuilder.shapeless(ItemInit.ANTLINE.get(), 8)
                .requires(Items.GOLD_INGOT)
                .requires(Items.LIGHT_BLUE_DYE)
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.ANTLINE_INDICATOR.get())
                .pattern("iai")
                .define('i', Items.IRON_INGOT)
                .define('a', ItemInit.ANTLINE.get())
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapelessRecipeBuilder.shapeless(ItemInit.ANTLINE_TIMER.get())
                .requires(ItemInit.ANTLINE_INDICATOR.get())
                .requires(Items.CLOCK)
                .unlockedBy("has_item", HAS_INDICATOR)
                .save(c);

        ShapelessRecipeBuilder.shapeless(ItemInit.ANTLINE_CONVERTER.get())
                .requires(ItemInit.ANTLINE_INDICATOR.get())
                .requires(Items.REDSTONE)
                .unlockedBy("has_item", HAS_INDICATOR)
                .group("antline_converter")
                .save(c, "portalmod:antline_converter_from_indicator");

        ShapelessRecipeBuilder.shapeless(ItemInit.ANTLINE_CONVERTER.get())
                .requires(ItemInit.ANTLINE_RECEIVER.get())
                .unlockedBy("has_item", HAS_INDICATOR)
                .group("antline_converter")
                .save(c, "portalmod:antline_converter_from_receiver");

        ShapelessRecipeBuilder.shapeless(ItemInit.ANTLINE_RECEIVER.get())
                .requires(ItemInit.ANTLINE_CONVERTER.get())
                .unlockedBy("has_item", HAS_INDICATOR)
                .save(c);



        // --- Testing Elements --- //

        ShapedRecipeBuilder.shaped(ItemInit.SUPER_BUTTON.get())
                .pattern("ipi")
                .pattern("#a#")
                .define('i', Items.IRON_NUGGET)
                .define('#', Items.BLACKSTONE)
                .define('p', ItemTagInit.PRESSURE_PLATES)
                .define('a', ItemInit.ANTLINE.get())
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.STANDING_BUTTON.get())
                .pattern("b").define('b', ItemTags.BUTTONS)
                .pattern("i").define('i', Items.IRON_INGOT)
                .pattern("#").define('#', Items.BLACKSTONE)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.CHAMBER_DOOR.get())
                .pattern("bb")
                .pattern("ii")
                .define('b', ItemInit.BLACKPLATE_SLAB.get())
                .define('i', Items.IRON_DOOR)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapelessRecipeBuilder.shapeless(ItemInit.PUSH_DOOR.get())
                .requires(Items.IRON_DOOR)
                .requires(ItemTags.BUTTONS)
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.TURRET.get())
                .pattern(" i ")
                .pattern("brb")
                .pattern("/i/")
                .define('i', Items.IRON_INGOT)
                .define('b', Items.BOW)
                .define('r', Items.REDSTONE)
                .define('/', Items.STICK)
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapelessRecipeBuilder.shapeless(ItemInit.BULLETS.get(), 4)
                .requires(Items.GUNPOWDER)
                .requires(Items.GOLD_INGOT)
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.CUBE_DROPPER.get())
                .pattern("III")
                .pattern("gDg")
                .pattern("iii")
                .define('I', Items.IRON_INGOT)
                .define('i', Items.IRON_NUGGET)
                .define('D', Items.DISPENSER)
                .define('g', Tags.Items.GLASS_PANES)
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.FIZZLER_EMITTER.get(), 2)
                .pattern("Bi").define('i', Items.IRON_INGOT)
                .pattern("Bd").define('d', Items.PRISMARINE_CRYSTALS)
                .pattern("Bi").define('B', Items.BLACKSTONE)
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.CHAMBER_SIGN.get())
                .pattern("igi").define('g', Items.GLOWSTONE)
                .pattern("ibi").define('b', Items.WHITE_BANNER)
                .pattern("igi").define('i', Items.IRON_NUGGET)
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.FAITHPLATE.get())
                .pattern(" p ")
                .pattern("Pai")
                .pattern("bbb")
                .define('p', Ingredient.of(Items.PISTON, Items.STICKY_PISTON))
                .define('P', ItemTagInit.PRESSURE_PLATES)
                .define('a', ItemInit.ANTLINE.get())
                .define('i', Items.IRON_INGOT)
                .define('b', ItemInit.BLACKPLATE_SLAB.get())
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.CONTAINER.get())
                .pattern("i i").define('i', Items.IRON_INGOT)
                .pattern("pip").define('p', Items.PAPER)
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapelessRecipeBuilder.shapeless(ItemInit.REPULSION_GEL.get())
                .requires(Items.WARPED_FUNGUS)
                .requires(Items.SLIME_BALL)
                .requires(ItemInit.CONTAINER.get())
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);

        ShapelessRecipeBuilder.shapeless(ItemInit.PROPULSION_GEL.get())
                .requires(Items.CRIMSON_FUNGUS)
                .requires(Items.MAGMA_CREAM)
                .requires(ItemInit.CONTAINER.get())
                .unlockedBy("has_item", HAS_ELEMENT)
                .save(c);



        // --- Building Blocks --- //

        ShapedRecipeBuilder.shaped(ItemInit.LUNECAST.get(), 4)
                .pattern("q#").define('q', Items.QUARTZ)
                .pattern("#q").define('#', Items.END_STONE)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.BLACKPLATE.get(), 4)
                .pattern("i#").define('i', Items.IRON_NUGGET)
                .pattern("#i").define('#', Items.BLACKSTONE)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        lunecastVariant(c, ItemInit.ARBORED_LUNECAST.get(), Ingredient.of(Items.GRASS, Items.TALL_GRASS, Items.SEAGRASS, Items.VINE));
        lunecastVariant(c, ItemInit.ERODED_LUNECAST.get(), Ingredient.of(Items.DIRT, Items.COARSE_DIRT, Items.GRAVEL));
        lunecastVariant(c, ItemInit.VINTAGE_LUNECAST.get(), Ingredient.of(ItemTags.PLANKS));
        blackplateVariant(c, ItemInit.ARBORED_BLACKPLATE.get(), Ingredient.of(Items.GRASS, Items.TALL_GRASS, Items.SEAGRASS, Items.VINE));
        blackplateVariant(c, ItemInit.ERODED_BLACKPLATE.get(), Ingredient.of(Items.DIRT, Items.COARSE_DIRT, Items.GRAVEL));
        blackplateVariant(c, ItemInit.VINTAGE_BLACKPLATE.get(), Ingredient.of(ItemTags.PLANKS));

        CookingRecipeBuilder.smelting(Ingredient.of(ItemInit.LUNECAST.get()),
                        ItemInit.FRACTURED_LUNECAST.get(),
                        0.1f, 200)
                .unlockedBy("has_item", HAS_LUNECAST)
                .save(c);

        CookingRecipeBuilder.smelting(Ingredient.of(ItemInit.BLACKPLATE.get()),
                        ItemInit.FRACTURED_BLACKPLATE.get(),
                        0.1f, 200)
                .unlockedBy("has_item", HAS_BLACKPLATE)
                .save(c);

        lunecastSlab(c, ItemInit.LUNECAST_SLAB.get(), ItemInit.LUNECAST.get());
        lunecastSlab(c, ItemInit.ARBORED_LUNECAST_SLAB.get(), ItemInit.ARBORED_LUNECAST.get());
        lunecastSlab(c, ItemInit.ERODED_LUNECAST_SLAB.get(), ItemInit.ERODED_LUNECAST.get());
        lunecastSlab(c, ItemInit.FRACTURED_LUNECAST_SLAB.get(), ItemInit.FRACTURED_LUNECAST.get());
        lunecastSlab(c, ItemInit.VINTAGE_LUNECAST_SLAB.get(), ItemInit.VINTAGE_LUNECAST.get());
        blackplateSlab(c, ItemInit.BLACKPLATE_SLAB.get(), ItemInit.BLACKPLATE.get());
        blackplateSlab(c, ItemInit.ARBORED_BLACKPLATE_SLAB.get(), ItemInit.ARBORED_BLACKPLATE.get());
        blackplateSlab(c, ItemInit.ERODED_BLACKPLATE_SLAB.get(), ItemInit.ERODED_BLACKPLATE.get());
        blackplateSlab(c, ItemInit.FRACTURED_BLACKPLATE_SLAB.get(), ItemInit.FRACTURED_BLACKPLATE.get());
        blackplateSlab(c, ItemInit.VINTAGE_BLACKPLATE_SLAB.get(), ItemInit.VINTAGE_BLACKPLATE.get());

        lunecastStairs(c, ItemInit.LUNECAST_STAIRS.get(), ItemInit.LUNECAST.get());
        lunecastStairs(c, ItemInit.ARBORED_LUNECAST_STAIRS.get(), ItemInit.ARBORED_LUNECAST.get());
        lunecastStairs(c, ItemInit.ERODED_LUNECAST_STAIRS.get(), ItemInit.ERODED_LUNECAST.get());
        lunecastStairs(c, ItemInit.FRACTURED_LUNECAST_STAIRS.get(), ItemInit.FRACTURED_LUNECAST.get());
        lunecastStairs(c, ItemInit.VINTAGE_LUNECAST_STAIRS.get(), ItemInit.VINTAGE_LUNECAST.get());
        blackplateStairs(c, ItemInit.BLACKPLATE_STAIRS.get(), ItemInit.BLACKPLATE.get());
        blackplateStairs(c, ItemInit.ARBORED_BLACKPLATE_STAIRS.get(), ItemInit.ARBORED_BLACKPLATE.get());
        blackplateStairs(c, ItemInit.ERODED_BLACKPLATE_STAIRS.get(), ItemInit.ERODED_BLACKPLATE.get());
        blackplateStairs(c, ItemInit.FRACTURED_BLACKPLATE_STAIRS.get(), ItemInit.FRACTURED_BLACKPLATE.get());
        blackplateStairs(c, ItemInit.VINTAGE_BLACKPLATE_STAIRS.get(), ItemInit.VINTAGE_BLACKPLATE.get());

        lunecastPlatform(c, ItemInit.LUNECAST_PLATFORM.get(), ItemInit.LUNECAST.get());
        lunecastPlatform(c, ItemInit.ARBORED_LUNECAST_PLATFORM.get(), ItemInit.ARBORED_LUNECAST.get());
        lunecastPlatform(c, ItemInit.ERODED_LUNECAST_PLATFORM.get(), ItemInit.ERODED_LUNECAST.get());
        lunecastPlatform(c, ItemInit.FRACTURED_LUNECAST_PLATFORM.get(), ItemInit.FRACTURED_LUNECAST.get());
        lunecastPlatform(c, ItemInit.VINTAGE_LUNECAST_PLATFORM.get(), ItemInit.VINTAGE_LUNECAST.get());
        blackplatePlatform(c, ItemInit.BLACKPLATE_PLATFORM.get(), ItemInit.BLACKPLATE.get());
        blackplatePlatform(c, ItemInit.ARBORED_BLACKPLATE_PLATFORM.get(), ItemInit.ARBORED_BLACKPLATE.get());
        blackplatePlatform(c, ItemInit.ERODED_BLACKPLATE_PLATFORM.get(), ItemInit.ERODED_BLACKPLATE.get());
        blackplatePlatform(c, ItemInit.FRACTURED_BLACKPLATE_PLATFORM.get(), ItemInit.FRACTURED_BLACKPLATE.get());
        blackplatePlatform(c, ItemInit.VINTAGE_BLACKPLATE_PLATFORM.get(), ItemInit.VINTAGE_BLACKPLATE.get());

        ShapedRecipeBuilder.shaped(ItemInit.CHAMBER_LIGHTS.get(), 2)
                .pattern("g#g").define('#', ItemInit.BLACKPLATE.get())
                .pattern("g#g").define('g', Items.GLOWSTONE_DUST)
                .unlockedBy("has_item", ROOT_CRITERION)
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.WIRE_MESH.get())
                .pattern("nnn").define('n', Items.IRON_NUGGET)
                .pattern("nnn")
                .unlockedBy("has_item", HAS_CHAMBER_DECORATION)
                .group("wire_mesh")
                .save(c);

        ShapelessRecipeBuilder.shapeless(ItemInit.WIRE_MESH.get(), 4)
                .requires(ItemInit.WIRE_MESH_BLOCK.get())
                .unlockedBy("has_item", HAS_CHAMBER_DECORATION)
                .group("wire_mesh")
                .save(c, "portalmod:wire_mesh_from_block");

        ShapedRecipeBuilder.shaped(ItemInit.WIRE_MESH_BLOCK.get())
                .pattern("ww").define('w', ItemInit.WIRE_MESH.get())
                .pattern("ww")
                .unlockedBy("has_item", HAS_CHAMBER_DECORATION)
                .save(c);

        ShapelessRecipeBuilder.shapeless(ItemInit.GOO_BUCKET.get())
                .requires(Items.SOUL_SAND)
                .requires(Items.DIRT)
                .requires(Items.BUCKET)
                .unlockedBy("has_item", HAS_CHAMBER_DECORATION)
                .save(c);



        ShapedRecipeBuilder.shaped(ItemInit.IRON_FRAME.get(), 8)
                .pattern(" i ").define('i', Items.IRON_INGOT)
                .pattern("i i")
                .pattern(" i ")
                .unlockedBy("has_item", ROOT_CRITERION)
                .group("iron_frame")
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.MESHED_IRON_FRAME.get(), 8)
                .pattern(" i ").define('i', Items.IRON_INGOT)
                .pattern("ixi").define('x', ItemInit.WIRE_MESH.get())
                .pattern(" i ")
                .unlockedBy("has_item", HAS_CHAMBER_DECORATION)
                .group("iron_frame")
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.BARRED_IRON_FRAME.get(), 8)
                .pattern(" i ").define('i', Items.IRON_INGOT)
                .pattern("ixi").define('x', Items.IRON_BARS)
                .pattern(" i ")
                .unlockedBy("has_item", HAS_CHAMBER_DECORATION)
                .group("iron_frame")
                .save(c);

        ShapedRecipeBuilder.shaped(ItemInit.PLATFORM_BEAM.get(), 6)
                .pattern("c").define('c', Items.CHAIN)
                .pattern("i").define('i', Items.IRON_INGOT)
                .pattern("i")
                .unlockedBy("has_item", HAS_CHAMBER_DECORATION)
                .save(c);

        rustyVariant(c, ItemInit.RUSTY_IRON_FRAME.get(), ItemInit.IRON_FRAME.get(), "rusty_iron_frame");
        rustyVariant(c, ItemInit.RUSTY_MESHED_IRON_FRAME.get(), ItemInit.MESHED_IRON_FRAME.get(), "rusty_iron_frame");
        rustyVariant(c, ItemInit.RUSTY_BARRED_IRON_FRAME.get(), ItemInit.BARRED_IRON_FRAME.get(), "rusty_iron_frame");
        rustyVariant(c, ItemInit.RUSTY_PLATFORM_BEAM.get(), ItemInit.PLATFORM_BEAM.get(), "rusty_beam");

    }

    public static void lunecastVariant(Consumer<IFinishedRecipe> c, IItemProvider variant, Ingredient ingredient) {
        ShapelessRecipeBuilder.shapeless(variant)
                .requires(ItemInit.LUNECAST.get())
                .requires(ingredient)
                .unlockedBy("has_item", HAS_LUNECAST)
                .group("lunecast_variant")
                .save(c);
    }

    public static void blackplateVariant(Consumer<IFinishedRecipe> c, IItemProvider variant, Ingredient ingredient) {
        ShapelessRecipeBuilder.shapeless(variant)
                .requires(ItemInit.BLACKPLATE.get())
                .requires(ingredient)
                .unlockedBy("has_item", HAS_BLACKPLATE)
                .group("blackplate_variant")
                .save(c);
    }

    public static void lunecastSlab(Consumer<IFinishedRecipe> c, IItemProvider slab, IItemProvider baseBlock) {
        slab(c, slab, baseBlock, "lunecast_slab");
    }

    public static void blackplateSlab(Consumer<IFinishedRecipe> c, IItemProvider slab, IItemProvider baseBlock) {
        slab(c, slab, baseBlock, "blackplate_slab");
    }

    public static void slab(Consumer<IFinishedRecipe> c, IItemProvider slab, IItemProvider baseBlock, String group) {
        ShapedRecipeBuilder.shaped(slab, 6)
                .pattern("###").define('#', baseBlock)
                .group(group)
                .unlockedBy("has_item", has(baseBlock))
                .save(c);
    }

    public static void lunecastStairs(Consumer<IFinishedRecipe> c, IItemProvider stairs, IItemProvider baseBlock) {
        stairs(c, stairs, baseBlock, "lunecast_stairs");
    }

    public static void blackplateStairs(Consumer<IFinishedRecipe> c, IItemProvider stairs, IItemProvider baseBlock) {
        stairs(c, stairs, baseBlock, "blackplate_stairs");
    }

    public static void stairs(Consumer<IFinishedRecipe> c, IItemProvider stairs, IItemProvider baseBlock, String group) {
        ShapedRecipeBuilder.shaped(stairs, 4)
                .pattern("#  ").define('#', baseBlock)
                .pattern("## ")
                .pattern("###")
                .group(group)
                .unlockedBy("has_item", has(baseBlock))
                .save(c);
    }

    public static void lunecastPlatform(Consumer<IFinishedRecipe> c, IItemProvider platform, IItemProvider baseBlock) {
        platform(c, platform, baseBlock, "lunecast_platform");
    }

    public static void blackplatePlatform(Consumer<IFinishedRecipe> c, IItemProvider platform, IItemProvider baseBlock) {
        platform(c, platform, baseBlock, "blackplate_platform");
    }

    public static void platform(Consumer<IFinishedRecipe> c, IItemProvider platform, IItemProvider baseBlock, String group) {
        ShapedRecipeBuilder.shaped(platform)
                .pattern("s").define('s', baseBlock)
                .pattern("i").define('i', ItemTagInit.IRON_FRAMES)
                .group(group)
                .unlockedBy("has_item", has(baseBlock))
                .save(c);
    }

    public static void rustyVariant(Consumer<IFinishedRecipe> c, IItemProvider rusty, IItemProvider regular, String group) {
        ShapelessRecipeBuilder.shapeless(rusty, 8)
                .requires(regular)
                .requires(regular)
                .requires(regular)
                .requires(regular)
                .requires(Items.WATER_BUCKET)
                .requires(regular)
                .requires(regular)
                .requires(regular)
                .requires(regular)
                .unlockedBy("has_item", HAS_CHAMBER_DECORATION)
                .group(group)
                .save(c);
    }
}
