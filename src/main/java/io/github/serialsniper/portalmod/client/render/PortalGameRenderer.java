package io.github.serialsniper.portalmod.client.render;

import com.mojang.blaze3d.matrix.*;

import io.github.serialsniper.portalmod.common.blocks.PortalableBlock;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.*;

public class PortalGameRenderer {
	private static ZombieEntity cameraEntity;
	private static boolean count = false;
	private static PortalActiveRenderInfo camera = new PortalActiveRenderInfo();

	private static void setCameraEntity(BlockPos pos, float partialTicks) {
		pos = new BlockPos(-141, 72, -15);

//		int yRot = (int)Minecraft.getInstance().level.getBlockState(otherPortalPos).getValue(PortableBlock.FACING).toYRot() + 180
//				- (int)Minecraft.getInstance().level.getBlockState(portalPos).getValue(PortableBlock.FACING).toYRot();

		if(!count) {
			cameraEntity = new ZombieEntity(EntityType.ZOMBIE, Minecraft.getInstance().level);
			cameraEntity.absMoveTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);

			count = true;
		}
	}

	private static void resetCameraEntity() {
		Minecraft.getInstance().setCameraEntity(Minecraft.getInstance().player);
	}

	private static void setupCamera(BlockPos pos, BlockPos otherPos, float partialTicks) {
		if(!(Minecraft.getInstance().level.getBlockState(pos).getBlock() instanceof PortalableBlock) || !(Minecraft.getInstance().level.getBlockState(otherPos).getBlock() instanceof PortalableBlock))
			return;

		Direction side = Minecraft.getInstance().level.getBlockState(pos).getValue(PortalableBlock.FACING);
		Direction otherSide = Minecraft.getInstance().level.getBlockState(otherPos).getValue(PortalableBlock.FACING);

		float yRot = otherSide.toYRot() - side.toYRot();

		yRot %= 360;
		if(yRot < 0)
			yRot += 360;

		int xVector = yRot > 90 ? 1 : -1;
		int zVector = (yRot + 90) % 360 > 90 ? 1 : -1;

//		double x = (double)(pos.getX() * xVector + otherPos.getX()) * -xVector;
//		double z = (double)(pos.getZ() * yVector + otherPos.getZ()) * -yVector;
		double x = otherPos.getY() - pos.getY();
		double z = otherPos.getZ() - pos.getZ();
		double y = otherPos.getY() - pos.getY();

//		Portal2.LOGGER.debug("x: " + x + ", y: " + y + ", z: " + z);

		camera.setup(Minecraft.getInstance().level, new Vector3d(x, y, z), xVector, zVector, pos, otherPos, yRot, 0, partialTicks);
	}

	public static void renderLevel(float partialTicks, long nanos, MatrixStack stack, BlockPos pos, BlockPos otherPos) {
		Minecraft.getInstance().gameRenderer.lightTexture.updateLightTexture(partialTicks);

		Minecraft.getInstance().gameRenderer.pick(partialTicks);
		ActiveRenderInfo activerenderinfo = Minecraft.getInstance().gameRenderer.getMainCamera();
		MatrixStack matrixstack = new MatrixStack();
		matrixstack.last().pose().multiply(Minecraft.getInstance().gameRenderer.getProjectionMatrix(activerenderinfo, partialTicks, true));

		Minecraft.getInstance().gameRenderer.bobHurt(matrixstack, partialTicks);
		if(Minecraft.getInstance().options.bobView) {
			Minecraft.getInstance().gameRenderer.bobView(matrixstack, partialTicks);
		}

		float f = MathHelper.lerp(partialTicks, Minecraft.getInstance().player.oPortalTime, Minecraft.getInstance().player.portalTime)
				* Minecraft.getInstance().options.screenEffectScale * Minecraft.getInstance().options.screenEffectScale;
		if(f > 0.0F) {
			int i = 20;
			float f1 = 5.0F / (f * f + 5.0F) - f * 0.04F;
			f1 = f1 * f1;
			Vector3f vector3f = new Vector3f(0.0F, MathHelper.SQRT_OF_TWO / 2.0F, MathHelper.SQRT_OF_TWO / 2.0F);
			matrixstack.mulPose(vector3f.rotationDegrees(((float)0 + partialTicks) * (float)i));
			matrixstack.scale(1.0F / f1, 1.0F, 1.0F);
			float f2 = -((float)0 + partialTicks) * (float)i;
			matrixstack.mulPose(vector3f.rotationDegrees(f2));
		}

		Matrix4f matrix4f = matrixstack.last().pose();
		Minecraft.getInstance().gameRenderer.resetProjectionMatrix(matrix4f);

		setupCamera(pos, otherPos, partialTicks);
		
//		net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(Minecraft.getInstance().gameRenderer, activerenderinfo, partialTicks);
//		activerenderinfo.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
//		stack.mulPose(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));

//		activerenderinfo.setAnglesInternal(activerenderinfo.getYRot(), activerenderinfo.getXRot());

//		Portal2.LOGGER.debug("ROTATION: [x = " + activerenderinfo.getXRot() + ", y = " + activerenderinfo.getYRot() + "]");

//		stack.translate(1, 0, 0);

//		stack.pushPose();
		stack.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
		stack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot()));
		NewWorldRenderer.renderLevel(stack, partialTicks, nanos, false, camera,
				Minecraft.getInstance().gameRenderer, Minecraft.getInstance().gameRenderer.lightTexture, matrix4f);
//		stack.popPose();

//		net.minecraftforge.client.ForgeHooksClient.dispatchRenderLast(Minecraft.getInstance().levelRenderer, stack, partialTicks, matrix4f, nanos);

//		camera.setup(
//				Minecraft.getInstance().level,
//				Minecraft.getInstance().getCameraEntity() == null ? Minecraft.getInstance().player : Minecraft.getInstance().getCameraEntity(),
//				!Minecraft.getInstance().options.getCameraType().isFirstPerson(),
//				Minecraft.getInstance().options.getCameraType().isMirrored(),
//				partialTicks
//		);

//		resetCameraEntity();
	}
}