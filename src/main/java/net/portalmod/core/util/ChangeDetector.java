package net.portalmod.core.util;

public class ChangeDetector {
    private boolean previous = false;
    private boolean current = false;
    
    public ChangeDetector() {}
    
    public void shift() {
        this.previous = this.current;
    }
    
    public ChangeDetector trigger(boolean value) {
        this.shift();
        this.current = value;
        return this;
    }

    public ChangeDetector set(boolean value) {
        this.current = value;
        return this;
    }
    
    public ChangeDetector trigger() {
        return this.trigger(true);
    }
    
    public boolean get() {
        return this.previous != this.current;
    }

    public boolean isRising() {
        return !this.previous && this.current;
    }

    public boolean isFalling() {
        return this.previous && !this.current;
    }
}