package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.vehicle.AbstractMinecart;

@Mixin(AbstractMinecart.class)
public interface AbstractMinecartAccessor {
    @Accessor(value = "lSteps")
    void pmSetLSteps(int value);

    @Accessor(value = "lx")
    void pmSetLX(double value);

    @Accessor(value = "ly")
    void pmSetLY(double value);

    @Accessor(value = "lz")
    void pmSetLZ(double value);
}