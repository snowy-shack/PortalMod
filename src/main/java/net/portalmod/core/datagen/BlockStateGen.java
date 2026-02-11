package net.portalmod.core.datagen;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.panel.PanelBlock;
import net.portalmod.common.sorted.panel.PanelState;
import net.portalmod.core.init.BlockInit;

import javax.annotation.Nonnull;

public class BlockStateGen extends BlockStateProvider {

    public BlockStateGen(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, PortalMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        genPanel(BlockInit.LUNECAST.get());
        genPanel(BlockInit.ARBORED_LUNECAST.get());
        genPanel(BlockInit.ERODED_LUNECAST.get());
        genPanel(BlockInit.FRACTURED_LUNECAST.get());
        genPanel(BlockInit.VINTAGE_LUNECAST.get());

        genPanel(BlockInit.BLACKPLATE.get());
//        genPanel(BlockInit.ARBORED_BLACKPLATE.get());
        genPanel(BlockInit.ERODED_BLACKPLATE.get());
        genPanel(BlockInit.FRACTURED_BLACKPLATE.get());
        genPanel(BlockInit.VINTAGE_BLACKPLATE.get());

        genPanelSlab(BlockInit.LUNECAST_SLAB.get(), "lunecast");
        genPanelSlab(BlockInit.ARBORED_LUNECAST_SLAB.get(), "arbored_lunecast");
        genPanelSlab(BlockInit.ERODED_LUNECAST_SLAB.get(), "eroded_lunecast");
        genPanelSlab(BlockInit.FRACTURED_LUNECAST_SLAB.get(), "fractured_lunecast");
        genPanelSlabFull(BlockInit.VINTAGE_LUNECAST_SLAB.get(), "vintage_lunecast");

        genPanelSlab(BlockInit.BLACKPLATE_SLAB.get(), "blackplate");
//        genPanelSlab(BlockInit.ARBORED_BLACKPLATE_SLAB.get(), "arbored_blackplate");
        genPanelSlab(BlockInit.ERODED_BLACKPLATE_SLAB.get(), "eroded_blackplate");
        genPanelSlab(BlockInit.FRACTURED_BLACKPLATE_SLAB.get(), "fractured_blackplate");
        genPanelSlab(BlockInit.VINTAGE_BLACKPLATE_SLAB.get(), "vintage_blackplate");

        genPanelStairs(BlockInit.LUNECAST_STAIRS.get(), "lunecast");
        genPanelStairs(BlockInit.ARBORED_LUNECAST_STAIRS.get(), "arbored_lunecast");
        genPanelStairs(BlockInit.ERODED_LUNECAST_STAIRS.get(), "eroded_lunecast");
        genPanelStairs(BlockInit.FRACTURED_LUNECAST_STAIRS.get(), "fractured_lunecast");
        genPanelStairsFull(BlockInit.VINTAGE_LUNECAST_STAIRS.get(), "vintage_lunecast");

        genPanelStairs(BlockInit.BLACKPLATE_STAIRS.get(), "blackplate");
//        genPanelStairs(BlockInit.ARBORED_BLACKPLATE_STAIRS.get(), "arbored_blackplate");
        genPanelStairs(BlockInit.ERODED_BLACKPLATE_STAIRS.get(), "eroded_blackplate");
        genPanelStairs(BlockInit.FRACTURED_BLACKPLATE_STAIRS.get(), "fractured_blackplate");
        genPanelStairs(BlockInit.VINTAGE_BLACKPLATE_STAIRS.get(), "vintage_blackplate");

    }

