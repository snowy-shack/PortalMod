package net.portalmod.common.sorted.trigger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.portalmod.PortalMod;
import net.portalmod.common.items.WrenchItem;
import net.portalmod.core.math.Vec3;
import org.lwjgl.opengl.GL11;

public class TriggerTER extends TileEntityRenderer<TriggerTileEntity> {
    private static final long START_TIME = System.currentTimeMillis();
    public static final ResourceLocation FIELD_TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/block/trigger_field.png");
    private static BufferBuilder triggerBuffer;

    public TriggerTER(TileEntityRendererDispatcher terd) {
        super(terd);
    }

    // todo texture atlas magic

    @Override
    public void render(TriggerTileEntity be, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer, int light, int overlay) {
        if(triggerBuffer == null) {
            triggerBuffer = new BufferBuilder(2097152);
        }

        if(Minecraft.getInstance().player == null || !WrenchItem.holdingWrench(Minecraft.getInstance().player))
            return;

        AxisAlignedBB aabb = null;

        if (TriggerSelectionClient.isSelecting(be)) {
            aabb = TriggerSelectionClient.getBox();
        } else if(be.hasField()) {
            aabb = be.getField();
        }

        if (aabb == null) return;

        if(!triggerBuffer.building()) {
            triggerBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        }

        this.renderTriggerField(aabb.deflate(0.0005), matrixStack);
    }

    private void renderTriggerField(AxisAlignedBB aabb, MatrixStack matrixStack) {
        float x0 = (float)aabb.minX;
        float y0 = (float)aabb.minY;
        float z0 = (float)aabb.minZ;
        float x1 = (float)aabb.maxX;
        float y1 = (float)aabb.maxY;
        float z1 = (float)aabb.maxZ;

        float sizeX = (float)(aabb.getXsize() / Math.round(aabb.getXsize()));
        float sizeY = (float)(aabb.getYsize() / Math.round(aabb.getYsize()));
        float sizeZ = (float)(aabb.getZsize() / Math.round(aabb.getZsize()));

        for(float y = y0; y < y1; y++) {
            for(float z = z0; z < z1; z++) {
                this.renderQuadX(triggerBuffer, matrixStack, new Vec3(x0, y, z), sizeY, sizeZ, false, false);
                this.renderQuadX(triggerBuffer, matrixStack, new Vec3(x0, y, z), sizeY, sizeZ, true, true);
                this.renderQuadX(triggerBuffer, matrixStack, new Vec3(x1, y, z), sizeY, sizeZ, true, false);
                this.renderQuadX(triggerBuffer, matrixStack, new Vec3(x1, y, z), sizeY, sizeZ, false, true);
            }
        }

        for(float z = z0; z < z1; z++) {
            for(float x = x0; x < x1; x++) {
                this.renderQuadY(triggerBuffer, matrixStack, new Vec3(x, y0, z), sizeX, sizeZ, false, false);
                this.renderQuadY(triggerBuffer, matrixStack, new Vec3(x, y0, z), sizeX, sizeZ, true, true);
                this.renderQuadY(triggerBuffer, matrixStack, new Vec3(x, y1, z), sizeX, sizeZ, true, false);
                this.renderQuadY(triggerBuffer, matrixStack, new Vec3(x, y1, z), sizeX, sizeZ, false, true);
            }
        }

        for(float y = y0; y < y1; y++) {
            for(float x = x0; x < x1; x++) {
                this.renderQuadZ(triggerBuffer, matrixStack, new Vec3(x, y, z0), sizeX, sizeY, false, false);
                this.renderQuadZ(triggerBuffer, matrixStack, new Vec3(x, y, z0), sizeX, sizeY, true, true);
                this.renderQuadZ(triggerBuffer, matrixStack, new Vec3(x, y, z1), sizeX, sizeY, true, false);
                this.renderQuadZ(triggerBuffer, matrixStack, new Vec3(x, y, z1), sizeX, sizeY, false, true);
            }
        }
    }

