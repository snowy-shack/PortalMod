package net.portalmod.core.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.portalmod.PortalMod;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.DEDICATED_SERVER)
public class ServerModEvents {

}