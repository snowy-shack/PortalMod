package net.portalmod.mixins;

import java.io.IOException;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.KeyboardListener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import net.portalmod.PortalMod;
import net.portalmod.client.render.Shader;

@Mixin(KeyboardListener.class)
public abstract class KeyboardListenerMixin {
    @Shadow protected abstract void debugFeedbackTranslated(String string, Object... objects);

    @Inject(at = @At(value = "RETURN"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;allChanged()V")),
            method = "handleDebugKeys(I)Z", cancellable = true)
    private void pmHandleDebugKeys(int key, CallbackInfoReturnable<Boolean> info) {
        if(!info.getReturnValue() && key == GLFW.GLFW_KEY_Z) {
            boolean error = false;
            try {
                Shader.reloadAll();
            } catch(IOException e) {
                error = true;
                e.printStackTrace();
                debugFeedbackTranslated("debug." + PortalMod.MODID + ".reload_shaders.error");
            }

            if(!error)
                debugFeedbackTranslated("debug." + PortalMod.MODID + ".reload_shaders.message");

            info.setReturnValue(true);
        } else if(key == GLFW.GLFW_KEY_Q) {
            Minecraft.getInstance().gui.getChat().addMessage(new TranslationTextComponent("debug." + PortalMod.MODID + ".reload_shaders.help"));
        }
    }
}