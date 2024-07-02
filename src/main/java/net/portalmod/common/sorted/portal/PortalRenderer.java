package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.ITextComponent;
import net.portalmod.client.render.NewWorldRenderer;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.client.screens.PortalModOptionsScreen;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.ShaderInit;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.RenderUtil;
import net.portalmod.core.util.StencilUtil;
import net.portalmod.mixins.accessors.FogRendererAccessor;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

public class PortalRenderer extends EntityRenderer<PortalEntity> {
    public static final float OFFSET = .00001f;
    public static int recursion = 0;
    
    public PortalRenderer(EntityRendererManager p_i46166_1_) {
        super(p_i46166_1_);
        initBuffer();
    }
    
    public void render(PortalEntity portal, float a, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int b) {
        super.render(portal, a, partialTicks, matrixStack, renderBuffer, b);
        renderFrame(portal, matrixStack, partialTicks);
//        matrixStack.popPose();
//        matrixStack.pushPose();
//        renderDuplicateFirstPersonEntity(partialTicks, matrixStack);
    }
    
    private void renderFrame(PortalEntity portal, MatrixStack matrixStack, float partialTicks) {
        if(true)
            return;

        if(recursion > 0) {
            MatrixStack s = new MatrixStack();
            PortalEntity.setupMatrix(s, currentPortal.getDirection(), currentPortal.getUpVector(), Vector3d.ZERO);
            s.translate(.5f, .5f, -.5f);

            Vector3d portalPos = currentPortal.position();
            Vector4f clipPlane = new Vector4f(Vector3f.ZN);
            clipPlane.transform(s.last().pose());
            Vector3d clipPlane3d = new Vector3d(clipPlane.x(), clipPlane.y(), clipPlane.z());
            if(clipPlane3d.dot(portal.position().subtract(portalPos.x, portalPos.y, portalPos.z)) >= 0)
                return;
        }
        
        final boolean open = portal.isOpen() && recursion < PortalModOptionsScreen.RECURSION.get();
        final int frameCount = open ? 1 : 8;
        final int frameTime = 10;
        final float frameIndex = open ? 0 : (((portal.tickCount / frameTime) % frameCount)
                + ((portal.tickCount % frameTime) + partialTicks) / frameTime);
        
        double d0 = MathHelper.lerp((double)partialTicks, portal.xOld, portal.getX());
        double d1 = MathHelper.lerp((double)partialTicks, portal.yOld, portal.getY());
        double d2 = MathHelper.lerp((double)partialTicks, portal.zOld, portal.getZ());
        
        Vector3i portalNormal = portal.getDirection().getNormal();
        
        matrixStack.pushPose();
        matrixStack.translate(-d0, -d1, -d2);
        matrixStack.translate(portalNormal.getX() * OFFSET * 2, portalNormal.getY() * OFFSET * 2, portalNormal.getZ() * OFFSET * 2);
        PortalEntity.setupMatrix(matrixStack, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        matrixStack.scale(1, 2, 1);
        
        Matrix4f projection = getProjectionMatrix();
        projection.multiply(matrixStack.last().pose());
        projection.store(modelViewProjection);

        matrixStack.popPose();
        
        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
//            Shader.uniform1i("frameCount", frameCount);
//            Shader.uniform1f("frameIndex", frameIndex);
//            Shader.uniformMatrix("modelViewProjection", modelViewProjection);
//            RenderUtil.bindTexture("texture", "textures/portal/" + (open ? "open_" : "closed_")
//                    + portal.getEnd().getSerializedName() + ".png", 0);
        
        ShaderInit.PORTAL_FRAME.get().bind()
            .setInt("frameCount", frameCount)
            .setFloat("frameIndex", frameIndex)
            .setMatrix("modelViewProjection", modelViewProjection);
            
        RenderUtil.bindTexture(ShaderInit.PORTAL_FRAME.get(), "texture", "textures/portal/" + (open ? "open_" : "closed_")
                + portal.getColor() + ".png", 0);
            
        RenderSystem.depthMask(false);
        RenderSystem.drawArrays(7, 0, 4);
        RenderSystem.depthMask(true);

        RenderSystem.bindTexture(0);
        RenderSystem.disableBlend();
        unbindBuffer();
        ShaderInit.PORTAL_FRAME.get().unbind();
    }
    
    public static void renderPortals(ActiveRenderInfo camera, float partialTicks) {
        if(true)
            return;

        Minecraft.getInstance().getMainRenderTarget().enableStencil();
        ClientWorld level = Minecraft.getInstance().level;
//        Vector3d cameraPos = camera.getPosition();
//        double d0 = cameraPos.x();
//        double d1 = cameraPos.y();
//        double d2 = cameraPos.z();
        
        for(Entity entity : level.entitiesForRendering())
            if(entity instanceof PortalEntity)
//                if(Minecraft.getInstance().getEntityRenderDispatcher().shouldRender(entity, frustum, d0, d1, d2))
                    renderPortalOld((PortalEntity)entity, camera, partialTicks);
    }

    public static void renderPortalFinal(PortalEntity portal, ActiveRenderInfo camera, float partialTicks) {
        if(recursion > 0)
            return;
        recursion++;

        // todo adapt for client
//        PortalEntity otherPortal = PortalManager.get(portal.getGunUUID(), portal.getEnd().other());
        Optional<PortalEntity> otherPortalOptional = portal.getOtherPortal();

        FloatBuffer modelViewProjection = getModelViewProjectionMatrix(portal, camera, OFFSET);

        if(otherPortalOptional.isPresent()) {
            PortalEntity otherPortal = otherPortalOptional.get();

            currentPortal = otherPortal;

    //        FloatBuffer modelViewProjection = getModelViewProjectionMatrix(portal, recursion == 0 ? camera : (PortalCamera)camera, OFFSET);
//            FloatBuffer modelViewProjection = getModelViewProjectionMatrix(portal, camera, OFFSET);

            StencilUtil.INSTANCE.enable().clear();

            portalQuad.bind();
            DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

            ShaderInit.PORTAL_VIEW.get().bind()
                    .setMatrix("modelViewProjection", modelViewProjection)
                    .setFloat("color", 0, 0, 0, 1);
            RenderUtil.bindTexture(ShaderInit.PORTAL_VIEW.get(), "mask", "textures/portal/portal_mask.png", 0);

            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();

            glEnable(GL_DEPTH_CLAMP);
            {
                ShaderInit.PORTAL_VIEW.get().setInt("phase", 0);
                StencilUtil.INSTANCE.write(GL_ALWAYS, 1, GL_KEEP, GL_KEEP, GL_REPLACE);
                RenderSystem.colorMask(false, false, false, false);
                RenderSystem.depthMask(false);
                RenderSystem.drawArrays(7, 0, 4);
                RenderSystem.depthMask(true);
                RenderSystem.colorMask(true, true, true, true);
            }
            glDisable(GL_DEPTH_CLAMP);

            ShaderInit.PORTAL_VIEW.get().bind()
                    .setFloat("color",
                            FogRendererAccessor.pmGetFogRed(),
                            FogRendererAccessor.pmGetFogGreen(),
                            FogRendererAccessor.pmGetFogBlue(),
                            1);

            ShaderInit.PORTAL_VIEW.get().setInt("phase", 1);
            StencilUtil.INSTANCE.read(GL_EQUAL, 1);
            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.depthFunc(GL_LESS);
            ShaderInit.PORTAL_VIEW.get().setInt("phase", 0);

            RenderSystem.bindTexture(0);
            unbindBuffer();
            ShaderInit.PORTAL_VIEW.get().unbind();






            Vector3f thisNormal = new Vec3(portal.getDirection().getNormal()).to3f();
            thisNormal.mul(.5f);
            Vector3d thisPos = Vector3d.atCenterOf(portal.blockPosition()).subtract(new Vector3d(thisNormal));

            Vector3f otherNormal = new Vec3(otherPortal.getDirection().getNormal()).to3f();
            otherNormal.mul(.5f);
            Vector3d otherPos = Vector3d.atCenterOf(otherPortal.blockPosition()).subtract(new Vector3d(otherNormal));

            Vector3f z1 = new Vec3(portal.getDirection().getNormal()).to3f();
            Vector3f y1 = new Vec3(portal.getUpVector().getNormal()).to3f();
            Vector3f x1 = y1.copy();
            x1.cross(z1);

            Vector3f z2 = new Vec3(otherPortal.getDirection().getOpposite().getNormal()).to3f();
            Vector3f y2 = new Vec3(otherPortal.getUpVector().getNormal()).to3f();
            Vector3f x2 = y2.copy();
            x2.cross(z2);

            Matrix4f portal1Matrix = new Matrix4f(new float[] {
                    x1.x(), x1.y(), x1.z(), 0,
                    y1.x(), y1.y(), y1.z(), 0,
                    z1.x(), z1.y(), z1.z(), 0,
                    0,      0,      0,      1
            });

            Matrix4f portal2Matrix = new Matrix4f(new float[] {
                    x2.x(), x2.y(), x2.z(), 0,
                    y2.x(), y2.y(), y2.z(), 0,
                    z2.x(), z2.y(), z2.z(), 0,
                    0,      0,      0,      1
            });

            portal2Matrix.transpose();
            Matrix4f portalToPortalMatrix = portal2Matrix.copy();
            portalToPortalMatrix.multiply(portal1Matrix);

            MatrixStack cameraStack = new MatrixStack();
            cameraStack.translate(otherPos.x, otherPos.y, otherPos.z);
            cameraStack.last().pose().multiply(portalToPortalMatrix);
            cameraStack.translate(-thisPos.x, -thisPos.y, -thisPos.z);

            Vector4f newCameraPos = new Vector4f(new Vector3f(camera.getPosition()));
            newCameraPos.transform(cameraStack.last().pose());

            Vector3f look, up;

            {
                float xRot = MathHelper.clamp(MathHelper.wrapDegrees(camera.getXRot()), -89.9f, 89.9f);
                float yRot = camera.getYRot();

                PortalCamera dummyCamera = new PortalCamera(Minecraft.getInstance().level, camera.getEntity(),
                        new Vec3(camera.getPosition()), xRot, yRot, 0, partialTicks);

                look = dummyCamera.getLookVector().copy();
                up = dummyCamera.getUpVector().copy();
            }

            Vector4f look4 = new Vector4f(look);
            look4.transform(portalToPortalMatrix);
            look = new Vector3f(look4.x(), look4.y(), look4.z());

            Vector4f up4 = new Vector4f(up);
            up4.transform(portalToPortalMatrix);
            up = new Vector3f(up4.x(), up4.y(), up4.z());

            float xRot = (float)(Math.acos(look.dot(Vector3f.YP)) * 180 / Math.PI - 90);
            Vector3f look2 = look.copy();
            look2.cross(Vector3f.YP);
            Vector3f yaw = Vector3f.YP.copy();
            yaw.cross(look2);

            Vector3f sinVec = Vector3f.ZP.copy();
            sinVec.cross(yaw);
            float sin = sinVec.y();
            float cos = Vector3f.ZP.copy().dot(yaw);
            float yRot = -(float)(Math.atan2(sin, cos) * 180 / Math.PI);

            Vector3f upNew = up.copy();
            Vector3f forwardsNew = look.copy();
            Vector3f rightOrtho = forwardsNew.copy();
            rightOrtho.cross(new Vector3f(0, 1, 0));
            rightOrtho.normalize();
            Vector3f upOrtho = rightOrtho.copy();
            upOrtho.cross(forwardsNew);
            upOrtho.normalize();

            Vector3f sinVec2 = upNew.copy();
            sinVec2.cross(upOrtho);
            float sin2 = (float)Math.sqrt(sinVec2.x() * sinVec2.x() + sinVec2.y() * sinVec2.y() + sinVec2.z() * sinVec2.z())
                    * Math.signum(sinVec2.dot(forwardsNew));
            float cos2 = upNew.copy().dot(upOrtho);
            float roll = -(float)(Math.atan2(sin2, cos2) * 180 / Math.PI);

            PortalCamera portalCamera = new PortalCamera(Minecraft.getInstance().level, camera.getEntity(),
                    new Vec3(newCameraPos.x(), newCameraPos.y(), newCameraPos.z()), xRot, yRot, roll, partialTicks);

            Vector3d cameraPos = camera.getPosition();
            long nanos = Util.getNanos();
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(roll));
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(xRot));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRot + 180));
            portal2Matrix.transpose();








            clipMatrix.pushPose();
            clipMatrix.last().pose().setIdentity();
            clipMatrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
            clipMatrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
            clipMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            PortalEntity.setupMatrix(clipMatrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());

            NewWorldRenderer.currentCamera = portalCamera;

            glEnable(GL_CLIP_PLANE0);
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.matrixMode(GL_MODELVIEW);
            RenderSystem.multMatrix(clipMatrix.last().pose());
            glClipPlane(GL_CLIP_PLANE0, new double[] {0, 0, -1, 0});
            RenderSystem.popMatrix();

            StencilUtil.INSTANCE.read(GL_EQUAL, 1);
            Minecraft.getInstance().levelRenderer.renderLevel(matrixStack, partialTicks, nanos, false, portalCamera, Minecraft.getInstance().gameRenderer,
                    Minecraft.getInstance().gameRenderer.lightTexture, getProjectionMatrix());

            glDisable(GL_CLIP_PLANE0);

            NewWorldRenderer.currentCamera = camera;

            clipMatrix.popPose();



            currentPortal = otherPortal;
        }

        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

        ShaderInit.PORTAL_VIEW.get().bind()
                .setMatrix("modelViewProjection", modelViewProjection)
                .setFloat("color", 0, 0, 0, 1);
        RenderUtil.bindTexture(ShaderInit.PORTAL_VIEW.get(), "mask", "textures/portal/portal_mask.png", 0);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        StencilUtil.INSTANCE.disable();

        glEnable(GL_DEPTH_CLAMP);
        {
            ShaderInit.PORTAL_VIEW.get().setInt("phase", 0);
//            StencilUtil.INSTANCE.read(GL_EQUAL, 1);
//            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.colorMask(true, true, true, true);
//            RenderSystem.depthFunc(GL_LESS);
        }
        glDisable(GL_DEPTH_CLAMP);

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.PORTAL_VIEW.get().unbind();

        StencilUtil.INSTANCE.disable();
