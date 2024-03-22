package net.portalmod.mixins.renderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.portalmod.common.sorted.cube.Cube;

@Mixin(LivingRenderer.class)
public class LivingRendererMixin {
    
    @Redirect(remap = false, method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isPassenger()Z"))
    private boolean pmShouldSit(LivingEntity entity) {
        return !(entity instanceof Cube) && entity.isPassenger();
    }
}