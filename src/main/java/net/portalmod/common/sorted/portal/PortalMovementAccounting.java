package net.portalmod.common.sorted.portal;

public final class PortalMovementAccounting {
    private PortalMovementAccounting() {}

    public static boolean shouldSuppressMovementExhaustion(boolean justPortaled, float exhaustion) {
        return justPortaled && exhaustion > 0.0F;
    }
}
