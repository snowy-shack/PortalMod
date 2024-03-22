package net.portalmod.client.screens.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;

public class NumberField extends TextFieldWidget {
    public NumberField(FontRenderer font, int x, int y, int width, int height, ITextComponent label) {
        super(font, x, y, width, height, label);
    }
}