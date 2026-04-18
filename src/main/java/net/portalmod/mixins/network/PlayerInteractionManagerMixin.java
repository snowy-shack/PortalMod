package net.portalmod.mixins.network;

import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.portalmod.common.sorted.portal.PortalServerProofManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInteractionManager.class)
public class PlayerInteractionManagerMixin {
    private BlockPos pmCapturedBlockPos;

    @Inject(
                        method = "handleBlockBreakAction",
            at = @At("HEAD")
    )
    private void pmCaptureBlockPos(BlockPos pos, CPlayerDiggingPacket.Action action, Direction direction, int buildLimit, CallbackInfo info) {
        this.pmCapturedBlockPos = pos;
    }

    @Redirect(
                        method = "handleBlockBreakAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/attributes/ModifiableAttributeInstance;getValue()D"
            )
    )
    private double pmAllowThroughPortalBreakReach(ModifiableAttributeInstance attribute) {
        PlayerInteractionManager thiss = (PlayerInteractionManager)(Object)this;
        if(PortalServerProofManager.getInstance().hasBelievableProof(thiss.player, this.pmCapturedBlockPos, true))
            return Double.POSITIVE_INFINITY;

        return attribute.getValue();
    }
}