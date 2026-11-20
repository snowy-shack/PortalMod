package net.portalmod.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
    @Invoker(value = "add")
    void pmForceAddPortal(Entity entity);
}