package net.portalmod.common.sorted.antline.indicator;

public class IndicatorInfo {
    public final int totalIndicators;
    public final int activatedIndicators;
    public final boolean hasIndicators;
    public final boolean allIndicatorsActivated;

    public IndicatorInfo(int totalIndicators, int activatedIndicators) {
        this.totalIndicators = totalIndicators;
        this.activatedIndicators = activatedIndicators;
        this.hasIndicators = totalIndicators > 0;
        this.allIndicatorsActivated = totalIndicators == activatedIndicators;
    }
}
