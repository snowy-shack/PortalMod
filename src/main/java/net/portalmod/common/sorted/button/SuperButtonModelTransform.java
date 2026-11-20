package net.portalmod.common.sorted.button;

import net.portalmod.core.math.Mat4;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import com.mojang.math.Transformation;
import org.joml.Vector3f;

public class SuperButtonModelTransform implements ModelState {
    private final Mat4 matrix;
    
    public SuperButtonModelTransform(Direction facing, QuadBlockCorner corner) {
        this.matrix = this.getMatrix(facing, corner);
    }
    
    private Mat4 getMatrix(Direction facing, QuadBlockCorner corner) {
        Mat4 matrix = Mat4.identity();
        Axis axis = facing.getAxis();
        
        if(facing.getAxisDirection() == AxisDirection.NEGATIVE) {
            if(facing.getAxis() == Axis.Y)
                matrix.rotateDeg(com.mojang.math.Axis.ZP, 180);
            else
                matrix.rotateDeg(com.mojang.math.Axis.YP, 180);
        }
        
        int axisFactor = axis == Axis.X ? 0 : -90;
        if(axis == Axis.X)
            matrix.rotateDeg(com.mojang.math.Axis.ZN, 90);
        if(axis == Axis.Z)
            matrix.rotateDeg(com.mojang.math.Axis.XP, 90);
        
        return matrix.rotateDeg(com.mojang.math.Axis.YP, corner.getRot() + axisFactor);
    }

    @Override
    public Transformation getRotation() {
        return new Transformation(matrix.to4f());
    }
}