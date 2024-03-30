package net.portalmod.common.sorted.chamberdoor;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.AntlineIndicatorBlock;
import net.portalmod.core.init.TileEntityTypeInit;
import net.portalmod.core.math.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChamberDoorTileEntity extends TileEntity implements ITickableTileEntity {

    public ChamberDoorTileEntity() {
        super(TileEntityTypeInit.CHAMBER_DOOR.get());
    }

    @Override
    public void tick() {
        BlockState blockState = this.getBlockState();
        BlockPos pos = this.getBlockPos();
        World world = this.level;
        Direction facing = blockState.getValue(ChamberDoorBlock.FACING);

        List<BlockPos> possibleIndicatorPositions = new ArrayList<>();
        possibleIndicatorPositions.addAll(getSurroundingPositions(blockState, pos));
        possibleIndicatorPositions.addAll(getSurroundingPositions(blockState, pos.relative(facing)));
        possibleIndicatorPositions.addAll(getSurroundingPositions(blockState, pos.relative(facing.getOpposite())));

        boolean hasIndicators = false;
        boolean allIndicatorsActivated = true;

        for (BlockPos blockPos : possibleIndicatorPositions) {
            BlockState worldBlockState = world.getBlockState(blockPos);
            if (worldBlockState.getBlock() instanceof AntlineIndicatorBlock) {
                hasIndicators = true;
                if (!worldBlockState.getValue(AntlineIndicatorBlock.ACTIVE)) {
                    allIndicatorsActivated = false;
                }
            }
        }

        boolean isOpen = blockState.getValue(ChamberDoorBlock.OPEN);

        if (blockState.getValue(ChamberDoorBlock.POWERED)) {
            if (!isOpen) {
                ChamberDoorBlock.setOpen(true, blockState, world, pos);
            }
        }
        else if (hasIndicators) {
            if (isOpen != allIndicatorsActivated) {
                ChamberDoorBlock.setOpen(allIndicatorsActivated, blockState, world, pos);
            }
        }
        else {
            boolean hasNearbyPlayer = false;
            for (PlayerEntity player : world.players()) {
                if (player.isSpectator()) {
                    continue;
                }
                Vector3d middlePos = Vector3d.atBottomCenterOf(pos).add(new Vec3(facing.getCounterClockWise().getNormal()).mul(0.5).add(0, 1, 0).to3d());
                boolean inFront = player.position().subtract(middlePos).multiply(1, 0, 1).dot(new Vec3(facing.getNormal()).to3d()) > 0;
                double playerDistance = player.position().distanceTo((middlePos));
                int changeProximity = isOpen ? 5 : 3;  // Open in 3, close in 5
                if (playerDistance < changeProximity && inFront || playerDistance < 1.5 && isOpen) {
                    hasNearbyPlayer = true;
                    break;
                }
            }
            if (isOpen != hasNearbyPlayer) {
                ChamberDoorBlock.setOpen(hasNearbyPlayer, blockState, world, pos);
            }
        }
    }

    /*
    horizontal >
    vertical ^

    5   6   7    8
    4   15  14   9
    3   16  13   10
    2   1   12   11

     */
    public static List<BlockPos> getSurroundingPositions(BlockState blockState, BlockPos pos) {
        Direction facing = blockState.getValue(ChamberDoorBlock.FACING);
        boolean isLower = blockState.getValue(ChamberDoorBlock.HALF) == DoubleBlockHalf.LOWER;
        boolean isLeft = blockState.getValue(ChamberDoorBlock.SIDE) == ChamberDoorBlock.Side.LEFT;

        Direction vertical = isLower ? Direction.UP : Direction.DOWN;
        Direction horizontal = isLeft ? facing.getCounterClockWise() : facing.getClockWise();

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
                pos.relative(horizontal).relative(vertical.getOpposite()),
                pos.relative(horizontal),
                pos.relative(horizontal).relative(vertical),
                pos.relative(vertical),
                pos
        ));
    }
}
