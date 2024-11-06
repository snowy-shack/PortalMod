package net.portalmod.mixins.accessors;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor(remap = false, value = "lerpSteps")
    void pmSetLerpSteps(int value);

    @Accessor(remap = false, value = "lerpX")
    void pmSetLerpX(double value);

    @Accessor(remap = false, value = "lerpY")
    void pmSetLerpY(double value);

    @Accessor(remap = false, value = "lerpZ")
    void pmSetLerpZ(double value);
}
