package io.github.sst.remake.util.render;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

public class ScissorUtils {
    private static final Stack<IntBuffer> buffer = new Stack<>();

    private static float getScaleFactor() {
        return Client.INSTANCE.screenManager.scaleFactor;
    }

    public static void startScissor(GuiComponent screen) {
        startScissor(screen.getX(), screen.getY(), screen.getWidth() + screen.getX(), screen.getHeight() + screen.getY(), true);
    }

    public static void startScissor(float x, float y, float width, float height) {
        startScissor((int) x, (int) y, (int) width, (int) height, true);
    }

    public static void startScissorRect(float x, float y, float width, float height) {
        startScissor((int) x, (int) y, (int) (x + width), (int) (y + height), true);
    }

    public static void startScissorNoGL(int x, int y, int width, int height) {
        startScissor(x, y, width, height, false);
    }

    public static void startScissor(int x, int y, int width, int height, boolean useOpenGLCoordinates) {
        if (!useOpenGLCoordinates) {
            float scaleFactor = getScaleFactor();
            x = (int) (x * scaleFactor);
            y = (int) (y * scaleFactor);
            width = (int) (width * scaleFactor);
            height = (int) (height * scaleFactor);
        } else {
            float[] startCoords = io.github.sst.remake.util.math.BufferUtils.screenCoordinatesToOpenGLCoordinates(x, y);
            x = (int) startCoords[0];
            y = (int) startCoords[1];

            float[] endCoords = io.github.sst.remake.util.math.BufferUtils.screenCoordinatesToOpenGLCoordinates(width, height);
            width = (int) endCoords[0];
            height = (int) endCoords[1];
        }

        if (GL11.glIsEnabled(GL_SCISSOR_TEST)) {
            IntBuffer previousScissor = BufferUtils.createIntBuffer(16);
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, previousScissor);
            buffer.push(previousScissor);

            int prevX = previousScissor.get(0);
            int prevY = MinecraftClient.getInstance().getWindow().getFramebufferHeight()
                    - previousScissor.get(1)
                    - previousScissor.get(3);
            int prevMaxX = prevX + previousScissor.get(2);
            int prevMaxY = prevY + previousScissor.get(3);

            if (x < prevX) {
                x = prevX;
            }
            if (y < prevY) {
                y = prevY;
            }
            if (width > prevMaxX) {
                width = prevMaxX;
            }
            if (height > prevMaxY) {
                height = prevMaxY;
            }
            if (y > height) {
                height = y;
            }
            if (x > width) {
                width = x;
            }
        }

        int scissorY = MinecraftClient.getInstance().getWindow().getFramebufferHeight() - height;
        int scissorWidth = width - x;
        int scissorHeight = height - y;

        GL11.glEnable(GL_SCISSOR_TEST);
        if (scissorWidth >= 0 && scissorHeight >= 0) {
            GL11.glScissor(x, scissorY, scissorWidth, scissorHeight);
        }
    }

    public static void restoreScissor() {
        if (buffer.isEmpty()) {
            GL11.glDisable(GL_SCISSOR_TEST);
        } else {
            IntBuffer buffer = ScissorUtils.buffer.pop();
            GL11.glScissor(buffer.get(0), buffer.get(1), buffer.get(2), buffer.get(3));
        }
    }
}
