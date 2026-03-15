package net.portalmod.common.sorted.faithplate;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.portalmod.core.math.Vec3;

import static net.portalmod.common.sorted.faithplate.FaithPlateConfigScreen.MAX_HEIGHT;

public class FaithPlateParabola {
    public static final double GRAVITY = 0.08;
    private final Vec3 target;
    private final Vec3 projectedTarget;
    private final double minHeight;
    private double height;

    public FaithPlateParabola(Vec3 target, double height) {
        this.target = target;
        this.projectedTarget = this.getProjectedTarget();
        this.minHeight = this.getMinHeight(this.getProjectedTarget());
        this.setHeight(height);
    }
    
    public FaithPlateParabola(Vector3d target, double height) {
        this(new Vec3(target), height);
    }
    
    public FaithPlateParabola(Vec3 target) {
        this(target, Double.NEGATIVE_INFINITY);
    }
    
    public FaithPlateParabola(Vector3d target) {
        this(target, Double.NEGATIVE_INFINITY);
    }
    
    public void setHeight(double height) {
        this.height = Math.min(MAX_HEIGHT, Math.max(height, this.minHeight));
    }
    
    public double getHeight() {
        return this.height;
    }
    
    private double getMinHeight(Vec3 target) {
        if(target.y == 0 || isVertical())
            return .5;
        
        Vec3 vertex = target.y > 0 ? target : new Vec3(0);
        Vec3 point = target.y < 0 ? target : new Vec3(0);
        
        double xb = target.x / 2d;
        double a = (vertex.y - point.y) / (2d * vertex.x * point.x - point.x * point.x - vertex.x * vertex.x);
        double b = -2d * a * vertex.x;
        
        return a * xb * xb + b * xb;
    }
    
    public Vec3 getProjectedTarget() {
        return new Vec3(Math.sqrt(target.x * target.x + target.z * target.z), target.y, 0);
    }
    
    public double getA() {
        return (projectedTarget.y - getB() * projectedTarget.x) / (projectedTarget.x * projectedTarget.x);
    }
    
    public double getB() {
        return (4 * height - projectedTarget.y) / projectedTarget.x;
    }
    
    public boolean isVertical() {
        return Double.isInfinite(getA()) || Double.isNaN(getA());
    }
    
    public double getAngle() {
        if(isVertical())
            return Math.PI / 2;
        return Math.atan(getB());
    }
    
    public double getVelocity() {
        if(isVertical())
            return Math.sqrt(2 * GRAVITY * height);
        return Math.sqrt(-(GRAVITY / (2d * getA()))) / Math.cos(getAngle()) * 0.996; // Magic scalar to combat overshooting
    }
    
    public double getRotation() {
        if(isVertical())
            return 0;
        return Math.atan2(target.z, target.x);
    }
    
    public double getComponentX() {
        return Math.cos(getRotation());
    }
    
    public double getComponentZ() {
        return Math.sin(getRotation());
    }
    
    public double getMiddlePoint() {
        return projectedTarget.x / 2;
    }

    public BlockRayTraceResult findFirstBlockHit(World world, FaithPlateTileEntity be) {
        double step = 0.01;
        Vec3 startOffset = new Vec3(0.5, 1, 0.5);
        Vec3 prev = startOffset.clone();

        double targetStep = Double.POSITIVE_INFINITY;
        for (double t = 0; t < targetStep; t += step) {
            double x = startOffset.x, y, z = startOffset.z;
            if (isVertical()) {
                y = t + startOffset.y;
                targetStep = Math.abs(projectedTarget.y) * 2;
            } else {
                y = getA() * t * t + getB() * t + startOffset.y;
                x = getComponentX() * t + startOffset.x;
                z = getComponentZ() * t + startOffset.z;
                targetStep = Math.abs(projectedTarget.x) * 2;
            }

            Vec3 current = new Vec3(x, y, z);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos.offset(be.getBlockPos()));

            if (!state.getCollisionShape(world, pos).isEmpty() && !state.is(be.getBlockState().getBlock())) {
                BlockPos prevBlock = new BlockPos(prev.x, prev.y, prev.z);
                Vector3i diff = pos.subtract(prevBlock);
                Direction face = Direction.fromNormal(diff.getX(), diff.getY(), diff.getZ());

                if (face == null) face = getNearestCubeFace(new Vector3d(current.x, current.y, current.z)).getOpposite();

                return new BlockRayTraceResult(new Vector3d(x, y, z), face, pos, false);
            }

            prev = current;
        }
        return null;
    }

    public Direction getNearestCubeFace(Vector3d p) {
        Vector3d point = p.subtract(new Vector3d(Math.floor(p.x), Math.floor(p.y), Math.floor(p.z)));
        double[] d = {point.x, 1 - point.x, point.y, 1 - point.y, point.z, 1 - point.z};
        Direction[] faces = {
                Direction.WEST, Direction.EAST,
                Direction.DOWN, Direction.UP,
                Direction.SOUTH, Direction.NORTH
        };

        int minIndex = 0;
        for (int i = 1; i < 6; i++) {
            if (d[i] < d[minIndex]) minIndex = i;
        }

        return faces[minIndex];
    }
}