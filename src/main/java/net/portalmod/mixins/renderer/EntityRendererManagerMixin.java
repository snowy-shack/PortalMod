package net.portalmod.mixins.renderer;

import net.portalmod.common.sorted.portal.DuplicateEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererManagerMixin {
    @Shadow private static void renderShadow(PoseStack p_229096_0_, MultiBufferSource p_229096_1_, Entity p_229096_2_, float p_229096_3_, float p_229096_4_, LevelReader p_229096_5_, float p_229096_6_) { }

    @Redirect(
                        method = "renderShadow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(DDD)D",
                    ordinal = 0
            )
    )
    private static double pmOverrideShadowPositionX(double factor, double o, double n) {
        if(DuplicateEntityRenderer.entityPosOverride != null)
            return DuplicateEntityRenderer.entityPosOverride.x;
        return Mth.lerp(factor, o, n);
    }

    @Redirect(
                        method = "renderShadow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(DDD)D",
                    ordinal = 1
            )
    )
    private static double pmOverrideShadowPositionY(double factor, double o, double n) {
        if(DuplicateEntityRenderer.entityPosOverride != null)
            return DuplicateEntityRenderer.entityPosOverride.y;
        return Mth.lerp(factor, o, n);
    }

    @Redirect(
                        method = "renderShadow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(DDD)D",
                    ordinal = 2
            )
    )
    private static double pmOverrideShadowPositionZ(double factor, double o, double n) {
        if(DuplicateEntityRenderer.entityPosOverride != null)
            return DuplicateEntityRenderer.entityPosOverride.z;
        return Mth.lerp(factor, o, n);
    }

    @Redirect(
                        method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V"
            )
    )
    private void pmOverrideShadowRendering(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, Entity entity, float strength, float partialTicks, LevelReader worldReader, float radius) {
        if(DuplicateEntityRenderer.entityShadowTransformOverride != null) {
            if(!DuplicateEntityRenderer.shouldRenderShadow)
                return;

            matrixStack = new PoseStack();
            matrixStack.last().pose().multiply(DuplicateEntityRenderer.entityShadowTransformOverride);
        }

        renderShadow(matrixStack, renderTypeBuffer, entity, strength, partialTicks, worldReader, radius);
    }
}