package net.portalmod.common.sorted.door;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.indicator.IndicatorActivated;
import net.portalmod.common.sorted.antline.indicator.IndicatorInfo;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChamberDoorTileEntity extends TileEntity implements ITickableTileEntity, IndicatorActivated {

    public ChamberDoorTileEntity() {
        super(TileEntityTypeInit.CHAMBER_DOOR.get());
    }

    @Override
    public void tick() {
        BlockState blockState = this.getBlockState();
        if (!(blockState.getBlock() instanceof ChamberDoorBlock)) {
            return;
        }
        ChamberDoorBlock doorBlock = (ChamberDoorBlock) blockState.getBlock();
        BlockPos pos = this.getBlockPos();
        World world = this.level;
        Direction facing = blockState.getValue(ChamberDoorBlock.FACING);

        IndicatorInfo indicatorInfo = this.checkIndicators(blockState, world, pos);
        boolean isOpen = blockState.getValue(ChamberDoorBlock.OPEN);

        // Powered by indicators
        if (indicatorInfo.hasIndicators) {
            if (isOpen != indicatorInfo.allIndicatorsActivated) {
                doorBlock.setOpen(indicatorInfo.allIndicatorsActivated, blockState, world, pos);
            }
            return;
        }

        Vector3d middlePos = ChamberDoorBlock.getExactMiddlePos(blockState, pos);
        int changeProximity = isOpen ? 4 : 3;  // Open in 3 blocks, close in 4

        // Powered by nearby player
        boolean hasNearbyPlayer = false;
        for (PlayerEntity player : world.players()) {
            if (player.isSpectator()) {
                continue;
            }
            boolean inFront = player.position().subtract(middlePos).multiply(1, 0, 1).dot(new Vec3(facing.getNormal()).to3d()) > 0;
            double playerDistance = player.position().distanceTo((middlePos));
            if (playerDistance < changeProximity && inFront || isOpen && playerDistance < 1.5) {
                hasNearbyPlayer = true;
                break;
            }
        }

        if (isOpen != hasNearbyPlayer) {
            doorBlock.setOpen(hasNearbyPlayer, blockState, world, pos);
        }
    }

    public boolean isAutomatic() {
        IndicatorInfo indicatorInfo = this.checkIndicators(this.getBlockState(), this.level, this.getBlockPos());
        return !indicatorInfo.hasIndicators;
    }

    @Override
    public List<BlockPos> getIndicatorPositions(BlockState blockState, World world, BlockPos pos) {
        Direction facing = blockState.getValue(ChamberDoorBlock.FACING);
        boolean isLower = blockState.getValue(ChamberDoorBlock.HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = blockState.getValue(ChamberDoorBlock.SIDE) == ChamberDoorBlock.Side.LEFT;

        Direction verticalDirection = isLower ? Direction.UP : Direction.DOWN;
        Direction horizontalDirection = isLeft ? facing.getCounterClockWise() : facing.getClockWise();

        List<BlockPos> possibleIndicatorPositions = new ArrayList<>();
        possibleIndicatorPositions.addAll(getSurroundingPositions(pos.relative(facing), verticalDirection, horizontalDirection));
        possibleIndicatorPositions.addAll(getDoorPositions(pos.relative(facing), verticalDirection, horizontalDirection)); // fill the gap
        possibleIndicatorPositions.addAll(getOuterCornerPositions(pos, verticalDirection, horizontalDirection));
        possibleIndicatorPositions.addAll(getOuterCornerPositions(pos.relative(facing.getOpposite()), verticalDirection, horizontalDirection));
        return possibleIndicatorPositions;
    }

    /*
    horizontal >
    vertical ^

    5      6       7      8
    4     door    door    9
    3     main    door    10
    2      1       12     11

     */
    public static List<BlockPos> getSurroundingPositions(BlockPos pos, Direction vertical, Direction horizontal) {
        return new ArrayList<>(Arrays.asList(
                pos.relative(vertical.getOpposite()),
                pos.relative(horizontal.getOpposite()).relative(vertical.getOpposite()),
                pos.relative(horizontal.getOpposite()),
                pos.relative(horizontal.getOpposite()).relative(vertical),
                pos.relative(horizontal.getOpposite()).relative(vertical, 2),
                pos.relative(vertical, 2),
                pos.relative(horizontal).relative(vertical, 2),
                pos.relative(horizontal, 2).relative(vertical, 2),
                pos.relative(horizontal, 2).relative(vertical),
                pos.relative(horizontal, 2),
                pos.relative(horizontal, 2).relative(vertical.getOpposite()),
                pos.relative(horizontal).relative(vertical.getOpposite())
        ));
    }

    public static List<BlockPos> getDoorPositions(BlockPos pos, Direction vertical, Direction horizontal) {
        return new ArrayList<>(Arrays.asList(
                pos.relative(horizontal),
                pos.relative(horizontal).relative(vertical),
                pos.relative(vertical),
                pos
        ));
    }

    /*

    2              3
       door  door
       main  door
    1              4

     */
    public static List<BlockPos> getOuterCornerPositions(BlockPos pos, Direction vertical, Direction horizontal) {
        return new ArrayList<>(Arrays.asList(
                pos.relative(horizontal.getOpposite()).relative(vertical.getOpposite()),
                pos.relative(horizontal.getOpposite()).relative(vertical, 2),
                pos.relative(horizontal, 2).relative(vertical, 2),
                pos.relative(horizontal, 2).relative(vertical.getOpposite())
        ));
    }
}
