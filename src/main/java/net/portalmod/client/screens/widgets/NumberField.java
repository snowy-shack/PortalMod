package net.portalmod.client.screens.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class NumberField extends EditBox {
    public NumberField(Font font, int x, int y, int width, int height, Component label) {
        super(font, x, y, width, height, label);
    }
}