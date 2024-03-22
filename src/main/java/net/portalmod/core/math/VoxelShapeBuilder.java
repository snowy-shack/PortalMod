package net.portalmod.core.math;

import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class VoxelShapeBuilder {
    private VoxelShape shape = VoxelShapes.empty();
    
    public VoxelShapeBuilder() {}
    
    public VoxelShapeBuilder add(VoxelShape shape) {
        this.shape = VoxelShapes.or(this.shape, shape);
        return this;
    }
    
    public VoxelShapeBuilder add(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.add(VoxelShapes.box(minX / 16f, minY / 16f, minZ / 16f, maxX / 16f, maxY / 16f, maxZ / 16f));
    }
    
    public VoxelShape build() {
        return shape;
    }
}