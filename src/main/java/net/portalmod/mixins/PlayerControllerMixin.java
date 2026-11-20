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

    @Inject(
                        method = "sendBlockAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    private void pmSendBreakProof(ServerboundPlayerActionPacket.Action action, BlockPos pos, Direction direction, CallbackInfo info) {
        List<PortalEntity> portalChain = PortalRenderer.getInstance().outlineRenderingPortalChain;

        if(portalChain != null)
            PacketInit.INSTANCE.sendToServer(new CThroughPortalProofPacket(portalChain.stream().mapToInt(Entity::getId).toArray()));
    }

    @Inject(
                        method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    private void pmSendPlaceProof(LocalPlayer player, ClientLevel level, InteractionHand hand, BlockHitResult rayTrace, CallbackInfoReturnable<InteractionResult> info) {
        List<PortalEntity> portalChain = PortalRenderer.getInstance().outlineRenderingPortalChain;

        if(portalChain != null)
            PacketInit.INSTANCE.sendToServer(new CThroughPortalProofPacket(portalChain.stream().mapToInt(Entity::getId).toArray()));
    }
}