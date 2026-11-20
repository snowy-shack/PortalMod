package net.portalmod.mixins.hooks.client;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.portalmod.core.event.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces Forge's RenderBlockOverlayEvent for the "inside a block" overlay.
 * Same search as vanilla getViewBlockingState, but we also need the block position.
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererHooksMixin {
    @Inject(method = "getViewBlockingState", at = @At("HEAD"), cancellable = true)
    private static void pmGetViewBlockingState(Player player, CallbackInfoReturnable<BlockState> cir) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 8; ++i) {
            double x = player.getX() + (double) (((float) ((i >> 0) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
            double y = player.getEyeY() + (double) (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
            double z = player.getZ() + (double) (((float) ((i >> 2) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
            pos.set(x, y, z);
            BlockState state = player.level().getBlockState(pos);
            if (state.getRenderShape() != RenderShape.INVISIBLE && state.isViewBlocking(player.level(), pos)) {
                cir.setReturnValue(ClientEvents.shouldRenderBlockOverlay(player, pos.immutable()) ? state : null);
                return;
            }
        }
        cir.setReturnValue(null);
    }
}
