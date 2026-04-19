package net.portalmod.mixins.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.portalmod.common.sorted.portal.DuplicateEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRendererManager.class)
public abstract class EntityRendererManagerMixin {
    @Shadow private static void renderShadow(MatrixStack p_229096_0_, IRenderTypeBuffer p_229096_1_, Entity p_229096_2_, float p_229096_3_, float p_229096_4_, IWorldReader p_229096_5_, float p_229096_6_) { }

    @Redirect(
                        method = "renderShadow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;lerp(DDD)D",
                    ordinal = 0
            )
    )
    private static double pmOverrideShadowPositionX(double factor, double o, double n) {
        if(DuplicateEntityRenderer.entityPosOverride != null)
            return DuplicateEntityRenderer.entityPosOverride.x;
        return MathHelper.lerp(factor, o, n);
    }

    @Redirect(
                        method = "renderShadow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;lerp(DDD)D",
                    ordinal = 1
            )
    )
    private static double pmOverrideShadowPositionY(double factor, double o, double n) {
        if(DuplicateEntityRenderer.entityPosOverride != null)
            return DuplicateEntityRenderer.entityPosOverride.y;
        return MathHelper.lerp(factor, o, n);
    }

    @Redirect(
                        method = "renderShadow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;lerp(DDD)D",
                    ordinal = 2
            )
    )
    private static double pmOverrideShadowPositionZ(double factor, double o, double n) {
        if(DuplicateEntityRenderer.entityPosOverride != null)
            return DuplicateEntityRenderer.entityPosOverride.z;
        return MathHelper.lerp(factor, o, n);
    }

    @Redirect(
                        method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRendererManager;renderShadow(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;Lnet/minecraft/entity/Entity;FFLnet/minecraft/world/IWorldReader;F)V"
            )
    )
    private void pmOverrideShadowRendering(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, Entity entity, float strength, float partialTicks, IWorldReader worldReader, float radius) {
        if(DuplicateEntityRenderer.entityShadowTransformOverride != null) {
            if(!DuplicateEntityRenderer.shouldRenderShadow)
                return;

            matrixStack = new MatrixStack();
            matrixStack.last().pose().multiply(DuplicateEntityRenderer.entityShadowTransformOverride);
        }

        renderShadow(matrixStack, renderTypeBuffer, entity, strength, partialTicks, worldReader, radius);
    }
}