package net.portalmod.mixins.renderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.portalmod.common.sorted.fizzler.FizzlerFieldBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements IResourceManagerReloadListener, AutoCloseable {

    @Redirect(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"), remap = false)
    public boolean fizzlerIsAirToo(BlockState instance, IBlockReader blockReader, BlockPos pos) {
        return instance.isAir(blockReader, pos) || instance.getBlock() instanceof FizzlerFieldBlock;
    }
}
