package net.portalmod.mixins.network;

import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.portalmod.common.sorted.portal.PortalServerProofManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

@Mixin(ServerPlayerGameMode.class)
public class PlayerInteractionManagerMixin {
    private BlockPos pmCapturedBlockPos;

    @Inject(
                        method = "handleBlockBreakAction",
            at = @At("HEAD")
    )
    private void pmCaptureBlockPos(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int buildLimit, int sequence, CallbackInfo info) {
        this.pmCapturedBlockPos = pos;
    }

    @Redirect(
                        method = "handleBlockBreakAction",
            // 1.20.1 compares against a fixed squared reach instead of Forge's reach attribute.
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            )
    )
    private double pmAllowThroughPortalBreakReach() {
        ServerPlayerGameMode thiss = (ServerPlayerGameMode)(Object)this;
        if(PortalServerProofManager.getInstance().hasBelievableProof(thiss.player, this.pmCapturedBlockPos, true))
            return Double.POSITIVE_INFINITY;

        return ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
    }
}