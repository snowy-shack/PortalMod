package net.portalmod.common.sorted.turret;

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

	public TurretModelOld() {
		texWidth = 32;
		texHeight = 32;

		head = new ModelPart(this);
		head.setPos(0.0F, 14.5F, 0.0F);
		head.texOffs(4, 18).addBox(-2.5F, -9.25F, -2.5F, 5.0F, 9.75F, 5.0F, 0.0F, false);
		head.texOffs(0, 0).addBox(-2.75F, -9.25F, -2.75F, 5.5F, 11.0F, 5.5F, 0.0F, false);
		head.texOffs(0, 1).addBox(1.75F, -12.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F, false);

		wing_left = new ModelPart(this);
		wing_left.setPos(2.5F, 12.0F, 0.0F);
		wing_left.texOffs(20, 0).addBox(1.25F, -5.0F, -2.0F, 2.0F, 10.0F, 4.0F, 0.0F, false);
		wing_left.texOffs(24, 14).addBox(1.25F, -1.5F, -1.5F, 1.0F, 4.0F, 3.0F, 0.0F, false);
		wing_left.texOffs(8, 16).addBox(-0.85F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		wing_right = new ModelPart(this);
		wing_right.setPos(-2.5F, 12.0F, 0.0F);
		wing_right.texOffs(20, 0).addBox(-3.25F, -5.0F, -2.0F, 2.0F, 10.0F, 4.0F, 0.0F, true);
		wing_right.texOffs(24, 14).addBox(-2.25F, -1.5F, -1.5F, 1.0F, 4.0F, 3.0F, 0.0F, true);
		wing_right.texOffs(8, 16).addBox(-2.15F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, true);

		leg_back = new ModelPart(this);
		leg_back.setPos(0.0F, 16.0F, 6.0F);
		setRotationAngle(leg_back, 0.3927F, 0.0F, 0.0F);
		

		cube_r1 = new ModelPart(this);
		cube_r1.setPos(0.0F, 0.4807F, -4.9875F);
		leg_back.addChild(cube_r1);
		setRotationAngle(cube_r1, -1.6581F, 0.0F, 0.0F);
		cube_r1.texOffs(26, 20).addBox(0.0F, -4.7059F, -3.0196F, 0.0F, 5.0F, 3.0F, 0.0F, false);

		cube_r2 = new ModelPart(this);
		cube_r2.setPos(0.5F, 1.75F, -0.5F);
		leg_back.addChild(cube_r2);
		setRotationAngle(cube_r2, -0.2618F, 0.0F, 0.0F);
		cube_r2.texOffs(20, 14).addBox(-1.0F, -1.7741F, -0.1173F, 1.0F, 8.0F, 1.0F, 0.0F, false);

		cube_r3 = new ModelPart(this);
		cube_r3.setPos(1.0F, 1.0F, -1.0F);
		leg_back.addChild(cube_r3);
		setRotationAngle(cube_r3, -0.2618F, 0.0F, 0.0F);
		cube_r3.texOffs(16, 1).addBox(-2.0F, -3.0241F, -0.1173F, 2.0F, 2.0F, 2.0F, 0.0F, false);

		leg_left = new ModelPart(this);
		leg_left.setPos(2.5F, 19.0F, -5.0F);
		setRotationAngle(leg_left, -0.3927F, -0.3927F, 0.0F);
		leg_left.texOffs(0, 17).addBox(-1.0F, -2.3858F, -1.574F, 2.0F, 2.0F, 2.0F, 0.0F, false);
		leg_left.texOffs(0, 22).addBox(-0.5F, -0.3858F, -1.074F, 1.0F, 6.0F, 1.0F, 0.0F, false);
		leg_left.texOffs(26, 21).addBox(0.0F, -4.8858F, 0.426F, 0.0F, 4.0F, 3.0F, 0.0F, false);

		leg_right = new ModelPart(this);
		leg_right.setPos(-2.5F, 19.0F, -5.0F);
		setRotationAngle(leg_right, -0.3927F, 0.3927F, 0.0F);
		leg_right.texOffs(0, 17).addBox(-1.0F, -2.3858F, -1.574F, 2.0F, 2.0F, 2.0F, 0.0F, true);
		leg_right.texOffs(0, 22).addBox(-0.5F, -0.3858F, -1.074F, 1.0F, 6.0F, 1.0F, 0.0F, true);
		leg_right.texOffs(26, 21).addBox(0.0F, -4.8858F, 0.426F, 0.0F, 4.0F, 3.0F, 0.0F, true);
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