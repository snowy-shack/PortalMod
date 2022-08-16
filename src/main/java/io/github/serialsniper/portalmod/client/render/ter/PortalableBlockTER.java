package io.github.serialsniper.portalmod.client.render.ter;

import com.mojang.blaze3d.matrix.*;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.core.event.ClientEvents;
import io.github.serialsniper.portalmod.client.render.CubeCoordinates;
import io.github.serialsniper.portalmod.client.render.PortalGameRenderer;
import io.github.serialsniper.portalmod.client.render.model.PortalableBakedModel;
import io.github.serialsniper.portalmod.common.blocks.PortalableBlock;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.common.blockentities.PortalableBlockTileEntity;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.state.properties.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.api.distmarker.*;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

@OnlyIn(Dist.CLIENT)
public class PortalableBlockTER extends TileEntityRenderer<PortalableBlockTileEntity> {
	public static final ResourceLocation STENCIL_BLUE = new ResourceLocation(PortalMod.MODID, "textures/portals/highlight/highlight_blue.png");
	public static final ResourceLocation STENCIL_ORANGE = new ResourceLocation(PortalMod.MODID, "textures/portals/highlight/highlight_orange.png");
	public static VertexBuffer screenBuffer;
	private static int recursion = 0;
	public static Vector3f clearColor = new Vector3f();

	public static final FloatBuffer projection = GLAllocation.createFloatBuffer(16);
	public static final FloatBuffer modelview = GLAllocation.createFloatBuffer(16);

	private static class Stencil {
		BlockState state;
		Matrix4f matrix;
	}

	private static List<Stencil> stencils = new ArrayList<>();
	
	public PortalableBlockTER(TileEntityRendererDispatcher terd) {
		super(terd);
	}

	private static void addStencil(BlockState state, Matrix4f matrix) {
		Stencil stencil = new Stencil();
		stencil.state = state;
		stencil.matrix = matrix;
		stencils.add(stencil);
	}

	public static void renderAllStencils() {
		for(Stencil stencil : stencils)
			renderStencil(stencil.state, stencil.matrix);

		stencils.clear();
		clearStencil();
	}

	private static void renderStencil(BlockState state, Matrix4f matrix) {
		if(recursion > 0)
			return;

//		clearStencil();

		int oldShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		PortalMod.shaders.bind();

		int model = glGetUniformLocation(PortalMod.shaders.getId(), "ModelMat");
		int projection = glGetUniformLocation(PortalMod.shaders.getId(), "ProjMat");
		int color = glGetUniformLocation(PortalMod.shaders.getId(), "color");

		int texture = glGetUniformLocation(PortalMod.shaders.getId(), "texture");
		int phase = glGetUniformLocation(PortalMod.shaders.getId(), "phase");

		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.multMatrix(matrix);

			float[] modelMat = new float[16];
			GL11.glGetFloatv(GL_MODELVIEW_MATRIX, modelMat);

			float[] projMat = new float[16];
			GL11.glGetFloatv(GL_PROJECTION_MATRIX, projMat);

			glUniformMatrix4fv(model, false, modelMat);
			glUniformMatrix4fv(projection, false, projMat);



			buildRect(state.getValue(PortalableBlock.FACING), CubeCoordinates.Facing.OUTSIDE, 2);
			DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

			GL11.glEnable(GL11.GL_STENCIL_TEST);
			RenderSystem.disableCull();

				RenderSystem.stencilMask(0xFF);
				RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
				RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

					RenderSystem.colorMask(false, false, false, false);
					RenderSystem.depthMask(false);

						glUniform1i(phase, 3);
						RenderSystem.drawArrays(7, 0, 4);

					RenderSystem.depthMask(true);
					RenderSystem.colorMask(true, true, true, true);

			RenderSystem.disableDepthTest();

				RenderSystem.stencilMask(0x00);
				RenderSystem.stencilFunc(GL_NOTEQUAL, 1, 0xFF);

//					RenderSystem.enableAlphaTest();
					RenderSystem.enableBlend();

						RenderSystem.activeTexture(GL_TEXTURE0);
						Minecraft.getInstance().textureManager.bind(
								state.getValue(PortalableBlock.END) == PortalEnd.BLUE ? PortalableBakedModel.HIGHLIGHT_BLUE : PortalableBakedModel.HIGHLIGHT_ORANGE);
						glUniform1i(texture, 0);

						glUniform1i(phase, 4);
						RenderSystem.drawArrays(7, 0, 4);

						RenderSystem.bindTexture(0);

//					RenderSystem.disableAlphaTest();
					RenderSystem.disableBlend();

			RenderSystem.enableDepthTest();
			RenderSystem.enableCull();
			GL11.glDisable(GL11.GL_STENCIL_TEST);

			unbindBuffer();

		RenderSystem.popMatrix();

		GL20.glUseProgram(oldShader);
	}

