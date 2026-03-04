package net.portalmod.common.sorted.trigger;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.portalmod.core.init.PacketInit;

public class TriggerSelectionClient {

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
        if (isSelectingEnd()) {
            selectingEnd = pos.subtract(selected.getBlockPos());
        } else {
            selectingStart = pos.subtract(selected.getBlockPos());
        }
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