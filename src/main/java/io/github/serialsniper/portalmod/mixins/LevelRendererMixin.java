package io.github.serialsniper.portalmod.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.serialsniper.portalmod.client.render.ter.PortalableBlockTER;
import io.github.serialsniper.portalmod.client.util.PortalLocation;
import io.github.serialsniper.portalmod.core.event.ClientEvents;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {

    @Inject(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/WorldRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/matrix/MatrixStack;DDD)V",
            shift = At.Shift.AFTER
    ), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/AtlasTexture;restoreLastBlurMipmap()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderTypeBuffers;bufferSource()Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;")
    ), method = "renderLevel(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V")
    private void renderPortals(MatrixStack stack, float partialTicks, long l, boolean b, ActiveRenderInfo camera, GameRenderer gr, LightTexture light, Matrix4f mat, CallbackInfo info) {
        if(ClientEvents.recursion)
            return;

        ClientEvents.recursion = true;

//		event.getInfo();

        for(int i = 0; i < ClientEvents.portals.size(); i++) {
//		for(PortalLocation portal : portals)
            PortalLocation portal = ClientEvents.portals.get(i);
            PortalableBlockTER.renderPortal(portal.getState(), portal.getTransform(), portal.getPos(), partialTicks);
        }

        ClientEvents.clearPortals();

        ClientEvents.recursion = false;
    }
}