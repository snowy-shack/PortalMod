package net.portalmod.common.sorted.superbutton;

import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.portalmod.core.math.Mat4;

public class SuperButtonModelTransform implements IModelTransform {
    private final Mat4 matrix;
    
    public SuperButtonModelTransform(Direction facing, QuadBlockCorner corner) {
        this.matrix = this.getMatrix(facing, corner);
    }
    
    private Mat4 getMatrix(Direction facing, QuadBlockCorner corner) {
        Mat4 matrix = Mat4.identity();
        Axis axis = facing.getAxis();
        
        if(facing.getAxisDirection() == AxisDirection.NEGATIVE) {
            if(facing.getAxis() == Axis.Y)
                matrix.rotateDeg(Vector3f.ZP, 180);
            else
                matrix.rotateDeg(Vector3f.YP, 180);
        }
        
        int axisFactor = axis == Axis.X ? 0 : -90;
        if(axis == Axis.X)
            matrix.rotateDeg(Vector3f.ZN, 90);
        if(axis == Axis.Z)
            matrix.rotateDeg(Vector3f.XP, 90);
        
        return matrix.rotateDeg(Vector3f.YP, corner.getRot() + axisFactor);
    }

    @Override
    public TransformationMatrix getRotation() {
        return new TransformationMatrix(matrix.to4f());
    }
}