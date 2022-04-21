package io.github.serialsniper.portalmod.client.render;

import net.minecraft.util.*;
import net.minecraft.util.math.vector.*;

public class CubeCoordinates {
    public enum Facing {
        OUTSIDE,
        INSIDE
    }

//    public static List<Vector3f[]> getAllQuadsExcept(Direction side, Facing facing, float d) {
//        Map<Direction, Vector3f[]> quads = new HashMap<>();
//
//        quads.put(Direction.NORTH, getQuad(Direction.NORTH, facing, d));
//        quads.put(Direction.SOUTH, getQuad(Direction.SOUTH, facing, d));
//        quads.put(Direction.WEST, getQuad(Direction.WEST, facing, d));
//        quads.put(Direction.EAST, getQuad(Direction.EAST, facing, d));
//        quads.put(Direction.UP, getQuad(Direction.UP, facing, d));
//        quads.put(Direction.DOWN, getQuad(Direction.DOWN, facing, d));
//
//        if(side != null)
//            quads.remove(side);
//
//        return (List<Vector3f[]>) quads.values();
//    }
//
//    public static List<Vector3f[]> getAllQuads(Facing facing, float d) {
//        return getAllQuadsExcept(null, facing, d);
//    }



    public static Vector3f[] getQuad(Direction side, Facing facing, float h) {
        if(facing == Facing.INSIDE)
            side = side.getOpposite();

        float x, y, z;

        switch(side) {
            default:
            case NORTH:
                z = facing == Facing.INSIDE ? 1 : 0;
                return new Vector3f[] {
                        new Vector3f(0, h, z),
                        new Vector3f(1, h, z),
                        new Vector3f(1, 0, z),
                        new Vector3f(0, 0, z)
                };

            case SOUTH:
                z = facing == Facing.INSIDE ? 0 : 1;
                return new Vector3f[] {
                        new Vector3f(1, h, z),
                        new Vector3f(0, h, z),
                        new Vector3f(0, 0, z),
                        new Vector3f(1, 0, z)
                };

            case WEST:
                x = facing == Facing.INSIDE ? 1 : 0;
                return new Vector3f[] {
                        new Vector3f(x, h, 1),
                        new Vector3f(x, h, 0),
                        new Vector3f(x, 0, 0),
                        new Vector3f(x, 0, 1)
                };

            case EAST:
                x = facing == Facing.INSIDE ? 0 : 1;
                return new Vector3f[] {
                        new Vector3f(x, h, 0),
                        new Vector3f(x, h, 1),
                        new Vector3f(x, 0, 1),
                        new Vector3f(x, 0, 0)
                };

            case UP:
                y = facing == Facing.INSIDE ? 0 : h;
                return new Vector3f[] {
                        new Vector3f(1, y, 0),
                        new Vector3f(0, y, 0),
                        new Vector3f(0, y, 1),
                        new Vector3f(1, y, 1)
                };

            case DOWN:
                y = facing == Facing.INSIDE ? h : 0;
                return new Vector3f[] {
                        new Vector3f(0, y, 0),
                        new Vector3f(1, y, 0),
                        new Vector3f(1, y, 1),
                        new Vector3f(0, y, 1)
                };
        }
    }
}