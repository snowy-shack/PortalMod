package net.portalmod.common.sorted.trigger;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.portalmod.PortalMod;
import net.portalmod.common.items.WrenchItem;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class TriggerTER implements BlockEntityRenderer<TriggerTileEntity> {
    private final TriggerFieldBakedModel triggerFieldBakedModel;

    public TriggerTER(BlockEntityRendererProvider.Context terd) {
        this.triggerFieldBakedModel = new TriggerFieldBakedModel();
    }

    @Override
    public void render(TriggerTileEntity be, float partialTicks, PoseStack matrixStack, MultiBufferSource renderBuffer, int light, int overlay) {
        if(Minecraft.getInstance().player == null || !WrenchItem.holdingWrench(Minecraft.getInstance().player))
            return;

        this.triggerFieldBakedModel.bakeQuadsOnce();

        AABB aabb = null;

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

    private void renderTriggerField(TriggerType type, AABB aabb, PoseStack matrixStack, MultiBufferSource renderBuffer, int light, int overlay) {
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

    private void renderQuad(MultiBufferSource renderBuffer, PoseStack matrixStack, int light, int overlay, float x, float y, float z, Direction face, ResourceLocation texture, boolean inside) {
        matrixStack.pushPose();
        matrixStack.translate(x, y, z);

        renderBuffer.getBuffer(RenderType.translucentMovingBlock()).putBulkData(matrixStack.last(),
                this.triggerFieldBakedModel.getQuad(face, texture),
                1, 1, 1, light, overlay);

        matrixStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TriggerTileEntity be) {
        return true;
    }
}