package net.portalmod.common.sorted.portalgun;

import net.portalmod.client.animation.AnimatedModel;
import net.portalmod.core.init.AnimationInit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

public class PortalGunModel extends AnimatedModel {
    protected final ModelPart gun;
    protected final ModelPart colour;
    protected final ModelPart stripes;

    // todo animations for left hand

    public PortalGunModel() {
        super(32, 32); // New texture

        gun = new ModelPart(this);
        {
            ModelPart gun_front = new ModelPart(this); // TODO DONE
            gun_front.setPos(0.0F, 24.0F, 0.0F);
            gun_front.texOffs(16, 13).addBox(-2.0F, -3.5F, -3.0F, 4.0F, 3.0F, 4.0F, 0.0F, false);
            gun_front.texOffs(18, 22).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
            gun_front.texOffs(11, 12).addBox(-1.5F, -4.5F, -2.0F, 3.0F, 3.0F, 1.0F, 0.0F, false);
            gun_front.texOffs(21, 20).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);

            ModelPart prong_right = new ModelPart(this); // TODO DONE ??
            prong_right.setPos(-1.2985F, -2.2491F, -2.5F);
            gun_front.addChild(prong_right);
            setRotationAngle(prong_right, -1.5708F, 0.0F, -2.0944F);
            prong_right.texOffs(11, 3).addBox(0.067F, -0.5F, -2.616F, 0.0F, 5.0F, 3.0F, 0.0F, true);

            ModelPart prong_left = new ModelPart(this); // TODO DONE ??
            prong_left.setPos(1.2985F, -2.2491F, -2.5F);
            gun_front.addChild(prong_left);
            setRotationAngle(prong_left, -1.5708F, 0.0F, 2.0944F);
            prong_left.texOffs(18, 3).addBox(-0.067F, -0.5F, -2.616F, 0.0F, 5.0F, 3.0F, 0.0F, false);

            ModelPart prong_top = new ModelPart(this); // TODO DONE ??
            prong_top.setPos(0.0F, -4.0F, -2.0F);
            gun_front.addChild(prong_top);
            setRotationAngle(prong_top, -1.5708F, 0.0F, 0.0F);
            prong_top.texOffs(25, 3).addBox(0.0F, -0.5F, -3.0F, 0.0F, 5.0F, 3.0F, 0.0F, false);


            // position of whole patat
            ModelPart potatoParent = new ModelPart(this);
            potatoParent.setPos(0f, 2f, -3f);
            prong_top.addChild(potatoParent);
            setRotationAngle(potatoParent, 1.7F, 0.0F, 0.4F);

            // the potato itself
            ModelPart potato = new ModelPart(this);
            potato.setPos(-0.2206F, -0.0222F, -0.3924F);
            potatoParent.addChild(potato);
            setRotationAngle(potato, -0.8248F, -0.6669F, 0.9374F);
            potato.texOffs(21, 1).addBox(-1.3119F, -1.0513F, -1.1565F, 3.0F, 2.0F, 2.0F, 0.0F, false);

            // the transparent plane
            ModelPart potatoWires = new ModelPart(this);
            potatoWires.setPos(-0.2018F, -0.067F, -0.7244F);
            potatoParent.addChild(potatoWires);
            setRotationAngle(potatoWires, 2.5876F, -0.0168F, -2.7786F);
            potatoWires.texOffs(12, 1).addBox(-2.3359F, -1.9776F, -0.3457F, 4.0F, 4.0F, 0.0F, 0.0F, false);

//            prong_top.addChild(potatoParent);


            ModelPart gun_base = new ModelPart(this); // ?? - TODO DONE
            gun_base.setPos(0.0F, 24.0F, 0.0F);
            gun_base.texOffs(1, 21).addBox(-2.5F, -5.5F, 2.0F, 5.0F, 4.0F, 6.0F, 0.0F, false); // Big white back
            gun_base.texOffs(1, 14).addBox(-1.5F, -3.0F, 4.0F, 3.0F, 2.0F, 3.0F, 0.0F, false);

            ModelPart bottom = new ModelPart(this);
            bottom.setPos(0.0F, -2.0F, 2.5F);
            gun_base.addChild(bottom);
            setRotationAngle(bottom, 0.0F, 3.1416F, 0.0F); // TODO duplicates???
            bottom.texOffs(1, 14).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, false);

