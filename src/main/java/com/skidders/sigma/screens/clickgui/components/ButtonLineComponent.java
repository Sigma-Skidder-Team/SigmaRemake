package com.skidders.sigma.screens.clickgui.components;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class ButtonLineComponent {

    public final String text;
    public final float x, y;
    private final Color color;

    private final Screen parent;

    public ButtonLineComponent(String text, float x, float y, Color color, Screen parent) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.parent = parent;
    }


    public void draw(MatrixStack matrices, int mouseX, int mouseY) {

    }

    public boolean click(double mouseX, double mouseY, int button) {
        return true;
    }

}
