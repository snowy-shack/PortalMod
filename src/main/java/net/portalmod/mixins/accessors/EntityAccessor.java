package net.portalmod.mixins.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker(value = "collide")
    Vector3d pmCollide(Vector3d delta);

    @Invoker(value = "getBoundingBoxForPose")
    AxisAlignedBB pmGetBoundingBoxForPose(Pose pose);

    @Accessor(value = "firstTick")
    boolean pmGetFirstTick();
}