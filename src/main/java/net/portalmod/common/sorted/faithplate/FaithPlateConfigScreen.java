package net.portalmod.common.sorted.faithplate;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.portalmod.PortalMod;
import net.portalmod.core.init.PacketInit;
import net.portalmod.core.init.ShaderInit;
import net.portalmod.core.math.Mat4;
import net.portalmod.core.math.Vec3;
import net.portalmod.mixins.accessors.MinecraftAccessor;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

public class FaithPlateConfigScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/faithplate.png");
    private static final String WHOLE_FLOAT_REGEX = "[\\d.,+\\-]*";
    private static final Pattern FLOAT_REGEX = Pattern.compile("^0*([+\\-])?(?:0*(\\d+\\.?\\d*).*)?$");
    private static final Pattern DIGIT_REGEX = Pattern.compile("^([+\\-])?((?:\\d\\.|\\d){1,5})?.*$");
    private static final long VRESIZE_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_VRESIZE_CURSOR);
    private static final int imageWidth = 230;
    private static final int imageHeight = 239;
    private int pitch = 18;

    public static final int MAX_HEIGHT = 100;

    private RenderWidget panel;
    private ExtendedButton selector;
    private NumberInputField heightField;
    private FaithplateCheckboxButton enable;

    private BlockPos selected;
    private FaithPlateParabola parabola;

    public FaithPlateConfigScreen(BlockPos selected) {
        super(new TranslationTextComponent("screen." + PortalMod.MODID + ".faithplate"));
        this.selected = selected;
    }

    private int getX() {
        return (width - imageWidth - 100) / 2;
    }

    private int getY() {
        return (height - imageHeight + 55) / 2;
    }

    @Override
    protected void init() {
        FaithPlateTileEntity be = (FaithPlateTileEntity)Minecraft.getInstance().level.getBlockEntity(selected);

        panel = addWidget(new RenderWidget(this, getX() + 10, getY() + 22, 210, 121, new StringTextComponent("Render Panel")));
        selector = addButton(new ExtendedButton(getX() + 7, getY() + 150, 216, 20, new TranslationTextComponent("container.faithplate.select"), button -> {
            FaithPlateTER.selected = selected;
            this.onClose();
        }));

        enable = addButton(new FaithplateCheckboxButton(getX() + 230, getY() + 25 + verticalOffset, 20, 20, new TranslationTextComponent("container.faithplate.enabled"), be.isEnabled()));
        enable.setUnavailable(false); // TODO detect unavailableness

        heightField = addWidget(new NumberInputField(this, font, getX() + 230, getY() + 70 + verticalOffset, 85, 20, new StringTextComponent("Height")));

        float middle = 0;
        float target = 0;

        if (be.getTargetPos() != null && be.getTargetFace() != null) {
            Vec3 normal = new Vec3(be.getTargetFace().getNormal()).mul(.5);
            Vec3 pos = new Vec3(be.getTargetPos()).add(0, -.5, 0).add(normal);

            parabola = new FaithPlateParabola(pos);
            parabola.setHeight(be.getHeight());

            middle = (float)parabola.getMiddlePoint();
            target = (float)parabola.getProjectedTarget().x;
        } else {
            panel.setEnabled(false);
            heightField.setEditable(false);
        }

        this.updateField();

        ShaderInit.FAITHPLATE_GUI.get().bind()
            .setMatrix("modelViewProjection", Mat4.createScale(pitch * 2f / (float) panel.getWidth(), pitch * 2f / (float) panel.getHeight(), 1).toBuffer())
            .unbind();

        ShaderInit.FAITHPLATE_GRID.get().bind()
            .setMatrix("modelViewProjection", Mat4.identity().toBuffer())
            .setInt("res", panel.getWidth(), panel.getHeight())
            .setInt("pitch", pitch)
            .setFloat("middle", middle)
            .setFloat("target", target)
            .setInt("offset", -90, -45)
            .unbind();
    }

    @Override
    public void onClose() {
        FaithPlateTileEntity be = (FaithPlateTileEntity)Minecraft.getInstance().level.getBlockEntity(selected);
        BlockPos pos = be.getTargetPos();
        Direction face = be.getTargetFace();
        CompoundNBT nbt = new CompoundNBT();
        
        if(pos != null && face != null) {
            CompoundNBT target = new CompoundNBT();
            target.putInt("x", pos.getX());
            target.putInt("y", pos.getY());
            target.putInt("z", pos.getZ());
            target.putByte("side", (byte)face.get3DDataValue());
            target.putFloat("height", (float)parabola.getHeight());
            nbt.put("target", target);
        }

        nbt.putBoolean("enabled", enable.selected());
        be.load(nbt);
        PacketInit.INSTANCE.sendToServer(new CFaithPlateUpdatedPacket(be.getBlockPos(), nbt));
        Minecraft.getInstance().setScreen(null);
        this.setCursor(0);
    }
    
    private void updateField() {
        if (!panel.enabled) return;

        String text = "";
        Matcher matcher = DIGIT_REGEX.matcher(parabola.getHeight() + "");
        if(parabola.getHeight() == (int)parabola.getHeight())
            matcher = DIGIT_REGEX.matcher((int)parabola.getHeight() + "");
        
        if(matcher.matches()) {
            text += matcher.group(1) != null ? matcher.group(1) : "";
            text += matcher.group(2) != null ? matcher.group(2) : "";
        } else {
            text = "0";
        }
        heightField.setValue(text);
    }
    
    private void updateParabola() {
        if (!panel.enabled) return;

        double height;
        
        try {
            height = Double.parseDouble(heightField.getValue());
        } catch(NumberFormatException e) {
            height = 0;
        }
        
        parabola.setHeight(height);
    }
    
    private void setCursor(long cursor) {
        GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), cursor);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int action) {
        this.setCursor(0);
        return super.mouseReleased(mouseX, mouseY, action);
    }
    
    @Override
    public void renderBackground(MatrixStack matrixStack, int i) {
        super.renderBackground(matrixStack, i);
        
        RenderSystem.color4f(1, 1, 1, 1);
        this.minecraft.getTextureManager().bind(TEXTURE);

        glEnable(GL_BLEND);
        blit(matrixStack, getX(), getY(), 0, 0, 330, 179, 512, 512);
        glDisable(GL_BLEND);
    }

    int verticalOffset = 75;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        panel.render(matrixStack, mouseX, mouseY, partialTicks);
        selector.render(matrixStack, mouseX, mouseY, partialTicks);
        enable.render(matrixStack, mouseX, mouseY, partialTicks);
        heightField.render(matrixStack, mouseX, mouseY, partialTicks);
        
        FontRenderer fontRenderer = Minecraft.getInstance().font;

        fontRenderer.draw(matrixStack, new TranslationTextComponent("container.faithplate"), getX() + 10, getY() + 7, 0xFFFFFF);
        drawString(matrixStack, fontRenderer, new TranslationTextComponent("container.faithplate.height"), getX() + 230, getY() + 53 + verticalOffset, 16777215 | 0xFF << 24);

        if (!panel.enabled)
            drawCenteredString(matrixStack, fontRenderer, new TranslationTextComponent("container.faithplate.noTarget"), (width - 100) / 2, (getY() + panel.y + panel.getHeight()) / 2, 0xFFFF5555);
    }
    
    private static class RenderWidget extends Widget {
        private final FaithPlateConfigScreen parent;
        private boolean handleClicked = false;
        private boolean enabled = true;
        private Vector2f offset = new Vector2f(0, 0);
        private Vector2f baseOffset = new Vector2f(-90, 45); // TODO replace
        private static Framebuffer frameBuffer;
        private static VertexBuffer vbo;
        
        public RenderWidget(FaithPlateConfigScreen parent, int x, int y, int width, int height, ITextComponent text) {
            super(x, y, width, height, text);
            this.parent = parent;

            MainWindow window = Minecraft.getInstance().getWindow();
            double guiScale = window.getGuiScale();

            if (frameBuffer != null) frameBuffer.destroyBuffers();

            frameBuffer = new Framebuffer(
                    (int)(getWidth() * guiScale),
                    (int)(getHeight() * guiScale),
                    true, Minecraft.ON_OSX);

            if (vbo != null) vbo.close();

            vbo = new VertexBuffer(DefaultVertexFormats.POSITION_TEX);
            float fbx = (float)((parent.width - getWidth() - 100) / 2 * guiScale);
            float fby = (float)((parent.height - (y + getHeight())) * guiScale);
            float fbw = fbx + frameBuffer.width;
            float fbh = fby + frameBuffer.height;

            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
            bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.vertex(fbx, fby, 0).uv(0.0F, 1).endVertex();
            bufferbuilder.vertex(fbw, fby, 0).uv(1, 1).endVertex();
            bufferbuilder.vertex(fbw, fbh, 0).uv(1, 0.0F).endVertex();
            bufferbuilder.vertex(fbx, fbh, 0).uv(0.0F, 0.0F).endVertex();
            bufferbuilder.end();
            vbo.upload(bufferbuilder);
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        // Hours wasted here: 5
        // TODO: Main quest - Eliminate the white rectangle
        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            if (!this.enabled) return;

            double a = 0;
            double b = 0;
            if (parent.parabola != null) {
                a = parent.parabola.getA();
                b = parent.parabola.getB();
            }

            Minecraft mc = Minecraft.getInstance();
            MainWindow window = mc.getWindow();
            int wWidth = window.getWidth();
            int wHeight = window.getHeight();
            double guiScale = window.getGuiScale();

            int fbX = (int) ((parent.width - getWidth() - 100) / 2 * guiScale);
            int fbY = (int) ((parent.height - (y + getHeight())) * guiScale);
            int fbW = (int) (getWidth() * guiScale);
            int fbH = (int) (getHeight() * guiScale);

            int[] prevViewport = new int[4];
            glGetIntegerv(GL_VIEWPORT, prevViewport);

            glViewport(fbX, fbY, fbW, fbH);
//            GlStateManager._color4f(0.0F, 0.0F, 0.0F, 1.0F);

            {
                RenderSystem.activeTexture(GL_TEXTURE0);
                mc.getTextureManager().bind(TEXTURE);

                RenderSystem.enableBlend();
                ShaderInit.FAITHPLATE_GRID.get().bind()
                        .setInt("sprite", 0)
                        .setInt("offset",  (int) offset.x - 90, (int) offset.y - 45)
                        .setInt("guisize", (int) guiScale)
                        .setFloat("a", (float) a)
                        .setFloat("b", (float) b)
                        .setFloat("atlasSize", 512, 512)
                        .setFloat("height", (float) parent.parabola.getHeight());

                glBegin(GL_QUADS);
                glVertex2f(-1, -1);
                glVertex2f( 1, -1);
                glVertex2f( 1,  1);
                glVertex2f(-1,  1);
                glEnd();

                ShaderInit.FAITHPLATE_GRID.get().unbind();
                RenderSystem.disableBlend();
            }

            IProfiler profiler = mc.getProfiler();

            profiler.push("[" + PortalMod.MODID + "] faithplate screen preview");
            profiler.push("render");

            Framebuffer previousFB = mc.getMainRenderTarget();
            ((MinecraftAccessor) mc).pmSetMainRenderTarget(frameBuffer);
            frameBuffer.bindWrite(true);

            profiler.popPush("blit");

            ((MinecraftAccessor) mc).pmSetMainRenderTarget(previousFB);
            previousFB.bindWrite(true);

            GlStateManager._viewport(fbX, fbY, wWidth, wHeight);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();

            RenderSystem.activeTexture(GL_TEXTURE0);

            ShaderInit.BLIT.get().bind();
            ShaderInit.BLIT.get().setInt("texture", 0);
            ShaderInit.BLIT.get().setMatrix("projection", new Matrix4f(new float[]{
                    2f / frameBuffer.width, 0, 0, -1,
                    0, 2f / frameBuffer.height, 0, -1,
                    0, 0, 1, 0,
                    0, 0, 0, 1
            }));

            glViewport(0, 0, getWidth(), getHeight());

            frameBuffer.bindRead();
            vbo.bind();
            DefaultVertexFormats.POSITION_TEX.setupBufferState(0L);
            RenderSystem.drawArrays(GL_QUADS, 0, 4);
            VertexBuffer.unbind();
            frameBuffer.unbindRead();

            ShaderInit.BLIT.get().unbind();

            profiler.popPush("render state restore");

            RenderHelper.setupFor3DItems();
            glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3]);

            profiler.pop();
            profiler.pop();
        }
        
        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            if (this.enabled)
                return super.clicked(mouseX, mouseY);
            return false;
        }
        
        @Override
        public void onClick(double mouseX, double mouseY) {
            if (!this.enabled) return;

            int offsetX = (int) (baseOffset.x + offset.x) + width  / 2;
            int offsetY = (int) (baseOffset.y - offset.y) + height / 2;
            int x = (int) (mouseX - this.x - offsetX - parent.pitch * parent.parabola.getMiddlePoint());
            int y = (int) (mouseY - this.y - offsetY + parent.pitch * parent.parabola.getHeight());

            handleClicked = Math.sqrt(x * x + y * y) < (float) parent.pitch / 4.0;

            if (handleClicked) parent.setCursor(VRESIZE_CURSOR);
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            if (!this.enabled) return false;

            parent.pitch += amount;
            parent.pitch = MathHelper.clamp(parent.pitch, 5, 50);
            ShaderInit.FAITHPLATE_GRID.get().bind().setInt("pitch", parent.pitch).unbind();
            return true;
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            if (!this.enabled) return;

            if (handleClicked) {
                if (Screen.hasAltDown() || Screen.hasControlDown() || Screen.hasShiftDown()) {
                    parent.parabola.setHeight(-Math.round((mouseY - this.y - baseOffset.y - this.height / 2 + offset.y) / parent.pitch));
                } else {
                    parent.parabola.setHeight(-(mouseY - this.y - baseOffset.y - this.height / 2 + offset.y) / parent.pitch);
                }
            } else {
                offset = new Vector2f(offset.x + (float) deltaX, offset.y - (float)deltaY);
            }
            
            parent.updateField();
        }
    }
    
    private static class NumberInputField extends TextFieldWidget {
        private final FaithPlateConfigScreen parent;
        
        public NumberInputField(FaithPlateConfigScreen parent, FontRenderer font, int x, int y, int width, int height, ITextComponent text) {
            super(font, x, y, width, height, text);
            this.parent = parent;
        }
        
        @Override
        public void deleteChars(int len) {
            if(!parent.panel.enabled)
                return;
            
            super.deleteChars(len);
            if(this.getValue().isEmpty())
                this.setValue("0");
            parent.updateParabola();
        }
        
        @Override
        public void insertText(String text) {
            if(!parent.panel.enabled
                    || !text.matches(WHOLE_FLOAT_REGEX)
                    || this.getValue().replace(".", "").replace("+", "").replace("-", "").length() >= 5)
                return;
            
            final String recovery = this.getValue();
            text = text.replaceAll(",", ".");
            
            if(text.contains(".") && this.getValue().contains("."))
                text = text.replaceAll(".", "");
            super.insertText(text);
            
            Matcher matcher;
            
            matcher = FLOAT_REGEX.matcher(this.getValue());
            if(matcher.matches()) {
                text = "";
                text += matcher.group(1) != null ? matcher.group(1) : "";
                text += matcher.group(2) != null ? matcher.group(2) : "";
            } else {
                text = recovery;
            }
            
            matcher = DIGIT_REGEX.matcher(text);
            if(matcher.matches()) {
                text = "";
                text += matcher.group(1) != null ? matcher.group(1) : "";
                text += matcher.group(2) != null ? matcher.group(2) : "";
            } else {
                text = recovery;
            }
            
            if(text.isEmpty())
                text = "0";
            
            this.setValue(text);
            parent.updateParabola();
        }
        
        @Override
        public boolean isFocused() {
            if(!parent.panel.enabled)
                return false;
            return super.isFocused();
        }
    }
    
