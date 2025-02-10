package net.portalmod.client.animation;

import java.util.ArrayList;
import java.util.List;

public class Animation {
    private final List<Part> parts;
    private long start;

    private Animation(List<Part> parts) {
        this.parts = parts;
        this.start = 0;
    }

    public void start(long millis) {
        this.start = millis;
    }

    public void start() {
        this.start(System.currentTimeMillis());
    }

    public void stop() {
        this.start = (long) (System.currentTimeMillis() - 1000000000000.0);
    }

    public double computeAbs(long time) {
        double accum = 0;
        Part selected = null;

        for(Part part : parts) {
            if(time < accum + part.duration) {
                selected = part;
                break;
            }
            accum += part.duration;
        }

        if(selected == null)
            return parts.get(parts.size() - 1).end;
        return selected.compute(time - accum);
    }

    public double compute(long time) {
        return computeAbs(time - start);
    }

    public static class Builder {
        private final List<Part> parts;
        private double lastValue;

        public Builder(double startValue) {
            this.parts = new ArrayList<>();
            this.lastValue = startValue;
        }

        public Builder() {
            this(0);
        }

        public Builder segment(Curve type, double start, double end, double duration) {
            parts.add(new Part(type, start, end, duration));
            lastValue = end;
            return this;
        }

        public Builder keyframe(Curve type, double end, double duration) {
            return this.segment(type, lastValue, end, duration);
        }

        public Animation build() {
            return new Animation(parts);
        }
    }

    private static class Part {
        private final Curve type;
        private final double start;
        private final double end;
        private final double duration;

        private Part(Curve type, double start, double end, double duration) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.duration = duration;
        }

        private double compute(double x) {
            return (end - start) * (1. - Math.pow(1. - x / duration, type.exp)) + start;
        }
    }

    public enum Curve {
        INV_CUBIC(1/3f),
        INV_QUADRATIC(1/2f),
        LINEAR(1),
        QUADRATIC(2),
        CUBIC(3);

        private final float exp;

        Curve(float exp) {
            this.exp = exp;
        }
    }
}