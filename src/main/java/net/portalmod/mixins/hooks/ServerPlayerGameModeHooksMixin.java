package net.portalmod.mixins.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.portalmod.core.interfaces.PlayerRemovableBlock;
import net.portalmod.core.interfaces.SneakBypassUse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Server side of Forge's Block#removedByPlayer and Item#doesSneakBypassUse. */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeHooksMixin {
    @Shadow @Final protected ServerPlayer player;
    @Shadow protected ServerLevel level;

    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean pmRemovedByPlayer(ServerLevel level, BlockPos pos, boolean isMoving) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof PlayerRemovableBlock removable)
            return removable.removedByPlayer(state, level, pos, this.player, this.player.hasCorrectToolForDrops(state), level.getFluidState(pos));
        return level.removeBlock(pos, isMoving);
    }

    @Redirect(method = "useItemOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"))
    private boolean pmSneakBypassUse(ServerPlayer player, ServerPlayer p2, Level level, ItemStack stack,
                                     net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        return player.isSecondaryUseActive() && !SneakBypassHelper.bypasses(player, level, hit.getBlockPos());
    }

    /** Forge rule: sneaking still uses the block if every held item allows it. */
    public static final class SneakBypassHelper {
        public static boolean bypasses(Player player, Level level, BlockPos pos) {
            return allows(player.getMainHandItem(), player, level, pos) && allows(player.getOffhandItem(), player, level, pos);
        }

        private static boolean allows(ItemStack stack, Player player, Level level, BlockPos pos) {
            return stack.isEmpty() || stack.getItem() instanceof SneakBypassUse item && item.doesSneakBypassUse(stack, level, pos, player);
        }
    }
}
