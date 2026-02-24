package io.github.sst.remake.util.math;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class BufferUtils {
    private static float getGuiScaleFactor() {
        return (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
    }

    /**
     * Transforms 2D coordinates using the current OpenGL model view matrix and applies scaling.
     * This method is typically used for converting screen coordinates to scaled OpenGL coordinates.
     *
     * @param x The x-coordinate to transform.
     * @param y The y-coordinate to transform.
     * @return A float array containing two elements:
     * [0] The transformed and scaled x-coordinate.
     * [1] The transformed and scaled y-coordinate.
     */
    public static float[] screenCoordinatesToOpenGLCoordinates(int x, int y) {
        FloatBuffer var4 = org.lwjgl.BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, var4);
        float var5 = var4.get(0) * (float) x + var4.get(4) * (float) y + var4.get(8) * 0.0F + var4.get(12);
        float var6 = var4.get(1) * (float) x + var4.get(5) * (float) y + var4.get(9) * 0.0F + var4.get(13);
        float var7 = var4.get(3) * (float) x + var4.get(7) * (float) y + var4.get(11) * 0.0F + var4.get(15);
        var5 /= var7;
        var6 /= var7;
        return new float[]{(float) Math.round(var5 * getGuiScaleFactor()), (float) Math.round(var6 * getGuiScaleFactor())};
    }

    public static float[] calculateAspectRatioFit(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
        float sourceAspect = sourceWidth / sourceHeight;
        float targetAspect = targetWidth / targetHeight;
        float fittedWidth;
        float fittedHeight;

        if (targetAspect > sourceAspect) {
            fittedWidth = targetWidth;
            fittedHeight = sourceHeight * targetWidth / sourceWidth;
        } else {
            fittedWidth = sourceWidth * targetHeight / sourceHeight;
            fittedHeight = targetHeight;
        }

        float offsetX = (targetWidth - fittedWidth) / 2.0F;
        float offsetY = (targetHeight - fittedHeight) / 2.0F;

        return new float[]{offsetX, offsetY, fittedWidth, fittedHeight};
    }
}