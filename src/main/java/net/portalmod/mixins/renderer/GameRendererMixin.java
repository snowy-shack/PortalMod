package net.portalmod.mixins.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow private float zoom;

    @Shadow private float zoomY;

    @Shadow private float zoomX;

    @Shadow protected abstract double getFov(ActiveRenderInfo p_215311_1_, float p_215311_2_, boolean p_215311_3_);

    @Shadow @Final private Minecraft minecraft;

    @Shadow private float renderDistance;

    @Inject(
            remap = false,
            method = "getProjectionMatrix",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pmGetProjectionMatrix(ActiveRenderInfo camera, float partialTicks, boolean b, CallbackInfoReturnable<Matrix4f> info) {
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.last().pose().setIdentity();
        if(this.zoom != 1.0F) {
            matrixstack.translate(this.zoomX, -this.zoomY, 0.0D);
            matrixstack.scale(this.zoom, this.zoom, 1.0F);
        }

        matrixstack.last().pose().multiply(Matrix4f.perspective(this.getFov(camera, partialTicks, b), (float)this.minecraft.getMainRenderTarget().width / (float)this.minecraft.getMainRenderTarget().height, 0.05F, this.renderDistance * 4.0F));
        info.setReturnValue(matrixstack.last().pose());
    }
}