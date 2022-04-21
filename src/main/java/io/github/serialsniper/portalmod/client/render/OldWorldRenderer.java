package io.github.serialsniper.portalmod.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.*;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.client.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import javax.annotation.*;
import java.util.*;

public class OldWorldRenderer {
    private static final ObjectList<LocalRenderInformationContainer> renderChunks = new ObjectArrayList<>(69696);

    @OnlyIn(Dist.CLIENT)
    public static class LocalRenderInformationContainer {
        public final ChunkRenderDispatcher.ChunkRender chunk;
        public final Direction sourceDirection;
        public byte directions;
        public final int step;

        private LocalRenderInformationContainer(ChunkRenderDispatcher.ChunkRender p_i46248_2_, @Nullable Direction p_i46248_3_, int p_i46248_4_) {
            this.chunk = p_i46248_2_;
            this.sourceDirection = p_i46248_3_;
            this.step = p_i46248_4_;
        }

        public void setDirections(byte p_189561_1_, Direction p_189561_2_) {
            this.directions = (byte)(this.directions | p_189561_1_ | 1 << p_189561_2_.ordinal());
        }

        public boolean hasDirection(Direction p_189560_1_) {
            return (this.directions & 1 << p_189560_1_.ordinal()) > 0;
        }
    }

//    private static void setupRender(ActiveRenderInfo p_228437_1_, ClippingHelper p_228437_2_, boolean p_228437_3_, int p_228437_4_, boolean p_228437_5_) {
//        Vector3d vector3d = p_228437_1_.getPosition();
//        if (Minecraft.getInstance().options.renderDistance != Minecraft.getInstance().levelRenderer.lastViewDistance) {
//            Minecraft.getInstance().levelRenderer.allChanged();
//        }
//
//        Minecraft.getInstance().levelRenderer.level.getProfiler().push("camera");
//        double d0 = Minecraft.getInstance().player.getX() - Minecraft.getInstance().levelRenderer.lastCameraX;
//        double d1 = Minecraft.getInstance().player.getY() - Minecraft.getInstance().levelRenderer.lastCameraY;
//        double d2 = Minecraft.getInstance().player.getZ() - Minecraft.getInstance().levelRenderer.lastCameraZ;
//        if (Minecraft.getInstance().levelRenderer.lastCameraChunkX != Minecraft.getInstance().player.xChunk || Minecraft.getInstance().levelRenderer.lastCameraChunkY != Minecraft.getInstance().player.yChunk || Minecraft.getInstance().levelRenderer.lastCameraChunkZ != Minecraft.getInstance().player.zChunk || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D) {
//            Minecraft.getInstance().levelRenderer.lastCameraX = Minecraft.getInstance().player.getX();
//            Minecraft.getInstance().levelRenderer.lastCameraY = Minecraft.getInstance().player.getY();
//            Minecraft.getInstance().levelRenderer.lastCameraZ = Minecraft.getInstance().player.getZ();
//            Minecraft.getInstance().levelRenderer.lastCameraChunkX = Minecraft.getInstance().player.xChunk;
//            Minecraft.getInstance().levelRenderer.lastCameraChunkY = Minecraft.getInstance().player.yChunk;
//            Minecraft.getInstance().levelRenderer.lastCameraChunkZ = Minecraft.getInstance().player.zChunk;
//            Minecraft.getInstance().levelRenderer.viewArea.repositionCamera(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getZ());
//        }
//
//        Minecraft.getInstance().levelRenderer.chunkRenderDispatcher.setCamera(vector3d);
//        Minecraft.getInstance().levelRenderer.level.getProfiler().popPush("cull");
//        Minecraft.getInstance().getProfiler().popPush("culling");
//        BlockPos blockpos = p_228437_1_.getBlockPosition();
//        ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender = Minecraft.getInstance().levelRenderer.viewArea.getRenderChunkAt(blockpos);
//        int i = 16;
//        BlockPos blockpos1 = new BlockPos(MathHelper.floor(vector3d.x / 16.0D) * 16, MathHelper.floor(vector3d.y / 16.0D) * 16, MathHelper.floor(vector3d.z / 16.0D) * 16);
//        float f = p_228437_1_.getXRot();
//        float f1 = p_228437_1_.getYRot();
//        Minecraft.getInstance().levelRenderer.needsUpdate = Minecraft.getInstance().levelRenderer.needsUpdate || !Minecraft.getInstance().levelRenderer.chunksToCompile.isEmpty() || vector3d.x != Minecraft.getInstance().levelRenderer.prevCamX || vector3d.y != Minecraft.getInstance().levelRenderer.prevCamY || vector3d.z != Minecraft.getInstance().levelRenderer.prevCamZ || (double)f != Minecraft.getInstance().levelRenderer.prevCamRotX || (double)f1 != Minecraft.getInstance().levelRenderer.prevCamRotY;
//        Minecraft.getInstance().levelRenderer.prevCamX = vector3d.x;
//        Minecraft.getInstance().levelRenderer.prevCamY = vector3d.y;
//        Minecraft.getInstance().levelRenderer.prevCamZ = vector3d.z;
//        Minecraft.getInstance().levelRenderer.prevCamRotX = (double)f;
//        Minecraft.getInstance().levelRenderer.prevCamRotY = (double)f1;
//        Minecraft.getInstance().getProfiler().popPush("update");
//        if (!p_228437_3_ && Minecraft.getInstance().levelRenderer.needsUpdate) {
//            Minecraft.getInstance().levelRenderer.needsUpdate = false;
//            Minecraft.getInstance().levelRenderer.renderChunks.clear();
//            Queue<WorldRenderer.LocalRenderInformationContainer> queue = Queues.newArrayDeque();
//            Entity.setViewScale(MathHelper.clamp((double)Minecraft.getInstance().options.renderDistance / 8.0D, 1.0D, 2.5D) * (double)Minecraft.getInstance().options.entityDistanceScaling);
//            boolean flag = Minecraft.getInstance().smartCull;
//            if (chunkrenderdispatcher$chunkrender != null) {
//                if (p_228437_5_ && Minecraft.getInstance().levelRenderer.level.getBlockState(blockpos).isSolidRender(Minecraft.getInstance().levelRenderer.level, blockpos)) {
//                    flag = false;
//                }
//
//                chunkrenderdispatcher$chunkrender.setFrame(p_228437_4_);
//                queue.add(new WorldRenderer.LocalRenderInformationContainer(chunkrenderdispatcher$chunkrender, (Direction)null, 0));
//            } else {
//                int j = blockpos.getY() > 0 ? 248 : 8;
//                int k = MathHelper.floor(vector3d.x / 16.0D) * 16;
//                int l = MathHelper.floor(vector3d.z / 16.0D) * 16;
//                List<WorldRenderer.LocalRenderInformationContainer> list = Lists.newArrayList();
//
//                for(int i1 = -Minecraft.getInstance().levelRenderer.lastViewDistance; i1 <= Minecraft.getInstance().levelRenderer.lastViewDistance; ++i1) {
//                    for(int j1 = -Minecraft.getInstance().levelRenderer.lastViewDistance; j1 <= Minecraft.getInstance().levelRenderer.lastViewDistance; ++j1) {
//                        ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender1 = Minecraft.getInstance().levelRenderer.viewArea.getRenderChunkAt(new BlockPos(k + (i1 << 4) + 8, j, l + (j1 << 4) + 8));
//                        if (chunkrenderdispatcher$chunkrender1 != null && p_228437_2_.isVisible(chunkrenderdispatcher$chunkrender1.bb)) {
//                            chunkrenderdispatcher$chunkrender1.setFrame(p_228437_4_);
//                            list.add(new WorldRenderer.LocalRenderInformationContainer(chunkrenderdispatcher$chunkrender1, (Direction)null, 0));
//                        }
//                    }
//                }
//
//                list.sort(Comparator.comparingDouble((p_230016_1_) -> {
//                    return blockpos.distSqr(p_230016_1_.chunk.getOrigin().offset(8, 8, 8));
//                }));
//                queue.addAll(list);
//            }
//
//            Minecraft.getInstance().getProfiler().push("iteration");
//
//            while(!queue.isEmpty()) {
//                WorldRenderer.LocalRenderInformationContainer worldrenderer$localrenderinformationcontainer1 = queue.poll();
//                ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender3 = worldrenderer$localrenderinformationcontainer1.chunk;
//                Direction direction = worldrenderer$localrenderinformationcontainer1.sourceDirection;
//                Minecraft.getInstance().levelRenderer.renderChunks.add(worldrenderer$localrenderinformationcontainer1);
//
//                for(Direction direction1 : Minecraft.getInstance().levelRenderer.DIRECTIONS) {
//                    ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender2 = Minecraft.getInstance().levelRenderer.getRelativeFrom(blockpos1, chunkrenderdispatcher$chunkrender3, direction1);
//                    if ((!flag || !worldrenderer$localrenderinformationcontainer1.hasDirection(direction1.getOpposite())) && (!flag || direction == null || chunkrenderdispatcher$chunkrender3.getCompiledChunk().facesCanSeeEachother(direction.getOpposite(), direction1)) && chunkrenderdispatcher$chunkrender2 != null && chunkrenderdispatcher$chunkrender2.hasAllNeighbors() && chunkrenderdispatcher$chunkrender2.setFrame(p_228437_4_) && p_228437_2_.isVisible(chunkrenderdispatcher$chunkrender2.bb)) {
//                        WorldRenderer.LocalRenderInformationContainer worldrenderer$localrenderinformationcontainer = new WorldRenderer.LocalRenderInformationContainer(chunkrenderdispatcher$chunkrender2, direction1, worldrenderer$localrenderinformationcontainer1.step + 1);
//                        worldrenderer$localrenderinformationcontainer.setDirections(worldrenderer$localrenderinformationcontainer1.directions, direction1);
//                        queue.add(worldrenderer$localrenderinformationcontainer);
//                    }
//                }
//            }
//
//            Minecraft.getInstance().getProfiler().pop();
//        }
//
//        Minecraft.getInstance().getProfiler().popPush("rebuildNear");
//        Set<ChunkRenderDispatcher.ChunkRender> set = Minecraft.getInstance().levelRenderer.chunksToCompile;
//        Minecraft.getInstance().levelRenderer.chunksToCompile = Sets.newLinkedHashSet();
//
////        for(WorldRenderer.WorldRenderer.LocalRenderInformationContainer worldrenderer$localrenderinformationcontainer2 : Minecraft.getInstance().levelRenderer.Minecraft.getInstance().levelRenderer.renderChunks) {
////            ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender4 = worldrenderer$localrenderinformationcontainer2.chunk;
////            if (chunkrenderdispatcher$chunkrender4.isDirty() || set.contains(chunkrenderdispatcher$chunkrender4)) {
////                Minecraft.getInstance().levelRenderer.needsUpdate = true;
////                BlockPos blockpos2 = chunkrenderdispatcher$chunkrender4.getOrigin().offset(8, 8, 8);
////                boolean flag1 = blockpos2.distSqr(blockpos) < 768.0D;
////                if (net.minecraftforge.common.ForgeConfig.CLIENT.alwaysSetupTerrainOffThread.get() || !chunkrenderdispatcher$chunkrender4.isDirtyFromPlayer() && !flag1) {
////                    Minecraft.getInstance().levelRenderer.chunksToCompile.add(chunkrenderdispatcher$chunkrender4);
////                } else {
////                    Minecraft.getInstance().getProfiler().push("build near");
////                    Minecraft.getInstance().levelRenderer.chunkRenderDispatcher.rebuildChunkSync(chunkrenderdispatcher$chunkrender4);
////                    chunkrenderdispatcher$chunkrender4.setNotDirty();
////                    Minecraft.getInstance().getProfiler().pop();
////                }
////            }
////        }
//
//        Minecraft.getInstance().levelRenderer.chunksToCompile.addAll(set);
//        Minecraft.getInstance().getProfiler().pop();
//    }

