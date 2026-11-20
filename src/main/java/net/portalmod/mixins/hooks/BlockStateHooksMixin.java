package net.portalmod.mixins.hooks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.portalmod.core.interfaces.CustomPushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Per-state piston push reaction for mod blocks (Forge: Block#getPistonPushReaction). */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateHooksMixin {
    @Shadow public abstract Block getBlock();

    @Inject(method = "getPistonPushReaction", at = @At("HEAD"), cancellable = true)
    private void pmGetPistonPushReaction(CallbackInfoReturnable<PushReaction> cir) {
        if (this.getBlock() instanceof CustomPushReaction custom)
            cir.setReturnValue(custom.getPistonPushReaction((BlockState) (Object) this));
    }
}