	public static void renderPortal(BlockState state, Matrix4f matrix, BlockPos pos, float partialTicks) {
		ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();

		MatrixStack stackNew = new MatrixStack();
//		stack.mulPose(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));
		stackNew.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
		stackNew.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
		stackNew.translate(pos.getX(), pos.getY(), pos.getZ());
		stackNew.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
		Matrix4f matrixNew = stackNew.last().pose();

		if(state.getValue(PortalableBlock.ACTIVE) && recursion <= 0) {
//			System.out.println("yeah");
			recursion++;

//			ClientEvents.addBlock(tileEntity.getLevel(), state, tileEntity.getBlockPos(), matrixStack, renderBuffer, combinedLight);

			if(state.getValue(PortalableBlock.HALF) == DoubleBlockHalf.LOWER
					&& Minecraft.getInstance().player.getMainHandItem().getItem() instanceof PortalGun) {
//				System.out.println("ofc");
//				ClientEvents.canRenderPortal = true;

				Minecraft.getInstance().getMainRenderTarget().enableStencil();

//				screenBuffer.draw(matrixStack.last().pose(), 7);

				int oldShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
				PortalMod.shaders.bind();

				int model = glGetUniformLocation(PortalMod.shaders.getId(), "ModelMat");
				int projection = glGetUniformLocation(PortalMod.shaders.getId(), "ProjMat");
				int texture = glGetUniformLocation(PortalMod.shaders.getId(), "texture");
				int color = glGetUniformLocation(PortalMod.shaders.getId(), "color");
				int phase = glGetUniformLocation(PortalMod.shaders.getId(), "phase");

				float[] modelMat, projMat;

//				FogRenderer.setupColor();
//				float[] clearColor = new float[4];
//				glGetFloatv(GL_COLOR_CLEAR_VALUE, clearColor);

//				Portal2.LOGGER.debug(clearColor[0] + " " + clearColor[1] + " " + clearColor[2] + " " + clearColor[3]);
//
//				Framebuffer fbo = Minecraft.getInstance().getMainRenderTarget();
//				Portal2.LOGGER.debug(fbo.clearChannels[0] + " " + fbo.clearChannels[1] + " " + fbo.clearChannels[2] + " " + fbo.clearChannels[3]);




//				GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, projection);
//				GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
//				Matrix4f projectionMatrix = (Matrix4f) new Matrix4f().load(projection.asReadOnlyBuffer());
//				Matrix4f modelViewMatrix = (Matrix4f) new Matrix4f().load(modelview.asReadOnlyBuffer());
//				Matrix4f result = Matrix4f.mul(modelViewMatrix, projectionMatrix, null);

				GL11.glEnable(GL11.GL_STENCIL_TEST);
				RenderSystem.stencilMask(0xFF);
				RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);

				RenderSystem.enableDepthTest();
				RenderSystem.enableCull();

				RenderSystem.pushMatrix();
				RenderSystem.loadIdentity();
				RenderSystem.multMatrix(matrixNew);

					PortalMod.shaders.bind();

					modelMat = new float[16];
					GL11.glGetFloatv(GL_MODELVIEW_MATRIX, modelMat);

					projMat = new float[16];
					GL11.glGetFloatv(GL_PROJECTION_MATRIX, projMat);


					glUniformMatrix4fv(model, false, modelMat);
					glUniformMatrix4fv(projection, false, projMat);

					GL11.glEnable(GL32.GL_DEPTH_CLAMP);

					buildRect(state.getValue(PortalableBlock.FACING), CubeCoordinates.Facing.OUTSIDE, 2);
					DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

					glUniform4f(color, clearColor.x(), clearColor.y(), clearColor.z(), 0);
	//				glUniform4f(color, 0, 0, 0, 0);

					RenderSystem.stencilMask(0xFF);
					RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
					RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

						RenderSystem.activeTexture(GL_TEXTURE0);
						Minecraft.getInstance().textureManager.bind(PortalableBakedModel.PORTAL_MASK);
						glUniform1i(texture, 0);

						glUniform1i(phase, 0);
						RenderSystem.drawArrays(7, 0, 4);

						RenderSystem.bindTexture(0);

					RenderSystem.stencilMask(0x00);
					RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
					RenderSystem.depthFunc(GL_ALWAYS);

					glUniform1i(phase, 1);
					RenderSystem.drawArrays(7, 0, 4);

					RenderSystem.depthFunc(GL_LESS);

					unbindBuffer();

					GL11.glDisable(GL32.GL_DEPTH_CLAMP);

					GL20.glUseProgram(oldShader);

						addStencil(state, matrixNew);

					glEnable(GL_CLIP_PLANE0);

					Vector3i normal = state.getValue(PortalableBlock.FACING).getNormal();
					glTranslatef(
							0.5f + (float)normal.getX() / 2,
							0.5f + (float)normal.getY() / 2,
							0.5f + (float)normal.getZ() / 2
					);
					glClipPlane(GL_CLIP_PLANE0, new double[] {
							-normal.getX(),
							-normal.getY(),
							-normal.getZ(),
							0
					});

				RenderSystem.popMatrix();

				RenderSystem.stencilMask(0x00);
				RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
				PortalGameRenderer.renderLevel(partialTicks, Util.getNanos(), new MatrixStack(), pos,
						PortalGun.getPortalPosition(Minecraft.getInstance().player.getMainHandItem(), state.getValue(PortalableBlock.END).other(), true));

				glDisable(GL_CLIP_PLANE0);







				RenderSystem.pushMatrix();
				RenderSystem.loadIdentity();
				RenderSystem.multMatrix(matrixNew);

				PortalMod.shaders.bind();

				modelMat = new float[16];
				GL11.glGetFloatv(GL_MODELVIEW_MATRIX, modelMat);

				projMat = new float[16];
				GL11.glGetFloatv(GL_PROJECTION_MATRIX, projMat);

				glUniformMatrix4fv(model, false, modelMat);
				glUniformMatrix4fv(projection, false, projMat);

				GL11.glEnable(GL32.GL_DEPTH_CLAMP);

				buildRect(state.getValue(PortalableBlock.FACING), CubeCoordinates.Facing.OUTSIDE, 2);
				DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

				glUniform4f(color, clearColor.x(), clearColor.y(), clearColor.z(), 0);
//				glUniform4f(color, 0, 0, 0, 0);

				RenderSystem.stencilMask(0x00);
				RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);

				RenderSystem.colorMask(false, false, false, false);

					glUniform1i(phase, 2);
					RenderSystem.drawArrays(7, 0, 4);

//					RenderSystem.disableCull();
//
//						RenderSystem.stencilMask(0xFF);
//						RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
//						RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
//
//						RenderSystem.depthMask(false);
//
//							RenderSystem.drawArrays(7, 0, 4);
//
//						RenderSystem.depthMask(true);
//
//					RenderSystem.enableCull();

				RenderSystem.colorMask(true, true, true, true);

				unbindBuffer();

				GL11.glDisable(GL32.GL_DEPTH_CLAMP);

				GL20.glUseProgram(oldShader);

				RenderSystem.popMatrix();



//				RenderSystem.stencilMask(0xFF);
//				RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
//				RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_ZERO);
				GL11.glDisable(GL11.GL_STENCIL_TEST);

//				ClientEvents.addPortal(matrixStack.last().pose(), state.getValue(PortableBlock.FACING),
//						state.getValue(PortableBlock.END), tileEntity.getBlockPos(),
//						PortalGun.getPortalPosition(Minecraft.getInstance().player.getMainHandItem(), state.getValue(PortableBlock.END).other()));
			}

//			clearStencil();

