package io.github.serialsniper.portalmod.core.event;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.client.render.PortalShaders;
import io.github.serialsniper.portalmod.client.render.entity.CreerRenderer;
import io.github.serialsniper.portalmod.client.render.model.loader.AntlineLoader;
import io.github.serialsniper.portalmod.client.render.model.loader.PortalableLoader;
import io.github.serialsniper.portalmod.client.render.entity.CompanionCubeRenderer;
import io.github.serialsniper.portalmod.client.render.model.AntlineBakedModel;
import io.github.serialsniper.portalmod.client.render.model.PortalableBakedModel;
import io.github.serialsniper.portalmod.client.render.ter.FaithPlateTER;
import io.github.serialsniper.portalmod.client.render.ter.PortalableBlockTER;
import io.github.serialsniper.portalmod.client.screens.FaithPlateConfigScreen;
import io.github.serialsniper.portalmod.common.entities.CompanionCube;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.core.init.BlockInit;
import io.github.serialsniper.portalmod.core.init.EntityInit;
import io.github.serialsniper.portalmod.core.init.ItemInit;
import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.client.registry.*;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.*;
import net.minecraftforge.fml.event.lifecycle.*;

import java.io.*;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ModEvents {
	
	@SubscribeEvent
	public static void clientSetup(final FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(BlockInit.PORTALABLE_BLOCK.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockInit.ANTLINE.get(), RenderType.cutout());

		ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.PORTABLE_BLOCK.get(), PortalableBlockTER::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.FAITHPLATE.get(), FaithPlateTER::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.COMPANION_CUBE.get(), CompanionCubeRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.CREER.get(), CreerRenderer::new);

		event.enqueueWork(ModEvents::register);
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(EntityInit.COMPANION_CUBE.get(), CompanionCube.createAttributes().build());
		event.put(EntityInit.CREER.get(), CreeperEntity.createAttributes().build());
	}
	
	public static void register() {
		ItemModelsProperties.register(ItemInit.PORTALGUN.get(), new ResourceLocation(PortalMod.MODID, "end"), PortalGun::getModelOverride);

		try {
			PortalMod.shaders = new PortalShaders();
			FaithPlateConfigScreen.shader = new PortalShaders(
					new ResourceLocation(PortalMod.MODID, "shaders/gui/vertex.vsh"),
					new ResourceLocation(PortalMod.MODID, "shaders/gui/fragment.fsh"));
			FaithPlateConfigScreen.gridShader = new PortalShaders(
					new ResourceLocation(PortalMod.MODID, "shaders/gui/grid/vertex.vsh"),
					new ResourceLocation(PortalMod.MODID, "shaders/gui/grid/fragment.fsh"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public static void onLoaderRegister(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(new ResourceLocation(PortalMod.MODID, "portalable_block"), new PortalableLoader());
		ModelLoaderRegistry.registerLoader(new ResourceLocation(PortalMod.MODID, "antline"), new AntlineLoader());
	}

//	@SubscribeEvent
//	public static void onModelBakeEvent(ModelBakeEvent event) {
//		ResourceLocation location = ItemInit.PORTALGUN.get().getRegistryName();
//		IBakedModel model = event.getModelRegistry().get(location);
//		event.getModelRegistry().put(location, new PortalGunModel(model).getModel());
//	}

	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		if(event.getMap().location() == AtlasTexture.LOCATION_BLOCKS) {
			for(ResourceLocation texture : PortalableBakedModel.getAllTextures())
				event.addSprite(texture);
			for(ResourceLocation texture : AntlineBakedModel.getAllTextures())
				event.addSprite(texture);
		}
	}
}