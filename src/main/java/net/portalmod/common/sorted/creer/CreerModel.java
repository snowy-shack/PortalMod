package net.portalmod.common.sorted.creer;

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


import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class CreerModel<T extends Entity> extends ListModel<T> {
    private final ModelPart head;
    private final ModelPart hair;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(PortalMod.MODID, "creer"), "main");
    public static final ModelLayerLocation ARMOR_LAYER = new ModelLayerLocation(new ResourceLocation(PortalMod.MODID, "creer"), "armor");

    /** Same shape as the 1.16 code; {@code grow} was the constructor argument (0 = body, 2 = charge layer). */
    public static LayerDefinition createBodyLayer(CubeDeformation grow) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, grow), PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, grow.extend(0.5F)), PartPose.offset(0.0F, 18.0F, 0.0F));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, grow);
        root.addOrReplaceChild("leg0", leg, PartPose.offset(-2.0F, 18.0F, 4.0F));
        root.addOrReplaceChild("leg1", leg, PartPose.offset(2.0F, 18.0F, 4.0F));
        root.addOrReplaceChild("leg2", leg, PartPose.offset(-2.0F, 18.0F, -4.0F));
        root.addOrReplaceChild("leg3", leg, PartPose.offset(2.0F, 18.0F, -4.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public CreerModel() {
        this(0.0F);
    }

    public CreerModel(float grow) {
        this(Minecraft.getInstance().getEntityModels().bakeLayer(grow == 0.0F ? LAYER : ARMOR_LAYER));
    }

    public CreerModel(ModelPart root) {
        this.head = root.getChild("head");
        this.hair = root.getChild("hair");
        this.leg0 = root.getChild("leg0");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
    }

    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head, this.leg0, this.leg1, this.leg2, this.leg3);
    }

    public void setupAnim(T p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
        this.head.yRot = p_225597_5_ * ((float)Math.PI / 180F);
        this.head.xRot = p_225597_6_ * ((float)Math.PI / 180F);
        this.leg0.xRot = Mth.cos(p_225597_2_ * 0.6662F) * 1.4F * p_225597_3_;
        this.leg1.xRot = Mth.cos(p_225597_2_ * 0.6662F + (float)Math.PI) * 1.4F * p_225597_3_;
        this.leg2.xRot = Mth.cos(p_225597_2_ * 0.6662F + (float)Math.PI) * 1.4F * p_225597_3_;
        this.leg3.xRot = Mth.cos(p_225597_2_ * 0.6662F) * 1.4F * p_225597_3_;
    }
}