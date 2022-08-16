package io.github.serialsniper.portalmod.core.init;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.blocks.AntlineBlock;
import io.github.serialsniper.portalmod.common.blocks.FaithPlateBlock;
import io.github.serialsniper.portalmod.common.blocks.PortalableBlock;
import io.github.serialsniper.portalmod.common.blocks.RadioBlock;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.registries.*;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PortalMod.MODID);
    public static final RegistryObject<PortalableBlock> PORTALABLE_BLOCK = BLOCKS.register("portalable_block",
            () -> new PortalableBlock(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE).lightLevel(i -> 10)));
    public static final RegistryObject<Block> UNPORTALABLE_BLOCK = BLOCKS.register("unportalable_block",
            () -> new Block(AbstractBlock.Properties.copy(Blocks.BLACK_CONCRETE)));
    public static final RegistryObject<RadioBlock> RADIO = BLOCKS.register("radio",
            () -> new RadioBlock(AbstractBlock.Properties.of(Material.DECORATION).strength(4.0F)));
    public static final RegistryObject<AntlineBlock> ANTLINE = BLOCKS.register("antline",
            () -> new AntlineBlock(AbstractBlock.Properties.copy(Blocks.REDSTONE_WIRE).lightLevel(i -> 7).emissiveRendering(($0, $1, $2) -> true)));
    public static final RegistryObject<FaithPlateBlock> FAITHPLATE = BLOCKS.register("faithplate",
            () -> new FaithPlateBlock(AbstractBlock.Properties.copy(Blocks.STONE)));
}