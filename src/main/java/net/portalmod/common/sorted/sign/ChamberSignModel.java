package net.portalmod.common.sorted.sign;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ChamberSignModel extends EntityModel<Entity> {
	public ModelRenderer main;

	public ChamberSignModel() {
		texWidth = 128;
		texHeight = 64;

    }

	public void changeModel(ChamberSignEntity sign) {
		main = new ModelRenderer(this);

		main.setPos(0.0F, 24.0F, 0.0F);

		// Base
		main.texOffs(1, 1).addBox(-12.0F, -24.0F, -1.0F, 24.0F, 48.0F, 2.0F, 0.0F, false);

		// Progress bar
		main.texOffs(8, 51).addBox(-9.0F, 4.0F, -1.01F, 1 + 2 * sign.getProgress(), 2.0F, 1.0F, 0.0F, false);

		// Numbers
		main.texOffs(53 + (sign.getLeftDigit() % 5 * 9), sign.getLeftDigit() > 4 ? 20 : 0).addBox(-9.0F, -17.0F, -1.01F, 8.0F, 18.0F, 1.0F, 0.0F, false);
		main.texOffs(53 + (sign.getRightDigit() % 5 * 9), sign.getRightDigit() > 4 ? 20 : 0).addBox(-1.0F, -17.0F, -1.01F, 8.0F, 18.0F, 1.0F, 0.0F, false);

		// Icons
		main.texOffs(1, 51 + (sign.isIcon1() ? 4 : 0)).addBox(-8.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51 + (sign.isIcon2() ? 4 : 0)).addBox(-4.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51 + (sign.isIcon3() ? 4 : 0)).addBox(1.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51 + (sign.isIcon4() ? 4 : 0)).addBox(5.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51 + (sign.isIcon5() ? 4 : 0)).addBox(-8.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51 + (sign.isIcon6() ? 4 : 0)).addBox(-4.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51 + (sign.isIcon7() ? 4 : 0)).addBox(1.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51 + (sign.isIcon8() ? 4 : 0)).addBox(5.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		main.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}