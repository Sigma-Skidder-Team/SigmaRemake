package com.skidders.sigma.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;

public class ClickGUI extends Screen {

    public ClickGUI(String title) {
        super(Text.of(title));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        fill(matrices, 20, 20, 20 + 120, 20 + 120, Color.WHITE.getRGB());
    }
}
