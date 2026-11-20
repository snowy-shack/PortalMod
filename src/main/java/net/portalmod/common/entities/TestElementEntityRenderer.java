package net.portalmod.common.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import net.minecraft.world.level.LightLayer;

public abstract class TestElementEntityRenderer<T extends TestElementEntity, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
    public TestElementEntityRenderer(EntityRenderDispatcher manager, M model, float v) {
        super(manager, model, v);
    }

    @Override
    public void render(T entity, float p_225623_2_, float p_225623_3_, PoseStack matrixStack, MultiBufferSource p_225623_5_, int light) {
        float f5 = (float)entity.getHurtTime() - p_225623_3_;
        float f6 = Math.max(0, entity.getDamage() - p_225623_3_);

        light = LightTexture.pack(
                entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()),
                entity.level().getBrightness(LightLayer.SKY, entity.blockPosition())
        ); // Fixup the light to be that of the entity

        matrixStack.pushPose();

        if (f5 > 0.0F) {
            matrixStack.mulPose(entity.getLookAngle().multiply(1, 0, 1).normalize().toVector3f().rotationDegrees(Mth.sin(f5) * f5 * f6 / 10.0F * (float)entity.getHurtDir()));
        }

        float wiggle = entity.getWiggle() - p_225623_3_;
        if (wiggle > 0.0F) {
            matrixStack.mulPose(entity.getLookAngle().multiply(1, 0, 1).normalize().toVector3f().rotationDegrees(Mth.sin(wiggle * 1.5f) * wiggle * 0.5f * (float)entity.getHurtDir()));
        }

        super.render(entity, p_225623_2_, p_225623_3_, matrixStack, p_225623_5_, entity.getFizzleLight(light));

        matrixStack.popPose();
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return Minecraft.renderNames() && entity.hasCustomName() && this.entityRenderDispatcher.crosshairPickEntity == entity;
    }
}
