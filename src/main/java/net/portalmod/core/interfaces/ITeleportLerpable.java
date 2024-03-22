package net.portalmod.core.interfaces;

import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Deque;

public interface ITeleportLerpable {
    Deque<Tuple<Vector3d, Vector3d>> getLerpPositions();
    boolean hasUsedPortal();
    void setHasUsedPortal(boolean hasUsedPortal);
}