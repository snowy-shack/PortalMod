package io.github.serialsniper.portalmod.core.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.client.KeyInit;
import io.github.serialsniper.portalmod.client.render.entity.PortalEntityRenderer;
import io.github.serialsniper.portalmod.client.render.ter.PortalableBlockTER;
import io.github.serialsniper.portalmod.client.screens.PortalOptionsScreen;
import io.github.serialsniper.portalmod.client.util.PortalLocation;
import io.github.serialsniper.portalmod.common.entities.AbstractCube;
import io.github.serialsniper.portalmod.common.entities.PortalEntity;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import io.github.serialsniper.portalmod.core.enums.PortalGunInteraction;
import io.github.serialsniper.portalmod.core.init.ItemInit;
import io.github.serialsniper.portalmod.core.init.PacketInit;
import io.github.serialsniper.portalmod.core.packet.PortalGunInteractionPacket;
import io.github.serialsniper.portalmod.mixins.MainMenuScreenAccessor;
import io.github.serialsniper.portalmod.mixins.MinecraftAccessor;
import io.github.serialsniper.portalmod.mixins.SplashesAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.Splashes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    public static final ResourceLocation CROSSHAIRS = new ResourceLocation(PortalMod.MODID, "textures/crosshairs.png");
    public static List<Block> blocks = new ArrayList<>();
    public static List<PortalLocation> portals = new ArrayList<>();
    public static boolean canRenderPortal = false;
    public static boolean recursion = false;
    public static class Block {
        public TileEntity te;
        public MatrixStack matrixStack;
        public IRenderTypeBuffer renderBuffer;
        public int combinedLight;
        public Block(TileEntity te, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int combinedLight) {
            this.te = te;
            this.matrixStack = matrixStack;
            this.renderBuffer = renderBuffer;
            this.combinedLight = combinedLight;
        }
    }
    public static void addPortal(Matrix4f matrix, Direction side, PortalEnd end, BlockPos pos, BlockPos otherPos, TileEntity tileEntity, MatrixStack stack, BlockState state) {
        if(!recursion)
            portals.add(new PortalLocation(matrix, side, end, pos, otherPos, tileEntity, stack, state));
    }
    public static void addBlock(TileEntity te, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int combinedLight) {
        blocks.add(new Block(te, matrixStack, renderBuffer, combinedLight));
    }
    public static void clearBlocks() {
        blocks.clear();
    }
    public static void clearPortals() {
        portals.clear();
    }
    private static boolean wasPressed = false;




    private static final ResourceLocation EDITION = new ResourceLocation(PortalMod.MODID, "textures/gui/title/edition.png");
    private static final RenderSkyboxCube CUBEMAP = new RenderSkyboxCube(new ResourceLocation(PortalMod.MODID, "textures/gui/title/background/panorama"));
    private static final ResourceLocation SPLASHES = new ResourceLocation(PortalMod.MODID, "texts/splashes.txt");
    private static ResourceLocation prevEdition;
    private static RenderSkyboxCube prevCubeMap;
    private static ResourceLocation prevSplashes;
	private static boolean fadingMainMenu = true;
	public static boolean toBeUpdatedMainMenu = true;
    
    public static void changeMainMenuResources(boolean custom) {
        if(prevEdition == null)
            prevEdition = MainMenuScreenAccessor.portalmod_getEdition();
        if(prevCubeMap == null)
            prevCubeMap = MainMenuScreenAccessor.portalmod_getCubeMap();
        if(prevSplashes == null)
            prevSplashes = SplashesAccessor.portalmod_getLocation();

        MainMenuScreenAccessor.portalmod_setEdition(custom ? EDITION : prevEdition);
        MainMenuScreenAccessor.portalmod_setCubeMap(custom ? CUBEMAP : prevCubeMap);
        SplashesAccessor.portalmod_setLocation(custom ? SPLASHES : prevSplashes);

        try {
            Minecraft minecraft = Minecraft.getInstance();
            Splashes splashes = new Splashes(Minecraft.getInstance().getUser());
            
            Method prepare = ObfuscationReflectionHelper.findMethod(Splashes.class, "prepare", IResourceManager.class, IProfiler.class);
            Method apply = ObfuscationReflectionHelper.findMethod(Splashes.class, "apply", List.class, IResourceManager.class, IProfiler.class);

//            Method prepare = Splashes.class.getDeclaredMethod("prepare", IResourceManager.class, IProfiler.class);
//            Method apply = Splashes.class.getDeclaredMethod("apply", List.class, IResourceManager.class, IProfiler.class);
            prepare.setAccessible(true);
            apply.setAccessible(true);
            List<String> splashList = (List<String>)prepare.invoke(splashes, minecraft.getResourceManager(), minecraft.getProfiler());
            apply.invoke(splashes, splashList, minecraft.getResourceManager(), minecraft.getProfiler());


        	Field splashManager = ObfuscationReflectionHelper.findField(Minecraft.class, "splashManager");
//            Field splashManager = Minecraft.class.getDeclaredField("splashManager");
            splashManager.setAccessible(true);
            splashManager.set(Minecraft.getInstance(), splashes);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static MainMenuScreen getMainMenu(boolean custom, boolean fadeIn) {
        if(prevEdition == null)
            prevEdition = MainMenuScreenAccessor.portalmod_getEdition();
        if(prevCubeMap == null)
            prevCubeMap = MainMenuScreenAccessor.portalmod_getCubeMap();
        if(prevSplashes == null)
            prevSplashes = SplashesAccessor.portalmod_getLocation();

        MainMenuScreenAccessor.portalmod_setEdition(custom ? EDITION : prevEdition);
        MainMenuScreenAccessor.portalmod_setCubeMap(custom ? CUBEMAP : prevCubeMap);
        SplashesAccessor.portalmod_setLocation(custom ? SPLASHES : prevSplashes);

        try {
            Minecraft minecraft = Minecraft.getInstance();
            Splashes splashes = new Splashes(Minecraft.getInstance().getUser());
            
            List<String> splashList = ((SplashesAccessor)splashes).portalmod_prepare(minecraft.getResourceManager(), minecraft.getProfiler());
            ((SplashesAccessor)splashes).portalmod_apply(splashList, minecraft.getResourceManager(), minecraft.getProfiler());
            ((MinecraftAccessor)Minecraft.getInstance()).portalmod_setSplashManager(splashes);
            
//            Method prepare = ObfuscationReflectionHelper.findMethod(Splashes.class, "prepare", IResourceManager.class, IProfiler.class);
//            Method apply = ObfuscationReflectionHelper.findMethod(Splashes.class, "apply", List.class, IResourceManager.class, IProfiler.class);

//            Method prepare = Splashes.class.getDeclaredMethod("prepare", IResourceManager.class, IProfiler.class);
//            Method apply = Splashes.class.getDeclaredMethod("apply", List.class, IResourceManager.class, IProfiler.class);
//            prepare.setAccessible(true);
//            apply.setAccessible(true);
//            List<String> splashList = (List<String>)prepare.invoke(splashes, minecraft.getResourceManager(), minecraft.getProfiler());
//            apply.invoke(splashes, splashList, minecraft.getResourceManager(), minecraft.getProfiler());
            
//        	Field splashManager = ObfuscationReflectionHelper.findField(Minecraft.class, "splashManager");
//            Field splashManager = Minecraft.class.getDeclaredField("splashManager");
//            splashManager.setAccessible(true);
//            splashManager.set(Minecraft.getInstance(), splashes);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return new MainMenuScreen(false);
    }
    
    @SubscribeEvent
    public static void screenOpen(final GuiOpenEvent event) {
    	if(!(event.getGui() instanceof MainMenuScreen) || !toBeUpdatedMainMenu)
    		return;
    	
		event.setGui(getMainMenu(PortalOptionsScreen.MENU.get(), fadingMainMenu));
		toBeUpdatedMainMenu = false;
		fadingMainMenu = false;
    }
    
    @SubscribeEvent
    public static void clientTick(final TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.START)
            return;

        World level = Minecraft.getInstance().level;
        PlayerEntity player = Minecraft.getInstance().player;

        if(KeyInit.PORTALGUN_INTERACT.isDown()) {
            if(player.getMainHandItem().getItem() == ItemInit.PORTALGUN.get() && !wasPressed) {
//				int rayLength = 100;
//				Vector3d playerRotation = Minecraft.getInstance().player.getViewVector(0);
//				Vector3d rayPath = playerRotation.scale(rayLength);
//
//				Vector3d from = Minecraft.getInstance().player.getEyePosition(0);
//				Vector3d to = from.add(rayPath);
//
//				RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null);
//				BlockRayTraceResult rayHit = Minecraft.getInstance().level.clip(rayCtx);

                // todo ray trace on the server too

//				if(rayHit.getType() == RayTraceResult.Type.MISS) {
//					return;
//				}

                if(player.hasPassenger(AbstractCube.class)) {
                    PortalGun.dropCube(player, false);
                    PacketInit.INSTANCE.sendToServer(new PortalGunInteractionPacket.Server.Builder(PortalGunInteraction.DROP_CUBE).build());

                    consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
                } else {
                    try {
                        Entity cube = Minecraft.getInstance().crosshairPickEntity;

                        if(cube instanceof AbstractCube) {
                            cube.startRiding(player);
                            PacketInit.INSTANCE.sendToServer(new PortalGunInteractionPacket.Server.Builder(PortalGunInteraction.PICK_CUBE).data(cube.getId()).build());

                            consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            wasPressed = true;
        } else {
            wasPressed = false;
        }
    }

    private static void consumeAllKeyPresses(InputMappings.Input k) {
        KeyBinding[] keys = Minecraft.getInstance().options.keyMappings;
        for(KeyBinding key : keys)
            if(key.getKey() == k)
                while(key.consumeClick());
    }

    @SubscribeEvent
    public static void onMouseClick(final InputEvent.ClickInputEvent event) {
        if(event.getHand() != Hand.MAIN_HAND)
            return;

        if(Minecraft.getInstance().player.getMainHandItem().getItem() == ItemInit.PORTALGUN.get()) {
            if(event.isAttack())
                PortalGun.handleLeftClick();
            if(event.isUseItem())
                PortalGun.handleRightClick();
            if(event.isPickBlock())
                return;

            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }
    @SubscribeEvent
    public static void onFogRender(final EntityViewRenderEvent.FogColors event) {
        PortalableBlockTER.clearColor.setX(event.getRed());
        PortalableBlockTER.clearColor.setY(event.getGreen());
        PortalableBlockTER.clearColor.setZ(event.getBlue());
    }

    @SubscribeEvent
    public static void onFogSetup(final EntityViewRenderEvent.RenderFogEvent event) {
        if(event.getType() != FogRenderer.FogType.FOG_TERRAIN || recursion)
            return;

//		recursion = true;
//
////		event.getInfo();
//
//		for(int i = 0; i < portals.size(); i++) {
////		for(PortalLocation portal : portals)
//			PortalLocation portal = portals.get(i);
//			PortalableBlockTER.renderPortal(portal.getState(), portal.getTransform(), portal.getPos(), (float) event.getRenderPartialTicks());
//		}
//
//		clearPortals();
//
//		recursion = false;
    }
    
    @SubscribeEvent
    public static void onRenderBlockOverlay(final RenderBlockOverlayEvent event) {
    	if(!PortalEntity.shouldRenderBlockOverlay(event.getPlayer().level, event.getBlockPos()))
    		event.setCanceled(true);
    }
    
    @SubscribeEvent
    public static void onRenderWorldLast(final RenderWorldLastEvent event) {
//		if(!recursion) {
//			recursion = true;

//			PortalModRenderer.render(event.getPartialTicks(), event.getMatrixStack());
//			Portal2.LOGGER.debug("RENDER");

//			for(PortalLocation portal : portals) {
//				if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_B)) {
//					PortalGameRenderer.renderLevel(event.getPartialTicks(), Util.getNanos(), new MatrixStack(), portal.getPos(), portal.getOtherPos());
//					continue;
//				}

//				PortableBlockTER.setupStencilForWorld();

//				Minecraft.getInstance().textureManager.bind(portal.getEnd() == PortalEnd.ORANGE ? STENCIL_BLUE : STENCIL_ORANGE);

//				PortalGameRenderer.renderLevel(event.getPartialTicks(), Util.getNanos(), new MatrixStack(), portal.getPos(), portal.getOtherPos());

//				DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP.setupBufferState(0L);
//				screenBuffer.draw(portal.getTransform(), 7);

//				if(Minecraft.getInstance().player.getMainHandItem().getItem() instanceof PortalGun)
//					PortableBlockTER.drawStencil(portal.getTransform(), portal.getEnd());

//				PortableBlockTER.clearStencilAndUnbind();
//			}

//			clearPortals();

//			RenderSystem.stencilMask(0xFF);
//			RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);

//			for(Block block : blocks) {
//				Portal2.LOGGER.debug("BLOCK RENDER");
//			}
//
//			clearBlocks();

//			recursion = false;
//			canRenderPortal = false;
//		}

//		blitFBOToScreen();

//		PortalableBlockTER.renderAllStencils();
        PortalEntityRenderer.renderHighlights();
    }

    @SubscribeEvent
    public static void onCameraSetup(final EntityViewRenderEvent.CameraSetup event) {
        // todo something
//        if(PortalFirstPersonRenderer.swinging && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof PortalGun && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
//            float actualSwingProgress = PortalFirstPersonRenderer.getAttackAnim((float) event.getRenderPartialTicks());
//            float animation = MathHelper.sin((actualSwingProgress * 2 - 0.5f) * (float) Math.PI) / 2 + 0.5f;
//
//            event.setPitch(event.getPitch() - animation * 3);
//            event.setYaw(event.getYaw() - animation * 3);
//        }

//        BlockState state = event.getInfo().getBlockAtCamera();
//        if(state.getBlock() == BlockInit.PORTALABLE_BLOCK.get()) {
//            Vector3d v;
//            if(state.getValue(PortalableBlock.END) == PortalEnd.BLUE)
//                v = event.getInfo().getPosition().add(new Vector3d(0, 0, 3));
//            else
//                v = event.getInfo().getPosition().add(new Vector3d(0, 0, -3));
//        }
        
        PortalEntity.teleportCamera(event);
    }

//	@SubscribeEvent
//	public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
//		if(event.getHand() != Hand.MAIN_HAND)
//			return;
//
//		Item item = Minecraft.getInstance().player.getMainHandItem().getItem();
//
//		if(item instanceof PortalGun) {
//			PortalGun gun = (PortalGun)item;
//
//			gun.placePortal(PortalEnd.BLUE, event.getWorld(), event.getPlayer());
//
//			event.setCanceled(true);
//		}
//	}

//	@SubscribeEvent
//	public static void onLeftClickEmpty(final PlayerInteractEvent.LeftClickEmpty event) {
//		if(event.getHand() != Hand.MAIN_HAND)
//			return;
//
//		Networking.sendToServer(new PortalShootPacket(PortalEnd.BLUE));
//	}

//	@SubscribeEvent
//	public static void onRightClickEmpty(final PlayerInteractEvent.RightClickEmpty event) {
//		PortalMod.LOGGER.debug("RIGHT CLICK");
//		Vector3d pos = event.getPlayer().position();
//		event.getPlayer().setPos(pos.x, pos.y + 10, pos.z);
//		event.getPlayer().rotate(Rotation.CLOCKWISE_90);
//		event.getPlayer().absMoveTo(pos.x, pos.y + 10, pos.z, event.getPlayer().getRotationVector().y + 180F, event.getPlayer().getRotationVector().x);
//	}

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent event) {
        // todo add to present method
//        if(event.phase == TickEvent.Phase.END)
//            PortalFirstPersonRenderer.updateSwingTime();
    }

    @SubscribeEvent
    public static void onRenderHand(final RenderHandEvent event) {
        // todo get better
//        if(Minecraft.getInstance().player.getMainHandItem().getItem() instanceof PortalGun) {
//            event.setCanceled(true);
//            PortalFirstPersonRenderer.renderArmWithItem(Minecraft.getInstance().player, event.getPartialTicks(), event.getInterpolatedPitch(),
//                    event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress(), event.getMatrixStack(), event.getBuffers(), event.getLight());
//        }
    }

    public static final ResourceLocation INFO_ICON = new ResourceLocation(PortalMod.MODID, "textures/gui/icons/info.png");

    public static final List<String> debugStrings = new ArrayList<>();
    
    @SubscribeEvent
    public static void onRenderOverlay(final RenderGameOverlayEvent.Post event) {
        ItemStack itemStack = Minecraft.getInstance().player.getMainHandItem();

        if(event.getType() == ElementType.CROSSHAIRS) {
            if(itemStack.getItem() instanceof PortalGun && Minecraft.getInstance().options.getCameraType().isFirstPerson()
                    && Minecraft.getInstance().gameMode.getPlayerMode() != GameType.SPECTATOR) {
                int u = 0;
                int v = 0;

                if(!PortalGun.hasPortal(itemStack, PortalEnd.BLUE, true))
                    u = 33;
                if(!PortalGun.hasPortal(itemStack, PortalEnd.ORANGE, true))
                    v = 33;

                // blit(stack, x, y, z, u, v, uWidth, uHeight, texwidth, texheight)
                RenderSystem.disableBlend();
                Minecraft.getInstance().getTextureManager().bind(CROSSHAIRS);
                AbstractGui.blit(event.getMatrixStack(),
                        Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 17,
                        Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 16,
                        0, u, v, 33, 33, 66, 66);
                RenderSystem.enableBlend();
            }
        } else if(event.getType() == ElementType.SUBTITLES) {
            FontRenderer fontRenderer = Minecraft.getInstance().font;
            MainWindow window = event.getWindow();
            float scale = 1.5f;
            
            int index = 0;
            for(String text : debugStrings) {
                RenderSystem.pushMatrix();
                RenderSystem.scalef(scale, scale, 1);
                RenderSystem.translatef(
                        window.getGuiScaledWidth() / (2f * scale) - (fontRenderer.width(text)) / 2f,
                        window.getGuiScaledHeight() / scale - 60 + fontRenderer.lineHeight * index++,
                        0
                );
                
                fontRenderer.draw(event.getMatrixStack(), text, 0, 0, 0xFF0000);
                
                RenderSystem.popMatrix();
            }
            
            debugStrings.clear();
            
//			FontRenderer fontRenderer = Minecraft.getInstance().font;
//			MainWindow window = event.getWindow();
//			String text = "Use the wrench to configure";
//			float scale = 1.5f;
//			int space = 11;
//
//			RenderSystem.pushMatrix();
//			RenderSystem.scalef(scale, scale, 1);
//			RenderSystem.translatef(
//					window.getGuiScaledWidth() / (2f * scale) - (fontRenderer.width(text) + space) / 2f,
//					window.getGuiScaledHeight() / scale - 60,
//					0
//			);

//			{
//				int x1 = -2;
//				int y1 = -2;
//				int x2 = 2 + fontRenderer.width(text) + space;
//				int y2 = 2 + fontRenderer.lineHeight;
//				int color = 0x3F000000;
//
//				if (x1 < x2) {
//					int i = x1;
//					x1 = x2;
//					x2 = i;
//				}
//
//				if (y2 < y1) {
//					int j = y2;
//					y2 = y1;
//					y1 = j;
//				}
//
//				float f3 = (float) (color >> 24 & 255) / 255.0F;
//				float f = (float) (color >> 16 & 255) / 255.0F;
//				float f1 = (float) (color >> 8 & 255) / 255.0F;
//				float f2 = (float) (color & 255) / 255.0F;
//				BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
//				RenderSystem.enableBlend();
//				RenderSystem.disableTexture();
//				RenderSystem.defaultBlendFunc();
//				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
//				bufferbuilder.vertex(event.getMatrixStack().last().pose(), (float) x1, (float) y1, 0.0F).color(f, f1, f2, f3).endVertex();
//				bufferbuilder.vertex(event.getMatrixStack().last().pose(), (float) x2, (float) y1, 0.0F).color(f, f1, f2, f3).endVertex();
//				bufferbuilder.vertex(event.getMatrixStack().last().pose(), (float) x2, (float) y2, 0.0F).color(f, f1, f2, f3).endVertex();
//				bufferbuilder.vertex(event.getMatrixStack().last().pose(), (float) x1, (float) y2, 0.0F).color(f, f1, f2, f3).endVertex();
//				bufferbuilder.end();
//				WorldVertexBufferUploader.end(bufferbuilder);
//				RenderSystem.enableTexture();
//				RenderSystem.disableBlend();
//			}
//
//			fontRenderer.draw(event.getMatrixStack(), text,space,0,0xE0E0E0);
////			fontRenderer.drawShadow(event.getMatrixStack(), text,0,0,0xE0E0E0);
//
//			RenderSystem.disableBlend();
//			Minecraft.getInstance().getTextureManager().bind(INFO_ICON);
//			AbstractGui.blit(event.getMatrixStack(),
//					0,
//					0,
//					0, 0, 0, 9, 9, 9, 9);
//			RenderSystem.enableBlend();
//
//			RenderSystem.popMatrix();
        }
    }
}