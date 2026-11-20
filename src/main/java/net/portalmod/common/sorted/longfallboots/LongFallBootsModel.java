package net.portalmod.common.sorted.longfallboots;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.portalmod.PortalMod;

/** The long fall boots armor model: two shoes that follow the wearer's legs. */
@Environment(EnvType.CLIENT)
public class LongFallBootsModel extends Model {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(PortalMod.MODID, "longfall_boots"), "main");

    private final ModelPart left_shoe;
    private final ModelPart right_shoe;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        CubeDeformation grow = new CubeDeformation(0.75F);
        mesh.getRoot().addOrReplaceChild("left_shoe", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, grow)
                .texOffs(16, 7).addBox(-2.0F, 4.0F, 3.5F, 4.0F, 7.0F, 2.0F, grow),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        mesh.getRoot().addOrReplaceChild("right_shoe", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, grow)
                .texOffs(16, 7).addBox(-2.0F, 4.0F, 3.5F, 4.0F, 7.0F, 2.0F, grow),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 16);
    }

    public LongFallBootsModel() {
        this(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
    }

    public LongFallBootsModel(ModelPart root) {
        super(RenderType::armorCutoutNoCull);
        this.left_shoe = root.getChild("left_shoe");
        this.right_shoe = root.getChild("right_shoe");
    }

    /** Copies the leg poses from the wearer's model so the shoes move with the legs. */
    public void copyLegsFrom(HumanoidModel<LivingEntity> model) {
        this.left_shoe.copyFrom(model.leftLeg);
        this.right_shoe.copyFrom(model.rightLeg);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.left_shoe.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.right_shoe.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
