package net.portalmod.mixins.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.portalmod.common.sorted.portal.PortalTransparencyHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ChunkRenderDispatcher.ChunkRender.class)
public class ChunkRenderMixin {
    @Shadow @Final private Map<RenderType, VertexBuffer> buffers;

    @Inject(
                        method = "<init>",
            at = @At("RETURN")
    )
    private void pmInit(ChunkRenderDispatcher crd, CallbackInfo ci) {
        this.buffers.put(PortalTransparencyHandler.PORTAL_TRANSLUCENT, new VertexBuffer(DefaultVertexFormats.BLOCK));
    }
}