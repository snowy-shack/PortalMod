package net.portalmod.skins;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.ForgeHooksClient;
import net.portalmod.PortalMod;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.init.ShaderInit;
import net.portalmod.core.math.Vec3;
import net.portalmod.core.util.Colour;
import net.portalmod.core.util.VertexRenderer;
import org.lwjgl.opengl.GL20;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public class ColorPickerWidget extends Widget {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/skinselector.png");
    private static final int COLOR_PICKER_U = 218;
    private static final int COLOR_PICKER_V = 177;
    private long showAnimationStart = -1;
    private long hideAnimationStart = -1;

    private static final int SV_PICKER_X         = 4;
    private static final int SV_PICKER_Y         = 4;
    private static final int SV_PICKER_WIDTH     = 49;
    private static final int SV_PICKER_HEIGHT    = 49;
    private static final float SV_PICKER_VAL_MIN = 0.1f;
    private static final float SV_PICKER_VAL_MAX = 0.9f;
    private final VertexRenderer svPickerColorQuad = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);
    private Rectangle svPickerRegion;

    private static final int SV_PICKER_CURSOR_WIDTH        = 8;
    private static final int SV_PICKER_CURSOR_HEIGHT       = 8;
    private static final int SV_PICKER_CURSOR_U            = 294;
    private static final int SV_PICKER_CURSOR_V            = 177;
    private static final int SV_PICKER_CURSOR_FOCUSED_U    = 310;
    private static final int SV_PICKER_CURSOR_COLOR_X      = 2;
    private static final int SV_PICKER_CURSOR_COLOR_Y      = 2;
    private static final int SV_PICKER_CURSOR_COLOR_WIDTH  = 4;
    private static final int SV_PICKER_CURSOR_COLOR_HEIGHT = 4;
    private final VertexRenderer svPickerCursorColorQuad = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);
    private float svPickerCursorX;
    private float svPickerCursorY;
    private boolean svPickerCursorDragging;
    private boolean svPickerCursorHovering;

    private static final int HUE_PICKER_X      = 64;
    private static final int HUE_PICKER_Y      = 4;
    private static final int HUE_PICKER_WIDTH  = 7;
    private static final int HUE_PICKER_HEIGHT = 49;
    private final VertexRenderer huePickerQuad = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);
    private Rectangle huePickerRegion;

    private static final int HUE_PICKER_CURSOR_WIDTH        = 15;
    private static final int HUE_PICKER_CURSOR_HEIGHT       = 8;
    private static final int HUE_PICKER_CURSOR_U            = 294;
    private static final int HUE_PICKER_CURSOR_V            = 186;
    private static final int HUE_PICKER_CURSOR_FOCUSED_U    = 310;
    private static final int HUE_PICKER_CURSOR_COLOR_X      = 2;
    private static final int HUE_PICKER_CURSOR_COLOR_Y      = 2;
    private static final int HUE_PICKER_CURSOR_COLOR_WIDTH  = 11;
    private static final int HUE_PICKER_CURSOR_COLOR_HEIGHT = 3;
    private final VertexRenderer huePickerCursorColorQuad = new VertexRenderer(DefaultVertexFormats.POSITION_TEX, GL_QUADS);
    private float huePickerCursorY;
    private boolean huePickerCursorDragging;
    private boolean huePickerCursorHovering;

    public ColorPickerWidget(int x, int y, int width, int height) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.init();
    }

    private void init() {
        this.svPickerRegion = new Rectangle(this.getSVPickerX(), this.getSVPickerY(), SV_PICKER_WIDTH, SV_PICKER_HEIGHT);
        this.huePickerRegion = new Rectangle(this.getHuePickerX(), this.getHuePickerY(), HUE_PICKER_WIDTH, HUE_PICKER_HEIGHT);

        this.loadTint();
        this.updateSVPickerCursor();
        this.updateHuePickerCursor();

        this.initShaders();
        this.initSVPickerQuad();
        this.initSVPickerCursorQuad();
        this.initHuePickerQuad();
        this.initHuePickerCursorQuad();
    }

    private void loadTint() {
        int tint = PortalModConfigManager.SKIN_TINT.get();
        Vec3 hsv = tint != 0 ? new Colour(tint).getHSV() : new Vec3(0, 0, 1);

        this.setHue((float)hsv.x);
        this.setSaturation((float)hsv.y);
        this.setValue((float)hsv.z);
    }

    private void initShaders() {
        MatrixStack matrixStack = new MatrixStack();
        this.setupProjection(matrixStack);

        ShaderInit.COLOR_PICKER_SV.get()
                .bind()
                .setMatrix("projection", matrixStack.last().pose())
                .setFloat("valMin", SV_PICKER_VAL_MIN)
                .setFloat("valMax", SV_PICKER_VAL_MAX)
                .unbind();

        ShaderInit.COLOR_PICKER_HUE.get()
                .bind()
                .setMatrix("projection", matrixStack.last().pose())
                .setFloat("saturation", .7f)
                .setFloat("value", .9f)
                .unbind();

        ShaderInit.COLOR_PICKER_SOLID.get()
                .bind()
                .setMatrix("projection", matrixStack.last().pose())
                .unbind();
    }

    private void setSolidShaderColor(float h, float s, float v) {
        ShaderInit.COLOR_PICKER_SOLID.get()
                .bind()
                .setFloat("hue", h)
                .setFloat("saturation", s)
                .setFloat("value", v)
                .unbind();
    }

    private void setShadersModelViewMatrix(MatrixStack matrixStack) {
        ShaderInit.COLOR_PICKER_SV.get().bind().setMatrix("modelView", matrixStack.last().pose());
        ShaderInit.COLOR_PICKER_HUE.get().bind().setMatrix("modelView", matrixStack.last().pose());
        ShaderInit.COLOR_PICKER_SOLID.get().bind().setMatrix("modelView", matrixStack.last().pose());
        GL20.glUseProgram(0);
    }

    private void setupProjection(MatrixStack matrixStack) {
        MainWindow mainWindow = Minecraft.getInstance().getWindow();
        matrixStack.scale(1, -1, 1);
        matrixStack.last().pose().multiply(Matrix4f.orthographic(
                (float)(mainWindow.getWidth() / mainWindow.getGuiScale()),
                (float)(mainWindow.getHeight() / mainWindow.getGuiScale()),
                1000,
                ForgeHooksClient.getGuiFarPlane()
        ));
        matrixStack.translate(0, 0, 1000 - ForgeHooksClient.getGuiFarPlane());
    }

    private void initQuad(VertexRenderer quad, Rectangle rect) {
        int x0 = rect.x;
        int y0 = rect.y;
        int x1 = rect.x + rect.width;
        int y1 = rect.y + rect.height;

        quad.reset();
        quad.data(bufferBuilder -> {
            bufferBuilder.vertex(x0, y1, 0).uv(0, 0).endVertex();
            bufferBuilder.vertex(x1, y1, 0).uv(1, 0).endVertex();
            bufferBuilder.vertex(x1, y0, 0).uv(1, 1).endVertex();
            bufferBuilder.vertex(x0, y0, 0).uv(0, 1).endVertex();
        });
    }

    private void initSVPickerQuad() {
        this.initQuad(svPickerColorQuad, this.svPickerRegion);
    }

    private void initSVPickerCursorQuad() {
        this.initQuad(svPickerCursorColorQuad, this.getSVPickerCursorColorRegion());
    }

    private void initHuePickerQuad() {
        this.initQuad(huePickerQuad, this.huePickerRegion);
    }

    private void initHuePickerCursorQuad() {
        this.initQuad(huePickerCursorColorQuad, this.getHuePickerCursorColorRegion());
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.pushPose();
        matrixStack.translate(0, this.computeAnimation(), 0);

        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        blit(matrixStack, this.x, this.y, COLOR_PICKER_U, COLOR_PICKER_V, this.width, this.height, 512, 512);

        this.setShadersModelViewMatrix(matrixStack);
        this.renderSVPicker(matrixStack);
        this.renderHuePicker(matrixStack);
        matrixStack.popPose();
    }

    private void blitRect(MatrixStack matrixStack, Rectangle rect, int u, int v) {
        blit(matrixStack, rect.x, rect.y, u, v, rect.width, rect.height, 512, 512);
    }

    private void renderSVPicker(MatrixStack matrixStack) {
        this.renderSVPickerColor();
        this.renderSVPickerCursor(matrixStack);
    }

    private void renderSVPickerColor() {
        ShaderInit.COLOR_PICKER_SV.get().bind().setFloat("hue", this.getHue());
        svPickerColorQuad.render();
        ShaderInit.COLOR_PICKER_SV.get().unbind();
    }

    private void renderSVPickerCursor(MatrixStack matrixStack) {
        this.blitRect(matrixStack, this.getSVPickerCursorRegion(),
                this.svPickerCursorHovering ? SV_PICKER_CURSOR_FOCUSED_U : SV_PICKER_CURSOR_U, SV_PICKER_CURSOR_V);
        this.renderSVPickerCursorColor();
    }

    private void renderSVPickerCursorColor() {
        this.setSolidShaderColor(this.getHue(), this.getSaturation(), this.getValue());
        ShaderInit.COLOR_PICKER_SOLID.get().bind();
        svPickerCursorColorQuad.render();
        ShaderInit.COLOR_PICKER_SOLID.get().unbind();
    }

    private void renderHuePicker(MatrixStack matrixStack) {
        this.renderHuePickerColor();
        this.renderHuePickerCursor(matrixStack);
    }

    private void renderHuePickerColor() {
        ShaderInit.COLOR_PICKER_HUE.get().bind();
        huePickerQuad.render();
        ShaderInit.COLOR_PICKER_HUE.get().unbind();
    }

    private void renderHuePickerCursor(MatrixStack matrixStack) {
        this.blitRect(matrixStack, this.getHuePickerCursorRegion(),
                this.huePickerCursorHovering ? HUE_PICKER_CURSOR_FOCUSED_U : HUE_PICKER_CURSOR_U, HUE_PICKER_CURSOR_V);
        this.renderHuePickerCursorColor();
    }

    private void renderHuePickerCursorColor() {
        this.setSolidShaderColor(this.getHue(), 1, .9f);
        ShaderInit.COLOR_PICKER_SOLID.get().bind();
        huePickerCursorColorQuad.render();
        ShaderInit.COLOR_PICKER_SOLID.get().unbind();
    }

    public void startShowAnimation() {
        this.hideAnimationStart = -1;
        this.showAnimationStart = System.currentTimeMillis();
    }

    public void startHideAnimation() {
        this.showAnimationStart = -1;
        this.hideAnimationStart = System.currentTimeMillis();
    }

    private float computeAnimation() {
        float hide = this.computeHideAnimation();
        float show = this.computeShowAnimation();
        return this.hideAnimationStart != -1 ? hide : show;
    }

    private float computeShowAnimation() {
        if(this.showAnimationStart == -1)
            return 0;

        float delta = (System.currentTimeMillis() - this.showAnimationStart) / 1000f;

        if(delta > 1) {
            this.showAnimationStart = -1;
            return 0;
        }

        float factor = (float)Math.exp(-10 * delta);
        return -(this.height + 10) * factor;
    }

    private float computeHideAnimation() {
        if(this.hideAnimationStart == -1)
            return 1;

        float delta = (System.currentTimeMillis() - this.hideAnimationStart) / 1000f;

        if(delta > 1) {
            this.hideAnimationStart = -1;
            return 1;
        }

        float factor = 1 - (float)Math.exp(-10 * delta);
        return -(this.height + 10) * factor;
    }

    public boolean isAnimating() {
        this.computeShowAnimation();
        this.computeHideAnimation();

        return this.showAnimationStart != -1 || this.hideAnimationStart != -1;
    }

    public float getHue() {
        return this.huePickerCursorY * 360;
    }

    public float getSaturation() {
        return this.svPickerCursorX;
    }

    public float getValue() {
        return (1 - this.svPickerCursorY) * (SV_PICKER_VAL_MAX - SV_PICKER_VAL_MIN) + SV_PICKER_VAL_MIN;
    }

    public Colour getTint() {
        return Colour.fromHSV(this.getHue(), this.getSaturation(), this.getValue());
    }

    private void setHue(float hue) {
        this.huePickerCursorY = MathHelper.clamp(hue / 360, 0, 1);
    }

    private void setSaturation(float saturation) {
        this.svPickerCursorX = MathHelper.clamp(saturation, 0, 1);
    }

    private int getSVPickerX() {
        return this.x + SV_PICKER_X;
    }

    private int getSVPickerY() {
        return this.y + SV_PICKER_Y;
    }

    private int getSVPickerCursorX() {
        return this.getSVPickerX() + (int)(svPickerCursorX * SV_PICKER_WIDTH) - SV_PICKER_CURSOR_WIDTH / 2;
    }

    private int getSVPickerCursorY() {
        return this.getSVPickerY() + (int)(svPickerCursorY * SV_PICKER_HEIGHT) - SV_PICKER_CURSOR_HEIGHT / 2;
    }

    private int getHuePickerX() {
        return this.x + HUE_PICKER_X;
    }

    private int getHuePickerY() {
        return this.y + HUE_PICKER_Y;
    }

    private int getHuePickerCursorX() {
        return (int)(this.getHuePickerX() + HUE_PICKER_WIDTH / 2f - HUE_PICKER_CURSOR_WIDTH / 2f);
    }

    private int getHuePickerCursorY() {
        return this.getHuePickerY() + (int)(huePickerCursorY * HUE_PICKER_HEIGHT) - HUE_PICKER_CURSOR_HEIGHT / 2;
    }

    private Rectangle getSVPickerRegion() {
        return new Rectangle(
                this.getSVPickerX(),
                this.getSVPickerY(),
                SV_PICKER_WIDTH,
                SV_PICKER_HEIGHT
        );
    }

    private Rectangle getSVPickerCursorRegion() {
        return new Rectangle(
                this.getSVPickerCursorX(),
                this.getSVPickerCursorY(),
                SV_PICKER_CURSOR_WIDTH,
                SV_PICKER_CURSOR_HEIGHT
        );
    }

    private Rectangle getSVPickerCursorColorRegion() {
        return new Rectangle(
                this.getSVPickerCursorX() + SV_PICKER_CURSOR_COLOR_X,
                this.getSVPickerCursorY() + SV_PICKER_CURSOR_COLOR_Y,
                SV_PICKER_CURSOR_COLOR_WIDTH,
                SV_PICKER_CURSOR_COLOR_HEIGHT
        );
    }

    private Rectangle getHuePickerRegion() {
        return new Rectangle(
                this.getHuePickerX(),
                this.getHuePickerY(),
                HUE_PICKER_WIDTH,
                HUE_PICKER_HEIGHT
        );
    }

    private Rectangle getHuePickerCursorRegion() {
        return new Rectangle(
                this.getHuePickerCursorX(),
                this.getHuePickerCursorY(),
                HUE_PICKER_CURSOR_WIDTH,
                HUE_PICKER_CURSOR_HEIGHT
        );
    }

    private Rectangle getHuePickerCursorColorRegion() {
        return new Rectangle(
                this.getHuePickerCursorX() + HUE_PICKER_CURSOR_COLOR_X,
                this.getHuePickerCursorY() + HUE_PICKER_CURSOR_COLOR_Y,
                HUE_PICKER_CURSOR_COLOR_WIDTH,
                HUE_PICKER_CURSOR_COLOR_HEIGHT
        );
    }

    private void setSVPickerCursorPosition(int x, int y) {
        this.svPickerCursorX = (float)(x - this.getSVPickerX()) / SV_PICKER_WIDTH;
        this.svPickerCursorY = (float)(y - this.getSVPickerY()) / SV_PICKER_HEIGHT;
        this.svPickerCursorX = MathHelper.clamp(this.svPickerCursorX, 0, 1);
        this.svPickerCursorY = MathHelper.clamp(this.svPickerCursorY, 0, 1);
        this.updateSVPickerCursor();
    }

    private void updateSVPickerCursor() {
        this.initQuad(svPickerCursorColorQuad, this.getSVPickerCursorColorRegion());
    }

    private void setHuePickerCursorPosition(int y) {
        this.huePickerCursorY = (float)(y - this.getHuePickerY()) / HUE_PICKER_HEIGHT;
        this.huePickerCursorY = MathHelper.clamp(this.huePickerCursorY, 0, 1);
        this.updateHuePickerCursor();
    }

    private void updateHuePickerCursor() {
        this.initQuad(huePickerCursorColorQuad, this.getHuePickerCursorColorRegion());
    }

    private boolean mouseOverSVPicker(double x, double y) {
        return this.getSVPickerRegion().contains(x, y) || this.getSVPickerCursorRegion().contains(x, y);
    }

    private boolean mouseOverHuePicker(double x, double y) {
        return this.getHuePickerRegion().contains(x, y) || this.getHuePickerCursorRegion().contains(x, y);
    }

    private void setValue(float value) {
        value = 1 - (value - SV_PICKER_VAL_MIN) / (SV_PICKER_VAL_MAX - SV_PICKER_VAL_MIN);
        this.svPickerCursorY = MathHelper.clamp(value, 0, 1);
    }

    @Override
    protected boolean clicked(double x, double y) {
        return super.clicked(x, y) && (mouseOverSVPicker(x, y) || mouseOverHuePicker(x, y));
    }

    @Override
    public void onClick(double x, double y) {
        if(this.mouseOverSVPicker(x, y)) {
            this.svPickerCursorDragging = true;
            this.setSVPickerCursorPosition((int)x, (int)y);
        }

        if(this.mouseOverHuePicker(x, y)) {
            this.huePickerCursorDragging = true;
            this.setHuePickerCursorPosition((int)y);
        }
    }

    public void mouseReleasedAnywhere(int button) {
        if(this.isValidClickButton(button)) {
            this.svPickerCursorDragging = false;
            this.huePickerCursorDragging = false;
        }
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);

        this.svPickerCursorHovering = this.getSVPickerCursorRegion().contains(x, y);
        this.huePickerCursorHovering = this.getHuePickerCursorRegion().contains(x, y);

        if(this.svPickerCursorDragging) {
            this.setSVPickerCursorPosition((int)x, (int)y);
        }

        if(this.huePickerCursorDragging) {
            this.setHuePickerCursorPosition((int)y);
        }
    }
}