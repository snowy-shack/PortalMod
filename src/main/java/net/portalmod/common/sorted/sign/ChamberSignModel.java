package net.portalmod.common.sorted.sign;

import net.minecraft.client.model.geom.builders.MeshDefinition;

import net.minecraft.client.model.geom.builders.LayerDefinition;

import net.minecraft.client.model.geom.builders.CubeListBuilder;

import net.minecraft.client.model.geom.PartPose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class ChamberSignModel extends EntityModel<Entity> {
	public ModelPart main;

	public ChamberSignModel() {
	}

	/**
	 * Rebuilds the sign geometry from the sign's state. 1.17+ models are built from a mesh definition
	 * and then baked, so we build a small mesh here and bake it straight away.
	 */
	public void changeModel(ChamberSignEntity sign) {
		MeshDefinition mesh = new MeshDefinition();
		CubeListBuilder cubes = CubeListBuilder.create()
				// Base
				.texOffs(1, 1).addBox(-12.0F, -24.0F, -1.0F, 24.0F, 48.0F, 2.0F)
				// Progress bar
				.texOffs(8, 51).addBox(-9.0F, 4.0F, -1.01F, 1 + Mth.clamp(sign.getProgress(), -1, 8) * 2, 2.0F, 1.0F)
				// Numbers
				.texOffs(53 + (sign.getLeftDigit() % 5 * 9), sign.getLeftDigit() > 4 ? 20 : 0).addBox(-9.0F, -17.0F, -1.01F, 8.0F, 18.0F, 1.0F)
				.texOffs(53 + (sign.getRightDigit() % 5 * 9), sign.getRightDigit() > 4 ? 20 : 0).addBox(-1.0F, -17.0F, -1.01F, 8.0F, 18.0F, 1.0F)
				// Icons
				.texOffs(1, 51 + (sign.getIcon1() ? 4 : 0)).addBox(-8.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F)
				.texOffs(1, 51 + (sign.getIcon2() ? 4 : 0)).addBox(-4.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F)
				.texOffs(1, 51 + (sign.getIcon3() ? 4 : 0)).addBox(1.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F)
				.texOffs(1, 51 + (sign.getIcon4() ? 4 : 0)).addBox(5.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F)
				.texOffs(1, 51 + (sign.getIcon5() ? 4 : 0)).addBox(-8.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F)
				.texOffs(1, 51 + (sign.getIcon6() ? 4 : 0)).addBox(-4.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F)
				.texOffs(1, 51 + (sign.getIcon7() ? 4 : 0)).addBox(1.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F)
				.texOffs(1, 51 + (sign.getIcon8() ? 4 : 0)).addBox(5.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F);
		mesh.getRoot().addOrReplaceChild("main", cubes, PartPose.offset(0.0F, 24.0F, 0.0F));
		main = LayerDefinition.create(mesh, 128, 64).bakeRoot().getChild("main");
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		main.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}