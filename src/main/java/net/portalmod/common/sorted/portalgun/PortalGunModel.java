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
        super(32, 16);

        gun = new ModelRenderer(this);
        {
            ModelRenderer gun_front = new ModelRenderer(this);
            gun_front.setPos(0.0F, 24.0F, 0.0F);
            gun_front.texOffs(16, 5).addBox(-2.0F, -3.5F, -3.0F, 4.0F, 3.0F, 4.0F, 0.0F, false);
            gun_front.texOffs(0, 2).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
            gun_front.texOffs(23, 12).addBox(-1.5F, -4.5F, -2.0F, 3.0F, 3.0F, 1.0F, 0.0F, false);
            gun_front.texOffs(16, 8).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 1.0F, 0.0F, 0.0F, false);

            ModelRenderer prong_right = new ModelRenderer(this);
            prong_right.setPos(-1.2985F, -2.2491F, -2.5F);
            gun_front.addChild(prong_right);
            setRotationAngle(prong_right, -1.5708F, 0.0F, -2.0944F);
            prong_right.texOffs(0, 4).addBox(0.067F, -0.5F, -2.616F, 0.0F, 5.0F, 3.0F, 0.0F, true);

            ModelRenderer prong_left = new ModelRenderer(this);
            prong_left.setPos(1.2985F, -2.2491F, -2.5F);
            gun_front.addChild(prong_left);
            setRotationAngle(prong_left, -1.5708F, 0.0F, 2.0944F);
            prong_left.texOffs(0, 4).addBox(-0.067F, -0.5F, -2.616F, 0.0F, 5.0F, 3.0F, 0.0F, false);

            ModelRenderer prong_top = new ModelRenderer(this);
            prong_top.setPos(0.0F, -4.0F, -2.0F);
            gun_front.addChild(prong_top);
            setRotationAngle(prong_top, -1.5708F, 0.0F, 0.0F);
            prong_top.texOffs(0, 4).addBox(0.0F, -0.5F, -3.0F, 0.0F, 5.0F, 3.0F, 0.0F, false);

            ModelRenderer gun_base = new ModelRenderer(this);
            gun_base.setPos(0.0F, 24.0F, 0.0F);
            gun_base.texOffs(0, 6).addBox(-2.5F, -5.5F, 2.0F, 5.0F, 4.0F, 6.0F, 0.0F, false);
            gun_base.texOffs(8, 1).addBox(-1.5F, -3.0F, 4.0F, 3.0F, 2.0F, 3.0F, 0.0F, false);

            ModelRenderer bottom = new ModelRenderer(this);
            bottom.setPos(0.0F, -2.0F, 2.5F);
            gun_base.addChild(bottom);
            setRotationAngle(bottom, 0.0F, 3.1416F, 0.0F);
            bottom.texOffs(8, 1).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, false);

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
            colour_front.setPos(0.0F, 24.0F, 0.0F);
            colour_front.texOffs(0, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 3.0F, 0.0F, false); //color_tube
            colour_front.texOffs(8, 1).addBox(-0.5F, -3.5F, -2.0F, 1.0F, 1.0F, 0.0F, 0.0F, false); //color_front

            ModelRenderer colour_base = new ModelRenderer(this);
            colour_base.setPos(0.0F, 24.0F, 0.0F);
            colour_base.texOffs(-2, 0).addBox(-0.5F, -5.5F, 3.0F, 1.0F, 0.0F, 2.0F, 0.0F, false); //color_top

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
            accent_1.texOffs(18, 0).addBox(-3.0F, -1.5F, -0.5F, 6.0F, 3.0F, 1.0F, 0.004F, true);

            ModelRenderer accent_2 = new ModelRenderer(this);
            accent_2.setPos(2.0F, -4.0F, 5.0F);
            stripes_base.addChild(accent_2);
            setRotationAngle(accent_2, 0.0F, -1.5708F, 0.0F);
            accent_2.texOffs(18, 0).addBox(-3.0F, -1.5F, -0.5F, 6.0F, 3.0F, 1.0F, 0.004F, false);

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