package net.portalmod.common.sorted.autoportal;

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
import net.minecraft.world.entity.Entity;

public class AutoPortalModel extends EntityModel<Entity> {
	public final ModelPart wawas;
	public final ModelPart frame;
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(PortalMod.MODID, "auto_portal_model"), "main");

	/** Model geometry (1.17+ format), converted from the 1.16 ModelRenderer code. */
	public static LayerDefinition createBodyLayer() {
	    MeshDefinition mesh = new MeshDefinition();
	    PartDefinition root = mesh.getRoot();
	    root.addOrReplaceChild("wawas", CubeListBuilder.create().mirror(true).texOffs(17, 1).addBox(7.0F, -14.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 4).addBox(7.0F, -9.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 7).addBox(7.0F, -4.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 7).addBox(7.0F, 2.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 10).addBox(7.0F, 7.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 13).addBox(7.0F, 12.0F, 0.0F, 4.0F, 2.0F, 0.0F).mirror(false).texOffs(17, 1).addBox(-11.0F, -14.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 4).addBox(-11.0F, -9.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 7).addBox(-11.0F, -4.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 7).addBox(-11.0F, 2.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 10).addBox(-11.0F, 7.0F, 0.0F, 4.0F, 2.0F, 0.0F).texOffs(17, 13).addBox(-11.0F, 12.0F, 0.0F, 4.0F, 2.0F, 0.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));
	    root.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(1, 1).addBox(-2.0F, -32.0F, -1.05F, 2.0F, 32.0F, 1.0F).texOffs(8, 1).addBox(0.0F, -32.0F, -1.05F, 1.0F, 32.0F, 1.0F).mirror(true).texOffs(1, 1).addBox(22.0F, -32.0F, -1.05F, 2.0F, 32.0F, 1.0F).texOffs(8, 1).addBox(21.0F, -32.0F, -1.05F, 1.0F, 32.0F, 1.0F), PartPose.offsetAndRotation(-11.0F, 40.0F, 1.05F, 0.0F, 0.0F, 0.0F));
	    return LayerDefinition.create(mesh, 64, 64);
	}

	public AutoPortalModel() {
	    this(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
	}

	public AutoPortalModel(ModelPart root) {
	    this.wawas = root.getChild("wawas");
	    this.frame = root.getChild("frame");
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		wawas.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		frame.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}