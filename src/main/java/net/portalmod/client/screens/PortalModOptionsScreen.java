package net.portalmod.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.portalmod.PortalMod;
import net.portalmod.client.screens.widgets.ToggleButton;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.injectors.MainMenuInjector;
import net.portalmod.skins.SkinSelectorScreen;

public class PortalModOptionsScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen lastScreen;
    private ToggleButton CROSSHAIR_BUTTON;
    private ToggleButton TOOLTIPS_BUTTON;
    private ToggleButton MENU_BUTTON;
    private Slider RECURSION_SLIDER;
    private ToggleButton RENDER_SELF_BUTTON;
    private ToggleButton HIGHLIGHTS_BUTTON;
    private Button SKIN_PAGE_BUTTON;
    
    public PortalModOptionsScreen(Screen lastScreen) {
        super(new TranslationTextComponent("options." + PortalMod.MODID + ".title"));
        this.lastScreen = lastScreen;
    }
    
    public static Button getAccessButton(Screen screen) {
        return new Button(screen.width / 2 - 75, screen.height / 6 + 144 - 6, 150, 20,
                new TranslationTextComponent("options." + PortalMod.MODID + ".button"),
                ($1) -> Minecraft.getInstance().setScreen(new PortalModOptionsScreen(screen)));
    }
    
    @Override
    protected void init() {
        int baseY = 50;
        int stepY = BUTTON_HEIGHT + 5;
        int x, y;

        x = this.width / 2 - BUTTON_WIDTH - 5;
        y = baseY;
        CROSSHAIR_BUTTON = this.createToggleButton(x, y, "crosshair", PortalModConfigManager.CROSSHAIR.get());
        RECURSION_SLIDER = this.createRecursionSlider(x, y += stepY);
        RENDER_SELF_BUTTON = this.createToggleButton(x, y += stepY, "render_self", PortalModConfigManager.RENDER_SELF.get());
        HIGHLIGHTS_BUTTON = this.createToggleButton(x, y += stepY, "highlights", PortalModConfigManager.HIGHLIGHTS.get());
        SKIN_PAGE_BUTTON = this.createSkinPageButton(x, y + stepY);

        x = this.width / 2 + 5;
        y = baseY;
        TOOLTIPS_BUTTON = this.createToggleButton(x, y, "tooltips", PortalModConfigManager.TOOLTIPS.get());
        MENU_BUTTON = this.createToggleButton(x, y + stepY, "menu", PortalModConfigManager.MENU.get());

        this.addButton(CROSSHAIR_BUTTON);
        this.addButton(TOOLTIPS_BUTTON);
        this.addButton(RENDER_SELF_BUTTON);
        this.addButton(HIGHLIGHTS_BUTTON);
        this.addButton(MENU_BUTTON);
        this.addButton(RECURSION_SLIDER);
        this.addButton(SKIN_PAGE_BUTTON);

        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, DialogTexts.GUI_DONE, (p_213056_1_) -> {
            close(true);
        }));
    }

    private TranslationTextComponent getText(String id) {
        return new TranslationTextComponent("options." + PortalMod.MODID + "." + id);
    }

    private ToggleButton createToggleButton(int x, int y, String id, boolean value) {
        return new ToggleButton(
                x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                this.getText(id),
                $1 -> {},
                value
        );
    }

    private RecursionSlider createRecursionSlider(int x, int y) {
        return new RecursionSlider(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                this.getText("recursion").append(": "), StringTextComponent.EMPTY,
                0, 9, PortalModConfigManager.RECURSION.get(), false, true, $1 -> {});
    }

    private Button createSkinPageButton(int x, int y) {
        return new Button(x, y, BUTTON_WIDTH * 2 + 10, BUTTON_HEIGHT, this.getText("skins"), b -> {
            this.save();
            Minecraft.getInstance().setScreen(new SkinSelectorScreen(this));
        });
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int action) {
        RECURSION_SLIDER.mouseReleased(mouseX, mouseY, action);
        return super.mouseReleased(mouseX, mouseY, action);
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderDirtBackground(0);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void onClose() {
        close(false);
    }
    
    private void close(boolean goBackElseClose) {
        boolean prevMenu = PortalModConfigManager.MENU.get();

        this.save();
        
        if(prevMenu != PortalModConfigManager.MENU.get())
            MainMenuInjector.needsUpdate = true;
        
        this.minecraft.setScreen(goBackElseClose || Minecraft.getInstance().level == null ? lastScreen : null);
    }

    private void save() {
        PortalModConfigManager.CROSSHAIR.set(CROSSHAIR_BUTTON.getValue());
        PortalModConfigManager.TOOLTIPS.set(TOOLTIPS_BUTTON.getValue());
        PortalModConfigManager.MENU.set(MENU_BUTTON.getValue());
        PortalModConfigManager.RECURSION.set((int)Math.round(RECURSION_SLIDER.getValue()));
        PortalModConfigManager.RENDER_SELF.set(RENDER_SELF_BUTTON.getValue());
        PortalModConfigManager.HIGHLIGHTS.set(HIGHLIGHTS_BUTTON.getValue());
    }

    private static class RecursionSlider extends Slider {
        public RecursionSlider(int x, int y, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler) {
            super(x, y, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler);
        }

        @Override
        public void updateSlider() {
            super.updateSlider();
            int range = ((int)this.maxValue - (int)this.minValue);
            this.sliderValue = (double)Math.round(this.sliderValue * range) / range;
        }
    }
}