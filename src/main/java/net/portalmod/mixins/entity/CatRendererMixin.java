package net.portalmod.mixins.entity;

import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.portalmod.PortalMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatRenderer.class)
public class CatRendererMixin {
    @Inject(
                        method = "getTextureLocation(Lnet/minecraft/entity/passive/CatEntity;)Lnet/minecraft/util/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )
    void getTextureLocation(CatEntity cat, CallbackInfoReturnable<ResourceLocation> info) {
        // todo maybe idk stitch texture
        if(cat.hasCustomName() && TextFormatting.stripFormatting(cat.getName().getString()).equalsIgnoreCase("qubit"))
            info.setReturnValue(new ResourceLocation(PortalMod.MODID, "textures/entity/cat/qubit.png"));
    }
}