package net.portalmod;

import net.portalmod.core.math.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PMState {
    public static float cameraRoll = 0;
    public static Vec3 cameraPosOverrideForRenderingSelf = null;
    public static List<Vec3> positionsToSkipRenderingSelf = new ArrayList<>();
}