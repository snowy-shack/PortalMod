package net.portalmod.common.sorted.laser;

import net.portalmod.core.math.VoxelShapeBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public class LaserRelayBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    
//    private static final HashMap<Direction, VoxelShapeGroup> SHAPES = new HashMap<>();
    private static final VoxelShape SHAPE = new VoxelShapeBuilder()
            .add(0, 0, 0, 16, 1.9, 16)
            .add(3, 0, 3, 13, 14, 13)
            .build();
    
    public LaserRelayBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(stateDefinition.any()
                .setValue(ACTIVE, false));
//        this.initAABBs();
    }
    
//    private void initAABBs() {
//        SHAPE = new VoxelShapeBuilder()
//                .add(0, 0, 0, 16, 1.9, 16)
//                .add(3, 0, 3, 13, 14, 13)
//                .build();
//
////        VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
////                .add(0, 0, 0, 12, 3, 12)
////                .add(0, 0, 12, 2.5f, 2, 16)
////                .add(12, 0, 0, 16, 2, 2.5f)
////                .build();
////        
////        for(Direction facing : Direction.values()) {
////            Mat4 matrix = Mat4.identity();
////            matrix.translate(new Vec3(.5));
////
////            if(facing.getAxisDirection() == AxisDirection.NEGATIVE)
////                matrix.scale(new Vec3(facing.step()).mul(2).add(1));
////
////            if(facing.getAxis() == Axis.X)
////                matrix.rotateDeg(Vector3f.ZP, -90);
////
////            if(facing.getAxis() == Axis.Z)
////                matrix.rotateDeg(Vector3f.XP, 90);
////
//////            matrix.rotateDeg(Vector3f.YP, 90);
////            matrix.translate(new Vec3(-.5));
////
////            SHAPES.put(facing, shape.clone().transform(matrix));
////        }
//    }
    
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
       return PushReaction.DESTROY;
    }
}