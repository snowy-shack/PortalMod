package io.github.serialsniper.portalmod.client.render;

import com.mojang.blaze3d.matrix.*;
import net.minecraft.client.*;
import net.minecraft.client.entity.player.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.*;

public class PortalFirstPersonRenderer {
    private static int swingTime = 0;
    private static float oAttackAnim = 0;
    private static float attackAnim = 0;
    public static boolean swinging = false;

    public static void renderItem(LivingEntity p_228397_1_, ItemStack p_228397_2_, ItemCameraTransforms.TransformType p_228397_3_, boolean p_228397_4_, MatrixStack p_228397_5_, IRenderTypeBuffer p_228397_6_, int p_228397_7_) {
        if (!p_228397_2_.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().renderStatic(p_228397_1_, p_228397_2_, p_228397_3_, p_228397_4_, p_228397_5_, p_228397_6_, p_228397_1_.level, p_228397_7_, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void applyItemArmTransform(MatrixStack p_228406_1_, HandSide p_228406_2_, float p_228406_3_) {
        int i = p_228406_2_ == HandSide.RIGHT ? 1 : -1;
        p_228406_1_.translate((double)((float)i * 0.56F), (double)(-0.52F), (double)-0.72F);
    }

    public static void updateSwingTime() {
        oAttackAnim = attackAnim;

        int duration = 1;

        if(Minecraft.getInstance().player != null) {
            duration = (int)(Minecraft.getInstance().player.getCurrentSwingDuration() * 1.5f);

            if(Minecraft.getInstance().player.swinging && swinging == false)
                swinging = true;
        }

        if(swinging) {
            if(++swingTime >= duration) {
                swingTime = 0;
                swinging = false;
            }
        } else {
            swingTime = 0;
        }

        attackAnim = (float)swingTime / (float)duration;
    }

    public static float getAttackAnim(float partialTicks) {
        float f = attackAnim - oAttackAnim;

        if(f < 0.0F) f++;

        return oAttackAnim + f * partialTicks;
    }

    public static void renderArmWithItem(AbstractClientPlayerEntity player, float partialTicks, float interpolatedPitch, Hand hand,
                                         float swingProgress, ItemStack stack, float equipProgress, MatrixStack transform, IRenderTypeBuffer buffers, int light) {

        float actualSwingProgress = getAttackAnim(partialTicks);

        HandSide handside = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        transform.pushPose();

        boolean flag3 = handside == HandSide.RIGHT;

        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            int k = flag3 ? 1 : -1;
            applyItemArmTransform(transform, handside, equipProgress);

        } else if (player.isAutoSpinAttack()) {
            applyItemArmTransform(transform, handside, equipProgress);
            int j = flag3 ? 1 : -1;
            transform.translate(((float)j * -0.4F), 0.8F, 0.3F);
            transform.mulPose(Vector3f.YP.rotationDegrees((float)j * 65.0F));
            transform.mulPose(Vector3f.ZP.rotationDegrees((float)j * -85.0F));

        } else {
//            float animation = MathHelper.sin(actualSwingProgress * (float)Math.PI);
            float animation = MathHelper.sin((actualSwingProgress * 2 - 0.5f) * (float)Math.PI) / 2 + 0.5f;
            transform.translate(0, 0, animation * 0.3);

//            float rotationAnimation = MathHelper.sin((actualSwingProgress - 0.5f) * 2 * (float)Math.PI);
            float rotationAnimation = MathHelper.sin((actualSwingProgress * 4 - 0.5f) * (float)Math.PI) / 2 + 0.5f;
            transform.mulPose(Vector3f.XP.rotationDegrees(animation * 7));

            if(actualSwingProgress >= 0.5)
                transform.mulPose(Vector3f.YP.rotationDegrees(rotationAnimation * 4));

            applyItemArmTransform(transform, handside, equipProgress);
        }

        renderItem(player, stack, flag3 ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag3, transform, buffers, light);

        transform.popPose();
    }
}