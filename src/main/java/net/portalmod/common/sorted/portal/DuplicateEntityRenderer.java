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
import net.portalmod.PMGlobals;
import net.portalmod.client.screens.PortalModOptionsScreen;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.RenderUtil;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;

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

            if(!PortalModOptionsScreen.RENDER_SELF.get()) {
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

    private static boolean shouldRender(Entity entity, ClippingHelper clippingHelper, Vec3 camPos, Mat4 matrix, float partialTicks) {
        if(!entity.shouldRender(camPos.x, camPos.y, camPos.z))
            return false;

        if(entity.noCulling)
            return true;

        Vec3 realEntityPos = DuplicateEntityRenderer.getEntityEyePositionAssumingSelf(entity, PMGlobals.partialTicks);
        Vec3 duplicateEntityPos = DuplicateEntityRenderer.getEntityEyePositionAssumingSelf(entity, PMGlobals.partialTicks, matrix);

        double d = duplicateEntityPos.clone().sub(PortalRenderer.getInstance().getCurrentCamera().getPosition()).magnitudeSqr();
        if(d < 0.001)
            return false;

        Vec3 offset = duplicateEntityPos.clone().sub(realEntityPos);
        AxisAlignedBB aabb = entity.getBoundingBoxForCulling().move(offset.to3d()).inflate(0.5);
        if(aabb.hasNaN() || aabb.getSize() == 0) {
            aabb = new AxisAlignedBB(duplicateEntityPos.to3d(), duplicateEntityPos.to3d()).inflate(2);
        }

        return clippingHelper.isVisible(aabb);
    }

    private static void renderDuplicateEntity(Entity entity, double camX, double camY, double camZ, float partialTicks, MatrixStack matrixStack, ClippingHelper clippingHelper) {
        double d0 = MathHelper.lerp(partialTicks, entity.xOld, entity.getX());
        double d1 = MathHelper.lerp(partialTicks, entity.yOld, entity.getY());
        double d2 = MathHelper.lerp(partialTicks, entity.zOld, entity.getZ());
        float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);

        if(entity instanceof PortalEntity || Minecraft.getInstance().player == null)
            return;

        List<PortalEntity> entities = PortalEntity.getOpenPortals(entity.level, entity.getBoundingBox().inflate(.2), portal -> true);
        for(PortalEntity portal : entities) {
            if(!portal.isOpen() || !portal.getOtherPortal().isPresent() || !portal.isEntityAlignedToPortal(entity))
                continue;

            PortalEntity otherPortal = portal.getOtherPortal().get();
            ActiveRenderInfo camera = PortalRenderer.getInstance().getCurrentCamera();
            EntityRendererManager erm = Minecraft.getInstance().levelRenderer.entityRenderDispatcher;

            Mat4 changeOfBasisMatrix = PortalRenderer.getPortalToPortalRotationMatrix(portal, otherPortal);
            Mat4 portalToPortalMatrix = PortalRenderer.getPortalToPortalMatrix(portal, otherPortal);

            boolean shouldRender = shouldRender(entity, clippingHelper,
                    new Vec3(camera.getPosition()).transform(portalToPortalMatrix),
                    portalToPortalMatrix, partialTicks)
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

            RenderUtil.setupClipPlane(new MatrixStack(), otherPortal, camera, portal.getWallAttachmentDistance(camera), true);

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

    public static Vec3 getEntityEyePositionAssumingSelf(Entity entity, float partialTicks, Mat4 matrix) {
        ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        ActiveRenderInfoAccessor cameraAccessor = (ActiveRenderInfoAccessor)camera;
        double eyeHeight = MathHelper.lerp(partialTicks, cameraAccessor.pmGetEyeHeightOld(), cameraAccessor.pmGetEyeHeight())
                - camera.getEntity().getEyeHeight() + entity.getEyeHeight();

        Vec3 entityPos = new Vec3(entity.xo, entity.yo, entity.zo).transform(matrix)
                .lerp(new Vec3(entity.position()).transform(matrix), partialTicks);
        entityPos.y += eyeHeight;
        return entityPos;
    }

    public static Vec3 getEntityEyePositionAssumingSelf(Entity entity, float partialTicks) {
        return getEntityEyePositionAssumingSelf(entity, partialTicks, Mat4.identity());
    }
}