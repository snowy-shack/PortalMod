package io.github.serialsniper.portalmod.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At(value = "RETURN"), method = "shouldRenderFace(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)Z", cancellable = true)
    private static void shouldRenderFace(BlockState state, IBlockReader level, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
//        if(Minecraft.getInstance().player.blockPosition().getX() == pos.getX()
//            && Minecraft.getInstance().player.blockPosition().getZ() == pos.getZ()
//            && Minecraft.getInstance().player.blockPosition().below().getY() == pos.getY()
//            && direction == Direction.UP)
//            info.setReturnValue(false);
    }
}