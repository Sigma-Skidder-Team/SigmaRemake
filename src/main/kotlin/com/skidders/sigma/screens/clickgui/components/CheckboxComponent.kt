package com.skidders.sigma.screens.clickgui.components

import com.skidders.sigma.utils.misc.MouseHandler
import com.skidders.sigma.utils.render.RenderUtil
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

import java.awt.*

class CheckboxComponent {

    fun draw(mouse: MouseHandler, value: Boolean, mouseX: Double, mouseY: Double, x: Float, y: Float): Boolean {
        val hover = RenderUtil.hovered(mouseX, mouseY, x, y, 12F, 12F)
        val mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
        RenderUtil.drawCircle(
            x + 6, y + 6, 5.5,
            if (value) if (mouseDown && hover) Color(36, 149, 229) else Color(41, 166, 255) else if (mouseDown && hover) Color(217, 217, 217) else Color(227, 227, 227))
        if (value) {
            RenderUtil.drawImage("jello/clickgui/check.png", x.toInt(), (y + 1).toInt(), 10F, 10F, 12, 12)
        }

        if (hover && mouse.isMouseClicked()) {
            return !value
        }

        return value
    }

}
