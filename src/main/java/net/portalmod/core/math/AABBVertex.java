package net.portalmod.core.math;

import com.google.common.collect.Lists;
import net.minecraft.util.Direction;

import java.util.List;

public class AABBVertex {
    private final Vec3 position;
    private final Corner corner;

    public AABBVertex(Vec3 position, Corner corner) {
        this.position = position;
        this.corner = corner;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public Corner getCorner() {
        return this.corner;
    }

    public enum Corner {
        LEFT_DOWN_FORWARDS(Direction.WEST, Direction.DOWN, Direction.NORTH),
        LEFT_DOWN_BACKWARDS(Direction.WEST, Direction.DOWN, Direction.SOUTH),
        LEFT_UP_FORWARDS(Direction.WEST, Direction.UP, Direction.NORTH),
        LEFT_UP_BACKWARDS(Direction.WEST, Direction.UP, Direction.SOUTH),
        RIGHT_DOWN_FORWARDS(Direction.EAST, Direction.DOWN, Direction.NORTH),
        RIGHT_DOWN_BACKWARDS(Direction.EAST, Direction.DOWN, Direction.SOUTH),
        RIGHT_UP_FORWARDS(Direction.EAST, Direction.UP, Direction.NORTH),
        RIGHT_UP_BACKWARDS(Direction.EAST, Direction.UP, Direction.SOUTH);

        final Direction x;
        final Direction y;
        final Direction z;

        Corner(Direction x, Direction y, Direction z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Direction getX() {
            return this.x;
        }

        public Direction getY() {
            return this.y;
        }

        public Direction getZ() {
            return this.z;
        }

        public List<Direction> getNormals() {
            return Lists.newArrayList(this.x, this.y, this.z);
        }
    }
}