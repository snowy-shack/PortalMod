package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.vector.Vector3d;

@Mixin(ActiveRenderInfo.class)
public interface ActiveRenderInfoAccessor {
    @Accessor(remap = false, value = "position")
    void pmSetPosition(Vector3d value);
    
    @Accessor(remap = false, value = "position")
    Vector3d pmGetPosition();
}