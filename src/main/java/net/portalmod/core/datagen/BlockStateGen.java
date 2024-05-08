package net.portalmod.core.datagen;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.portalmod.PortalMod;
import net.portalmod.core.init.BlockInit;

public class BlockStateGen extends BlockStateProvider {

    public BlockStateGen(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, PortalMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        genPanelSlab(BlockInit.LUNECAST_SLAB.get(), "lunecast");
        genPanelSlab(BlockInit.ARBORED_LUNECAST_SLAB.get(), "arbored_lunecast");
        genPanelSlab(BlockInit.ERODED_LUNECAST_SLAB.get(), "eroded_lunecast");
        genPanelSlab(BlockInit.FRACTURED_LUNECAST_SLAB.get(), "fractured_lunecast");
        genPanelSlab(BlockInit.VINTAGE_LUNECAST_SLAB.get(), "vintage_lunecast");

        genPanelSlab(BlockInit.BLACKPLATE_SLAB.get(), "blackplate");
        genPanelSlab(BlockInit.ARBORED_BLACKPLATE_SLAB.get(), "arbored_blackplate");
        genPanelSlab(BlockInit.ERODED_BLACKPLATE_SLAB.get(), "eroded_blackplate");
        genPanelSlab(BlockInit.FRACTURED_BLACKPLATE_SLAB.get(), "fractured_blackplate");
        genPanelSlab(BlockInit.VINTAGE_BLACKPLATE_SLAB.get(), "vintage_blackplate");

        genPanelStairs(BlockInit.LUNECAST_STAIRS.get(), "lunecast");
        genPanelStairs(BlockInit.ARBORED_LUNECAST_STAIRS.get(), "arbored_lunecast");
        genPanelStairs(BlockInit.ERODED_LUNECAST_STAIRS.get(), "eroded_lunecast");
        genPanelStairs(BlockInit.FRACTURED_LUNECAST_STAIRS.get(), "fractured_lunecast");
        genPanelStairs(BlockInit.VINTAGE_LUNECAST_STAIRS.get(), "vintage_lunecast");

        genPanelStairs(BlockInit.BLACKPLATE_STAIRS.get(), "blackplate");
        genPanelStairs(BlockInit.ARBORED_BLACKPLATE_STAIRS.get(), "arbored_blackplate");
        genPanelStairs(BlockInit.ERODED_BLACKPLATE_STAIRS.get(), "eroded_blackplate");
        genPanelStairs(BlockInit.FRACTURED_BLACKPLATE_STAIRS.get(), "fractured_blackplate");
        genPanelStairs(BlockInit.VINTAGE_BLACKPLATE_STAIRS.get(), "vintage_blackplate");

    }

    public void genPanelSlab(Block block, String name) {
        this.slabBlock(
                (SlabBlock) block,
                new ResourceLocation(PortalMod.MODID,  "block/" + name + "_tiles"),
                new ResourceLocation(PortalMod.MODID,  "block/" + name + "_tiles"),
                new ResourceLocation(PortalMod.MODID,  "block/" + name + "_bottom"),
                new ResourceLocation(PortalMod.MODID,  "block/" + name + "_top")
        );
    }

    public void genPanelStairs(Block block, String name) {
        this.stairsBlock(
                (StairsBlock) block,
                new ResourceLocation(PortalMod.MODID,  "block/" + name + "_tiles"),
                new ResourceLocation(PortalMod.MODID,  "block/" + name + "_bottom"),
                new ResourceLocation(PortalMod.MODID,  "block/" + name + "_top")
        );
    }
}
