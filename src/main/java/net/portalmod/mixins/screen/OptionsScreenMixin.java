package net.portalmod.mixins.screen;

import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.portalmod.client.screens.PortalModOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    
    protected OptionsScreenMixin(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }
    
    @Inject(at = @At(value = "TAIL"), method = "init()V")
    private void pmInit(CallbackInfo info) {
        this.addButton(PortalModOptionsScreen.getAccessButton(this));
    }
}