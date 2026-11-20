package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor(value = "lerpSteps")
    void pmSetLerpSteps(int value);

    @Accessor(value = "lerpX")
    void pmSetLerpX(double value);

    @Accessor(value = "lerpY")
    void pmSetLerpY(double value);

    @Accessor(value = "lerpZ")
    void pmSetLerpZ(double value);
}
