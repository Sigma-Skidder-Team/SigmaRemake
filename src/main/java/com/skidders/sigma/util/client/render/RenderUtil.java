package com.skidders.sigma.util.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.SigmaReborn;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Stack;

public class RenderUtil implements IMinecraft {

    private static final Stack<IntBuffer> buffer = new Stack<>();

    public static boolean hovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float cornerRadius, int color) {
        drawRoundedRect(x, y + cornerRadius, x + width, y + height - cornerRadius, color);
        drawRoundedRect(x + cornerRadius, y, x + width - cornerRadius, y + cornerRadius, color);
        drawRoundedRect(x + cornerRadius, y + height - cornerRadius, x + width - cornerRadius, y + height, color);
        applyScaledScissor(x, y, x + cornerRadius, y + cornerRadius);
        drawPoint(x + cornerRadius, y + cornerRadius, cornerRadius * 2.0F, color);
        restoreScissor();
        applyScaledScissor(x + width - cornerRadius, y, x + width, y + cornerRadius);
        drawPoint(x - cornerRadius + width, y + cornerRadius, cornerRadius * 2.0F, color);
        restoreScissor();
        applyScaledScissor(x, y + height - cornerRadius, x + cornerRadius, y + height);
        drawPoint(x + cornerRadius, y - cornerRadius + height, cornerRadius * 2.0F, color);
        restoreScissor();
        applyScaledScissor(x + width - cornerRadius, y + height - cornerRadius, x + width, y + height);
        drawPoint(x - cornerRadius + width, y - cornerRadius + height, cornerRadius * 2.0F, color);
        restoreScissor();
    }

    public static void applyScaledScissor(float x, float y, float width, float height) {
        startScissor((int) x, (int) y, (int) width, (int) height, true);
    }

    public static void restoreScissor() {
        if (buffer.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);  // Disable scissor test if buffer is empty
        } else {
            IntBuffer scissorParams = buffer.pop();
            GL11.glScissor(scissorParams.get(0), scissorParams.get(1), scissorParams.get(2), scissorParams.get(3));
        }
    }

    public static void drawPoint(float x, float y, float size, int color) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

        // Extracting the RGBA components from the color integer
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(red, green, blue, alpha);

        GL11.glEnable(2832);  // Enable point smoothing
        GL11.glEnable(3042);  // Enable blending

        GL11.glPointSize(size * SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex2f(x, y);
        GL11.glEnd();

        GL11.glDisable(2832);  // Disable point smoothing
        GL11.glDisable(3042);  // Disable blending

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(float x1, float y1, float x2, float y2, int color) {
        if (x1 < x2) {
            int tempX = (int) x1;
            x1 = x2;
            x2 = (float) tempX;
        }

        if (y1 < y2) {
            int tempY = (int) y1;
            y1 = y2;
            y2 = (float) tempY;
        }

        // Extract RGBA color components from the color integer
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.color4f(red, green, blue, alpha);

        bufferBuilder.begin(7, VertexFormats.POSITION);
        bufferBuilder.vertex((double) x1, (double) y2, 0.0).next();
        bufferBuilder.vertex((double) x2, (double) y2, 0.0).next();
        bufferBuilder.vertex((double) x2, (double) y1, 0.0).next();
        bufferBuilder.vertex((double) x1, (double) y1, 0.0).next();
        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect2(float var0, float var1, float var2, float var3, int var4) {
        drawRoundedRect(var0, var1, var0 + var2, var1 + var3, var4);
    }

    public static void startScissor(float var0, float var1, float var2, float var3) {
        startScissor((int) var0, (int) var1, (int) var0 + (int) var2, (int) var1 + (int) var3, true);
    }

    public static void startScissor(int x, int y, int width, int height, boolean isScaled) {
        if (!isScaled) {
            x = (int) ((float) x * SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor);
            y = (int) ((float) y * SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor);
            width = (int) ((float) width * SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor);
            height = (int) ((float) height * SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor);
        } else {
            float[] scaledPosition1 = getScaledPosition(x, y);
            x = (int) scaledPosition1[0];
            y = (int) scaledPosition1[1];
            float[] scaledPosition2 = getScaledPosition(width, height);
            width = (int) scaledPosition2[0];
            height = (int) scaledPosition2[1];
        }

        if (GL11.glIsEnabled(3089)) {
            IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
            GL11.glGetIntegerv(3088, viewportBuffer);
            buffer.push(viewportBuffer);
            int viewportHeight = viewportBuffer.get(0);
            int viewportY = mc.getWindow().getFramebufferHeight() - viewportBuffer.get(1) - viewportBuffer.get(3);
            int viewportWidth = viewportHeight + viewportBuffer.get(2);
            int viewportMaxHeight = viewportY + viewportBuffer.get(3);

            if (x < viewportHeight) {
                x = viewportHeight;
            }

            if (y < viewportY) {
                y = viewportY;
            }

            if (width > viewportWidth) {
                width = viewportWidth;
            }

            if (height > viewportMaxHeight) {
                height = viewportMaxHeight;
            }

            if (y > height) {
                height = y;
            }

            if (x > width) {
                width = x;
            }
        }

        int scissorY = mc.getWindow().getFramebufferHeight() - height;
        int scissorWidth = width - x;
        int scissorHeight = height - y;

        GL11.glEnable(3089);
        if (scissorWidth >= 0 && scissorHeight >= 0) {
            GL11.glScissor(x, scissorY, scissorWidth, scissorHeight);
        }
    }

    public static float[] getScaledPosition(int x, int y) {
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(2982, matrixBuffer);

        float transformedX = matrixBuffer.get(0) * (float) x + matrixBuffer.get(4) * (float) y + matrixBuffer.get(8) * 0.0F + matrixBuffer.get(12);
        float transformedY = matrixBuffer.get(1) * (float) x + matrixBuffer.get(5) * (float) y + matrixBuffer.get(9) * 0.0F + matrixBuffer.get(13);
        float w = matrixBuffer.get(3) * (float) x + matrixBuffer.get(7) * (float) y + matrixBuffer.get(11) * 0.0F + matrixBuffer.get(15);

        transformedX /= w;
        transformedY /= w;

        return new float[] { (float) Math.round(transformedX * getWindowScaleFactor()), (float) Math.round(transformedY * getWindowScaleFactor()) };
    }

    public static float getWindowScaleFactor() {
        return (float) mc.getWindow().getScaleFactor();
    }

    public static void begin() {

    }
}
