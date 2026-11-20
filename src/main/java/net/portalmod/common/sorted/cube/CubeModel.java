package net.portalmod.common.sorted.cube;

import net.portalmod.PortalMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class CubeModel<T extends Cube> extends EntityModel<T> {
    private final ModelPart cube;
//    public static Shader2 shader;
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(PortalMod.MODID, "cube_model"), "main");

    /** Model geometry (1.17+ format), converted from the 1.16 ModelRenderer code. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0F, -12.5F, -6.0F, 12.0F, 12.0F, 12.0F).texOffs(0, 0).addBox(-6.5F, -13.0F, -6.5F, 13.0F, 13.0F, 13.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public CubeModel() {
        this(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
    }

    public CubeModel(ModelPart root) {
        this.cube = root.getChild("cube");
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