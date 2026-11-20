package net.portalmod.common.sorted.portalgun;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;

public class PortalGunClientTest {
    @Test
    public void shouldDeferRightClickToVanillaForItemFramesAndArmorStands() {
        assertTrue(PortalGunClient.shouldDeferRightClickToVanilla(ItemFrame.class));
        assertTrue(PortalGunClient.shouldDeferRightClickToVanilla(ArmorStand.class));
    }

    @Test
    public void shouldHandleRightClickWhenNoVanillaEntityIsTargeted() {
        assertFalse(PortalGunClient.shouldDeferRightClickToVanilla(null));
    }
}
