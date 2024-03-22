package net.portalmod.common.sorted.portal;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public class DiscontinuousLerpPos {
    private final Vector3d from;
    private Vector3d to;
    private int ticks = 1;
    private int tick = 1;

    public DiscontinuousLerpPos(Vector3d from, Vector3d to, int ticks) {
        this.from = from;
        this.to = to;
        this.ticks = ticks;
    }

    public DiscontinuousLerpPos(Vector3d from) {
        this.from = from;
    }

    public void extendLerp(Vector3d to) {
        if(!this.isIncomplete())
            this.ticks++;
        this.to = to;
    }

    public boolean isExtended() {
        return this.ticks > 1;
    }

    public boolean isDone() {
        return this.tick == this.ticks;
    }

    public void consume() {
        this.tick++;
    }

    public void apply(Entity entity) {
        double fragmentX = (this.to.x - this.from.x) / this.ticks;
        double fragmentY = (this.to.y - this.from.y) / this.ticks;
        double fragmentZ = (this.to.z - this.from.x) / this.ticks;
        double x = this.from.x + fragmentX * this.tick;
        double y = this.from.y + fragmentY * this.tick;
        double z = this.from.z + fragmentZ * this.tick;
        double xo = this.from.x + fragmentX * (this.tick - 1);
        double yo = this.from.y + fragmentY * (this.tick - 1);
        double zo = this.from.z + fragmentZ * (this.tick - 1);
        entity.setPos(x, y, z);
        entity.xo = xo;
        entity.yo = yo;
        entity.zo = zo;
        entity.xOld = xo;
        entity.yOld = yo;
        entity.zOld = zo;
    }

    public Vector3d getFrom() {
        return this.from;
    }

    public Vector3d getTo() {
        return this.to;
    }

    public boolean isIncomplete() {
        return this.to == null;
    }

    public int getTicks() {
        return this.ticks;
    }
}