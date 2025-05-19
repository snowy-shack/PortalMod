package net.portalmod.common.sorted.portalgun;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.portalmod.client.animation.AnimatedModel;
import net.portalmod.core.init.AnimationInit;

public class PortalGunModel extends AnimatedModel {
    protected final ModelRenderer gun;
    protected final ModelRenderer colour;
    protected final ModelRenderer stripes;

    // todo animations for left hand

    public PortalGunModel() {
        super(32, 32); // New texture

        gun = new ModelRenderer(this);
        {
            ModelRenderer gun_front = new ModelRenderer(this); // TODO DONE
            gun_front.setPos(0.0F, 24.0F, 0.0F);
            gun_front.texOffs(16, 13).addBox(-2.0F, -3.5F, -3.0F, 4.0F, 3.0F, 4.0F, 0.0F, false);
            gun_front.texOffs(18, 22).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
            gun_front.texOffs(11, 12).addBox(-1.5F, -4.5F, -2.0F, 3.0F, 3.0F, 1.0F, 0.0F, false);
            gun_front.texOffs(21, 20).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);

            ModelRenderer prong_right = new ModelRenderer(this); // TODO DONE ??
            prong_right.setPos(-1.2985F, -2.2491F, -2.5F);
            gun_front.addChild(prong_right);
            setRotationAngle(prong_right, -1.5708F, 0.0F, -2.0944F);
            prong_right.texOffs(11, 3).addBox(0.067F, -0.5F, -2.616F, 0.0F, 5.0F, 3.0F, 0.0F, true);

            ModelRenderer prong_left = new ModelRenderer(this); // TODO DONE ??
            prong_left.setPos(1.2985F, -2.2491F, -2.5F);
            gun_front.addChild(prong_left);
            setRotationAngle(prong_left, -1.5708F, 0.0F, 2.0944F);
            prong_left.texOffs(18, 3).addBox(-0.067F, -0.5F, -2.616F, 0.0F, 5.0F, 3.0F, 0.0F, false);

            ModelRenderer prong_top = new ModelRenderer(this); // TODO DONE ??
            prong_top.setPos(0.0F, -4.0F, -2.0F);
            gun_front.addChild(prong_top);
            setRotationAngle(prong_top, -1.5708F, 0.0F, 0.0F);
            prong_top.texOffs(25, 3).addBox(0.0F, -0.5F, -3.0F, 0.0F, 5.0F, 3.0F, 0.0F, false);


            // position of whole patat
            ModelRenderer potatoParent = new ModelRenderer(this);
            potatoParent.setPos(0f, 2f, -3f);
            prong_top.addChild(potatoParent);
            setRotationAngle(potatoParent, 1.7F, 0.0F, 0.4F);

            // the potato itself
            ModelRenderer potato = new ModelRenderer(this);
            potato.setPos(-0.2206F, -0.0222F, -0.3924F);
            potatoParent.addChild(potato);
            setRotationAngle(potato, -0.8248F, -0.6669F, 0.9374F);
            potato.texOffs(21, 1).addBox(-1.3119F, -1.0513F, -1.1565F, 3.0F, 2.0F, 2.0F, 0.0F, false);

            // the transparent plane
            ModelRenderer potatoWires = new ModelRenderer(this);
            potatoWires.setPos(-0.2018F, -0.067F, -0.7244F);
            potatoParent.addChild(potatoWires);
            setRotationAngle(potatoWires, 2.5876F, -0.0168F, -2.7786F);
            potatoWires.texOffs(12, 1).addBox(-2.3359F, -1.9776F, -0.3457F, 4.0F, 4.0F, 0.0F, 0.0F, false);

//            prong_top.addChild(potatoParent);


            ModelRenderer gun_base = new ModelRenderer(this); // ?? - TODO DONE
            gun_base.setPos(0.0F, 24.0F, 0.0F);
            gun_base.texOffs(1, 21).addBox(-2.5F, -5.5F, 2.0F, 5.0F, 4.0F, 6.0F, 0.0F, false); // Big white back
            gun_base.texOffs(1, 14).addBox(-1.5F, -3.0F, 4.0F, 3.0F, 2.0F, 3.0F, 0.0F, false);

            ModelRenderer bottom = new ModelRenderer(this);
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

        colour = new ModelRenderer(this);
        {
            ModelRenderer colour_front = new ModelRenderer(this);
            colour_front.setPos(0.0F, 24.0F, 0.0F); // 1: The glass tube, 2: Operational end of the device
            colour_front.texOffs(1, 6).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 3.0F, 0.0F, false); //color_tube
            colour_front.texOffs(9, 7).addBox(-0.5F, -3.5F, -2.0F, 1.0F, 1.0F, 0.0F, 0.0F, false); //color_front

            ModelRenderer colour_base = new ModelRenderer(this);
            colour_base.setPos(0.0F, 24.0F, 0.0F); // Light at the top
            colour_base.texOffs(1, 12).addBox(-0.5F, -5.5F, 3.0F, 1.0F, 0.0F, 2.0F, 0.0F, false); //color_top

            colour.addChild(colour_front);
            colour.addChild(colour_base);

            attachAnimation("shoot", colour_front, Z, AnimationInit.COMPRESSION);
            attachAnimation("lift",  colour_front, Z, AnimationInit.COMPRESSION_START);
            attachAnimation("drop",  colour_front, Z, AnimationInit.COMPRESSION_STOP);
        }

        stripes = new ModelRenderer(this);
        {
            ModelRenderer stripes_base = new ModelRenderer(this);
            stripes_base.setPos(0.0F, 24.0F, 0.0F);

            ModelRenderer accent_1 = new ModelRenderer(this);
            accent_1.setPos(-2.0F, -4.0F, 5.0F);
            stripes_base.addChild(accent_1);
            setRotationAngle(accent_1, 0.0F, 1.5708F, 0.0F);
            accent_1.texOffs(1, 1).addBox(-3.0F, -1.5F, -0.5F, 6.0F, 3.0F, 1.0F, 0.004F, true);

            ModelRenderer accent_2 = new ModelRenderer(this);
            accent_2.setPos(2.0F, -4.0F, 5.0F);
            stripes_base.addChild(accent_2);
            setRotationAngle(accent_2, 0.0F, -1.5708F, 0.0F);
            accent_2.texOffs(1, 1).addBox(-3.0F, -1.5F, -0.5F, 6.0F, 3.0F, 1.0F, 0.004F, false);

            stripes.addChild(stripes_base);
        }
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        gun.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}