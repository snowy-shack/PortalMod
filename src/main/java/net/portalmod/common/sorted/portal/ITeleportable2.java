package net.portalmod.common.sorted.portal;

public interface ITeleportable2 {
    void setJustUsedPortal(int justUsedPortal);
    int getJustUsedPortal();
    boolean hasJustUsedPortal();
    void removeJustUsedPortal();
}