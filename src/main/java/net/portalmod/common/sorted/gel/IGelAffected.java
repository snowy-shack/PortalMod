package net.portalmod.common.sorted.gel;

import net.minecraft.world.phys.Vec3;

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

    void setLastDeltaMovement(Vec3 lastDeltaMovement);
    Vec3 getLastDeltaMovement();
    void setLastLastDeltaMovement(Vec3 lastLastDeltaMovement);
    Vec3 getLastLastDeltaMovement();
}
