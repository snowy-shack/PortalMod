package net.portalmod.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.portalmod.common.sorted.panel.PanelBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets plains plants (flowers, grass, saplings) grow on arbored panels.
 * Forge did this through Block#canSustainPlant; vanilla checks BushBlock#mayPlaceOn instead.
 */
@Mixin(BushBlock.class)
public class BlockMixin {
    @Inject(method = "mayPlaceOn", at = @At("RETURN"), cancellable = true)
    private void allowArboredPlains(BlockState state, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ())
            return;
        if (state.getBlock() instanceof PanelBlock
                && BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().startsWith("arbored")) {
            cir.setReturnValue(state.isFaceSturdy(world, pos.below(), Direction.UP));
        }
    }
}
