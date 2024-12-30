package net.portalmod.core.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.turret.TurretEntity;
import net.portalmod.core.init.EntityInit;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.DEDICATED_SERVER)
public class ServerModEvents {
    @SubscribeEvent
    public static void registerAttributes(final EntityAttributeCreationEvent event) {
        event.put(EntityInit.COMPANION_CUBE.get(), Cube.createAttributes().build());
        event.put(EntityInit.STORAGE_CUBE.get(), Cube.createAttributes().build());
        event.put(EntityInit.VINTAGE_CUBE.get(), Cube.createAttributes().build());
        event.put(EntityInit.GABE.get(), Cube.createAttributes().build());
        event.put(EntityInit.TURRET.get(), TurretEntity.createAttributes().build());
    }
}