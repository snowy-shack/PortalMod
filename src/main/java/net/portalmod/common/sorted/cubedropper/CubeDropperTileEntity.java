package net.portalmod.common.sorted.cubedropper;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.portalmod.common.sorted.antline.AntlineIndicatorBlock;
import net.portalmod.core.init.TileEntityTypeInit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CubeDropperTileEntity extends TileEntity implements ITickableTileEntity {
    public CubeDropperTileEntity() {
        super(TileEntityTypeInit.CUBE_DROPPER.get());
    }

    @Override
    public void tick() {
        BlockState blockState = this.getBlockState();
        if (!(blockState.getBlock() instanceof CubeDropperBlock)) {
            return;
        }
        CubeDropperBlock dropperBlock = (CubeDropperBlock) blockState.getBlock();
        BlockPos pos = this.getBlockPos();
        World world = this.level;

        List<BlockPos> possibleIndicatorPositions = new ArrayList<>();
        possibleIndicatorPositions.addAll(getSurroundingPositions(pos));
        possibleIndicatorPositions.addAll(getSurroundingPositions(pos.below()));

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

        boolean isOpen = blockState.getValue(CubeDropperBlock.OPEN);

        if (blockState.getValue(CubeDropperBlock.POWERED)) {
            if (!isOpen) {
                dropperBlock.setOpen(true, blockState, world, pos);
            }
        }
        else if (hasIndicators) {
            if (isOpen != allIndicatorsActivated) {
                dropperBlock.setOpen(allIndicatorsActivated, blockState, world, pos);
            }
        }
        else {
            if (isOpen) {
                dropperBlock.setOpen(false, blockState, world, pos);
            }
        }
    }

    /*
    horizontal >
    vertical ^

    5      6       7      8
    4   dropper  dropper  9
    3   updated  dropper  10
    2      1       12     11

     */
    public static List<BlockPos> getSurroundingPositions(BlockPos pos) {
        return new ArrayList<>(Arrays.asList(
                pos.south(),
                pos.south().west(),
                pos.west(),
                pos.north().west(),
                pos.north(2).west(),
                pos.north(2),
                pos.north(2).east(),
                pos.north(2).east(2),
                pos.north().east(2),
                pos.east(2),
                pos.south().east(2),
                pos.south().east()
        ));
    }
}
