package net.portalmod.client.render;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class PortalFirstPersonRenderer {
    private static int swingTime = 0;
    private static float oAttackAnim = 0;
    private static float attackAnim = 0;
    public static boolean swinging = false;

    public static void renderItem(LivingEntity p_228397_1_, ItemStack p_228397_2_, ItemTransforms.TransformType p_228397_3_, boolean p_228397_4_, PoseStack p_228397_5_, MultiBufferSource p_228397_6_, int p_228397_7_) {
        if (!p_228397_2_.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().renderStatic(p_228397_1_, p_228397_2_, p_228397_3_, p_228397_4_, p_228397_5_, p_228397_6_, p_228397_1_.level(), p_228397_7_, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void applyItemArmTransform(PoseStack p_228406_1_, HumanoidArm p_228406_2_, float p_228406_3_) {
        int i = p_228406_2_ == HumanoidArm.RIGHT ? 1 : -1;
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

    public static void renderArmWithItem(AbstractClientPlayer player, float partialTicks, float interpolatedPitch, InteractionHand hand,
                                         float swingProgress, ItemStack stack, float equipProgress, PoseStack transform, MultiBufferSource buffers, int light) {

        float actualSwingProgress = getAttackAnim(partialTicks);

        HumanoidArm handside = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        transform.pushPose();

        boolean flag3 = handside == HumanoidArm.RIGHT;

        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            int k = flag3 ? 1 : -1;
            applyItemArmTransform(transform, handside, equipProgress);

        } else if (player.isAutoSpinAttack()) {
            applyItemArmTransform(transform, handside, equipProgress);
            int j = flag3 ? 1 : -1;
            transform.translate(((float)j * -0.4F), 0.8F, 0.3F);
            transform.mulPose(Axis.YP.rotationDegrees((float)j * 65.0F));
            transform.mulPose(Axis.ZP.rotationDegrees((float)j * -85.0F));

        } else {
//            float animation = Mth.sin(actualSwingProgress * (float)Math.PI);
            float animation = Mth.sin((actualSwingProgress * 2 - 0.5f) * (float)Math.PI) / 2 + 0.5f;
            transform.translate(0, 0, animation * 0.3);

//            float rotationAnimation = Mth.sin((actualSwingProgress - 0.5f) * 2 * (float)Math.PI);
            float rotationAnimation = Mth.sin((actualSwingProgress * 4 - 0.5f) * (float)Math.PI) / 2 + 0.5f;
            transform.mulPose(Axis.XP.rotationDegrees(animation * 7));

            if(actualSwingProgress >= 0.5)
                transform.mulPose(Axis.YP.rotationDegrees(rotationAnimation * 4));

            applyItemArmTransform(transform, handside, equipProgress);
        }

        renderItem(player, stack, flag3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag3, transform, buffers, light);

        transform.popPose();
    }
}