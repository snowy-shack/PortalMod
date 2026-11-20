package net.portalmod.mixins.network;

import org.spongepowered.asm.mixin.injection.Constant;

import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.portalmod.common.sorted.portal.IClientTeleportable;
import net.portalmod.common.sorted.portal.PortalServerProofManager;
import net.portalmod.core.interfaces.ITeleportLerpable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetHandlerMixin {
    private BlockPos pmCapturedBlockPos;

    @ModifyVariable(
                        method = "handleMovePlayer",
            at = @At("STORE"),
            ordinal = 0
    )
    private boolean pmAvoidResettingFallDistance(boolean value) {
        IClientTeleportable player = ((IClientTeleportable)((ServerGamePacketListenerImpl)(Object)this).player);
        if(player.getJustPortaled()) {
            return false;
        }
        return value;
    }

    @Inject(
                        method = "handleMovePlayer",
            at = @At("RETURN")
    )
    private void pmClearPortalMovementState(CallbackInfo info) {
        ((IClientTeleportable)((ServerGamePacketListenerImpl)(Object)this).player).setJustPortaled(false);
    }

    @Redirect(
                        method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isSleeping()Z",
                    ordinal = 2
            )
    )
    private boolean pmAllowPortalTeleportation(ServerPlayer player) {
        return true; // todo actual logic to check tp validity here
    }

    @Redirect(
                        method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isChangingDimension()Z"
            )
    )
    private boolean pmAllowPortalTeleportation2(ServerPlayer player) {
        if(((ITeleportLerpable)player).hasUsedPortal())
            return true;
        return player.isChangingDimension();
    }

    @Redirect(
                        method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/BlockHitResult;getBlockPos()Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos pmCaptureBlockPos(BlockHitResult instance) {
        this.pmCapturedBlockPos = instance.getBlockPos();
        return this.pmCapturedBlockPos;
    }

    @Redirect(
                        method = "handleUseItemOn",
            // 1.20.1 uses a fixed squared reach instead of Forge's reach attribute.
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            )
    )
    private double pmAllowThroughPortalBreakReach() {
        ServerGamePacketListenerImpl thiss = (ServerGamePacketListenerImpl)(Object)this;
        if(PortalServerProofManager.getInstance().hasBelievableProof(thiss.player, this.pmCapturedBlockPos, false))
            return Double.POSITIVE_INFINITY;

        return ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
    }

    /** Second vanilla check (player within 8 blocks of the block); same through-portal exception. */
    @ModifyConstant(method = "handleUseItemOn", constant = @Constant(doubleValue = 64.0))
    private double pmAllowThroughPortalUseDistance(double distanceSqr) {
        ServerGamePacketListenerImpl thiss = (ServerGamePacketListenerImpl)(Object)this;
        if(PortalServerProofManager.getInstance().hasBelievableProof(thiss.player, this.pmCapturedBlockPos, false))
            return Double.POSITIVE_INFINITY;
        return distanceSqr;
    }
}
