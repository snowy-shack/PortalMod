package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker(value = "collide")
    Vec3 pmCollide(Vec3 delta);

    @Invoker(value = "getBoundingBoxForPose")
    AABB pmGetBoundingBoxForPose(Pose pose);

    @Accessor(value = "firstTick")
    boolean pmGetFirstTick();
}