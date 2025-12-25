package net.portalmod.common.sorted.portal;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.portalmod.PMGlobals;
import net.portalmod.PMState;
import net.portalmod.PortalMod;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.client.screens.PortalModOptionsScreen;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.core.init.ShaderInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.RenderUtil;
import net.portalmod.core.util.VertexRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;

import java.awt.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

public class PortalRenderer {
    private static PortalRenderer instance;

    private static final VertexRenderer portalMesh = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);
    private static final VertexRenderer screenQuad = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);
    private static final VertexRenderer blitQuad = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);

    private static final Framebuffer tempFBO = new Framebuffer(
            Minecraft.getInstance().getWindow().getWidth(),
            Minecraft.getInstance().getWindow().getHeight(),
            true,
            Minecraft.ON_OSX
    );

    // todo create a state class and dump everything in idk
    public int recursion = 0;
    public ActiveRenderInfo currentCamera;
    public PortalCamera currentUnteleportedCamera = null;
    public int renderedPortals = 0;
    public boolean currentlyRenderingPortals = false;
    private boolean fabulousGraphics = false;
    private final Deque<PortalEntity> portalStack = new ArrayDeque<>();
    public MatrixStack clipMatrix = new MatrixStack();
    private final float[] projectionBuffer = new float[16];
    public Vec3 clearColor = new Vec3(0);

    static {
        portalMesh.reset();
        portalMesh.data(bufferBuilder -> {
            bufferBuilder.vertex(0, 0, 0).uv(0, 0).endVertex();
            bufferBuilder.vertex(1, 0, 0).uv(1, 0).endVertex();
            bufferBuilder.vertex(1, 2, 0).uv(1, 1).endVertex();
            bufferBuilder.vertex(0, 2, 0).uv(0, 1).endVertex();
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

    private PortalRenderer() { }

    public static PortalRenderer getInstance() {
        if(instance == null)
            instance = new PortalRenderer();
        return instance;
    }

    private void renderMask(PortalEntity portal, Matrix4f modelView) {
        glUseProgram(0);

        int age = portal.getAge();
        boolean spawning = age < 4;

        String path = "textures/portal/"
                + "mask"
                + (spawning ? "_spawning" + age : "")
                + ".png";

        glEnable(GL_TEXTURE_2D);
        RenderSystem.activeTexture(GL_TEXTURE0);
        Minecraft.getInstance().textureManager.bind(new ResourceLocation(PortalMod.MODID, path));

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

    private void renderBackground() {
        ShaderInit.COLOR.get().bind().setFloat("color", (float)clearColor.x, (float)clearColor.y, (float)clearColor.z, 1);

        RenderSystem.depthFunc(GL_ALWAYS);
        screenQuad.render();
        RenderSystem.depthFunc(GL_LESS);

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.COLOR.get().unbind();
    }

    private void renderDepth(Matrix4f modelViewProjection) {
        glUseProgram(0);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        GL11.glEnable(GL_ALPHA_TEST);
        glEnable(GL_DEPTH_CLAMP);
        RenderSystem.depthFunc(GL_ALWAYS);
        RenderSystem.colorMask(false, false, false, false);
        portalMesh.render(new Mat4(modelViewProjection));
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL_LESS);
        glDisable(GL_DEPTH_CLAMP);
        GL11.glDisable(GL_ALPHA_TEST);

        RenderSystem.bindTexture(0);
        unbindBuffer();
    }

    private void renderBorder(PortalEntity portal, float partialTicks, Matrix4f modelViewProjection) {
        Minecraft mc = Minecraft.getInstance();

        if(mc.player == null)
            return;

        int ticks = mc.player.tickCount;
        int age = portal.getAge();
        boolean open = portal.isOpen() && recursion <= PortalModOptionsScreen.RECURSION.get();
        boolean spawning = age < 4;

        String path = "textures/portal/"
                + (open ? "open_" : "closed_")
                + portal.getColor()
                + (spawning ? "_spawning" : "")
                + ".png";
        ResourceLocation location = new ResourceLocation(PortalMod.MODID, path);

        Optional<Dimension> optionalTextureSize = PortalAnimatedTextureHelper.getTextureSize(location);
        if(!optionalTextureSize.isPresent())
            return;

        Dimension textureSize = optionalTextureSize.get();
        int frameCount = (int)textureSize.getHeight() / (2 * (int)textureSize.getWidth());

        float frameIndex;
        if(spawning) {
            frameIndex = age % frameCount;
        } else {
            int frameTime = 5;
            frameIndex = ((int)(ticks / 5) % frameCount) + ((ticks % frameTime) + partialTicks) / frameTime;
        }

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

    public static Mat4 getPortalToPortalRotationMatrix(PortalEntity portal, PortalEntity otherPortal) {
        OrthonormalBasis srcBasis = portal.getSourceBasis();
        OrthonormalBasis dstBasis = otherPortal.getDestinationBasis();
        return srcBasis.getChangeOfBasisMatrix(dstBasis);
    }

    public static Mat4 getPortalToPortalRotationMatrix(PortalEntity portal, PartialPortal otherPortal) {
        OrthonormalBasis srcBasis = portal.getSourceBasis();
        OrthonormalBasis dstBasis = otherPortal.getDestinationBasis();
        return srcBasis.getChangeOfBasisMatrix(dstBasis);
    }

    public static Mat4 getPortalToPortalMatrix(PortalEntity portal, PortalEntity otherPortal) {
        Vec3 thisPos = new Vec3(portal.position());
        Vec3 otherPos = new Vec3(otherPortal.position());

        return Mat4.identity()
                .mul(Mat4.createTranslation(otherPos.x, otherPos.y, otherPos.z))
                .mul(getPortalToPortalRotationMatrix(portal, otherPortal))
                .mul(Mat4.createTranslation(-thisPos.x, -thisPos.y, -thisPos.z));
    }

    public static Mat4 getPortalToPortalMatrix(PortalEntity portal, PartialPortal otherPortal) {
        Vec3 thisPos = new Vec3(portal.position());
        Vec3 otherPos = new Vec3(otherPortal.getPosition());

        return Mat4.identity()
                .mul(Mat4.createTranslation(otherPos.x, otherPos.y, otherPos.z))
                .mul(getPortalToPortalRotationMatrix(portal, otherPortal))
                .mul(Mat4.createTranslation(-thisPos.x, -thisPos.y, -thisPos.z));
    }

    public void renderPortals(ClientWorld level, ActiveRenderInfo camera, ClippingHelper clippingHelper, Matrix4f projectionMatrix, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Framebuffer mainFBO = mc.getMainRenderTarget();
        mc.levelRenderer.renderBuffers.bufferSource().endBatch();

        ObjectList<WorldRenderer.LocalRenderInformationContainer> renderChunks = new ObjectArrayList<>();

        if(recursion == 0) {
            currentlyRenderingPortals = true;
            fabulousGraphics = mc.options.graphicsMode == GraphicsFanciness.FABULOUS;

            renderChunks.addAll(mc.levelRenderer.renderChunks);

            if(fabulousGraphics) {
                int w = mc.getWindow().getWidth();
                int h = mc.getWindow().getHeight();

                if(tempFBO.width != w || tempFBO.height != h)
                    tempFBO.resize(w, h, Minecraft.ON_OSX);

                tempFBO.enableStencil();
                tempFBO.bindWrite(false);
                GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            }

            mainFBO.bindWrite(false);
            GL11.glEnable(GL_STENCIL_TEST);
            RenderSystem.stencilMask(0x80);
            RenderSystem.clear(GL_STENCIL_BUFFER_BIT, false);
        }

        renderedPortals = 0;

        for(Entity entity : level.entitiesForRendering()) {
            if(entity instanceof PortalEntity) {
                renderPortal((PortalEntity)entity, camera, clippingHelper, projectionMatrix, partialTicks, fabulousGraphics);
                renderedPortals++;
            }
        }

        if(recursion == 0) {
            currentlyRenderingPortals = false;

            mc.levelRenderer.renderChunks.clear();
            mc.levelRenderer.renderChunks.addAll(renderChunks);
        }

        if(fabulousGraphics) {
            blitFBOtoFBO(mainFBO, tempFBO);
            tempFBO.copyDepthFrom(mainFBO);
            mainFBO.bindWrite(false);
        }

        if(PortalModOptionsScreen.HIGHLIGHTS.get())
            renderHighlights(camera, projectionMatrix);

        GL11.glEnable(GL_ALPHA_TEST);
    }

    private boolean discardPortal(PortalEntity portal, ActiveRenderInfo camera, ClippingHelper clippingHelper) {
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

    private ActiveRenderInfo setupCamera(ActiveRenderInfo camera, PortalEntity portal, float partialTicks) {
        if(!portal.getOtherPortal().isPresent())
            return camera;

        Mat4 portalToPortalRotationMatrix = getPortalToPortalRotationMatrix(portal, portal.getOtherPortal().get());
        Mat4 portalToPortalMatrix = getPortalToPortalMatrix(portal, portal.getOtherPortal().get());
        Vec3 newCameraPos = new Vec3(camera.getPosition()).transform(portalToPortalMatrix);

        float xRot = camera.getXRot();
        float yRot = camera.getYRot();
        float zRot = camera instanceof PortalCamera ? ((PortalCamera)camera).getRoll() : PMState.cameraRoll;
        OrthonormalBasis basis = EulerConverter.toVectors(xRot, yRot, zRot);
        basis.transform(portalToPortalRotationMatrix);
        EulerConverter.EulerAngles angles = EulerConverter.toEulerAnglesLeastRoll(basis);

        return new PortalCamera(Minecraft.getInstance().level, camera.getEntity(),
                newCameraPos, angles.getPitch(), angles.getYaw(), angles.getRoll(), partialTicks);
    }

    private void setupMatrixStack(MatrixStack matrixStack, ActiveRenderInfo camera) {
        if(camera instanceof PortalCamera) {
            PortalCamera portalCamera = (PortalCamera)camera;
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(portalCamera.getRoll()));
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(portalCamera.getXRot()));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(portalCamera.getYRot() + 180));
        }
    }

    private void blitFBOtoFBO(Framebuffer src, Framebuffer dest) {
        ShaderInit.ACTUAL_BLIT.get().bind()
                .setMatrix("projection", Matrix4f.createScaleMatrix(1, 1, 1))
                .setInt("texture", 0);

        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.activeTexture(GL_TEXTURE0);
        src.bindRead();
        dest.bindWrite(false);
        blitQuad.render();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        ShaderInit.ACTUAL_BLIT.get().unbind();
    }

    private void setupSkyAndFog(ActiveRenderInfo camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null) {
            FogRenderer.setupColor(camera, partialTicks, mc.level, mc.options.renderDistance,
                    mc.gameRenderer.getDarkenWorldAmount(partialTicks));
            float renderDistance = mc.gameRenderer.getRenderDistance();
            boolean hasFog = mc.level.effects().isFoggyAt(MathHelper.floor(camera.getBlockPosition().getX()), MathHelper.floor(camera.getBlockPosition().getY()))
                    || mc.gui.getBossOverlay().shouldCreateWorldFog();
            if(Minecraft.getInstance().options.renderDistance >= 4)
                FogRenderer.setupFog(camera, FogRenderer.FogType.FOG_SKY, renderDistance, hasFog, partialTicks);
            FogRenderer.setupFog(camera, FogRenderer.FogType.FOG_TERRAIN,
                    Math.max(renderDistance - 16, 32), hasFog, partialTicks);
        }
    }

    private void renderPortal(PortalEntity portal, ActiveRenderInfo camera, ClippingHelper clippingHelper, Matrix4f projectionMatrix, float partialTicks, boolean fabulousGraphics) {
        recursion++;

        if(PMGlobals.DEBUG) {
            glEnable(GL43.GL_DEBUG_OUTPUT);
            glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, "Rendering portal, recursion: " + recursion);
        }

        Minecraft mc = Minecraft.getInstance();
        Framebuffer mainFBO = mc.getMainRenderTarget();
        Matrix4f modelView = getModelViewMatrix(portal, camera, portal.getWallAttachmentDistance(camera));

        if(!discardPortal(portal, camera, clippingHelper)) {
            GL11.glEnable(GL_STENCIL_TEST);
            Optional<PortalEntity> otherPortalOptional = portal.getOtherPortal();

            MatrixStack matrixStack = new MatrixStack();
            ActiveRenderInfo portalCamera = setupCamera(camera, portal, partialTicks);
            setupMatrixStack(matrixStack, portalCamera);
            setupSkyAndFog(portalCamera, partialTicks);

            RenderSystem.stencilMask(0x7F);
            RenderSystem.stencilFunc(GL_EQUAL, recursion - 1, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_INCR);
            renderMask(portal, modelView);

            RenderSystem.stencilMask(0);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            renderBackground();

            if(otherPortalOptional.isPresent() && !isDeepest()) {
                PortalEntity otherPortal = otherPortalOptional.get();
                portalStack.push(otherPortal);

                clipMatrix.pushPose();
                RenderUtil.setupClipPlane(clipMatrix, portal, camera, portal.getWallAttachmentDistance(camera), false);

                currentCamera = portalCamera;

                PortalCamera prevUnteleportedCamera = currentUnteleportedCamera;
                currentUnteleportedCamera = prevUnteleportedCamera == null ? null
                        : new PortalCamera(setupCamera(prevUnteleportedCamera, portal, partialTicks), partialTicks);

                Vec3 oldCameraPosOverrideForRenderingSelf = PMState.cameraPosOverrideForRenderingSelf;
                PMState.cameraPosOverrideForRenderingSelf = PMState.cameraPosOverrideForRenderingSelf == null ? null
                        : PMState.cameraPosOverrideForRenderingSelf.clone().transform(getPortalToPortalMatrix(portal, otherPortal));

                mc.levelRenderer.renderLevel(matrixStack, partialTicks, Util.getNanos(), false, portalCamera,
                        mc.gameRenderer, mc.gameRenderer.lightTexture, projectionMatrix);

                TileEntityRendererDispatcher.instance.prepare(portal.level, mc.getTextureManager(), mc.font, camera, mc.hitResult);
                mc.levelRenderer.entityRenderDispatcher.prepare(portal.level, camera, mc.crosshairPickEntity);

                currentCamera = camera;
                currentUnteleportedCamera = prevUnteleportedCamera;
                PMState.cameraPosOverrideForRenderingSelf = oldCameraPosOverrideForRenderingSelf;

                clipMatrix.popPose();
                portalStack.pop();

                if(fabulousGraphics) {
                    GL11.glEnable(GL_STENCIL_TEST);
                    RenderSystem.stencilMask(0);
                    RenderSystem.stencilFunc(GL_NOTEQUAL, recursion, 0x7F);
                    RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
                    blitFBOtoFBO(tempFBO, mainFBO);
                    mainFBO.copyDepthFrom(tempFBO);
                    mainFBO.bindWrite(false);
                }
            }

            Matrix4f modelViewProjection = projectionMatrix.copy();
            modelViewProjection.multiply(getModelViewMatrix(portal, camera, portal.getWallAttachmentDistance(camera) * 2));

            glDisable(GL_CLIP_PLANE0);
            GL11.glEnable(GL_STENCIL_TEST);
            RenderSystem.color4f(1, 1, 1, 1);

            RenderSystem.stencilMask(0x80);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0xFF);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_INVERT);
            renderMask(portal, modelView);

            RenderSystem.stencilMask(0);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            renderBorder(portal, partialTicks, modelViewProjection);
            renderDepth(modelView);

            RenderSystem.stencilMask(0x7F);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_DECR);
            renderDepth(modelView);

            if(!fabulousGraphics) {
                RenderSystem.stencilMask(0);
                RenderSystem.stencilFunc(GL_EQUAL, recursion - 1, 0x7F);
                RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            }
        }

        if(fabulousGraphics)
            GL11.glDisable(GL_STENCIL_TEST);

        if(!isShallowest()) {
            RenderUtil.setStandardClipPlane(clipMatrix.last().pose());
        } else {
            glDisable(GL_CLIP_PLANE0);
        }

        setupSkyAndFog(camera, partialTicks);

        if(PMGlobals.DEBUG)
            GL43.glPopDebugGroup();

        recursion--;
    }

    private boolean isDeepest() {
        return recursion > PortalModOptionsScreen.RECURSION.get();
    }

    private boolean isShallowest() {
        return recursion <= 1;
    }

    public void renderHighlights(ActiveRenderInfo camera, Matrix4f projectionMatrix) {
        if(PMGlobals.DEBUG)
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, "Highlights");

        ClientWorld level = Minecraft.getInstance().level;
        ClientPlayerEntity player = Minecraft.getInstance().player;

        if(level == null || player == null)
            return;

        GL11.glEnable(GL_STENCIL_TEST);
        RenderSystem.stencilMask(0);
        RenderSystem.stencilFunc(GL_EQUAL, recursion, 0xFF);
        RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL_GREATER);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        for(Entity entity : level.entitiesForRendering()) {
            if(!(entity instanceof PortalEntity))
                continue;

            PortalEntity portal = (PortalEntity)entity;
            Vector3d cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            if(currentCamera != null)
                cameraPos = currentCamera.getPosition();

            ItemStack item = player.getMainHandItem();
            Optional<UUID> gunUUID = PortalGun.getUUID(item);
            if (!(item.getItem() instanceof PortalGun) || !gunUUID.isPresent() || !gunUUID.get().equals(portal.getGunUUID()))
                continue;

            Matrix4f modelViewProjection = projectionMatrix.copy();
            modelViewProjection.multiply(getModelViewMatrix(portal, camera, portal.getWallAttachmentDistance(camera) * 3));

            ShaderInit.PORTAL_HIGHLIGHT.get().bind()
                    .setMatrix("modelViewProjection", modelViewProjection)
                    .setFloat("intensity", (float)portal.position().distanceTo(cameraPos));

            RenderUtil.bindTexture(ShaderInit.PORTAL_HIGHLIGHT.get(), "texture",
                    "textures/portal/highlight_" + portal.getColor() + ".png", 0);

            portalMesh.render();
        }

        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL_LESS);
        RenderSystem.depthMask(true);
        RenderSystem.bindTexture(0);
        RenderSystem.disableBlend();
        unbindBuffer();
        ShaderInit.PORTAL_HIGHLIGHT.get().unbind();

        if(fabulousGraphics) {
            GL11.glDisable(GL_STENCIL_TEST);
        } else {
            RenderSystem.stencilMask(0);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        }

        if(PMGlobals.DEBUG)
            GL43.glPopDebugGroup();
    }
    
    private void unbindBuffer() {
        DefaultVertexFormats.POSITION_TEX.clearBufferState();
        VertexBuffer.unbind();
    }

    private Matrix4f getModelViewMatrix(PortalEntity portal, ActiveRenderInfo camera, float offset) {
        Vector3d cameraPos = camera.getPosition();
        Vector3i portalNormal = portal.getDirection().getNormal();
        MatrixStack matrix = new MatrixStack();
        Vec3 offsetNormal = new Vec3(camera.getPosition()).sub(portal.position()).normalize().mul(offset);
        float roll = camera instanceof PortalCamera ? ((PortalCamera)camera).getRoll() : PMState.cameraRoll;

        matrix.mulPose(Vector3f.ZP.rotationDegrees(roll));
        matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        matrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        matrix.translate(offsetNormal.x, offsetNormal.y, offsetNormal.z);
        matrix.translate(portalNormal.getX() * 0.0001f, portalNormal.getY() * 0.0001f, portalNormal.getZ() * 0.0001f);
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        PortalEntity.setupMatrix(matrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        return matrix.last().pose();
    }

    public ActiveRenderInfo getCurrentCamera() {
        if(this.currentCamera == null)
            return Minecraft.getInstance().gameRenderer.getMainCamera();
        return this.currentCamera;
    }
}