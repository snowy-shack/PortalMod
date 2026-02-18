package net.portalmod.common.sorted.portal;

public interface PortalHandler {
    void onTeleport(PortalEntity from, PortalEntity to);
    void onTeleportPacket();
}