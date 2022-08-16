package io.github.serialsniper.portalmod.core.event;

import java.io.IOException;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.client.KeyInit;
import io.github.serialsniper.portalmod.client.render.PortalGunItemColor;
import io.github.serialsniper.portalmod.client.render.PortalShaders;
import io.github.serialsniper.portalmod.client.render.entity.CompanionCubeRenderer;
import io.github.serialsniper.portalmod.client.render.entity.CreerRenderer;
import io.github.serialsniper.portalmod.client.render.entity.PortalEntityRenderer;
import io.github.serialsniper.portalmod.client.render.entity.StorageCubeRenderer;
import io.github.serialsniper.portalmod.client.render.model.AntlineBakedModel;
import io.github.serialsniper.portalmod.client.render.model.FaithPlateTargetBakedModel;
import io.github.serialsniper.portalmod.client.render.model.PortalableBakedModel;
import io.github.serialsniper.portalmod.client.render.model.loader.AntlineLoader;
import io.github.serialsniper.portalmod.client.render.model.loader.PortalableLoader;
import io.github.serialsniper.portalmod.client.render.ter.FaithPlateTER;
import io.github.serialsniper.portalmod.client.screens.FaithPlateConfigScreen;
import io.github.serialsniper.portalmod.common.entities.AbstractCube;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.core.init.BlockInit;
import io.github.serialsniper.portalmod.core.init.EntityInit;
import io.github.serialsniper.portalmod.core.init.ItemInit;
import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ModEvents {
	
	@SubscribeEvent
	public static void clientSetup(final FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(BlockInit.PORTALABLE_BLOCK.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(BlockInit.ANTLINE.get(), RenderType.cutout());

//		ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.PORTABLE_BLOCK.get(), PortalableBlockTER::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.FAITHPLATE.get(), FaithPlateTER::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.COMPANION_CUBE.get(), CompanionCubeRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.STORAGE_CUBE.get(), StorageCubeRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.CREER.get(), CreerRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.PORTAL.get(), PortalEntityRenderer::new);
		Minecraft.getInstance().getItemColors().register(new PortalGunItemColor(), ItemInit.PORTALGUN.get());

		KeyInit.init();
        Minecraft.getInstance().getMainRenderTarget().enableStencil();

		event.enqueueWork(ModEvents::register);
	}
	
	@SubscribeEvent
	public static void registerAttributes(final EntityAttributeCreationEvent event) {
		event.put(EntityInit.COMPANION_CUBE.get(), AbstractCube.createAttributes().build());
		event.put(EntityInit.STORAGE_CUBE.get(), AbstractCube.createAttributes().build());
		event.put(EntityInit.CREER.get(), CreeperEntity.createAttributes().build());
	}
	
	public static void register() {
		ItemModelsProperties.register(ItemInit.PORTALGUN.get(), new ResourceLocation(PortalMod.MODID, "color"), PortalGun::getColorOverride);
        ItemModelsProperties.register(ItemInit.PORTALGUN.get(), new ResourceLocation(PortalMod.MODID, "grab"), PortalGun::getGrabOverride);
        ItemModelsProperties.register(ItemInit.PORTALGUN.get(), new ResourceLocation(PortalMod.MODID, "accent"), PortalGun::getAccentOverride);

		try {
			PortalMod.shaders = new PortalShaders();
			PortalMod.portalFrameShader = new PortalShaders("portal/vertex", "portal/frame");
			PortalMod.portalHighlightShader = new PortalShaders("portal/vertex", "portal/highlight");
			PortalMod.portalInnerShader = new PortalShaders("portal/inner");
            PortalMod.portalViewShader = new PortalShaders("portal/vertex", "portal/view");
			FaithPlateConfigScreen.shader = new PortalShaders("gui/vertex", "gui/fragment");
			FaithPlateConfigScreen.gridShader = new PortalShaders("gui/vertex", "gui/grid");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public static void onLoaderRegister(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(new ResourceLocation(PortalMod.MODID, "portalable_block"), new PortalableLoader());
		ModelLoaderRegistry.registerLoader(new ResourceLocation(PortalMod.MODID, "antline"), new AntlineLoader());
	}
	
	@SubscribeEvent
	public static void onModelBakeEvent(ModelBakeEvent event) {
//		System.out.println("BAKE");
//		ResourceLocation location = ItemInit.PORTALGUN.get().getRegistryName();
//		IBakedModel model = event.getModelRegistry().get(location);
//		event.getModelRegistry().put(location, new PortalGunModel(model));
	}
	
	@SuppressWarnings("deprecation")
    @SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		if(event.getMap().location() == AtlasTexture.LOCATION_BLOCKS) {
			for(ResourceLocation texture : PortalableBakedModel.getAllTextures())
				event.addSprite(texture);
			for(ResourceLocation texture : AntlineBakedModel.getAllTextures())
				event.addSprite(texture);
			for(ResourceLocation texture : FaithPlateTargetBakedModel.getAllTextures())
				event.addSprite(texture);
		}
	}
}