            gun.addChild(gun_front);
            gun.addChild(gun_base);

            attachAnimation("shoot", gun_front,   Z,    AnimationInit.COMPRESSION);
            attachAnimation("shoot", prong_right, XROT, AnimationInit.CLAWS);
            attachAnimation("shoot", prong_left,  XROT, AnimationInit.CLAWS);
            attachAnimation("shoot", prong_top,   XROT, AnimationInit.CLAWS);

            attachAnimation("lift", gun_front,   Z,    AnimationInit.COMPRESSION_START);
            attachAnimation("lift", prong_right, XROT, AnimationInit.CLAWS_OPEN);
            attachAnimation("lift", prong_left,  XROT, AnimationInit.CLAWS_OPEN);
            attachAnimation("lift", prong_top,   XROT, AnimationInit.CLAWS_OPEN);

            attachAnimation("drop", gun_front,   Z,    AnimationInit.COMPRESSION_STOP);
            attachAnimation("drop", prong_right, XROT, AnimationInit.CLAWS_CLOSE);
            attachAnimation("drop", prong_left,  XROT, AnimationInit.CLAWS_CLOSE);
            attachAnimation("drop", prong_top,   XROT, AnimationInit.CLAWS_CLOSE);
        }

        colour = new ModelPart(this);
        {
            ModelPart colour_front = new ModelPart(this);
            colour_front.setPos(0.0F, 24.0F, 0.0F); // 1: The glass tube, 2: Operational end of the device
            colour_front.texOffs(1, 6).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 3.0F, 0.0F, false); //color_tube
            colour_front.texOffs(9, 7).addBox(-0.5F, -3.5F, -2.0F, 1.0F, 1.0F, 0.0F, 0.0F, false); //color_front

            ModelPart colour_base = new ModelPart(this);
            colour_base.setPos(0.0F, 24.0F, 0.0F); // Light at the top
            colour_base.texOffs(-1, 12).addBox(-0.5F, -5.5F, 3.0F, 1.0F, 0.0F, 2.0F, 0.0F, false); //color_top

            colour.addChild(colour_front);
            colour.addChild(colour_base);

            attachAnimation("shoot", colour_front, Z, AnimationInit.COMPRESSION);
            attachAnimation("lift",  colour_front, Z, AnimationInit.COMPRESSION_START);
            attachAnimation("drop",  colour_front, Z, AnimationInit.COMPRESSION_STOP);
        }

        stripes = new ModelPart(this);
        {
            ModelPart stripes_base = new ModelPart(this);
            stripes_base.setPos(0.0F, 24.0F, 0.0F);

            ModelPart accent_1 = new ModelPart(this);
            accent_1.setPos(-2.0F, -4.0F, 5.0F);
            stripes_base.addChild(accent_1);
            setRotationAngle(accent_1, 0.0F, 1.5708F, 0.0F);
            accent_1.texOffs(1, 1).addBox(-3.0F, -1.5F, -0.5F, 6.0F, 3.0F, 1.0F, 0.004F, true);

            ModelPart accent_2 = new ModelPart(this);
            accent_2.setPos(2.0F, -4.0F, 5.0F);
            stripes_base.addChild(accent_2);
            setRotationAngle(accent_2, 0.0F, -1.5708F, 0.0F);
            accent_2.texOffs(1, 1).addBox(-3.0F, -1.5F, -0.5F, 6.0F, 3.0F, 1.0F, 0.004F, false);

            stripes.addChild(stripes_base);
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        gun.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}