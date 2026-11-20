package net.portalmod.client.screens.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public class ToggleButton extends Button {
    private boolean value;

    public ToggleButton(int x, int y, int width, int height, Component text, IPressable pressable, boolean value) {
        super(x, y, width, height, text, ($1) -> {});
        this.value = value;
    }

    public ToggleButton(int x, int y, int width, int height, Component text, IPressable pressable, boolean value, ITooltip tooltip) {
        super(x, y, width, height, text, ($1) -> {}, tooltip);
        this.value = value;
    }

    @Override
    public void onClick(double p_230982_1_, double p_230982_3_) {
        super.onClick(p_230982_1_, p_230982_3_);
        value = !value;
    }

    @Override
    public Component getMessage() {
        return new TextComponent(super.getMessage().getString() + ": "
                + (value ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF"));
    }

    public boolean getValue() {
        return value;
    }
}