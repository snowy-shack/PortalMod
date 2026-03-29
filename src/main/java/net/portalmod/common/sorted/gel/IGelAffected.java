package net.portalmod.common.sorted.gel;

import net.minecraft.util.math.vector.Vector3d;

public interface IGelAffected {
    int MAX_PROPULSION_TICKS = 30;

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

    // Only for propulsion gel for now
    void setLeftGround(boolean leftGround);
    boolean getLeftGround();

    int getPropulsionTicks();
    void setPropulsionTicks(int ticks);
    void incrementPropulsionTicks();
    void decrementPropulsionTicks();

    void setServerBoosting(boolean serverBoosting);
    boolean isServerBoosting();

    void setLastDeltaMovement(Vector3d lastDeltaMovement);
    Vector3d getLastDeltaMovement();
}
