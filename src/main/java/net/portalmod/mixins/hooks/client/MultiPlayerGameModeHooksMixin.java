package net.portalmod.mixins.hooks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.portalmod.core.interfaces.PlayerRemovableBlock;
import net.portalmod.mixins.hooks.ServerPlayerGameModeHooksMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Client side of Forge's Block#removedByPlayer and Item#doesSneakBypassUse. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeHooksMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean pmRemovedByPlayer(Level level, BlockPos pos, BlockState newState, int flags) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof PlayerRemovableBlock removable && this.minecraft.player != null)
            return removable.removedByPlayer(state, level, pos, this.minecraft.player, false, level.getFluidState(pos));
        return level.setBlock(pos, newState, flags);
    }

    @Redirect(method = "performUseItemOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"))
    private boolean pmSneakBypassUse(LocalPlayer player, LocalPlayer p2, net.minecraft.world.InteractionHand hand,
                                     net.minecraft.world.phys.BlockHitResult hit) {
        return player.isSecondaryUseActive() && !ServerPlayerGameModeHooksMixin.SneakBypassHelper.bypasses(player, player.level(), hit.getBlockPos());
    }
}
