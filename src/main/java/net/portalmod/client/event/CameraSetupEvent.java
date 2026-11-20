package net.portalmod.client.event;

import net.minecraft.client.Camera;

/**
 * Mutable camera angles for the current frame, passed to camera-setup handlers.
 * Replaces Forge's EntityViewRenderEvent.CameraSetup; fired from GameRendererHooksMixin.
 */
public final class CameraSetupEvent {
    private final Camera info;
    private final double partialTicks;
    private float yaw;
    private float pitch;
    private float roll;

    public CameraSetupEvent(Camera info, double partialTicks, float yaw, float pitch, float roll) {
        this.info = info;
        this.partialTicks = partialTicks;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public Camera getInfo() { return info; }
    public double getRenderPartialTicks() { return partialTicks; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public void setRoll(float roll) { this.roll = roll; }
}
