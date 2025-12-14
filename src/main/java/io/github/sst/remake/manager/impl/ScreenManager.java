package io.github.sst.remake.manager.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.RunLoopEvent;
import io.github.sst.remake.event.impl.render.Render2DEvent;
import io.github.sst.remake.event.impl.window.WindowResizeEvent;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ScreenManager extends Manager implements IMinecraft {
    public float scaleFactor = 1.0F;
    public int[] mousePositions = new int[2];

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
    public void onRender(Render2DEvent event) {
        RenderSystem.pushMatrix();

        double localScaleFactor = client.getWindow().getScaleFactor() / (double) ((float) Math.pow(client.getWindow().getScaleFactor(), 2.0));
        GL11.glScaled(localScaleFactor, localScaleFactor, 1.0);
        GL11.glScaled(this.scaleFactor, this.scaleFactor, 1.0);
        RenderSystem.disableDepthTest();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0F, 0.0F, 1000.0F);
        //Client.getInstance().guiManager.renderWatermark();
        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F);

        RenderSystem.popMatrix();
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