//    private static class DropdownMenu extends Widget {
//        private static final ResourceLocation TEXTURE = new ResourceLocation(PortalMod.MODID, "textures/gui/dropdownmenu.png");
//        private static final int WIDTH = 64, HEIGHT = 20;
//        private List<ITextComponent> entries;
//        private int selected;
//        private boolean open;
//
//        public DropdownMenu(int x, int y, ITextComponent text, List<ITextComponent> entries) {
//            this(x, y, text, entries, 0);
//        }
//
//        public DropdownMenu(int x, int y, ITextComponent text, List<ITextComponent> entries, int selected) {
//            super(x, y, WIDTH, HEIGHT, text);
//            this.open = false;
//            this.entries = entries;
//            this.selected = selected;
//        }
//
//        @Override
//        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
//            boolean hovering = mouseX >= x && mouseX <= x + 64 && mouseY >= y && mouseY <= y + 20;
//
//            Minecraft minecraft = Minecraft.getInstance();
//            minecraft.getTextureManager().bind(TEXTURE);
//            RenderSystem.enableDepthTest();
//            FontRenderer fontrenderer = minecraft.font;
//            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
//            RenderSystem.enableBlend();
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//
//            blit(matrixStack, x, y, 32, this.isFocused() ? 20 : 0, 64, 20, 256, 256);
//            blit(matrixStack, x + 48, y + 2, open ? 16 : 0, this.isFocused() || hovering ? 16 : 0, 16, 16, 256, 256);
//            drawString(matrixStack, fontrenderer, entries.get(selected), this.x + (this.height - 8) / 2, this.y + (this.height - 8) / 2, 0xFFE0E0E0);
//
//            if(open) {
//                int i = 1;
//                for(ITextComponent entry : entries) {
//                    minecraft.getTextureManager().bind(TEXTURE);
//                    RenderSystem.enableDepthTest();
//                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
//                    RenderSystem.enableBlend();
//                    RenderSystem.defaultBlendFunc();
//                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//
//                    blit(matrixStack, x, y + i * 20, 32, this.isFocused() ? 20 : 0, 64, 20, 256, 256);
//                    drawString(matrixStack, fontrenderer, entry, this.x + (this.height - 8) / 2, this.y + (this.height - 8) / 2 + i++ * 20, 0xFFE0E0E0);
//                }
//            }
//        }
//
//        @Override
//        public void onClick(double mouseX, double mouseY) {
//            super.onClick(mouseX, mouseY);
//            open = !open;
//        }
//    }
}