package io.github.serialsniper.portalmod.client.util;

import com.mojang.blaze3d.matrix.*;
import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import net.minecraft.block.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.*;

public class PortalLocation {
    private Matrix4f transform;
    private Direction side;
    private PortalEnd end;
    private BlockPos pos;
    private BlockPos otherPos;
    private TileEntity tileEntity;
    private MatrixStack stack;
    private BlockState state;

    public PortalLocation(Matrix4f transform, Direction side, PortalEnd end, BlockPos pos, BlockPos otherPos, TileEntity tileEntity, MatrixStack stack, BlockState state) {
        this.transform = transform;
        this.side = side;
        this.end = end;
        this.pos = pos;
        this.otherPos = otherPos;
        this.tileEntity = tileEntity;
        this.stack = stack;
        this.state = state;
    }

    public Matrix4f getTransform() {
        return transform;
    }

    public Direction getSide() {
        return side;
    }

    public PortalEnd getEnd() {
        return end;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockPos getOtherPos() {
        return otherPos;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public MatrixStack getStack() {
        return stack;
    }

    public BlockState getState() {
        return state;
    }
}