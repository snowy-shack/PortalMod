package net.portalmod.mixins;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
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

@Mixin(PlayerController.class)
public class PlayerControllerMixin {

    @Inject(
                        method = "sendBlockAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/play/ClientPlayNetHandler;send(Lnet/minecraft/network/IPacket;)V"
            )
    )
    private void pmSendBreakProof(CPlayerDiggingPacket.Action action, BlockPos pos, Direction direction, CallbackInfo info) {
        List<PortalEntity> portalChain = PortalRenderer.getInstance().outlineRenderingPortalChain;

        if(portalChain != null)
            PacketInit.INSTANCE.sendToServer(new CThroughPortalProofPacket(portalChain.stream().mapToInt(Entity::getId).toArray()));
    }

    @Inject(
                        method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/play/ClientPlayNetHandler;send(Lnet/minecraft/network/IPacket;)V"
            )
    )
    private void pmSendPlaceProof(ClientPlayerEntity player, ClientWorld level, Hand hand, BlockRayTraceResult rayTrace, CallbackInfoReturnable<ActionResultType> info) {
        List<PortalEntity> portalChain = PortalRenderer.getInstance().outlineRenderingPortalChain;

        if(portalChain != null)
            PacketInit.INSTANCE.sendToServer(new CThroughPortalProofPacket(portalChain.stream().mapToInt(Entity::getId).toArray()));
    }
}