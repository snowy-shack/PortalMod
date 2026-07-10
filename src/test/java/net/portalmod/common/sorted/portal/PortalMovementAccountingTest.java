package net.portalmod.common.sorted.portal;

import org.junit.Assert;
import org.junit.Test;

public class PortalMovementAccountingTest {
    @Test
    public void suppressesMovementExhaustionForPortalTeleportDistance() {
        Assert.assertTrue(PortalMovementAccounting.shouldSuppressMovementExhaustion(true, 10.0F));
    }

    @Test
    public void keepsNormalMovementExhaustion() {
        Assert.assertFalse(PortalMovementAccounting.shouldSuppressMovementExhaustion(false, 0.1F));
    }

    @Test
    public void keepsNonPositiveExhaustionEvenAfterPortal() {
        Assert.assertFalse(PortalMovementAccounting.shouldSuppressMovementExhaustion(true, 0.0F));
    }
}
