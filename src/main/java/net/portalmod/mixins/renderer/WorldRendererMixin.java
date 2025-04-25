package net.portalmod.mixins.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.portalmod.common.sorted.fizzler.FizzlerFieldBlock;
import net.portalmod.common.sorted.portalgun.PortalGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements IResourceManagerReloadListener, AutoCloseable {

    @Inject(method = "renderHitOutline", at = @At("HEAD"), cancellable = true)
    private void render(MatrixStack p_228429_1_, IVertexBuilder p_228429_2_, Entity entity, double p_228429_4_, double p_228429_6_, double p_228429_8_, BlockPos p_228429_10_, BlockState p_228429_11_, CallbackInfo ci) {
        entity.getHandSlots().forEach(itemStack -> {
            if (itemStack.getItem() instanceof PortalGun) {
                ci.cancel();
            }
        });
    }

    @Redirect(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"), remap = false)
    public boolean fizzlerIsAirToo(BlockState instance, IBlockReader blockReader, BlockPos pos) {
        return instance.isAir(blockReader, pos) || instance.getBlock() instanceof FizzlerFieldBlock;
    }
}
