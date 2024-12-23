package com.skidders.sigma.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.sigma.utils.IMinecraft;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import java.awt.*;

public class RenderUtil implements IMinecraft {

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

}
