package io.github.serialsniper.portalmod.mixins;

import io.github.serialsniper.portalmod.core.init.ItemInit;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(at = @At(value = "RETURN"), method = "getArmPose(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/renderer/entity/model/BipedModel$ArmPose;", cancellable = true)
    private static void onGetArmPose(AbstractClientPlayerEntity player, Hand hand, CallbackInfoReturnable<BipedModel.ArmPose> info) {
        if(info.getReturnValue() == BipedModel.ArmPose.ITEM && player.getItemInHand(hand).getItem() == ItemInit.PORTALGUN.get())
            info.setReturnValue(BipedModel.ArmPose.CROSSBOW_HOLD);
    }
}