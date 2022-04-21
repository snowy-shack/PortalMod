package io.github.serialsniper.portalmod.core.init;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.common.blockentities.AntlineTileEntity;
import io.github.serialsniper.portalmod.common.blockentities.FaithPlateTileEntity;
import io.github.serialsniper.portalmod.common.blockentities.PortalableBlockTileEntity;
import io.github.serialsniper.portalmod.common.blockentities.RadioBlockTileEntity;
import net.minecraft.tileentity.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.registries.*;

public class TileEntityTypeInit {
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, PortalMod.MODID);
	
	public static final RegistryObject<TileEntityType<PortalableBlockTileEntity>> PORTABLE_BLOCK = TILE_ENTITY_TYPES.register("portable_block",
			() -> TileEntityType.Builder.of(PortalableBlockTileEntity::new, BlockInit.PORTALABLE_BLOCK.get()).build(null));
	
	public static final RegistryObject<TileEntityType<RadioBlockTileEntity>> RADIO = TILE_ENTITY_TYPES.register("radio",
			() -> TileEntityType.Builder.of(RadioBlockTileEntity::new, BlockInit.RADIO.get()).build(null));

//	public static final RegistryObject<TileEntityType<StencilBoxTileEntity>> STENCILBOX = TILE_ENTITY_TYPES.register("stencilbox",
//			() -> TileEntityType.Builder.of(StencilBoxTileEntity::new, BlockInit.STENCILBOX.get()).build(null));

	public static final RegistryObject<TileEntityType<AntlineTileEntity>> ANTLINE = TILE_ENTITY_TYPES.register("antline",
			() -> TileEntityType.Builder.of(AntlineTileEntity::new, BlockInit.ANTLINE.get()).build(null));

	public static final RegistryObject<TileEntityType<FaithPlateTileEntity>> FAITHPLATE = TILE_ENTITY_TYPES.register("faithplate",
			() -> TileEntityType.Builder.of(FaithPlateTileEntity::new, BlockInit.FAITHPLATE.get()).build(null));
}