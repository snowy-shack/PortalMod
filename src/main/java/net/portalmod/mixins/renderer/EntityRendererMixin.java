package net.portalmod.mixins.renderer;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow abstract protected int getBlockLightLevel(Entity entity, BlockPos pos);
    @Shadow abstract protected int getSkyLightLevel(Entity entity, BlockPos pos);

    // todo only when rendering item in hand i guess
    // todo also teleport eyes instead of moving

    // BEWARE: PORTAL RENDERING
    @Inject(
                        method = "getPackedLightCoords(Lnet/minecraft/entity/Entity;F)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void pmDeceiveLightEngine(Entity entity, float partialTicks, CallbackInfoReturnable<Integer> info) {
        Minecraft mc = Minecraft.getInstance();
        if(entity != mc.cameraEntity)
            return;

        BlockPos pos = mc.gameRenderer.getMainCamera().getBlockPosition();
        info.setReturnValue(LightTexture.pack(
                this.getBlockLightLevel(entity, pos),
                this.getSkyLightLevel(entity, pos)
        ));
    }
}