package net.portalmod.common.sorted.portalgun;

import org.junit.Assert;
import org.junit.Test;

public class PortalGunCrosshairRendererTest {
    @Test
    public void centersOddSizedCrosshairOnEvenGuiDimensionLikeVanilla() {
        Assert.assertEquals(495, PortalGunCrosshairRenderer.centeredCrosshairOrigin(1024, 33));
    }

    @Test
    public void centersOddSizedCrosshairOnOddGuiDimensionLikeVanilla() {
        Assert.assertEquals(496, PortalGunCrosshairRenderer.centeredCrosshairOrigin(1025, 33));
    }
}
