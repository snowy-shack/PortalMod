package net.portalmod.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.portalmod.PortalMod;
import net.portalmod.client.screens.widgets.ToggleButton;
import net.portalmod.core.injectors.MainMenuInjector;
import net.portalmod.skins.SkinSelectorScreen;

public class PortalModOptionsScreen extends Screen {
    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CROSSHAIR;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ADHESION_GEL;
    public static final ForgeConfigSpec.ConfigValue<Boolean> TOOLTIPS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CUSTOM_GUN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MENU;
    public static final ForgeConfigSpec.ConfigValue<Integer> RECURSION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> RENDER_SELF;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HIGHLIGHTS;

    private final Screen lastScreen;
    private ToggleButton CROSSHAIR_BUTTON;
    private ToggleButton TOOLTIPS_BUTTON;
    private ToggleButton CUSTOM_GUN_BUTTON;
    private ToggleButton MENU_BUTTON;
    private Slider RECURSION_SLIDER;
    private ToggleButton RENDER_SELF_BUTTON;
    private ToggleButton HIGHLIGHTS_BUTTON;
    private Button SKIN_PAGE_BUTTON;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        CROSSHAIR = builder.define("crosshair", false);
        ADHESION_GEL = builder.define("adhesion_gel", false);
        CUSTOM_GUN = builder.define("custom_gun", true);
        MENU = builder.define("menu", true);
        TOOLTIPS = builder.define("tooltips", true);
        RECURSION = builder.define("recursion", 3);
        RENDER_SELF = builder.define("render_self", true);
        HIGHLIGHTS = builder.define("highlights", true);

        CONFIG = builder.build();
    }
    
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
        final int buttonWidth = 150;
        final int buttonHeight = 20;

        int baseY = 50;

        CROSSHAIR_BUTTON = new ToggleButton(this.width / 2 - buttonWidth - 5, baseY, buttonWidth, buttonHeight,
                new TranslationTextComponent("options." + PortalMod.MODID + ".crosshair"), $1 -> {}, CROSSHAIR.get());

        RECURSION_SLIDER = new Slider(this.width / 2 - buttonWidth - 5, baseY + (buttonHeight + 5), buttonWidth, buttonHeight,
                new TranslationTextComponent("options." + PortalMod.MODID + ".recursion").append(": "), StringTextComponent.EMPTY,
                0, 9, RECURSION.get(), false, true, $1 -> {}) {
            
            @Override
            public void updateSlider() {
                super.updateSlider();
                int range = ((int)this.maxValue - (int)this.minValue);
                this.sliderValue = (double)Math.round(this.sliderValue * range) / range;
            }
        };

        RENDER_SELF_BUTTON = new ToggleButton(this.width / 2 - buttonWidth - 5, baseY + (buttonHeight + 5) * 2, buttonWidth, buttonHeight,
                new TranslationTextComponent("options." + PortalMod.MODID + ".render_self"), $1 -> {}, RENDER_SELF.get());

        HIGHLIGHTS_BUTTON = new ToggleButton(this.width / 2 - buttonWidth - 5, baseY + (buttonHeight + 5) * 3, buttonWidth, buttonHeight,
                new TranslationTextComponent("options." + PortalMod.MODID + ".highlights"), $1 -> {}, HIGHLIGHTS.get());

        TOOLTIPS_BUTTON = new ToggleButton(this.width / 2 + 5, baseY, buttonWidth, buttonHeight,
                new TranslationTextComponent("options." + PortalMod.MODID + ".tooltips"), $1 -> {}, TOOLTIPS.get());

        MENU_BUTTON = new ToggleButton(this.width / 2 + 5, baseY + (buttonHeight + 5), buttonWidth, buttonHeight,
                new TranslationTextComponent("options." + PortalMod.MODID + ".menu"), $1 -> {}, MENU.get());

        // TODO remove
//        CUSTOM_GUN_BUTTON = new ToggleButton(this.width / 2 - buttonWidth - 5, baseY + (buttonHeight + 5) * 2, buttonWidth, buttonHeight,
//                new TranslationTextComponent("options." + PortalMod.MODID + ".custom_gun"), $1 -> {}, CUSTOM_GUN.get());
        
        SKIN_PAGE_BUTTON = new Button(this.width / 2 - buttonWidth - 5, baseY + (buttonHeight + 5) * 4, buttonWidth * 2 + 10, buttonHeight,
                new TranslationTextComponent("options." + PortalMod.MODID + ".skins"), button -> {
                    Minecraft.getInstance().setScreen(new SkinSelectorScreen(this));
                });
        
        this.addButton(CROSSHAIR_BUTTON);
        this.addButton(TOOLTIPS_BUTTON);
        this.addButton(RENDER_SELF_BUTTON);
        this.addButton(HIGHLIGHTS_BUTTON);
//        this.addButton(CUSTOM_GUN_BUTTON);
        this.addButton(MENU_BUTTON);
        this.addButton(RECURSION_SLIDER);
        this.addButton(SKIN_PAGE_BUTTON);

        this.addButton(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, DialogTexts.GUI_DONE, (p_213056_1_) -> {
            close(true);
        }));
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
        boolean prevMenu = MENU.get();
        CROSSHAIR.set(CROSSHAIR_BUTTON.getValue());
        TOOLTIPS.set(TOOLTIPS_BUTTON.getValue());
//        CUSTOM_GUN.set(CUSTOM_GUN_BUTTON.getValue());
        MENU.set(MENU_BUTTON.getValue());
        RECURSION.set((int)Math.round(RECURSION_SLIDER.getValue()));
        RENDER_SELF.set(RENDER_SELF_BUTTON.getValue());
        HIGHLIGHTS.set(HIGHLIGHTS_BUTTON.getValue());
        
        if(prevMenu != MENU.get())
            MainMenuInjector.needsUpdate = true;
        
        this.minecraft.setScreen(goBackElseClose || Minecraft.getInstance().level == null ? lastScreen : null);
    }
}