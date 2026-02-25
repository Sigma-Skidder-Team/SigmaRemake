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
        FloatBuffer modelViewMatrix = org.lwjgl.BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix);

        float ndcX = modelViewMatrix.get(0) * x + modelViewMatrix.get(4) * y + modelViewMatrix.get(12);
        float ndcY = modelViewMatrix.get(1) * x + modelViewMatrix.get(5) * y + modelViewMatrix.get(13);
        float clipW = modelViewMatrix.get(3) * x + modelViewMatrix.get(7) * y + modelViewMatrix.get(15);

        ndcX /= clipW;
        ndcY /= clipW;

        float guiScale = getGuiScaleFactor();
        return new float[] {
                (float) Math.round(ndcX * guiScale),
                (float) Math.round(ndcY * guiScale)
        };
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