package net.portalmod.core.math;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

public class VoxelShapeBuilder {
    private VoxelShape shape = Shapes.empty();
    
    public VoxelShapeBuilder() {}
    
    public VoxelShapeBuilder add(VoxelShape shape) {
        this.shape = Shapes.or(this.shape, shape);
        return this;
    }
    
    public VoxelShapeBuilder add(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.add(Shapes.box(minX / 16f, minY / 16f, minZ / 16f, maxX / 16f, maxY / 16f, maxZ / 16f));
    }
    
    public VoxelShape build() {
        return shape;
    }
}