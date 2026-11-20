package net.portalmod.mixins.hooks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Forge's RenderLivingEvent.Pre (cancellable). */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererHooksMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void pmOnRender(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack,
                            MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (ClientEvents.renderLiving((LivingEntityRenderer<?, ?>) (Object) this, entity, partialTicks, poseStack, buffers, light))
            ci.cancel();
    }
}
