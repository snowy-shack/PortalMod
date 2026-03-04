package net.portalmod.common.sorted.autoportal;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class AutoPortalModel extends EntityModel<Entity> {
	public final ModelRenderer wawas;
	public final ModelRenderer frame;

	public AutoPortalModel() {
		texWidth = 64;
		texHeight = 64;

		wawas = new ModelRenderer(this);
		wawas.setPos(0.0F, 24.0F, 0.0F);
		wawas.texOffs(17, 1).addBox(7.0F, -14.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, true);
		wawas.texOffs(17, 4).addBox(7.0F, -9.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, true);
		wawas.texOffs(17, 7).addBox(7.0F, -4.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, true);
		wawas.texOffs(17, 7).addBox(7.0F, 2.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, true);
		wawas.texOffs(17, 10).addBox(7.0F, 7.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, true);
		wawas.texOffs(17, 13).addBox(7.0F, 12.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, true);
		wawas.texOffs(17, 1).addBox(-11.0F, -14.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, false);
		wawas.texOffs(17, 4).addBox(-11.0F, -9.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, false);
		wawas.texOffs(17, 7).addBox(-11.0F, -4.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, false);
		wawas.texOffs(17, 7).addBox(-11.0F, 2.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, false);
		wawas.texOffs(17, 10).addBox(-11.0F, 7.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, false);
		wawas.texOffs(17, 13).addBox(-11.0F, 12.0F, 0.0F, 4.0F, 2.0F, 0.0F, 0.0F, false);

		frame = new ModelRenderer(this);
		frame.setPos(-11.0F, 40.0F, 1.05F);
		frame.texOffs(1, 1).addBox(-2.0F, -32.0F, -1.05F, 2.0F, 32.0F, 1.0F, 0.0F, false);
		frame.texOffs(8, 1).addBox(0.0F, -32.0F, -1.05F, 1.0F, 32.0F, 1.0F, 0.0F, false);
		frame.texOffs(1, 1).addBox(22.0F, -32.0F, -1.05F, 2.0F, 32.0F, 1.0F, 0.0F, true);
		frame.texOffs(8, 1).addBox(21.0F, -32.0F, -1.05F, 1.0F, 32.0F, 1.0F, 0.0F, true);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		wawas.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		frame.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}