package net.portalmod.mixins.network;

import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.portalmod.common.sorted.portal.IClientTeleportable;
import net.portalmod.common.sorted.portal.PortalServerProofManager;
import net.portalmod.core.interfaces.ITeleportLerpable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin {
    private BlockPos pmCapturedBlockPos;

    @ModifyVariable(
                        method = "handleMovePlayer",
            at = @At("STORE"),
            ordinal = 0
    )
    private boolean pmAvoidResettingFallDistance(boolean value) {
        IClientTeleportable player = ((IClientTeleportable)((ServerPlayNetHandler)(Object)this).player);
        if(player.getJustPortaled()) {
            player.setJustPortaled(false);
            return false;
        }
        return value;
    }

    @Redirect(
                        method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;isSleeping()Z",
                    ordinal = 2
            )
    )
    private boolean pmAllowPortalTeleportation(ServerPlayerEntity player) {
        return true; // todo actual logic to check tp validity here
    }

    @Redirect(
                        method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;isChangingDimension()Z"
            )
    )
    private boolean pmAllowPortalTeleportation2(ServerPlayerEntity player) {
        if(((ITeleportLerpable)player).hasUsedPortal())
            return true;
        return player.isChangingDimension();
    }

    @Redirect(
                        method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockRayTraceResult;getBlockPos()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos pmCaptureBlockPos(BlockRayTraceResult instance) {
        this.pmCapturedBlockPos = instance.getBlockPos();
        return this.pmCapturedBlockPos;
    }

    @Redirect(
                        method = "handleUseItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/attributes/ModifiableAttributeInstance;getValue()D"
            )
    )
    private double pmAllowThroughPortalBreakReach(ModifiableAttributeInstance attribute) {
        ServerPlayNetHandler thiss = (ServerPlayNetHandler)(Object)this;
        if(PortalServerProofManager.getInstance().hasBelievableProof(thiss.player, this.pmCapturedBlockPos, false))
            return Double.POSITIVE_INFINITY;

        return attribute.getValue();
    }
}