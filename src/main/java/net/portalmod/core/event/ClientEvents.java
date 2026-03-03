package net.portalmod.core.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
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
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.portalmod.PMState;
import net.portalmod.PortalMod;
import net.portalmod.client.render.PortalFirstPersonRenderer;
import net.portalmod.client.render.WatermarkRenderer;
import net.portalmod.common.entities.TestElementEntity;
import net.portalmod.common.items.ModSpawnEggItem;
import net.portalmod.common.sorted.button.StandingButtonBlock;
import net.portalmod.common.sorted.creer.CreerRenderer;
import net.portalmod.common.sorted.faithplate.CFaithPlateEndConfigPacket;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.common.sorted.faithplate.Flingable;
import net.portalmod.common.sorted.goo.GooBlock;
import net.portalmod.common.sorted.portal.CameraAnimator;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalEntityClient;
import net.portalmod.common.sorted.portal.PortalRenderer;
import net.portalmod.common.sorted.portalgun.*;
import net.portalmod.common.sorted.portalgun.skins.SkinManager;
import net.portalmod.common.sorted.trigger.TriggerSelectionClient;
import net.portalmod.common.sorted.trigger.TriggerTER;
import net.portalmod.core.chunkviewer.ChunkViewer;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.*;
import net.portalmod.core.injectors.LivingEntityInjector;
import net.portalmod.core.injectors.MainMenuInjector;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.ChangeDetector;
import net.portalmod.core.util.DebugRenderer;
import net.portalmod.mixins.accessors.ActiveRenderInfoAccessor;
import net.portalmod.mixins.accessors.ChunkManagerAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START)
            SkinManager.getClientInstance().tick();
    }

    @SubscribeEvent
    public static void onClientLogin(final ClientPlayerNetworkEvent.LoggedInEvent event) {
        SkinManager.getClientInstance().onClientLogin();
    }

//    @SubscribeEvent
//    public static void onClientLevelLoad(final WorldEvent.Load event) {
////        PortalPairCache.select(event.getWorld().isClientSide()).clear();
//        World overworld = ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD);
//        if(!event.getWorld().isClientSide() && event.getWorld() == overworld) {
//            PortalManager.clear();
//            ((ServerWorld)event.getWorld()).getDataStorage().get(PortalManager::getInstance, PortalManager.PATH);
//        }
//    }
    
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

