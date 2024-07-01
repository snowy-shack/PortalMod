package net.portalmod.common.sorted.faithplate;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.portalmod.client.animation.AnimatedModel;
import net.portalmod.core.init.AnimationInit;

public class FaithPlatePlateModel extends AnimatedModel {
    protected final ModelRenderer bone;
    protected final ModelRenderer bb_main;

    public FaithPlatePlateModel() {
        super(64, 64);
        
        bone = new ModelRenderer(this);
        bone.setPos(-2.5F, 29.5F, -1.5F);
        setRotationAngle(bone, -0.2182F, 0.0F, 0.0F);

        attachAnimation("launch", bone, XROT, AnimationInit.FAITHPLATE_BONE);

        {
            ModelRenderer handles_r1 = new ModelRenderer(this);
            handles_r1.setPos(3.5F, 1.1778F, -2.9707F);
            bone.addChild(handles_r1);
            setRotationAngle(handles_r1, 0.2182F, 0.0F, 0.0F);
            handles_r1.texOffs(10, 0).addBox(-5.0F, -4.6778F, -3.0293F, 8.0F, 7.0F, 18.0F, 0.0F, false);

            ModelRenderer arm = new ModelRenderer(this);
            arm.setPos(2.5F, -2.4837F, 8.3688F);
            bone.addChild(arm);
            setRotationAngle(arm, 0.3491F, 0.0F, 0.0F);

            attachAnimation("launch", arm, XROT, AnimationInit.FAITHPLATE_ARM);

            {
                ModelRenderer weight_r1 = new ModelRenderer(this);
                weight_r1.setPos(0.0F, -0.1033F, 0.1286F);
                arm.addChild(weight_r1);
                setRotationAngle(weight_r1, -0.1309F, 0.0F, 0.0F);
                weight_r1.texOffs(0, 0).addBox(-3.0F, -3.8809F, -4.114F, 6.0F, 8.0F, 8.0F, 0.0F, false);

                ModelRenderer lock = new ModelRenderer(this);
                lock.setPos(-2.5F, -1.0671F, 0.2114F);
                arm.addChild(lock);
                setRotationAngle(lock, -0.1309F, 0.0F, 0.0F);
                lock.texOffs(44, 0).addBox(0.0F, -5.3019F, -0.2783F, 5.0F, 2.0F, 4.0F, 0.0F, false);

                attachAnimation("launch", lock, XROT, AnimationInit.FAITHPLATE_LOCK);

//                {
//                    ModelRenderer lock_r1 = new ModelRenderer(this);
//                    lock_r1.setPos(2.5F, -3.7885F, 4.2513F);
//                    lock.addChild(lock_r1);
//                    setRotationAngle(lock_r1, -0.1309F, 0.0F, 0.0F); // -0.6545F x
//                    lock_r1.texOffs(44, 0).addBox(-2.5F, 0.3497F, -3.3566F, 5.0F, 2.0F, 4.0F, 0.0F, false);
//                }

                ModelRenderer plate = new ModelRenderer(this);
                plate.setPos(-2.0F, -3.0013F, 2.2893F);
                arm.addChild(plate);
                setRotationAngle(plate, 0.2618F, 0.0F, 0.0F);

                attachAnimation("launch", plate, XROT, AnimationInit.FAITHPLATE_PLATE);

                {
                    ModelRenderer plate_beam_r1 = new ModelRenderer(this);
                    plate_beam_r1.setPos(2.5F, -5.3086F, -3.081F);
                    plate.addChild(plate_beam_r1);
                    setRotationAngle(plate_beam_r1, -0.3927F, 0.0F, 0.0F);
                    plate_beam_r1.texOffs(0, 25).addBox(-2.0F, 3.0F, -10.0F, 3.0F, 3.0F, 9.0F, 0.0F, false);
                    plate_beam_r1.texOffs(4, 25).addBox(-5.0F, 1.0F, -16.0F, 9.0F, 2.0F, 20.0F, 0.0F, false);
                }
            }
        }

        bb_main = new ModelRenderer(this);
        bb_main.setPos(0.0F, 24.0F, 0.0F);

        {
            ModelRenderer glass_r1 = new ModelRenderer(this);
            glass_r1.setPos(0.0F, 0.0F, 0.0F);
            bb_main.addChild(glass_r1);
            setRotationAngle(glass_r1, 0.0F, 1.5708F, 0.0F);
            glass_r1.texOffs(-14, 47).addBox(-15.0F, 0.0F, -7.0F, 30.0F, 0.0F, 14.0F, 0.0F, false);
        }
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
//        bone.xRot = red;
//        arm.xRot = green;
//        plate.xRot = blue;
//        lock.xRot = alpha;
        
        bone.render(matrixStack, buffer, packedLight, packedOverlay);
        bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
    }
    
    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}