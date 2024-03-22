package net.portalmod.mixins.accessors;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMinecartEntity.class)
public interface AbstractMinecartAccessor {
    @Accessor(remap = false, value = "lSteps")
    void pmSetLSteps(int value);

    @Accessor(remap = false, value = "lx")
    void pmSetLX(double value);

    @Accessor(remap = false, value = "ly")
    void pmSetLY(double value);

    @Accessor(remap = false, value = "lz")
    void pmSetLZ(double value);
}