			recursion--;
		}
	}

	@Override
	public void render(PortalableBlockTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int combinedLight, int combinedOverlay) {
		if(!tileEntity.getBlockState().getValue(PortalableBlock.ACTIVE))
			return;

//		PortalHelper.getHierarchy().forEach((x, y) -> {
//			System.out.println(y.getBlue());
//			System.out.println(y.getOrange());
//		});

		ClientEvents.addPortal(matrixStack.last().pose(), tileEntity.getBlockState().getValue(PortalableBlock.FACING),
				tileEntity.getBlockState().getValue(PortalableBlock.END), tileEntity.getBlockPos(),
				PortalGun.getPortalPosition(Minecraft.getInstance().player.getMainHandItem(), tileEntity.getBlockState().getValue(PortalableBlock.END).other(), true), tileEntity, matrixStack, tileEntity.getBlockState());

//		renderPortal(tileEntity.getBlockState(), matrixStack.last().pose(), tileEntity.getBlockPos(), partialTicks);
	}

	public static void setupStencilForWorld() {
		RenderSystem.stencilMask(0x00);
		RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}

	public static void buildPortalStencilMask(Matrix4f matrix, Direction side) {
		drawScreen(matrix, Direction.UP, CubeCoordinates.Facing.INSIDE, 2);
		drawScreen(matrix, Direction.DOWN, CubeCoordinates.Facing.INSIDE, 1);

		for(int i = 0; i < 4; i++) {
			side = side.getClockWise();
			drawScreen(matrix, side, i == 3 ? CubeCoordinates.Facing.OUTSIDE : CubeCoordinates.Facing.INSIDE, 2);
		}
	}

	public static void buildRect(Direction side, CubeCoordinates.Facing facing, float height) {
		float r, g, b, a;
		r = g = b = a = 1;

		Vector3f[] coords = CubeCoordinates.getQuad(side, facing, height);

		if(screenBuffer != null)
			screenBuffer.close();

		screenBuffer = new VertexBuffer(DefaultVertexFormats.POSITION_TEX);

		BufferBuilder builder = Tessellator.getInstance().getBuilder();
		builder.begin(7, DefaultVertexFormats.POSITION_TEX);

		builder.vertex(coords[0].x(), coords[0].y(), coords[0].z()).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).normal(1, 1, 1).endVertex();
		builder.vertex(coords[1].x(), coords[1].y(), coords[1].z()).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).normal(1, 1, 1).endVertex();
		builder.vertex(coords[2].x(), coords[2].y(), coords[2].z()).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).normal(1, 1, 1).endVertex();
		builder.vertex(coords[3].x(), coords[3].y(), coords[3].z()).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).normal(1, 1, 1).endVertex();

		builder.end();

		screenBuffer.upload(builder);
		screenBuffer.bind();
	}

	public static void setupStencil() {
		GL11.glEnable(GL11.GL_STENCIL_TEST);

		RenderSystem.stencilMask(0xFF);
		RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);

		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
	}

	public static void clearStencil() {
		GL11.glEnable(GL11.GL_STENCIL_TEST);

		RenderSystem.stencilMask(0xFF);
		RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);

		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}

	public static void unbindBuffer() {
		DefaultVertexFormats.POSITION_TEX.clearBufferState();
		VertexBuffer.unbind();
	}

	public static void drawScreen(Matrix4f matrix, Direction side, CubeCoordinates.Facing facing, float height) {
		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();

		RenderSystem.stencilMask(0xFF);
		RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);

		buildRect(side, facing, height);
		RenderSystem.bindTexture(0);

		DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
		screenBuffer.draw(matrix, 7);

		unbindBuffer();
	}

	public static void drawStencil(Matrix4f matrix, PortalEnd end) {
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();

		RenderSystem.stencilMask(0x00);
		RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);

		Minecraft.getInstance().textureManager.bind(end == PortalEnd.BLUE ? STENCIL_BLUE : STENCIL_ORANGE);

		DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
		screenBuffer.draw(matrix, 7);

		RenderSystem.enableDepthTest();
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}
}