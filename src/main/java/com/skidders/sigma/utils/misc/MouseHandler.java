package com.skidders.sigma.utils.misc;

import org.lwjgl.glfw.GLFW;

public class MouseHandler {

    private boolean mouseClicked = false;
    private boolean mouseButtonPressed = false;

    public MouseHandler(long windowHandle) {
        // Set up the mouse button callback
        GLFW.glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) { // Left mouse button
                if (action == GLFW.GLFW_PRESS) { // Button is pressed
                    mouseButtonPressed = true;
                } else if (action == GLFW.GLFW_RELEASE) { // Button is released
                    mouseButtonPressed = false;
                    mouseClicked = false;
                }
            }
        });
    }

    public boolean isMouseClicked() {
        if (mouseButtonPressed && !mouseClicked) {
            mouseClicked = true;
            return true; // Register click
        }
        return false; // No new click detected
    }

}
