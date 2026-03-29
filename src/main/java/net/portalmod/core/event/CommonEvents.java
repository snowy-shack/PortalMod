package net.portalmod.core.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.portalmod.PortalMod;
import net.portalmod.common.commands.PortalCommand;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.common.sorted.portalgun.skins.ServerSkinManager;
import net.portalmod.common.sorted.trigger.TriggerSelectionServer;
import net.portalmod.core.init.GameRuleInit;
import net.portalmod.core.init.PacketInit;

import java.util.ArrayList;
import java.util.HashMap;

@Mod.EventBusSubscriber(modid = PortalMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {
    // todo tidy up all event classes

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        PortalCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            ServerSkinManager.getInstance().tick();
            PortalManager.getInstance().tick();
            VolatilePortalHelperManager.getInstance().clearVolatilePortalHelpers();
        }
    }

    @SubscribeEvent
    public static void onServerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        ServerSkinManager.getInstance().onServerLogin((ServerPlayerEntity)event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerJoin(final EntityJoinWorldEvent event) {
        if(event.getWorld().isClientSide() || event.getWorld().getServer() == null)
            return;

        if(!(event.getEntity() instanceof PlayerEntity))
            return;

        PortalManager.getInstance().getPortalMap().forEach((k, v) -> {
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(
                    () -> (ServerPlayerEntity)event.getEntity()), new SPortalPairPacket(k, new PartialPortalPair(v)));
        });

        GameRuleInit.sendBooleanRule((ServerPlayerEntity)event.getEntity(), GameRuleInit.PORTAL_SLOWSHOT);
        GameRuleInit.sendBooleanRule((ServerPlayerEntity)event.getEntity(), GameRuleInit.USE_PORTALABLE_BLACKLIST);
    }

    @SubscribeEvent
    public static void onPlayerLeave(final EntityLeaveWorldEvent event) {
        if(!(event.getEntity() instanceof ServerPlayerEntity))
            return;

        FaithPlateTileEntity.endConfigurationForPlayer((PlayerEntity)event.getEntity());
        TriggerSelectionServer.endConfiguration((ServerPlayerEntity)event.getEntity());
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        if(event.getWorld().isClientSide())
            return;

        PortalManager.getInstance().getPortalsPerChunk()
                .getOrDefault(((ServerWorld)event.getWorld()).dimension(), new HashMap<>())
                .getOrDefault(event.getChunk().getPos(), new ArrayList<>())
                .forEach(portal -> {
                    portal.removed = false;
                    event.getWorld().addFreshEntity(portal);
                });
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload event) {
        if(event.getWorld().isClientSide() || !(event.getWorld() instanceof ServerWorld))
            return;

        PortalManager.getInstance().getPortalsPerChunk()
                .getOrDefault(((ServerWorld)event.getWorld()).dimension(), new HashMap<>())
                .getOrDefault(event.getChunk().getPos(), new ArrayList<>())
                .forEach(Entity::remove);
    }

    @SubscribeEvent
    public static void onLevelLoad(final WorldEvent.Load event) {
        if(event.getWorld().isClientSide()) {
            ClientPortalManager.getInstance().clear();
            return;
        }

        if(event.getWorld() != ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD))
            return;

        PortalManager.getInstance().clear();
        ((ServerWorld)event.getWorld()).getDataStorage().get(PortalManager::getInstance, PortalManager.PATH);
    }
}