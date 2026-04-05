package net.portalmod.core.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
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

@Mod.EventBusSubscriber(modid = PortalMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    // --- logical client and logical server ---

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        PortalCommand.register(event.getDispatcher());
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

    @SubscribeEvent
    public static void onLivingUpdate(final LivingEvent.LivingUpdateEvent event) {
        LivingEntityInjector.onPreTick(event.getEntityLiving());
    }

    @SubscribeEvent
    public static void onLivingFall(final LivingFallEvent event) {
        if(event.getEntityLiving().getItemBySlot(EquipmentSlotType.FEET).getItem() == ItemInit.LONGFALL_BOOTS.get())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;

        if(event.phase == TickEvent.Phase.START)
            if(player.abilities.flying)
                ((Flingable)player).setFlinging(false);

        if(event.phase == TickEvent.Phase.END && player.isLocalPlayer()) {
            if(player.inventory.getSelected().getItem() != ItemInit.WRENCH.get()) {
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

    @SubscribeEvent
    public static void onPlayerPickUpItem(final PlayerEvent.ItemPickupEvent event) {
        ItemEntity originalItemEntity = event.getOriginalEntity();
        PlayerEntity player = event.getPlayer();
        World level = player.level;

        if(Fizzleable.isFizzleableItem(event.getStack())) {
            RayTraceContext context = new RayTraceContext(player.getEyePosition(1), originalItemEntity.position(),
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, player);

            BlockRayTraceResult result = ModUtil.customClip(level, context, pos -> {
                BlockState state = level.getBlockState(pos);
                Block block = state.getBlock();

                if(Fizzler.isActiveFizzler(state)) {
                    return Optional.of(((Fizzler)block).getFieldShape(state));
                }

                return Optional.empty();
            });

            if(result.getType() == RayTraceResult.Type.BLOCK) {
                BlockPos pos = result.getBlockPos();
                BlockState state = level.getBlockState(pos);

                if(Fizzler.isActiveFizzler(state)) {
                    ((Fizzleable) player).onTouchingFizzler();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(final LivingDropsEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof TestElementEntity) {
            for (ItemEntity itemEntity : event.getDrops()) {
                ItemStack itemStack = itemEntity.getItem();
                if (itemStack.getItem() instanceof ModSpawnEggItem && entity.hasCustomName()) {
                    itemStack.setHoverName(entity.getCustomName());
                }
            }
        }
    }

    // --- logical server only ---

    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            ServerSkinManager.getInstance().tick();
            PortalManager.getInstance().tick();
            VolatilePortalHelperManager.getInstance().clearVolatilePortalHelpers();
            PortalHelperServerManager.getInstance().tick();
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
}