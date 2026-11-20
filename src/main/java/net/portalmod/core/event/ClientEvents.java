package net.portalmod.core.event;

import net.portalmod.common.sorted.portal.ClientPortalManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.portalmod.client.event.CameraSetupEvent;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
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

/**
 * Client game event handlers. Forge called these through its event bus; on Fabric they are wired up
 * in {@link #register()} to Fabric API callbacks, or called from hook mixins in
 * {@code net.portalmod.mixins.hooks.client}.
 */
@Environment(EnvType.CLIENT)
public class ClientEvents {

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onClientLogin());
        ClientTickEvents.START_CLIENT_TICK.register(client -> clientTick(true));
        ClientTickEvents.END_CLIENT_TICK.register(client -> clientTick(false));
        WorldRenderEvents.LAST.register(ctx -> onRenderWorldLast(ctx.matrixStack()));
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> renderDebugStrings(graphics));
    }

    /** Client side of Forge's WorldEvent.Load; called when a ClientLevel is created. */
    public static void onClientLevelLoad() {
        ClientPortalManager.getInstance().clear();
    }

    public static void onClientLogin() {
        ClientSkinManager.getInstance().onClientLogin();
    }

    /** Called after PlayerRenderer#setModelProperties (Forge: RenderPlayerEvent.Pre). */
    public static void onPlayerRender(AbstractClientPlayer player, PlayerRenderer renderer) {
        HumanoidModel.ArmPose mainHand;
        HumanoidModel.ArmPose offHand;
        
        if(player.getMainArm() == HumanoidArm.RIGHT) {
            mainHand = renderer.getModel().rightArmPose;
            offHand = renderer.getModel().leftArmPose;
        } else {
            offHand = renderer.getModel().rightArmPose;
            mainHand = renderer.getModel().leftArmPose;
        }
        
        if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PortalGun)
            mainHand = ArmPose.BOW_AND_ARROW;
        if(player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof PortalGun)
            offHand = ArmPose.BOW_AND_ARROW;
        
        if(mainHand.isTwoHanded())
            offHand = player.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        
        if(player.getMainArm() == HumanoidArm.RIGHT) {
            renderer.getModel().rightArmPose = mainHand;
            renderer.getModel().leftArmPose = offHand;
        } else {
            renderer.getModel().rightArmPose = offHand;
            renderer.getModel().leftArmPose = mainHand;
        }
    }

    /** Lets the mod swap the title screen (Forge: GuiOpenEvent). Returns the screen to open. */
    public static Screen screenOpen(Screen screen) {
        if(!(screen instanceof TitleScreen)) return screen;

        Screen replacement = MainMenuInjector.getInjectedMenu(PortalModConfigManager.MENU.get(), MainMenuInjector.fading);
        MainMenuInjector.needsUpdate = false;
        MainMenuInjector.fading = false;
        return replacement;
    }
    
    private static final ChangeDetector creerNameDetector = new ChangeDetector();
    
    /** Called before LivingEntityRenderer#render; returns true to cancel vanilla rendering (Forge: RenderLivingEvent.Pre). */
    public static boolean renderLiving(LivingEntityRenderer<?, ?> renderer, LivingEntity livingEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light) {
        if(renderer instanceof CreerRenderer)
            return false;
        
        if(!CreerRenderer.isCreer(livingEntity)) {
            creerNameDetector.trigger(false);
            return false;
        }
        
        creerNameDetector.trigger(true);
        
        Creeper entity = (Creeper)livingEntity;
        
        if(creerNameDetector.get())
            entity.refreshDimensions();
        
        CreerRenderer.INSTANCE.get().render(entity, 0, partialTicks, matrixStack, renderTypeBuffer, light);
        return true;
    }

    /**
     * Fog density inside goo, or null for vanilla fog (Forge: EntityViewRenderEvent.FogDensity).
     * 1.17+ only has linear fog, so FogRendererHooksMixin turns this density into a fog distance.
     */
    public static Float fogDensity(Camera info) {
        if (isCameraInGoo(info))
            return GooBlock.getFogDensity(info.getEntity());
        return null;
    }

    private static boolean isCameraInGoo(Camera info) {
        return info.getEntity().level().getFluidState(info.getBlockPosition()).is(FluidTagInit.GOO);
    }

    /** Fog colour inside goo, or null for vanilla (Forge: EntityViewRenderEvent.FogColors). */
    public static Vector3f fogColor(Camera info) {
        if (isCameraInGoo(info))
            return GooBlock.getFogColor().to3f();
        return null;
    }

    /** Changes the size of "creer" creepers (Forge: EntityEvent.Size). */
    public static EntityDimensions entitySize(Entity entity, EntityDimensions size) {
        if(!CreerRenderer.isCreer(entity))
            return size;

        return EntityDimensions.scalable(size.width, 1f);
    }
    
    private static boolean wasPressed = false;
    
    public static void clientTick(boolean start) {
        if(!start) {
            PortalFirstPersonRenderer.updateSwingTime();
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

                ClipContext rayCtx = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null);
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

        ClipContext rayCtx = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null);
        BlockHitResult rayHit = Minecraft.getInstance().level.clip(rayCtx);

        // Drop entity
        ItemStack itemStack = player.getMainHandItem();
        if (player.hasPassenger(e -> e instanceof TestElementEntity)) {
            TestElementEntity.dropHeldEntities(player, false, false, itemStack);
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.DROP_ENTITY).build());

            consumeAllKeyPresses(KeyBindingHelper.getBoundKeyOf(KeyInit.PORTALGUN_INTERACT));
            return;
        }

        // Press block
        Block block = Minecraft.getInstance().level.getBlockState(rayHit.getBlockPos()).getBlock();
        if (block instanceof InteractKeyInteractable && ((InteractKeyInteractable) block).interactKeyInteract(player, rayHit)) {
            consumeAllKeyPresses(KeyBindingHelper.getBoundKeyOf(KeyInit.PORTALGUN_INTERACT));
            return;
        }

        // Pick up entity
        try {
//            Entity entity = Minecraft.getInstance().crosshairPickEntity;

            // Use collision shapes to target the entity instead of visual shapes
            // todo: this increases the throwing lag even more ugh
//            ClipContext collisionRayCtx = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
//            BlockHitResult collisionRayHit = Minecraft.getInstance().level.clip(collisionRayCtx);

            List<PortalEntity> portalChain = ModUtil.getPortalsAlongRay(player.level(), new Vec3(from), new Vec3(to), portal -> true);
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

                consumeAllKeyPresses(KeyBindingHelper.getBoundKeyOf(KeyInit.PORTALGUN_INTERACT));
            }
        } catch (Exception e) {
            PortalMod.LOGGER.error("Error while picking up entity", e);
        }
    }

    private static void consumeAllKeyPresses(InputConstants.Key k) {
        KeyMapping[] keys = Minecraft.getInstance().options.keyMappings;
        for(KeyMapping key : keys)
            if(KeyBindingHelper.getBoundKeyOf(key) == k)
                while(key.consumeClick());
    }

    /** Returns true to swallow the click (Forge: InputEvent.RawMouseEvent). */
    public static boolean onMouseClick(int button, int action) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return false;

        if(mc.overlay == null && mc.screen == null) {
            if(player.getMainHandItem().getItem() instanceof PortalGun) {
                return PortalGunClient.getInstance().handleMouseButtons(button, action);
            } else {
                PortalGunClient.getInstance().resetPresses();
            }
        }
        return false;
    }
    
    /** Returns false to hide the in-wall block overlay (Forge: RenderBlockOverlayEvent). */
    public static boolean shouldRenderBlockOverlay(Player player, BlockPos pos) {
        return PortalEntity.shouldRenderBlockOverlay(player.level(), pos);
    }

    public static void onRenderWorldLast(PoseStack matrixStack) {
        DebugRenderer.renderAllShapes(matrixStack);
    }

    public static void onCameraSetup(final CameraSetupEvent event) {
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

    /** Called with the final fog colour each frame. */
    public static void onFogSetup(float red, float green, float blue) {
        PortalRenderer.getInstance().clearColor = new Vec3(red, green, blue);
    }

    public static final List<String> debugStrings = new ArrayList<>();

    /** Called after the vanilla crosshair is drawn (Forge: RenderGameOverlayEvent.Post CROSSHAIRS). */
    public static void onRenderCrosshair(GuiGraphics graphics) {
        PortalGunCrosshairRenderer.render(graphics.pose());

        if(PortalMod.WATERMARK) {
            WatermarkRenderer.render(graphics.pose());
        }
    }

    private static void renderDebugStrings(GuiGraphics graphics) {
        Font fontRenderer = Minecraft.getInstance().font;
        int x = 2;
        int y = 2;
        int index = 0;
        for(String text : debugStrings) {
            graphics.drawString(fontRenderer, text, x, y + fontRenderer.lineHeight * index++, 0xFFFFAA00);
        }

        debugStrings.clear();
    }
}
