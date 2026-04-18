package net.portalmod.mixins.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/client/multiplayer/ClientChunkProvider$ChunkArray")
public class ChunkArrayMixin {
//    @Inject(
//            //            method = "inRange",
//            at = @At("RETURN"),
//            cancellable = true
//    )
//    private void pmInRangeCountingPortalLoadedChunks(int x, int z, CallbackInfoReturnable<Boolean> info) {
//        info.setReturnValue(info.getReturnValueZ() || (x == 5 && z == -1));
//    }
}