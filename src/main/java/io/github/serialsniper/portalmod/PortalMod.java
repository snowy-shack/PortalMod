package io.github.serialsniper.portalmod;

import io.github.serialsniper.portalmod.client.render.PortalShaders;
import io.github.serialsniper.portalmod.core.init.*;
import io.github.serialsniper.portalmod.core.util.Networking;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.*;

import net.minecraftforge.common.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.*;

@Mod(PortalMod.MODID)
public class PortalMod {
	// science isn't about why, it's about why not.

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "portalmod";
	public static PortalShaders shaders;
	
	public PortalMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		ItemInit.ITEMS.register(bus);
		BlockInit.BLOCKS.register(bus);
		SoundInit.SOUNDS.register(bus);
		EntityInit.ENTITIES.register(bus);
		TileEntityTypeInit.TILE_ENTITY_TYPES.register(bus);
		Networking.registerMessages();
		PacketInit.init();

		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void setup(final FMLCommonSetupEvent event) {}
}