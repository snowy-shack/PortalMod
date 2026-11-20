package net.portalmod.mixins.network;

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
    private void pmCaptureBlockPos(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int buildLimit, CallbackInfo info) {
        this.pmCapturedBlockPos = pos;
    }

    @Redirect(
                        method = "handleBlockBreakAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;getValue()D"
            )
    )
    private double pmAllowThroughPortalBreakReach(AttributeInstance attribute) {
        ServerPlayerGameMode thiss = (ServerPlayerGameMode)(Object)this;
        if(PortalServerProofManager.getInstance().hasBelievableProof(thiss.player, this.pmCapturedBlockPos, true))
            return Double.POSITIVE_INFINITY;

        return attribute.getValue();
    }
}