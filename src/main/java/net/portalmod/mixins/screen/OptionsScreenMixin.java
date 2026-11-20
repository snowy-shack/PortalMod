package net.portalmod.mixins.screen;

import net.portalmod.client.screens.PortalModOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    
    protected OptionsScreenMixin(Component p_i51108_1_) {
        super(p_i51108_1_);
    }
    
    @Inject(at = @At(value = "TAIL"), method = "init()V")
    private void pmInit(CallbackInfo info) {
        this.addRenderableWidget(PortalModOptionsScreen.getAccessButton(this));
    }
}