//        if(player.abilities.flying && !player.isPassenger()) {
//            Vector3d velocity = player.getDeltaMovement().multiply(1, 1. / .6, 1);
//            if(velocity.y > 10)
//                velocity = new Vector3d(velocity.x, 10, velocity.z);
//            player.setDeltaMovement(velocity);
//        }
    }

    @SubscribeEvent
    public static void onLevelLoad(final WorldEvent.Load event) {

    }
    
    @SubscribeEvent
    public static void onLivingUpdate(final LivingUpdateEvent event) {
        LivingEntityInjector.onPreTick(event.getEntityLiving());
        
        LivingEntity entity = (LivingEntity)event.getEntity();
        World level = entity.level;
        BlockPos onPos;
        
        {
            int i = MathHelper.floor(entity.position().x);
            int j = MathHelper.floor(entity.position().y - (double)0.2F);
            int k = MathHelper.floor(entity.position().z);
            onPos = new BlockPos(i, j, k);
            if(entity.level.isEmptyBlock(onPos)) {
                BlockPos blockpos1 = onPos.below();
                BlockState blockstate = entity.level.getBlockState(blockpos1);
                if(blockstate.collisionExtendsVertically(entity.level, blockpos1, entity))
                    onPos = blockpos1;
            }
        }
        
        boolean isCollidingFaithPlate = false;
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        BlockPos blockpos = new BlockPos(axisalignedbb.minX - .5, axisalignedbb.minY - .5, axisalignedbb.minZ - .5);
        BlockPos blockpos1 = new BlockPos(axisalignedbb.maxX + .5, axisalignedbb.maxY + .5, axisalignedbb.maxZ + .5);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        
        // TODO make method willLaunch in faithplate
        cycle:
        if(level.hasChunksAt(blockpos, blockpos1)) {
           for(int i = blockpos.getX(); i <= blockpos1.getX(); i++) {
              for(int j = blockpos.getY(); j <= blockpos1.getY(); j++) {
                 for(int k = blockpos.getZ(); k <= blockpos1.getZ(); k++) {
                    blockpos$mutable.set(i, j, k);
                    TileEntity blockEntity = level.getBlockEntity(blockpos$mutable);
                    if(blockEntity instanceof FaithPlateTileEntity) {
                        FaithPlateTileEntity faithPlate = (FaithPlateTileEntity)blockEntity;
                        if(((FaithPlateTileEntity)blockEntity).getTrigger().intersects(axisalignedbb)
                            && !(faithPlate.getTargetPos() == null || faithPlate.getTargetFace() == null || !faithPlate.isEnabled())) {
                            isCollidingFaithPlate = true;
                            break cycle;
                        }
                    }
                 }
              }
           }
        }
        
//        if((entity.horizontalCollision || entity.verticalCollision) && !isCollidingFaithPlate)
//            ((IFaithPlateLaunchable)entity).setLaunched(false);
    }
    
    @SubscribeEvent
    public static void onLivingFall(final LivingFallEvent event) {
        if(event.getEntityLiving().getItemBySlot(EquipmentSlotType.FEET).getItem() == ItemInit.LONGFALL_BOOTS.get())
            event.setCanceled(true);
    }
    
    @SubscribeEvent
    public static void screenOpen(final GuiOpenEvent event) {
        if(!(event.getGui() instanceof MainMenuScreen) || !MainMenuInjector.needsUpdate)
            return;

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
    public static void renderFog(final EntityViewRenderEvent.RenderFogEvent event) {
//        FogRenderer.setupNoFog();
    }

    @SubscribeEvent
    public static void fogDensity(final EntityViewRenderEvent.FogDensity event) {
        if (event.getInfo().getFluidInCamera().is(FluidTagInit.GOO)) {
            event.setDensity(GooBlock.FOG_DENSITY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void fogColor(final EntityViewRenderEvent.FogColors event) {
        FluidState fluidInCamera = event.getInfo().getFluidInCamera();
        if (fluidInCamera.is(FluidTagInit.GOO)) {
            GooBlock.setFogColor(event);
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
        if(event.phase != TickEvent.Phase.START) {
            return;
        }

//        System.out.println(MathHelper.floor(Minecraft.getInstance().player.getX()) >> 4);
//        System.out.println(MathHelper.floor(Minecraft.getInstance().player.getZ()) >> 4);

//        if(Minecraft.getInstance().level != null)
//
//        Field storage = ObfuscationReflectionHelper.findField(ClientChunkProvider.class, "storage");
//        try {
//            storage.get(Minecraft.getInstance().level.getChunkSource());
//        } catch(IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }

//        World level = Minecraft.getInstance().level;

        // STIK ER IN
        // 🔥
//        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_J)) {
//            ChunkViewer.getInstance().setVisible(true);
//        }

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

        // TODO ray trace on the server too
        // hey this is lars here, vanilla doesnt do that, they send a packet with the BlockRayTraceResult to the server and then don't confirm anything
        // which makes sense because there can be a desync between the two so the server just trusts the client

        // Drop entity
        ItemStack itemStack = player.getMainHandItem();
        if (player.hasPassenger(TestElementEntity.class)) {
            TestElementEntity.dropHeldEntities(player, false, false, itemStack);
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.DROP_ENTITY).build());

            consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
            return;
        }

        // Press button
        Block block = Minecraft.getInstance().level.getBlockState(rayHit.getBlockPos()).getBlock();
        double buttonReach = player.getAttributeValue(AttributeInit.BUTTON_REACH.get());
        if (block instanceof StandingButtonBlock && rayHit.getLocation().subtract(player.getEyePosition(1)).length() < buttonReach) {
            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.PRESS_BUTTON).blockHit(rayHit).build());
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

            EntityRayTraceResult entityRayTraceResult = ProjectileHelper.getEntityHitResult(player, from, to, player.getBoundingBox().expandTowards(rayPath), TestElementEntity::isHoldable, rayLength * rayLength);

            if (entityRayTraceResult == null) return;

            Entity entity = entityRayTraceResult.getEntity();

            // Set reach origin to feet so it is possible to pick things up from above further than from below
            Vector3d reachPosition = player.position().add(0, 0.2, 0);

            double grabReach = player.getAttributeValue(AttributeInit.GRAB_REACH.get());

            if (entity instanceof TestElementEntity && entity.position().subtract(reachPosition).length() < grabReach + entity.getBbWidth() / 2) {
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

//    private static final Map<Object, Tuple<VoxelShape, Long>> DEBUG_SHAPES = new HashMap<>();
//
//    public static void addDebugShape(Object key, VoxelShape shape) {
//        DEBUG_SHAPES.put(key, new Tuple<>(shape, System.currentTimeMillis()));
//    }

    public static VoxelShape DEBUG_SHAPE = VoxelShapes.empty();

    @SubscribeEvent
    public static void onRenderWorldLast(final RenderWorldLastEvent event) {
//        MatrixStack matrix = new MatrixStack();
//        ActiveRenderInfo camera = Minecraft.getInstance().gameRenderer.getMainCamera();
//        matrix.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
//        matrix.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
//        
//        Vector3d cameraPos = camera.getPosition();
//        matrix.translate(0 - cameraPos.x, 60 - cameraPos.y, 0 - cameraPos.z);
//        
//        RenderUtil.setClipPlane(0, camera, Vector3d.ZERO, new Vector3d(0, 0, 1));
//        RenderUtil.setClipPlane(1, camera, Vector3d.ZERO, new Vector3d(1, 0, 0));
//        
//        Minecraft.getInstance().getItemRenderer().renderStatic(
//                new ItemStack(Items.DIAMOND),
//                TransformType.FIXED, 0, 0, matrix,
//                Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource());
//        
//        Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource().endBatch();
//        
//        RenderUtil.disableClipPlane(0);
//        RenderUtil.disableClipPlane(1);
        
//        PortalEntityRenderer.renderHighlights();

        // BEWARE: PORTAL RENDERING
//        PortalRenderer.renderHighlights();






//        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_C)) {
//            RenderSystem.clear(16640, Minecraft.ON_OSX);
//            MainWindow mainwindow = Minecraft.getInstance().getWindow();
////            RenderSystem.matrixMode(5889);
////            RenderSystem.loadIdentity();
////            RenderSystem.ortho(0.0D, (double)mainwindow.getWidth() / mainwindow.getGuiScale(), (double)mainwindow.getHeight() / mainwindow.getGuiScale(), 0.0D, 1000.0D, net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
////            RenderSystem.matrixMode(5888);
////            RenderSystem.loadIdentity();
////            RenderSystem.translatef(0.0F, 0.0F, 1000.0F - net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
////
////            AbstractGui.blit(new MatrixStack(), );
//
//
////            RenderSystem.disableCull();
////            RenderSystem.enableBlend();
////
////            RenderSystem.activeTexture(GL_TEXTURE0);
////
////            ShaderInit.BLIT.get().bind();
////            ShaderInit.BLIT.get().setInt("texture", 0);
////            ShaderInit.BLIT.get().setMatrix("projection", Matrix4f.orthographic(mainwindow.getWidth(), mainwindow.getHeight(), -1, 1));
//////            ShaderInit.BLIT.get().setMatrix("projection", new Matrix4f(new float[] {
//////                    2f / frameBuffer.width, 0, 0, -1,
//////                    0, 2f / frameBuffer.height, 0, -1,
//////                    0, 0, 1, 0,
//////                    0, 0, 0, 1
//////            }));
////
////            GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
////            Minecraft.getInstance().getMainRenderTarget().getDepthTextureId();
////            vbo.bind();
////            DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
////            RenderSystem.drawArrays(7, 0, 4);
////            VertexBuffer.unbind();
////            frameBuffer.unbindRead();
////
////            ShaderInit.BLIT.get().unbind();
//
//            Framebuffer fbo = Minecraft.getInstance().getMainRenderTarget();
//
//
//            GlStateManager._colorMask(true, true, true, false);
//            GlStateManager._disableDepthTest();
//            GlStateManager._depthMask(false);
//            GlStateManager._matrixMode(5889);
//            GlStateManager._loadIdentity();
//            GlStateManager._ortho(0.0D, fbo.width, fbo.height, 0.0D, 1000.0D, 3000.0D);
//            GlStateManager._matrixMode(5888);
//            GlStateManager._loadIdentity();
//            GlStateManager._translatef(0.0F, 0.0F, -2000.0F);
//            GlStateManager._viewport(0, 0, fbo.width, fbo.height);
//            GlStateManager._enableTexture();
//            GlStateManager._disableLighting();
//            GlStateManager._disableAlphaTest();
//
//            GlStateManager._disableBlend();
//            GlStateManager._enableColorMaterial();
//
//            GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
////            fbo.bindRead();
//            GL11.glBindTexture(GL_TEXTURE_2D, fbo.getColorTextureId());
//            float f = (float)fbo.width;
//            float f1 = (float)fbo.height;
//            float f2 = (float)fbo.viewWidth / (float)fbo.width;
//            float f3 = (float)fbo.viewHeight / (float)fbo.height;
//            Tessellator tessellator = RenderSystem.renderThreadTesselator();
//            BufferBuilder bufferbuilder = tessellator.getBuilder();
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.vertex(0.0D, (double)f1, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
//            bufferbuilder.vertex((double)f, (double)f1, 0.0D).uv(f2, 0.0F).color(255, 255, 255, 255).endVertex();
//            bufferbuilder.vertex((double)f, 0.0D, 0.0D).uv(f2, f3).color(255, 255, 255, 255).endVertex();
//            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, f3).color(255, 255, 255, 255).endVertex();
//            tessellator.end();
//            fbo.unbindRead();
//            GlStateManager._depthMask(true);
//            GlStateManager._colorMask(true, true, true, true);
//        }




//        DEBUG_SHAPES.forEach((k, v) -> {
//            for(AxisAlignedBB aabb : v.getA().toAabbs()) {
//                WorldRenderer.renderLineBox(event.getMatrixStack(),
//                        Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource().getBuffer(RenderType.lines()),
//                        aabb, 1, 1, 1, 1);
//            }
//
//            if(System.currentTimeMillis() - v.getB() > 1000)
//                DEBUG_SHAPES.remove(k);
//        });



//        for(AxisAlignedBB aabb : DEBUG_SHAPE.toAabbs()) {
//            WorldRenderer.renderLineBox(event.getMatrixStack(),
//                    Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource().getBuffer(RenderType.lines()),
//                    aabb.move(-event.getContext().prevCamX, -event.getContext().prevCamY, -event.getContext().prevCamZ),
//                    1, 1, 1, 1);
//        }

//        for(Tuple<VoxelShape, Long> shape : DEBUG_SHAPES) {
//
//        }
//        DEBUG_SHAPES.clear();

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

            PortalEntityClient.teleportCameraAndApply(event);
        }

        PMState.cameraRoll = event.getRoll();
    }

    @SubscribeEvent
    public static void onFogSetup(final EntityViewRenderEvent.FogColors event) {
        PortalRenderer.getInstance().clearColor = new Vec3(event.getRed(), event.getGreen(), event.getBlue());
    }

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent event) {
        // TODO add to present method
        if(event.phase == TickEvent.Phase.END)
            PortalFirstPersonRenderer.updateSwingTime();
    }
    
    public static final ResourceLocation INFO_ICON = new ResourceLocation(PortalMod.MODID, "textures/gui/icons/info.png");
    public static final List<String> debugStrings = new ArrayList<>();
    
    @SubscribeEvent
    public static void onRenderOverlay(final RenderGameOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontRenderer = minecraft.font;
        Entity entity = minecraft.getCameraEntity();
        ActiveRenderInfo camera = minecraft.gameRenderer.getMainCamera();
        Vector3d velocity = entity.getDeltaMovement();
        
//        if (event.getType() == ElementType.TEXT && !minecraft.options.renderDebug) {
//            String fps = Minecraft.getInstance().fpsString.split("fps")[0];
//            String pos = String.format(Locale.ROOT, "pos: %.2f %.2f %.2f", entity.getX(), entity.getY(), entity.getZ());
//            String ang = String.format(Locale.ROOT, "ang: %.2f %.2f", camera.getXRot(), camera.getYRot() % 360);
//            String vel = String.format(Locale.ROOT, "vel: %.2f %.2f %.2f", velocity.x, velocity.y, velocity.z);
//            fontRenderer.draw(new MatrixStack(), fps + "fps", 2, 2, 14737632);
//            fontRenderer.draw(new MatrixStack(), pos, 2, 2 + 10 * 1, 14737632);
//            fontRenderer.draw(new MatrixStack(), ang, 2, 2 + 10 * 2, 14737632);
//            fontRenderer.draw(new MatrixStack(), vel, 2, 2 + 10 * 3, 14737632);
//
////            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
////            Texture textureAtlas = textureManager.getTexture(new ResourceLocation("textures/atlas/blocks.png"));
////            textureAtlas.bind();
////            Screen.blit(new MatrixStack(), 0, 0, 0, 0, 0, 512, 512, 512, 512);
//        }


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
            
//            FontRenderer fontRenderer = Minecraft.getInstance().font;
//            MainWindow window = event.getWindow();
//            String text = "Use the wrench to configure";
//            float scale = 1.5f;
//            int space = 11;
//
//            RenderSystem.pushMatrix();
//            RenderSystem.scalef(scale, scale, 1);
//            RenderSystem.translatef(
//                    window.getGuiScaledWidth() / (2f * scale) - (fontRenderer.width(text) + space) / 2f,
//                    window.getGuiScaledHeight() / scale - 60,
//                    0
//            );

//            {
//                int x1 = -2;
//                int y1 = -2;
//                int x2 = 2 + fontRenderer.width(text) + space;
//                int y2 = 2 + fontRenderer.lineHeight;
//                int color = 0x3F000000;
//
//                if (x1 < x2) {
//                    int i = x1;
//                    x1 = x2;
//                    x2 = i;
//                }
//
//                if (y2 < y1) {
//                    int j = y2;
//                    y2 = y1;
//                    y1 = j;
//                }
//
//                float f3 = (float) (color >> 24 & 255) / 255.0F;
//                float f = (float) (color >> 16 & 255) / 255.0F;
//                float f1 = (float) (color >> 8 & 255) / 255.0F;
//                float f2 = (float) (color & 255) / 255.0F;
//                BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
//                RenderSystem.enableBlend();
//                RenderSystem.disableTexture();
//                RenderSystem.defaultBlendFunc();
//                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
//                bufferbuilder.vertex(event.getMatrixStack().last().pose(), (float) x1, (float) y1, 0.0F).color(f, f1, f2, f3).endVertex();
//                bufferbuilder.vertex(event.getMatrixStack().last().pose(), (float) x2, (float) y1, 0.0F).color(f, f1, f2, f3).endVertex();
//                bufferbuilder.vertex(event.getMatrixStack().last().pose(), (float) x2, (float) y2, 0.0F).color(f, f1, f2, f3).endVertex();
//                bufferbuilder.vertex(event.getMatrixStack().last().pose(), (float) x1, (float) y2, 0.0F).color(f, f1, f2, f3).endVertex();
//                bufferbuilder.end();
//                WorldVertexBufferUploader.end(bufferbuilder);
//                RenderSystem.enableTexture();
//                RenderSystem.disableBlend();
//            }
//
//            fontRenderer.draw(event.getMatrixStack(), text,space,0,0xE0E0E0);
////            fontRenderer.drawShadow(event.getMatrixStack(), text,0,0,0xE0E0E0);
//
//            RenderSystem.disableBlend();
//            Minecraft.getInstance().getTextureManager().bind(INFO_ICON);
//            AbstractGui.blit(event.getMatrixStack(),
//                    0,
//                    0,
//                    0, 0, 0, 9, 9, 9, 9);
//            RenderSystem.enableBlend();
//
//            RenderSystem.popMatrix();
        }
    }
}