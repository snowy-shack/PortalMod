package net.portalmod.common.sorted.cube.companion;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.portalmod.PortalMod;
import net.portalmod.common.sorted.cube.Cube;
import net.portalmod.common.sorted.cube.CubeModel;

public class CompanionCubeGlowLayer<T extends Cube> extends LayerRenderer<T, CubeModel<T>> {
    private static final RenderType COMPANION_CUBE_GLOW = RenderType.entityCutout(new ResourceLocation(PortalMod.MODID, "textures/entity/cube/companion_cube_glow.png"));
    private static final RenderType COMPANION_CUBE_ACTIVE_GLOW = RenderType.entityCutout(new ResourceLocation(PortalMod.MODID, "textures/entity/cube/companion_cube_active_glow.png"));

    public CompanionCubeGlowLayer(IEntityRenderer<T, CubeModel<T>> p_i50939_1_) {
        super(p_i50939_1_);
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int i, T cube, float f1, float f2, float f3, float f4, float f5, float f6) {
        IVertexBuilder ivertexbuilder = renderTypeBuffer.getBuffer(cube.isActive() ? COMPANION_CUBE_ACTIVE_GLOW : COMPANION_CUBE_GLOW);
        this.getParentModel().renderToBuffer(matrixStack, ivertexbuilder, LightTexture.pack(
                Math.max(LightTexture.block(i), 9),
                Math.max(LightTexture.sky(i), 9)
        ), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}