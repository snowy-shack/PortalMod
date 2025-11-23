package net.portalmod.common.sorted.portal;

import javax.annotation.Nullable;

public class PartialPortalPair {
    private PartialPortal blue, orange;

    public PartialPortalPair() {}

    public PartialPortalPair(@Nullable PartialPortal blue, @Nullable PartialPortal orange) {
        this.blue = blue;
        this.orange = orange;
    }

    public PartialPortalPair(PortalPair pair) {
        if(pair.has(PortalEnd.PRIMARY))
            this.blue = new PartialPortal(pair.get(PortalEnd.PRIMARY));
        if(pair.has(PortalEnd.SECONDARY))
            this.orange = new PartialPortal(pair.get(PortalEnd.SECONDARY));
    }

    public boolean has(PortalEnd end) {
        return this.get(end) != null;
    }

    public PartialPortal get(PortalEnd end) {
        if(end == PortalEnd.PRIMARY)
            return blue;
        if(end == PortalEnd.SECONDARY)
            return orange;
        return null;
    }

    public boolean isEmpty() {
        return this.blue == null && this.orange == null;
    }

    public boolean isFull() {
        return this.blue != null && this.orange != null;
    }

    public void set(PortalEnd end, @Nullable PartialPortal portal) {
        if(end == PortalEnd.PRIMARY)
            this.blue = portal;
        if(end == PortalEnd.SECONDARY)
            this.orange = portal;
    }
}