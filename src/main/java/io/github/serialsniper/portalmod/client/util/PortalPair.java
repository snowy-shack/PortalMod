package io.github.serialsniper.portalmod.client.util;

import io.github.serialsniper.portalmod.core.enums.PortalEnd;
import net.minecraft.util.math.*;

public class PortalPair {
    private BlockPos blue;
    private BlockPos orange;

    public void setBlue(BlockPos pos) {
        this.blue = pos;
    }

    public void setOrange(BlockPos pos) {
        this.orange = pos;
    }

    public void set(PortalEnd end, BlockPos pos) {
        switch(end) {
            case BLUE:
                setBlue(pos);
                break;

            case ORANGE:
                setOrange(pos);
                break;
        }
    }

    public boolean hasBlue() {
        return blue != null;
    }

    public boolean hasOrange() {
        return orange != null;
    }

    public boolean has(PortalEnd end) {
        switch(end) {
            case BLUE: return hasBlue();
            case ORANGE: return hasOrange();
        }

        return false;
    }

    public BlockPos getBlue() {
        return blue;
    }

    public BlockPos getOrange() {
        return orange;
    }

    public BlockPos get(PortalEnd end) {
        switch(end) {
            case BLUE: return getBlue();
            case ORANGE: return getOrange();
        }

        return BlockPos.ZERO;
    }
}