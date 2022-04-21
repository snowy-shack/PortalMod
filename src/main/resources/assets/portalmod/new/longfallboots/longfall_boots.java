// Made with Blockbench 3.9.2
// Exported for Minecraft version 1.15 - 1.16 with MCP mappings
// Paste this class into your mod and generate all required imports


public class custom_model extends EntityModel<Entity> {
	private final ModelRenderer LeftLeg;
	private final ModelRenderer RightLeg;

	public custom_model() {
		textureWidth = 64;
		textureHeight = 32;

		LeftLeg = new ModelRenderer(this);
		LeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		LeftLeg.setTextureOffset(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.75F, false);
		LeftLeg.setTextureOffset(14, 14).addBox(-2.0F, 4.0F, 3.5F, 4.0F, 7.0F, 2.0F, 0.75F, false);

		RightLeg = new ModelRenderer(this);
		RightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		RightLeg.setTextureOffset(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.75F, true);
		RightLeg.setTextureOffset(14, 14).addBox(-2.0F, 4.0F, 3.5F, 4.0F, 7.0F, 2.0F, 0.75F, true);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		LeftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		RightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}