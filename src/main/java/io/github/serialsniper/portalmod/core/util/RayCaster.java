package io.github.serialsniper.portalmod.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class RayCaster {
    public static BlockRayTraceResult castBlock(PlayerEntity player, World level, int length) {
        Vector3d rayPath = player.getViewVector(0).scale(length);
        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null);
        return level.clip(rayCtx);
    }
}