    public void genPanel(Block block) {
        String name = block.getRegistryName().getPath();

        ModelFile singleModel = this.models().cubeBottomTop(name,
                blockPath(name),
                blockPath(name + "_top"),
                blockPath(name + "_bottom"));

        ModelFile topModel = this.models().withExistingParent(name + "_panel_top", "portalmod:block/cube_part_top")
                .texture("side", blockPath(name + "_panel_top"))
                .texture("top", blockPath(name + "_top"));

        ModelFile bottomModel = this.models().withExistingParent(name + "_panel_bottom", "portalmod:block/cube_part_bottom")
                .texture("side", blockPath(name + "_panel_bottom"))
                .texture("bottom", blockPath(name + "_bottom"));

        ModelFile cornerBottomModel = this.models().withExistingParent(name + "_panel_corner_bottom", "portalmod:block/cube_corner_bottom")
                .texture("bottom", blockPath(name + "_bottom"))
                .texture("back", blockPath(name + "_panel_bottom"))
                .texture("left", blockPath(name + "_panel_bottom_left"))
                .texture("right", blockPath(name + "_panel_bottom_right"));

        ModelFile cornerTopModel = this.models().withExistingParent(name + "_panel_corner_top", "portalmod:block/cube_corner_top")
                .texture("top", blockPath(name + "_top"))
                .texture("back", blockPath(name + "_panel_top"))
                .texture("left", blockPath(name + "_panel_top_left"))
                .texture("right", blockPath(name + "_panel_top_right"));

        this.getVariantBuilder(block)
                .partialState().with(PanelBlock.STATE, PanelState.SINGLE).addModels(new ConfiguredModel(singleModel))

                .partialState().with(PanelBlock.STATE, PanelState.TOP).addModels(new ConfiguredModel(topModel))
                .partialState().with(PanelBlock.STATE, PanelState.BOTTOM).addModels(new ConfiguredModel(bottomModel))

                .partialState().with(PanelBlock.AXIS, Direction.Axis.Z).with(PanelBlock.STATE, PanelState.BOTTOM_LEFT).addModels(new ConfiguredModel(cornerBottomModel))
                .partialState().with(PanelBlock.AXIS, Direction.Axis.Z).with(PanelBlock.STATE, PanelState.BOTTOM_RIGHT).addModels(new ConfiguredModel(cornerBottomModel, 0, 180, true))
                .partialState().with(PanelBlock.AXIS, Direction.Axis.Z).with(PanelBlock.STATE, PanelState.TOP_LEFT).addModels(new ConfiguredModel(cornerTopModel))
                .partialState().with(PanelBlock.AXIS, Direction.Axis.Z).with(PanelBlock.STATE, PanelState.TOP_RIGHT).addModels(new ConfiguredModel(cornerTopModel, 0, 180, true))
                .partialState().with(PanelBlock.AXIS, Direction.Axis.X).with(PanelBlock.STATE, PanelState.BOTTOM_LEFT).addModels(new ConfiguredModel(cornerBottomModel, 0, 90, true))
                .partialState().with(PanelBlock.AXIS, Direction.Axis.X).with(PanelBlock.STATE, PanelState.BOTTOM_RIGHT).addModels(new ConfiguredModel(cornerBottomModel, 0, 270, true))
                .partialState().with(PanelBlock.AXIS, Direction.Axis.X).with(PanelBlock.STATE, PanelState.TOP_LEFT).addModels(new ConfiguredModel(cornerTopModel, 0, 90, true))
                .partialState().with(PanelBlock.AXIS, Direction.Axis.X).with(PanelBlock.STATE, PanelState.TOP_RIGHT).addModels(new ConfiguredModel(cornerTopModel, 0, 270, true));
    }

    public void genPanelSlab(Block block, String name) {
        this.panelSlabBlock(
                (SlabBlock) block, name,
                blockPath(name + "_tiles"),
                blockPath(name + "_bottom"),
                blockPath(name + "_top")
        );
    }

    public void genPanelSlabFull(Block block, String name) {
        this.panelSlabBlock(
                (SlabBlock) block, name,
                blockPath(name + "_tiles"),
                blockPath(name + "_tiles"),
                blockPath(name + "_tiles")
        );
    }

    public void panelSlabBlock(SlabBlock block, String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        this.slabBlock(block,
                models().slab(name + "_slab", side, bottom, top),
                models().slabTop(name + "_slab_top", side, bottom, top),
                models().cubeBottomTop(name + "_slab_double", side, bottom, top));
    }

    public void genPanelStairs(Block block, String name) {
        this.stairsBlock(
                (StairsBlock) block,
                blockPath(name + "_tiles"),
                blockPath(name + "_bottom"),
                blockPath(name + "_top")
        );
    }

    public void genPanelStairsFull(Block block, String name) {
        this.stairsBlock((StairsBlock) block, blockPath(name + "_tiles"));
    }

    @Nonnull
    public static ResourceLocation blockPath(String name) {
        return new ResourceLocation(PortalMod.MODID, "block/" + name);
    }
}
