package net.portalmod.mixins.entity;

import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.portalmod.PortalMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(RabbitRenderer.class)
public class RabbitRendererMixin {
    @Inject(
                        method = "getTextureLocation(Lnet/minecraft/entity/passive/RabbitEntity;)Lnet/minecraft/util/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )
    void getTextureLocation(RabbitEntity rabbit, CallbackInfoReturnable<ResourceLocation> cir) {
        // todo maybe idk stitch texture
        String name = TextFormatting.stripFormatting(rabbit.getName().getString());
        if(rabbit.hasCustomName() && (Objects.equals(name, "bun") || Objects.equals(name, "niko")))
            cir.setReturnValue(new ResourceLocation(PortalMod.MODID, "textures/entity/rabbit/bun.png"));
    }
}