    public static void renderLevel(MatrixStack p_228426_1_, float p_228426_2_, long p_228426_3_, boolean p_228426_5_, PortalActiveRenderInfo p_228426_6_, GameRenderer p_228426_7_, LightTexture p_228426_8_, Matrix4f p_228426_9_) {
        TileEntityRendererDispatcher.instance.prepare(Minecraft.getInstance().levelRenderer.level, Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().font, p_228426_6_, Minecraft.getInstance().hitResult);
        Minecraft.getInstance().levelRenderer.entityRenderDispatcher.prepare(Minecraft.getInstance().levelRenderer.level, p_228426_6_, Minecraft.getInstance().crosshairPickEntity);
//        IProfiler iprofiler = Minecraft.getInstance().levelRenderer.level.getProfiler();
//        iprofiler.popPush("light_updates");
        Minecraft.getInstance().level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
        Vector3d vector3d = p_228426_6_.getPosition();
        double d0 = vector3d.x();
        double d1 = vector3d.y();
        double d2 = vector3d.z();
        Matrix4f matrix4f = p_228426_1_.last().pose();
//        iprofiler.popPush("culling");
        boolean flag = Minecraft.getInstance().levelRenderer.capturedFrustum != null;
        ClippingHelper clippinghelper;
        if (flag) {
            clippinghelper = Minecraft.getInstance().levelRenderer.capturedFrustum;
            clippinghelper.prepare(Minecraft.getInstance().levelRenderer.frustumPos.x, Minecraft.getInstance().levelRenderer.frustumPos.y, Minecraft.getInstance().levelRenderer.frustumPos.z);
        } else {
            clippinghelper = new ClippingHelper(matrix4f, p_228426_9_);
            clippinghelper.prepare(d0, d1, d2);
        }

//        Minecraft.getInstance().getProfiler().popPush("captureFrustum");
        if (Minecraft.getInstance().levelRenderer.captureFrustum) {
            Minecraft.getInstance().levelRenderer.captureFrustum(matrix4f, p_228426_9_, vector3d.x, vector3d.y, vector3d.z, flag ? new ClippingHelper(matrix4f, p_228426_9_) : clippinghelper);
            Minecraft.getInstance().levelRenderer.captureFrustum = false;
        }

//        iprofiler.popPush("clear");
        FogRenderer.setupColor(p_228426_6_, p_228426_2_, Minecraft.getInstance().level, Minecraft.getInstance().options.renderDistance, p_228426_7_.getDarkenWorldAmount(p_228426_2_));

        if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_B))
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
//        else
//            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        float f = p_228426_7_.getRenderDistance();
        boolean flag1 = Minecraft.getInstance().level.effects().isFoggyAt(MathHelper.floor(d0), MathHelper.floor(d1)) || Minecraft.getInstance().gui.getBossOverlay().shouldCreateWorldFog();
        if (Minecraft.getInstance().options.renderDistance >= 4) {
            FogRenderer.setupFog(p_228426_6_, FogRenderer.FogType.FOG_SKY, f, flag1, p_228426_2_);
//            iprofiler.popPush("sky");
            Minecraft.getInstance().levelRenderer.renderSky(p_228426_1_, p_228426_2_);
        }

