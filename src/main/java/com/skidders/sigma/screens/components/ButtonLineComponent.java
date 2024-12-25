package com.skidders.sigma.screens.components;

import com.skidders.sigma.utils.render.font.Renderer;
import com.skidders.sigma.utils.render.RenderUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class ButtonLineComponent {

    private final Renderer font;
    public final String text;
    public final float x, y;
    private final Color color;

    private final Screen parent;

    public ButtonLineComponent(String text, float x, float y, Renderer font, Color color, Screen parent) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.font = font;
        this.color = color;
        this.parent = parent;
    }

    public void draw(MatrixStack matrices, int mouseX, int mouseY) {
        boolean hover = RenderUtil.hovered(mouseX, mouseY, x - font.getWidth(text) / 2, y, font.getWidth(text), font.getHeight(text));
        //center text by X
        font.drawString(text, x - font.getWidth(text) / 2, y, color);

        if (hover) {
            RenderUtil.drawRectangle(matrices, x - font.getWidth(text) / 2, y + font.getHeight() + 2, font.getWidth(text), 1, Color.WHITE);
        }
    }

    public boolean click(double mouseX, double mouseY, int button) {
        boolean hover = RenderUtil.hovered(mouseX, mouseY, x - font.getWidth(text) / 2, y, font.getWidth(text), font.getHeight(text));

        return hover && button != -1;
    }

}