//        glDisable(GL_CLIP_PLANE0);






        int ticks = Minecraft.getInstance().player.tickCount;

        final boolean open = portal.isOpen() && recursion < PortalModOptionsScreen.RECURSION.get();
        final int frameCount = open ? 1 : 8;
        final int frameTime = 10;
        final float frameIndex = open ? 0 : (((ticks / frameTime) % frameCount)
                + ((ticks % frameTime) + partialTicks) / frameTime);

        double d0 = MathHelper.lerp(partialTicks, portal.xOld, portal.getX());
        double d1 = MathHelper.lerp(partialTicks, portal.yOld, portal.getY());
        double d2 = MathHelper.lerp(partialTicks, portal.zOld, portal.getZ());

        Vector3i portalNormal = portal.getDirection().getNormal();

//        matrixStack.pushPose();
//        matrixStack.translate(-d0, -d1, -d2);
//        matrixStack.translate(portalNormal.getX() * OFFSET * 2, portalNormal.getY() * OFFSET * 2, portalNormal.getZ() * OFFSET * 2);
//        PortalEntity.setupMatrix(matrixStack, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
//        matrixStack.scale(1, 2, 1);
//
////        Matrix4f projection = getProjectionMatrix();
////        projection.multiply(matrixStack.last().pose());
////        projection.store(modelViewProjection);
//
//        matrixStack.popPose();

        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        ShaderInit.PORTAL_FRAME.get().bind()
                .setInt("frameCount", frameCount)
                .setFloat("frameIndex", frameIndex)
                .setMatrix("modelViewProjection", modelViewProjection);

        RenderUtil.bindTexture(ShaderInit.PORTAL_FRAME.get(), "texture", "textures/portal/portal_" + (open ? "open_" : "closed_")
                + portal.getColor() + ".png", 0);

        RenderSystem.depthMask(false);
        RenderSystem.drawArrays(7, 0, 4);
        RenderSystem.depthMask(true);

        RenderSystem.bindTexture(0);
        RenderSystem.disableBlend();
        unbindBuffer();
        ShaderInit.PORTAL_FRAME.get().unbind();

        recursion--;
    }
    
    private static void renderPortalOld(PortalEntity portal, ActiveRenderInfo camera, float partialTicks) {
        if(recursion <= 0) {
            StencilUtil.INSTANCE.enable().clear().disable();
            

            
            RenderSystem.disableDepthTest();
            

          MatrixStack matrix = new MatrixStack();
          ActiveRenderInfo camera2 = Minecraft.getInstance().gameRenderer.getMainCamera();
          matrix.mulPose(Vector3f.XP.rotationDegrees(camera2.getXRot()));
          matrix.mulPose(Vector3f.YP.rotationDegrees(camera2.getYRot() + 180.0F));
          
          Vector3d cameraPos = camera2.getPosition();
          matrix.translate(0 - cameraPos.x, 60 - cameraPos.y, 0 - cameraPos.z);
          
//          RenderUtil.setClipPlane(0, camera, Vector3d.ZERO, new Vector3d(0, 0, 1));
//          RenderUtil.setClipPlane(1, camera, Vector3d.ZERO, new Vector3d(1, 0, 0));
            //TODO remove
//          Minecraft.getInstance().getItemRenderer().renderStatic(
//                  new ItemStack(Items.DIAMOND),
//                  TransformType.FIXED, 0, 0, matrix,
//                  Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource());
//
//          Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource().endBatch();
          
//          RenderUtil.disableClipPlane(0);
//          RenderUtil.disableClipPlane(1);
            
            RenderSystem.enableDepthTest();
            
            
            
        } else {
            MatrixStack s = new MatrixStack();
            PortalEntity.setupMatrix(s, currentPortal.getDirection(), currentPortal.getUpVector(), Vector3d.ZERO);
            s.translate(.5f, .5f, -.5f);
    
            Vector3d portalPos = currentPortal.position();
            Vector4f clipPlane = new Vector4f(Vector3f.ZN);
            clipPlane.transform(s.last().pose());
            Vector3d clipPlane3d = new Vector3d(clipPlane.x(), clipPlane.y(), clipPlane.z());
            if(clipPlane3d.dot(portal.position().subtract(portalPos.x, portalPos.y, portalPos.z)) >= 0)
                return;
        }
        
        if(recursion >= PortalModOptionsScreen.RECURSION.get() || !portal.isOpen())
            return;
        
        PortalEntity otherPortal = PortalPairCache.CLIENT.get(portal.getGunUUID(), portal.getEnd().other());
        currentPortal = otherPortal;
        
        FloatBuffer modelViewProjection = getModelViewProjectionMatrix(portal, recursion == 0 ? camera : (PortalCamera)camera, OFFSET);
        
        recursion++;
        
        StencilUtil.INSTANCE.enable();
        
        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
        
        float r = FogRendererAccessor.pmGetFogRed();
        float g = FogRendererAccessor.pmGetFogGreen();
        float b = FogRendererAccessor.pmGetFogBlue();
        
//        ShaderInit.PORTAL_VIEW.get().bind();
//        Shader.uniformMatrix("modelViewProjection", modelViewProjection);
//        Shader.uniform4f("color", r, g, b, 1);
//        RenderUtil.bindTextureOld("mask", "textures/portal/portal_mask.png", 0);
        ShaderInit.PORTAL_VIEW.get().bind()
            .setMatrix("modelViewProjection", modelViewProjection)
            .setFloat("color", r, g, b, 1);
        RenderUtil.bindTexture(ShaderInit.PORTAL_VIEW.get(), "mask", "textures/portal/portal_mask.png", 0);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        glEnable(GL_DEPTH_CLAMP);
        {
            ShaderInit.PORTAL_VIEW.get().setInt("phase", 0);
//            Shader.uniform1i("phase", 0);
            StencilUtil.INSTANCE.write(GL_EQUAL, recursion - 1, GL_KEEP, GL_KEEP, GL_INCR);
            RenderSystem.depthMask(false);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.depthMask(true);
        }
        
        {
            ShaderInit.PORTAL_VIEW.get().setInt("phase", 1);
//            Shader.uniform1i("phase", 1);
            StencilUtil.INSTANCE.read(GL_EQUAL, recursion);
            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.depthFunc(GL_LESS);
        }
        glDisable(GL_DEPTH_CLAMP);

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.PORTAL_VIEW.get().unbind();
        
        Vector3f thisNormal = new Vec3(portal.getDirection().getNormal()).to3f();
        thisNormal.mul(.5f);
        Vector3d thisPos = Vector3d.atCenterOf(portal.blockPosition()).subtract(new Vector3d(thisNormal));
        
        Vector3f otherNormal = new Vec3(otherPortal.getDirection().getNormal()).to3f();
        otherNormal.mul(.5f);
        Vector3d otherPos = Vector3d.atCenterOf(otherPortal.blockPosition()).subtract(new Vector3d(otherNormal));
        
        Vector3f z1 = new Vec3(portal.getDirection().getNormal()).to3f();
        Vector3f y1 = new Vec3(portal.getUpVector().getNormal()).to3f();
        Vector3f x1 = y1.copy();
        x1.cross(z1);
        
        Vector3f z2 = new Vec3(otherPortal.getDirection().getOpposite().getNormal()).to3f();
        Vector3f y2 = new Vec3(otherPortal.getUpVector().getNormal()).to3f();
        Vector3f x2 = y2.copy();
        x2.cross(z2);
        
        Matrix4f portal1Matrix = new Matrix4f(new float[] {
                x1.x(), x1.y(), x1.z(), 0,
                y1.x(), y1.y(), y1.z(), 0,
                z1.x(), z1.y(), z1.z(), 0,
                0,      0,      0,      1
        });
        
        Matrix4f portal2Matrix = new Matrix4f(new float[] {
                x2.x(), x2.y(), x2.z(), 0,
                y2.x(), y2.y(), y2.z(), 0,
                z2.x(), z2.y(), z2.z(), 0,
                0,      0,      0,      1
        });

        portal2Matrix.transpose();
        Matrix4f portalToPortalMatrix = portal2Matrix.copy();
        portalToPortalMatrix.multiply(portal1Matrix);
        
        MatrixStack cameraStack = new MatrixStack();
        cameraStack.translate(otherPos.x, otherPos.y, otherPos.z);
        cameraStack.last().pose().multiply(portalToPortalMatrix);
        cameraStack.translate(-thisPos.x, -thisPos.y, -thisPos.z);
        
        Vector4f newCameraPos = new Vector4f(new Vector3f(camera.getPosition()));
        newCameraPos.transform(cameraStack.last().pose());
        
        Vector3f look, up;
        
        {
            float xRot = MathHelper.clamp(MathHelper.wrapDegrees(camera.getXRot()), -89.9f, 89.9f);
            float yRot = camera.getYRot();
            
            PortalCamera dummyCamera = new PortalCamera(Minecraft.getInstance().level, camera.getEntity(),
                    new Vec3(camera.getPosition()), xRot, yRot, 0, partialTicks);
            
            look = dummyCamera.getLookVector().copy();
            up = dummyCamera.getUpVector().copy();
        }
        
        Vector4f look4 = new Vector4f(look);
        look4.transform(portalToPortalMatrix);
        look = new Vector3f(look4.x(), look4.y(), look4.z());
        
        Vector4f up4 = new Vector4f(up);
        up4.transform(portalToPortalMatrix);
        up = new Vector3f(up4.x(), up4.y(), up4.z());
        
        float xRot = (float)(Math.acos(look.dot(Vector3f.YP)) * 180 / Math.PI - 90);
        Vector3f look2 = look.copy();
        look2.cross(Vector3f.YP);
        Vector3f yaw = Vector3f.YP.copy();
        yaw.cross(look2);
        
        Vector3f sinVec = Vector3f.ZP.copy();
        sinVec.cross(yaw);
        float sin = sinVec.y();
        float cos = Vector3f.ZP.copy().dot(yaw);
        float yRot = -(float)(Math.atan2(sin, cos) * 180 / Math.PI);
        
        Vector3f upNew = up.copy();
        Vector3f forwardsNew = look.copy();
        Vector3f rightOrtho = forwardsNew.copy();
        rightOrtho.cross(new Vector3f(0, 1, 0));
        rightOrtho.normalize();
        Vector3f upOrtho = rightOrtho.copy();
        upOrtho.cross(forwardsNew);
        upOrtho.normalize();
        
        Vector3f sinVec2 = upNew.copy();
        sinVec2.cross(upOrtho);
        float sin2 = (float)Math.sqrt(sinVec2.x() * sinVec2.x() + sinVec2.y() * sinVec2.y() + sinVec2.z() * sinVec2.z())
                * Math.signum(sinVec2.dot(forwardsNew));
        float cos2 = upNew.copy().dot(upOrtho);
        float roll = -(float)(Math.atan2(sin2, cos2) * 180 / Math.PI);
        
        PortalCamera portalCamera = new PortalCamera(Minecraft.getInstance().level, camera.getEntity(),
                new Vec3(newCameraPos.x(), newCameraPos.y(), newCameraPos.z()), xRot, yRot, roll, partialTicks);
        
        Vector3d cameraPos = camera.getPosition();
        long nanos = Util.getNanos();
        MatrixStack matrixStack = new MatrixStack();
//        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(roll));
//        matrixStack.mulPose(Vector3f.XP.rotationDegrees(portalCamera.getXRot()));
//        matrixStack.mulPose(Vector3f.YP.rotationDegrees(portalCamera.getYRot() + 180));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(roll));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRot + 180));
//        portal2Matrix.transpose();
        portal2Matrix.transpose();
//        matrixStack.last().pose().multiply(portal2Matrix);
        
        clipMatrix.pushPose();
        clipMatrix.last().pose().setIdentity();
        clipMatrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        clipMatrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        clipMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(clipMatrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        
        NewWorldRenderer.currentCamera = portalCamera;
        
        glEnable(GL_CLIP_PLANE0);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.multMatrix(clipMatrix.last().pose());
        glClipPlane(GL_CLIP_PLANE0, new double[] {0, 0, -1, 0});
        RenderSystem.popMatrix();
            StencilUtil.INSTANCE.read(GL_LEQUAL, recursion);
            NewWorldRenderer.renderLevel(matrixStack, partialTicks, nanos, false, portalCamera, Minecraft.getInstance().gameRenderer,
                    Minecraft.getInstance().gameRenderer.lightTexture, getProjectionMatrix());
        glDisable(GL_CLIP_PLANE0);
        
        NewWorldRenderer.currentCamera = camera;

        clipMatrix.popPose();

        currentPortal = otherPortal;
        
        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
        
//        ShaderInit.PORTAL_VIEW.get()1.bind();
//        Shader.uniformMatrix("modelViewProjection", modelViewProjection);
//        Shader.uniform4f("color", 0, 0, 0, 1);
//        RenderUtil.bindTextureOld("mask", "textures/portal/portal_mask.png", 0);
        ShaderInit.PORTAL_VIEW.get().bind()
            .setMatrix("modelViewProjection", modelViewProjection)
            .setFloat("color", 0, 0, 0, 1);
        RenderUtil.bindTexture(ShaderInit.PORTAL_VIEW.get(), "mask", "textures/portal/portal_mask.png", 0);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        glEnable(GL_DEPTH_CLAMP);
        {
            ShaderInit.PORTAL_VIEW.get().setInt("phase", 0);
//            Shader.uniform1i("phase", 0);
            StencilUtil.INSTANCE.write(GL_EQUAL, recursion, GL_KEEP, GL_KEEP, GL_DECR);
            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthFunc(GL_LESS);
        }
        glDisable(GL_DEPTH_CLAMP);

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.PORTAL_VIEW.get().unbind();

        recursion--;

        if(recursion <= 0) {
            StencilUtil.INSTANCE.disable();
            glDisable(GL_CLIP_PLANE0);
        } else {
            StencilUtil.INSTANCE.read(GL_LEQUAL, recursion);
            glEnable(GL_CLIP_PLANE0);
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.matrixMode(GL_MODELVIEW);
            RenderSystem.multMatrix(clipMatrix.last().pose());
            glClipPlane(GL_CLIP_PLANE0, new double[] {0, 0, -1, 0});
            RenderSystem.popMatrix();
        }
    }
    
    private static MatrixStack clipMatrix = new MatrixStack();
    private static PortalEntity currentPortal;
    
    private static void renderPortal(PortalEntity portal, ActiveRenderInfo camera, float partialTicks) {
        if(recursion <= 0) {
            StencilUtil.INSTANCE.enable().clear().disable();
        } else {
            MatrixStack s = new MatrixStack();
            PortalEntity.setupMatrix(s, currentPortal.getDirection(), currentPortal.getUpVector(), Vector3d.ZERO);
            s.translate(.5f, .5f, -.5f);
            
            Vector3d portalPos = currentPortal.position();
            Vector4f clipPlane = new Vector4f(Vector3f.ZN);
            clipPlane.transform(s.last().pose());
            Vector3d clipPlane3d = new Vector3d(clipPlane.x(), clipPlane.y(), clipPlane.z());
            if(clipPlane3d.dot(portal.position().subtract(portalPos.x, portalPos.y, portalPos.z)) >= 0)
                return;

//            Vec3 portalPos = new Vec3(portal.position());
//            Vec3 currentPortalPos = new Vec3(currentPortal.position());
//            double intersection = new Vec3(Vector3f.ZN)
//                    .transform(new Mat4(s.last().pose()))
//                    .dot(portalPos.sub(currentPortalPos));
//            
//            if(intersection >= 0)
//                return;
        }
        
        if(recursion >= PortalModOptionsScreen.RECURSION.get() || !portal.isOpen())
            return;
        
        PortalEntity otherPortal = PortalPairCache.CLIENT.get(portal.getGunUUID(), portal.getEnd().other());
        currentPortal = otherPortal;
        
        FloatBuffer modelViewProjection = getModelViewProjectionMatrix(portal, recursion == 0 ? camera : (PortalCamera)camera, OFFSET);
        
        recursion++;
        
        StencilUtil.INSTANCE.enable();
        
        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
        
        float r = FogRendererAccessor.pmGetFogRed();
        float g = FogRendererAccessor.pmGetFogGreen();
        float b = FogRendererAccessor.pmGetFogBlue();
        
        ShaderInit.PORTAL_VIEW.get().bind()
            .setMatrix("modelViewProjection", modelViewProjection)
            .setFloat("color", r, g, b, 1);
        RenderUtil.bindTexture(ShaderInit.PORTAL_VIEW.get(), "mask", "textures/portal/portal_mask.png", 0);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        
        glEnable(GL_DEPTH_CLAMP);
        {
            ShaderInit.PORTAL_VIEW.get().setInt("phase", 0);
            StencilUtil.INSTANCE.write(GL_EQUAL, recursion - 1, GL_KEEP, GL_KEEP, GL_INCR);
            RenderSystem.depthMask(false);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.depthMask(true);
        }
        
        {
            ShaderInit.PORTAL_VIEW.get().setInt("phase", 1);
            StencilUtil.INSTANCE.read(GL_EQUAL, recursion);
            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.depthFunc(GL_LESS);
        }
        glDisable(GL_DEPTH_CLAMP);
        
        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.PORTAL_VIEW.get().unbind();
        
        
//        Vec3 thisPos = new Vec3(portal.blockPosition())
//                .add(.5)
//                .sub(new Vec3(portal.getDirection().step())
//                        .mul(.5));
//        
//        Vec3 otherPos = new Vec3(otherPortal.blockPosition())
//                .add(.5)
//                .sub(new Vec3(otherPortal.getDirection().step())
//                        .mul(.5));
//        
//        Vec3 z1 = new Vec3(portal.getDirection().step());
//        Vec3 y1 = new Vec3(portal.getUpVector().step());
//        Vec3 x1 = y1.clone().cross(z1);
//        
//        Vec3 z2 = new Vec3(otherPortal.getDirection().step());
//        Vec3 y2 = new Vec3(otherPortal.getUpVector().step());
//        Vec3 x2 = y1.clone().cross(z1);
//        
//        Mat4 portal1Matrix = new Mat4(
//                x1.x, x1.y, x1.z, 0,
//                y1.x, y1.y, y1.z, 0,
//                z1.x, z1.y, z1.z, 0,
//                0,    0,    0,    1
//        );
//        
//        Mat4 portalToPortalMatrix = new Mat4(
//                x2.x, x2.y, x2.z, 0,
//                y2.x, y2.y, y2.z, 0,
//                z2.x, z2.y, z2.z, 0,
//                0,    0,    0,    1
//        ).transpose().mul(new Mat4(
//                x1.x, x1.y, x1.z, 0,
//                y1.x, y1.y, y1.z, 0,
//                z1.x, z1.y, z1.z, 0,
//                0,    0,    0,    1
//        ));
        
        
        
        Vector3f thisNormal = new Vec3(portal.getDirection().getNormal()).to3f();
        thisNormal.mul(.5f);
        Vector3d thisPos = Vector3d.atCenterOf(portal.blockPosition()).subtract(new Vector3d(thisNormal));
        
        Vector3f otherNormal = new Vec3(otherPortal.getDirection().getNormal()).to3f();
        otherNormal.mul(.5f);
        Vector3d otherPos = Vector3d.atCenterOf(otherPortal.blockPosition()).subtract(new Vector3d(otherNormal));
        
        Vector3f z1 = new Vec3(portal.getDirection().getNormal()).to3f();
        Vector3f y1 = new Vec3(portal.getUpVector().getNormal()).to3f();
        Vector3f x1 = y1.copy();
        x1.cross(z1);
        
        Vector3f z2 = new Vec3(otherPortal.getDirection().getOpposite().getNormal()).to3f();
        Vector3f y2 = new Vec3(otherPortal.getUpVector().getNormal()).to3f();
        Vector3f x2 = y2.copy();
        x2.cross(z2);
        
        Matrix4f portal1Matrix = new Matrix4f(new float[] {
                x1.x(), x1.y(), x1.z(), 0,
                y1.x(), y1.y(), y1.z(), 0,
                z1.x(), z1.y(), z1.z(), 0,
                0,      0,      0,      1
        });
        
        Matrix4f portal2Matrix = new Matrix4f(new float[] {
                x2.x(), x2.y(), x2.z(), 0,
                y2.x(), y2.y(), y2.z(), 0,
                z2.x(), z2.y(), z2.z(), 0,
                0,      0,      0,      1
        });

        portal2Matrix.transpose();
        Matrix4f portalToPortalMatrix = portal2Matrix.copy();
        portalToPortalMatrix.multiply(portal1Matrix);
        
        MatrixStack cameraStack = new MatrixStack();
        cameraStack.translate(otherPos.x, otherPos.y, otherPos.z);
        cameraStack.last().pose().multiply(portalToPortalMatrix);
        cameraStack.translate(-thisPos.x, -thisPos.y, -thisPos.z);
        
//        Vec3 newCameraPos = new Vec3(camera.getPosition())
//                .transform(new Mat4(cameraStack.last().pose()));
        
        Vector4f newCameraPos = new Vector4f(new Vector3f(camera.getPosition()));
        newCameraPos.transform(cameraStack.last().pose());
        
        Vector3f look, up;
//        Vec3 look, up;
        
        {
            float xRot = MathHelper.clamp(MathHelper.wrapDegrees(camera.getXRot()), -89.9f, 89.9f);
            float yRot = camera.getYRot();
            
            PortalCamera dummyCamera = new PortalCamera(Minecraft.getInstance().level, camera.getEntity(),
                    new Vec3(camera.getPosition()), xRot, yRot, 0, partialTicks);
            
            look = dummyCamera.getLookVector().copy();
            up = dummyCamera.getLookVector().copy();
            
//            look = new Vec3(dummyCamera.getLookVector()).transform(portalToPortalMatrix);
//            up = new Vec3(dummyCamera.getUpVector()).transform(portalToPortalMatrix);
        }
        
        Vector4f look4 = new Vector4f(look);
        look4.transform(portalToPortalMatrix);
        look = new Vector3f(look4.x(), look4.y(), look4.z());
        
        Vector4f up4 = new Vector4f(up);
        up4.transform(portalToPortalMatrix);
        up = new Vector3f(up4.x(), up4.y(), up4.z());
        
        float xRot = (float)(Math.acos(look.dot(Vector3f.YP)) * 180 / Math.PI - 90);
//        Vec3 yaw = new Vec3(Vector3f.YP)
//                .cross(look.clone()
//                        .cross(new Vec3(Vector3f.YP)));
        
        Vector3f look2 = look.copy();
        look2.cross(Vector3f.YP);
        Vector3f yaw = Vector3f.YP.copy();
        yaw.cross(look2);
        
//        Vec3 sinVec = new Vec3(Vector3f.ZP)
//                .cross(yaw);
//        double sin = sinVec.y;
//        double cos = new Vec3(Vector3f.ZP).dot(yaw);
//        float yRot = -(float)(Math.atan2(sin, cos) * 180 / Math.PI);
        
        Vector3f sinVec = Vector3f.ZP.copy();
        sinVec.cross(yaw);
        float sin = sinVec.y();
        float cos = Vector3f.ZP.copy().dot(yaw);
        float yRot = -(float)(Math.atan2(sin, cos) * 180 / Math.PI);
        
//        Vec3 upNew = up.clone();
//        Vec3 forwardsNew = look.clone();
//        Vec3 rightOrtho = forwardsNew.clone()
//                .cross(new Vec3(Vector3f.YP))
//                .normalize();
//        Vec3 upOrtho = rightOrtho.clone()
//                .cross(forwardsNew)
//                .normalize();
        
        Vector3f upNew = up.copy();
        Vector3f forwardsNew = look.copy();
        Vector3f rightOrtho = forwardsNew.copy();
        rightOrtho.cross(new Vector3f(0, 1, 0));
        rightOrtho.normalize();
        Vector3f upOrtho = rightOrtho.copy();
        upOrtho.cross(forwardsNew);
        upOrtho.normalize();
        
//        Vec3 sinVec2 = upNew.clone().cross(upOrtho);
//        double sin2 = Math.sqrt(sinVec2.x * sinVec2.x + sinVec2.y * sinVec2.y + sinVec2.z * sinVec2.z)
//                * Math.signum(sinVec2.dot(forwardsNew));
//        double cos2 = upNew.dot(upOrtho);
//        float roll = -(float)(Math.atan2(sin2, cos2) * 180 / Math.PI);
        
        Vector3f sinVec2 = upNew.copy();
        sinVec2.cross(upOrtho);
        float sin2 = (float)Math.sqrt(sinVec2.x() * sinVec2.x() + sinVec2.y() * sinVec2.y() + sinVec2.z() * sinVec2.z())
                * Math.signum(sinVec2.dot(forwardsNew));
        float cos2 = upNew.copy().dot(upOrtho);
        float roll = -(float)(Math.atan2(sin2, cos2) * 180 / Math.PI);
        
        PortalCamera portalCamera = new PortalCamera(Minecraft.getInstance().level, camera.getEntity(),
                new Vec3(newCameraPos.x(), newCameraPos.y(), newCameraPos.z()), xRot, yRot, roll, partialTicks);
        
        Vector3d cameraPos = camera.getPosition();
        long nanos = Util.getNanos();
        MatrixStack matrixStack = new MatrixStack();
//        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(roll));
//        matrixStack.mulPose(Vector3f.XP.rotationDegrees(portalCamera.getXRot()));
//        matrixStack.mulPose(Vector3f.YP.rotationDegrees(portalCamera.getYRot() + 180));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(roll));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(xRot));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRot + 180));
//        portal2Matrix.transpose();
//        portal2Matrix.transpose();
//        matrixStack.last().pose().multiply(portal2Matrix);
        
        clipMatrix.pushPose();
        clipMatrix.last().pose().setIdentity();
        clipMatrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        clipMatrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        clipMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(clipMatrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        
        NewWorldRenderer.currentCamera = portalCamera;
        
        glEnable(GL_CLIP_PLANE0);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.multMatrix(clipMatrix.last().pose());
        glClipPlane(GL_CLIP_PLANE0, new double[] {0, 0, -1, 0});
        RenderSystem.popMatrix();
            StencilUtil.INSTANCE.read(GL_LEQUAL, recursion);
            NewWorldRenderer.renderLevel(matrixStack, partialTicks, nanos, false, portalCamera, Minecraft.getInstance().gameRenderer,
                    Minecraft.getInstance().gameRenderer.lightTexture, getProjectionMatrix());
        glDisable(GL_CLIP_PLANE0);
        
        NewWorldRenderer.currentCamera = camera;

        clipMatrix.popPose();

        currentPortal = otherPortal;
        
        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
        
        ShaderInit.PORTAL_VIEW.get().bind()
                .setMatrix("modelViewProjection", modelViewProjection)
                .setFloat("color", 0, 0, 0, 1);
        RenderUtil.bindTexture(ShaderInit.PORTAL_VIEW.get(), "mask", "textures/portal/portal_mask.png", 0);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        glEnable(GL_DEPTH_CLAMP);
        {
            ShaderInit.PORTAL_VIEW.get().setInt("phase", 0);
            StencilUtil.INSTANCE.write(GL_EQUAL, recursion, GL_KEEP, GL_KEEP, GL_DECR);
            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthFunc(GL_LESS);
        }
        glDisable(GL_DEPTH_CLAMP);

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.PORTAL_VIEW.get().unbind();

        recursion--;

        if(recursion <= 0) {
            StencilUtil.INSTANCE.disable();
            glDisable(GL_CLIP_PLANE0);
        } else {
            StencilUtil.INSTANCE.read(GL_LEQUAL, recursion);
            glEnable(GL_CLIP_PLANE0);
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.matrixMode(GL_MODELVIEW);
            RenderSystem.multMatrix(clipMatrix.last().pose());
            glClipPlane(GL_CLIP_PLANE0, new double[] {0, 0, -1, 0});
            RenderSystem.popMatrix();
        }
    }
    
    public static void renderHighlights() {
        try {
            ClientWorld level = Minecraft.getInstance().level;
            for(Entity entity : level.entitiesForRendering()) {
                if(!(entity instanceof PortalEntity))
                    continue;

                PortalEntity portal = (PortalEntity)entity;
                Vector3d cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

                ItemStack item = Minecraft.getInstance().player.getMainHandItem();
                if(!(item.getItem() instanceof PortalGun) || !PortalGun.getUUID(item).equals(portal.getGunUUID()))
                    continue;
                
                portalQuad.bind();
                DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

//                ShaderInit.PORTAL_HIGHLIGHT.get().bind();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL_GREATER);
                RenderSystem.disableCull();
                
//                    Shader.uniformMatrix("modelViewProjection", getModelViewProjectionMatrix(portal, null, OFFSET * 3));
                    ShaderInit.PORTAL_HIGHLIGHT.get().bind()
                        .setMatrix("modelViewProjection", getModelViewProjectionMatrix(portal, null, OFFSET * 3));
                    
                    RenderUtil.bindTexture(ShaderInit.PORTAL_HIGHLIGHT.get(), "texture",
                            "textures/portal/highlight_" + portal.getColor() + ".png", 0);
//                    Shader.uniform1f("intensity", (float)portal.position().distanceTo(cameraPos));
                    ShaderInit.PORTAL_HIGHLIGHT.get().setFloat("intensity", (float)portal.position().distanceTo(cameraPos));
                    
                    RenderSystem.drawArrays(7, 0, 4);
                    
                RenderSystem.enableCull();
                RenderSystem.depthFunc(GL_LESS);
                RenderSystem.bindTexture(0);
                RenderSystem.disableBlend();
                unbindBuffer();
                ShaderInit.PORTAL_HIGHLIGHT.get().unbind();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void unbindBuffer() {
        DefaultVertexFormats.POSITION_TEX.clearBufferState();
        VertexBuffer.unbind();
    }
    
    public static boolean shouldNotRenderEntity(Entity entity, double camX, double camY, double camZ) {
//        if(!(entity instanceof PortalEntity))
//            return true;
        
        if(entity != Minecraft.getInstance().cameraEntity)
            return false;
        
//        Vector3d pos = entity.getEyePosition(1);
//        double x = pos.x - camX;
//        double y = pos.y - camY;
//        double z = pos.z - camZ;
        
//        if(x * x + y * y + z * z < .2)
//            return true;
        
        return false;
    }
    
    public static void renderDuplicateFirstPersonEntity(float partialTicks, MatrixStack matrixStack) {
        return;
        
//        Minecraft mc = Minecraft.getInstance();
//        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
//        Vector3d cameraPos = camera.getPosition();
//        
//        if(camera.isDetached() || mc.cameraEntity == null)
//            return;
//        
//        renderDuplicateEntity(
//                mc.cameraEntity,
//                cameraPos.x, cameraPos.y, cameraPos.z,
//                partialTicks, matrixStack,
//                mc.levelRenderer.renderBuffers.bufferSource(),
//                mc.levelRenderer.entityRenderDispatcher);
    }
    
    public static void renderDuplicateEntity(Entity entity, double x, double y, double z, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, EntityRendererManager erm) {
//        if(true)
//            return;
        if(entity instanceof PlayerEntity)
            return;
        
        double d0 = MathHelper.lerp((double)partialTicks, entity.xOld, entity.getX()) - x;
        double d1 = MathHelper.lerp((double)partialTicks, entity.yOld, entity.getY()) - y;
        double d2 = MathHelper.lerp((double)partialTicks, entity.zOld, entity.getZ()) - z;
        float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);

        Vector3d curPos = new Vector3d(
                MathHelper.lerp(partialTicks, entity.xOld, entity.getX()),
                MathHelper.lerp(partialTicks, entity.yOld, entity.getY()),
                MathHelper.lerp(partialTicks, entity.zOld, entity.getZ())
        );
        
        if(entity instanceof PortalEntity)
            return;
        
//        List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(.2));
        List<PortalEntity> entities = PortalEntity.getOpenPortals(entity.level, entity.getBoundingBox().inflate(.2), portal -> true);
        for(Entity e : entities) {
            if(e instanceof PortalEntity) {
                PortalEntity portal = (PortalEntity)e;
                if(!portal.isOpen())
                    continue;
                
//                Vector3d offset = portal.getRenderOffset();
//                if(offset == Vector3d.ZERO)
//                    continue;

//                Vector3d offset = portal.teleportPoint(new Vec3(entity.getPosition(partialTicks))).to3d()
//                        .subtract(portal.getCenter());

                Vector3d centerOffset = entity.getBoundingBox().getCenter().subtract(entity.position());
                Vector3d entityCenter = entity.getPosition(partialTicks).add(centerOffset);

//                Vector3d offset = portal
//                        .teleportPoint(new Vec3(entityCenter))
//                        .to3d()
//                        .subtract(entityCenter);
;
                Vector3d offset = portal
                        .teleportPoint(new Vec3(curPos))
                        .to3d()
                        .subtract(curPos);
                
                PortalEntity otherPortal = portal.getOtherPortal().get();
//                Vector3d otherPortalPivot = portal.getPivotPoint().add(offset);
                Vector3d otherPortalPivot = portal.getOtherPortal().get().getPivotPoint();
                ActiveRenderInfo camera = NewWorldRenderer.getCurrentCamera();
                
//                Vector3f forwards = otherPortal.getDirection().step();
//                Vector3f up = otherPortal.getUpVector().step();
//                Vector3f right = up.copy();
//                right.cross(forwards);
//                Vector3f down = up.copy();
//                down.mul(-1);
//                Vector3f left = right.copy();
//                left.mul(-1);
                
//                if(shouldNotRenderEntity(entity, x - offset.x, y - offset.y, z - offset.z))
//                    continue;
                
                
                
                Vec3 forwards = new Vec3(otherPortal.getDirection().getNormal());
                Vec3 up = new Vec3(otherPortal.getUpVector().getNormal());
                Vec3 right = up.clone().cross(forwards);
                Vec3 down = up.clone().negate();
                Vec3 left = right.clone().negate();
                
//                RenderUtil.setClipPlane(0, camera, portal.getCenter().add(offset),
//                        new Vector3d(otherPortal.getDirection().step()));

                RenderUtil.setClipPlane(0, camera, portal.getOtherPortal().get().getCenter(),
                        new Vec3(otherPortal.getDirection().getNormal()).to3d());
                
                RenderUtil.setClipPlane(1, camera, otherPortalPivot.add(right.clone().mul(.5).to3d()), left.to3d());
                RenderUtil.setClipPlane(2, camera, otherPortalPivot.add(left.clone().mul(.5).to3d()), right.to3d());
                RenderUtil.setClipPlane(3, camera, otherPortalPivot.add(up.clone().mul(.5).add(up).to3d()), down.to3d());
                RenderUtil.setClipPlane(4, camera, otherPortalPivot.add(down.clone().mul(.5).to3d()), up.to3d());

//                Vector3d vector3d = erm.getRenderer(entity).getRenderOffset(entity, partialTicks);
//                double d3 = d0 + vector3d.x();
//                double d4 = d1 + vector3d.y();
//                double d5 = d2 + vector3d.z();

                // todo actually transform

                matrixStack.pushPose();
                matrixStack.translate(offset.x, offset.y, offset.z);
//                matrixStack.translate(d3, d4, d5);
//                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
//                matrixStack.translate(-d3, -d4, -d5);
                erm.render(entity, d0, d1, d2, f,
                        partialTicks, matrixStack, renderTypeBuffer,
                        erm.getPackedLightCoords(entity, partialTicks));
                matrixStack.popPose();
                
                RenderUtil.disableClipPlane(0);
                RenderUtil.disableClipPlane(1);
                RenderUtil.disableClipPlane(2);
                RenderUtil.disableClipPlane(3);
                RenderUtil.disableClipPlane(4);
            }
        }
    }

    private static final FloatBuffer modelViewProjection = BufferUtils.createFloatBuffer(16);
    
    private static FloatBuffer getModelViewProjectionMatrix(PortalEntity portal, @Nullable ActiveRenderInfo camera, float offset) {
        Matrix4f projection = getProjectionMatrix();
        projection.multiply(getModelViewMatrix(portal, camera, offset));
        projection.store(modelViewProjection);
        return modelViewProjection;
    }
    
    private static Matrix4f getModelViewMatrix(PortalEntity portal, @Nullable ActiveRenderInfo camera, float offset) {
        if(camera == null)
            camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        
        Vector3d cameraPos = camera.getPosition();
        Vector3i portalNormal = portal.getDirection().getNormal();
        MatrixStack matrix = new MatrixStack();

        // TODO add roll too
        matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        matrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        matrix.translate(portalNormal.getX() * offset, portalNormal.getY() * offset, portalNormal.getZ() * offset);
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(matrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        matrix.scale(1, 2, 1);
        return matrix.last().pose();
    }
    
    private static FloatBuffer getModelViewProjectionMatrix(PortalEntity portal, @Nullable PortalCamera camera, float offset) {
        if(camera == null)
            return getModelViewProjectionMatrix(portal, (ActiveRenderInfo)null, offset);
        
        Matrix4f projection = getProjectionMatrix();
        projection.multiply(getModelViewMatrix(portal, camera, offset));
        projection.store(modelViewProjection);
        return modelViewProjection;
    }
    
    private static Matrix4f getModelViewMatrix(PortalEntity portal, @Nullable PortalCamera camera, float offset) {
        if(camera == null)
            return getModelViewMatrix(portal, (ActiveRenderInfo)null, offset);
        
        Vector3d cameraPos = camera.getPosition();
        Vector3i portalNormal = portal.getDirection().getNormal();
        MatrixStack matrix = new MatrixStack();
        
        // TODO add roll too
        matrix.mulPose(Vector3f.ZP.rotationDegrees(camera.getRoll()));
        matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        matrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        matrix.translate(portalNormal.getX() * offset, portalNormal.getY() * offset, portalNormal.getZ() * offset);
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(matrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        matrix.scale(1, 2, 1);
        return matrix.last().pose();
    }
    
    private static final float[] projectionBuffer = new float[16];
    
    private static Matrix4f getProjectionMatrix() {
        glGetFloatv(GL_PROJECTION_MATRIX, projectionBuffer);
        Matrix4f proj = new Matrix4f(projectionBuffer);
        proj.transpose();
        return proj;
    }
    
    private static VertexBuffer portalQuad;
    
    public static void initBuffer() {
        portalQuad = new VertexBuffer(DefaultVertexFormats.POSITION_TEX);

        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        builder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.vertex(0, 0, 0).uv(0, 0).endVertex();
        builder.vertex(1, 0, 0).uv(1, 0).endVertex();
        builder.vertex(1, 1, 0).uv(1, 1).endVertex();
        builder.vertex(0, 1, 0).uv(0, 1).endVertex();
        builder.end();

        portalQuad.upload(builder);
    }
    
    public ResourceLocation getTextureLocation(PortalEntity p_110775_1_) {
        return AtlasTexture.LOCATION_BLOCKS;
    }
    
    protected boolean shouldShowName(PortalEntity portal) {
        return portal.hasCustomName();
    }
    
    protected void renderNameTag(PortalEntity portal, ITextComponent text, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int light) {
        matrixStack.pushPose();
        Vector3d down = new Vec3(portal.getUpVector().getNormal()).mul(-.5).to3d();
        matrixStack.translate(down.x, down.y, down.z);
        Vector3d forwards = new Vec3(portal.getDirection().getNormal()).mul(.2).to3d();
        matrixStack.translate(forwards.x, forwards.y, forwards.z);
        super.renderNameTag(portal, text, matrixStack, renderBuffer, light);
        matrixStack.popPose();
    }
}