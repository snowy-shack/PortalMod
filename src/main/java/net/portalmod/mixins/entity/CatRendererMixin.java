package net.portalmod.mixins.entity;

import net.portalmod.PortalMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

@Mixin(CatRenderer.class)
public class CatRendererMixin {
    @Inject(
                        method = "getTextureLocation(Lnet/minecraft/world/entity/animal/Cat;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )
    void getTextureLocation(Cat cat, CallbackInfoReturnable<ResourceLocation> info) {
        // todo maybe idk stitch texture
        if(cat.hasCustomName() && ChatFormatting.stripFormatting(cat.getName().getString()).equalsIgnoreCase("qubit"))
            info.setReturnValue(new ResourceLocation(PortalMod.MODID, "textures/entity/cat/qubit.png"));
    }
}