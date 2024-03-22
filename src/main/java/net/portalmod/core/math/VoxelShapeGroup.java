package net.portalmod.core.math;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class VoxelShapeGroup {
    private VoxelShape shape;
    private final Map<String, VoxelShape> parts = new HashMap<>();
    
    private VoxelShapeGroup(VoxelShape shape, Map<String, VoxelShape> parts) {
        this.shape = VoxelShapes.or(shape);
        parts.forEach((name, part) -> {
            this.parts.put(name, VoxelShapes.or(part));
        });
    }
    
    public VoxelShapeGroup clone() {
        return new VoxelShapeGroup(this.shape, this.parts);
    }
    
    public VoxelShapeGroup transform(Mat4 matrix, boolean transformVariants) {
        this.shape = transformShape(this.shape, matrix);
        if(transformVariants)
            this.parts.forEach((key, variant) -> this.parts.put(key, transformShape(variant, matrix)));
        return this;
    }
    
    public VoxelShapeGroup transform(Mat4 matrix) {
        return this.transform(matrix, true);
    }
    
    private VoxelShape transformShape(VoxelShape shape, Mat4 matrix) {
        VoxelShape transformed[] = { VoxelShapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Vec3 min = new Vec3(minX, minY, minZ).transform(matrix);
            Vec3 max = new Vec3(maxX, maxY, maxZ).transform(matrix);
            VoxelShape box = VoxelShapes.box(min.x, min.y, min.z, max.x, max.y, max.z);
            transformed[0] = VoxelShapes.or(transformed[0], box);
        });
        return transformed[0];
    }
    
    public VoxelShape getVariant(String key) {
        return VoxelShapes.or(this.shape, this.getPart(key));
    }
    
    public VoxelShape getPart(String key) {
        if(this.parts.containsKey(key))
            return VoxelShapes.or(this.parts.get(key));
        return VoxelShapes.empty();
    }
    
    public VoxelShape getShape() {
        return this.shape;
    }
    
    public static class Builder {
        private VoxelShape shape = VoxelShapes.empty();
        private Map<String, VoxelShape> parts = new HashMap<>();
        
        public Builder() {}
        
        public Builder(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.add(VoxelShapes.box(minX / 16f, minY / 16f, minZ / 16f, maxX / 16f, maxY / 16f, maxZ / 16f));
        }
        
        public Builder add(VoxelShape shape) {
            this.shape = VoxelShapes.or(this.shape, shape);
            return this;
        }
        
        public Builder add(VoxelShapeGroup shape) {
            return this.add(shape.shape);
        }
        
        public Builder add(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            return this.add(VoxelShapes.box(minX / 16f, minY / 16f, minZ / 16f, maxX / 16f, maxY / 16f, maxZ / 16f));
        }
        
        public Builder addPart(String key, VoxelShape part) {
            this.parts.put(key, part);
            return this;
        }
        
        public Builder addPart(String key, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            return this.addPart(key, VoxelShapes.box(minX / 16f, minY / 16f, minZ / 16f, maxX / 16f, maxY / 16f, maxZ / 16f));
        }
        
        public VoxelShapeGroup build() {
            return new VoxelShapeGroup(this.shape, this.parts);
        }
    }
}