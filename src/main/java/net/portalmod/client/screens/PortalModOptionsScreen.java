package net.portalmod.client.screens;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.MutableComponent;

import net.portalmod.PortalMod;
import net.portalmod.client.screens.widgets.ToggleButton;
import net.portalmod.core.config.PortalModConfigManager;
import net.portalmod.core.injectors.MainMenuInjector;
import net.portalmod.common.sorted.portalgun.skins.SkinSelectorScreen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class PortalModOptionsScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen lastScreen;
    private ToggleButton CROSSHAIR_BUTTON;
    private RecursionSlider RECURSION_SLIDER;
    private ToggleButton RENDER_SELF_BUTTON;
    private ToggleButton HIGHLIGHTS_BUTTON;
    private ToggleButton TOOLTIPS_BUTTON;
    private ToggleButton MENU_BUTTON;
    private ToggleButton FUNNELING_BUTTON;
    private ToggleButton SEPARATE_GUN_BUTTON;
    private Button SKIN_PAGE_BUTTON;
    
    public PortalModOptionsScreen(Screen lastScreen) {
        super(Component.translatable("options." + PortalMod.MODID + ".title"));
        this.lastScreen = lastScreen;
    }
    
    public static Button getAccessButton(Screen screen) {
        return Button.builder(Component.translatable("options." + PortalMod.MODID + ".button"), ($1) -> Minecraft.getInstance().setScreen(new PortalModOptionsScreen(screen))).bounds(screen.width / 2 - 75, screen.height / 6 + 144 - 6, 150, 20).build();
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
        HIGHLIGHTS_BUTTON = this.createToggleButton(x, y + stepY, "highlights", PortalModConfigManager.HIGHLIGHTS.get());

        x = this.width / 2 + 5;
        y = baseY;
        TOOLTIPS_BUTTON = this.createToggleButton(x, y, "tooltips", PortalModConfigManager.TOOLTIPS.get());
        MENU_BUTTON = this.createToggleButton(x, y += stepY, "menu", PortalModConfigManager.MENU.get());
        FUNNELING_BUTTON = this.createToggleButton(x, y += stepY, "portal_funneling", PortalModConfigManager.PORTAL_FUNNELING.get());
        SEPARATE_GUN_BUTTON = this.createToggleButton(x, y += stepY, "separate_gun", PortalModConfigManager.SEPARATE_GUN.get());
        SKIN_PAGE_BUTTON = this.createSkinPageButton(x, y + stepY);

        this.addRenderableWidget(CROSSHAIR_BUTTON);
        this.addRenderableWidget(RECURSION_SLIDER);
        this.addRenderableWidget(RENDER_SELF_BUTTON);
        this.addRenderableWidget(HIGHLIGHTS_BUTTON);
        this.addRenderableWidget(TOOLTIPS_BUTTON);
        this.addRenderableWidget(MENU_BUTTON);
        this.addRenderableWidget(FUNNELING_BUTTON);
        this.addRenderableWidget(SEPARATE_GUN_BUTTON);
        this.addRenderableWidget(SKIN_PAGE_BUTTON);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_213056_1_) -> {
            close(true);
        }).bounds(this.width / 2 - 100, this.height / 6 + 168, 200, 20).build());
    }

    private MutableComponent getText(String id) {
        return Component.translatable("options." + PortalMod.MODID + "." + id);
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
                this.getText("recursion").append(": "), PortalModConfigManager.RECURSION.get());
    }

    private Button createSkinPageButton(int x, int y) {
        return Button.builder(this.getText("skins"), b -> {
            this.save();
            Minecraft.getInstance().setScreen(SkinSelectorScreen.getInstance(this));
        }).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int action) {
        RECURSION_SLIDER.mouseReleased(mouseX, mouseY, action);
        return super.mouseReleased(mouseX, mouseY, action);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        renderDirtBackground(graphics);
        graphics.drawCenteredString( this.font, this.title, this.width / 2, 20, 16777215);
        super.render(graphics, mouseX, mouseY, partialTicks);
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
        PortalModConfigManager.RECURSION.set(RECURSION_SLIDER.getIntValue());
        PortalModConfigManager.RENDER_SELF.set(RENDER_SELF_BUTTON.getValue());
        PortalModConfigManager.HIGHLIGHTS.set(HIGHLIGHTS_BUTTON.getValue());
        PortalModConfigManager.TOOLTIPS.set(TOOLTIPS_BUTTON.getValue());
        PortalModConfigManager.MENU.set(MENU_BUTTON.getValue());
        PortalModConfigManager.PORTAL_FUNNELING.set(FUNNELING_BUTTON.getValue());
        PortalModConfigManager.SEPARATE_GUN.set(SEPARATE_GUN_BUTTON.getValue());
    }

    /** Whole-number slider from 0 to 9 (Forge's Slider widget is gone; this uses vanilla's AbstractSliderButton). */
    private static class RecursionSlider extends AbstractSliderButton {
        private static final int MIN = 0;
        private static final int MAX = 9;
        private final Component prefix;

        public RecursionSlider(int x, int y, int width, int height, Component prefix, int currentVal) {
            super(x, y, width, height, Component.empty(), (double) (currentVal - MIN) / (MAX - MIN));
            this.prefix = prefix;
            this.updateMessage();
        }

        public int getIntValue() {
            return MIN + (int) Math.round(this.value * (MAX - MIN));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(prefix.copy().append(String.valueOf(getIntValue())));
        }

        @Override
        protected void applyValue() {
            // Snap to whole steps.
            this.value = (double) (getIntValue() - MIN) / (MAX - MIN);
        }
    }
}
