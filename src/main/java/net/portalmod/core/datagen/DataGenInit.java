package net.portalmod.core.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.portalmod.PortalMod;

@Mod.EventBusSubscriber(modid = PortalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenInit {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(new BlockStateGen(generator, helper));
        generator.addProvider(new RecipeGen(generator));
        generator.addProvider(new LootTableGen(generator));
    }
}
