package net.portalmod.client.render;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.world.IBlockReader;
import net.portalmod.core.math.Vec3;

public class PortalCamera extends ActiveRenderInfo {
    private float roll;
    
    public PortalCamera(IBlockReader level, Entity entity, Vec3 position, float xRot, float yRot, float roll, float partialTicks) {
        super.setup(level, entity, false, false, partialTicks);
        this.setRotation(yRot, xRot);
        this.setPosition(position.x, position.y, position.z);
        this.roll = roll;
    }
    
    @Override
    public boolean isDetached() {
        return false;
    }
    
    public float getRoll() {
        return this.roll;
    }
}