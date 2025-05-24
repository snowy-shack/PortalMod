package net.portalmod;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.portalmod.client.render.BlockColorHandler;
import net.portalmod.client.screens.PortalModOptionsScreen;
import net.portalmod.core.init.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PortalMod.MODID)
public class PortalMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "portalmod";
    public static final String API_BEARER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFseWRwbWZldnhhYWJsdW1uY3lqIiwicm9sZSI6ImFub24iLCJpYXQiOjE2NjMzNjg1NjUsImV4cCI6MTk3ODk0NDU2NX0.ISXdn1qWzDqpyHwfTORCdf8GNkpL_mCzXG6LrZv9Bys";

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
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
                        PortalModOptionsScreen.CONFIG, "portalmod-client.toml"));
    }
}