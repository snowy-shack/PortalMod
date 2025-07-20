package net.portalmod.mixins.accessors;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ActiveRenderInfo.class)
public interface ActiveRenderInfoAccessor {
    @Accessor(remap = false, value = "position")
    void pmSetPosition(Vector3d value);
    
    @Accessor(remap = false, value = "position")
    Vector3d pmGetPosition();

    @Accessor(remap = false, value = "blockPosition")
    BlockPos.Mutable pmGetBlockPosition();

    @Accessor(remap = false, value = "eyeHeight")
    float pmGetEyeHeight();

    @Accessor(remap = false, value = "eyeHeightOld")
    float pmGetEyeHeightOld();

    @Invoker(remap = false, value = "setRotation")
    void pmSetRotation(float yaw, float pitch);
}