//        iprofiler.popPush("fog");
        FogRenderer.setupFog(p_228426_6_, FogRenderer.FogType.FOG_TERRAIN, Math.max(f - 16.0F, 32.0F), flag1, p_228426_2_);
//        iprofiler.popPush("terrain_setup");
        Minecraft.getInstance().levelRenderer.setupRender(p_228426_6_, clippinghelper, flag, Minecraft.getInstance().levelRenderer.frameId, Minecraft.getInstance().player.isSpectator());
//        iprofiler.popPush("updatechunks");
        int i = 30;
        int j = Minecraft.getInstance().options.framerateLimit;
        long k = 33333333L;
        long l;
        if ((double)j == AbstractOption.FRAMERATE_LIMIT.getMaxValue()) {
            l = 0L;
        } else {
            l = (long)(1000000000 / j);
        }

        long i1 = Util.getNanos() - p_228426_3_;
        long j1 = Minecraft.getInstance().levelRenderer.frameTimes.registerValueAndGetMean(i1);
        long k1 = j1 * 3L / 2L;
        long l1 = MathHelper.clamp(k1, l, 33333333L);
        Minecraft.getInstance().levelRenderer.compileChunksUntil(p_228426_3_ + l1);
//        iprofiler.popPush("terrain");
        renderChunkLayer(RenderType.solid(), p_228426_1_, d0, d1, d2);
        Minecraft.getInstance().getModelManager().getAtlas(AtlasTexture.LOCATION_BLOCKS).setBlurMipmap(false, Minecraft.getInstance().options.mipmapLevels > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        renderChunkLayer(RenderType.cutoutMipped(), p_228426_1_, d0, d1, d2);
        Minecraft.getInstance().getModelManager().getAtlas(AtlasTexture.LOCATION_BLOCKS).restoreLastBlurMipmap();
        renderChunkLayer(RenderType.cutout(), p_228426_1_, d0, d1, d2);
        if (Minecraft.getInstance().levelRenderer.level.effects().constantAmbientLight()) {
            RenderHelper.setupNetherLevel(p_228426_1_.last().pose());
        } else {
            RenderHelper.setupLevel(p_228426_1_.last().pose());
        }

//        iprofiler.popPush("entities");
        Minecraft.getInstance().levelRenderer.renderedEntities = 0;
        Minecraft.getInstance().levelRenderer.culledEntities = 0;
        if (Minecraft.getInstance().levelRenderer.itemEntityTarget != null) {
            Minecraft.getInstance().levelRenderer.itemEntityTarget.clear(Minecraft.ON_OSX);
            Minecraft.getInstance().levelRenderer.itemEntityTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

        if (Minecraft.getInstance().levelRenderer.weatherTarget != null) {
            Minecraft.getInstance().levelRenderer.weatherTarget.clear(Minecraft.ON_OSX);
        }

        if (Minecraft.getInstance().levelRenderer.shouldShowEntityOutlines()) {
            Minecraft.getInstance().levelRenderer.entityTarget.clear(Minecraft.ON_OSX);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

        boolean flag2 = false;
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource();

        for(Entity entity : Minecraft.getInstance().levelRenderer.level.entitiesForRendering()) {
            if ((Minecraft.getInstance().levelRenderer.entityRenderDispatcher.shouldRender(entity, clippinghelper, d0, d1, d2) || entity.hasIndirectPassenger(Minecraft.getInstance().player)) && (entity != p_228426_6_.getEntity() || p_228426_6_.isDetached() || p_228426_6_.getEntity() instanceof LivingEntity && ((LivingEntity)p_228426_6_.getEntity()).isSleeping()) && (!(entity instanceof ClientPlayerEntity) || p_228426_6_.getEntity() == entity || (entity == Minecraft.getInstance().player && !Minecraft.getInstance().player.isSpectator()))) { //FORGE: render local player entity when it is not the renderViewEntity
                ++Minecraft.getInstance().levelRenderer.renderedEntities;
                if (entity.tickCount == 0) {
                    entity.xOld = entity.getX();
                    entity.yOld = entity.getY();
                    entity.zOld = entity.getZ();
                }

                IRenderTypeBuffer irendertypebuffer;
                if (Minecraft.getInstance().levelRenderer.shouldShowEntityOutlines() && Minecraft.getInstance().shouldEntityAppearGlowing(entity)) {
                    flag2 = true;
                    OutlineLayerBuffer outlinelayerbuffer = Minecraft.getInstance().levelRenderer.renderBuffers.outlineBufferSource();
                    irendertypebuffer = outlinelayerbuffer;
                    int i2 = entity.getTeamColor();
                    int j2 = 255;
                    int k2 = i2 >> 16 & 255;
                    int l2 = i2 >> 8 & 255;
                    int i3 = i2 & 255;
                    outlinelayerbuffer.setColor(k2, l2, i3, 255);
                } else {
                    irendertypebuffer = irendertypebuffer$impl;
                }

                Minecraft.getInstance().levelRenderer.renderEntity(entity, d0, d1, d2, p_228426_2_, p_228426_1_, irendertypebuffer);
            }
        }

        Minecraft.getInstance().levelRenderer.checkPoseStack(p_228426_1_);
        irendertypebuffer$impl.endBatch(RenderType.entitySolid(AtlasTexture.LOCATION_BLOCKS));
        irendertypebuffer$impl.endBatch(RenderType.entityCutout(AtlasTexture.LOCATION_BLOCKS));
        irendertypebuffer$impl.endBatch(RenderType.entityCutoutNoCull(AtlasTexture.LOCATION_BLOCKS));
        irendertypebuffer$impl.endBatch(RenderType.entitySmoothCutout(AtlasTexture.LOCATION_BLOCKS));
//        iprofiler.popPush("blockentities");

        for(WorldRenderer.LocalRenderInformationContainer worldrenderer$localrenderinformationcontainer : Minecraft.getInstance().levelRenderer.renderChunks) {
            List<TileEntity> list = worldrenderer$localrenderinformationcontainer.chunk.getCompiledChunk().getRenderableBlockEntities();
            if (!list.isEmpty()) {
                for(TileEntity tileentity1 : list) {
                    if(!clippinghelper.isVisible(tileentity1.getRenderBoundingBox())) continue;
                    BlockPos blockpos3 = tileentity1.getBlockPos();
                    IRenderTypeBuffer irendertypebuffer1 = irendertypebuffer$impl;
                    p_228426_1_.pushPose();
                    p_228426_1_.translate((double)blockpos3.getX() - d0, (double)blockpos3.getY() - d1, (double)blockpos3.getZ() - d2);
                    SortedSet<DestroyBlockProgress> sortedset = Minecraft.getInstance().levelRenderer.destructionProgress.get(blockpos3.asLong());
                    if (sortedset != null && !sortedset.isEmpty()) {
                        int j3 = sortedset.last().getProgress();
                        if (j3 >= 0) {
                            MatrixStack.Entry matrixstack$entry = p_228426_1_.last();
                            IVertexBuilder ivertexbuilder = new MatrixApplyingVertexBuilder(Minecraft.getInstance().levelRenderer.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(j3)), matrixstack$entry.pose(), matrixstack$entry.normal());
                            irendertypebuffer1 = (p_230014_2_) -> {
                                IVertexBuilder ivertexbuilder3 = irendertypebuffer$impl.getBuffer(p_230014_2_);
                                return p_230014_2_.affectsCrumbling() ? VertexBuilderUtils.create(ivertexbuilder, ivertexbuilder3) : ivertexbuilder3;
                            };
                        }
                    }

                    TileEntityRendererDispatcher.instance.render(tileentity1, p_228426_2_, p_228426_1_, irendertypebuffer1);
                    p_228426_1_.popPose();
                }
            }
        }

        synchronized(Minecraft.getInstance().levelRenderer.globalBlockEntities) {
            for(TileEntity tileentity : Minecraft.getInstance().levelRenderer.globalBlockEntities) {
                if(!clippinghelper.isVisible(tileentity.getRenderBoundingBox())) continue;
                BlockPos blockpos2 = tileentity.getBlockPos();
                p_228426_1_.pushPose();
                p_228426_1_.translate((double)blockpos2.getX() - d0, (double)blockpos2.getY() - d1, (double)blockpos2.getZ() - d2);
                TileEntityRendererDispatcher.instance.render(tileentity, p_228426_2_, p_228426_1_, irendertypebuffer$impl);
                p_228426_1_.popPose();
            }
        }

        Minecraft.getInstance().levelRenderer.checkPoseStack(p_228426_1_);
        irendertypebuffer$impl.endBatch(RenderType.solid());
        irendertypebuffer$impl.endBatch(Atlases.solidBlockSheet());
        irendertypebuffer$impl.endBatch(Atlases.cutoutBlockSheet());
        irendertypebuffer$impl.endBatch(Atlases.bedSheet());
        irendertypebuffer$impl.endBatch(Atlases.shulkerBoxSheet());
        irendertypebuffer$impl.endBatch(Atlases.signSheet());
        irendertypebuffer$impl.endBatch(Atlases.chestSheet());
        Minecraft.getInstance().levelRenderer.renderBuffers.outlineBufferSource().endOutlineBatch();
        if (flag2) {
            Minecraft.getInstance().levelRenderer.entityEffect.process(p_228426_2_);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }

//        iprofiler.popPush("destroyProgress");

        for(Long2ObjectMap.Entry<SortedSet<DestroyBlockProgress>> entry : Minecraft.getInstance().levelRenderer.destructionProgress.long2ObjectEntrySet()) {
            BlockPos blockpos1 = BlockPos.of(entry.getLongKey());
            double d3 = (double)blockpos1.getX() - d0;
            double d4 = (double)blockpos1.getY() - d1;
            double d5 = (double)blockpos1.getZ() - d2;
            if (!(d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)) {
                SortedSet<DestroyBlockProgress> sortedset1 = entry.getValue();
                if (sortedset1 != null && !sortedset1.isEmpty()) {
                    int k3 = sortedset1.last().getProgress();
                    p_228426_1_.pushPose();
                    p_228426_1_.translate((double)blockpos1.getX() - d0, (double)blockpos1.getY() - d1, (double)blockpos1.getZ() - d2);
                    MatrixStack.Entry matrixstack$entry1 = p_228426_1_.last();
                    IVertexBuilder ivertexbuilder1 = new MatrixApplyingVertexBuilder(Minecraft.getInstance().levelRenderer.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(k3)), matrixstack$entry1.pose(), matrixstack$entry1.normal());
                    Minecraft.getInstance().getBlockRenderer().renderBreakingTexture(Minecraft.getInstance().levelRenderer.level.getBlockState(blockpos1), blockpos1, Minecraft.getInstance().levelRenderer.level, p_228426_1_, ivertexbuilder1);
                    p_228426_1_.popPose();
                }
            }
        }

        Minecraft.getInstance().levelRenderer.checkPoseStack(p_228426_1_);
        RayTraceResult raytraceresult = Minecraft.getInstance().hitResult;
        if (p_228426_5_ && raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
//            iprofiler.popPush("outline");
            BlockPos blockpos = ((BlockRayTraceResult)raytraceresult).getBlockPos();
            BlockState blockstate = Minecraft.getInstance().levelRenderer.level.getBlockState(blockpos);
            if (!net.minecraftforge.client.ForgeHooksClient.onDrawBlockHighlight(Minecraft.getInstance().levelRenderer, p_228426_6_, raytraceresult, p_228426_2_, p_228426_1_, irendertypebuffer$impl))
                if (!blockstate.isAir(Minecraft.getInstance().levelRenderer.level, blockpos) && Minecraft.getInstance().levelRenderer.level.getWorldBorder().isWithinBounds(blockpos)) {
                    IVertexBuilder ivertexbuilder2 = irendertypebuffer$impl.getBuffer(RenderType.lines());
                    Minecraft.getInstance().levelRenderer.renderHitOutline(p_228426_1_, ivertexbuilder2, p_228426_6_.getEntity(), d0, d1, d2, blockpos, blockstate);
                }
        } else if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
            net.minecraftforge.client.ForgeHooksClient.onDrawBlockHighlight(Minecraft.getInstance().levelRenderer, p_228426_6_, raytraceresult, p_228426_2_, p_228426_1_, irendertypebuffer$impl);
        }

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(p_228426_1_.last().pose());
        Minecraft.getInstance().debugRenderer.render(p_228426_1_, irendertypebuffer$impl, d0, d1, d2);
        RenderSystem.popMatrix();
        irendertypebuffer$impl.endBatch(Atlases.translucentCullBlockSheet());
        irendertypebuffer$impl.endBatch(Atlases.bannerSheet());
        irendertypebuffer$impl.endBatch(Atlases.shieldSheet());
        irendertypebuffer$impl.endBatch(RenderType.armorGlint());
        irendertypebuffer$impl.endBatch(RenderType.armorEntityGlint());
        irendertypebuffer$impl.endBatch(RenderType.glint());
        irendertypebuffer$impl.endBatch(RenderType.glintDirect());
        irendertypebuffer$impl.endBatch(RenderType.glintTranslucent());
        irendertypebuffer$impl.endBatch(RenderType.entityGlint());
        irendertypebuffer$impl.endBatch(RenderType.entityGlintDirect());
        irendertypebuffer$impl.endBatch(RenderType.waterMask());
        Minecraft.getInstance().levelRenderer.renderBuffers.crumblingBufferSource().endBatch();
        if (Minecraft.getInstance().levelRenderer.transparencyChain != null) {
            irendertypebuffer$impl.endBatch(RenderType.lines());
            irendertypebuffer$impl.endBatch();
            Minecraft.getInstance().levelRenderer.translucentTarget.clear(Minecraft.ON_OSX);
            Minecraft.getInstance().levelRenderer.translucentTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
//            iprofiler.popPush("translucent");
            renderChunkLayer(RenderType.translucent(), p_228426_1_, d0, d1, d2);
//            iprofiler.popPush("string");
            renderChunkLayer(RenderType.tripwire(), p_228426_1_, d0, d1, d2);
            Minecraft.getInstance().levelRenderer.particlesTarget.clear(Minecraft.ON_OSX);
            Minecraft.getInstance().levelRenderer.particlesTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            RenderState.PARTICLES_TARGET.setupRenderState();
//            iprofiler.popPush("particles");
            Minecraft.getInstance().particleEngine.renderParticles(p_228426_1_, irendertypebuffer$impl, p_228426_8_, p_228426_6_, p_228426_2_, clippinghelper);
            RenderState.PARTICLES_TARGET.clearRenderState();
        } else {
//            iprofiler.popPush("translucent");
            renderChunkLayer(RenderType.translucent(), p_228426_1_, d0, d1, d2);
            irendertypebuffer$impl.endBatch(RenderType.lines());
            irendertypebuffer$impl.endBatch();
//            iprofiler.popPush("string");
            renderChunkLayer(RenderType.tripwire(), p_228426_1_, d0, d1, d2);
//            iprofiler.popPush("particles");
            Minecraft.getInstance().particleEngine.renderParticles(p_228426_1_, irendertypebuffer$impl, p_228426_8_, p_228426_6_, p_228426_2_, clippinghelper);
        }

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(p_228426_1_.last().pose());
        if (Minecraft.getInstance().options.getCloudsType() != CloudOption.OFF) {
            if (Minecraft.getInstance().levelRenderer.transparencyChain != null) {
                Minecraft.getInstance().levelRenderer.cloudsTarget.clear(Minecraft.ON_OSX);
                RenderState.CLOUDS_TARGET.setupRenderState();
//                iprofiler.popPush("clouds");
                Minecraft.getInstance().levelRenderer.renderClouds(p_228426_1_, p_228426_2_, d0, d1, d2);
                RenderState.CLOUDS_TARGET.clearRenderState();
            } else {
//                iprofiler.popPush("clouds");
                Minecraft.getInstance().levelRenderer.renderClouds(p_228426_1_, p_228426_2_, d0, d1, d2);
            }
        }

        if (Minecraft.getInstance().levelRenderer.transparencyChain != null) {
            RenderState.WEATHER_TARGET.setupRenderState();
//            iprofiler.popPush("weather");
            Minecraft.getInstance().levelRenderer.renderSnowAndRain(p_228426_8_, p_228426_2_, d0, d1, d2);
            Minecraft.getInstance().levelRenderer.renderWorldBounds(p_228426_6_);
            RenderState.WEATHER_TARGET.clearRenderState();
            Minecraft.getInstance().levelRenderer.transparencyChain.process(p_228426_2_);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        } else {
            RenderSystem.depthMask(false);
//            iprofiler.popPush("weather");
            Minecraft.getInstance().levelRenderer.renderSnowAndRain(p_228426_8_, p_228426_2_, d0, d1, d2);
            Minecraft.getInstance().levelRenderer.renderWorldBounds(p_228426_6_);
            RenderSystem.depthMask(true);
        }

        renderDebug(p_228426_6_);
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        FogRenderer.setupNoFog();
    }

    public static void renderChunkLayer(RenderType p_228441_1_, MatrixStack p_228441_2_, double p_228441_3_, double p_228441_5_, double p_228441_7_) {
        p_228441_1_.setupRenderState();
        if (p_228441_1_ == RenderType.translucent()) {
//            this.minecraft.getProfiler().push("translucent_sort");
            double d0 = p_228441_3_ - Minecraft.getInstance().levelRenderer.xTransparentOld;
            double d1 = p_228441_5_ - Minecraft.getInstance().levelRenderer.yTransparentOld;
            double d2 = p_228441_7_ - Minecraft.getInstance().levelRenderer.zTransparentOld;
            if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D) {
                Minecraft.getInstance().levelRenderer.xTransparentOld = p_228441_3_;
                Minecraft.getInstance().levelRenderer.yTransparentOld = p_228441_5_;
                Minecraft.getInstance().levelRenderer.zTransparentOld = p_228441_7_;
                int i = 0;

                for(WorldRenderer.LocalRenderInformationContainer worldrenderer$localrenderinformationcontainer : Minecraft.getInstance().levelRenderer.renderChunks) {
                    if (i < 15 && worldrenderer$localrenderinformationcontainer.chunk.resortTransparency(p_228441_1_, Minecraft.getInstance().levelRenderer.chunkRenderDispatcher)) {
                        ++i;
                    }
                }
            }

//            this.minecraft.getProfiler().pop();
        }

