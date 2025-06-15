package net.portalmod.mixins.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.PMGlobals;
import net.portalmod.common.sorted.portal.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WorldRenderer.class)
public class LevelRendererMixinFinal {

    @Shadow @Final public ObjectList<WorldRenderer.LocalRenderInformationContainer> renderChunks;

    @Shadow public ChunkRenderDispatcher chunkRenderDispatcher;

    // BEWARE: PORTAL RENDERING
    @Redirect(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"
            )
    )
    private void pmRegulateClearScreen(int buffers, boolean onOsx) {
        if(PMGlobals.ENABLE_CLEAR)
            RenderSystem.clear(buffers, onOsx);
    }

    private static int recursionDepth = 0;

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "renderLevel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmRenderPortals(MatrixStack matrixStack, float partialTicks, long l, boolean b, ActiveRenderInfo camera, GameRenderer gr, LightTexture lt, Matrix4f matrix, CallbackInfo info) {
//        Minecraft.getInstance().getMainRenderTarget().enableStencil();
//        ClientWorld level = Minecraft.getInstance().level;
//
//        if(level == null)
//            return;
//
//        if(recursionDepth == 0) {
//            FogRenderer.setupColor(camera, partialTicks,
//                    Minecraft.getInstance().level,
//                    Minecraft.getInstance().options.renderDistance,
//                    Minecraft.getInstance().gameRenderer.getDarkenWorldAmount(partialTicks));
//            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
//        }
//
//        for(Entity entity : level.entitiesForRendering()) {
//            if(entity instanceof PortalEntity) {
//                recursionDepth++;
//                PortalRenderer.renderPortalFinal((PortalEntity)entity, camera, partialTicks);
//                recursionDepth--;
//            }
//        }
        ClientWorld level = Minecraft.getInstance().level;

        if(level == null)
            return;

        Vector3d vector3d = camera.getPosition();
        double d0 = vector3d.x();
        double d1 = vector3d.y();
        double d2 = vector3d.z();
        Matrix4f matrix4f = matrixStack.last().pose();
        ClippingHelper clippinghelper = new ClippingHelper(matrix4f, matrix);
        clippinghelper.prepare(d0, d1, d2);

        if(!PortalRenderer.getInstance().renderPortals(level, camera, clippinghelper, partialTicks))
            info.cancel();
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
    @Inject(
            remap = false,
            method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V",
            at = @At("TAIL")
    )
    private void pmRenderDuplicateEntity(Entity entity, double x, double y, double z, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, CallbackInfo info) {
//        PortalEntityRenderer.renderDuplicateEntity(entity, x, y, z, partialTicks, matrixStack, renderTypeBuffer, ((WorldRenderer)(Object)this).entityRenderDispatcher);
        PortalRenderer.getInstance().renderDuplicateEntity(entity, x, y, z, partialTicks, matrixStack, renderTypeBuffer, ((WorldRenderer)(Object)this).entityRenderDispatcher);
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;pushMatrix()V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void pmRenderPortalHighlights(MatrixStack matrixStack, float f, long l, boolean b, ActiveRenderInfo camera, GameRenderer gr, LightTexture lt, Matrix4f m, CallbackInfo ci) {
        PortalRenderer.getInstance().renderHighlights(camera);
    }
}