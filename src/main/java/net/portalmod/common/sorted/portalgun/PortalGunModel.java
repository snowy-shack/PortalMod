package net.portalmod.common.sorted.portalgun;

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

public class PortalGunModel extends AnimatedModel {
    protected final ModelPart gun;
    protected final ModelPart colour;
    protected final ModelPart stripes;

    // todo animations for left hand
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(PortalMod.MODID, "portal_gun_model"), "main");

    /** Model geometry (1.17+ format), converted from the 1.16 ModelRenderer code. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition gun_def = root.addOrReplaceChild("gun", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition gun_front_def = gun_def.addOrReplaceChild("gun_front", CubeListBuilder.create().texOffs(16, 13).addBox(-2.0F, -3.5F, -3.0F, 4.0F, 3.0F, 4.0F).texOffs(18, 22).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 2.0F).texOffs(11, 12).addBox(-1.5F, -4.5F, -2.0F, 3.0F, 3.0F, 1.0F).texOffs(21, 20).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 1.0F, 0.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        gun_front_def.addOrReplaceChild("prong_right", CubeListBuilder.create().mirror(true).texOffs(11, 3).addBox(0.067F, -0.5F, -2.616F, 0.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(-1.2985F, -2.2491F, -2.5F, -1.5708F, 0.0F, -2.0944F));
        gun_front_def.addOrReplaceChild("prong_left", CubeListBuilder.create().texOffs(18, 3).addBox(-0.067F, -0.5F, -2.616F, 0.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(1.2985F, -2.2491F, -2.5F, -1.5708F, 0.0F, 2.0944F));
        PartDefinition prong_top_def = gun_front_def.addOrReplaceChild("prong_top", CubeListBuilder.create().texOffs(25, 3).addBox(0.0F, -0.5F, -3.0F, 0.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -4.0F, -2.0F, -1.5708F, 0.0F, 0.0F));
        PartDefinition potatoParent_def = prong_top_def.addOrReplaceChild("potatoParent", CubeListBuilder.create(), PartPose.offsetAndRotation(0f, 2f, -3f, 1.7F, 0.0F, 0.4F));
        potatoParent_def.addOrReplaceChild("potato", CubeListBuilder.create().texOffs(21, 1).addBox(-1.3119F, -1.0513F, -1.1565F, 3.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(-0.2206F, -0.0222F, -0.3924F, -0.8248F, -0.6669F, 0.9374F));
        potatoParent_def.addOrReplaceChild("potatoWires", CubeListBuilder.create().texOffs(12, 1).addBox(-2.3359F, -1.9776F, -0.3457F, 4.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(-0.2018F, -0.067F, -0.7244F, 2.5876F, -0.0168F, -2.7786F));
        PartDefinition gun_base_def = gun_def.addOrReplaceChild("gun_base", CubeListBuilder.create().texOffs(1, 21).addBox(-2.5F, -5.5F, 2.0F, 5.0F, 4.0F, 6.0F).texOffs(1, 14).addBox(-1.5F, -3.0F, 4.0F, 3.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        gun_base_def.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(1, 14).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -2.0F, 2.5F, 0.0F, 3.1416F, 0.0F));
        PartDefinition colour_def = root.addOrReplaceChild("colour", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        colour_def.addOrReplaceChild("colour_front", CubeListBuilder.create().texOffs(1, 6).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 3.0F).texOffs(9, 7).addBox(-0.5F, -3.5F, -2.0F, 1.0F, 1.0F, 0.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        colour_def.addOrReplaceChild("colour_base", CubeListBuilder.create().texOffs(-1, 12).addBox(-0.5F, -5.5F, 3.0F, 1.0F, 0.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition stripes_def = root.addOrReplaceChild("stripes", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition stripes_base_def = stripes_def.addOrReplaceChild("stripes_base", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        stripes_base_def.addOrReplaceChild("accent_1", CubeListBuilder.create().mirror(true).texOffs(1, 1).addBox(-3.0F, -1.5F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.004F)), PartPose.offsetAndRotation(-2.0F, -4.0F, 5.0F, 0.0F, 1.5708F, 0.0F));
        stripes_base_def.addOrReplaceChild("accent_2", CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -1.5F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.004F)), PartPose.offsetAndRotation(2.0F, -4.0F, 5.0F, 0.0F, -1.5708F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public PortalGunModel() {
        this(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
    }

    public PortalGunModel(ModelPart root) {
        this.gun = root.getChild("gun");
        ModelPart gun_front = this.gun.getChild("gun_front");
        ModelPart prong_right = gun_front.getChild("prong_right");
        ModelPart prong_left = gun_front.getChild("prong_left");
        ModelPart prong_top = gun_front.getChild("prong_top");
        ModelPart potatoParent = prong_top.getChild("potatoParent");
        ModelPart potato = potatoParent.getChild("potato");
        ModelPart potatoWires = potatoParent.getChild("potatoWires");
        ModelPart gun_base = this.gun.getChild("gun_base");
        ModelPart bottom = gun_base.getChild("bottom");
        this.colour = root.getChild("colour");
        ModelPart colour_front = this.colour.getChild("colour_front");
        ModelPart colour_base = this.colour.getChild("colour_base");
        this.stripes = root.getChild("stripes");
        ModelPart stripes_base = this.stripes.getChild("stripes_base");
        ModelPart accent_1 = stripes_base.getChild("accent_1");
        ModelPart accent_2 = stripes_base.getChild("accent_2");
        attachAnimation("shoot", gun_front, Z, AnimationInit.COMPRESSION);
        attachAnimation("shoot", prong_right, XROT, AnimationInit.CLAWS);
        attachAnimation("shoot", prong_left, XROT, AnimationInit.CLAWS);
        attachAnimation("shoot", prong_top, XROT, AnimationInit.CLAWS);
        attachAnimation("lift", gun_front, Z, AnimationInit.COMPRESSION_START);
        attachAnimation("lift", prong_right, XROT, AnimationInit.CLAWS_OPEN);
        attachAnimation("lift", prong_left, XROT, AnimationInit.CLAWS_OPEN);
        attachAnimation("lift", prong_top, XROT, AnimationInit.CLAWS_OPEN);
        attachAnimation("drop", gun_front, Z, AnimationInit.COMPRESSION_STOP);
        attachAnimation("drop", prong_right, XROT, AnimationInit.CLAWS_CLOSE);
        attachAnimation("drop", prong_left, XROT, AnimationInit.CLAWS_CLOSE);
        attachAnimation("drop", prong_top, XROT, AnimationInit.CLAWS_CLOSE);
        attachAnimation("shoot", colour_front, Z, AnimationInit.COMPRESSION);
        attachAnimation("lift", colour_front, Z, AnimationInit.COMPRESSION_START);
        attachAnimation("drop", colour_front, Z, AnimationInit.COMPRESSION_STOP);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        gun.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}