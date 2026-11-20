package net.portalmod.core.event;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.portalmod.PMState;
import net.portalmod.PortalMod;
import net.portalmod.client.render.PortalFirstPersonRenderer;
import net.portalmod.client.render.WatermarkRenderer;
import net.portalmod.common.blocks.InteractKeyInteractable;
import net.portalmod.common.entities.Fizzleable;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.ModSpawnEggItem;
import net.portalmod.common.sorted.creer.CreerRenderer;
import net.portalmod.common.sorted.faithplate.CFaithPlateEndConfigPacket;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.common.sorted.fizzler.Fizzler;
import net.portalmod.common.sorted.goo.GooBlock;
import net.portalmod.common.sorted.portal.CameraAnimator;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalEntityClient;
import net.portalmod.common.sorted.portal.PortalRenderer;
import net.portalmod.common.sorted.portalgun.*;
import net.portalmod.common.sorted.portalgun.skins.ClientSkinManager;
import net.portalmod.common.sorted.trigger.TriggerSelectionClient;
import net.portalmod.common.sorted.trigger.TriggerTER;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.*;
import net.portalmod.core.injectors.LivingEntityInjector;
import net.portalmod.core.injectors.MainMenuInjector;
import net.portalmod.core.math.AABBUtil;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ChangeDetector;
import net.portalmod.core.util.DebugRenderer;
import net.portalmod.core.util.ModUtil;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.ClipContext;
import org.joml.Vector3f;
import net.minecraft.world.level.Level;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.FORGE, value = EnvType.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientLogin(final ClientPlayerNetworkEvent.LoggedInEvent event) {
        ClientSkinManager.getInstance().onClientLogin();
    }

    @SubscribeEvent
    public static void onPlayerRender(final RenderPlayerEvent.Pre event) {
        HumanoidModel.ArmPose mainHand;
        HumanoidModel.ArmPose offHand;
        
        if(event.getEntityLiving().getMainArm() == HumanoidArm.RIGHT) {
            mainHand = event.getRenderer().getModel().rightArmPose;
            offHand = event.getRenderer().getModel().leftArmPose;
        } else {
            offHand = event.getRenderer().getModel().rightArmPose;
            mainHand = event.getRenderer().getModel().leftArmPose;
        }
        
        if(event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PortalGun)
            mainHand = ArmPose.BOW_AND_ARROW;
        if(event.getPlayer().getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof PortalGun)
            offHand = ArmPose.BOW_AND_ARROW;
        
        if(mainHand.isTwoHanded())
            offHand = event.getEntityLiving().getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        
        if(event.getEntityLiving().getMainArm() == HumanoidArm.RIGHT) {
            event.getRenderer().getModel().rightArmPose = mainHand;
            event.getRenderer().getModel().leftArmPose = offHand;
        } else {
            event.getRenderer().getModel().rightArmPose = offHand;
            event.getRenderer().getModel().leftArmPose = mainHand;
        }
    }

    @SubscribeEvent
    public static void screenOpen(final GuiOpenEvent event) {
        if(!(event.getGui() instanceof TitleScreen)) return;

        event.setGui(MainMenuInjector.getInjectedMenu(PortalModConfigManager.MENU.get(), MainMenuInjector.fading));
        MainMenuInjector.needsUpdate = false;
        MainMenuInjector.fading = false;
    }
    
    private static final ChangeDetector creerNameDetector = new ChangeDetector();
    
    @SubscribeEvent
    public static void renderLiving(final RenderLivingEvent.Pre<?, ?> event) {
        if(event.getRenderer() instanceof CreerRenderer)
            return;
        
        if(!CreerRenderer.isCreer(event.getEntity())) {
            creerNameDetector.trigger(false);
            return;
        }
        
        creerNameDetector.trigger(true);
        
        Creeper entity = (Creeper)event.getEntity();
        float partialTicks = event.getPartialRenderTick();
        PoseStack matrixStack = event.getMatrixStack();
        int light = event.getLight();
        MultiBufferSource renderTypeBuffer = event.getBuffers();
        
        if(creerNameDetector.get())
            entity.refreshDimensions();
        
        event.setCanceled(true);
        CreerRenderer.INSTANCE.get().render(entity, 0, partialTicks, matrixStack, renderTypeBuffer, light);
    }

    @SubscribeEvent
    public static void fogDensity(final EntityViewRenderEvent.FogDensity event) {
        Camera info = event.getInfo();
        if (info.getFluidInCamera().is(FluidTagInit.GOO)) {
            event.setDensity(GooBlock.getFogDensity(info.getEntity()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void fogColor(final EntityViewRenderEvent.FogColors event) {
        Camera info = event.getInfo();
        if (info.getFluidInCamera().is(FluidTagInit.GOO)) {
            Vector3f color = GooBlock.getFogColor().to3f();
            event.setRed(color.x());
            event.setGreen(color.y());
            event.setBlue(color.z());
        }
    }

    @SubscribeEvent
    public static void entitySize(final EntityEvent.Size event) {
        if(!CreerRenderer.isCreer(event.getEntity()))
            return;
        
        EntityDimensions size = event.getOldSize();
        event.setNewSize(EntityDimensions.scalable(size.width, 1f), true);
    }
    
    private static boolean wasPressed = false;
    
    @SubscribeEvent
    public static void clientTick(final TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            PortalFirstPersonRenderer.updateSwingTime();
        }

        if(event.phase != TickEvent.Phase.START) {
            return;
        }

        ClientSkinManager.getInstance().tick();

        // STIK ER IN
        // 🔥

        Minecraft mc = Minecraft.getInstance();
        Player player = Minecraft.getInstance().player;
        Level level = Minecraft.getInstance().level;

        if(player != null && level != null && Minecraft.getInstance().gameMode != null) {
            if(TriggerSelectionClient.isSelecting()) {
                float rayLength = Minecraft.getInstance().gameMode.getPickRange();
                net.minecraft.world.phys.Vec3 rayPath = player.getViewVector(0).scale(rayLength);
                net.minecraft.world.phys.Vec3 from = player.getEyePosition(0);
                net.minecraft.world.phys.Vec3 to = from.add(rayPath);

                ClipContext rayCtx = new ClipContext(from, to, ClipContext.BlockMode.OUTLINE, ClipContext.FluidMode.NONE, null);
                BlockHitResult rayHit = Minecraft.getInstance().level.clip(rayCtx);

                BlockPos pos = rayHit.getBlockPos();
                BlockState state = level.getBlockState(pos);

                if (state.isFaceSturdy(level, pos, rayHit.getDirection()))
                    pos = pos.relative(rayHit.getDirection());

                TriggerSelectionClient.updateSelectedPos(pos);
            }

            if(mc.overlay != null || mc.screen != null) {
                if(player.getMainHandItem().getItem() instanceof PortalGun) {
                    PortalGunClient.getInstance().resetPresses();
                }
            }
        }

        PortalGunClient.getInstance().tick();
        handleInteractKey();
    }

    public static void handleInteractKey() {
        if (!KeyInit.PORTALGUN_INTERACT.isDown()) {
            wasPressed = false;
            return;
        }

        if (wasPressed) return;
        wasPressed = true;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.isSpectator()) return;

        float rayLength = Minecraft.getInstance().gameMode.getPickRange();
        net.minecraft.world.phys.Vec3 playerRotation = player.getViewVector(0);
        net.minecraft.world.phys.Vec3 rayPath = playerRotation.scale(rayLength);

        net.minecraft.world.phys.Vec3 from = player.getEyePosition(0);
        net.minecraft.world.phys.Vec3 to = from.add(rayPath);

        ClipContext rayCtx = new ClipContext(from, to, ClipContext.BlockMode.OUTLINE, ClipContext.FluidMode.NONE, null);
        BlockHitResult rayHit = Minecraft.getInstance().level.clip(rayCtx);

        // Drop entity
        ItemStack itemStack = player.getMainHandItem();
        if (player.hasPassenger(TestElementEntity.class)) {
            TestElementEntity.dropHeldEntities(player, false, false, itemStack);
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.DROP_ENTITY).build());

            consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
            return;
        }

        // Press block
        Block block = Minecraft.getInstance().level.getBlockState(rayHit.getBlockPos()).getBlock();
        if (block instanceof InteractKeyInteractable && ((InteractKeyInteractable) block).interactKeyInteract(player, rayHit)) {
            consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
            return;
        }

        // Pick up entity
        try {
//            Entity entity = Minecraft.getInstance().crosshairPickEntity;

            // Use collision shapes to target the entity instead of visual shapes
            // todo: this increases the throwing lag even more ugh
//            ClipContext collisionRayCtx = new ClipContext(from, to, ClipContext.BlockMode.COLLIDER, ClipContext.FluidMode.NONE, null);
//            BlockHitResult collisionRayHit = Minecraft.getInstance().level.clip(collisionRayCtx);

            List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(player.level, new Vec3(from), new Vec3(to), portal -> true);
            Mat4 portalMatrix = ModUtil.getMatrixFromPortalChain(portalChain);
            Pair<net.minecraft.world.phys.Vec3, net.minecraft.world.phys.Vec3> ray = ModUtil.teleportRay(portalChain, from, to);
            AABB aabb = player.getBoundingBox().expandTowards(rayPath);
            aabb = AABBUtil.transform(aabb, portalMatrix);

            EntityHitResult realWorldEntityRayTraceResult = ProjectileUtil.getEntityHitResult(player, from, to, player.getBoundingBox().expandTowards(rayPath), TestElementEntity::isHoldable, rayLength * rayLength);
            EntityHitResult teleportedEntityRayTraceResult = ProjectileUtil.getEntityHitResult(player, ray.getFirst(), ray.getSecond(), aabb, TestElementEntity::isHoldable, rayLength * rayLength);
            EntityHitResult entityRayTraceResult;

            if(realWorldEntityRayTraceResult == null && teleportedEntityRayTraceResult == null) {
                return;
            }

            boolean realWorld = true;

            if(teleportedEntityRayTraceResult == null) {
                entityRayTraceResult = realWorldEntityRayTraceResult;

            } else if(realWorldEntityRayTraceResult == null) {
                entityRayTraceResult = teleportedEntityRayTraceResult;
                realWorld = false;

            } else {
                net.minecraft.world.phys.Vec3 teleportedFrom = new Vec3(from).transform(portalMatrix).to3d();
                double realWorldDistance = realWorldEntityRayTraceResult.getLocation().subtract(from).lengthSqr();
                double teleportedDistance = teleportedEntityRayTraceResult.getLocation().subtract(teleportedFrom).lengthSqr();
                entityRayTraceResult = realWorldDistance <= teleportedDistance ? realWorldEntityRayTraceResult : teleportedEntityRayTraceResult;
                realWorld = realWorldDistance <= teleportedDistance;
            }

            Entity entity = entityRayTraceResult.getEntity();

            // Set reach origin to feet so it is possible to pick things up from above further than from below
            net.minecraft.world.phys.Vec3 reachPosition = player.position().add(0, 0.2, 0);
            double grabReach = player.getAttributeValue(AttributeInit.GRAB_REACH.get());
            Vec3 distance = new Vec3(entity.position()).sub(new Vec3(reachPosition).transform(realWorld ? Mat4.identity() : portalMatrix));

            if (entity instanceof TestElementEntity && distance.magnitude() < grabReach + entity.getBbWidth() / 2) {
                ((TestElementEntity) entity).pickUp(player);

                consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
            }
        } catch (Exception e) {
            PortalMod.LOGGER.error("Error while picking up entity", e);
        }
    }

    private static void consumeAllKeyPresses(InputConstants.Input k) {
        KeyMapping[] keys = Minecraft.getInstance().options.keyMappings;
        for(KeyMapping key : keys)
            if(key.getKey() == k)
                while(key.consumeClick());
    }

    @SubscribeEvent
    public static void onMouseClick(final InputEvent.RawMouseEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return;

        if(mc.overlay == null && mc.screen == null) {
            if(player.getMainHandItem().getItem() instanceof PortalGun) {
                boolean handled = PortalGunClient.getInstance().handleMouseButtons(event.getButton(), event.getAction());

                if(handled) {
                    event.setCanceled(true);
                }
            } else {
                PortalGunClient.getInstance().resetPresses();
            }
        }
    }
    
    @SubscribeEvent
    public static void onRenderBlockOverlay(final RenderBlockOverlayEvent event) {
        if(!PortalEntity.shouldRenderBlockOverlay(event.getPlayer().level, event.getBlockPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderWorldLast(final RenderWorldLastEvent event) {
        DebugRenderer.renderAllShapes(event.getMatrixStack());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCameraSetup(final EntityViewRenderEvent.CameraSetup event) {
        PMState.cameraPosOverrideForRenderingSelf = null;

        if(Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            Optional<Float> optionalPitch = CameraAnimator.getInstance().getRelativePitch();
            optionalPitch.ifPresent(relativePitch -> event.setPitch(event.getPitch() + relativePitch));

            Optional<Float> optionalYaw = CameraAnimator.getInstance().getRelativeYaw();
            optionalYaw.ifPresent(relativeYaw -> event.setYaw(event.getYaw() + relativeYaw));

            Optional<Float> optionalRoll = CameraAnimator.getInstance().getRelativeRoll();
            optionalRoll.ifPresent(relativeRoll -> event.setRoll(event.getRoll() + relativeRoll));

            Optional<Vec3> optionalPos = CameraAnimator.getInstance().getRelativePos();
            optionalPos.ifPresent(pos -> {
                PMState.cameraPosOverrideForRenderingSelf = new Vec3(event.getInfo().getPosition());
                ((ActiveRenderInfoAccessor)event.getInfo()).pmSetPosition(event.getInfo().getPosition().add(pos.to3d()));
            });
        }

        PortalEntityClient.teleportCameraAndApply(event);

        PMState.cameraRoll = event.getRoll();
    }

    @SubscribeEvent
    public static void onFogSetup(final EntityViewRenderEvent.FogColors event) {
        PortalRenderer.getInstance().clearColor = new Vec3(event.getRed(), event.getGreen(), event.getBlue());
    }

    public static final List<String> debugStrings = new ArrayList<>();
    
    @SubscribeEvent
    public static void onRenderOverlay(final RenderGameOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Font fontRenderer = minecraft.font;

        if(event.getType() == ElementType.CROSSHAIRS) {
            PortalGunCrosshairRenderer.render(event.getMatrixStack());

            if(PortalMod.WATERMARK) {
                WatermarkRenderer.render(event.getMatrixStack());
            }
            
        } else if(event.getType() == ElementType.SUBTITLES) {
            int x = 2;
            int y = 2;
            int index = 0;
            for(String text : debugStrings) {
                fontRenderer.drawShadow(event.getMatrixStack(), text, x, y + fontRenderer.lineHeight * index++, 0xFFFFAA00);
            }

            debugStrings.clear();
        }
    }
}