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

    public PortalCamera(ActiveRenderInfo camera, float partialTicks) {
        this(camera.getEntity().level, camera.getEntity(), new Vec3(camera.getPosition()), camera.getXRot(), camera.getYRot(),
                camera instanceof PortalCamera ? ((PortalCamera)camera).getRoll() : 0, partialTicks);
    }

    @Override
    public boolean isDetached() {
        return false;
    }

    public void setPitch(float pitch) {
        this.setAnglesInternal(this.getYRot(), pitch);
    }

    public void setYaw(float yaw) {
        this.setAnglesInternal(yaw, this.getXRot());
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }
    
    public float getRoll() {
        return this.roll;
    }
}