package net.portalmod.mixins;

import net.portalmod.common.sorted.portal.CThroughPortalProofPacket;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalRenderer;
import net.portalmod.core.init.PacketInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(MultiPlayerGameMode.class)
public class PlayerControllerMixin {

    // 1.19+ sends block actions through startPrediction (sendBlockAction is gone); send the proof first.
    @Inject(method = "startPrediction", at = @At("HEAD"))
    private void pmSendBreakProof(net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.client.multiplayer.prediction.PredictiveAction action, CallbackInfo info) {
        List<PortalEntity> portalChain = PortalRenderer.getInstance().outlineRenderingPortalChain;

        if(portalChain != null)
            PacketInit.INSTANCE.sendToServer(new CThroughPortalProofPacket(portalChain.stream().mapToInt(Entity::getId).toArray()));
    }

    // useItemOn also goes through startPrediction in 1.19+, so pmSendBreakProof covers it.
}