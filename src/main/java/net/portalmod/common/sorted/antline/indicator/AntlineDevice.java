package net.portalmod.common.sorted.antline.indicator;

import net.portalmod.core.interfaces.CustomPushReaction;
import com.mojang.math.Axis;
import net.portalmod.common.sorted.antline.AntlineConnector;
import net.portalmod.core.math.BiHashMap;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.math.VoxelShapeGroup;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import net.minecraft.world.level.BlockGetter;

/**
 * The parent of all antline indicator things.
 * <p>
 * Handles the shape and rotation blockstates.
 */
public class AntlineDevice extends FaceAttachedHorizontalDirectionalBlock implements AntlineConnector, CustomPushReaction {
    private static final BiHashMap<Direction, AttachFace, VoxelShapeGroup> SHAPE = new BiHashMap<>();

    public AntlineDevice(Properties properties) {
        super(properties);
        this.initAABBs();
    }

    protected void initAABBs() {
        VoxelShapeGroup shape = new VoxelShapeGroup.Builder()
                .add(0, 3, 3, 2, 13, 13)
                .build();

        for(Direction facing : Direction.values()) {
            for(AttachFace attachFace : AttachFace.values()) {
                Mat4 matrix = Mat4.identity();
                matrix.translate(new Vec3(.5));

                if(attachFace != AttachFace.WALL) {
                    int angle = (attachFace == AttachFace.FLOOR) ? 90 : -90;
                    matrix.rotateDeg(Axis.ZP, angle);
                } else {
                    int angle = facing.get2DDataValue() * -90 - 90;
                    matrix.rotateDeg(Axis.YP, angle);
                }
                matrix.translate(new Vec3(-.5));

                SHAPE.put(facing, attachFace, shape.clone().transform(matrix));
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING), state.getValue(FACE)).getShape();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }

    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.DESTROY;
    }

    @Override
    public Direction getHorsedOn(BlockState state) {
        return getConnectedDirection(state).getOpposite();
    }

    @Override
    public boolean antlineConnectsInDirection(Direction direction, BlockState state) {
        return direction.getAxis() != getHorsedOn(state).getAxis();
    }
}

/*
Antline Device:
    - Connects to antline
    - Shape
    - Rotation

    Antline Output:
        - Activated by antline
        - Powers elements
        - Reversed property

        Antline Indicator
        Antline Timer

    Antline Icon:
        - Icon switching

        Antline Decoder:
            - Activated by antline
            - Powers redstone

        Antline Encoder:
            - Activated by redstone
            - Powers antline
 */
