package com.skidders.sigma.utils.misc;

import org.lwjgl.glfw.GLFW;

public class MouseHandler {

    private final long windowHandle;
    private boolean mouseClicked = false;

    public MouseHandler(long windowHandle) {
        this.windowHandle = windowHandle;
    }

    public boolean isMouseClicked() {
        // Check if the left mouse button is pressed
        boolean mouseButtonPressed = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (mouseButtonPressed && !mouseClicked) {
            mouseClicked = true;
            return true; // Register click
        } else if (!mouseButtonPressed) {
            mouseClicked = false; // Reset click state when button is released
        }

        return false; // No new click detected
    }
}