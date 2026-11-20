package net.portalmod.core.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;

public class RayTraceContextWrapper extends ClipContext {
    private final ClipContext wrapped;
    private Vec3 from;
    private Vec3 to;

    public RayTraceContextWrapper(ClipContext context) {
        super(Vec3.ZERO, Vec3.ZERO, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null);
        this.wrapped = context;
    }

    public void setTo(Vec3 to) {
        this.to = to;
    }

    public void setFrom(Vec3 from) {
        this.from = from;
    }

    public Vec3 getTo() {
        return this.to != null ? this.to : this.wrapped.getTo();
    }

    public Vec3 getFrom() {
        return this.from != null ? this.from : this.wrapped.getFrom();
    }

    public VoxelShape getBlockShape(BlockState state, BlockGetter level, BlockPos pos) {
        return this.wrapped.getBlockShape(state, level, pos);
    }

    public VoxelShape getFluidShape(FluidState state, BlockGetter level, BlockPos pos) {
        return this.wrapped.getFluidShape(state, level, pos);
    }
}