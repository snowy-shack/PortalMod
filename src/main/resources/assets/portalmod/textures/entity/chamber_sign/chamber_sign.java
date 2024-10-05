// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


public class chamber_sign extends EntityModel<Entity> {
	private final ModelRenderer bb_main;

	public chamber_sign() {
		texWidth = 128;
		texHeight = 64;

		bb_main = new ModelRenderer(this);
		main.setPos(0.0F, 24.0F, 0.0F);
		main.texOffs(1, 1).addBox(-12.0F, -24.0F, -1.0F, 24.0F, 48.0F, 2.0F, 0.0F, false);
		main.texOffs(8, 51).addBox(-9.0F, 4.0F, -1.01F, 18.0F, 2.0F, 1.0F, 0.0F, false);
		main.texOffs(53, 0).addBox(-9.0F, -17.0F, -1.01F, 8.0F, 18.0F, 1.0F, 0.0F, false);
		main.texOffs(63, 0).addBox(-1.0F, -17.0F, -1.01F, 8.0F, 18.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 55).addBox(-8.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51).addBox(-4.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 55).addBox(1.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 55).addBox(5.0F, 11.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 55).addBox(5.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51).addBox(1.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 55).addBox(-4.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
		main.texOffs(1, 51).addBox(-8.0F, 15.0F, -1.01F, 3.0F, 3.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}