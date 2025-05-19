package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.portalmod.PortalMod;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.client.screens.PortalModOptionsScreen;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.ShaderInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.RenderUtil;
import net.portalmod.core.util.StencilUtil;
import net.portalmod.core.util.VertexRenderer;
import net.portalmod.mixins.accessors.FogRendererAccessor;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

import javax.annotation.Nullable;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

public class PortalRenderer {
    public static int recursion = 0;

    private static final VertexRenderer portalMesh = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);
    private static final VertexRenderer screenQuad = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);
    private static final VertexRenderer blitQuad = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);

    static {
        portalMesh.reset();
        portalMesh.data(bufferBuilder -> {
            bufferBuilder.vertex(0, 0, 0).uv(0, 0).endVertex();
            bufferBuilder.vertex(1, 0, 0).uv(1, 0).endVertex();
            bufferBuilder.vertex(1, 1, 0).uv(1, 1).endVertex();
            bufferBuilder.vertex(0, 1, 0).uv(0, 1).endVertex();
        });

        screenQuad.reset();
        screenQuad.data(bufferBuilder -> {
            bufferBuilder.vertex(-1, -1, 1).uv(0, 0).endVertex();
            bufferBuilder.vertex( 1, -1, 1).uv(1, 0).endVertex();
            bufferBuilder.vertex( 1,  1, 1).uv(1, 1).endVertex();
            bufferBuilder.vertex(-1,  1, 1).uv(0, 1).endVertex();
        });

        blitQuad.reset();
        blitQuad.data(bufferBuilder -> {
            bufferBuilder.vertex(-1, -1, 0).uv(0, 0).endVertex();
            bufferBuilder.vertex( 1, -1, 0).uv(1, 0).endVertex();
            bufferBuilder.vertex( 1,  1, 0).uv(1, 1).endVertex();
            bufferBuilder.vertex(-1,  1, 0).uv(0, 1).endVertex();
        });
    }

    private static void renderMask(Matrix4f modelView) {
        glUseProgram(0);

        glEnable(GL_TEXTURE_2D);
        RenderSystem.activeTexture(GL_TEXTURE0);
        Minecraft.getInstance().textureManager.bind(new ResourceLocation(PortalMod.MODID, "textures/portal/portal_mask.png"));

        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        GL11.glEnable(GL_ALPHA_TEST);
        glEnable(GL_DEPTH_CLAMP);
        RenderSystem.depthMask(false);
        portalMesh.render(new Mat4(modelView));
        RenderSystem.depthMask(true);
        glDisable(GL_DEPTH_CLAMP);
        GL11.glDisable(GL_ALPHA_TEST);

        RenderSystem.colorMask(true, true, true, true);

        RenderSystem.bindTexture(0);
        unbindBuffer();
    }

    private static void renderBackground() {
        ShaderInit.COLOR.get().bind().setFloat(
                "color",
                FogRendererAccessor.pmGetFogRed(),
                FogRendererAccessor.pmGetFogGreen(),
                FogRendererAccessor.pmGetFogBlue(),
                1
        );

        RenderSystem.depthFunc(GL_ALWAYS);
        screenQuad.render();
        RenderSystem.depthFunc(GL_LESS);

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.COLOR.get().unbind();
    }

    private static void renderDepth(Matrix4f modelViewProjection) {
        ShaderInit.PORTAL_VIEW.get().bind()
                .setMatrix("modelViewProjection", modelViewProjection)
                .setFloat("color", 1, 0, 0, 1);
        ShaderInit.PORTAL_VIEW.get().setInt("phase", 0);
        RenderUtil.bindTexture(ShaderInit.PORTAL_VIEW.get(), "mask", "textures/portal/portal_mask.png", 0);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        glEnable(GL_DEPTH_CLAMP);
        RenderSystem.depthFunc(GL_ALWAYS);
        RenderSystem.colorMask(false, false, false, true);
        portalMesh.render();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL_LESS);
        glDisable(GL_DEPTH_CLAMP);

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.PORTAL_VIEW.get().unbind();
    }

    private static final HashMap<ResourceLocation, Tuple<Integer, Integer>> TEXTURE_SIZES = new HashMap<>();

    private static void renderBorder(PortalEntity portal, float partialTicks, Matrix4f modelViewProjection) {
        Minecraft mc = Minecraft.getInstance();

        if(mc.player == null)
            return;

        int ticks = mc.player.tickCount;
        boolean open = portal.isOpen() && recursion <= PortalModOptionsScreen.RECURSION.get();
        String path = "textures/portal/" + (open ? "open_" : "closed_") + portal.getColor() + ".png";
        ResourceLocation location = new ResourceLocation(PortalMod.MODID, path);

        if(!TEXTURE_SIZES.containsKey(location)) {
            try {
                SimpleTexture.TextureData data = SimpleTexture.TextureData.load(mc.getResourceManager(), location);
                TEXTURE_SIZES.put(location, new Tuple<>(data.getImage().getWidth(), data.getImage().getHeight()));
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }
        }

        Tuple<Integer, Integer> textureSize = TEXTURE_SIZES.get(location);
        int frameCount = textureSize.getB() / (2 * textureSize.getA());
        int frameTime = 5;
        float frameIndex = ((int)(ticks / frameTime) % frameCount) + ((ticks % frameTime) + partialTicks) / frameTime;

        ShaderInit.PORTAL_FRAME.get().bind()
                .setInt("frameCount", frameCount)
                .setFloat("frameIndex", frameIndex)
                .setMatrix("modelViewProjection", modelViewProjection);

        RenderUtil.bindTexture(ShaderInit.PORTAL_FRAME.get(), "texture", path, 0);

        RenderSystem.enableDepthTest();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        portalMesh.render();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.PORTAL_FRAME.get().unbind();
    }

    private static Tuple<Mat4, Mat4> getPortalToPortalMatrix(PortalEntity portal, PortalEntity otherPortal) {
        Vector3d thisPos = portal.position();
        Vector3d otherPos = otherPortal.position();

        OrthonormalBasis srcBasis = portal.getSourceBasis();
        OrthonormalBasis dstBasis = otherPortal.getDestinationBasis();
        Mat4 changeOfBasisMatrix = srcBasis.getChangeOfBasisMatrix(dstBasis);

        Mat4 portalToPortalMatrixFull = Mat4.identity()
                .mul(Mat4.createTranslation(otherPos.x, otherPos.y, otherPos.z))
                .mul(changeOfBasisMatrix)
                .mul(Mat4.createTranslation(-thisPos.x, -thisPos.y, -thisPos.z));
        return new Tuple<>(changeOfBasisMatrix, portalToPortalMatrixFull);
    }

    private static void setClipPlane(int plane, Matrix4f matrix, Vec3 vector) {
        glEnable(GL_CLIP_PLANE0 + plane);
        RenderSystem.matrixMode(GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(matrix);
        glClipPlane(GL_CLIP_PLANE0 + plane, new double[] { vector.x, vector.y, vector.z, 0 });
        RenderSystem.popMatrix();
    }

    private static void disableClipPlane(int plane) {
        glDisable(GL_CLIP_PLANE0 + plane);
    }

    private static Framebuffer portalTempFBO = new Framebuffer(
            Minecraft.getInstance().getWindow().getWidth(),
            Minecraft.getInstance().getWindow().getHeight(),
            true,
            Minecraft.ON_OSX
    );

    private static int recursionDepth = 0;
    public static int renderedPortals = 0;
    public static int actuallyRenderedPortals = 0;
    public static boolean canClear = true;
    private static boolean fabulousGraphics = false;

    public static boolean renderPortals(ClientWorld level, ActiveRenderInfo camera, ClippingHelper clippingHelper, float partialTicks) {
        if(recursionDepth == 0) {
            fabulousGraphics = Minecraft.getInstance().options.graphicsMode == GraphicsFanciness.FABULOUS;

            PortalRenderer.canClear = fabulousGraphics;
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

            FogRenderer.setupColor(camera, partialTicks, level,
                    Minecraft.getInstance().options.renderDistance,
                    Minecraft.getInstance().gameRenderer.getDarkenWorldAmount(partialTicks));

            GL11.glStencilMask(~0);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);

            int w = Minecraft.getInstance().getWindow().getWidth();
            int h = Minecraft.getInstance().getWindow().getHeight();

            if(fabulousGraphics) {
                if(portalTempFBO.width != w || portalTempFBO.height != h)
                    portalTempFBO.resize(w, h, Minecraft.ON_OSX);

                portalTempFBO.enableStencil();
                portalTempFBO.bindWrite(false);
                GL11.glStencilMask(~0);
                GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
                portalTempFBO.setClearColor(0, 0, 0, 0);
                portalTempFBO.clear(Minecraft.ON_OSX);
            }

            actuallyRenderedPortals = 0;
        }

        renderedPortals = 0;

        for(Entity entity : level.entitiesForRendering()) {
            if(entity instanceof PortalEntity) {
                recursionDepth++;
                renderPortal((PortalEntity) entity, camera, clippingHelper, partialTicks, fabulousGraphics);
                renderedPortals++;
                recursionDepth--;
            }
        }

        Framebuffer mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        mainRenderTarget.bindWrite(false);

        if(fabulousGraphics) {
            StencilUtil.INSTANCE.disable();

            glClearColor(
                    FogRendererAccessor.pmGetFogRed(),
                    FogRendererAccessor.pmGetFogGreen(),
                    FogRendererAccessor.pmGetFogBlue(),
                    1
            );
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if(renderedPortals > 0) {
                glEnable(GL_BLEND);
                blitFBOtoFBO(portalTempFBO, mainRenderTarget);
                mainRenderTarget.copyDepthFrom(portalTempFBO);
            }
        }

        mainRenderTarget.bindWrite(false);
        return true;
    }

    private static final Deque<PortalEntity> portalStack = new ArrayDeque<>();

    private static boolean discardPortal(PortalEntity portal, ActiveRenderInfo camera, ClippingHelper clippingHelper) {
        Vec3 cameraPos = new Vec3(camera.getPosition());
        Vec3 portalPos = new Vec3(portal.position());
        Vec3 portalToCamera = cameraPos.sub(portalPos);
        Vec3 portalNormal = new Vec3(portal.getDirection());

        // discard portals facing away from camera
        if(portalToCamera.magnitude() > 1 && portalToCamera.clone().normalize().dot(portalNormal) < 0)
            return true;

        // discard portals behind parent portal
        if(!portalStack.isEmpty()) {
            PortalEntity parentPortal = portalStack.peek();
            Vec3 parentPortalPos = new Vec3(parentPortal.position());
            Vec3 parentPortalNormal = new Vec3(parentPortal.getDirection());
            Vec3 parentPortalPosWithMargin = parentPortalPos.clone().sub(parentPortalNormal.clone().mul(2));
            Vec3 parentPortalToPortal = portalPos.clone().sub(parentPortalPosWithMargin);
            if(parentPortalToPortal.normalize().dot(parentPortalNormal) < 0)
                return true;

            // discard self
            if(portal == portalStack.peek())
                return true;
        }

        // discard portals outside view frustum
        return portalToCamera.magnitude() > 1 && !clippingHelper.isVisible(portal.getBoundingBox());
    }

    private static void setupClipPlane(int plane, MatrixStack clipMatrix, PortalEntity portal, ActiveRenderInfo camera) {
        Vector3d cameraPos = camera.getPosition();
        clipMatrix.last().pose().setIdentity();
        if(camera instanceof PortalCamera)
            clipMatrix.mulPose(Vector3f.ZP.rotationDegrees(((PortalCamera)camera).getRoll()));
        clipMatrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        clipMatrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        clipMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(clipMatrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());

        setClipPlane(plane, clipMatrix.last().pose(), new Vec3(0, 0, -1));
    }

    private static ActiveRenderInfo setupCameraAndMatrix(ActiveRenderInfo camera, MatrixStack matrixStack, PortalEntity portal, float partialTicks) {
        if(!portal.getOtherPortal().isPresent())
            return camera;

        Tuple<Mat4, Mat4> portalToPortalMatrices = getPortalToPortalMatrix(portal, portal.getOtherPortal().get());
        Mat4 portalToPortalMatrix = portalToPortalMatrices.getA();
        Mat4 portalToPortalMatrixFull = portalToPortalMatrices.getB();

        Vec3 newCameraPos = new Vec3(camera.getPosition()).transform(portalToPortalMatrixFull);

        float xRot = MathHelper.clamp(MathHelper.wrapDegrees(camera.getXRot()), -89.9f, 89.9f);
        float yRot = camera.getYRot();
        float zRot = camera instanceof PortalCamera ? ((PortalCamera)camera).getRoll() : 0;
        OrthonormalBasis basis = EulerConverter.toVectors(xRot, yRot, zRot);
        basis.transform(portalToPortalMatrix);
        EulerConverter.EulerAngles angles = EulerConverter.toEulerAngles(basis);

        PortalCamera portalCamera = new PortalCamera(Minecraft.getInstance().level, camera.getEntity(),
                newCameraPos, angles.getPitch(), angles.getYaw(), angles.getRoll(), partialTicks);

        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(angles.getRoll()));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(angles.getPitch()));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(angles.getYaw() + 180));

        return portalCamera;
    }

    private static void blitFBOtoFBO(Framebuffer src, Framebuffer dest) {
        ShaderInit.ACTUAL_BLIT.get().bind()
                .setMatrix("projection", Matrix4f.createScaleMatrix(1, 1, 1))
                .setInt("texture", 0);

        RenderSystem.activeTexture(GL_TEXTURE0);
        src.bindRead();
        dest.bindWrite(false);
        blitQuad.render();

        ShaderInit.ACTUAL_BLIT.get().unbind();
    }

    private static void setupSkyColor(ActiveRenderInfo camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null) {
            FogRenderer.setupColor(camera, partialTicks, mc.level, mc.options.renderDistance,
                    mc.gameRenderer.getDarkenWorldAmount(partialTicks));
        }
    }

    private static void renderPortal(PortalEntity portal, ActiveRenderInfo camera, ClippingHelper clippingHelper, float partialTicks, boolean fabulousGraphics) {
        if(discardPortal(portal, camera, clippingHelper))
            return;

        recursion++;
        StencilUtil.INSTANCE.enable();

        glEnable(GL43.GL_DEBUG_OUTPUT);
        glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, "Rendering portal, recursion: " + recursion);

        Minecraft mc = Minecraft.getInstance();
        Framebuffer mainRenderTarget = mc.getMainRenderTarget();
        Matrix4f modelView = getModelViewMatrix(portal, camera, PortalEntityRenderer.OFFSET);
        Optional<PortalEntity> otherPortalOptional = portal.getOtherPortal();

        MatrixStack matrixStack = new MatrixStack();
        ActiveRenderInfo portalCamera = setupCameraAndMatrix(camera, matrixStack, portal, partialTicks);
        setupSkyColor(portalCamera, partialTicks);

        if(fabulousGraphics)
            portalTempFBO.bindWrite(false);

        StencilUtil.INSTANCE.write(GL_EQUAL, recursion - 1, GL_KEEP, GL_KEEP, GL_INCR);
        renderMask(modelView);

        StencilUtil.INSTANCE.read(GL_EQUAL, recursion);
        renderBackground();

        if(otherPortalOptional.isPresent() && recursion <= PortalModOptionsScreen.RECURSION.get()) {
            PortalEntity otherPortal = otherPortalOptional.get();
            portalStack.push(otherPortal);

            clipMatrix.pushPose();
            setupClipPlane(0, clipMatrix, portal, camera);

            if(!fabulousGraphics)
                StencilUtil.INSTANCE.enable().read(GL_EQUAL, recursion);

            mc.levelRenderer.renderLevel(matrixStack, partialTicks, Util.getNanos(), false, portalCamera,
                    mc.gameRenderer, mc.gameRenderer.lightTexture, getProjectionMatrix());

            clipMatrix.popPose();
            portalStack.pop();
        }

        Matrix4f modelViewProjection = getModelViewProjectionMatrix(portal, camera, PortalEntityRenderer.OFFSET);

        if(fabulousGraphics) {
            StencilUtil.INSTANCE.enable().read(GL_EQUAL, recursion);
            blitFBOtoFBO(mainRenderTarget, portalTempFBO);
        }

        StencilUtil.INSTANCE.read(GL_EQUAL, recursion);
        renderDepth(modelViewProjection);

        StencilUtil.INSTANCE.read(GL_EQUAL, recursion);
        renderBorder(portal, partialTicks, modelViewProjection);

        StencilUtil.INSTANCE.write(GL_EQUAL, recursion, GL_KEEP, GL_KEEP, GL_DECR);
        renderDepth(modelViewProjection);

        StencilUtil.INSTANCE.read(GL_EQUAL, recursion - 1);

        if(recursion > 1) {
            setClipPlane(0, clipMatrix.last().pose(), new Vec3(0, 0, -1));
        } else {
            disableClipPlane(0);
            setupSkyColor(camera, partialTicks);
        }

        if(fabulousGraphics)
            mainRenderTarget.bindWrite(false);

        GL43.glPopDebugGroup();

        actuallyRenderedPortals++;
        recursion--;
    }

    private static MatrixStack clipMatrix = new MatrixStack();
    private static PortalEntity currentPortal;

    public static void renderHighlights(ActiveRenderInfo camera) {
        GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, "Highlights");

        ClientWorld level = Minecraft.getInstance().level;
        ClientPlayerEntity player = Minecraft.getInstance().player;

        if(level == null || player == null)
            return;

        // todo no
        int fb = GL11.glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        if(fabulousGraphics)
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);

        StencilUtil.INSTANCE.enable();
        StencilUtil.INSTANCE.mask(0x80);
        GL11.glClear(GL_STENCIL_BUFFER_BIT);

        StencilUtil.INSTANCE.write();
        StencilUtil.INSTANCE.mask(0x80);
        StencilUtil.INSTANCE.func(GL_ALWAYS, 0x80, 0x80);
        StencilUtil.INSTANCE.op(GL_KEEP, GL_KEEP, GL_REPLACE);

        for(Entity entity : level.entitiesForRendering()) {
            if(!(entity instanceof PortalEntity))
                continue;

            PortalEntity portal = (PortalEntity)entity;
            Matrix4f modelView = getModelViewMatrix(portal, camera, PortalEntityRenderer.OFFSET * 100);

            renderMask(modelView);
            GL11.glEnable(GL_ALPHA_TEST);
        }

        StencilUtil.INSTANCE.read();
        if(fabulousGraphics) {
            StencilUtil.INSTANCE.func(GL_NOTEQUAL, 0x80, 0x80);
            StencilUtil.INSTANCE.op(GL_KEEP, GL_KEEP, GL_KEEP);
        } else {
            StencilUtil.INSTANCE.func(GL_EQUAL, recursion, 0xFF);
            StencilUtil.INSTANCE.op(GL_KEEP, GL_KEEP, GL_KEEP);
        }

        for(Entity entity : level.entitiesForRendering()) {
            if(!(entity instanceof PortalEntity))
                continue;

            PortalEntity portal = (PortalEntity)entity;
            Vector3d cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            ItemStack item = player.getMainHandItem();
            Optional<UUID> gunUUID = PortalGun.getUUID(item);
            if (!(item.getItem() instanceof PortalGun) || !gunUUID.isPresent() || !gunUUID.get().equals(portal.getGunUUID()))
                continue;

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL_GREATER);
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();

                ShaderInit.PORTAL_HIGHLIGHT.get().bind()
                    .setMatrix("modelViewProjection", getModelViewProjectionMatrix(portal, camera, PortalEntityRenderer.OFFSET * 100));

                RenderUtil.bindTexture(ShaderInit.PORTAL_HIGHLIGHT.get(), "texture",
                        "textures/portal/highlight_" + portal.getColor() + ".png", 0);
                ShaderInit.PORTAL_HIGHLIGHT.get().setFloat("intensity", (float)portal.position().distanceTo(cameraPos));

                portalMesh.render();

            RenderSystem.enableCull();
            RenderSystem.depthFunc(GL_LEQUAL);
            RenderSystem.depthMask(true);
            RenderSystem.bindTexture(0);
            RenderSystem.disableBlend();
            unbindBuffer();
            ShaderInit.PORTAL_HIGHLIGHT.get().unbind();
        }

        StencilUtil.INSTANCE.read(GL_EQUAL, recursion);
        GL30.glBindFramebuffer(GL_FRAMEBUFFER, fb);

        if(fabulousGraphics)
            StencilUtil.INSTANCE.disable();

        GL43.glPopDebugGroup();
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
////        if(true)
////            return;
//        if(entity instanceof PlayerEntity)
//            return;
//
//        double d0 = MathHelper.lerp((double)partialTicks, entity.xOld, entity.getX()) - x;
//        double d1 = MathHelper.lerp((double)partialTicks, entity.yOld, entity.getY()) - y;
//        double d2 = MathHelper.lerp((double)partialTicks, entity.zOld, entity.getZ()) - z;
//        float f = MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot);
//
//        Vector3d curPos = new Vector3d(
//                MathHelper.lerp(partialTicks, entity.xOld, entity.getX()),
//                MathHelper.lerp(partialTicks, entity.yOld, entity.getY()),
//                MathHelper.lerp(partialTicks, entity.zOld, entity.getZ())
//        );
//
//        if(entity instanceof PortalEntity)
//            return;
//
////        List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(.2));
//        List<PortalEntity> entities = PortalEntity.getOpenPortals(entity.level, entity.getBoundingBox().inflate(.2), portal -> true);
//        for(Entity e : entities) {
//            if(e instanceof PortalEntity) {
//                PortalEntity portal = (PortalEntity)e;
//                if(!portal.isOpen())
//                    continue;
//
////                Vector3d offset = portal.getRenderOffset();
////                if(offset == Vector3d.ZERO)
////                    continue;
//
////                Vector3d offset = portal.teleportPoint(new Vec3(entity.getPosition(partialTicks))).to3d()
////                        .subtract(portal.getCenter());
//
//                Vector3d centerOffset = entity.getBoundingBox().getCenter().subtract(entity.position());
//                Vector3d entityCenter = entity.getPosition(partialTicks).add(centerOffset);
//
////                Vector3d offset = portal
////                        .teleportPoint(new Vec3(entityCenter))
////                        .to3d()
////                        .subtract(entityCenter);
//;
//                Vector3d offset = portal
//                        .teleportPoint(new Vec3(curPos))
//                        .to3d()
//                        .subtract(curPos);
//
//                PortalEntity otherPortal = portal.getOtherPortal().get();
////                Vector3d otherPortalPivot = portal.getPivotPoint().add(offset);
//                Vector3d otherPortalPivot = portal.getOtherPortal().get().getPivotPoint();
//                ActiveRenderInfo camera = NewWorldRenderer.getCurrentCamera();
//
////                Vector3f forwards = otherPortal.getDirection().step();
////                Vector3f up = otherPortal.getUpVector().step();
////                Vector3f right = up.copy();
////                right.cross(forwards);
////                Vector3f down = up.copy();
////                down.mul(-1);
////                Vector3f left = right.copy();
////                left.mul(-1);
//
////                if(shouldNotRenderEntity(entity, x - offset.x, y - offset.y, z - offset.z))
////                    continue;
//
//
//
//                Vec3 forwards = new Vec3(otherPortal.getDirection().getNormal());
//                Vec3 up = new Vec3(otherPortal.getUpVector().getNormal());
//                Vec3 right = up.clone().cross(forwards);
//                Vec3 down = up.clone().negate();
//                Vec3 left = right.clone().negate();
//
////                RenderUtil.setClipPlane(0, camera, portal.getCenter().add(offset),
////                        new Vector3d(otherPortal.getDirection().step()));
//
//                RenderUtil.setClipPlane(0, camera, portal.getOtherPortal().get().getCenter(),
//                        new Vec3(otherPortal.getDirection().getNormal()).to3d());
//
//                RenderUtil.setClipPlane(1, camera, otherPortalPivot.add(right.clone().mul(.5).to3d()), left.to3d());
//                RenderUtil.setClipPlane(2, camera, otherPortalPivot.add(left.clone().mul(.5).to3d()), right.to3d());
//                RenderUtil.setClipPlane(3, camera, otherPortalPivot.add(up.clone().mul(.5).add(up).to3d()), down.to3d());
//                RenderUtil.setClipPlane(4, camera, otherPortalPivot.add(down.clone().mul(.5).to3d()), up.to3d());
//
////                Vector3d vector3d = erm.getRenderer(entity).getRenderOffset(entity, partialTicks);
////                double d3 = d0 + vector3d.x();
////                double d4 = d1 + vector3d.y();
////                double d5 = d2 + vector3d.z();
//
//                // todo actually transform
//
//                matrixStack.pushPose();
//                matrixStack.translate(offset.x, offset.y, offset.z);
////                matrixStack.translate(d3, d4, d5);
////                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180));
////                matrixStack.translate(-d3, -d4, -d5);
//                erm.render(entity, d0, d1, d2, f,
//                        partialTicks, matrixStack, renderTypeBuffer,
//                        erm.getPackedLightCoords(entity, partialTicks));
//                matrixStack.popPose();
//
//                RenderUtil.disableClipPlane(0);
//                RenderUtil.disableClipPlane(1);
//                RenderUtil.disableClipPlane(2);
//                RenderUtil.disableClipPlane(3);
//                RenderUtil.disableClipPlane(4);
//            }
//        }
    }
    
    private static Matrix4f getModelViewProjectionMatrix(PortalEntity portal, @Nullable ActiveRenderInfo camera, float offset) {
        Matrix4f projection = getProjectionMatrix();
        projection.multiply(getModelViewMatrix(portal, camera, offset));
        return projection;
    }
    
    private static Matrix4f getModelViewMatrix(PortalEntity portal, @Nullable ActiveRenderInfo camera, float offset) {
        if(camera == null)
            camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        
        Vector3d cameraPos = camera.getPosition();
        Vector3i portalNormal = portal.getDirection().getNormal();
        MatrixStack matrix = new MatrixStack();

        if(camera instanceof PortalCamera)
            matrix.mulPose(Vector3f.ZP.rotationDegrees(((PortalCamera)camera).getRoll()));
        matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        matrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        matrix.translate(portalNormal.getX() * offset, portalNormal.getY() * offset, portalNormal.getZ() * offset);
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(matrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        matrix.scale(1, 2, 1);
        return matrix.last().pose();
    }
    
    private static Matrix4f getModelViewProjectionMatrix(PortalEntity portal, @Nullable PortalCamera camera, float offset) {
        if(camera == null)
            return getModelViewProjectionMatrix(portal, (ActiveRenderInfo)null, offset);
        
        Matrix4f projection = getProjectionMatrix();
        projection.multiply(getModelViewMatrix(portal, camera, offset));
        return projection;
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
}