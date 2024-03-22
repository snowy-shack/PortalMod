package net.portalmod.core.util;

public class ChangeDetector {
    private boolean previous = false;
    private boolean current = false;
    
    public ChangeDetector() {}
    
    private void shift() {
        this.previous = this.current;
    }
    
    public ChangeDetector trigger(boolean value) {
        this.shift();
        this.current = value;
        return this;
    }
    
    public ChangeDetector trigger() {
        return this.trigger(true);
    }
    
    public boolean get() {
        return this.previous != this.current;
    }
}