//        this.minecraft.getProfiler().push("filterempty");
//        this.minecraft.getProfiler().popPush(() -> {
//            return "render_" + p_228441_1_;
//        });
        boolean flag = p_228441_1_ != RenderType.translucent();
        ObjectListIterator<WorldRenderer.LocalRenderInformationContainer> objectlistiterator = Minecraft.getInstance().levelRenderer.renderChunks.listIterator(flag ? 0 : Minecraft.getInstance().levelRenderer.renderChunks.size());

        while(true) {
            if (flag) {
                if (!objectlistiterator.hasNext()) {
                    break;
                }
            } else if (!objectlistiterator.hasPrevious()) {
                break;
            }

            WorldRenderer.LocalRenderInformationContainer worldrenderer$localrenderinformationcontainer1 = flag ? objectlistiterator.next() : objectlistiterator.previous();
            ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender = worldrenderer$localrenderinformationcontainer1.chunk;
            if (!chunkrenderdispatcher$chunkrender.getCompiledChunk().isEmpty(p_228441_1_)) {
                VertexBuffer vertexbuffer = chunkrenderdispatcher$chunkrender.getBuffer(p_228441_1_);
                p_228441_2_.pushPose();
                BlockPos blockpos = chunkrenderdispatcher$chunkrender.getOrigin();
                p_228441_2_.translate((double)blockpos.getX() - p_228441_3_, (double)blockpos.getY() - p_228441_5_, (double)blockpos.getZ() - p_228441_7_);
                vertexbuffer.bind();
                Minecraft.getInstance().levelRenderer.format.setupBufferState(0L);
                vertexbuffer.draw(p_228441_2_.last().pose(), 7);
                p_228441_2_.popPose();
            }
        }

        VertexBuffer.unbind();
        RenderSystem.clearCurrentColor();
        Minecraft.getInstance().levelRenderer.format.clearBufferState();
