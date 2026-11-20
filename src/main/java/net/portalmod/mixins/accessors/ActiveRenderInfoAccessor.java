package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

@Mixin(Camera.class)
public interface ActiveRenderInfoAccessor {
    @Accessor(value = "position")
    void pmSetPosition(Vec3 value);
    
    @Accessor(value = "position")
    Vec3 pmGetPosition();

    @Accessor(value = "blockPosition")
    BlockPos.MutableBlockPos pmGetBlockPosition();

    @Accessor(value = "eyeHeight")
    float pmGetEyeHeight();

    @Accessor(value = "eyeHeightOld")
    float pmGetEyeHeightOld();

    @Invoker(value = "setRotation")
    void pmSetRotation(float yaw, float pitch);
}