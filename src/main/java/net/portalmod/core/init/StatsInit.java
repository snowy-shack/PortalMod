package net.portalmod.core.init;

import net.portalmod.PortalMod;

import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

public class StatsInit {
    private StatsInit() {}
    public static void init() {}
    
    public static final ResourceLocation PORTALS_SHOT = makeCustomStat("portals_shot", StatFormatter.DEFAULT);
    
    private static ResourceLocation makeCustomStat(String name, StatFormatter formatter) {
        ResourceLocation resourcelocation = new ResourceLocation(PortalMod.MODID, name);
        Registry.register(Registry.CUSTOM_STAT, resourcelocation, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, formatter);
        return resourcelocation;
    }
}