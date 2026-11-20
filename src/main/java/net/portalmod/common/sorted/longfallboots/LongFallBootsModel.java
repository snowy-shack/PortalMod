package net.portalmod.common.sorted.longfallboots;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class LongFallBootsModel extends HumanoidModel<LivingEntity> {
    private final ModelPart left_shoe;
    private final ModelPart right_shoe;
    private final LivingEntity entity;
    
    public LongFallBootsModel(LivingEntity entity) {
        super(0);

        this.texWidth = 32;
        this.texHeight = 16;

        this.left_shoe = new ModelPart(this);
        this.left_shoe.setPos(2.0F, 12.0F, 0.0F);
        this.left_shoe.texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.75F, false);
        this.left_shoe.texOffs(16, 7).addBox(-2.0F, 4.0F, 3.5F, 4.0F, 7.0F, 2.0F, 0.75F, false);

        this.right_shoe = new ModelPart(this);
        this.right_shoe.setPos(-2.0F, 12.0F, 0.0F);
        this.right_shoe.texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.75F, true);
        this.right_shoe.texOffs(16, 7).addBox(-2.0F, 4.0F, 3.5F, 4.0F, 7.0F, 2.0F, 0.75F, true);
        
        this.entity = entity;
    }
    
    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        this.setupAnim(entity, packedOverlay, red, green, blue, alpha);
        this.left_shoe.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.right_shoe.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    
    @Override
    public void setupAnim(LivingEntity entity, float f1, float f2, float f3, float f4, float f5) {
        super.setupAnim(entity, f1, f2, f3, f4, f5);
        
//        if(entity instanceof LivingEntity) {
            EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
            LivingEntityRenderer<?, ?> renderer = (LivingEntityRenderer<?, ?>)manager.getRenderer(entity);
            if(renderer.getModel() instanceof HumanoidModel) {
                HumanoidModel<?> model = (HumanoidModel<?>)renderer.getModel();
                this.left_shoe.copyFrom(model.leftLeg);
                this.right_shoe.copyFrom(model.rightLeg);
            }
//        }
    }
}