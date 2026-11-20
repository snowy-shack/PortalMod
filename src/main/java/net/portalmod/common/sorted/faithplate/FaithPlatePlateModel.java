package net.portalmod.common.sorted.faithplate;

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
import net.portalmod.client.animation.AnimatedModel;
import net.portalmod.core.init.AnimationInit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

public class FaithPlatePlateModel extends AnimatedModel {
    protected final ModelPart bone;
    protected final ModelPart bb_main;
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(PortalMod.MODID, "faith_plate_plate_model"), "main");

    /** Model geometry (1.17+ format), converted from the 1.16 ModelRenderer code. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bone_def = root.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.5F, 29.5F, -1.5F, -0.2182F, 0.0F, 0.0F));
        bone_def.addOrReplaceChild("handles_r1", CubeListBuilder.create().texOffs(10, 0).addBox(-5.0F, -4.6778F, -3.0293F, 8.0F, 7.0F, 18.0F), PartPose.offsetAndRotation(3.5F, 1.1778F, -2.9707F, 0.2182F, 0.0F, 0.0F));
        PartDefinition arm_def = bone_def.addOrReplaceChild("arm", CubeListBuilder.create(), PartPose.offsetAndRotation(2.5F, -2.4837F, 8.3688F, 0.3491F, 0.0F, 0.0F));
        arm_def.addOrReplaceChild("weight_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.870F, -4.114F, 6.0F, 8.0F, 8.0F), PartPose.offsetAndRotation(0.0F, -0.1033F, 0.1286F, -0.1309F, 0.0F, 0.0F));
        arm_def.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(44, 0).addBox(0.0F, -5.3019F, -0.2783F, 5.0F, 2.0F, 4.0F), PartPose.offsetAndRotation(-2.5F, -1.0671F, 0.2114F, -0.1309F, 0.0F, 0.0F));
        PartDefinition plate_def = arm_def.addOrReplaceChild("plate", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -3.0013F, 2.2893F, 0.2618F, 0.0F, 0.0F));
        plate_def.addOrReplaceChild("plate_beam_r1", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, 3.01F, -10.0F, 3.0F, 3.0F, 9.0F).texOffs(4, 25).addBox(-5.0F, 1.0F, -16.0F, 9.0F, 2.0F, 20.0F), PartPose.offsetAndRotation(2.5F, -5.3086F, -3.081F, -0.3927F, 0.0F, 0.0F));
        PartDefinition bb_main_def = root.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        bb_main_def.addOrReplaceChild("glass_r1", CubeListBuilder.create().texOffs(-14, 47).addBox(-15.0F, 0.0F, -7.0F, 30.0F, 0.0F, 14.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public FaithPlatePlateModel() {
        this(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
    }

    public FaithPlatePlateModel(ModelPart root) {
        this.bone = root.getChild("bone");
        ModelPart handles_r1 = this.bone.getChild("handles_r1");
        ModelPart arm = this.bone.getChild("arm");
        ModelPart weight_r1 = arm.getChild("weight_r1");
        ModelPart lock = arm.getChild("lock");
        ModelPart plate = arm.getChild("plate");
        ModelPart plate_beam_r1 = plate.getChild("plate_beam_r1");
        this.bb_main = root.getChild("bb_main");
        ModelPart glass_r1 = this.bb_main.getChild("glass_r1");
        attachAnimation("launch", bone, XROT, AnimationInit.FAITHPLATE_BONE);
        attachAnimation("launch", arm, XROT, AnimationInit.FAITHPLATE_ARM);
        attachAnimation("launch", lock, XROT, AnimationInit.FAITHPLATE_LOCK);
        attachAnimation("launch", plate, XROT, AnimationInit.FAITHPLATE_PLATE);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
//        bone.xRot = red;
//        arm.xRot = green;
//        plate.xRot = blue;
//        lock.xRot = alpha;
        
        bone.render(matrixStack, buffer, packedLight, packedOverlay);
        bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
    }
    
    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}