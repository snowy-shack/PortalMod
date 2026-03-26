package net.portalmod.common.sorted.trigger;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.portalmod.core.init.PacketInit;

public class TriggerSelectionClient {
    public static final int MAX_FIELD_SIZE = 32;
    public static final int MAX_DISTANCE_FROM_BLOCK = 32;

    public static TriggerTileEntity selected;
    public static BlockPos selectingStart;
    public static BlockPos selectingEnd;

    public static boolean isSelecting() {
        return selected != null;
    }

    public static boolean isSelectingStart() {
        return selected != null && selectingStart != null && selectingEnd == null;
    }

    public static boolean isSelectingEnd() {
        return selected != null && selectingStart != null && selectingEnd != null;
    }

    public static boolean isSelecting(TriggerTileEntity trigger) {
        return selected == trigger;
    }

    public static void startSelecting(TriggerTileEntity trigger) {
        selected = trigger;
        selectingStart = null;
        selectingEnd = null;
    }

    public static void confirmSelection() {
        if (isSelectingStart()) {
            selectingEnd = new BlockPos(selectingStart);
        } else if (isSelectingEnd()) {
            PacketInit.INSTANCE.sendToServer(new CTriggerEndConfigPacket(
                    TriggerSelectionClient.selected.getBlockPos(),
                    TriggerSelectionClient.selectingStart,
                    TriggerSelectionClient.selectingEnd
            ));

            stopSelecting();
        }
    }

    public static void abort() {
        PacketInit.INSTANCE.sendToServer(new CTriggerAbortConfigPacket(TriggerSelectionClient.selected.getBlockPos()));
        stopSelecting();
    }

    public static void stopSelecting() {
        selected = null;
        selectingStart = null;
        selectingEnd = null;
    }

    public static void updateSelectedPos(BlockPos pos) {
        if (selected == null || selected.isRemoved()) {
            abort();
            return;
        }

        BlockPos blockEntityPos = selected.getBlockPos();

        if (isSelectingEnd()) {
            selectingEnd = limitPosToDistance(
                    limitPosToDistance(pos, selectingStart.offset(blockEntityPos), MAX_FIELD_SIZE - 1),
                    blockEntityPos,
                    MAX_DISTANCE_FROM_BLOCK - 1
            ).subtract(blockEntityPos);
        } else {
            selectingStart = limitPosToDistance(pos, blockEntityPos, MAX_DISTANCE_FROM_BLOCK - 1).subtract(blockEntityPos);
        }
    }

    public static BlockPos limitPosToDistance(BlockPos pos, BlockPos origin, int distance) {
        BlockPos min = origin.offset(-distance, -distance, -distance);
        BlockPos max = origin.offset(distance, distance, distance);

        BlockPos.Mutable result = pos.mutable();
        result.clamp(Direction.Axis.X, min.getX(), max.getX());
        result.clamp(Direction.Axis.Y, min.getY(), max.getY());
        result.clamp(Direction.Axis.Z, min.getZ(), max.getZ());
        return result.immutable();
    }

    public static AxisAlignedBB getBox() {
        if (isSelectingStart()) {
            return new AxisAlignedBB(selectingStart);
        }

        if (isSelectingEnd()) {
            return new AxisAlignedBB(selectingStart, selectingEnd).expandTowards(1, 1, 1);
        }

        return null;
    }
}