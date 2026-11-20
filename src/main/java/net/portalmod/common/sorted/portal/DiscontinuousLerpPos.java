package net.portalmod.common.sorted.portal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class DiscontinuousLerpPos {
    private final Vec3 from;
    private Vec3 to;
    private int ticks = 1;
    private int tick = 1;

    public DiscontinuousLerpPos(Vec3 from, Vec3 to, int ticks) {
        this.from = from;
        this.to = to;
        this.ticks = ticks;
    }

    public DiscontinuousLerpPos(Vec3 from) {
        this.from = from;
    }

    public void extendLerp(Vec3 to) {
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

    public Vec3 getFrom() {
        return this.from;
    }

    public Vec3 getTo() {
        return this.to;
    }

    public boolean isIncomplete() {
        return this.to == null;
    }

    public int getTicks() {
        return this.ticks;
    }
}