package net.portalmod.common.sorted.gel;

import net.minecraft.util.math.vector.Vector3d;

public interface IGelAffected {
    void setLastNeurtalHeight(float distance);
    float getLastNeutralHeight();

    // Lasts until moving down
    void setBounced(boolean newBounced);
    boolean getBounced();

    // Lasts one tick
    void setHorizontalBounced(boolean newHorizontalBounced);
    boolean getHorizontalBounced();

    void setWasOnGround(boolean wasOnGround);
    boolean getWasOnGround();

    void setAffectedBySpeedGel(boolean affectedBySpeedGel);
    boolean getAffectedBySpeedGel();

    void setTicksSinceSpeedGel(int ticksSinceSpeedGel);
    int getTicksSinceSpeedGel();

    void setLastDeltaMovement(Vector3d lastDeltaMovement);
    Vector3d getLastDeltaMovement();
}
