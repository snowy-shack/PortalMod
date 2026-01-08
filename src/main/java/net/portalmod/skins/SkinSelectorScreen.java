package net.portalmod.skins;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;
import net.portalmod.PortalMod;
import org.lwjgl.opengl.GL11;

public class SkinSelectorScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/skinselector.png");
    private static final int WIDTH  = 328;
    private static final int HEIGHT = 172;

    private final Screen lastScreen;
    private boolean initialized;

    private static final int SKIN_PREVIEW_X      = 217;
    private static final int SKIN_PREVIEW_Y      = 17;
    private static final int SKIN_PREVIEW_WIDTH  = 100;
    private static final int SKIN_PREVIEW_HEIGHT = 100;
    private SkinPreviewWidget skinPreviewWidget;

    private static final int SKIN_LIST_X       = 8;
    private static final int SKIN_LIST_Y       = 18;
    private static final int SKIN_LIST_WIDTH   = 189;
    private static final int SKIN_LIST_HEIGHT  = 142;
    private static final int SKIN_ENTRY_HEIGHT = 30;
    private final List<PortalGunSkin> skins;
    private final List<SkinEntryWidget> skinEntryList;
    private Rectangle listRegion;

    private static final int SCROLLBAR_THUMB_U             = 336;
    private static final int SCROLLBAR_THUMB_V             = 0;
    private static final int SCROLLBAR_THUMB_EDGE_HEIGHT   = 4;
    private static final int SCROLLBAR_THUMB_CENTER_HEIGHT = 6;
    private static final int SCROLLBAR_THUMB_WIDTH         = 7;
    private float scrollAmount;
    private boolean draggingScrollbar;
    private float draggingScrollbarRelativeY;

    public SkinSelectorScreen(Screen lastScreen) {
        super(new TranslationTextComponent("options." + PortalMod.MODID + ".skins.title"));
        this.lastScreen = lastScreen;
        this.skins = new ArrayList<>();
        this.skinEntryList = new ArrayList<>();
        this.scrollAmount = 0;
    }

    @Override
    protected void init() {
        this.skinPreviewWidget = new SkinPreviewWidget(this.getX() + SKIN_PREVIEW_X, this.getY() + SKIN_PREVIEW_Y,
                SKIN_PREVIEW_WIDTH, SKIN_PREVIEW_HEIGHT);
        this.addWidget(this.skinPreviewWidget);

        this.listRegion = new Rectangle(this.getX() + SKIN_LIST_X, this.getY() + SKIN_LIST_Y,
                SKIN_LIST_WIDTH, SKIN_LIST_HEIGHT);
        this.initSkinList();

        this.initialized = true;
    }

    private void fetchSkinList() {
        HttpGet request = new HttpGet("https://api.portalmod.net/v1/skins");

        String data;
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpEntity response = client.execute(request).getEntity();
            data = IOUtils.toString(response.getContent(), StandardCharsets.UTF_8);
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        this.skins.addAll(new Gson().fromJson(data, PortalGunSkin.Deserializer.class));
    }

    private void initSkinList() {
        if(!this.initialized) {
            this.fetchSkinList();
        }

        TextureManager tm = Minecraft.getInstance().textureManager;
        Texture missingno = tm.getTexture(new ResourceLocation("missingno"));

        this.skinEntryList.clear();

        int i = 0;
        for(PortalGunSkin skin : this.skins) {
            ResourceLocation location = new ResourceLocation(PortalMod.MODID, "gun/" + skin.skin_id);
            Texture texture = tm.getTexture(location);

            if(texture == null || texture == missingno)
                continue;

            SkinEntryWidget widget = new SkinEntryWidget(
                    this.listRegion.x, this.listRegion.y + SKIN_ENTRY_HEIGHT * i++,
                    this.listRegion.width, SKIN_ENTRY_HEIGHT,
                    this, this.skinPreviewWidget, skin
            );
            skinEntryList.add(widget);
            this.addWidget(widget);
        }

        if(!this.skinEntryList.isEmpty()) {
            this.skinEntryList.get(0).setSelected(true, false);
        }
    }

    private int getX() {
        return (this.width - WIDTH) / 2;
    }

    private int getY() {
        return (this.height - HEIGHT) / 2;
    }

    public void selectEntry(SkinEntryWidget entry) {
        this.skinEntryList.forEach(item -> item.setSelected(false, false));
        entry.setSelected(true, true);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int i) {
        super.renderBackground(matrixStack, i);

        this.minecraft.getTextureManager().bind(TEXTURE);
        blit(matrixStack, this.getX(), this.getY(), 0, 0, WIDTH, HEIGHT, 512, 512);
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.font.draw(matrixStack, "Skin Selector", (float)this.getX() + 8, (float)this.getY() + 6, 4210752);
        this.skinPreviewWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderSkinList(matrixStack, mouseX, mouseY, partialTicks);
        this.renderScrollbar(matrixStack);
    }

    private void renderSkinList(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);

        RenderSystem.colorMask(false, false, false, false);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        this.drawRectangle(matrixStack, listRegion);
        RenderSystem.colorMask(true, true, true, true);

        RenderSystem.stencilMask(0);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);

        matrixStack.pushPose();
        matrixStack.translate(0, -this.scrollAmount * (this.getListHeight() - this.listRegion.height), 0);
        this.skinEntryList.forEach(skinEntryWidget -> skinEntryWidget.render(matrixStack, mouseX, mouseY, partialTicks));
        matrixStack.popPose();

        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    private void renderScrollbar(MatrixStack matrixStack) {
        if(this.getListHeight() <= this.listRegion.height)
            return;

        Minecraft.getInstance().getTextureManager().bind(TEXTURE);

        int yOffset = 0;
        int vOffset = 0;

        blitScrollbarThumbPart(matrixStack, yOffset, vOffset, SCROLLBAR_THUMB_EDGE_HEIGHT);
        yOffset += SCROLLBAR_THUMB_EDGE_HEIGHT;
        vOffset += SCROLLBAR_THUMB_EDGE_HEIGHT;

        while(yOffset < this.getScrollbarThumbHeight() - SCROLLBAR_THUMB_EDGE_HEIGHT) {
            blitScrollbarThumbPart(matrixStack, yOffset, vOffset, SCROLLBAR_THUMB_CENTER_HEIGHT);
            yOffset += SCROLLBAR_THUMB_CENTER_HEIGHT;
        }
        vOffset += SCROLLBAR_THUMB_CENTER_HEIGHT;

        blitScrollbarThumbPart(matrixStack, yOffset, vOffset, SCROLLBAR_THUMB_EDGE_HEIGHT);
    }

    private void blitScrollbarThumbPart(MatrixStack matrixStack, int yOffset, int vOffset, int height) {
        Rectangle thumbRegion = this.getScrollbarThumbRegion();
        blit(matrixStack, thumbRegion.x, thumbRegion.y + yOffset,
                SCROLLBAR_THUMB_U, SCROLLBAR_THUMB_V + vOffset,
                SCROLLBAR_THUMB_WIDTH, height, 512, 512);
    }

    private int getScrollbarThumbHeight() {
        int scrollbarHeight = (int)((float)this.listRegion.height / this.getListHeight() * this.getScrollbarRegion().height);
        return (scrollbarHeight - 2 * SCROLLBAR_THUMB_EDGE_HEIGHT) / SCROLLBAR_THUMB_CENTER_HEIGHT * SCROLLBAR_THUMB_CENTER_HEIGHT + 2 * SCROLLBAR_THUMB_EDGE_HEIGHT;
    }

    private int getScrollbarThumbRange() {
        return this.getScrollbarRegion().height - this.getScrollbarThumbHeight();
    }

    private Rectangle getScrollbarRegion() {
        return new Rectangle(
                this.listRegion.x + this.listRegion.width + 1,
                this.listRegion.y + 1,
                SCROLLBAR_THUMB_WIDTH,
                this.listRegion.height - 2
        );
    }

    private Rectangle getScrollbarThumbRegion() {
        Rectangle scrollbarRegion = this.getScrollbarRegion();
        int thumbHeight = this.getScrollbarThumbHeight();

        return new Rectangle(
                scrollbarRegion.x,
                scrollbarRegion.y + (int)(this.scrollAmount * this.getScrollbarThumbRange()),
                SCROLLBAR_THUMB_WIDTH,
                thumbHeight
        );
    }

    private void drawRectangle(MatrixStack matrixStack, Rectangle rectangle) {
        Matrix4f matrix = matrixStack.last().pose();
        int x0 = rectangle.x;
        int y0 = rectangle.y;
        int x1 = rectangle.x + rectangle.width;
        int y1 = rectangle.y + rectangle.height;

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferbuilder.vertex(matrix, x0, y0, 0).endVertex();
        bufferbuilder.vertex(matrix, x0, y1, 0).endVertex();
        bufferbuilder.vertex(matrix, x1, y1, 0).endVertex();
        bufferbuilder.vertex(matrix, x1, y0, 0).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);
    }

    private int getListHeight() {
        return this.skinEntryList.size() * SKIN_ENTRY_HEIGHT;
    }

    @Override
    public void tick() {
        super.tick();
        this.skinPreviewWidget.tick();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if(this.getScrollbarThumbRegion().contains(x, y)) {
            this.draggingScrollbar = true;
            this.draggingScrollbarRelativeY = (float)y - this.getScrollbarThumbRegion().y;
        }

        if(listRegion.contains(x, y))
            return super.mouseClicked(x, y + this.scrollAmount * (this.getListHeight() - this.listRegion.height), button);
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        this.draggingScrollbar = false;
        this.skinPreviewWidget.mouseReleasedAnywhere(button);

        return super.mouseReleased(x, y, button);
    }

    @Override
    public void mouseMoved(double x, double y) {
        if(this.draggingScrollbar) {
            this.scrollAmount = (float)(y - this.getScrollbarRegion().y - this.draggingScrollbarRelativeY) / this.getScrollbarThumbRange();
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0, 1);
        }

        this.skinPreviewWidget.mouseMoved(x,y);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        if(this.listRegion.contains(x, y)) {
            this.scrollAmount -= (float)amount * 20 / this.getListHeight();
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0, 1);
        }

        return super.mouseScrolled(x, y, amount);
    }

    @Override
    public void onClose() {
        close(false);
    }

    private void close(boolean goBackElseClose) {
        this.minecraft.setScreen(goBackElseClose || Minecraft.getInstance().level == null ? lastScreen : null);
    }
}