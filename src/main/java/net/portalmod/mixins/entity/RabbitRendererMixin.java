package net.portalmod.mixins.entity;

import net.portalmod.PortalMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

@Mixin(RabbitRenderer.class)
public class RabbitRendererMixin {
    @Inject(
                        method = "getTextureLocation(Lnet/minecraft/world/entity/animal/Rabbit;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )
    void getTextureLocation(Rabbit rabbit, CallbackInfoReturnable<ResourceLocation> cir) {
        // todo maybe idk stitch texture
        String name = ChatFormatting.stripFormatting(rabbit.getName().getString());
        if(rabbit.hasCustomName() && (Objects.equals(name, "bun") || Objects.equals(name, "niko")))
            cir.setReturnValue(new ResourceLocation(PortalMod.MODID, "textures/entity/rabbit/bun.png"));
    }
}