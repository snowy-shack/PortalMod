package net.portalmod.client.render;

import net.portalmod.core.math.Vec3;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

public class PortalCamera extends Camera {
    private float roll;
    
    public PortalCamera(BlockGetter level, Entity entity, Vec3 position, float xRot, float yRot, float roll, float partialTicks) {
        super.setup(level, entity, false, false, partialTicks);
        this.setRotation(yRot, xRot);
        this.setPosition(position.x, position.y, position.z);
        this.roll = roll;
    }

    public PortalCamera(Camera camera, float partialTicks) {
        this(camera.getEntity().level(), camera.getEntity(), new Vec3(camera.getPosition()), camera.getXRot(), camera.getYRot(),
                camera instanceof PortalCamera ? ((PortalCamera)camera).getRoll() : 0, partialTicks);
    }

    @Override
    public boolean isDetached() {
        return false;
    }

    public void setPitch(float pitch) {
        this.setRotation(this.getYRot(), pitch);
    }

    public void setYaw(float yaw) {
        this.setRotation(yaw, this.getXRot());
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }
    
    public float getRoll() {
        return this.roll;
    }
}