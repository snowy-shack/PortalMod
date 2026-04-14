package net.portalmod.common.sorted.trigger;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.portalmod.PortalMod;
import net.portalmod.common.items.WrenchItem;

import java.util.ArrayList;
import java.util.List;

public class TriggerTER extends TileEntityRenderer<TriggerTileEntity> {

    public TriggerTER(TileEntityRendererDispatcher terd) {
        super(terd);
    }

    @Override
    public void render(TriggerTileEntity be, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int light, int overlay) {
        if(Minecraft.getInstance().player == null || !WrenchItem.holdingWrench(Minecraft.getInstance().player))
            return;

        AxisAlignedBB aabb = null;

        if (TriggerSelectionClient.isSelecting(be)) {
            aabb = TriggerSelectionClient.getBox();
        } else if(be.hasField()) {
            aabb = be.getField();
        }

        if (aabb == null) return;

        this.renderTriggerField(be.getTriggerType(), aabb, matrixStack, renderBuffer, LightTexture.pack(15, 0), overlay);
    }

    private static ResourceLocation getFieldTexture(TriggerType type, boolean inside) {
        String path = "block/trigger_field_";

        switch(type) {
            case PLAYER: path += "players"; break;
            case MOB:    path += "mobs";    break;
        }

        path += "_" + (inside ? "inside" : "outside");
        return new ResourceLocation(PortalMod.MODID, path);
    }

    public static List<ResourceLocation> getAllFieldTextures() {
        List<ResourceLocation> textures = new ArrayList<>();

        for(TriggerType type : TriggerType.values()) {
            textures.add(getFieldTexture(type, false));
            textures.add(getFieldTexture(type, true));
        }

        return textures;
    }

    private void renderTriggerField(TriggerType type, AxisAlignedBB aabb, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int light, int overlay) {
        float x0 = (float)aabb.minX;
        float y0 = (float)aabb.minY;
        float z0 = (float)aabb.minZ;
        float x1 = (float)aabb.maxX;
        float y1 = (float)aabb.maxY;
        float z1 = (float)aabb.maxZ;

        ResourceLocation outsideTexture = getFieldTexture(type, false);
        ResourceLocation insideTexture = getFieldTexture(type, true);

        for(float y = y0; y < y1; y++) {
            for(float z = z0; z < z1; z++) {
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x0, y, z, Direction.WEST, outsideTexture, false);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x0 - 1, y, z, Direction.EAST, insideTexture, true);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x1 - 1, y, z, Direction.EAST, outsideTexture, false);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x1, y, z, Direction.WEST, insideTexture, true);
            }
        }

        for(float z = z0; z < z1; z++) {
            for(float x = x0; x < x1; x++) {
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x, y0, z, Direction.DOWN, outsideTexture, false);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x, y0 - 1, z, Direction.UP, insideTexture, true);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x, y1 - 1, z, Direction.UP, outsideTexture, false);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x, y1, z, Direction.DOWN, insideTexture, true);
            }
        }

        for(float y = y0; y < y1; y++) {
            for(float x = x0; x < x1; x++) {
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x, y, z0, Direction.NORTH, outsideTexture, false);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x, y, z0 - 1, Direction.SOUTH, insideTexture, true);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x, y, z1 - 1, Direction.SOUTH, outsideTexture, false);
                this.renderQuad(renderBuffer, matrixStack, light, overlay, x, y, z1, Direction.NORTH, insideTexture, true);
            }
        }
    }

    private void renderQuad(IRenderTypeBuffer renderBuffer, MatrixStack matrixStack, int light, int overlay, float x, float y, float z, Direction face, ResourceLocation texture, boolean inside) {
        matrixStack.pushPose();
        matrixStack.translate(x, y, z);

        final float offset = 0.001f;
        renderBuffer.getBuffer(RenderType.translucentMovingBlock()).putBulkData(matrixStack.last(),
                new TriggerFieldBakedModel().getQuad(face, texture, offset),
                1, 1, 1, light, overlay);

        matrixStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TriggerTileEntity be) {
        return true;
    }
}