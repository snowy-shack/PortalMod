package net.portalmod.common.sorted.portal;

public interface IClientTeleportable {
    void setJustPortaled(boolean justPortaled);
    boolean getJustPortaled();
    void removeJustPortaled();
}