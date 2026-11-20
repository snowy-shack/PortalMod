package net.portalmod.mixins.hooks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.portalmod.core.interfaces.RedstoneConnectable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets redstone dust connect to mod blocks (Forge: Block#canConnectRedstone). */
@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireHooksMixin {
    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void pmShouldConnectTo(BlockState state, @Nullable Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof RedstoneConnectable connectable)
            cir.setReturnValue(connectable.canConnectRedstone(state, null, null, direction));
    }
}
