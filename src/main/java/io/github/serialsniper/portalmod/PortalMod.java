package io.github.serialsniper.portalmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.serialsniper.portalmod.client.render.PortalShaders;
import io.github.serialsniper.portalmod.client.screens.PortalOptionsScreen;
import io.github.serialsniper.portalmod.core.init.BlockInit;
import io.github.serialsniper.portalmod.core.init.EntityInit;
import io.github.serialsniper.portalmod.core.init.ItemInit;
import io.github.serialsniper.portalmod.core.init.PacketInit;
import io.github.serialsniper.portalmod.core.init.ParticleInit;
import io.github.serialsniper.portalmod.core.init.SoundInit;
import io.github.serialsniper.portalmod.core.init.TileEntityTypeInit;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PortalMod.MODID)
public class PortalMod {
    // science isn't about why, it's about why not.
	
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "portalmod";
    
    public static PortalShaders shaders,
            portalFrameShader,
            portalHighlightShader,
            portalInnerShader,
            portalViewShader;
    
    public PortalMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        ParticleInit.PARTICLE_TYPES.register(bus);
        ItemInit.ITEMS.register(bus);
        BlockInit.BLOCKS.register(bus);
        SoundInit.SOUNDS.register(bus);
        EntityInit.ENTITIES.register(bus);
        TileEntityTypeInit.TILE_ENTITY_TYPES.register(bus);
        PacketInit.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PortalOptionsScreen.CONFIG, "portalmod-client.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void setup(final FMLCommonSetupEvent event) {}
}