package net.portalmod.common.sorted.portal;

import java.util.function.Consumer;

public class PortalPair {
    private PortalEntity blue, orange;

    public PortalPair() {}

    public PortalPair(PortalEntity blue, PortalEntity orange) {
        this.blue = blue;
        this.orange = orange;
    }

    public boolean has(PortalEnd end) {
        return this.get(end) != null;
    }

    public PortalEntity get(PortalEnd end) {
        if(end == PortalEnd.PRIMARY)
            return blue;
        if(end == PortalEnd.SECONDARY)
            return orange;
        return null;
    }

    public void computeIfPresent(PortalEnd end, Consumer<PortalEntity> procedure) {
        if(this.get(end) != null)
            procedure.accept(this.get(end));
    }
    
    public boolean isEmpty() {
        return this.blue == null && this.orange == null;
    }
    
    public boolean isFull() {
        return this.blue != null && this.orange != null;
    }
    
    public void set(PortalEnd end, PortalEntity portal) {
        if(end == PortalEnd.PRIMARY)
            this.blue = portal;
        if(end == PortalEnd.SECONDARY)
            this.orange = portal;
    }
    
    public void remove(PortalEntity portal) {
        if(this.blue == portal)
            this.blue = null;
        if(this.orange == portal)
            this.orange = null;
    }
}