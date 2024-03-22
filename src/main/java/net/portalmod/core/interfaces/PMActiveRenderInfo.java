package net.portalmod.core.interfaces;

import net.minecraft.world.IBlockReader;
import net.portalmod.core.math.Vec3;

public interface PMActiveRenderInfo {
    void pmSetupForOrtho(IBlockReader level, Vec3 position, float yRot, float xRot, float zoom);
}