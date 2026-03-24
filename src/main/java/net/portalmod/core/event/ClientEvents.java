package net.portalmod.core.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.BipedModel.ArmPose;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
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
import net.portalmod.common.blocks.EKeyInteractable;
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
import net.portalmod.common.sorted.portalgun.skins.SkinManager;
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

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientLogin(final ClientPlayerNetworkEvent.LoggedInEvent event) {
        SkinManager.getClientInstance().onClientLogin();
    }

    @SubscribeEvent
    public static void onPlayerRender(final RenderPlayerEvent.Pre event) {
        BipedModel.ArmPose mainHand;
        BipedModel.ArmPose offHand;
        
        if(event.getEntityLiving().getMainArm() == HandSide.RIGHT) {
            mainHand = event.getRenderer().getModel().rightArmPose;
            offHand = event.getRenderer().getModel().leftArmPose;
        } else {
            offHand = event.getRenderer().getModel().rightArmPose;
            mainHand = event.getRenderer().getModel().leftArmPose;
        }
        
        if(event.getPlayer().getItemInHand(Hand.MAIN_HAND).getItem() instanceof PortalGun)
            mainHand = ArmPose.BOW_AND_ARROW;
        if(event.getPlayer().getItemInHand(Hand.OFF_HAND).getItem() instanceof PortalGun)
            offHand = ArmPose.BOW_AND_ARROW;
        
        if(mainHand.isTwoHanded())
            offHand = event.getEntityLiving().getOffhandItem().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
        
        if(event.getEntityLiving().getMainArm() == HandSide.RIGHT) {
            event.getRenderer().getModel().rightArmPose = mainHand;
            event.getRenderer().getModel().leftArmPose = offHand;
        } else {
            event.getRenderer().getModel().rightArmPose = offHand;
            event.getRenderer().getModel().leftArmPose = mainHand;
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent event) {
        PlayerEntity player = event.player;

        if(event.phase == Phase.START)
            if(player.abilities.flying)
                ((Flingable)player).setFlinging(false);

        if(event.phase == Phase.END && player.isLocalPlayer()) {
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
    public static void onLivingUpdate(final LivingUpdateEvent event) {
        LivingEntityInjector.onPreTick(event.getEntityLiving());
    }
    
    @SubscribeEvent
    public static void onLivingFall(final LivingFallEvent event) {
        if(event.getEntityLiving().getItemBySlot(EquipmentSlotType.FEET).getItem() == ItemInit.LONGFALL_BOOTS.get())
            event.setCanceled(true);
    }
    
    @SubscribeEvent
    public static void screenOpen(final GuiOpenEvent event) {
        if(!(event.getGui() instanceof MainMenuScreen)) return;

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
        
        CreeperEntity entity = (CreeperEntity)event.getEntity();
        float partialTicks = event.getPartialRenderTick();
        MatrixStack matrixStack = event.getMatrixStack();
        int light = event.getLight();
        IRenderTypeBuffer renderTypeBuffer = event.getBuffers();
        
        if(creerNameDetector.get())
            entity.refreshDimensions();
        
        event.setCanceled(true);
        CreerRenderer.INSTANCE.get().render(entity, 0, partialTicks, matrixStack, renderTypeBuffer, light);
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

    @SubscribeEvent
    public static void fogDensity(final EntityViewRenderEvent.FogDensity event) {
        ActiveRenderInfo info = event.getInfo();
        if (info.getFluidInCamera().is(FluidTagInit.GOO)) {
            event.setDensity(GooBlock.getFogDensity(info.getEntity()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void fogColor(final EntityViewRenderEvent.FogColors event) {
        ActiveRenderInfo info = event.getInfo();
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
        
        EntitySize size = event.getOldSize();
        event.setNewSize(EntitySize.scalable(size.width, 1f), true);
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

        SkinManager.getClientInstance().tick();

        // STIK ER IN
        // 🔥

        PlayerEntity player = Minecraft.getInstance().player;
        World level = Minecraft.getInstance().level;

        if(player != null && level != null && Minecraft.getInstance().gameMode != null) {
            if(TriggerSelectionClient.isSelecting()) {
                float rayLength = Minecraft.getInstance().gameMode.getPickRange();
                Vector3d rayPath = player.getViewVector(0).scale(rayLength);
                Vector3d from = player.getEyePosition(0);
                Vector3d to = from.add(rayPath);

                RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null);
                BlockRayTraceResult rayHit = Minecraft.getInstance().level.clip(rayCtx);

                BlockPos pos = rayHit.getBlockPos();
                BlockState state = level.getBlockState(pos);

                if (state.isFaceSturdy(level, pos, rayHit.getDirection()))
                    pos = pos.relative(rayHit.getDirection());

                TriggerSelectionClient.updateSelectedPos(pos);
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

        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.isSpectator()) return;

        float rayLength = Minecraft.getInstance().gameMode.getPickRange();
        Vector3d playerRotation = player.getViewVector(0);
        Vector3d rayPath = playerRotation.scale(rayLength);

        Vector3d from = player.getEyePosition(0);
        Vector3d to = from.add(rayPath);

        RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null);
        BlockRayTraceResult rayHit = Minecraft.getInstance().level.clip(rayCtx);

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
        if (block instanceof EKeyInteractable && ((EKeyInteractable) block).eKeyInteract(player, rayHit)) {
            consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
            return;
        }

        // Pick up entity
        try {
//            Entity entity = Minecraft.getInstance().crosshairPickEntity;

            // Use collision shapes to target the entity instead of visual shapes
            // todo: this increases the throwing lag even more ugh
//            RayTraceContext collisionRayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);
//            BlockRayTraceResult collisionRayHit = Minecraft.getInstance().level.clip(collisionRayCtx);

            List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(player.level, new Vec3(from), new Vec3(to), portal -> true);
            Mat4 portalMatrix = ModUtil.getMatrixFromPortalChain(portalChain);
            Pair<Vector3d, Vector3d> ray = ModUtil.teleportRay(portalChain, from, to);
            AxisAlignedBB aabb = player.getBoundingBox().expandTowards(rayPath);
            aabb = AABBUtil.transform(aabb, portalMatrix);

            EntityRayTraceResult entityRayTraceResult = ProjectileHelper.getEntityHitResult(player, ray.getFirst(), ray.getSecond(), aabb, TestElementEntity::isHoldable, rayLength * rayLength);

            if (entityRayTraceResult == null) return;

            Entity entity = entityRayTraceResult.getEntity();

            // Set reach origin to feet so it is possible to pick things up from above further than from below
            Vector3d reachPosition = player.position().add(0, 0.2, 0);
            double grabReach = player.getAttributeValue(AttributeInit.GRAB_REACH.get());
            Vec3 distance = new Vec3(entity.position()).sub(new Vec3(reachPosition).transform(portalMatrix));

            if (entity instanceof TestElementEntity && distance.magnitude() < grabReach + entity.getBbWidth() / 2) {
                ((TestElementEntity) entity).pickUp(player);

                consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
            }
        } catch (Exception e) {
            PortalMod.LOGGER.error("Error while picking up entity", e);
        }
    }

    private static void consumeAllKeyPresses(InputMappings.Input k) {
        KeyBinding[] keys = Minecraft.getInstance().options.keyMappings;
        for(KeyBinding key : keys)
            if(key.getKey() == k)
                while(key.consumeClick());
    }

    @SubscribeEvent
    public static void onMouseClick(final InputEvent.RawMouseEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if(player == null)
            return;

        if(mc.overlay == null && mc.screen == null) {
            if(player.getMainHandItem().getItem() instanceof PortalGun) {
                boolean handled = PortalGunClient.getInstance().handleMouseButtons(event.getButton(), event.getAction());

                if(handled) {
                    event.setCanceled(true);
                }
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
        TriggerTER.renderAllTriggers();
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
        FontRenderer fontRenderer = minecraft.font;

        if(PortalMod.WATERMARK) {
            WatermarkRenderer.render(event.getMatrixStack());
        }

        if(event.getType() == ElementType.CROSSHAIRS) {
            PortalGunCrosshairRenderer.render(event.getMatrixStack());
            
        } else if(event.getType() == ElementType.SUBTITLES) {
            MainWindow window = event.getWindow();
            float scale = 1.5f;
            
            int index = 0;
            for(String text : debugStrings) {
                RenderSystem.pushMatrix();
                RenderSystem.scalef(scale, scale, 1);
                RenderSystem.translatef(
                        window.getGuiScaledWidth() / (2f * scale) - (fontRenderer.width(text)) / 2f,
                        window.getGuiScaledHeight() / scale - 60 + fontRenderer.lineHeight * index++,
                        0
                );
                
                fontRenderer.draw(event.getMatrixStack(), text, 0, 0, 0xFF0000);
                
                RenderSystem.popMatrix();
            }
            
            debugStrings.clear();
        }
    }
}