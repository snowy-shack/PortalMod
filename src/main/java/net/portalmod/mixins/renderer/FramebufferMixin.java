package net.portalmod.mixins.renderer;

import java.nio.IntBuffer;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.injection.Redirect;

import org.spongepowered.asm.mixin.Unique;

import org.spongepowered.asm.mixin.Shadow;

import org.lwjgl.opengl.GL30;

import net.portalmod.core.interfaces.StencilTarget;

import com.mojang.blaze3d.platform.GlStateManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderTarget;

@Mixin(RenderTarget.class)
public abstract class FramebufferMixin implements StencilTarget {
    @Shadow public int width;
    @Shadow public int height;
    @Shadow public int viewWidth;
    @Shadow public int viewHeight;

    @Shadow public abstract void resize(int width, int height, boolean clearError);

    @Unique
    private boolean pmStencilEnabled = false;

    @Override
    public void pmEnableStencil() {
        if (this.pmStencilEnabled)
            return;
        this.pmStencilEnabled = true;
        this.resize(this.viewWidth, this.viewHeight, Minecraft.ON_OSX);
    }

    @Override
    public boolean pmIsStencilEnabled() {
        return this.pmStencilEnabled;
    }

    // Same as Forge's stencil patch: a combined depth+stencil texture instead of depth only.
    @Redirect(method = "createBuffers", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"))
    private void pmDepthStencilTexture(int target, int level, int internalFormat, int w, int h, int border,
                                       int format, int type, @Nullable IntBuffer pixels) {
        if (this.pmStencilEnabled)
            GlStateManager._texImage2D(target, level, GL30.GL_DEPTH32F_STENCIL8, w, h, border,
                    GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);
        else
            GlStateManager._texImage2D(target, level, internalFormat, w, h, border, format, type, pixels);
    }

    @Redirect(method = "createBuffers", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V"))
    private void pmAttachDepthStencil(int target, int attachment, int texTarget, int texture, int level) {
        GlStateManager._glFramebufferTexture2D(target, this.pmStencilEnabled ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : attachment,
                texTarget, texture, level);
    }

    // BEWARE: PORTAL RENDERING
    @ModifyVariable(
            method = "bindWrite",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private boolean pmBindMainFBWithViewport(boolean value) {
        return value || ((Object)this == Minecraft.getInstance().getMainRenderTarget());
    }
}
