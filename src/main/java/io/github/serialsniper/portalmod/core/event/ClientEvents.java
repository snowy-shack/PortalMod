package io.github.serialsniper.portalmod.core.event;

import com.mojang.blaze3d.matrix.*;
import com.mojang.blaze3d.systems.*;
import com.mojang.realmsclient.util.TextRenderingUtils;
import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.client.render.PortalFirstPersonRenderer;
import io.github.serialsniper.portalmod.client.render.PortalShaders;
import io.github.serialsniper.portalmod.client.render.ter.PortalableBlockTER;
import io.github.serialsniper.portalmod.client.util.PortalLocation;
import io.github.serialsniper.portalmod.common.blocks.PortalableBlock;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import io.github.serialsniper.portalmod.core.init.BlockInit;
import io.github.serialsniper.portalmod.core.util.InputUtil;
import io.github.serialsniper.portalmod.core.util.Networking;
import io.github.serialsniper.portalmod.core.util.PortalShootPacket;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.*;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

	private static int count = 0;

	@SubscribeEvent
	public static void onReload(final AddReloadListenerEvent event) {
//		event.addListener((stage, resourceManager, profiler, profiler2, executor, executor2) -> {
//			if(count++ > 0) {
//				try {
//					PortalShaders.reloadAll();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			return new CompletableFuture<Void>() {
//				@Override
//				public boolean complete(Void value) {
//					return true;
//				}
//			};
//		});
	}

	private static boolean wasPressed = false;

	@SubscribeEvent
	public static void onKeyPress(final InputEvent.KeyInputEvent event) {
		long window = Minecraft.getInstance().getWindow().getWindow();
		if(event.getKey() == GLFW.GLFW_KEY_S) {
			if(InputMappings.isKeyDown(window, GLFW.GLFW_KEY_F3) && event.getAction() == GLFW.GLFW_PRESS && !wasPressed) {
				try {
					PortalShaders.reloadAll();
				} catch (IOException e) {
					e.printStackTrace();
				}
				PortalMod.LOGGER.info("All Portal Shaders reloaded");
			}
			wasPressed = event.getAction() != 0;
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
		// todo use with portals
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

		PortalableBlockTER.renderAllStencils();
	}

	@SubscribeEvent
	public static void onCameraSetup(final EntityViewRenderEvent.CameraSetup event) {
		if(PortalFirstPersonRenderer.swinging && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof PortalGun && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
			float actualSwingProgress = PortalFirstPersonRenderer.getAttackAnim((float) event.getRenderPartialTicks());
			float animation = MathHelper.sin((actualSwingProgress * 2 - 0.5f) * (float) Math.PI) / 2 + 0.5f;

			event.setPitch(event.getPitch() - animation * 3);
			event.setYaw(event.getYaw() - animation * 3);
		}

//		if(InputUtil.isKeyDown(GLFW.GLFW_KEY_ENTER)) {
//			Entity cameraEntity = event.getInfo().getEntity();
//			World level = cameraEntity.level;
//			BlockState state = level.getBlockState(new BlockPos(event.getInfo().getPosition()));
//			BlockState state = level.getBlockState(event.getInfo().getBlockPosition());
			BlockState state = event.getInfo().getBlockAtCamera();
			if(state.getBlock() == BlockInit.PORTALABLE_BLOCK.get()) {
				Vector3d v;
				if(state.getValue(PortalableBlock.END) == PortalEnd.BLUE)
					v = event.getInfo().getPosition().add(new Vector3d(0, 0, 3));
				else
					v = event.getInfo().getPosition().add(new Vector3d(0, 0, -3));
				System.out.println(event.getInfo().getPosition());
				System.out.println(v);
//				event.getInfo().setPosition(v);
				System.out.println(event.getInfo().getPosition());
			}
//		}
	}
	
	@SubscribeEvent
	public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
		if(event.getHand() != Hand.MAIN_HAND)
			return;
		
		Item item = Minecraft.getInstance().player.getMainHandItem().getItem();
		
		if(item instanceof PortalGun) {
			PortalGun gun = (PortalGun)item;
			
			gun.placePortal(PortalEnd.BLUE, event.getWorld(), event.getPlayer());
			
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onLeftClickEmpty(final PlayerInteractEvent.LeftClickEmpty event) {
		if(event.getHand() != Hand.MAIN_HAND)
			return;
		
		Networking.sendToServer(new PortalShootPacket(PortalEnd.BLUE));
	}
	
	@SubscribeEvent
	public static void onRightClickEmpty(final PlayerInteractEvent.RightClickEmpty event) {
		PortalMod.LOGGER.debug("RIGHT CLICK");
//		Vector3d pos = event.getPlayer().position();
//		event.getPlayer().setPos(pos.x, pos.y + 10, pos.z);
//		event.getPlayer().rotate(Rotation.CLOCKWISE_90);
//		event.getPlayer().absMoveTo(pos.x, pos.y + 10, pos.z, event.getPlayer().getRotationVector().y + 180F, event.getPlayer().getRotationVector().x);
	}

	@SubscribeEvent
	public static void onTick(final TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.END)
			PortalFirstPersonRenderer.updateSwingTime();
	}

	@SubscribeEvent
	public static void onRenderHand(final RenderHandEvent event) {
		if(Minecraft.getInstance().player.getMainHandItem().getItem() instanceof PortalGun) {
			event.setCanceled(true);
			PortalFirstPersonRenderer.renderArmWithItem(Minecraft.getInstance().player, event.getPartialTicks(), event.getInterpolatedPitch(),
					event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress(), event.getMatrixStack(), event.getBuffers(), event.getLight());
		}
	}

	public static final ResourceLocation INFO_ICON = new ResourceLocation(PortalMod.MODID, "textures/gui/icons/info.png");

	@SubscribeEvent
	public static void onRenderOverlay(final RenderGameOverlayEvent.Post event) {
		Item item = Minecraft.getInstance().player.getMainHandItem().getItem();
		
		if(event.getType() == ElementType.CROSSHAIRS) {
			if(Minecraft.getInstance().options.getCameraType().isFirstPerson() && item instanceof PortalGun) {
				int u = 0;
				int v = 0;

//				if(PortalHelper.get(PortalGun.getUUID(Minecraft.getInstance().player.getMainHandItem())).hasOrange())
//					u = 33;
//				if(PortalHelper.get(PortalGun.getUUID(Minecraft.getInstance().player.getMainHandItem())).hasBlue())
//					v = 33;

				if(!PortalGun.hasPortal(Minecraft.getInstance().player.getMainHandItem(), PortalEnd.ORANGE))
					u = 33;
				if(!PortalGun.hasPortal(Minecraft.getInstance().player.getMainHandItem(), PortalEnd.BLUE))
					v = 33;
				
				RenderSystem.disableBlend();
				Minecraft.getInstance().getTextureManager().bind(CROSSHAIRS);
				AbstractGui.blit(event.getMatrixStack(),
						Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 17, Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 16, 0,
						u, v, 33, 33, 66, 66);
				RenderSystem.enableBlend();
	
				// blit(stack, x, y, z, u, v, uWidth, uHeight, texwidth, texheight)
			}
		} else if(event.getType() == ElementType.SUBTITLES) {
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
//
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