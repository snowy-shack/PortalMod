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

        if(!PortalRenderer.renderPortals(level, camera, clippinghelper, partialTicks))
            info.cancel();
    }

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V",
            at = @At("TAIL")
    )
    private void pmRenderDuplicateEntity(Entity entity, double x, double y, double z, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, CallbackInfo info) {
//        PortalEntityRenderer.renderDuplicateEntity(entity, x, y, z, partialTicks, matrixStack, renderTypeBuffer, ((WorldRenderer)(Object)this).entityRenderDispatcher);
        PortalRenderer.renderDuplicateEntity(entity, x, y, z, partialTicks, matrixStack, renderTypeBuffer, ((WorldRenderer)(Object)this).entityRenderDispatcher);
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
        PortalRenderer.renderHighlights(camera);
    }
}