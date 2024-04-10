package net.portalmod.common.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
        //todo fix shadow
        float f5 = (float)entity.getHurtTime() - p_225623_3_;
        float f6 = Math.max(0, entity.getDamage() - p_225623_3_);

        if (f5 > 0.0F) {
            matrixStack.mulPose(new Vector3f(entity.getLookAngle().multiply(1, 0, 1).normalize()).rotationDegrees(MathHelper.sin(f5) * f5 * f6 / 10.0F * (float)entity.getHurtDir()));
        }

        super.render(entity, p_225623_2_, p_225623_3_, matrixStack, p_225623_5_, entity.getFizzleLight(light));
    }

    @Override
    protected boolean shouldShowName(T entity) {
        return Minecraft.renderNames() && entity.hasCustomName() && this.entityRenderDispatcher.crosshairPickEntity == entity;
    }
}
