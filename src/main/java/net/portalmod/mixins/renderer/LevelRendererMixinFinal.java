package net.portalmod.mixins.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Matrix4f;
import net.portalmod.PMGlobals;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.core.util.Colour;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class LevelRendererMixinFinal {

    @Shadow @Final public ObjectList<WorldRenderer.LocalRenderInformationContainer> renderChunks;

    @Shadow public ChunkRenderDispatcher chunkRenderDispatcher;

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

    @Inject(
            remap = false,
            method = "renderLevel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmRenderPortals(MatrixStack matrixStack, float partialTicks, long l, boolean b, ActiveRenderInfo camera, GameRenderer gr, LightTexture lt, Matrix4f matrix, CallbackInfo info) {
        Minecraft.getInstance().getMainRenderTarget().enableStencil();
        ClientWorld level = Minecraft.getInstance().level;

        if(level == null)
            return;

        if(recursionDepth == 0) {
            FogRenderer.setupColor(camera, partialTicks,
                    Minecraft.getInstance().level,
                    Minecraft.getInstance().options.renderDistance,
                    Minecraft.getInstance().gameRenderer.getDarkenWorldAmount(partialTicks));
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        }

        for(Entity entity : level.entitiesForRendering()) {
            if(entity instanceof PortalEntity) {
                recursionDepth++;
                PortalRenderer.renderPortalFinal((PortalEntity)entity, camera, partialTicks);
                recursionDepth--;
            }
        }
    }

    @Inject(
            remap = false,
            method = "renderChunkLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/IProfiler;push(Ljava/lang/String;)V",
                    ordinal = 0
            )
    )
    private void pmResortTransparency(RenderType type, MatrixStack matrixStack, double x, double y, double z, CallbackInfo info) {
        for(WorldRenderer.LocalRenderInformationContainer lric : this.renderChunks) {
            lric.chunk.resortTransparency(type, this.chunkRenderDispatcher);
        }
    }

    @Inject(
            remap = false,
            method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V",
            at = @At("TAIL")
    )
    private void pmRenderDuplicateEntity(Entity entity, double x, double y, double z, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, CallbackInfo info) {
        PortalRenderer.renderDuplicateEntity(entity, x, y, z, partialTicks, matrixStack, renderTypeBuffer, ((WorldRenderer)(Object)this).entityRenderDispatcher);
    }
}