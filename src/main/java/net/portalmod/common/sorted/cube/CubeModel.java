package net.portalmod.common.sorted.cube;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class CubeModel<T extends Cube> extends EntityModel<T> {
    private final ModelPart cube;
//    public static Shader2 shader;

    public CubeModel() {
        texWidth = 64;
        texHeight = 64;

        cube = new ModelPart(this);
        cube.setPos(0.0F, 24.0F, 0.0F);
        cube.texOffs(0, 26).addBox(-6.0F, -12.5F, -6.0F, 12.0F, 12.0F, 12.0F, 0.0F, false);
        cube.texOffs(0, 0).addBox(-6.5F, -13.0F, -6.5F, 13.0F, 13.0F, 13.0F, 0.0F, false);
//        cube.texOffs(0, 50).addBox(-4.5F, -11.0F, -4.5F, 9.0F, 9.0F, 9.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        cube.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}