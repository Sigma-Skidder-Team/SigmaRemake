package io.github.sst.remake.util.render;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
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

    public static void startScissor(CustomGuiScreen screen) {
        startScissor(screen.getXA(), screen.getYA(), screen.getWidthA() + screen.getXA(), screen.getHeightA() + screen.getYA(), true);
    }

    public static void startScissor(int x, int y, int width, int height, boolean scale) {
        if (!scale) {
            x = (int) ((float) x * getScaleFactor());
            y = (int) ((float) y * getScaleFactor());
            width = (int) ((float) width * getScaleFactor());
            height = (int) ((float) height * getScaleFactor());
        } else {
            float[] var7 = io.github.sst.remake.util.math.BufferUtils.screenCoordinatesToOpenGLCoordinates(x, y);
            x = (int) var7[0];
            y = (int) var7[1];
            float[] var8 = io.github.sst.remake.util.math.BufferUtils.screenCoordinatesToOpenGLCoordinates(width, height);
            width = (int) var8[0];
            height = (int) var8[1];
        }

        if (GL11.glIsEnabled(GL_SCISSOR_TEST)) {
            IntBuffer var17 = BufferUtils.createIntBuffer(16);
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, var17);
            buffer.push(var17);
            int var18 = var17.get(0);
            int var9 = MinecraftClient.getInstance().getWindow().getFramebufferHeight() - var17.get(1) - var17.get(3);
            int var10 = var18 + var17.get(2);
            int var11 = var9 + var17.get(3);
            if (x < var18) {
                x = var18;
            }

            if (y < var9) {
                y = var9;
            }

            if (width > var10) {
                width = var10;
            }

            if (height > var11) {
                height = var11;
            }

            if (y > height) {
                height = y;
            }

            if (x > width) {
                width = x;
            }
        }

        int adjustedY = MinecraftClient.getInstance().getWindow().getFramebufferHeight() - height;
        int width2 = width - x;
        int height2 = height - y;
        GL11.glEnable(GL_SCISSOR_TEST);
        if (width2 >= 0 && height2 >= 0) {
            GL11.glScissor(x, adjustedY, width2, height2);
        }
    }

    public static void restoreScissor() {
        if (buffer.isEmpty()) {
            GL11.glDisable(GL_SCISSOR_TEST);
        } else {
            IntBuffer var2 = buffer.pop();
            GL11.glScissor(var2.get(0), var2.get(1), var2.get(2), var2.get(3));
        }
    }

}
