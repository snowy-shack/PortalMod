package net.portalmod.mixins.renderer;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.portalmod.common.sorted.portal.PortalRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow(remap = false) abstract protected int getBlockLightLevel(Entity entity, BlockPos pos);
    @Shadow(remap = false) abstract protected int getSkyLightLevel(Entity entity, BlockPos pos);

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ClippingHelper;DDD)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private void pmShouldRender(Entity entity, ClippingHelper clippingHelper, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> info) {
//        if(PortalEntityRenderer.shouldNotRenderEntity(entity, camX, camY, camZ))
//            info.setReturnValue(false);
        if(PortalRenderer.shouldNotRenderEntity(entity, camX, camY, camZ))
            info.setReturnValue(false);
    }

    // todo only when rendering item in hand i guess
    // todo also teleport eyes instead of moving

    // BEWARE: PORTAL RENDERING
    @Inject(
            remap = false,
            method = "getPackedLightCoords(Lnet/minecraft/entity/Entity;F)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void pmDeceiveLightEngine(Entity entity, float partialTicks, CallbackInfoReturnable<Integer> info) {
        BlockPos pos = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();
        info.setReturnValue(LightTexture.pack(
                this.getBlockLightLevel(entity, pos),
                this.getSkyLightLevel(entity, pos)
        ));
    }
}