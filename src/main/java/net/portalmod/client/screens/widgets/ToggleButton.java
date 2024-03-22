package net.portalmod.client.screens.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ToggleButton extends Button {
    private boolean value;

    public ToggleButton(int x, int y, int width, int height, ITextComponent text, IPressable pressable, boolean value) {
        super(x, y, width, height, text, ($1) -> {});
        this.value = value;
    }

    public ToggleButton(int x, int y, int width, int height, ITextComponent text, IPressable pressable, boolean value, ITooltip tooltip) {
        super(x, y, width, height, text, ($1) -> {}, tooltip);
        this.value = value;
    }

    @Override
    public void onClick(double p_230982_1_, double p_230982_3_) {
        super.onClick(p_230982_1_, p_230982_3_);
        value = !value;
    }

    @Override
    public ITextComponent getMessage() {
        return new StringTextComponent(super.getMessage().getString() + ": "
                + (value ? TextFormatting.GREEN + "ON" : TextFormatting.RED + "OFF"));
    }

    public boolean getValue() {
        return value;
    }
}