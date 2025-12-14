package io.github.sst.remake.manager.impl;

import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.RunLoopEvent;
import io.github.sst.remake.event.impl.window.WindowResizeEvent;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import org.lwjgl.glfw.GLFW;

public class ScreenManager extends Manager implements IMinecraft {
    public int[] mousePositions = new int[2];
    public float scaleFactor = 1.0F;

    @Override
    public void init() {
        GLFW.glfwSetCursor(client.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR));
        scaleFactor = (float) (client.getWindow().getFramebufferHeight() / client.getWindow().getHeight());
    }

    @Subscribe
    public void onRunLoop(RunLoopEvent event) {
        if (!event.pre) {
            this.mousePositions[0] = Math.max(0, Math.min(client.getWindow().getWidth(), (int) client.mouse.getX()));
            this.mousePositions[1] = Math.max(0, Math.min(client.getWindow().getHeight(), (int) client.mouse.getY()));
        }
    }

    @Subscribe
    public void onWindow(WindowResizeEvent event) {
        if (client.getWindow().getWidth() != 0 && client.getWindow().getHeight() != 0) {
            scaleFactor = (float) Math.max(
                    client.getWindow().getFramebufferWidth() / client.getWindow().getWidth(),
                    client.getWindow().getFramebufferHeight() / client.getWindow().getHeight()
            );
        }
    }
}
