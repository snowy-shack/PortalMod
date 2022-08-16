package io.github.serialsniper.portalmod.client.render;

import java.util.List;
import java.util.SortedSet;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;

import io.github.serialsniper.portalmod.client.render.entity.PortalEntityRenderer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.AbstractOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.OutlineLayerBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class NewWorldRenderer {
    @SuppressWarnings("deprecation")
    public static void renderLevel(MatrixStack p_228426_1_, float p_228426_2_, long p_228426_3_, boolean p_228426_5_, ActiveRenderInfo p_228426_6_, GameRenderer p_228426_7_, LightTexture p_228426_8_, Matrix4f p_228426_9_) {
//        TileEntityRendererDispatcher.instance.prepare(Minecraft.getInstance().levelRenderer.level, Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().font, p_228426_6_, Minecraft.getInstance().hitResult);
//        Minecraft.getInstance().levelRenderer.entityRenderDispatcher.prepare(Minecraft.getInstance().levelRenderer.level, p_228426_6_, Minecraft.getInstance().crosshairPickEntity);
        IProfiler iprofiler = Minecraft.getInstance().levelRenderer.level.getProfiler();
        iprofiler.popPush("light_updates");
        Minecraft.getInstance().level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
        Vector3d vector3d = p_228426_6_.getPosition();
        double d0 = vector3d.x();
        double d1 = vector3d.y();
        double d2 = vector3d.z();
        Matrix4f matrix4f = p_228426_1_.last().pose();
        iprofiler.popPush("culling");
        boolean flag = Minecraft.getInstance().levelRenderer.capturedFrustum != null;
        ClippingHelper clippinghelper;
        if (flag) {
            clippinghelper = Minecraft.getInstance().levelRenderer.capturedFrustum;
            clippinghelper.prepare(Minecraft.getInstance().levelRenderer.frustumPos.x, Minecraft.getInstance().levelRenderer.frustumPos.y, Minecraft.getInstance().levelRenderer.frustumPos.z);
        } else {
            clippinghelper = new ClippingHelper(matrix4f, p_228426_9_);
            clippinghelper.prepare(d0, d1, d2);
        }

        Minecraft.getInstance().getProfiler().popPush("captureFrustum");
        if (Minecraft.getInstance().levelRenderer.captureFrustum) {
            Minecraft.getInstance().levelRenderer.captureFrustum(matrix4f, p_228426_9_, vector3d.x, vector3d.y, vector3d.z, flag ? new ClippingHelper(matrix4f, p_228426_9_) : clippinghelper);
            Minecraft.getInstance().levelRenderer.captureFrustum = false;
        }

        iprofiler.popPush("clear");
        FogRenderer.setupColor(p_228426_6_, p_228426_2_, Minecraft.getInstance().level, Minecraft.getInstance().options.renderDistance, p_228426_7_.getDarkenWorldAmount(p_228426_2_));
//        RenderSystem.clear(16640, Minecraft.ON_OSX);
        float f = p_228426_7_.getRenderDistance();
        boolean flag1 = Minecraft.getInstance().level.effects().isFoggyAt(MathHelper.floor(d0), MathHelper.floor(d1)) || Minecraft.getInstance().gui.getBossOverlay().shouldCreateWorldFog();
        if (Minecraft.getInstance().options.renderDistance >= 4) {
            FogRenderer.setupFog(p_228426_6_, FogRenderer.FogType.FOG_SKY, f, flag1, p_228426_2_);
            iprofiler.popPush("sky");
            Minecraft.getInstance().levelRenderer.renderSky(p_228426_1_, p_228426_2_);
        }

        iprofiler.popPush("fog");
        FogRenderer.setupFog(p_228426_6_, FogRenderer.FogType.FOG_TERRAIN, Math.max(f - 16.0F, 32.0F), flag1, p_228426_2_);
        iprofiler.popPush("terrain_setup");
        Minecraft.getInstance().levelRenderer.setupRender(p_228426_6_, clippinghelper, flag, Minecraft.getInstance().levelRenderer.frameId++, Minecraft.getInstance().player.isSpectator());
//        setupRender(p_228426_6_, clippinghelper, flag, Minecraft.getInstance().levelRenderer.frameId++, Minecraft.getInstance().player.isSpectator());
        iprofiler.popPush("updatechunks");
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
        iprofiler.popPush("terrain");
        Minecraft.getInstance().levelRenderer.renderChunkLayer(RenderType.solid(), p_228426_1_, d0, d1, d2);
        Minecraft.getInstance().getModelManager().getAtlas(AtlasTexture.LOCATION_BLOCKS).setBlurMipmap(false, Minecraft.getInstance().options.mipmapLevels > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        Minecraft.getInstance().levelRenderer.renderChunkLayer(RenderType.cutoutMipped(), p_228426_1_, d0, d1, d2);
        Minecraft.getInstance().getModelManager().getAtlas(AtlasTexture.LOCATION_BLOCKS).restoreLastBlurMipmap();
        Minecraft.getInstance().levelRenderer.renderChunkLayer(RenderType.cutout(), p_228426_1_, d0, d1, d2);
        PortalEntityRenderer.renderPortals(p_228426_6_, p_228426_2_);
        if (Minecraft.getInstance().levelRenderer.level.effects().constantAmbientLight()) {
            RenderHelper.setupNetherLevel(p_228426_1_.last().pose());
        } else {
            RenderHelper.setupLevel(p_228426_1_.last().pose());
        }

        iprofiler.popPush("entities");
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
        iprofiler.popPush("blockentities");

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

        iprofiler.popPush("destroyProgress");

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
            iprofiler.popPush("outline");
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
            iprofiler.popPush("translucent");
            Minecraft.getInstance().levelRenderer.renderChunkLayer(RenderType.translucent(), p_228426_1_, d0, d1, d2);
            iprofiler.popPush("string");
            Minecraft.getInstance().levelRenderer.renderChunkLayer(RenderType.tripwire(), p_228426_1_, d0, d1, d2);
            Minecraft.getInstance().levelRenderer.particlesTarget.clear(Minecraft.ON_OSX);
            Minecraft.getInstance().levelRenderer.particlesTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
            RenderState.PARTICLES_TARGET.setupRenderState();
            iprofiler.popPush("particles");
            Minecraft.getInstance().particleEngine.renderParticles(p_228426_1_, irendertypebuffer$impl, p_228426_8_, p_228426_6_, p_228426_2_, clippinghelper);
            RenderState.PARTICLES_TARGET.clearRenderState();
        } else {
            iprofiler.popPush("translucent");
            Minecraft.getInstance().levelRenderer.renderChunkLayer(RenderType.translucent(), p_228426_1_, d0, d1, d2);
            irendertypebuffer$impl.endBatch(RenderType.lines());
            irendertypebuffer$impl.endBatch();
            iprofiler.popPush("string");
            Minecraft.getInstance().levelRenderer.renderChunkLayer(RenderType.tripwire(), p_228426_1_, d0, d1, d2);
            iprofiler.popPush("particles");
            Minecraft.getInstance().particleEngine.renderParticles(p_228426_1_, irendertypebuffer$impl, p_228426_8_, p_228426_6_, p_228426_2_, clippinghelper);
        }

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(p_228426_1_.last().pose());
        if (Minecraft.getInstance().options.getCloudsType() != CloudOption.OFF) {
            if (Minecraft.getInstance().levelRenderer.transparencyChain != null) {
                Minecraft.getInstance().levelRenderer.cloudsTarget.clear(Minecraft.ON_OSX);
                RenderState.CLOUDS_TARGET.setupRenderState();
                iprofiler.popPush("clouds");
                Minecraft.getInstance().levelRenderer.renderClouds(p_228426_1_, p_228426_2_, d0, d1, d2);
                RenderState.CLOUDS_TARGET.clearRenderState();
            } else {
                iprofiler.popPush("clouds");
                Minecraft.getInstance().levelRenderer.renderClouds(p_228426_1_, p_228426_2_, d0, d1, d2);
            }
        }

        if (Minecraft.getInstance().levelRenderer.transparencyChain != null) {
            RenderState.WEATHER_TARGET.setupRenderState();
            iprofiler.popPush("weather");
            Minecraft.getInstance().levelRenderer.renderSnowAndRain(p_228426_8_, p_228426_2_, d0, d1, d2);
            Minecraft.getInstance().levelRenderer.renderWorldBounds(p_228426_6_);
            RenderState.WEATHER_TARGET.clearRenderState();
            Minecraft.getInstance().levelRenderer.transparencyChain.process(p_228426_2_);
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        } else {
            RenderSystem.depthMask(false);
            iprofiler.popPush("weather");
            Minecraft.getInstance().levelRenderer.renderSnowAndRain(p_228426_8_, p_228426_2_, d0, d1, d2);
            Minecraft.getInstance().levelRenderer.renderWorldBounds(p_228426_6_);
            RenderSystem.depthMask(true);
        }

        Minecraft.getInstance().levelRenderer.renderDebug(p_228426_6_);
        RenderSystem.shadeModel(7424);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        FogRenderer.setupNoFog();
    }
}