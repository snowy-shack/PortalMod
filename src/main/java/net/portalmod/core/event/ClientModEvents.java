package net.portalmod.core.event;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import net.fabricmc.api.EnvType;
import net.portalmod.PortalMod;
import net.portalmod.common.items.ModSpawnEggItem;
import net.portalmod.common.sorted.antline.AntlineBakedModel;
import net.portalmod.common.sorted.autoportal.AutoPortalTER;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.GabeRenderer;
import net.portalmod.common.sorted.cube.companion.CompanionCubeRenderer;
import net.portalmod.common.sorted.cube.storage.StorageCubeRenderer;
import net.portalmod.common.sorted.cube.vintage.VintageCubeRenderer;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.common.sorted.portal.PortalEntityRenderer;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunItemColor;
import net.portalmod.common.sorted.portalgun.skins.ClientSkinManager;
import net.portalmod.common.sorted.sign.ChamberSignRenderer;
import net.portalmod.common.sorted.trigger.TriggerTER;
import net.portalmod.common.sorted.turret.TurretEntity;
import net.portalmod.common.sorted.turret.TurretRenderer;
import net.portalmod.core.init.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.item.Item;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

/**
 * Client setup: render layers, renderers, item properties and custom models.
 * Forge ran this from FMLClientSetupEvent and other mod-bus events; Fabric runs {@link #register()}
 * from the client entrypoint.
 */
@Environment(EnvType.CLIENT)
public class ClientModEvents {

    public static void register() {
        clientSetup();
        // The window and main framebuffer only exist once the client has fully started.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ((net.portalmod.core.interfaces.StencilTarget) client.getMainRenderTarget()).pmEnableStencil();
            ShaderInit.REGISTRY.registerAll();
            // Needs the texture manager, which exists only once the client has started.
            ClientSkinManager.getInstance().onClientStartup();
            // Dev check: -Dportalmod.auditMixins=true applies every mixin now, so broken
            // injection points fail at startup instead of later in game.
            if (Boolean.getBoolean("portalmod.auditMixins"))
                org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
        });
        ModelLoadingPlugin.register(net.portalmod.client.render.PortalModModels::initialize);
    }

    private static void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.autoportal.AutoPortalModel.LAYER, net.portalmod.common.sorted.autoportal.AutoPortalModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.portalgun.PortalGunModel.LAYER, net.portalmod.common.sorted.portalgun.PortalGunModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.faithplate.FaithPlatePlateModel.LAYER, net.portalmod.common.sorted.faithplate.FaithPlatePlateModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.cube.CubeModel.LAYER, net.portalmod.common.sorted.cube.CubeModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.turret.TurretModel.LAYER, net.portalmod.common.sorted.turret.TurretModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.turret.TurretModelOld.LAYER, net.portalmod.common.sorted.turret.TurretModelOld::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.creer.CreerModel.LAYER, () -> net.portalmod.common.sorted.creer.CreerModel.createBodyLayer(CubeDeformation.NONE));
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.creer.CreerModel.ARMOR_LAYER, () -> net.portalmod.common.sorted.creer.CreerModel.createBodyLayer(new CubeDeformation(2.0F)));
        EntityModelLayerRegistry.registerModelLayer(net.portalmod.common.sorted.longfallboots.LongFallBootsModel.LAYER, net.portalmod.common.sorted.longfallboots.LongFallBootsModel::createBodyLayer);
    }

    private static void clientSetup() {
        registerModelLayers();
        net.portalmod.common.sorted.longfallboots.LongFallBootsClient.register();

        KeyInit.init();

        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.ANTLINE.get(),                 RenderType.cutout());
//        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.LASER_EMITTER.get(),           RenderType.cutout());
//        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.LASER_CATCHER.get(),           RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.WIRE_MESH_BLOCK.get(),         RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.WIRE_MESH.get(),               RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.IRON_FRAME.get(),              RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.BARRED_IRON_FRAME.get(),       RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.MESHED_IRON_FRAME.get(),       RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.RUSTY_IRON_FRAME.get(),        RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.RUSTY_BARRED_IRON_FRAME.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.RUSTY_MESHED_IRON_FRAME.get(), RenderType.cutout());

        // This is for rendering the vines
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.ARBORED_BLACKPLATE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.ARBORED_BLACKPLATE_SLAB.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.ARBORED_BLACKPLATE_STAIRS.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.ARBORED_BLACKPLATE_PLATFORM.get(), RenderType.cutout());

//        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.LASER_RELAY.get(),     RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.CHAMBER_LIGHTS.get(),  RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.CUBE_DROPPER.get(),    RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.FIZZLER_EMITTER.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.FIZZLER_FIELD.get(),   RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.PROPULSION_GEL.get(),  RenderType.translucent()); // Test
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.REPULSION_GEL.get(),   RenderType.translucent()); // Test
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.STANDING_BUTTON.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BlockInit.TRIGGER.get(),         RenderType.cutout());

        BlockEntityRenderers.register(TileEntityTypeInit.FAITHPLATE.get(), FaithPlateTER::new);
        BlockEntityRenderers.register(TileEntityTypeInit.TRIGGER.get(), TriggerTER::new);
        BlockEntityRenderers.register(TileEntityTypeInit.AUTOPORTAL.get(), AutoPortalTER::new);
        EntityRendererRegistry.register(EntityInit.COMPANION_CUBE.get(), CompanionCubeRenderer::new);
        EntityRendererRegistry.register(EntityInit.STORAGE_CUBE.get(), StorageCubeRenderer::new);
        EntityRendererRegistry.register(EntityInit.VINTAGE_CUBE.get(), VintageCubeRenderer::new);
        EntityRendererRegistry.register(EntityInit.GABE.get(), GabeRenderer::new);
        EntityRendererRegistry.register(EntityInit.PORTAL.get(), PortalEntityRenderer::new);
        EntityRendererRegistry.register(EntityInit.TURRET.get(), TurretRenderer::new);
        EntityRendererRegistry.register(EntityInit.CHAMBER_SIGN.get(), ChamberSignRenderer::new);
        ColorProviderRegistry.ITEM.register(new PortalGunItemColor(), ItemInit.PORTALGUN.get());
        // Portal gun is drawn in code (its item model has parent builtin/entity).
        net.portalmod.common.sorted.portalgun.PortalGunISTER portalGunRenderer = new net.portalmod.common.sorted.portalgun.PortalGunISTER();
        BuiltinItemRendererRegistry.INSTANCE.register(ItemInit.PORTALGUN.get(), portalGunRenderer::renderByItem);

        registerItemProperty(ItemInit.PORTALGUN.get(), "color", (stack, level, entity, seed) -> PortalGun.getColorOverride(stack, level, entity));

        FluidRenderHandlerRegistry.INSTANCE.register(FluidInit.GOO_FLUID.get(), FluidInit.GOO_FLOWING.get(),
                new SimpleFluidRenderHandler(new ResourceLocation(PortalMod.MODID, "block/goo_still"),
                        new ResourceLocation(PortalMod.MODID, "block/goo_flow")));

    }

    private static void registerItemProperty(Item item, String name, ClampedItemPropertyFunction getter) {
        ItemProperties.register(item, new ResourceLocation(PortalMod.MODID, name), getter);
    }
}
