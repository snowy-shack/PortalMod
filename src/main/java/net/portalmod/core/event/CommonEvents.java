package net.portalmod.core.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.level.chunk.LevelChunk;
import java.util.List;

import net.portalmod.core.network.PacketDistributor;
import net.portalmod.PortalMod;
import net.portalmod.common.commands.PortalCommand;
import net.portalmod.common.entities.Fizzleable;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.ModSpawnEggItem;
import net.portalmod.common.sorted.faithplate.CFaithPlateEndConfigPacket;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.common.sorted.fizzler.Fizzler;
import net.portalmod.common.sorted.portal.*;
import net.portalmod.common.sorted.portalgun.PortalHelperServerManager;
import net.portalmod.common.sorted.portalgun.skins.ServerSkinManager;
import net.portalmod.common.sorted.trigger.TriggerSelectionClient;
import net.portalmod.common.sorted.trigger.TriggerSelectionServer;
import net.portalmod.core.init.GameRuleInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.injectors.LivingEntityInjector;
import net.portalmod.core.util.ModUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/**
 * Common (both sides) game event handlers.
 * Forge called these through its event bus; on Fabric they are wired up in {@link #register()},
 * either to Fabric API callbacks or to small hook mixins in {@code net.portalmod.mixins.hooks}.
 */
public class CommonEvents {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> PortalCommand.register(dispatcher));
        ServerWorldEvents.LOAD.register((server, level) -> onLevelLoad(level));
        ServerTickEvents.START_SERVER_TICK.register(server -> onServerTickStart());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onServerLogin(handler.getPlayer()));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof PortalEntity portal) portal.onAddedToWorld();
            onEntityJoin(entity, level);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof PortalEntity portal) portal.onRemovedFromWorld();
            onEntityLeave(entity);
        });
        ServerChunkEvents.CHUNK_LOAD.register(CommonEvents::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(CommonEvents::onChunkUnload);
    }

    // --- logical client and logical server ---

    /** Server side of Forge's WorldEvent.Load. The client side is ClientEvents#onClientLevelLoad. */
    public static void onLevelLoad(ServerLevel level) {
        if(level != level.getServer().getLevel(Level.OVERWORLD))
            return;

        PortalManager.getInstance().clear();
        level.getDataStorage().computeIfAbsent(PortalManager::load, PortalManager::getInstance, PortalManager.PATH);
    }

    /** Called at the start of LivingEntity#tick (Forge: LivingUpdateEvent). */
    public static void onLivingUpdate(LivingEntity entity) {
        LivingEntityInjector.onPreTick(entity);
    }

    /** Returns true to cancel fall damage (Forge: LivingFallEvent). */
    public static boolean shouldCancelFall(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).getItem() == ItemInit.LONGFALL_BOOTS.get();
    }

    /** Called at the start and end of Player#tick (Forge: PlayerTickEvent). */
    public static void onPlayerTick(Player player, boolean start) {
        if(start)
            if(player.getAbilities().flying)
                ((Flingable)player).setFlinging(false);

        if(!start && player.isLocalPlayer()) {
            if(player.getInventory().getSelected().getItem() != ItemInit.WRENCH.get()) {
                if(FaithPlateTER.selected != null) {
                    PacketInit.INSTANCE.sendToServer(new CFaithPlateEndConfigPacket(FaithPlateTER.selected));
                    FaithPlateTER.selected = null;
                }

                if(TriggerSelectionClient.isSelecting()) {
                    TriggerSelectionClient.abort();
                }
            }
        }
    }

    /** Called after a player picks up an item entity (Forge: PlayerEvent.ItemPickupEvent). */
    public static void onPlayerPickUpItem(Player player, ItemEntity originalItemEntity, ItemStack stack) {
        Level level = player.level();

        if(Fizzleable.isFizzleableItem(stack)) {
            ClipContext context = new ClipContext(player.getEyePosition(1), originalItemEntity.position(),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player);

            BlockHitResult result = ModUtil.customClip(level, context, pos -> {
                BlockState state = level.getBlockState(pos);
                Block block = state.getBlock();

                if(Fizzler.isActiveFizzler(state)) {
                    return Optional.of(((Fizzler)block).getFieldShape(state));
                }

                return Optional.empty();
            });

            if(result.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = result.getBlockPos();
                BlockState state = level.getBlockState(pos);

                if(Fizzler.isActiveFizzler(state)) {
                    ((Fizzleable) player).onTouchingFizzler();
                }
            }
        }
    }

    // --- logical server only ---

    public static void onServerTickStart() {
        ServerSkinManager.getInstance().tick();
        PortalManager.getInstance().tick();
        VolatilePortalHelperManager.getInstance().clearVolatilePortalHelpers();
        PortalHelperServerManager.getInstance().tick();
    }

    public static void onServerLogin(ServerPlayer player) {
        ServerSkinManager.getInstance().onServerLogin(player);
    }

    public static void onEntityJoin(Entity entity, ServerLevel level) {
        if(!(entity instanceof ServerPlayer player))
            return;

        PortalManager.getInstance().getPortalMap().forEach((k, v) -> {
            PacketInit.INSTANCE.send(PacketDistributor.PLAYER.with(
                    () -> player), new SPortalPairPacket(k, new PartialPortalPair(v)));
        });

        GameRuleInit.sendBooleanRule(player, GameRuleInit.PORTAL_SLOWSHOT);
        GameRuleInit.sendBooleanRule(player, GameRuleInit.USE_PORTALABLE_BLACKLIST);
    }

    public static void onEntityLeave(Entity entity) {
        if(!(entity instanceof ServerPlayer player))
            return;

        FaithPlateTileEntity.endConfigurationForPlayer(player);
        TriggerSelectionServer.endConfiguration(player);
    }

    // TODO(port): since 1.17 entities are stored separately from chunks (EntitySectionStorage), so
    // re-adding portal entities on chunk load may now duplicate or race with vanilla entity loading.
    // Kept as in 1.16.5 for now; needs testing in game.
    public static void onChunkLoad(ServerLevel level, LevelChunk chunk) {
        PortalManager.getInstance().getPortalsPerChunk()
                .getOrDefault(level.dimension(), new HashMap<>())
                .getOrDefault(chunk.getPos(), new ArrayList<>())
                .forEach(portal -> {
                    portal.unsetRemoved();
                    level.addFreshEntity(portal);
                });
    }

    public static void onChunkUnload(ServerLevel level, LevelChunk chunk) {
        PortalManager.getInstance().getPortalsPerChunk()
                .getOrDefault(level.dimension(), new HashMap<>())
                .getOrDefault(chunk.getPos(), new ArrayList<>())
                .forEach(portal -> portal.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK));
    }
}
