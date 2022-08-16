package io.github.serialsniper.portalmod.client.render.entity;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.FloatBuffer;

import javax.annotation.Nullable;

import org.lwjgl.BufferUtils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.client.render.NewWorldRenderer;
import io.github.serialsniper.portalmod.client.render.PortalCamera;
import io.github.serialsniper.portalmod.client.render.PortalShaders;
import io.github.serialsniper.portalmod.client.render.ter.PortalableBlockTER;
import io.github.serialsniper.portalmod.client.screens.PortalOptionsScreen;
import io.github.serialsniper.portalmod.client.util.RenderUtil;
import io.github.serialsniper.portalmod.common.entities.PortalEntity;
import io.github.serialsniper.portalmod.common.items.PortalGun;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import io.github.serialsniper.portalmod.core.event.ClientEvents;
import io.github.serialsniper.portalmod.core.init.ItemInit;
import io.github.serialsniper.portalmod.core.math.Vec3;
import io.github.serialsniper.portalmod.core.util.PortalPairManager;
import io.github.serialsniper.portalmod.core.util.StencilHelper;
import io.github.serialsniper.portalmod.mixins.FogRendererAccessor;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;

@SuppressWarnings("deprecation")
public class PortalEntityRenderer extends EntityRenderer<PortalEntity> {
    public static final float OFFSET = .00001f;
    private static int recursion = 0;
    
    public PortalEntityRenderer(EntityRendererManager p_i46166_1_) {
        super(p_i46166_1_);
		initBuffer();
    }
    
    public void render(PortalEntity portal, float a, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int b) {
        renderFrame(portal, matrixStack, partialTicks);
    }
    
    private void renderFrame(PortalEntity portal, MatrixStack matrixStack, float partialTicks) {
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
        
    	final boolean open = portal.isOpen() && recursion < PortalOptionsScreen.RECURSION.get();
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

        PortalMod.portalFrameShader.bind();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
            PortalShaders.uniform1i("frameCount", frameCount);
            PortalShaders.uniform1f("frameIndex", frameIndex);
            PortalShaders.uniformMatrix("modelViewProjection", modelViewProjection);
            RenderUtil.bindTexture("texture", "textures/portals/" + (open ? "open_" : "closed_")
            		+ portal.getEnd().getSerializedName() + ".png", 0);

            RenderSystem.depthMask(false);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.depthMask(true);

        RenderSystem.bindTexture(0);
        RenderSystem.disableBlend();
        PortalableBlockTER.unbindBuffer();
        PortalMod.portalFrameShader.unbind();
    }
    
    public static void renderPortals(ActiveRenderInfo camera, float partialTicks) {
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
        ClientWorld level = Minecraft.getInstance().level;
        Vector3d cameraPos = camera.getPosition();
        double d0 = cameraPos.x();
        double d1 = cameraPos.y();
        double d2 = cameraPos.z();
        
        for(Entity entity : level.entitiesForRendering())
            if(entity instanceof PortalEntity)
//                if(Minecraft.getInstance().getEntityRenderDispatcher().shouldRender(entity, frustum, d0, d1, d2))
                    renderPortal((PortalEntity)entity, camera, partialTicks);
    }
    
    private static MatrixStack clipMatrix = new MatrixStack();
    private static PortalEntity currentPortal;
    
