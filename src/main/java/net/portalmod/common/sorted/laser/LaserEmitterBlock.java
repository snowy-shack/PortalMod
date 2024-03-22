package net.portalmod.common.sorted.laser;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

public class LaserEmitterBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    
    private static final Map<Direction, VoxelShapeGroup> SHAPES = new HashMap<>();
    
    public LaserEmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP));
        this.initAABBs();
    }
    
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    private void initAABBs() {
        VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
                .add(0, 0, 0, 16, 1, 16)
                .add(3, 0, 3, 13, 3, 13)
                .build();
        
        for(Direction facing : Direction.values()) {
            Mat4 matrix = Mat4.identity();
            matrix.translate(new Vec3(.5));
            
            if(facing.getAxisDirection() == AxisDirection.POSITIVE)
                matrix.scale(new Vec3(facing.getNormal()).mul(2).add(1));
            
            if(facing.getAxis() == Axis.X)
                matrix.rotateDeg(Vector3f.ZP, -90);
            
            if(facing.getAxis() == Axis.Z)
                matrix.rotateDeg(Vector3f.XP, 90);
            
            matrix.translate(new Vec3(-.5));
            
            SHAPES.put(facing, shape.clone().transform(matrix));
        }
    }
    
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.getValue(FACING)).getShape();
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
       return PushReaction.DESTROY;
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}