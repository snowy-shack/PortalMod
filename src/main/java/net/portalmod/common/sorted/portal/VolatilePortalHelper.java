package net.portalmod.common.sorted.portal;

import net.minecraft.util.Direction;
import net.portalmod.core.math.Vec3;

public class VolatilePortalHelper implements AbstractPortalHelper {
    private static final float TOLERANCE = 0.01f;

    public final Vec3 position;
    public final Direction normal;
    public final float radius;

    public VolatilePortalHelper(Vec3 position, Direction normal, float radius) {
        this.position = position;
        this.normal = normal;
        this.radius = radius;
    }

    public boolean willHelpPortal(Vec3 hitPos, Direction face) {
        if(face != this.normal)
            return false;

        if(hitPos.clone().sub(this.position).magnitudeSqr() > radius * radius * 4)
            return false;

        double zDistance = Math.abs(hitPos.choose(face.getAxis()) - this.position.choose(face.getAxis()));
        if(zDistance > TOLERANCE)
            return false;

        double dx = Math.abs(hitPos.x - this.position.x);
        double dy = Math.abs(hitPos.y - this.position.y);
        double dz = Math.abs(hitPos.z - this.position.z);

        return Math.max(dy, Math.max(dx, dz)) <= radius;
    }

    public Vec3 helpPortal(Vec3 hitPos, Direction face) {
        if(!this.willHelpPortal(hitPos, face))
            return hitPos;

        Vec3 newPos = this.position.clone();
        newPos.set(face.getAxis(), hitPos.choose(face.getAxis()));
        return newPos;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof VolatilePortalHelper))
            return false;

        VolatilePortalHelper other = (VolatilePortalHelper)obj;
        return other.position.equals(this.position) && other.normal.equals(this.normal) && other.radius == this.radius;
    }
}