    private static void renderPortal(PortalEntity portal, ActiveRenderInfo camera, float partialTicks) {
        if(recursion <= 0) {
            StencilHelper.INSTANCE.enable().clear().disable();
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
        
        if(recursion >= PortalOptionsScreen.RECURSION.get() || !portal.isOpen())
            return;
        
        PortalEntity otherPortal = PortalPairManager.CLIENT.get(portal.getGunUUID(), portal.getEnd().other());
        currentPortal = otherPortal;
        
        FloatBuffer modelViewProjection = getModelViewProjectionMatrix(portal, recursion == 0 ? camera : (PortalCamera)camera, OFFSET);
        
        recursion++;
        
        StencilHelper.INSTANCE.enable();
        
        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
        
        float r = FogRendererAccessor.portalmod_getFogRed();
        float g = FogRendererAccessor.portalmod_getFogGreen();
        float b = FogRendererAccessor.portalmod_getFogBlue();
        
        PortalMod.portalViewShader.bind();
        PortalShaders.uniformMatrix("modelViewProjection", modelViewProjection);
        PortalShaders.uniform4f("color", r, g, b, 1);
        RenderUtil.bindTexture("mask", "textures/portals/portal_mask.png", 0);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        glEnable(GL_DEPTH_CLAMP);
        {
            PortalShaders.uniform1i("phase", 0);
            StencilHelper.INSTANCE.write(GL_EQUAL, recursion - 1, GL_KEEP, GL_KEEP, GL_INCR);
            RenderSystem.depthMask(false);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.depthMask(true);
        }
        
        {
            PortalShaders.uniform1i("phase", 1);
            StencilHelper.INSTANCE.read(GL_EQUAL, recursion);
            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.depthFunc(GL_LESS);
        }
        glDisable(GL_DEPTH_CLAMP);

        RenderSystem.bindTexture(0);
        PortalableBlockTER.unbindBuffer();
        PortalMod.portalViewShader.unbind();
        
        Vector3f thisNormal = portal.getDirection().step();
        thisNormal.mul(.5f);
        Vector3d thisPos = Vector3d.atCenterOf(portal.blockPosition()).subtract(new Vector3d(thisNormal));
        
        Vector3f otherNormal = otherPortal.getDirection().step();
        otherNormal.mul(.5f);
        Vector3d otherPos = Vector3d.atCenterOf(otherPortal.blockPosition()).subtract(new Vector3d(otherNormal));
        
        Vector3f z1 = portal.getDirection().step();
        Vector3f y1 = portal.getUpVector().step();
        Vector3f x1 = y1.copy();
        x1.cross(z1);
        
        Vector3f z2 = otherPortal.getDirection().getOpposite().step();
        Vector3f y2 = otherPortal.getUpVector().step();
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
        
        glEnable(GL_CLIP_PLANE0);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.multMatrix(clipMatrix.last().pose());
        glClipPlane(GL_CLIP_PLANE0, new double[] {0, 0, -1, 0});
        RenderSystem.popMatrix();
            StencilHelper.INSTANCE.read(GL_LEQUAL, recursion);
            NewWorldRenderer.renderLevel(matrixStack, partialTicks, nanos, false, portalCamera, Minecraft.getInstance().gameRenderer,
                    Minecraft.getInstance().gameRenderer.lightTexture, getProjectionMatrix());
        glDisable(GL_CLIP_PLANE0);

        clipMatrix.popPose();

        currentPortal = otherPortal;
        
        portalQuad.bind();
        DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
        
        PortalMod.portalViewShader.bind();
        PortalShaders.uniformMatrix("modelViewProjection", modelViewProjection);
        PortalShaders.uniform4f("color", 0, 0, 0, 1);
        RenderUtil.bindTexture("mask", "textures/portals/portal_mask.png", 0);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        glEnable(GL_DEPTH_CLAMP);
        {
            PortalShaders.uniform1i("phase", 0);
            StencilHelper.INSTANCE.write(GL_EQUAL, recursion, GL_KEEP, GL_KEEP, GL_DECR);
            RenderSystem.depthFunc(GL_ALWAYS);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.drawArrays(7, 0, 4);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthFunc(GL_LESS);
        }
        glDisable(GL_DEPTH_CLAMP);

        RenderSystem.bindTexture(0);
        PortalableBlockTER.unbindBuffer();
        PortalMod.portalViewShader.unbind();

        recursion--;

        if(recursion <= 0) {
            StencilHelper.INSTANCE.disable();
            glDisable(GL_CLIP_PLANE0);
        } else {
            StencilHelper.INSTANCE.read(GL_LEQUAL, recursion);
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
                if(item.getItem() != ItemInit.PORTALGUN.get() || !PortalGun.getUUID(item).equals(portal.getGunUUID()))
                    continue;
                
                portalQuad.bind();
                DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);

                PortalMod.portalHighlightShader.bind();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL_GREATER);
                RenderSystem.disableCull();

                    PortalShaders.uniformMatrix("modelViewProjection", getModelViewProjectionMatrix(portal, null, OFFSET * 3));

                    RenderUtil.bindTexture("texture", "textures/portals/highlight_" + portal.getEnd().getSerializedName() + ".png", 0);
                    PortalShaders.uniform1f("intensity", (float)portal.position().distanceTo(cameraPos));

                    RenderSystem.drawArrays(7, 0, 4);

                RenderSystem.enableCull();
                RenderSystem.depthFunc(GL_LESS);
                RenderSystem.bindTexture(0);
                RenderSystem.disableBlend();
                PortalableBlockTER.unbindBuffer();
                PortalMod.portalHighlightShader.unbind();
            }
        } catch(Exception e) {
            e.printStackTrace();
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

        // todo add roll too
        matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        matrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        matrix.translate(portalNormal.getX() * offset, portalNormal.getY() * offset, portalNormal.getZ() * offset);
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(matrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        matrix.scale(1, 2, 1);
        return matrix.last().pose();
    }
    
    private static FloatBuffer getModelViewProjectionMatrix(PortalEntity portal, PortalCamera camera, float offset) {
        Matrix4f projection = getProjectionMatrix();
        projection.multiply(getModelViewMatrix(portal, camera, offset));
        projection.store(modelViewProjection);
        return modelViewProjection;
    }
    
    private static Matrix4f getModelViewMatrix(PortalEntity portal, PortalCamera camera, float offset) {
        Vector3d cameraPos = camera.getPosition();
        Vector3i portalNormal = portal.getDirection().getNormal();
        MatrixStack matrix = new MatrixStack();
        
        // todo add roll too
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
        return false;
    }
    
    protected void renderNameTag(PortalEntity portal, ITextComponent text, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int someValue) {}
}