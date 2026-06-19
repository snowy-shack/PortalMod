package net.portalmod.common.sorted.portalgun;

import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PortalGunClientTest {
    @Test
    public void shouldDeferRightClickToVanillaForItemFramesAndArmorStands() {
        assertTrue(PortalGunClient.shouldDeferRightClickToVanilla(ItemFrameEntity.class));
        assertTrue(PortalGunClient.shouldDeferRightClickToVanilla(ArmorStandEntity.class));
    }

    @Test
    public void shouldHandleRightClickWhenNoVanillaEntityIsTargeted() {
        assertFalse(PortalGunClient.shouldDeferRightClickToVanilla(null));
    }
}
