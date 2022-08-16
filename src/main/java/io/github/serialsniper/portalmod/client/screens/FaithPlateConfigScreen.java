package io.github.serialsniper.portalmod.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.serialsniper.portalmod.PortalMod;
import io.github.serialsniper.portalmod.client.render.PortalShaders;
import io.github.serialsniper.portalmod.client.render.ter.FaithPlateTER;
import io.github.serialsniper.portalmod.common.blockentities.FaithPlateTileEntity;
import io.github.serialsniper.portalmod.core.util.FaithPlateParabola;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class FaithPlateConfigScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/faithplate.png");
    private CheckboxButton enable;
//    private DropdownMenu direction;
    private ExtendedButton selector;
    private RenderWidget panel;
    public static PortalShaders shader;
    public static PortalShaders gridShader;

    BlockPos selected;

    public FaithPlateConfigScreen(BlockPos selected) {
        super(new TranslationTextComponent("screen." + PortalMod.MODID + ".faithplate"));
        this.selected = selected;
    }

    // todo add width and height

    private int getX() {
        return (width - 230) / 2;
    }

    private int getY() {
        return (height - 219) / 2;
    }

    @Override
    protected void init() {
        enable = addButton(new CheckboxButton(getX() + 7, getY() + 165, 20, 20, new StringTextComponent("Enable"), false));
        selector = addButton(new ExtendedButton(getX() + 7, getY() + 135, 216, 20, new StringTextComponent("Select Target"), button -> {
            FaithPlateTER.selected = selected;
            Minecraft.getInstance().setScreen(null);
        }));
        panel = addWidget(new RenderWidget(getX() + 10, getY() + 7, 210, 121, new StringTextComponent("Render Panel")));

        final int pitch = 18;
        final int w = panel.getWidth();
        final int h = panel.getHeight();

        shader.bind();
        shader.uniformMatrix("modelViewProjection", new float[] {
                pitch * 2f / (float)w, 0, 0, 0,
                0, pitch * 2f / (float)h, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        });
        shader.unbind();

        gridShader.bind();
        gridShader.uniformMatrix("modelViewProjection", new float[] {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        });
        gridShader.uniform2i("res", panel.getWidth(), panel.getHeight());
        gridShader.uniform1i("pitch", pitch);

        FaithPlateTileEntity be = (FaithPlateTileEntity)Minecraft.getInstance().level.getBlockEntity(selected);

        float a, b;

        if(be.getTargetPos() != null) {
            Direction hitDirection = be.getTargetSide();
            Vector3i hitNormal = hitDirection.getNormal();
            Vector3d hitNormalDouble = new Vector3d(hitNormal.getX() * .5, hitNormal.getY() * .5, hitNormal.getZ() * .5);

            Vector3i hitPos = be.getTargetPos();
            System.out.println(hitPos);
            Vector3d hitPosDouble = new Vector3d(hitPos.getX(), hitPos.getY(), hitPos.getZ()).add(0, -.5, 0).add(hitNormalDouble);

//            Vector3i thisPos = selected;
//            Vector3d thisPosDouble = new Vector3d(thisPos.getX(), thisPos.getY(), thisPos.getZ()).add(.5, 1, .5);

//            Vector3d offset = hitPosDouble.subtract(thisPosDouble);
//            Vector3d offset = hitPosDouble;

            FaithPlateParabola parabola = new FaithPlateParabola(hitPosDouble, Double.NEGATIVE_INFINITY);
            a = (float)parabola.getA();
            b = (float)parabola.getB();
        } else {
            a = 0;
            b = 0;
        }

        gridShader.uniform1f("a", a);
        gridShader.uniform1f("b", b);
        gridShader.uniform2i("offset", -90, -45);
        gridShader.unbind();
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int i) {
        super.renderBackground(matrixStack, i);

        RenderSystem.color4f(1, 1, 1, 1);
        this.minecraft.getTextureManager().bind(TEXTURE);
        blit(matrixStack, getX(), getY(), 0, 0, 230, 219, 256, 256);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        enable.render(matrixStack, mouseX, mouseY, partialTicks);
        selector.render(matrixStack, mouseX, mouseY, partialTicks);

        MainWindow window = Minecraft.getInstance().getWindow();
        int wWidth = window.getWidth();
        int wHeight = window.getHeight();
        double guiScale = window.getGuiScale();

        glViewport(
            (int)((width - panel.getWidth()) / 2 * guiScale),
            (int)((height - (panel.y + panel.getHeight())) * guiScale),
            (int)(panel.getWidth() * guiScale),
            (int)(panel.getHeight() * guiScale)
        );
        panel.render(matrixStack, mouseX, mouseY, partialTicks);
        glViewport(0, 0, wWidth, wHeight);

        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.font;
        drawCenteredString(matrixStack, fontrenderer, "No target selected", width / 2, getY() + 219 - 20, 0xFFFF5555);
    }

    private static class RenderWidget extends Widget {
        public RenderWidget(int x, int y, int width, int height, ITextComponent text) {
            super(x, y, width, height, text);
        }

        private Vector2f offset = new Vector2f(-90, -45);

        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.enableBlend();

            gridShader.bind();
            gridShader.uniform2i("offset", (int)offset.x, (int)offset.y);
            glBegin(GL_QUADS);
                glVertex2f(-1, -1);
                glVertex2f( 1, -1);
                glVertex2f( 1,  1);
                glVertex2f(-1,  1);
            glEnd();
            gridShader.unbind();

            RenderSystem.disableBlend();
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            offset = new Vector2f(offset.x + (float)deltaX, offset.y - (float)deltaY);
        }
    }

    private static class DropdownMenu extends Widget {
        private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/dropdownmenu.png");
        private static final int WIDTH = 64, HEIGHT = 20;
        private List<ITextComponent> entries;
        private int selected;
        private boolean open;

        public DropdownMenu(int x, int y, ITextComponent text, List<ITextComponent> entries) {
            this(x, y, text, entries, 0);
        }

        public DropdownMenu(int x, int y, ITextComponent text, List<ITextComponent> entries, int selected) {
            super(x, y, WIDTH, HEIGHT, text);
            this.open = false;
            this.entries = entries;
            this.selected = selected;
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            boolean hovering = mouseX >= x && mouseX <= x + 64 && mouseY >= y && mouseY <= y + 20;

            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getTextureManager().bind(TEXTURE);
            RenderSystem.enableDepthTest();
            FontRenderer fontrenderer = minecraft.font;
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            blit(matrixStack, x, y, 32, this.isFocused() ? 20 : 0, 64, 20, 256, 256);
            blit(matrixStack, x + 48, y + 2, open ? 16 : 0, this.isFocused() || hovering ? 16 : 0, 16, 16, 256, 256);
            drawString(matrixStack, fontrenderer, entries.get(selected), this.x + (this.height - 8) / 2, this.y + (this.height - 8) / 2, 0xFFE0E0E0);

            if(open) {
                int i = 1;
                for(ITextComponent entry : entries) {
                    minecraft.getTextureManager().bind(TEXTURE);
                    RenderSystem.enableDepthTest();
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                    blit(matrixStack, x, y + i * 20, 32, this.isFocused() ? 20 : 0, 64, 20, 256, 256);
                    drawString(matrixStack, fontrenderer, entry, this.x + (this.height - 8) / 2, this.y + (this.height - 8) / 2 + i++ * 20, 0xFFE0E0E0);
                }
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            super.onClick(mouseX, mouseY);
            open = !open;
        }
    }
}