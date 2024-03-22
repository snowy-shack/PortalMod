package net.portalmod.core.interfaces;

import net.portalmod.common.sorted.portal.DiscontinuousLerpPos;

import java.util.Deque;

public interface IDiscontinuouslyLerpable {
    Deque<DiscontinuousLerpPos> getLerpPosQueue();
}