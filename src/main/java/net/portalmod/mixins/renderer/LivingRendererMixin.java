package net.portalmod.mixins.renderer;

import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.portalmod.common.entities.TestElementEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingRenderer.class)
public class LivingRendererMixin {
    
    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isPassenger()Z"))
    private boolean pmShouldSit(LivingEntity entity) {
        return !(TestElementEntity.isHoldable(entity)) && entity.isPassenger();
    }
}