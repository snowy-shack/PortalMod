package net.portalmod.core.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.portalmod.PortalMod;
import net.portalmod.client.render.WatermarkRenderer;
import net.portalmod.common.items.ModSpawnEggItem;
import net.portalmod.common.sorted.antline.AntlineBakedModel;
import net.portalmod.common.sorted.antline.AntlineLoader;
import net.portalmod.common.sorted.button.SuperButtonGeometry;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.GabeRenderer;
import net.portalmod.common.sorted.cube.companion.CompanionCubeRenderer;
import net.portalmod.common.sorted.cube.storage.StorageCubeRenderer;
import net.portalmod.common.sorted.cube.vintage.VintageCubeRenderer;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.common.sorted.portal.PortalEntityRenderer;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunGeometry;
import net.portalmod.common.sorted.portalgun.PortalGunItemColor;
import net.portalmod.common.sorted.sign.ChamberSignRenderer;
import net.portalmod.common.sorted.turret.TurretEntity;
import net.portalmod.common.sorted.turret.TurretRenderer;
import net.portalmod.core.init.*;
import net.portalmod.common.sorted.portalgun.skins.SkinManager;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        SkinManager.getClientInstance().onClientStartup();

//        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PortalModOptionsScreen.CONFIG, "portalmod-client.toml");

        KeyInit.init();
        Minecraft.getInstance().getMainRenderTarget().enableStencil();

        RenderTypeLookup.setRenderLayer(BlockInit.ANTLINE.get(),                 RenderType.cutout());
//        RenderTypeLookup.setRenderLayer(BlockInit.LASER_EMITTER.get(),           RenderType.cutout());
//        RenderTypeLookup.setRenderLayer(BlockInit.LASER_CATCHER.get(),           RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.WIRE_MESH_BLOCK.get(),         RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.WIRE_MESH.get(),               RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.IRON_FRAME.get(),              RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.BARRED_IRON_FRAME.get(),       RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.MESHED_IRON_FRAME.get(),       RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.RUSTY_IRON_FRAME.get(),        RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.RUSTY_BARRED_IRON_FRAME.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.RUSTY_MESHED_IRON_FRAME.get(), RenderType.cutout());

        // This is for rendering the vines
        RenderTypeLookup.setRenderLayer(BlockInit.ARBORED_BLACKPLATE.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.ARBORED_BLACKPLATE_SLAB.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.ARBORED_BLACKPLATE_STAIRS.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.ARBORED_BLACKPLATE_PLATFORM.get(), RenderType.cutout());

//        RenderTypeLookup.setRenderLayer(BlockInit.LASER_RELAY.get(),     RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.CHAMBER_LIGHTS.get(),  RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.CUBE_DROPPER.get(),    RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.FIZZLER_EMITTER.get(), RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.FIZZLER_FIELD.get(),   RenderType.translucent());
        RenderTypeLookup.setRenderLayer(BlockInit.PROPULSION_GEL.get(),  RenderType.translucent()); // Test
        RenderTypeLookup.setRenderLayer(BlockInit.REPULSION_GEL.get(),   RenderType.translucent()); // Test
        RenderTypeLookup.setRenderLayer(BlockInit.STANDING_BUTTON.get(), RenderType.cutout());

//        RenderTypeLookup.setRenderLayer(BlockInit.GOO.get(),         RenderType.translucent());
//        RenderTypeLookup.setRenderLayer(FluidInit.GOO_FLUID.get(),   RenderType.translucent());
//        RenderTypeLookup.setRenderLayer(FluidInit.GOO_FLOWING.get(), RenderType.translucent());

        ClientRegistry.bindTileEntityRenderer(TileEntityTypeInit.FAITHPLATE.get(), FaithPlateTER::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.COMPANION_CUBE.get(), CompanionCubeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.STORAGE_CUBE.get(), StorageCubeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.VINTAGE_CUBE.get(), VintageCubeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.GABE.get(), GabeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.PORTAL.get(), PortalEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.TURRET.get(), TurretRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.CHAMBER_SIGN.get(), ChamberSignRenderer::new);
        Minecraft.getInstance().getItemColors().register(new PortalGunItemColor(), ItemInit.PORTALGUN.get());

//        APIWrapper.init();

        event.enqueueWork(() -> {
            registerItemProperty(ItemInit.PORTALGUN.get(), "color",  PortalGun::getColorOverride);
//            registerItemProperty(ItemInit.PORTALGUN.get(), "grab",   PortalGun::getGrabOverride);
//            registerItemProperty(ItemInit.PORTALGUN.get(), "accent", PortalGun::getAccentOverride);

            ShaderInit.REGISTRY.registerAll();
        });
    }

    private static void registerItemProperty(Item item, String name, IItemPropertyGetter getter) {
        ItemModelsProperties.register(item, new ResourceLocation(PortalMod.MODID, name), getter);
    }

    @SubscribeEvent
    public static void registerAttributes(final EntityAttributeCreationEvent event) {
        event.put(EntityInit.COMPANION_CUBE.get(), Cube.createAttributes().build());
        event.put(EntityInit.STORAGE_CUBE.get(), Cube.createAttributes().build());
        event.put(EntityInit.VINTAGE_CUBE.get(), Cube.createAttributes().build());
        event.put(EntityInit.GABE.get(), Cube.createAttributes().build());
        event.put(EntityInit.TURRET.get(), TurretEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterSpawnEggs(RegistryEvent.Register<EntityType<?>> event) {
        ModSpawnEggItem.register();
    }

    @SubscribeEvent
    public static void onLoaderRegister(ModelRegistryEvent event) {
        registerLoader("antline",           new AntlineLoader());
        registerLoader("super_button",      new SuperButtonGeometry.Loader());
        registerLoader("portalgun",         new PortalGunGeometry.Loader());
    }

    private static void registerLoader(String name, IModelLoader<?> loader) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(PortalMod.MODID, name), loader);
    }

    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) {
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        TextureInit.register(event);

        if(event.getMap().location() == AtlasTexture.LOCATION_BLOCKS) {
            for(ResourceLocation texture : AntlineBakedModel.getAllTextures())
                event.addSprite(texture);
//            for(ResourceLocation texture : FaithPlateTargetBakedModel.getAllTextures())
//                event.addSprite(texture);
            event.addSprite(FaithPlateTER.TEXTURE_BLUE);
            event.addSprite(FaithPlateTER.TEXTURE_ORANGE);
            event.addSprite(WatermarkRenderer.WM_LEFT);
            event.addSprite(WatermarkRenderer.WM_RIGHT);
        }
    }
}