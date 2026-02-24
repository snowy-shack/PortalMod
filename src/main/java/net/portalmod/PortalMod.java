package net.portalmod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.portalmod.client.render.BlockColorHandler;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(PortalMod.MODID)
public class PortalMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "portalmod";
    public static final boolean DEBUG = false;
    public static final boolean WATERMARK = false;

    public PortalMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);

        EntityInit.ENTITIES.register(bus);
        TileEntityTypeInit.TILE_ENTITY_TYPES.register(bus);
        ItemInit.ITEMS.register(bus);
        BlockInit.BLOCKS.register(bus);
        FluidInit.FLUIDS.register(bus);
        ParticleInit.PARTICLE_TYPES.register(bus);
        SoundInit.SOUNDS.register(bus);
        RecipeInit.RECIPES.register(bus);
        AttributeInit.ATTRIBUTES.register(bus);
        PacketInit.init();
        StatsInit.init();
        ItemTagInit.init();
        BlockTagInit.init();
        EntityTagInit.init();
        FluidTagInit.init();
        createConfigs();

        bus.register(new BlockColorHandler());

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CriteriaTriggerInit.REGISTRY.registerAll();

        ItemInit.registerFluidDispenserBehavior();
    }

    private void createConfigs() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> PortalModConfigManager::init);
    }

    public static File getModFolder() {
        return new File(Minecraft.getInstance().gameDirectory, PortalMod.MODID);
    }
}