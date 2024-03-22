package net.portalmod.core.init;

import net.minecraft.stats.IStatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.portalmod.PortalMod;

public class StatsInit {
    private StatsInit() {}
    public static void init() {}
    
    public static final ResourceLocation PORTALS_SHOT = makeCustomStat("portals_shot", IStatFormatter.DEFAULT);
    
    private static ResourceLocation makeCustomStat(String name, IStatFormatter formatter) {
        ResourceLocation resourcelocation = new ResourceLocation(PortalMod.MODID, name);
        Registry.register(Registry.CUSTOM_STAT, resourcelocation, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, formatter);
        return resourcelocation;
    }
}