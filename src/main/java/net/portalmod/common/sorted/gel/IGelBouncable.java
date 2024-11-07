package net.portalmod.common.sorted.gel;

public interface IGelBouncable {
    void setLastFallDistance(float distance);
    float getLastFallDistance();

    void setBounced(boolean newBounced);
    boolean getBounced();
    void setWasOnGround(boolean wasOnGround);
    boolean getWasOnGround();
}
