package net.portalmod.core.interfaces;

import java.util.Deque;

import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec3;

public interface ITeleportLerpable {
    Deque<Tuple<Vec3, Vec3>> getLerpPositions();
    boolean hasUsedPortal();
    void setHasUsedPortal(boolean hasUsedPortal);
}