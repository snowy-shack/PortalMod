package net.portalmod.common.sorted.turret;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3d;

public class TurretModel<T extends TurretEntity> extends EntityModel<T> {
	private final ModelRenderer head;
	private final ModelRenderer wing_left;
	private final ModelRenderer wing_right;
	private final ModelRenderer leg_back;
    private final ModelRenderer leg_left;
	private final ModelRenderer leg_right;

	public TurretModel() {
		texWidth = 32;
		texHeight = 32;

		head = new ModelRenderer(this);
		head.setPos(0.0F, 14.5F, 0.0F);
		head.texOffs(4, 18).addBox(-2.5F, -8.5F, -2.5F, 5.0F, 9.0F, 5.0F, 0.0F, false);
		head.texOffs(0, 0).addBox(-2.5F, -9.25F, -2.5F, 5.0F, 11.0F, 5.0F, 0.25F, false);
		head.texOffs(0, 1).addBox(1.75F, -12.5F, 0.0F, 0.0F, 3.0F, 1.0F, 0.0F, false);

		wing_left = new ModelRenderer(this);
		wing_left.texOffs(20, 0).addBox(1.25F, -5.0F, -2.0F, 2.0F, 10.0F, 4.0F, 0.0F, false);
		wing_left.texOffs(24, 14).addBox(1.25F, -1.5F, -1.5F, 1.0F, 4.0F, 3.0F, 0.0F, false);
		wing_left.texOffs(8, 16).addBox(-0.85F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, false);

		wing_right = new ModelRenderer(this);
		wing_right.texOffs(20, 0).addBox(-3.25F, -5.0F, -2.0F, 2.0F, 10.0F, 4.0F, 0.0F, true);
		wing_right.texOffs(24, 14).addBox(-2.25F, -1.5F, -1.5F, 1.0F, 4.0F, 3.0F, 0.0F, true);
		wing_right.texOffs(8, 16).addBox(-2.15F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, 0.0F, true);

		leg_back = new ModelRenderer(this);
		leg_back.setPos(0.0F, 16.0F, 6.0F);
		setRotationAngle(leg_back, 0.3927F, 0.0F, 0.0F);

        ModelRenderer cube_r1 = new ModelRenderer(this);
		cube_r1.setPos(0.0F, 0.4807F, -4.9875F);
		leg_back.addChild(cube_r1);
		setRotationAngle(cube_r1, -1.6581F, 0.0F, 0.0F);
		cube_r1.texOffs(26, 20).addBox(0.0F, -4.7059F, -3.0196F, 0.0F, 5.0F, 3.0F, 0.0F, false);

        ModelRenderer cube_r2 = new ModelRenderer(this);
		cube_r2.setPos(0.5F, 1.75F, -0.5F);
		leg_back.addChild(cube_r2);
		setRotationAngle(cube_r2, -0.2618F, 0.0F, 0.0F);
		cube_r2.texOffs(20, 14).addBox(-1.0F, -1.7741F, -0.1173F, 1.0F, 8.0F, 1.0F, 0.0F, false);

        ModelRenderer cube_r3 = new ModelRenderer(this);
		cube_r3.setPos(1.0F, 1.0F, -1.0F);
		leg_back.addChild(cube_r3);
		setRotationAngle(cube_r3, -0.2618F, 0.0F, 0.0F);
		cube_r3.texOffs(16, 1).addBox(-2.0F, -3.0241F, -0.1173F, 2.0F, 2.0F, 2.0F, 0.0F, false);

		leg_left = new ModelRenderer(this);
		leg_left.setPos(2.5F, 19.0F, -5.0F);
		setRotationAngle(leg_left, -0.3927F, -0.3927F, 0.0F);
		leg_left.texOffs(0, 17).addBox(-1.0F, -2.3858F, -1.574F, 2.0F, 2.0F, 2.0F, 0.0F, false);
		leg_left.texOffs(0, 22).addBox(-0.5F, -0.3858F, -1.074F, 1.0F, 6.0F, 1.0F, 0.0F, false);
		leg_left.texOffs(26, 21).addBox(0.0F, -4.8858F, 0.426F, 0.0F, 4.0F, 3.0F, 0.0F, false);

		leg_right = new ModelRenderer(this);
		leg_right.setPos(-2.5F, 19.0F, -5.0F);
		setRotationAngle(leg_right, -0.3927F, 0.3927F, 0.0F);
		leg_right.texOffs(0, 17).addBox(-1.0F, -2.3858F, -1.574F, 2.0F, 2.0F, 2.0F, 0.0F, true);
		leg_right.texOffs(0, 22).addBox(-0.5F, -0.3858F, -1.074F, 1.0F, 6.0F, 1.0F, 0.0F, true);
		leg_right.texOffs(26, 21).addBox(0.0F, -4.8858F, 0.426F, 0.0F, 4.0F, 3.0F, 0.0F, true);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		float wingOffset, yaw, pitch;
		Vector3d wingAngle;

		float yRod = -(float) Math.toRadians(entity.yRot);

		Vector3d laserDir = entity.turretToTarget.normalize();

		float rotatedX = (float) laserDir.x * (float) Math.cos(yRod) - (float) laserDir.z * (float) Math.sin(yRod);
		float rotatedZ = (float) laserDir.x * (float) Math.sin(yRod) + (float) laserDir.z * (float) Math.cos(yRod);

		Vector3d laserDirR = new Vector3d(rotatedX, laserDir.y, rotatedZ);

		yaw = -(float) Math.atan2(laserDirR.x, laserDirR.z);
		pitch = -(float) Math.asin(laserDirR.y);

		wingOffset = (entity.state == TurretState.OPENING
				   || entity.state == TurretState.SHOOTING
				   || entity.state == TurretState.LOST_TARGET
		           || entity.state == TurretState.FALLING) ? 2.0F : 0.0F;

		setRotationAngle(wing_left, pitch, yaw, 0);
		setRotationAngle(wing_right, pitch, yaw, 0);

		wing_left.setPos(0.5F + wingOffset, 12.0F, 0.0F);
		wing_right.setPos(-0.5F - wingOffset, 12.0F, 0.0F);
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		head.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		wing_left.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		wing_right.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_back.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_left.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leg_right.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}