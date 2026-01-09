package net.portalmod.mixins.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.PMGlobals;
import net.portalmod.PMState;
import net.portalmod.client.render.PortalCamera;
import net.portalmod.client.screens.PortalModOptionsScreen;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.core.math.Vec3;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WorldRenderer.class)
public class LevelRendererMixinFinal {

    // BEWARE: PORTAL RENDERING
    @Redirect(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"
            )
    )
    private void pmClear(int buffers, boolean onOsx) {
        if(PortalRenderer.getInstance().recursion == 0) {
            GL11.glStencilMask(~0);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT, onOsx);
        }
    }

    @Redirect(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/FogRenderer;setupColor(Lnet/minecraft/client/renderer/ActiveRenderInfo;FLnet/minecraft/client/world/ClientWorld;IF)V"
            )
    )
    private void pmSetupColor(ActiveRenderInfo camera, float partialTicks, ClientWorld level, int renderDistance, float darken) {
        if(PortalRenderer.getInstance().currentlyRenderingPortals && Minecraft.getInstance().player != null) {
            camera = new PortalCamera(camera, partialTicks);
            camera.setPosition(Minecraft.getInstance().player.position());
        }

        FogRenderer.setupColor(camera, partialTicks, level, renderDistance, darken);
    }

    @Redirect(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/FogRenderer;setupFog(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/FogRenderer$FogType;FZF)V"
            )
    )
    private void pmSetupFog(ActiveRenderInfo camera, FogRenderer.FogType type, float renderDistance, boolean b, float partialTicks) {
        if(PortalRenderer.getInstance().currentlyRenderingPortals && Minecraft.getInstance().player != null) {
            camera = new PortalCamera(camera, partialTicks);
            camera.setPosition(Minecraft.getInstance().player.position());
        }

        FogRenderer.setupFog(camera, type, renderDistance, b, partialTicks);
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/DimensionRenderInfo;constantAmbientLight()Z"
            )
    )
    private void pmRenderPortals(MatrixStack matrixStack, float partialTicks, long l, boolean b, ActiveRenderInfo camera, GameRenderer gr, LightTexture lt, Matrix4f matrix, CallbackInfo info) {
        ClientWorld level = Minecraft.getInstance().level;

        Vector3d vector3d = camera.getPosition();
        double x = vector3d.x();
        double y = vector3d.y();
        double z = vector3d.z();
        Matrix4f matrix4f = matrixStack.last().pose();
        ClippingHelper clippinghelper = new ClippingHelper(matrix4f, matrix);
        clippinghelper.prepare(x, y, z);

        PortalRenderer.getInstance().renderPortals(level, camera, clippinghelper, matrix, partialTicks);
    }

    // BEWARE: PORTAL RENDERING
    @ModifyArgs(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/WorldRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/matrix/MatrixStack;DDD)V"
            )
    )
    private void pmSwitchToPortalTransparency(Args args) {
        RenderType renderType = args.get(0);
        if(renderType == RenderType.translucent() && PortalRenderer.getInstance().currentlyRenderingPortals)
            args.set(0, PortalTransparencyHandler.PORTAL_TRANSLUCENT);
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
            remap = false,
            method = "renderChunkLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;translucent()Lnet/minecraft/client/renderer/RenderType;",
                    ordinal = 1
            )
    )
    private RenderType pmRenderPortalTransparencyBackwards() {
        return PortalRenderer.getInstance().currentlyRenderingPortals ? PortalTransparencyHandler.PORTAL_TRANSLUCENT : RenderType.translucent();
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
            remap = false,
            method = "renderChunkLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;isEmpty(Lnet/minecraft/client/renderer/RenderType;)Z"
            )
    )
    private boolean pmIsPortalTransparencyEmpty(ChunkRenderDispatcher.CompiledChunk compiledChunk, RenderType renderType) {
        return compiledChunk.isEmpty(renderType == PortalTransparencyHandler.PORTAL_TRANSLUCENT ? RenderType.translucent() : renderType);
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "renderChunkLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;setupRenderState()V",
                    shift = At.Shift.AFTER
            )
    )
    private void pmResortPortalTransparency(RenderType renderType, MatrixStack matrixStack, double x, double y, double z, CallbackInfo ci) {
        // todo probably optimize a bit
        // todo do asynchronously
        if(renderType == PortalTransparencyHandler.PORTAL_TRANSLUCENT) {
            Minecraft.getInstance().getProfiler().push("pm_translucent_sort");
            PortalTransparencyHandler.resortTransparency(PortalRenderer.getInstance().currentCamera);
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
            remap = false,
            method = "setupRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;setCamera(Lnet/minecraft/util/math/vector/Vector3d;)V"
            )
    )
    private void pmUseMainCameraForRebuilding(ChunkRenderDispatcher instance, Vector3d pos) {
        instance.setCamera(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    private boolean pmAvoidRenderingOutlineInFakeEntities(Minecraft instance, Entity entity) {
        if(PortalRenderer.getInstance().recursion > 0)
            return false;
        return instance.shouldEntityAppearGlowing(entity);
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;isDetached()Z"
            )
    )
    private boolean pmRenderSelf(ActiveRenderInfo instance) {
        if(!PortalModOptionsScreen.RENDER_SELF.get())
            return instance.isDetached();
        return true;
    }

    // BEWARE: PORTAL RENDERING
    @Redirect(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRendererManager;shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ClippingHelper;DDD)Z"
            )
    )
    private boolean pmShouldRenderSelf(EntityRendererManager erm, Entity entity, ClippingHelper clippingHelper, double camX, double camY, double camZ) {
        if(!PortalModOptionsScreen.RENDER_SELF.get() || entity != Minecraft.getInstance().cameraEntity)
            return erm.shouldRender(entity, clippingHelper, camX, camY, camZ);
        return DuplicateEntityRenderer.shouldRenderSelf(entity, clippingHelper);
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/IProfiler;popPush(Ljava/lang/String;)V",
                    ordinal = 10
            )
    )
    private void pmRenderDuplicateEntities(MatrixStack matrixStack, float partialTicks, long l, boolean b, ActiveRenderInfo camera, GameRenderer gr, LightTexture lt, Matrix4f projectionMatrix, CallbackInfo info) {
        Vector3d vector3d = camera.getPosition();
        double camX = vector3d.x();
        double camY = vector3d.y();
        double camZ = vector3d.z();

        ClippingHelper clippinghelper = new ClippingHelper(matrixStack.last().pose(), projectionMatrix);
        clippinghelper.prepare(camX, camY, camZ);

        DuplicateEntityRenderer.renderDuplicateEntities(clippinghelper, camX, camY, camZ, partialTicks, matrixStack, camera);
    }
}