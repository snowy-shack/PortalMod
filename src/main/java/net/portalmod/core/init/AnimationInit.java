package net.portalmod.core.init;

import net.portalmod.client.animation.Animation;
import net.portalmod.client.animation.Animation.Curve;

public class AnimationInit {
    private AnimationInit() {}
    
    public static final Animation COMPRESSION = new Animation.Builder()
    .keyframe(Curve.CUBIC, 3, 100)
    .keyframe(Curve.QUADRATIC, 0, 400)
    .build();

    public static final Animation CLAWS = new Animation.Builder(-1.5708)
    .keyframe(Curve.CUBIC, -2.2689, 100)
    .keyframe(Curve.QUADRATIC, -1.5708, 400)
    .build();

    public static final Animation RECOIL_X = new Animation.Builder()
    .keyframe(Curve.CUBIC, 30, 100)
    .keyframe(Curve.QUADRATIC, 0, 400)
    .build();

    public static final Animation RECOIL_Y = new Animation.Builder()
    .keyframe(Curve.QUADRATIC, 15, 200)
    .keyframe(Curve.QUADRATIC, 0, 300)
    .build();

    public static final Animation FIZZLE_BODY = new Animation.Builder()
    .keyframe(Curve.LINEAR,  10, 100)
    .keyframe(Curve.LINEAR, -10, 100)
    .keyframe(Curve.LINEAR,   5, 100)
    .keyframe(Curve.LINEAR,  -5, 100)
    .keyframe(Curve.LINEAR,   0, 100)
    .build();

    public static final Animation FAITHPLATE_BONE = new Animation.Builder(-0.2182f)
    .keyframe(Curve.QUADRATIC, .1f, 150)
    .keyframe(Curve.QUADRATIC, -0.2182f, 600)
    .build();

    public static final Animation FAITHPLATE_ARM = new Animation.Builder(0.3491f)
    .keyframe(Curve.QUADRATIC, -.3f, 150)
    .keyframe(Curve.QUADRATIC, 0.3491f, 600)
    .build();

    public static final Animation FAITHPLATE_PLATE = new Animation.Builder(0.2618f)
    .keyframe(Curve.QUADRATIC, -.7f, 150)
    .keyframe(Curve.QUADRATIC, 0.2618f, 600)
    .build();

    public static final Animation FAITHPLATE_LOCK = new Animation.Builder(0)
    .keyframe(Curve.QUADRATIC, -1.2f, 150)
    .keyframe(Curve.QUADRATIC, 0, 600)
    .build();
}