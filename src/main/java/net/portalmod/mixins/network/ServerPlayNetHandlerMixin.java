package net.portalmod.mixins.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.portalmod.common.sorted.portal.IClientTeleportable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin {
    @ModifyVariable(
            remap = false,
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
            remap = false,
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
}