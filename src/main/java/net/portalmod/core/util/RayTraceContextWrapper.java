package net.portalmod.core.util;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class RayTraceContextWrapper extends RayTraceContext {
    private final RayTraceContext wrapped;
    private Vector3d from;
    private Vector3d to;

    public RayTraceContextWrapper(RayTraceContext context) {
        super(Vector3d.ZERO, Vector3d.ZERO, BlockMode.COLLIDER, FluidMode.ANY, null);
        this.wrapped = context;
    }

    public void setTo(Vector3d to) {
        this.to = to;
    }

    public void setFrom(Vector3d from) {
        this.from = from;
    }

    public Vector3d getTo() {
        return this.to != null ? this.to : this.wrapped.getTo();
    }

    public Vector3d getFrom() {
        return this.from != null ? this.from : this.wrapped.getFrom();
    }

    public VoxelShape getBlockShape(BlockState state, IBlockReader level, BlockPos pos) {
        return this.wrapped.getBlockShape(state, level, pos);
    }

    public VoxelShape getFluidShape(FluidState state, IBlockReader level, BlockPos pos) {
        return this.wrapped.getFluidShape(state, level, pos);
    }
}