package net.portalmod.common.sorted.portal;

import net.minecraft.util.math.MathHelper;
import net.portalmod.core.math.Vec3;

import java.util.Optional;

public class CameraAnimator {
    private static CameraAnimator instance;

    private float pitchStart = 0;
    private float pitchEnd = 0;
    private long pitchStartTime = -1;
    private int pitchAnimationDuration;

    private float yawStart = 0;
    private float yawEnd = 0;
    private long yawStartTime = -1;
    private int yawAnimationDuration;

    private float rollStart = 0;
    private float rollEnd = 0;
    private long rollStartTime = -1;
    private int rollAnimationDuration;

    private Vec3 posStart = Vec3.origin();
    private Vec3 posEnd = Vec3.origin();
    private long posStartTime = -1;
    private int posAnimationDuration;

    private CameraAnimator() {}

    public static CameraAnimator getInstance() {
        if(instance == null)
            instance = new CameraAnimator();
        return instance;
    }

    public void startPitchAnimation(float pitchStart, float pitchEnd, int animationDuration, boolean shallowestAngle) {
        if(shallowestAngle) {
            pitchStart = normalizeAngle(pitchStart);
            pitchEnd = normalizeAngle(pitchEnd);

            float diff = pitchStart - pitchEnd;
            if(diff > 360 - diff)
                pitchStart -= 360;
        }

        this.pitchStart = pitchStart;
        this.pitchEnd = pitchEnd;
        this.pitchStartTime = System.currentTimeMillis();
        this.pitchAnimationDuration = animationDuration;
    }

    public void startYawAnimation(float yawStart, float yawEnd, int animationDuration, boolean shallowestAngle) {
        if(shallowestAngle) {
            yawStart = normalizeAngle(yawStart);
            yawEnd = normalizeAngle(yawEnd);

            float diff = yawStart - yawEnd;
            if(diff > 360 - diff)
                yawStart -= 360;
        }

        this.yawStart = yawStart;
        this.yawEnd = yawEnd;
        this.yawStartTime = System.currentTimeMillis();
        this.yawAnimationDuration = animationDuration;
    }

    public void startRollAnimation(float rollStart, float rollEnd, int animationDuration, boolean shallowestAngle) {
        if(shallowestAngle) {
            rollStart = normalizeAngle(rollStart);
            rollEnd = normalizeAngle(rollEnd);

            float diff = rollStart - rollEnd;
            if(diff > 360 - diff)
                rollStart -= 360;
        }

        this.rollStart = rollStart;
        this.rollEnd = rollEnd;
        this.rollStartTime = System.currentTimeMillis();
        this.rollAnimationDuration = animationDuration;
    }

    public void startPosAnimation(Vec3 posStart, Vec3 posEnd, int animationDuration) {
        this.posStart = posStart;
        this.posEnd = posEnd;
        this.posStartTime = System.currentTimeMillis();
        this.posAnimationDuration = animationDuration;
    }

    public static float normalizeAngle(float angle) {
        while(angle >= 360)
            angle -= 360;

        while(angle < 0)
            angle += 360;

        return angle;
    }

    private Optional<Float> getFactor(long startTime, int duration) {
        long delta = System.currentTimeMillis() - startTime;

        if(startTime > -1 && delta <= duration * 2L) {
            float factor = (float)delta / duration;
            float easedFactor = 1 - (float)Math.exp(-factor * 3);
            return Optional.of(easedFactor);
        }

        return Optional.empty();
    }

    public Optional<Float> getRelativePitch() {
        return getFactor(this.pitchStartTime, pitchAnimationDuration)
                .map(x -> MathHelper.lerp(x, this.pitchStart - this.pitchEnd, 0));
    }

    public Optional<Float> getRelativeYaw() {
        return getFactor(this.yawStartTime, yawAnimationDuration)
                .map(x -> MathHelper.lerp(x, this.yawStart - this.yawEnd, 0));
    }

    public Optional<Float> getRelativeRoll() {
        return getFactor(this.rollStartTime, rollAnimationDuration)
                .map(x -> MathHelper.lerp(x, this.rollStart - this.rollEnd, 0));
    }

    public Optional<Vec3> getRelativePos() {
        return getFactor(this.posStartTime, posAnimationDuration)
                .map(x -> this.posStart.clone().sub(this.posEnd).lerp(Vec3.origin(), x));
    }
}