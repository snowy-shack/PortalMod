package net.portalmod.core.interfaces;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.portalmod.PortalMod;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = PortalMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public interface IParticleRT extends IParticleRenderType {
    net.minecraft.client.particle.IParticleRenderType PARTICLE_SHEET_TRANSLUCENT_LIT = new net.minecraft.client.particle.IParticleRenderType() {
        @Override
        public void begin(BufferBuilder p_217600_1_, TextureManager p_217600_2_) {
            RenderSystem.depthMask(true);
            p_217600_2_.bind(AtlasTexture.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(516, 0.003921569F);
            p_217600_1_.begin(7, DefaultVertexFormats.PARTICLE);
        }
//        public void begin(BufferBuilder p_217600_1_, TextureManager p_217600_2_) {
//            RenderSystem.depthMask(true);
//            p_217600_2_.bind(AtlasTexture.LOCATION_PARTICLES);
//            RenderSystem.enableBlend();
//            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
//                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            RenderSystem.alphaFunc(516, 0.003921569F);
//            p_217600_1_.begin(7, DefaultVertexFormats.PARTICLE);
//        }

        @Override
        public void end(Tessellator p_217599_1_) {
            p_217599_1_.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_TRANSLUCENT_LIT";
        }
    };
}