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
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.*;
import net.minecraft.block.BlockState;
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
import net.portalmod.core.util.ModUtil;
import net.portalmod.core.util.RenderUtil;
import net.portalmod.core.util.VertexRenderer;
import net.portalmod.mixins.accessors.LevelRendererAccessor;
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

    /** Cached {@code Optional.of(VoxelShapes.empty())} for the portal-sight shape override hot path. */
    private static final Optional<VoxelShape> EMPTY_SHAPE_OVERRIDE = Optional.of(VoxelShapes.empty());

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

        boolean isOuter = recursion == 0;
        if(isOuter) {
            PROFILE.enabled = portalProfilerEnabled();
            if(PROFILE.enabled) {
                PROFILE.reset();
                PROFILE.push("frame");
            }
            portalsCulledByNdcRect = 0;
            portalsStencilSkippedOccludedRays = 0;
            parentNdcRectStack.clear();
            currentParentNdcRect[0] = -1f;
            currentParentNdcRect[1] = -1f;
            currentParentNdcRect[2] =  1f;
            currentParentNdcRect[3] =  1f;
        }
        PROFILE.push("portals@r" + recursion);

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
                PROFILE.push("renderPortal");
                renderPortal((PortalEntity)entity, camera, clippingHelper, projectionMatrix, partialTicks, fabulousGraphics);
                PROFILE.pop();
                renderedPortals++;
            }
        }

        if(recursion == 0) {
            currentlyRenderingPortals = false;
        }

        if(fabulousGraphics) {
            PROFILE.push("fabulousBlit@r" + recursion);
            blitFBOtoFBO(mainFBO, tempFBO);
            tempFBO.copyDepthFrom(mainFBO);
            mainFBO.bindWrite(false);
            PROFILE.pop();
        }

        if(PortalModConfigManager.HIGHLIGHTS.get()) {
            PROFILE.push("highlights");
            renderHighlights(camera, projectionMatrix);
            PROFILE.pop();
        }

        if(recursion == 0 && !fabulousGraphics) {
            mainFBO.bindWrite(false);
            GL11.glEnable(GL_STENCIL_TEST);
            RenderSystem.stencilMask(0xFF);
            RenderSystem.clear(GL_STENCIL_BUFFER_BIT, false);
        }

        GL11.glEnable(GL_ALPHA_TEST);

        PROFILE.pop();
        if(isOuter) {
            PROFILE.pop();
            PROFILE.snapshotInto(net.portalmod.core.event.ClientEvents.debugStrings);
        }
    }

    private boolean discardPortal(PortalEntity portal, ActiveRenderInfo camera, ClippingHelper clippingHelper, Matrix4f projectionMatrix) {
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
        if(portalToCamera.magnitude() > 1 && !clippingHelper.isVisible(portal.getBoundingBox()))
            return true;

        // Portal-edge frustum narrowing: at recursion >= 1 we know no pixel outside
        // the parent portal's screen-space silhouette can pass the stencil test, so
        // any child portal whose NDC bounding rect does not intersect the current
        // parent rect is invisible no matter how we render it. Skip at r=0 (parent
        // rect is the full screen) and whenever the camera is dangerously close to
        // the portal plane (w-divide becomes unstable).
        if(recursion >= 1 && portalToCamera.magnitude() > 1.5) {
            float[] rect = computePortalNdcRect(portal, camera, projectionMatrix);
            if(rect != null && !rectsIntersect(rect, currentParentNdcRect)) {
                portalsCulledByNdcRect++;
                return true;
            }
        }
        return false;
    }

    /**
     * Project the portal's AABB corners through {@code projection * view} and return
     * an NDC-space bounding rect {@code {xMin, yMin, xMax, yMax}}. Returns {@code null}
     * when the AABB straddles the near plane (any corner has w &lt;= epsilon), in which
     * case the caller should skip NDC-based culling to avoid false positives.
     */
    private float[] computePortalNdcRect(PortalEntity portal, ActiveRenderInfo camera, Matrix4f projectionMatrix) {
        AxisAlignedBB bb = portal.getBoundingBox();
        Matrix4f view = getViewMatrix(camera);
        Matrix4f mvp = projectionMatrix.copy();
        mvp.multiply(view);

        float xMin =  Float.POSITIVE_INFINITY, yMin =  Float.POSITIVE_INFINITY;
        float xMax = Float.NEGATIVE_INFINITY, yMax = Float.NEGATIVE_INFINITY;

        double[] xs = {bb.minX, bb.maxX};
        double[] ys = {bb.minY, bb.maxY};
        double[] zs = {bb.minZ, bb.maxZ};
        for(double x : xs) {
            for(double y : ys) {
                for(double z : zs) {
                    Vector4f corner = new Vector4f((float)x, (float)y, (float)z, 1f);
                    corner.transform(mvp);
                    float w = corner.w();
                    if(w <= 0.01f) return null;
                    float nx = corner.x() / w;
                    float ny = corner.y() / w;
                    if(nx < xMin) xMin = nx;
                    if(nx > xMax) xMax = nx;
                    if(ny < yMin) yMin = ny;
                    if(ny > yMax) yMax = ny;
                }
            }
        }

        if(xMin < -1f) xMin = -1f;
        if(yMin < -1f) yMin = -1f;
        if(xMax >  1f) xMax =  1f;
        if(yMax >  1f) yMax =  1f;
        return new float[]{xMin, yMin, xMax, yMax};
    }

    /** At most one occlusion-debug dust burst per portal per game tick when profiling. */
    private static final Map<UUID, Long> PROFILER_PORTAL_OCCLUSION_LAST_PARTICLE_TICK = new HashMap<>();

    /** Grid width × height for {@link #portalOpeningControlPoints}; product = sample count. */
    private static final int PORTAL_OPENING_GRID_W = 8;
    private static final int PORTAL_OPENING_GRID_H = 16;
    private static final int PORTAL_OPENING_CONTROL_POINT_COUNT = PORTAL_OPENING_GRID_W * PORTAL_OPENING_GRID_H;

    /**
     * World-space sample points on the portal opening laid out in a {@link #PORTAL_OPENING_GRID_W}×{@link #PORTAL_OPENING_GRID_H}
     * grid (row-major: index = row * W + col). Local coords (a,b) in [-1,1]² map to {@code center + a*right + b*up}
     * (source basis: half width 0.5, half height 1).
     *
     * @param out length must be at least {@link #PORTAL_OPENING_CONTROL_POINT_COUNT}
     */
    private static void portalOpeningControlPoints(PortalEntity portal, Vector3d[] out) {
        OrthonormalBasis basis = portal.getSourceBasis();
        Vec3 rightUnit = basis.getX().normalize();
        Vec3 upUnit = basis.getY().normalize();
        double rx = rightUnit.x * 0.5, ry = rightUnit.y * 0.5, rz = rightUnit.z * 0.5;
        double ux = upUnit.x,         uy = upUnit.y,         uz = upUnit.z;
        Vector3d center = portal.getBoundingBox().getCenter();
        int idx = 0;
        for(int row = 0; row < PORTAL_OPENING_GRID_H; row++) {
            double b = portalOpeningGridCoord(PORTAL_OPENING_GRID_H, row);
            for(int col = 0; col < PORTAL_OPENING_GRID_W; col++) {
                double a = portalOpeningGridCoord(PORTAL_OPENING_GRID_W, col);
                out[idx++] = new Vector3d(
                        center.x + a * rx + b * ux,
                        center.y + a * ry + b * uy,
                        center.z + a * rz + b * uz);
            }
        }
    }

    /** {@code [-1, 1]} inclusive edge positions for a uniform grid with {@code dim} samples ({@code dim==1} → 0). */
    private static double portalOpeningGridCoord(int dim, int index) {
        if(dim <= 1)
            return 0;
        return -1.0 + 2.0 * index / (dim - 1);
    }

    private static boolean blockCountsAsOpaqueForPortalSight(BlockState state) {
        if(state.isAir())
            return false;
        // Blocks that opt out of face-culling occlusion (glass, panes, bars, leaves,
        // scaffolding, fences, etc.) are visually see-through enough that the portal
        // surface behind them still needs to draw, so the ray should pass through.
        if(!state.canOcclude())
            return false;
        // Defensive fallback for modded blocks that claim canOcclude=true while having
        // no collider (fluids, decorations). In vanilla this is already covered by
        // canOcclude, but modded content doesn't always follow the rule.
        if(!state.getMaterial().blocksMotion())
            return false;
        return true;
    }

    /**
     * {@code true} if the ray from {@code from} reaches {@code corner} without an opaque block in front of it.
     * The shape override collapses every non-sight-blocking block (glass, panes, bars,
     * leaves, scaffolding, fluids, …) to {@link VoxelShapes#empty()}, so the raycast
     * keeps traversing through stacks of non-occluders and only reports a hit when it
     * reaches an actually solid block.
     *
     * <p>Only called from {@link #portalOpeningFullyOccluded} at {@code recursion == 1}
     * with the real main camera, so portal-chain traversal is not needed — a plain
     * {@link ModUtil#customClip} is faster than {@code clipThroughPortals*} for this
     * 128-ray-per-frame hot path.
     *
     * <p>When {@code emitDebugParticles} is {@code true}, spawns a coloured dust at the
     * corner sample and (if the ray was blocked) at the exact block-face hit point, so
     * you can see the ray results ingame.
     */
    private static boolean cornerVisibleAlongRay(ClientWorld world, Vector3d from, Vector3d corner,
                                                 @Nullable Entity viewer, boolean emitDebugParticles) {
        RayTraceContext ctx = new RayTraceContext(from, corner, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, viewer);
        BlockRayTraceResult hit = ModUtil.customClip(world, ctx, pos ->
                blockCountsAsOpaqueForPortalSight(world.getBlockState(pos))
                        ? Optional.empty()
                        : EMPTY_SHAPE_OVERRIDE);

        boolean visible;
        if(hit.getType() == RayTraceResult.Type.MISS) {
            visible = true;
        } else {
            double distCorner = from.distanceTo(corner);
            double distHit = from.distanceTo(hit.getLocation());
            visible = distHit >= distCorner - RAY_CORNER_EPSILON;
        }

        if(emitDebugParticles) {
            // Corner sample: green = ray reached it (portal will render), red = occluded.
            RedstoneParticleData cornerDust = visible
                    ? new RedstoneParticleData(0.1F, 1.0F, 0.2F, 0.2F)
                    : new RedstoneParticleData(1.0F, 0.15F, 0.1F, 0.2F);
            world.addParticle(cornerDust, corner.x, corner.y, corner.z, 0, 0, 0);

            if(!visible && hit.getType() != RayTraceResult.Type.MISS) {
                // Yellow dust at the block-face where the ray stopped. If grates/glass
                // are working correctly, this should be on the solid wall behind them,
                // not on the grate itself.
                Vector3d h = hit.getLocation();
                RedstoneParticleData hitDust = new RedstoneParticleData(1.0F, 0.9F, 0.2F, 0.2F);
                world.addParticle(hitDust, h.x, h.y, h.z, 0, 0, 0);
            }
        }

        return visible;
    }

    /**
     * {@code true} if every grid sample on the portal opening is blocked by opaque geometry from the camera
     * (cheap skip path). Only meaningful from {@link #renderPortal} when {@code recursion == 1} (main view);
     * nested passes skip this.
     */
    public boolean portalOpeningFullyOccluded(PortalEntity portal, ActiveRenderInfo camera) {
        ClientWorld world = Minecraft.getInstance().level;
        if(world == null)
            return false;

        Vector3d from = camera.getPosition();
        Entity viewer = camera.getEntity();
        if(viewer == null)
            viewer = Minecraft.getInstance().player;

        Vector3d[] points = new Vector3d[PORTAL_OPENING_CONTROL_POINT_COUNT];
        portalOpeningControlPoints(portal, points);

        // Rate-limit debug particles to once per game tick per portal so the HUD
        // stays readable when the occlusion test runs every frame.
        boolean emitDebugParticles = false;
        if(PROFILE.enabled) {
            long gt = world.getGameTime();
            UUID id = portal.getUUID();
            Long lastTick = PROFILER_PORTAL_OCCLUSION_LAST_PARTICLE_TICK.get(id);
            if(lastTick == null || lastTick != gt) {
                PROFILER_PORTAL_OCCLUSION_LAST_PARTICLE_TICK.put(id, gt);
                emitDebugParticles = true;
            }
        }

        boolean anyVisible = false;
        for(Vector3d p : points) {
            boolean v = cornerVisibleAlongRay(world, from, p, viewer, emitDebugParticles);
            if(v) {
                anyVisible = true;
                // Without particles we can early-out on the first visible sample. When
                // debug particles are on we keep going so every corner gets coloured.
                if(!emitDebugParticles)
                    return false;
            }
        }
        return !anyVisible;
    }

    private static boolean rectsIntersect(float[] a, float[] b) {
        return !(a[2] < b[0] || a[0] > b[2] || a[3] < b[1] || a[1] > b[3]);
    }

    private static float[] intersectRect(float[] a, float[] b) {
        float xMin = Math.max(a[0], b[0]);
        float yMin = Math.max(a[1], b[1]);
        float xMax = Math.min(a[2], b[2]);
        float yMax = Math.min(a[3], b[3]);
        if(xMin >= xMax || yMin >= yMax) return null;
        return new float[]{xMin, yMin, xMax, yMax};
    }

    private void finishPortalEntity(PortalEntity portal, ActiveRenderInfo camera, float partialTicks, boolean fabulousGraphics) {
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
        if(PROFILE.enabled) {
            PROFILE.portalsVisited++;
            if(recursion > PROFILE.maxRecursionReached)
                PROFILE.maxRecursionReached = recursion;
        }

        if(PortalMod.DEBUG) {
            glEnable(GL43.GL_DEBUG_OUTPUT);
            glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
            GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 0, "Rendering portal, recursion: " + recursion);
        }

        PROFILE.push("discardPortal");
        boolean culled = discardPortal(portal, camera, clippingHelper, projectionMatrix);
        PROFILE.pop();
        if(culled) {
            if(PROFILE.enabled)
                PROFILE.portalsCulled++;
            finishPortalEntity(portal, camera, partialTicks, fabulousGraphics);
            return;
        }

        // Four-corner ray occlusion only for the outermost portal pass (recursion 1 after increment).
        // Nested renderLevel uses PortalCamera; established discard/stencil/nesting handles those.
        boolean openingOccluded = recursion == 1 && portalOpeningFullyOccluded(portal, camera);
        if(openingOccluded) {
            portalsStencilSkippedOccludedRays++;
            finishPortalEntity(portal, camera, partialTicks, fabulousGraphics);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Framebuffer mainFBO = mc.getMainRenderTarget();

        Matrix4f modelMatrix = getModelMatrix(portal, camera, portal.getWallAttachmentDistance(camera));
        Matrix4f viewMatrix = getViewMatrix(camera);

        Matrix4f modelView = viewMatrix.copy();
        modelView.multiply(modelMatrix);

        {
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
            PROFILE.push("mask(INCR)");
            renderMask(portal, modelMatrix, viewMatrix, projectionMatrix);
            PROFILE.pop();

            RenderSystem.stencilMask(0);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            PROFILE.push("background");
            renderBackground();
            PROFILE.pop();

            boolean canNestRender = otherPortalOptional.isPresent() && !isDeepest();

            if(canNestRender) {
                PortalEntity otherPortal = otherPortalOptional.get();
                portalStack.push(otherPortal);

                // Narrow the screen-space clip region to this portal's silhouette
                // for the nested render. Children will be tested against this rect
                // in discardPortal, matching Source's portal-edge frustum clipping.
                float[] savedParentNdcRect = new float[]{
                        currentParentNdcRect[0], currentParentNdcRect[1],
                        currentParentNdcRect[2], currentParentNdcRect[3]};
                parentNdcRectStack.push(savedParentNdcRect);
                float[] portalRect = computePortalNdcRect(portal, camera, projectionMatrix);
                if(portalRect != null) {
                    float[] narrowed = intersectRect(savedParentNdcRect, portalRect);
                    if(narrowed != null) {
                        currentParentNdcRect[0] = narrowed[0];
                        currentParentNdcRect[1] = narrowed[1];
                        currentParentNdcRect[2] = narrowed[2];
                        currentParentNdcRect[3] = narrowed[3];
                    }
                }

                clipMatrix.pushPose();
                RenderUtil.setupClipPlane(clipMatrix, portal, camera, 0, false);

                currentCamera = portalCamera;

                Vec3 oldCameraPosOverrideForRenderingSelf = PMState.cameraPosOverrideForRenderingSelf;
                PMState.cameraPosOverrideForRenderingSelf = PMState.cameraPosOverrideForRenderingSelf == null ? null
                        : PMState.cameraPosOverrideForRenderingSelf.clone().transform(PortalEntity.getPortalToPortalMatrix(portal, otherPortal));

                PROFILE.push("saveRenderChunks");
                ObjectList<WorldRenderer.LocalRenderInformationContainer> renderChunks = new ObjectArrayList<>();
                renderChunks.addAll(mc.levelRenderer.renderChunks);
                PROFILE.pop();

                boolean renderOutline = this.shouldRenderOutline(portalChain);
                if(PROFILE.enabled)
                    PROFILE.portalsNested++;

                // Shorter BFS for the nested chunk walk. In 1.16.5, setupRender's BFS grid
                // iterates +/- lastViewDistance around the camera chunk, so clamping that
                // field shrinks the seed grid proportionally to distance^2. We leave
                // options.renderDistance untouched so nothing else (fog, setViewScale, F3,
                // GUI) is disturbed; a mixin redirects only the `options.renderDistance !=
                // lastViewDistance` check at the top of setupRender so it doesn't trip
                // allChanged() during our intentional mismatch.
                LevelRendererAccessor lvlAcc = (LevelRendererAccessor)mc.levelRenderer;
                int origLastViewDistance = lvlAcc.pmGetLastViewDistance();
                // Halve the BFS radius per recursion level (capped at /32). Each
                // nested camera hop sees progressively less of the world through
                // the shrinking silhouette, so we shrink the chunk-walk seed grid
                // geometrically. At r=1: /2, r=2: /4, r=3: /8, ... — mirrors
                // Source engine's portal-aware PVS pruning without needing a PVS.
                int shift = Math.min(recursion, 5);
                int clampedDistance = Math.max(2, origLastViewDistance >> shift);
                boolean distanceClamped = clampedDistance < origLastViewDistance;
                int previousOverride = nestedBfsDistanceOverride;
                if(distanceClamped) {
                    lvlAcc.pmSetLastViewDistance(clampedDistance);
                    nestedBfsDistanceOverride = clampedDistance;
                }

                PROFILE.push("nestedRenderLevel@r" + recursion);
                try {
                    mc.levelRenderer.renderLevel(matrixStack, partialTicks, Util.getNanos(), renderOutline, portalCamera,
                            mc.gameRenderer, mc.gameRenderer.lightTexture, projectionMatrix);
                } finally {
                    if(distanceClamped) {
                        lvlAcc.pmSetLastViewDistance(origLastViewDistance);
                        nestedBfsDistanceOverride = previousOverride;
                    }
                    PROFILE.pop();
                }

                PROFILE.push("restoreRenderChunks");
                mc.levelRenderer.needsUpdate = true;
                mc.levelRenderer.renderChunks.clear();
                mc.levelRenderer.renderChunks.addAll(renderChunks);

                TileEntityRendererDispatcher.instance.prepare(portal.level, mc.getTextureManager(), mc.font, camera, mc.hitResult);
                mc.levelRenderer.entityRenderDispatcher.prepare(portal.level, camera, mc.crosshairPickEntity);
                PROFILE.pop();

                currentCamera = camera;
                PMState.cameraPosOverrideForRenderingSelf = oldCameraPosOverrideForRenderingSelf;

                clipMatrix.popPose();
                portalStack.pop();

                float[] restored = parentNdcRectStack.pop();
                currentParentNdcRect[0] = restored[0];
                currentParentNdcRect[1] = restored[1];
                currentParentNdcRect[2] = restored[2];
                currentParentNdcRect[3] = restored[3];

                if(fabulousGraphics) {
                    GL11.glEnable(GL_STENCIL_TEST);
                    RenderSystem.stencilMask(0);
                    RenderSystem.stencilFunc(GL_NOTEQUAL, recursion, 0x7F);
                    RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
                    PROFILE.push("fabulousBlit(nested)");
                    blitFBOtoFBO(tempFBO, mainFBO);
                    mainFBO.copyDepthFrom(tempFBO);
                    mainFBO.bindWrite(false);
                    PROFILE.pop();
                }
            }

            glDisable(GL_CLIP_PLANE0);
            GL11.glEnable(GL_STENCIL_TEST);
            RenderSystem.color4f(1, 1, 1, 1);

            RenderSystem.stencilMask(0x80);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0xFF);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_INVERT);
            PROFILE.push("mask(INVERT)");
            renderMask(portal, modelMatrix, viewMatrix, projectionMatrix);
            PROFILE.pop();

            RenderSystem.stencilMask(0);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            PROFILE.push("border");
            renderBorder(portal, modelMatrix, viewMatrix, projectionMatrix);
            PROFILE.pop();

            RenderSystem.stencilMask(0x7F);
            RenderSystem.stencilFunc(GL_EQUAL, recursion, 0x7F);
            RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_DECR);
            PROFILE.push("depth(DECR)");
            renderDepth(modelView);
            PROFILE.pop();

            if(!fabulousGraphics) {
                RenderSystem.stencilMask(0);
                RenderSystem.stencilFunc(GL_EQUAL, recursion - 1, 0x7F);
                RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            }
        }

        finishPortalEntity(portal, camera, partialTicks, fabulousGraphics);
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