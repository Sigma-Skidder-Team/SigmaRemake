package com.skidders.sigma.screens.components;

import com.skidders.sigma.utils.misc.MouseHandler;
import com.skidders.sigma.utils.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class CheckboxComponent {

    public boolean draw(MouseHandler mouse, boolean value, double mouseX, double mouseY, float x, float y) {
        boolean hover = RenderUtil.hovered(mouseX, mouseY, x, y, 12, 12);
        boolean mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        RenderUtil.drawCircle(x + 6, y + 6, 5.5, value ? mouseDown && hover ? new Color(36, 149, 229) : new Color(41, 166, 255) : mouseDown && hover ? new Color(217, 217, 217) : new Color(227, 227, 227));

        if (hover && mouse.isMouseClicked()) {
            return !value;
        }

        return value;
    }

}
