package net.portalmod.mixins.renderer;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.interfaces.PMActiveRenderInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ActiveRenderInfo.class)
public abstract class ActiveRenderInfoMixin implements PMActiveRenderInfo {
    @Shadow private boolean initialized;

    @Shadow private IBlockReader level;

    @Shadow private Entity entity;

    @Shadow private boolean detached;

    @Shadow private boolean mirror;

    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Shadow public abstract void setPosition(Vector3d position);

    @Shadow protected abstract void move(double x, double y, double z);

    @Shadow protected abstract double getMaxZoom(double zoom);

    @Shadow private Vector3d position;

    @Shadow @Final private Vector3f forwards;

    @Override
    public void pmSetupForOrtho(IBlockReader level, Vec3 position, float yRot, float xRot, float zoom) {
        this.initialized = true;
        this.level = level;
        this.entity = null;
        this.detached = true;
        this.mirror = false;
        this.setRotation(yRot, xRot);
        this.setPosition(position.to3d());
        this.move(-this.pmGetMaxZoom(zoom), 0.0D, 0.0D);
    }

    private double pmGetMaxZoom(double zoom) {
        for(int i = 0; i < 8; ++i) {
            float f = (float)((i & 1) * 2 - 1);
            float f1 = (float)((i >> 1 & 1) * 2 - 1);
            float f2 = (float)((i >> 2 & 1) * 2 - 1);
            f = f * 0.1F;
            f1 = f1 * 0.1F;
            f2 = f2 * 0.1F;
            Vector3d vector3d = this.position.add(f, f1, f2);
            Vector3d vector3d1 = new Vector3d(this.position.x - (double)this.forwards.x() * zoom + (double)f + (double)f2, this.position.y - (double)this.forwards.y() * zoom + (double)f1, this.position.z - (double)this.forwards.z() * zoom + (double)f2);
            RayTraceResult raytraceresult = this.level.clip(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, this.entity));
            if(raytraceresult.getType() != RayTraceResult.Type.MISS) {
                double d0 = raytraceresult.getLocation().distanceTo(this.position);
                if(d0 < zoom) {
                    zoom = d0;
                }
            }
        }

        return zoom;
    }
}