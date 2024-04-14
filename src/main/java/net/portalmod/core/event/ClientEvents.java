package net.portalmod.core.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
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
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.portalmod.PortalMod;
import net.portalmod.client.render.PortalFirstPersonRenderer;
import net.portalmod.client.screens.PortalModOptionsScreen;
import net.portalmod.common.blocks.StandingButtonBlock;
import net.portalmod.common.sorted.creer.CreerRenderer;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.faithplate.FaithPlateTER;
import net.portalmod.common.sorted.faithplate.FaithPlateTileEntity;
import net.portalmod.common.sorted.faithplate.IFaithPlateLaunchable;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalEntityClient;
import net.portalmod.common.sorted.portal.PortalRenderer;
import net.portalmod.common.sorted.portalgun.CPortalGunInteractionPacket;
import net.portalmod.common.sorted.portalgun.PortalGun;
import net.portalmod.common.sorted.portalgun.PortalGunCrosshairRenderer;
import net.portalmod.common.sorted.portalgun.PortalGunInteraction;
import net.portalmod.common.sorted.turret.TurretEntity;
import net.portalmod.core.chunkviewer.ChunkViewer;
import net.portalmod.core.init.FluidTagInit;
import net.portalmod.core.init.ItemInit;
import net.portalmod.core.init.KeyInit;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.injectors.LivingEntityInjector;
import net.portalmod.core.injectors.MainMenuInjector;
import net.portalmod.core.util.ChangeDetector;
import net.portalmod.mixins.accessors.ChunkManagerAccessor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

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

        if(!event.player.level.isClientSide) {
            ServerWorld level = (ServerWorld)event.player.level;
            Iterable<ChunkHolder> chunkList = ((ChunkManagerAccessor)level.getChunkSource().chunkMap).pmGetChunks();

            if(level.getGameTime() % 20 == 0) {
                ChunkViewer.getInstance().getChunkList().clear();
                for(ChunkHolder chunk : chunkList)
                    ChunkViewer.getInstance().getChunkList().add(new ChunkPos(chunk.getPos().x, chunk.getPos().z + 30));
                ChunkViewer.getInstance().refresh();
            }
        }
        
        if(event.phase == Phase.START)
            if(player.abilities.flying)
                ((IFaithPlateLaunchable)player).setLaunched(false);
        
        if(event.phase == Phase.END)
            if(player.inventory.getSelected().getItem() != ItemInit.WRENCH.get())
                FaithPlateTER.selected = null;
        
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

        // todo dont actually have this
        // im so done with this
        ChunkViewer.getInstance().setVisible(false);

        event.setGui(MainMenuInjector.getInjectedMenu(PortalModOptionsScreen.MENU.get(), MainMenuInjector.fading));
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
    public static void renderFog(final EntityViewRenderEvent.RenderFogEvent event) {
//        FogRenderer.setupNoFog();
    }

    @SubscribeEvent
    public static void fogDensity(final EntityViewRenderEvent.FogDensity event) {
        if (event.getInfo().getFluidInCamera().is(FluidTagInit.GOO)) {
            event.setDensity(0.95f);
            event.setCanceled(true);
        }
    }

    // R: 55
    // G: 46
    // B: 33
    @SubscribeEvent
    public static void fogColor(final EntityViewRenderEvent.FogColors event) {
        FluidState fluidInCamera = event.getInfo().getFluidInCamera();
        if (fluidInCamera.is(FluidTagInit.GOO)) {
            event.setRed(0.215f);
            event.setGreen(0.180f);
            event.setBlue(0.129f);
        }
    }
    
    @SubscribeEvent
    public static void entitySize(final EntityEvent.Size event) {
        if(!CreerRenderer.isCreer(event.getEntity()))
            return;
        
        EntitySize size = event.getOldSize();
        event.setNewSize(EntitySize.scalable(size.width, size.height * .55f), true);
    }
    
    private static boolean wasPressed = false;
    
    @SubscribeEvent
    public static void clientTick(final TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.START)
            return;

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
        PlayerEntity player = Minecraft.getInstance().player;

        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_J)) {
            ChunkViewer.getInstance().setVisible(true);
        }

        if(KeyInit.PORTALGUN_INTERACT.isDown()) {
            if(player.getMainHandItem().getItem() instanceof PortalGun && !wasPressed) {
                float rayLength = Minecraft.getInstance().gameMode.getPickRange();
                Vector3d playerRotation = Minecraft.getInstance().player.getViewVector(0);
                Vector3d rayPath = playerRotation.scale(rayLength);

                Vector3d from = Minecraft.getInstance().player.getEyePosition(0);
                Vector3d to = from.add(rayPath);

                RayTraceContext rayCtx = new RayTraceContext(from, to, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null);
                BlockRayTraceResult rayHit = Minecraft.getInstance().level.clip(rayCtx);

                // TODO ray trace on the server too
                // hey this is lars here, vanilla doesnt do that, they send a packet with the BlockRayTraceResult to the server and then don't confirm anything

//                if(rayHit.getType() == RayTraceResult.Type.MISS) {
//                    return;
//                }

                if(player.hasPassenger(Cube.class) || player.hasPassenger(TurretEntity.class)) {
                    PortalGun.dropCube(player, false);
                    PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.DROP_ENTITY).build());

                    consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
                }
                else if (Minecraft.getInstance().level.getBlockState(rayHit.getBlockPos()).getBlock() instanceof StandingButtonBlock) {
                    PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.PRESS_BUTTON).blockHit(rayHit).build());
                    consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
                }
                else {
                    try {
                        Entity entity = Minecraft.getInstance().crosshairPickEntity;

                        if(PortalGun.isHoldable(entity)) {
                            entity.startRiding(player);
                            PacketInit.INSTANCE.sendToServer(new CPortalGunInteractionPacket.Builder(PortalGunInteraction.PICK_ENTITY).data(entity.getId()).build());

                            consumeAllKeyPresses(KeyInit.PORTALGUN_INTERACT.getKey());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            wasPressed = true;
        } else {
            wasPressed = false;
        }
    }

    private static void consumeAllKeyPresses(InputMappings.Input k) {
        KeyBinding[] keys = Minecraft.getInstance().options.keyMappings;
        for(KeyBinding key : keys)
            if(key.getKey() == k)
                while(key.consumeClick());
    }

    @SubscribeEvent
    public static void onMouseClick(final InputEvent.ClickInputEvent event) {
        if(event.getHand() != Hand.MAIN_HAND)
            return;
        
        boolean isItemFrame = Minecraft.getInstance().getEntityRenderDispatcher().crosshairPickEntity instanceof ItemFrameEntity;
        if(isItemFrame && event.isUseItem())
            return;
        
        if(Minecraft.getInstance().player.getMainHandItem().getItem() instanceof PortalGun) {
            if(event.isAttack())
                PortalGun.handleLeftClick();
            if(event.isUseItem())
                PortalGun.handleRightClick();
            if(event.isPickBlock())
                return;

            event.setCanceled(true);
            event.setSwingHand(false);
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
        
        PortalRenderer.renderHighlights();






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
    }

    @SubscribeEvent
    public static void onCameraSetup(final EntityViewRenderEvent.CameraSetup event) {
        // TODO add animations
//        if(PortalFirstPersonRenderer.swinging && Minecraft.getInstance().player.getMainHandItem().getItem() instanceof PortalGun && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
//            float actualSwingProgress = PortalFirstPersonRenderer.getAttackAnim((float) event.getRenderPartialTicks());
//            float animation = MathHelper.sin((actualSwingProgress * 2 - 0.5f) * (float) Math.PI) / 2 + 0.5f;
//
//            event.setPitch(event.getPitch() - animation * 3);
//            event.setYaw(event.getYaw() - animation * 3);
//        }
        
        PortalEntityClient.teleportCamera(event);
        
        float xRot = event.getInfo().getXRot();
        float yRot = event.getInfo().getYRot();
        event.setPitch(xRot);
        event.setYaw(yRot);

        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_G)) {

        }
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
        
        if(event.getType() == ElementType.TEXT && !minecraft.options.renderDebug) {
            String fps = Minecraft.getInstance().fpsString.split("fps")[0];
            String pos = String.format(Locale.ROOT, "pos: %.2f %.2f %.2f", entity.getX(), entity.getY(), entity.getZ());
            String ang = String.format(Locale.ROOT, "ang: %.2f %.2f", camera.getXRot(), camera.getYRot());
            String vel = String.format(Locale.ROOT, "vel: %.2f %.2f %.2f", velocity.x, velocity.y, velocity.z);
            fontRenderer.draw(new MatrixStack(), fps + "fps", 2, 2, 14737632);
            fontRenderer.draw(new MatrixStack(), pos, 2, 2 + 10 * 1, 14737632);
            fontRenderer.draw(new MatrixStack(), ang, 2, 2 + 10 * 2, 14737632);
            fontRenderer.draw(new MatrixStack(), vel, 2, 2 + 10 * 3, 14737632);


            
            
//            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
//            Texture textureAtlas = textureManager.getTexture(new ResourceLocation("textures/atlas/blocks.png"));
//            textureAtlas.bind();
//            Screen.blit(new MatrixStack(), 0, 0, 0, 0, 0, 512, 512, 512, 512);
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