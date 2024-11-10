package net.portalmod.common.sorted.gel;

import net.minecraft.util.math.vector.Vector3d;

public interface IGelAffected<Vector3f> {
    void setLastNeurtalHeight(float distance);
    float getLastNeutralHeight();

    void setBounced(boolean newBounced);
    boolean getBounced();

    void setWasOnGround(boolean wasOnGround);
    boolean getWasOnGround();

    void setAffectedBySpeedGel(boolean affectedBySpeedGel);
    boolean getAffectedBySpeedGel();

    void setTicksSinceSpeedGel(int ticksSinceSpeedGel);
    int getTicksSinceSpeedGel();

    void setLastDeltaMovement(Vector3d lastDeltaMovement);
    Vector3d getLastDeltaMovement();
}