//        this.minecraft.getProfiler().pop();
        p_228441_1_.clearRenderState();
    }

    public static void renderDebug(ActiveRenderInfo p_228446_1_) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        if (Minecraft.getInstance().chunkPath || Minecraft.getInstance().chunkVisibility) {
            double d0 = p_228446_1_.getPosition().x();
            double d1 = p_228446_1_.getPosition().y();
            double d2 = p_228446_1_.getPosition().z();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();

            for(WorldRenderer.LocalRenderInformationContainer worldrenderer$localrenderinformationcontainer : Minecraft.getInstance().levelRenderer.renderChunks) {
                ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender = worldrenderer$localrenderinformationcontainer.chunk;
                RenderSystem.pushMatrix();
                BlockPos blockpos = chunkrenderdispatcher$chunkrender.getOrigin();
                RenderSystem.translated((double)blockpos.getX() - d0, (double)blockpos.getY() - d1, (double)blockpos.getZ() - d2);
                if (Minecraft.getInstance().chunkPath) {
                    bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0F);
                    int i = worldrenderer$localrenderinformationcontainer.step == 0 ? 0 : MathHelper.hsvToRgb((float)worldrenderer$localrenderinformationcontainer.step / 50.0F, 0.9F, 0.9F);
                    int j = i >> 16 & 255;
                    int k = i >> 8 & 255;
                    int l = i & 255;
                    Direction direction = worldrenderer$localrenderinformationcontainer.sourceDirection;
                    if (direction != null) {
                        bufferbuilder.vertex(8.0D, 8.0D, 8.0D).color(j, k, l, 255).endVertex();
                        bufferbuilder.vertex((double)(8 - 16 * direction.getStepX()), (double)(8 - 16 * direction.getStepY()), (double)(8 - 16 * direction.getStepZ())).color(j, k, l, 255).endVertex();
                    }

                    tessellator.end();
                    RenderSystem.lineWidth(1.0F);
                }

                if (Minecraft.getInstance().chunkVisibility && !chunkrenderdispatcher$chunkrender.getCompiledChunk().hasNoRenderableLayers()) {
                    bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0F);
                    int i1 = 0;

                    for(Direction direction2 : WorldRenderer.DIRECTIONS) {
                        for(Direction direction1 : WorldRenderer.DIRECTIONS) {
                            boolean flag = chunkrenderdispatcher$chunkrender.getCompiledChunk().facesCanSeeEachother(direction2, direction1);
                            if (!flag) {
                                ++i1;
                                bufferbuilder.vertex((double)(8 + 8 * direction2.getStepX()), (double)(8 + 8 * direction2.getStepY()), (double)(8 + 8 * direction2.getStepZ())).color(1, 0, 0, 1).endVertex();
                                bufferbuilder.vertex((double)(8 + 8 * direction1.getStepX()), (double)(8 + 8 * direction1.getStepY()), (double)(8 + 8 * direction1.getStepZ())).color(1, 0, 0, 1).endVertex();
                            }
                        }
                    }

                    tessellator.end();
                    RenderSystem.lineWidth(1.0F);
                    if (i1 > 0) {
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        float f = 0.5F;
                        float f1 = 0.2F;
                        bufferbuilder.vertex(0.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        tessellator.end();
                    }
                }

                RenderSystem.popMatrix();
            }

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
        }

        if (Minecraft.getInstance().levelRenderer.capturedFrustum != null) {
            RenderSystem.disableCull();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(10.0F);
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)(Minecraft.getInstance().levelRenderer.frustumPos.x - p_228446_1_.getPosition().x), (float)(Minecraft.getInstance().levelRenderer.frustumPos.y - p_228446_1_.getPosition().y), (float)(Minecraft.getInstance().levelRenderer.frustumPos.z - p_228446_1_.getPosition().z));
            RenderSystem.depthMask(true);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            Minecraft.getInstance().levelRenderer.addFrustumQuad(bufferbuilder, 0, 1, 2, 3, 0, 1, 1);
            Minecraft.getInstance().levelRenderer.addFrustumQuad(bufferbuilder, 4, 5, 6, 7, 1, 0, 0);
            Minecraft.getInstance().levelRenderer.addFrustumQuad(bufferbuilder, 0, 1, 5, 4, 1, 1, 0);
            Minecraft.getInstance().levelRenderer.addFrustumQuad(bufferbuilder, 2, 3, 7, 6, 0, 0, 1);
            Minecraft.getInstance().levelRenderer.addFrustumQuad(bufferbuilder, 0, 4, 7, 3, 0, 1, 0);
            Minecraft.getInstance().levelRenderer.addFrustumQuad(bufferbuilder, 1, 5, 6, 2, 1, 0, 1);
            tessellator.end();
            RenderSystem.depthMask(false);
            bufferbuilder.begin(1, DefaultVertexFormats.POSITION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 0);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 1);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 1);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 2);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 2);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 3);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 3);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 0);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 4);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 5);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 5);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 6);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 6);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 7);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 7);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 4);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 0);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 4);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 1);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 5);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 2);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 6);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 3);
            Minecraft.getInstance().levelRenderer.addFrustumVertex(bufferbuilder, 7);
            tessellator.end();
            RenderSystem.popMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.lineWidth(1.0F);
        }

    }
}