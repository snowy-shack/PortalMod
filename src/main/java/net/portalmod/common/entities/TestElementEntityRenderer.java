package net.portalmod.common.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public abstract class TestElementEntityRenderer<T extends TestElementEntity, M extends EntityModel<T>> extends LivingRenderer<T, M> {
    public TestElementEntityRenderer(EntityRendererManager manager, M model, float v) {
        super(manager, model, v);
    }

    @Override
    public void render(T entity, float p_225623_2_, float p_225623_3_, MatrixStack matrixStack, IRenderTypeBuffer p_225623_5_, int light) {
        float f5 = (float)entity.getHurtTime() - p_225623_3_;
        float f6 = Math.max(0, entity.getDamage() - p_225623_3_);

        int rawBrightness = entity.level.getLightEngine().getRawBrightness(entity.blockPosition(), 0);
        light = LightTexture.pack(rawBrightness, rawBrightness); // Fixup the light to be that of the entity

        matrixStack.pushPose();

        if (f5 > 0.0F) {
            matrixStack.mulPose(new Vector3f(entity.getLookAngle().multiply(1, 0, 1).normalize()).rotationDegrees(MathHelper.sin(f5) * f5 * f6 / 10.0F * (float)entity.getHurtDir()));
        }

        float wiggle = entity.getWiggle() - p_225623_3_;
        if (wiggle > 0.0F) {
            matrixStack.mulPose(new Vector3f(entity.getLookAngle().multiply(1, 0, 1).normalize()).rotationDegrees(MathHelper.sin(wiggle * 1.5f) * wiggle * 0.5f * (float)entity.getHurtDir()));
        }

        super.render(entity, p_225623_2_, p_225623_3_, matrixStack, p_225623_5_, entity.getFizzleLight(light));

        matrixStack.popPose();
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return Minecraft.renderNames() && entity.hasCustomName() && this.entityRenderDispatcher.crosshairPickEntity == entity;
    }
}
