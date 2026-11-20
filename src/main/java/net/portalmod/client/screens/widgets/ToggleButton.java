package net.portalmod.client.screens.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** An on/off button. Its label shows the current value. */
public class ToggleButton extends Button {
    private boolean value;

    public ToggleButton(int x, int y, int width, int height, Component text, OnPress pressable, boolean value) {
        this(x, y, width, height, text, pressable, value, null);
    }

    public ToggleButton(int x, int y, int width, int height, Component text, OnPress pressable, boolean value, @Nullable Tooltip tooltip) {
        super(x, y, width, height, text, ($1) -> {}, DEFAULT_NARRATION);
        this.value = value;
        if (tooltip != null)
            this.setTooltip(tooltip);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        value = !value;
    }

    @Override
    public Component getMessage() {
        return Component.literal(super.getMessage().getString() + ": "
                + (value ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF"));
    }

    public boolean getValue() {
        return value;
    }
}
