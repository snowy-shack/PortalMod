package net.portalmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.portalmod.core.event.CommonEvents;
import net.portalmod.core.init.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/** Fabric "main" entrypoint (runs on both client and dedicated server). */
public class PortalMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "portalmod";
    public static final boolean DEBUG = false;
    public static final boolean WATERMARK = false;

    @Nullable
    private static MinecraftServer server;

    /** The running logical server, or null when none is running (replaces Forge's ServerLifecycleHooks). */
    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);

        // Loading each class registers its entries (see core.registry.DeferredRegister).
        // Order matters: blocks before items, block entities and entities after blocks.
        SoundInit.SOUNDS.register();
        FluidInit.FLUIDS.register();
        BlockInit.BLOCKS.register();
        ItemInit.ITEMS.register();
        EntityInit.ENTITIES.register();
        TileEntityTypeInit.TILE_ENTITY_TYPES.register();
        ParticleInit.PARTICLE_TYPES.register();
        RecipeInit.RECIPES.register();
        AttributeInit.ATTRIBUTES.register();
        net.portalmod.core.PortalModTab.init();
        PacketInit.init();
        StatsInit.init();
        ItemTagInit.init();
        BlockTagInit.init();
        EntityTagInit.init();
        FluidTagInit.init();

        EntityInit.registerAttributes();
        CriteriaTriggerInit.REGISTRY.registerAll();
        GameRuleInit.registerAll();
        ArgumentTypeInit.registerAll();
        ItemInit.registerFluidDispenserBehavior();

        CommonEvents.register();
    }
}
