package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.portalmod.PMState;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.RenderUtil;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;
import net.portalmod.mixins.accessors.MinecraftAccessor;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class DuplicateEntityRenderer {
    public static Vec3 entityPosOverride = null;
    public static Matrix4f entityShadowTransformOverride = null;
    public static boolean shouldRenderShadow = true;

    public static void renderDuplicateEntities(ClippingHelper clippinghelper, double d0, double d1, double d2, float partialTicks, MatrixStack matrixStack, ActiveRenderInfo camera) {
        Minecraft mc = Minecraft.getInstance();
        WorldRenderer lr = Minecraft.getInstance().levelRenderer;

        if(mc.player == null)
            return;

        Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource().endBatch();

        for(Entity entity : lr.level.entitiesForRendering()) {
            boolean shouldRender = !(entity instanceof ClientPlayerEntity)
                    || camera.getEntity() == entity
                    || (entity == mc.player && !mc.player.isSpectator());

            if(!PortalModConfigManager.RENDER_SELF.get()) {
                shouldRender &= entity != camera.getEntity()
                        || camera.isDetached()
                        || camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping();
            }

            if(shouldRender) {
                lr.renderedEntities++;

                if (entity.tickCount == 0) {
                    entity.xOld = entity.getX();
                    entity.yOld = entity.getY();
                    entity.zOld = entity.getZ();
                }

                renderDuplicateEntity(entity, d0, d1, d2, partialTicks, matrixStack, clippinghelper);
            }
        }
    }

    public static boolean shouldRenderSelf(Entity entity, ClippingHelper clippingHelper) {
        return shouldRender(entity, Mat4.identity(), clippingHelper);
    }

    private static boolean shouldRenderEntity(Entity entity, ClippingHelper clippingHelper, Vec3 camPos, Mat4 matrix) {
        if(!entity.shouldRender(camPos.x, camPos.y, camPos.z))
            return false;

        if(entity.noCulling)
            return true;

        return shouldRender(entity, matrix, clippingHelper);
    }

    private static boolean shouldRender(Entity entity, Mat4 matrix, ClippingHelper clippingHelper) {
        ActiveRenderInfo currentCamera = PortalRenderer.getInstance().getCurrentCamera();
        Vec3 cameraPos = PMState.cameraPosOverrideForRenderingSelf != null
                ? PMState.cameraPosOverrideForRenderingSelf
                : new Vec3(currentCamera.getPosition());

        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfoAccessor mainCameraAccessor = (ActiveRenderInfoAccessor)mc.gameRenderer.getMainCamera();
        float partialTicks = mc.isPaused() ? ((MinecraftAccessor)mc).pmGetPausePartialTick() : mc.getFrameTime();
        float eyeHeight = MathHelper.lerp(partialTicks, mainCameraAccessor.pmGetEyeHeightOld(), mainCameraAccessor.pmGetEyeHeight());

        Vec3 partialTickedOffset = new Vec3(entity.getPosition(partialTicks)).sub(entity.position());
        AxisAlignedBB aabb = entity.getBoundingBox().move(partialTickedOffset.to3d());
        aabb = transformAABB(aabb, matrix);
        AxisAlignedBB aabbCull = entity.getBoundingBoxForCulling().move(partialTickedOffset.to3d()).inflate(0.5);
        aabbCull = transformAABB(aabbCull, matrix);

        if(entity != mc.cameraEntity)
            return clippingHelper.isVisible(aabbCull);

        if(aabb.contains(cameraPos.to3d()) ^ aabb.contains(cameraPos.clone().sub(0, eyeHeight, 0).to3d()))
            return false;

        Vec3 duplicateEntityPos = new Vec3(entity.getPosition(partialTicks)).add(0, eyeHeight, 0).transform(matrix);
        float distance = (float)cameraPos.clone().sub(duplicateEntityPos).magnitudeSqr();
        return distance > 0.001 && clippingHelper.isVisible(aabbCull);
    }

    private static void renderDuplicateEntity(Entity entity, double camX, double camY, double camZ, float partialTicks, MatrixStack matrixStack, ClippingHelper clippingHelper) {
        double d0 = MathHelper.lerp(partialTicks, entity.xOld, entity.getX());
        double d1 = MathHelper.lerp(partialTicks, entity.yOld, entity.getY());
        double d2 = MathHelper.lerp(partialTicks, entity.zOld, entity.getZ());
        float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);

        if(entity instanceof PortalEntity || Minecraft.getInstance().player == null)
            return;

        List<PortalEntity> entities = PortalEntity.getOpenPortals(entity.level,
                entity.getBoundingBox().inflate(.2), portal -> true);

        for(PortalEntity portal : entities) {
            if(!portal.isOpen() || !portal.getOtherPortal().isPresent() || !portal.isEntityAlignedToPortal(entity))
                continue;

            PortalEntity otherPortal = portal.getOtherPortal().get();
            ActiveRenderInfo camera = PortalRenderer.getInstance().getCurrentCamera();
            EntityRendererManager erm = Minecraft.getInstance().levelRenderer.entityRenderDispatcher;

            Mat4 changeOfBasisMatrix = PortalEntity.getPortalToPortalRotationMatrix(portal, otherPortal);
            Mat4 portalToPortalMatrix = PortalEntity.getPortalToPortalMatrix(portal, otherPortal);

            Vec3 cameraPos = new Vec3(camera.getPosition()).transform(portalToPortalMatrix);
            boolean shouldRender = shouldRenderEntity(entity, clippingHelper, cameraPos, portalToPortalMatrix)
                    || entity.hasIndirectPassenger(Minecraft.getInstance().player);

            if(!shouldRender)
                continue;

            matrixStack.pushPose();
            matrixStack.translate(-camX, -camY, -camZ);
            matrixStack.last().pose().multiply(portalToPortalMatrix.to4f());

            matrixStack.pushPose();
            matrixStack.translate(d0, d1, d2);
            matrixStack.last().pose().multiply(changeOfBasisMatrix.transpose().to4f());
            entityPosOverride = new Vec3(entity.xOld, entity.yOld, entity.zOld)
                    .lerp(entity.position(), partialTicks)
                    .transform(portalToPortalMatrix);
            entityShadowTransformOverride = matrixStack.last().pose();
            shouldRenderShadow = portal.getDirection().getAxis().isHorizontal() && otherPortal.getDirection().getAxis().isHorizontal();
            matrixStack.popPose();

            RenderUtil.setupClipPlane(new MatrixStack(), otherPortal, camera, 0, true);

            erm.render(entity, d0, d1, d2, f,
                    partialTicks, matrixStack, Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource(),
                    erm.getPackedLightCoords(entity, partialTicks));

            Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource().endBatch();

            if(PortalRenderer.getInstance().recursion >= 1) {
                RenderUtil.setStandardClipPlane(PortalRenderer.getInstance().clipMatrix.last().pose());
            } else {
                glDisable(GL_CLIP_PLANE0);
            }

            matrixStack.popPose();

            entityPosOverride = null;
            entityShadowTransformOverride = null;
            shouldRenderShadow = true;
        }
    }

    private static AxisAlignedBB transformAABB(AxisAlignedBB aabb, Mat4 matrix) {
        Vec3 min = new Vec3(aabb.minX, aabb.minY, aabb.minZ).transform(matrix);
        Vec3 max = new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ).transform(matrix);
        return new AxisAlignedBB(min.to3d(), max.to3d());
    }
}