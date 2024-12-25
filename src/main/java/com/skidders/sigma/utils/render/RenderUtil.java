package com.skidders.sigma.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.sigma.utils.IMinecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtil implements IMinecraft {

    public static boolean hovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static void drawRectangle(MatrixStack matrixStack, float x, float y, float width, float height, int color) {
        fill(matrixStack.peek().getModel(), x, y, x + width, y + height, color);
    }

    public static void drawRectangle(MatrixStack matrixStack, float x, float y, float width, float height, Color color) {
        fill(matrixStack.peek().getModel(), x, y, x + width, y + height, color.getRGB());
    }

    private static void fill(Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        if (x1 < x2) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float j = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(g, h, j, f).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static final int STEPS = 60;
    private static final double ANGLE =  Math.PI * 2 / STEPS;

    public static void drawCircle(float x, float y, double radius, Color color) {
        drawSetup();
        GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        for(int i = 0; i <= STEPS; i++) {
            GL11.glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        GL11.glEnd();

        GL11.glLineWidth(1.5f);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        for(int i = 0; i <= STEPS; i++) {
            GL11.glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        drawFinish();
    }

    private static void drawSetup() {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA.field_22545, GlStateManager.SrcFactor.ONE_MINUS_SRC_ALPHA.field_22545);
    }

    private static void drawFinish() {
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.clearCurrentColor();
    }

    public static void drawImage(String image, MatrixStack matrices, int x, int y, float width, float height, float u, float v) {
        mc.getTextureManager().bindTexture(new Identifier("sigma-reborn", image));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        DrawableHelper.drawTexture(new MatrixStack(), x, y, 0, 0, (int) width, (int) height, (int) width, (int) height);
        RenderSystem.disableBlend();
    }

}
