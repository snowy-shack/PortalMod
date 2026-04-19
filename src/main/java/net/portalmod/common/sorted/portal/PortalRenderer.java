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
import net.portalmod.PMState;
import net.portalmod.PortalMod;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.client.render.Shader;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.trigger.TriggerTER;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.ShaderInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.RenderUtil;
import net.portalmod.core.util.VertexRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

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
    public int renderedPortals = 0;
    public boolean currentlyRenderingPortals = false;
    private boolean fabulousGraphics = false;
    private final Deque<PortalEntity> portalStack = new ArrayDeque<>();
    public MatrixStack clipMatrix = new MatrixStack();
    private final float[] projectionBuffer = new float[16];
    public Vec3 clearColor = new Vec3(0);

    public List<PortalEntity> outlineRenderingPortalChain;
    public final Deque<PortalEntity> portalChain = new ArrayDeque<>();

    /**
     * While &gt; 0 we are inside a nested portal render in which we temporarily lowered
     * {@code LevelRenderer.lastViewDistance} to reduce the chunk-BFS grid bounds in
     * {@code setupRender}. The mixin in {@link net.portalmod.mixins.renderer.LevelRendererMixin}
     * uses this value to mask the {@code options.renderDistance != lastViewDistance}
     * guard so that the intentional mismatch does not trigger {@code allChanged()} and
     * rebuild every chunk.
     *
     * <p>IMPORTANT: {@code options.renderDistance} is never mutated by this path, so fog,
     * F3, entity view scale, the options GUI, etc. always see the player's real setting.
     */
    public int nestedBfsDistanceOverride = -1;

    /**
     * Screen-space (NDC) bounding rect of the portal we are currently rendering
     * <em>through</em>, stored as {@code {xMin, yMin, xMax, yMax}} in NDC coords
     * ({@code [-1,1]}). At the outer frame it is the full screen; each time we
     * recurse into a portal, we intersect this rect with the portal's own projected
     * silhouette and push it, then restore on exit.
     *
     * <p>This mirrors Source's portal-edge frustum clipping: even if the child
     * portal is inside the camera's full frustum, it gets culled when it projects
     * outside the screen-space region already clipped by the parent portal's
     * silhouette (equivalently: outside the stencil-mask pixels it could draw into).
     * Cheap 2D rect-vs-rect, but kills the exponential fan-out when multiple portal
     * pairs are visible.
     */
    private final float[] currentParentNdcRect = new float[]{-1f, -1f, 1f, 1f};
    private final Deque<float[]> parentNdcRectStack = new ArrayDeque<>();
    public int portalsCulledByNdcRect = 0;
    /** Per outer frame: skipped full stencil/mask/border — all four opening-corner rays hit opaque blocks before the corner. */
    public int portalsStencilSkippedOccludedRays = 0;

    private static final double RAY_CORNER_EPSILON = 0.08;

    // --- profiler ---
    public static final Profile PROFILE = new Profile();

    /**
     * Lightweight per-frame profiler for the portal renderer. Use {@link #push}/{@link #pop}
     * to measure nested regions. Totals are snapshotted each frame into
     * {@code ClientEvents.debugStrings} and drawn on the HUD.
     *
     * <p>Each label accumulates total nanoseconds, total invocations, and max single-call
     * nanoseconds. Enable with JVM flag {@code -Dportalmod.portalProfiler=true}. Off by default.
     */
    public static final class Profile {
        public boolean enabled = false;
        public int topN = 12;

        private static final class Entry {
            long totalNs;
            long maxNs;
            long calls;
        }

        private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();
        private final Deque<String> labelStack = new ArrayDeque<>();
        private final Deque<Long> startStack = new ArrayDeque<>();

        public int portalsVisited;
        public int portalsCulled;
        public int portalsNested;
        public int maxRecursionReached;

        public void push(String label) {
            if(!enabled) return;
            labelStack.push(label);
            startStack.push(System.nanoTime());
        }

        public void pop() {
            if(!enabled) return;
            if(labelStack.isEmpty() || startStack.isEmpty()) return;
            long elapsed = System.nanoTime() - startStack.pop();
            String label = labelStack.pop();
            Entry e = entries.get(label);
            if(e == null) {
                e = new Entry();
                entries.put(label, e);
            }
            e.totalNs += elapsed;
            e.calls++;
            if(elapsed > e.maxNs)
                e.maxNs = elapsed;
        }

        public void reset() {
            entries.clear();
            labelStack.clear();
            startStack.clear();
            portalsVisited = 0;
            portalsCulled = 0;
            portalsNested = 0;
            maxRecursionReached = 0;
        }

        private static String fmt(long ns) {
            double ms = ns / 1_000_000.0;
            return String.format(Locale.ROOT, "%6.2fms", ms);
        }

        public void snapshotInto(List<String> out) {
            if(!enabled) return;
            Entry frame = entries.get("frame");
            long frameNs = frame == null ? 1 : Math.max(1, frame.totalNs);

            PortalRenderer pr = PortalRenderer.getInstance();
            out.add(String.format(Locale.ROOT,
                    "PortalProfile %s | portals v=%d c=%d(ndc=%d) nested=%d occ=%d depth=%d/max%d",
                    fmt(frameNs), portalsVisited, portalsCulled, pr.portalsCulledByNdcRect,
                    portalsNested, pr.portalsStencilSkippedOccludedRays, maxRecursionReached, PortalModConfigManager.RECURSION.get()));

            List<Map.Entry<String, Entry>> sorted = new ArrayList<>(entries.entrySet());
            sorted.sort((a, b) -> Long.compare(b.getValue().totalNs, a.getValue().totalNs));

            int shown = 0;
            for(Map.Entry<String, Entry> kv : sorted) {
                if(shown++ >= topN) break;
                Entry e = kv.getValue();
                double pct = (e.totalNs * 100.0) / frameNs;
                out.add(String.format(Locale.ROOT,
                        "%-28s %s x%-3d max %s %5.1f%%",
                        kv.getKey(), fmt(e.totalNs), e.calls, fmt(e.maxNs), pct));
            }
        }
    }

    /** {@code -Dportalmod.portalProfiler=true} */
    public static boolean portalProfilerEnabled() {
        return Boolean.getBoolean("portalmod.portalProfiler");
    }

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

    private void renderMask(PortalEntity portal, Matrix4f model, Matrix4f view, Matrix4f projectionMatrix) {
        ShaderInit.PORTAL_MASK.get().bind()
                .setMatrix("model", model)
                .setMatrix("view", view)
                .setMatrix("projection", projectionMatrix);

        this.setupShaderClipPlane(ShaderInit.PORTAL_MASK.get(), this.portalStack.peekFirst());

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
        portalMesh.render();
        RenderSystem.depthMask(true);
        glDisable(GL_DEPTH_CLAMP);
        GL11.glDisable(GL_ALPHA_TEST);

        RenderSystem.colorMask(true, true, true, true);

        RenderSystem.bindTexture(0);
        unbindBuffer();
        ShaderInit.PORTAL_MASK.get().unbind();
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

    private void renderDepth(Matrix4f modelView) {
        glUseProgram(0);

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        GL11.glEnable(GL_ALPHA_TEST);
        glEnable(GL_DEPTH_CLAMP);
        RenderSystem.depthFunc(GL_ALWAYS);
        RenderSystem.colorMask(false, false, false, false);
        portalMesh.render(new Mat4(modelView));
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(GL_LESS);
        glDisable(GL_DEPTH_CLAMP);
        GL11.glDisable(GL_ALPHA_TEST);

        RenderSystem.bindTexture(0);
        unbindBuffer();
    }

    private void renderBorder(PortalEntity portal, Matrix4f model, Matrix4f view, Matrix4f projectionMatrix) {
        Minecraft mc = Minecraft.getInstance();

        if(mc.player == null)
            return;

        int ticks = mc.player.tickCount;
        int age = portal.getAge();
        boolean open = portal.isOpen() && recursion <= PortalModConfigManager.RECURSION.get();
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

        int frameIndex;
        if(spawning) {
            frameIndex = age % frameCount;
        } else {
            final int frameTime = 1;
            frameIndex = (ticks / frameTime) % frameCount;
        }

        ShaderInit.PORTAL_FRAME.get().bind()
                .setInt("frameCount", frameCount)
                .setInt("frameIndex", frameIndex)
                .setMatrix("model", model)
                .setMatrix("view", view)
                .setMatrix("projection", projectionMatrix);

        this.setupShaderClipPlane(ShaderInit.PORTAL_FRAME.get(), this.portalStack.peekFirst());

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

    public void renderPortals(ClientWorld level, ActiveRenderInfo camera, ClippingHelper clippingHelper, Matrix4f projectionMatrix, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Framebuffer mainFBO = mc.getMainRenderTarget();
        mc.levelRenderer.renderBuffers.bufferSource().endBatch();

        if(recursion == 0) {
            currentlyRenderingPortals = true;
            fabulousGraphics = mc.options.graphicsMode == GraphicsFanciness.FABULOUS;

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
        }

        if(fabulousGraphics) {
            blitFBOtoFBO(mainFBO, tempFBO);
            tempFBO.copyDepthFrom(mainFBO);
            mainFBO.bindWrite(false);
        }

        if(PortalModConfigManager.HIGHLIGHTS.get())
            renderHighlights(camera, projectionMatrix);

        if(recursion == 0 && !fabulousGraphics) {
            mainFBO.bindWrite(false);
            GL11.glEnable(GL_STENCIL_TEST);
            RenderSystem.stencilMask(0xFF);
            RenderSystem.clear(GL_STENCIL_BUFFER_BIT, false);
        }

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

        Mat4 portalToPortalRotationMatrix = PortalEntity.getPortalToPortalRotationMatrix(portal, portal.getOtherPortal().get());
        Mat4 portalToPortalMatrix = PortalEntity.getPortalToPortalMatrix(portal, portal.getOtherPortal().get());
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
        this.portalChain.addLast(portal);

        if(PortalMod.DEBUG) {
            glEnable(GL43.GL_DEBUG_OUTPUT);
            glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, "Rendering portal, recursion: " + recursion);
        }

        Minecraft mc = Minecraft.getInstance();
        Framebuffer mainFBO = mc.getMainRenderTarget();

        Matrix4f modelMatrix = getModelMatrix(portal, camera, portal.getWallAttachmentDistance(camera));
        Matrix4f modelMatrix2 = getModelMatrix(portal, camera, portal.getWallAttachmentDistance(camera) * 2);
        Matrix4f viewMatrix = getViewMatrix(camera);

        Matrix4f modelView = viewMatrix.copy();
        modelView.multiply(modelMatrix);

        if(!discardPortal(portal, camera, clippingHelper)) {
            GL11.glEnable(GL_STENCIL_TEST);
            Optional<PortalEntity> otherPortalOptional = portal.getOtherPortal();

            MatrixStack matrixStack = new MatrixStack();
            ActiveRenderInfo portalCamera = setupCamera(camera, portal, partialTicks);
            setupMatrixStack(matrixStack, portalCamera);
            setupSkyAndFog(portalCamera, partialTicks);

            ActiveRenderInfo fogCamera = portalCamera;
            if(portal.getOtherPortal().isPresent()) {
                fogCamera = new PortalCamera(portalCamera, partialTicks);
                fogCamera.setPosition(portal.getOtherPortal().get().position());
            }

            FogRenderer.setupColor(fogCamera, partialTicks, mc.level, mc.options.renderDistance,
                    mc.gameRenderer.getDarkenWorldAmount(partialTicks));

            RenderSystem.stencilMask(0x7F);
            RenderSystem.stencilFunc(GL_EQUAL, recursion - 1, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_INCR);
            renderMask(portal, modelMatrix2, viewMatrix, projectionMatrix);

            RenderSystem.stencilMask(0);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            renderBackground();

            if(otherPortalOptional.isPresent() && !isDeepest()) {
                PortalEntity otherPortal = otherPortalOptional.get();
                portalStack.push(otherPortal);

                clipMatrix.pushPose();
                RenderUtil.setupClipPlane(clipMatrix, portal, camera, 0, false);

                currentCamera = portalCamera;

                Vec3 oldCameraPosOverrideForRenderingSelf = PMState.cameraPosOverrideForRenderingSelf;
                PMState.cameraPosOverrideForRenderingSelf = PMState.cameraPosOverrideForRenderingSelf == null ? null
                        : PMState.cameraPosOverrideForRenderingSelf.clone().transform(PortalEntity.getPortalToPortalMatrix(portal, otherPortal));

                ObjectList<WorldRenderer.LocalRenderInformationContainer> renderChunks = new ObjectArrayList<>();
                renderChunks.addAll(mc.levelRenderer.renderChunks);

                boolean renderOutline = this.shouldRenderOutline(portalChain);
                mc.levelRenderer.renderLevel(matrixStack, partialTicks, Util.getNanos(), renderOutline, portalCamera,
                        mc.gameRenderer, mc.gameRenderer.lightTexture, projectionMatrix);

                mc.levelRenderer.needsUpdate = true;
                mc.levelRenderer.renderChunks.clear();
                mc.levelRenderer.renderChunks.addAll(renderChunks);

                TileEntityRendererDispatcher.instance.prepare(portal.level, mc.getTextureManager(), mc.font, camera, mc.hitResult);
                mc.levelRenderer.entityRenderDispatcher.prepare(portal.level, camera, mc.crosshairPickEntity);

                currentCamera = camera;
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

            glDisable(GL_CLIP_PLANE0);
            GL11.glEnable(GL_STENCIL_TEST);
            RenderSystem.color4f(1, 1, 1, 1);

            RenderSystem.stencilMask(0x80);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0xFF);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_INVERT);
            renderMask(portal, modelMatrix, viewMatrix, projectionMatrix);

            RenderSystem.stencilMask(0);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            renderBorder(portal, modelMatrix2, viewMatrix, projectionMatrix);
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

        ActiveRenderInfo fogCamera = camera;
        if(portal.getOtherPortal().isPresent()) {
            fogCamera = new PortalCamera(camera, partialTicks);
            fogCamera.setPosition(portal.getOtherPortal().get().position());
        }

        setupSkyAndFog(fogCamera, partialTicks);

        if(PortalMod.DEBUG)
            GL43.glPopDebugGroup();

        this.portalChain.removeLast();
        recursion--;
    }

    public boolean shouldRenderOutline(@Nullable Deque<PortalEntity> portalChain) {
        if(portalChain == null || this.outlineRenderingPortalChain == null)
            return (portalChain == null || portalChain.isEmpty())
                    && (this.outlineRenderingPortalChain == null || this.outlineRenderingPortalChain.isEmpty());

        if(portalChain.size() != this.outlineRenderingPortalChain.size())
            return false;

        Iterator<PortalEntity> iterator = this.outlineRenderingPortalChain.iterator();
        for(PortalEntity portal : portalChain)
            if(portal != iterator.next())
                return false;
        return true;
    }

    private boolean isDeepest() {
        return recursion > PortalModConfigManager.RECURSION.get();
    }

    private boolean isShallowest() {
        return recursion <= 1;
    }

    public void renderHighlights(ActiveRenderInfo camera, Matrix4f projectionMatrix) {
        if(PortalMod.DEBUG)
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

            Matrix4f model = getModelMatrix(portal, camera, portal.getWallAttachmentDistance(camera) * 3);
            Matrix4f view = getViewMatrix(camera);

            ShaderInit.PORTAL_HIGHLIGHT.get().bind()
                    .setMatrix("model", model)
                    .setMatrix("view", view)
                    .setMatrix("projection", projectionMatrix)
                    .setFloat("intensity", (float)portal.position().distanceTo(cameraPos));

            this.setupShaderClipPlane(ShaderInit.PORTAL_HIGHLIGHT.get(), this.portalStack.peekFirst());

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

        if(PortalMod.DEBUG)
            GL43.glPopDebugGroup();
    }
    
    private void unbindBuffer() {
        DefaultVertexFormats.POSITION_TEX.clearBufferState();
        VertexBuffer.unbind();
    }

    private void setupShaderClipPlane(Shader shader, @Nullable PortalEntity portal) {
        if(portal == null) {
            shader.bind().setInt("clipPlaneEnabled", 0);
            return;
        }

        Vec3 pos = new Vec3(portal.position());
        Vec3 vec = new Vec3(portal.getDirection());

        shader.bind()
                .setInt("clipPlaneEnabled", 1)
                .setFloat("clipVec", (float)vec.x, (float)vec.y, (float)vec.z)
                .setFloat("clipPos", (float)pos.x, (float)pos.y, (float)pos.z);
    }

    private Matrix4f getModelMatrix(PortalEntity portal, ActiveRenderInfo camera, float offset) {
        Vector3i portalNormal = portal.getDirection().getNormal();
        MatrixStack matrix = new MatrixStack();
        Vec3 offsetNormal = new Vec3(camera.getPosition()).sub(portal.position()).normalize().mul(offset);

        matrix.translate(offsetNormal.x, offsetNormal.y, offsetNormal.z);
        matrix.translate(portalNormal.getX() * 0.0001f, portalNormal.getY() * 0.0001f, portalNormal.getZ() * 0.0001f);
        PortalEntity.setupMatrix(matrix, portal.getDirection(), portal.getUpVector(), portal.getPivotPoint());
        return matrix.last().pose();
    }

    private Matrix4f getViewMatrix(ActiveRenderInfo camera) {
        Vector3d cameraPos = camera.getPosition();
        MatrixStack matrix = new MatrixStack();
        float roll = camera instanceof PortalCamera ? ((PortalCamera)camera).getRoll() : PMState.cameraRoll;

        matrix.mulPose(Vector3f.ZP.rotationDegrees(roll));
        matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        matrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return matrix.last().pose();
    }

    public ActiveRenderInfo getCurrentCamera() {
        if(this.currentCamera == null)
            return Minecraft.getInstance().gameRenderer.getMainCamera();
        return this.currentCamera;
    }
}