    private float getOffset() {
        return (int)((System.currentTimeMillis() - START_TIME) / 100) / 8.0f % 1.0f;
    }

    private void renderQuadX(BufferBuilder bb, MatrixStack matrixStack, Vec3 origin, float sizeY, float sizeZ, boolean negative, boolean inside) {
        float x0 = (float)origin.x;
        float y0 = (float)origin.y;
        float z0 = (float)origin.z;
        float y1 = y0 + sizeY;
        float z1 = z0 + sizeZ;

        float offset = this.getOffset();
        float u0 = inside ? 1/2f : 0;
        float v0 = offset + 0;
        float u1 = inside ? 1 : 1/2f;
        float v1 = offset + 1/8f;

        bb.vertex(matrixStack.last().pose(), x0, y0, negative ? z1 : z0).uv(u0, v1).endVertex();
        bb.vertex(matrixStack.last().pose(), x0, y0, negative ? z0 : z1).uv(u1, v1).endVertex();
        bb.vertex(matrixStack.last().pose(), x0, y1, negative ? z0 : z1).uv(u1, v0).endVertex();
        bb.vertex(matrixStack.last().pose(), x0, y1, negative ? z1 : z0).uv(u0, v0).endVertex();
    }

    private void renderQuadY(BufferBuilder bb, MatrixStack matrixStack, Vec3 origin, float sizeX, float sizeZ, boolean negative, boolean inside) {
        float x0 = (float)origin.x;
        float y0 = (float)origin.y;
        float z0 = (float)origin.z;
        float x1 = x0 + sizeX;
        float z1 = z0 + sizeZ;

        float offset = this.getOffset();
        float u0 = inside ? 1/2f : 0;
        float v0 = offset + 0;
        float u1 = inside ? 1 : 1/2f;
        float v1 = offset + 1/8f;

        bb.vertex(matrixStack.last().pose(), negative ? x0 : x1, y0, z1).uv(u0, v1).endVertex();
        bb.vertex(matrixStack.last().pose(), negative ? x1 : x0, y0, z1).uv(u1, v1).endVertex();
        bb.vertex(matrixStack.last().pose(), negative ? x1 : x0, y0, z0).uv(u1, v0).endVertex();
        bb.vertex(matrixStack.last().pose(), negative ? x0 : x1, y0, z0).uv(u0, v0).endVertex();
    }

    private void renderQuadZ(BufferBuilder bb, MatrixStack matrixStack, Vec3 origin, float sizeX, float sizeY, boolean negative, boolean inside) {
        float x0 = (float)origin.x;
        float y0 = (float)origin.y;
        float z0 = (float)origin.z;
        float x1 = x0 + sizeX;
        float y1 = y0 + sizeY;

        float offset = this.getOffset();
        float u0 = inside ? 1/2f : 0;
        float v0 = offset + 0;
        float u1 = inside ? 1 : 1/2f;
        float v1 = offset + 1/8f;

        bb.vertex(matrixStack.last().pose(), negative ? x0 : x1, y0, z0).uv(u0, v1).endVertex();
        bb.vertex(matrixStack.last().pose(), negative ? x1 : x0, y0, z0).uv(u1, v1).endVertex();
        bb.vertex(matrixStack.last().pose(), negative ? x1 : x0, y1, z0).uv(u1, v0).endVertex();
        bb.vertex(matrixStack.last().pose(), negative ? x0 : x1, y1, z0).uv(u0, v0).endVertex();
    }

    public static void renderAllTriggers() {
        if(triggerBuffer != null && triggerBuffer.building()) {
            triggerBuffer.sortQuads(0, 0, 0);
            triggerBuffer.end();

            RenderSystem.enableBlend();
            Minecraft.getInstance().textureManager.bind(FIELD_TEXTURE);
            WorldVertexBufferUploader.end(triggerBuffer);
            RenderSystem.disableBlend();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TriggerTileEntity be) {
        return true;
    }
}