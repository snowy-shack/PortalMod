package net.portalmod.common.sorted.portal;

public interface ITeleportable {
    void setLastUsedPortal(int lastUsedPortal);
    int getLastUsedPortal();
    boolean hasLastUsedPortal();
    void removeLastUsedPortal();
}