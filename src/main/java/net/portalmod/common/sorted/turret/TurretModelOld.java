package net.portalmod.common.sorted.turret;

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

public class TurretModelOld<T extends TurretEntity> extends EntityModel<T> {
	private final ModelPart head;
	private final ModelPart wing_left;
	private final ModelPart wing_right;
	private final ModelPart leg_back;
	private final ModelPart cube_r1;
	private final ModelPart cube_r2;
	private final ModelPart cube_r3;
	private final ModelPart leg_left;
	private final ModelPart leg_right;
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(PortalMod.MODID, "turret_model_old"), "main");

	/** Model geometry (1.17+ format), converted from the 1.16 ModelRenderer code. */
	public static LayerDefinition createBodyLayer() {
	    MeshDefinition mesh = new MeshDefinition();
	    PartDefinition root = mesh.getRoot();
	    root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(4, 18).addBox(-2.5F, -9.25F, -2.5F, 5.0F, 9.75F, 5.0F).texOffs(0, 0).addBox(-2.75F, -9.25F, -2.75F, 5.5F, 11.0F, 5.5F).texOffs(0, 1).addBox(1.75F, -12.5F, 0.0F, 0.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 14.5F, 0.0F, 0.0F, 0.0F, 0.0F));
	    root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(20, 0).addBox(1.25F, -5.0F, -2.0F, 2.0F, 10.0F, 4.0F).texOffs(24, 14).addBox(1.25F, -1.5F, -1.5F, 1.0F, 4.0F, 3.0F).texOffs(8, 16).addBox(-0.85F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(2.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
	    root.addOrReplaceChild("wing_right", CubeListBuilder.create().mirror(true).texOffs(20, 0).addBox(-3.25F, -5.0F, -2.0F, 2.0F, 10.0F, 4.0F).texOffs(24, 14).addBox(-2.25F, -1.5F, -1.5F, 1.0F, 4.0F, 3.0F).texOffs(8, 16).addBox(-2.15F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-2.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
	    PartDefinition leg_back_def = root.addOrReplaceChild("leg_back", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.0F, 6.0F, 0.3927F, 0.0F, 0.0F));
	    leg_back_def.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(26, 20).addBox(0.0F, -4.7059F, -3.0196F, 0.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.4807F, -4.9875F, -1.6581F, 0.0F, 0.0F));
	    leg_back_def.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(20, 14).addBox(-1.0F, -1.7741F, -0.1173F, 1.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.5F, 1.75F, -0.5F, -0.2618F, 0.0F, 0.0F));
	    leg_back_def.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 1).addBox(-2.0F, -3.0241F, -0.1173F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(1.0F, 1.0F, -1.0F, -0.2618F, 0.0F, 0.0F));
	    root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -2.3858F, -1.574F, 2.0F, 2.0F, 2.0F).texOffs(0, 22).addBox(-0.5F, -0.3858F, -1.074F, 1.0F, 6.0F, 1.0F).texOffs(26, 21).addBox(0.0F, -4.8858F, 0.426F, 0.0F, 4.0F, 3.0F), PartPose.offsetAndRotation(2.5F, 19.0F, -5.0F, -0.3927F, -0.3927F, 0.0F));
	    root.addOrReplaceChild("leg_right", CubeListBuilder.create().mirror(true).texOffs(0, 17).addBox(-1.0F, -2.3858F, -1.574F, 2.0F, 2.0F, 2.0F).texOffs(0, 22).addBox(-0.5F, -0.3858F, -1.074F, 1.0F, 6.0F, 1.0F).texOffs(26, 21).addBox(0.0F, -4.8858F, 0.426F, 0.0F, 4.0F, 3.0F), PartPose.offsetAndRotation(-2.5F, 19.0F, -5.0F, -0.3927F, 0.3927F, 0.0F));
	    return LayerDefinition.create(mesh, 32, 32);
	}

	public TurretModelOld() {
	    this(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
	}

	public TurretModelOld(ModelPart root) {
	    this.head = root.getChild("head");
	    this.wing_left = root.getChild("wing_left");
	    this.wing_right = root.getChild("wing_right");
	    this.leg_back = root.getChild("leg_back");
	    this.cube_r1 = this.leg_back.getChild("cube_r1");
	    this.cube_r2 = this.leg_back.getChild("cube_r2");
	    this.cube_r3 = this.leg_back.getChild("cube_r3");
	    this.leg_left = root.getChild("leg_left");
	    this.leg_right = root.getChild("leg_right");
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		wing_left.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		wing_right.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_back.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_left.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_right.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}