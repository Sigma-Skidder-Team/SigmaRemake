package io.github.sst.remake.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.porting.StateManager;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.opengl.texture.TextureImpl;
import org.newdawn.slick.util.math.Color;

import java.nio.ByteBuffer;

public class RenderUtils implements IMinecraft {
    private static int dynamicPixelTextureId = -1;
    private static int dynamicPixelTextureWidth = -1;
    private static int dynamicPixelTextureHeight = -1;

    private static float getScaleFactor() {
        return Client.INSTANCE.screenManager.scaleFactor;
    }

    public static void drawRoundedRect(float x, float y, float width, float height, int color) {
        if (x < width) {
            int adjustedWidth = (int) x;
            x = width;
            width = (float) adjustedWidth;
        }

        if (y < height) {
            int adjustedHeight = (int) y;
            y = height;
            height = (float) adjustedHeight;
        }

        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;

        buildTessellator(x, y, width, height, alpha, red, green, blue);
    }

    public static void drawRoundedRect2(float x, float y, float width, float height, int color) {
        drawRoundedRect(x, y, x + width, y + height, color);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        drawRoundedRect(x, y + radius, x + width, y + height - radius, color);
        drawRoundedRect(x + radius, y, x + width - radius, y + radius, color);
        drawRoundedRect(x + radius, y + height - radius, x + width - radius, y + height, color);

        ScissorUtils.startScissor(x, y, x + radius, y + radius);
        drawCircle(x + radius, y + radius, radius * 2.0F, color);
        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(x + width - radius, y, x + width, y + radius);
        drawCircle(x - radius + width, y + radius, radius * 2.0F, color);
        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(x, y + height - radius, x + radius, y + height);
        drawCircle(x + radius, y - radius + height, radius * 2.0F, color);
        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(x + width - radius, y + height - radius, x + width, y + height);
        drawCircle(x - radius + width, y - radius + height, radius * 2.0F, color);
        ScissorUtils.restoreScissor();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, float alpha) {
        StateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);
        int color = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, alpha);

        drawImage(x - radius, y - radius, radius, radius, Resources.SHADOW_CORNER_1, color);
        drawImage(x + width, y - radius, radius, radius, Resources.SHADOW_CORNER_2, color);
        drawImage(x - radius, y + height, radius, radius, Resources.SHADOW_CORNER_3, color);
        drawImage(x + width, y + height, radius, radius, Resources.SHADOW_CORNER_4, color);
        drawImage(x - radius, y, radius, height, Resources.SHADOW_LEFT, color, false);
        drawImage(x + width, y, radius, height, Resources.SHADOW_RIGHT, color, false);
        drawImage(x, y - radius, width, radius, Resources.SHADOW_TOP, color, false);
        drawImage(x, y + height, width, radius, Resources.SHADOW_BOTTOM, color, false);
    }

    public static void drawImage(float x, float y, float width, float height, Texture tex, float alphaValue) {
        drawImage(x, y, width, height, tex, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, alphaValue));
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture) {
        drawImage(x, y, width, height, texture, -1);
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture, int color) {
        drawImage(x, y, width, height, texture, color, 0.0F, 0.0F, (float) texture.getImageWidth(), (float) texture.getImageHeight(), true);
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture, int color, boolean linearFiltering) {
        drawImage(x, y, width, height, texture, color, 0.0F, 0.0F, (float) texture.getImageWidth(), (float) texture.getImageHeight(), linearFiltering);
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture, int color, float tlX, float tlY, float siW, float siH) {
        drawImage(x, y, width, height, texture, color, tlX, tlY, siW, siH, true);
    }

    public static void drawImage(float x, float y, float width, float height, Texture texture, int color, float tlX, float tlY, float siW, float siH, boolean linearFiltering) {
        if (texture != null) {
            RenderSystem.assertThread(RenderSystem::isOnRenderThread);
            StateManager.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            StateManager.color4f(0.0F, 0.0F, 0.0F, 0.0F);

            x = (float) Math.round(x);
            width = (float) Math.round(width);
            y = (float) Math.round(y);
            height = (float) Math.round(height);

            float red = (float) (color >> 16 & 0xFF) / 255.0F;
            float green = (float) (color >> 8 & 0xFF) / 255.0F;
            float blue = (float) (color & 0xFF) / 255.0F;
            float alpha = (float) (color >> 24 & 0xFF) / 255.0F;

            RenderSystem.enableBlend();
            RenderSystem.disableTexture();

            StateManager.color4f(red, green, blue, alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            RenderSystem.bindTexture(texture.getTextureID());
            texture.bind();

            float uScale = width / (float) texture.getTextureWidth() / (width / (float) texture.getImageWidth());
            float vScale = height / (float) texture.getTextureHeight() / (height / (float) texture.getImageHeight());

            float u0 = siW / (float) texture.getImageWidth() * uScale;
            float v0 = siH / (float) texture.getImageHeight() * vScale;
            float u1 = tlX / (float) texture.getImageWidth() * uScale;
            float v1 = tlY / (float) texture.getImageHeight() * vScale;

            if (!linearFiltering) {
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            } else {
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            }

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).texture(u1, v1).next();
            buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).texture(u1, v1 + v0).next();
            buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).texture(u1 + u0, v1 + v0).next();
            buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).texture(u1 + u0, v1).next();

            tessellator.draw();

            RenderSystem.disableTexture();
            RenderSystem.disableBlend();

            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public static void drawTexture(float x, float y, float width, float height, Texture texture, int color) {
        if (texture == null) return;

        drawImage(x, y, width, height, texture, color, 0.0F, 0.0F, (float) texture.getImageWidth(), (float) texture.getImageHeight(), true);
        drawImage(x, y, width, height, texture, color, 0.0F, 0.0F, (float) texture.getImageWidth(), (float) texture.getImageHeight(), false);
    }

    public static void drawRoundedButton(float x, float y, float width, float height, float radius, int color) {
        drawRoundedRect(x, y + radius, x + width, y + height - radius, color);
        drawRoundedRect(x + radius, y, x + width - radius, y + height, color);
        drawCircle(x + radius, y + radius, radius * 2.0F, color);
        drawCircle(x - radius + width, y + radius, radius * 2.0F, color);
        drawCircle(x + radius, y - radius + height, radius * 2.0F, color);
        drawCircle(x - radius + width, y - radius + height, radius * 2.0F, color);
    }

    public static void drawCircle(float x, float y, float size, int color) {
        StateManager.color4f(0.0F, 0.0F, 0.0F, 0.0F);
        StateManager.color4f(0.0F, 0.0F, 0.0F, 0.0F);

        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        StateManager.color4f(red, green, blue, alpha);

        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        RenderSystem.enableBlend();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        float radius = size / 2.0F;
        int segments = 360;

        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();

        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2 * i / segments;
            float vx = (float) (x + (Math.cos(angle) * radius));
            float vy = (float) (y + (Math.sin(angle) * radius));
            buffer.vertex(vx, vy, 0.0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawString(TrueTypeFont font, float x, float y, String text, int color, FontAlignment widthAlignment, FontAlignment heightAlignment) {
        drawString(font, x, y, text, color, widthAlignment, heightAlignment, false);
    }

    public static void drawString(TrueTypeFont font, float x, float y, String text, int color) {
        drawString(font, x, y, text, color, FontAlignment.LEFT, FontAlignment.TOP, false);
    }

    public static void drawString(TrueTypeFont font, float x, float y, String text, int color, FontAlignment widthAlignment, FontAlignment heightAlignment, boolean shadow) {
        StateManager.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        StateManager.color4f(0.0F, 0.0F, 0.0F, 0.0F);

        int adjustedX = 0;
        int adjustedY = 0;

        switch (widthAlignment) {
            case CENTER:
                adjustedX = -font.getWidth(text) / 2;
                break;

            case RIGHT:
                adjustedX = -font.getWidth(text);
                break;

            default:
                break;
        }

        switch (heightAlignment) {
            case CENTER:
                adjustedY = -font.getHeight(text) / 2;
                break;

            case BOTTOM:
                adjustedY = -font.getHeight(text);
                break;

            default:
                break;
        }

        StateManager.pushMatrix();

        boolean noHiDpiFontAvailable = false;
        if ((double) getScaleFactor() == 2.0) {
            if (font == FontUtils.HELVETICA_LIGHT_20) {
                font = FontUtils.HELVETICA_LIGHT_40;
            } else if (font == FontUtils.HELVETICA_LIGHT_25) {
                font = FontUtils.HELVETICA_LIGHT_50;
            } else if (font == FontUtils.HELVETICA_LIGHT_12) {
                font = FontUtils.HELVETICA_LIGHT_24;
            } else if (font == FontUtils.HELVETICA_LIGHT_14) {
                font = FontUtils.HELVETICA_LIGHT_28;
            } else if (font == FontUtils.HELVETICA_LIGHT_18) {
                font = FontUtils.HELVETICA_LIGHT_36;
            } else if (font == FontUtils.REGULAR_20) {
                font = FontUtils.REGULAR_40;
            } else if (font == FontUtils.HELVETICA_MEDIUM_20) {
                font = FontUtils.HELVETICA_MEDIUM_40;
            } else if (font == FontUtils.HELVETICA_MEDIUM_25) {
                font = FontUtils.HELVETICA_MEDIUM_50;
            } else {
                noHiDpiFontAvailable = true;
            }

            if (!noHiDpiFontAvailable) {
                StateManager.translatef(x, y, 0.0F);
                StateManager.scalef(1.0F / getScaleFactor(), 1.0F / getScaleFactor(), 1.0F / getScaleFactor());
                StateManager.translatef(-x, -y, 0.0F);
                adjustedX = (int) ((float) adjustedX * getScaleFactor());
                adjustedY = (int) ((float) adjustedY * getScaleFactor());
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (shadow) {
            font.drawString((float) Math.round(x + (float) adjustedX), (float) (Math.round(y + (float) adjustedY) + 2), text, new Color(0.0F, 0.0F, 0.0F, 0.35F));
        }

        if (text != null) {
            float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
            float red = (float) (color >> 16 & 0xFF) / 255.0F;
            float green = (float) (color >> 8 & 0xFF) / 255.0F;
            float blue = (float) (color & 0xFF) / 255.0F;

            font.drawString((float) Math.round(x + (float) adjustedX), (float) Math.round(y + (float) adjustedY), text, new Color(red, green, blue, alpha));
            TextureImpl.unbind();
        }

        RenderSystem.disableBlend();
        StateManager.popMatrix();
    }

    public static void drawFilledArc(float x, float y, float radius, int color) {
        drawFilledArc(x, y, 0.0F, 360.0F, radius - 1.0F, color);
    }

    public static void drawFilledArc(float x, float y, float startAngle, float endAngle, float radius, int color) {
        drawFilledArc(x, y, startAngle, endAngle, radius, radius, color);
    }

    public static void drawFilledArc(float x, float y, float startAngle, float endAngle, float hRadius, float vRadius, int color) {
        if (startAngle > endAngle) {
            float tempAngle = endAngle;
            endAngle = startAngle;
            startAngle = tempAngle;
        }

        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (alpha > 0.5F) {
            RenderSystem.lineWidth(2.0F);

            buffer.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (float angle = endAngle; angle >= startAngle; angle -= 4.0F) {
                float radiusX = (float) Math.cos(Math.toRadians(angle)) * hRadius * 1.001F;
                float radiusY = (float) Math.sin(Math.toRadians(angle)) * vRadius * 1.001F;
                buffer.vertex(x + radiusX, y + radiusY, 0.0).color(red, green, blue, alpha).next();
            }
            tessellator.draw();
        }

        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();

        for (float angle = endAngle; angle >= startAngle; angle -= 4.0F) {
            float radiusX = (float) Math.cos(Math.toRadians(angle)) * hRadius;
            float radiusY = (float) Math.sin(Math.toRadians(angle)) * vRadius;
            buffer.vertex(x + radiusX, y + radiusY, 0.0).color(red, green, blue, alpha).next();
        }

        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawGradient(int x, int y, int width, int height, int color1, int color2) {
        float red1 = (float) (color1 >> 16 & 0xFF) / 255.0F;
        float green1 = (float) (color1 >> 8 & 0xFF) / 255.0F;
        float blue1 = (float) (color1 & 0xFF) / 255.0F;
        float alpha1 = (float) (color1 >> 24 & 0xFF) / 255.0F;

        float red2 = (float) (color2 >> 16 & 0xFF) / 255.0F;
        float green2 = (float) (color2 >> 8 & 0xFF) / 255.0F;
        float blue2 = (float) (color2 & 0xFF) / 255.0F;
        float alpha2 = (float) (color2 >> 24 & 0xFF) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        StateManager.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        StateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(width, y, 0.0).color(red1, green1, blue1, alpha1).next();
        buffer.vertex(x, y, 0.0).color(red1, green1, blue1, alpha1).next();
        buffer.vertex(x, height, 0.0).color(red2, green2, blue2, alpha2).next();
        buffer.vertex(width, height, 0.0).color(red2, green2, blue2, alpha2).next();
        tessellator.draw();

        StateManager.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        StateManager.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void drawRoundedRect2(float x, float y, float width, float height, int radius, int color) {
        drawRoundedRect(x, height - (float) radius, width - (float) radius, height, color);
        drawRoundedRect(x, y, width - (float) radius, y + (float) radius, color);
        drawRoundedRect(x, y + (float) radius, x + (float) radius, height - (float) radius, color);
        drawRoundedRect(width - (float) radius, y, width, height, color);
    }

    public static void drawRoundedRect3(float x, float y, float width, float height, int color) {
        drawRoundedRect2(x, y, width, height, 1, color);
    }

    public static void drawTriangle(float left, float top, float right, float bottom, float tipX, float tipY, int color) {
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        buffer.vertex(left, top, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(tipX, tipY, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(right, bottom, 0.0).color(red, green, blue, alpha).next();

        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawTexturedQuad(float x, float y, float width, float height, int color, float u, float v, float textureWidth, float textureHeight) {
        x = (float) Math.round(x);
        y = (float) Math.round(y);
        width = (float) Math.round(width);
        height = (float) Math.round(height);

        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        float texU = u / textureWidth;
        float texV = v / textureHeight;
        float texU2 = texU + 1.0f;
        float texV2 = texV + 1.0f;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        buffer.vertex(x, y, 0.0f).texture(texU, texV).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0f).texture(texU, texV2).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0f).texture(texU2, texV2).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0f).texture(texU2, texV).color(red, green, blue, alpha).next();

        tessellator.draw();

        RenderSystem.disableBlend();
    }

    public static void drawFloatingPanel(int x, int y, int width, int height, int color) {
        drawFloatingPanelClipped(x, y, width, height, color, x, y);
    }

    public static void drawFloatingPanelClipped(int x, int y, int width, int height, int color, int scissorX, int scissorY) {
        int tileSize = 36;
        int padding = 10;
        int innerOffset = tileSize - padding;

        renderTile(tileSize, padding, innerOffset, x, y, width, height, color);

        ScissorUtils.startScissorNoGL(
                scissorX - tileSize,
                scissorY + padding,
                scissorX - innerOffset + tileSize,
                scissorY - padding + height
        );

        for (int offsetY = 0; offsetY < height; offsetY += tileSize) {
            drawImage(
                    (float) (x - innerOffset),
                    (float) (y + padding + offsetY),
                    (float) tileSize,
                    (float) tileSize,
                    Resources.FLOATING_BORDER,
                    color
            );
        }

        ScissorUtils.restoreScissor();

        ScissorUtils.startScissorNoGL(
                scissorX,
                scissorY - innerOffset,
                scissorX + width - padding,
                scissorY + padding
        );

        for (int offsetX = 0; offsetX < width; offsetX += tileSize) {
            rotate90Tile(x, y, tileSize);
            drawImage(
                    (float) (x - innerOffset),
                    (float) (y - padding - offsetX),
                    (float) tileSize,
                    (float) tileSize,
                    Resources.FLOATING_BORDER,
                    color
            );
            StateManager.popMatrix();
        }

        ScissorUtils.restoreScissor();

        ScissorUtils.startScissorNoGL(
                scissorX + width - padding,
                scissorY - innerOffset,
                x + width + innerOffset,
                scissorY + height - padding
        );

        for (int offsetY = 0; offsetY < height; offsetY += tileSize) {
            rotate180Tile(x, y, tileSize);
            drawImage(
                    (float) (x - width + padding),
                    (float) (y - padding - offsetY),
                    (float) tileSize,
                    (float) tileSize,
                    Resources.FLOATING_BORDER,
                    color
            );
            StateManager.popMatrix();
        }

        ScissorUtils.restoreScissor();

        ScissorUtils.startScissorNoGL(
                scissorX - padding,
                scissorY - innerOffset + height - tileSize,
                scissorX + width - padding,
                scissorY + height + padding * 2
        );

        for (int offsetX = 0; offsetX < width; offsetX += tileSize) {
            rotate270Tile(x, y, tileSize);
            drawImage(
                    (float) (x - height + padding),
                    (float) (y + padding + offsetX),
                    (float) tileSize,
                    (float) tileSize,
                    Resources.FLOATING_BORDER,
                    color
            );
            StateManager.popMatrix();
        }

        ScissorUtils.restoreScissor();
    }

    public static void drawFloatingPanelScaled(int x, int y, int width, int height, int color) {
        int tileSize = 36;
        int padding = 10;
        int innerOffset = tileSize - padding;

        renderTile(tileSize, padding, innerOffset, x, y, width, height, color);

        ScissorUtils.startScissor(
                x - tileSize,
                y + padding,
                x - innerOffset + tileSize,
                y - padding + height,
                true
        );

        for (int offsetY = 0; offsetY < height; offsetY += tileSize) {
            drawImage(
                    (float) (x - innerOffset),
                    (float) (y + padding + offsetY) - 0.4F,
                    (float) tileSize,
                    (float) tileSize + 0.4F,
                    Resources.FLOATING_BORDER,
                    color
            );
        }

        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(
                x,
                y - innerOffset,
                x + width - padding,
                y + padding,
                true
        );

        for (int offsetX = 0; offsetX < width; offsetX += tileSize) {
            rotate90Tile(x, y, tileSize);
            drawImage(
                    (float) (x - innerOffset),
                    (float) (y - padding - offsetX) - 0.4F,
                    (float) tileSize,
                    (float) tileSize + 0.4F,
                    Resources.FLOATING_BORDER,
                    color
            );
            StateManager.popMatrix();
        }

        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(
                x + width - padding,
                y - innerOffset,
                x + width + innerOffset,
                y + height - padding,
                true
        );

        for (int offsetY = 0; offsetY < height; offsetY += tileSize) {
            rotate180Tile(x, y, tileSize);
            drawImage(
                    (float) (x - width + padding),
                    (float) (y - padding - offsetY) - 0.4F,
                    (float) tileSize,
                    (float) tileSize + 0.4F,
                    Resources.FLOATING_BORDER,
                    color
            );
            StateManager.popMatrix();
        }

        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(
                x - padding,
                y - innerOffset + height - tileSize,
                x + width - padding,
                y + height + padding * 2,
                true
        );

        for (int offsetX = 0; offsetX < width; offsetX += tileSize) {
            rotate270Tile(x, y, tileSize);
            drawImage(
                    (float) (x - height + padding),
                    (float) (y + padding + offsetX) - 0.4F,
                    (float) tileSize,
                    (float) tileSize + 0.4F,
                    Resources.FLOATING_BORDER,
                    color
            );
            StateManager.popMatrix();
        }

        ScissorUtils.restoreScissor();
    }

    private static void renderTile(int tileSize, int padding, int innerOffset, float x, float y, float width, float height, int color) {
        drawRoundedRect(
                x + padding,
                y + padding,
                x + width - padding,
                y + height - padding,
                color
        );

        drawImage(
                x - innerOffset,
                y - innerOffset,
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );

        StateManager.pushMatrix();
        StateManager.translatef(x + width - tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
        StateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
        StateManager.translatef(-x - width - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
        drawImage(
                x + width - innerOffset,
                y - innerOffset,
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        StateManager.popMatrix();

        StateManager.pushMatrix();
        StateManager.translatef(x + width - tileSize / 2.0f, y + height + tileSize / 2.0f, 0.0F);
        StateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        StateManager.translatef(-x - width - tileSize / 2.0f, -y - height - tileSize / 2.0f, 0.0F);
        drawImage(
                x + width - innerOffset,
                y + padding + height,
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        StateManager.popMatrix();

        StateManager.pushMatrix();
        StateManager.translatef(x - tileSize / 2.0f, y + height + tileSize / 2.0f, 0.0F);
        StateManager.rotatef(270.0F, 0.0F, 0.0F, 1.0F);
        StateManager.translatef(-x - tileSize / 2.0f, -y - height - tileSize / 2.0f, 0.0F);
        drawImage(
                x + padding,
                y + padding + height,
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        StateManager.popMatrix();
    }

    private static void rotate90Tile(int x, int y, int tileSize) {
        StateManager.pushMatrix();
        StateManager.translatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
        StateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
        StateManager.translatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
    }

    private static void rotate270Tile(int x, int y, int tileSize) {
        StateManager.pushMatrix();
        StateManager.translatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
        StateManager.rotatef(270.0F, 0.0F, 0.0F, 1.0F);
        StateManager.translatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
    }

    private static void rotate180Tile(int x, int y, int tileSize) {
        StateManager.pushMatrix();
        StateManager.translatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
        StateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        StateManager.translatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
    }

    public static void drawPanelShadow(float x, float y, float width, float height, float shadowSize, float alpha) {
        int shadowColor = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, alpha);

        drawImage(x, y, shadowSize, height, Resources.SHADOW_RIGHT, shadowColor, false);
        drawImage(x + width - shadowSize, y, shadowSize, height, Resources.SHADOW_LEFT, shadowColor, false);
        drawImage(x, y, width, shadowSize, Resources.SHADOW_BOTTOM, shadowColor, false);
        drawImage(x, y + height - shadowSize, width, shadowSize, Resources.SHADOW_TOP, shadowColor, false);
    }

    public static void renderItemStack(ItemStack stack, int x, int y, int width, int height) {
        if (stack != null) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);

            StateManager.pushMatrix();
            StateManager.translatef((float) x, (float) y, 0.0F);
            StateManager.scalef((float) width / 16.0F, (float) height / 16.0F, 0.0F);

            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            if (stack.getCount() == 0) {
                stack = new ItemStack(stack.getItem());
            }

            RenderHelper.setupGuiFlatDiffuseLighting();
            GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, new float[]{0.4F, 0.4F, 0.4F, 1.0F});

            StateManager.enableColorMaterial();
            StateManager.disableLighting();
            RenderSystem.enableBlend();

            StateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthFunc(GL11.GL_ALWAYS);

            itemRenderer.renderInGui(stack, 0, 0);

            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            StateManager.popMatrix();

            StateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);
            StateManager.glMultiTexCoord2f(GL13.GL_TEXTURE2, 240.0F, 240.0F);
            RenderSystem.disableDepthTest();
            TextureImpl.unbind();

            MinecraftClient.getInstance().getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);
            RenderHelper.setupGui3DDiffuseLighting();
        }
    }

    public static void drawColoredQuad(int x1, int y1, int x2, int y2, int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
        float a1 = (float) (topLeftColor >> 24 & 0xFF) / 255.0F;
        float r1 = (float) (topLeftColor >> 16 & 0xFF) / 255.0F;
        float g1 = (float) (topLeftColor >> 8 & 0xFF) / 255.0F;
        float b1 = (float) (topLeftColor & 0xFF) / 255.0F;

        float a2 = (float) (topRightColor >> 24 & 0xFF) / 255.0F;
        float r2 = (float) (topRightColor >> 16 & 0xFF) / 255.0F;
        float g2 = (float) (topRightColor >> 8 & 0xFF) / 255.0F;
        float b2 = (float) (topRightColor & 0xFF) / 255.0F;

        float a3 = (float) (bottomRightColor >> 24 & 0xFF) / 255.0F;
        float r3 = (float) (bottomRightColor >> 16 & 0xFF) / 255.0F;
        float g3 = (float) (bottomRightColor >> 8 & 0xFF) / 255.0F;
        float b3 = (float) (bottomRightColor & 0xFF) / 255.0F;

        float a4 = (float) (bottomLeftColor >> 24 & 0xFF) / 255.0F;
        float r4 = (float) (bottomLeftColor >> 16 & 0xFF) / 255.0F;
        float g4 = (float) (bottomLeftColor >> 8 & 0xFF) / 255.0F;
        float b4 = (float) (bottomLeftColor & 0xFF) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        StateManager.disableAlphaTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO
        );
        StateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x2, y1, 0.0).color(r2, g2, b2, a2).next();
        buffer.vertex(x1, y1, 0.0).color(r1, g1, b1, a1).next();
        buffer.vertex(x1, y2, 0.0).color(r4, g4, b4, a4).next();
        buffer.vertex(x2, y2, 0.0).color(r3, g3, b3, a3).next();

        tessellator.draw();

        StateManager.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        StateManager.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void drawVerticalDivider(float x, float yStart, float xEnd, float yEnd, int color) {
        drawVerticalDivider(x, yStart, xEnd, yEnd, 1, color);
    }

    public static void drawVerticalDivider(float x, float yStart, float xEnd, float yEnd, int thickness, int color) {
        drawRoundedRect(x, yEnd - (float) thickness, xEnd - (float) thickness, yEnd, color);
        drawRoundedRect(x, yStart, xEnd - (float) thickness, yStart + (float) thickness, color);
        drawRoundedRect(x, yStart + (float) thickness, x + (float) thickness, yEnd - (float) thickness, color);
        drawRoundedRect(xEnd - (float) thickness, yStart, xEnd, yEnd, color);
    }

    public static void drawTexturedQuad(float x, float y, float width, float height, ByteBuffer pixelBuffer, int color, float textureOffsetX, float textureOffsetY, float textureWidth, float textureHeight, boolean flipX, boolean flipY) {
        x = (float) Math.round(x);
        y = (float) Math.round(y);
        width = (float) Math.round(width);
        height = (float) Math.round(height);

        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        StateManager.color4f(red, green, blue, alpha);

        RenderSystem.enableBlend();
        RenderSystem.enableTexture();

        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        if (dynamicPixelTextureId == -1) {
            dynamicPixelTextureId = GL11.glGenTextures();
            dynamicPixelTextureWidth = -1;
            dynamicPixelTextureHeight = -1;
        }

        StateManager.glBindTexture(GL11.GL_TEXTURE_2D, dynamicPixelTextureId);

        resetGlUnpackState();

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

        if (dynamicPixelTextureWidth != (int) textureWidth || dynamicPixelTextureHeight != (int) textureHeight) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, (int) textureWidth, (int) textureHeight, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixelBuffer);
            dynamicPixelTextureWidth = (int) textureWidth;
            dynamicPixelTextureHeight = (int) textureHeight;
        } else {
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, (int) textureWidth, (int) textureHeight, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixelBuffer);
        }

        float u = textureOffsetX / textureWidth;
        float v = textureOffsetY / textureHeight;

        float uMax = 1.0f;
        float vMax = 1.0f;

        StateManager.glBegin(GL11.GL_QUADS);
        StateManager.glTexCoord2f(u + (flipX ? uMax : 0.0F), v + (flipY ? vMax : 0.0F));
        StateManager.glVertex2f(x, y);

        StateManager.glTexCoord2f(u + (flipX ? uMax : 0.0F), v + (flipY ? 0.0F : vMax));
        StateManager.glVertex2f(x, y + height);

        StateManager.glTexCoord2f(u + (flipX ? 0.0F : uMax), v + (flipY ? 0.0F : vMax));
        StateManager.glVertex2f(x + width, y + height);

        StateManager.glTexCoord2f(u + (flipX ? 0.0F : uMax), v + (flipY ? vMax : 0.0F));
        StateManager.glVertex2f(x + width, y);
        StateManager.glEnd();

        StateManager.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRotatedTriangle() {
        StateManager.translatef(0.0F, 0.0F, 0.3F);
        StateManager.glNormal3f(0.0F, 0.0F, 1.0F);
        StateManager.rotated(-37.0, 1.0, 0.0, 0.0);

        StateManager.glBegin(GL11.GL_TRIANGLES);
        StateManager.glVertex2f(0.0F, 0.4985F);
        StateManager.glVertex2f(-0.3F, 0.0F);
        StateManager.glVertex2f(0.3F, 0.0F);
        StateManager.glEnd();
    }

    public static void drawColoredRect(float x1, float y1, float x2, float y2, int color) {
        if (x1 < x2) {
            int temp = (int) x1;
            x1 = x2;
            x2 = temp;
        }

        if (y1 < y2) {
            int temp = (int) y1;
            y1 = y2;
            y2 = temp;
        }

        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;

        buildTessellator(x1, y1, x2, y2, alpha, red, green, blue);
    }

    private static void buildTessellator(float x1, float y1, float x2, float y2, float alpha, float red, float green, float blue) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO
        );

        StateManager.color4f(red, green, blue, alpha);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y2, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y1, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x1, y1, 0.0).color(red, green, blue, alpha).next();

        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawColoredRotatedTriangle(int color) {
        StateManager.color4fv(ColorHelper.unpackColorToRGBA(color));
        drawRotatedTriangle();
    }

    public static void drawCircleOutline(float radius) {
        StateManager.glBegin(GL11.GL_LINE_LOOP);
        for (int angle = 0; angle < 360; angle++) {
            double rad = angle * Math.PI / 180.0;
            StateManager.glVertex2d(Math.cos(rad) * radius, Math.sin(rad) * radius);
        }
        StateManager.glEnd();
    }

    public static void drawFilledCircle(float radius) {
        StateManager.glBegin(GL11.GL_TRIANGLE_FAN);
        for (int angle = 0; angle < 360; angle++) {
            double rad = angle * Math.PI / 180.0;
            StateManager.glVertex2d(Math.cos(rad) * radius, Math.sin(rad) * radius);
        }
        StateManager.glEnd();
    }

    public static void drawRotatingTriangles(int color) {
        for (int angle = 0; angle <= 270; angle += 90) {
            StateManager.pushMatrix();
            StateManager.rotatef(angle, 0.0F, 1.0F, 0.0F);
            StateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
            drawColoredRotatedTriangle(ColorHelper.blendColors(ClientColors.DEEP_TEAL, color, 0.04F * angle / 90.0F));
            StateManager.popMatrix();
        }

        for (int angle = 0; angle <= 270; angle += 90) {
            StateManager.pushMatrix();
            StateManager.rotatef(angle, 0.0F, 1.0F, 0.0F);
            drawColoredRotatedTriangle(ColorHelper.blendColors(ClientColors.DEEP_TEAL, color, 0.04F * angle / 90.0F));
            StateManager.popMatrix();
        }
    }

    public static void drawWaypointIndicator(float x, float y, float z, String label, int color, float scale) {
        boolean lightingWasEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        StateManager.disableLighting();
        RenderSystem.depthMask(false);

        StateManager.pushMatrix();
        StateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);
        StateManager.color4f(0.0F, 0.0F, 0.0F, 0.114F);
        StateManager.translated(x + 0.5, y, z + 0.5);
        StateManager.rotatef(90.0F, -1.0F, 0.0F, 0.0F);
        drawFilledCircle(0.5F);
        StateManager.popMatrix();

        StateManager.pushMatrix();
        StateManager.color4fv(ColorHelper.unpackColorToRGBA(color));
        StateManager.translated(x + 0.5, y + 0.7F, z + 0.5);
        StateManager.rotatef((float) (client.player.age % 90 * 4), 0.0F, -1.0F, 0.0F);
        RenderSystem.lineWidth(1.4F + 1.4F / scale);
        drawCircleOutline(0.6F);
        StateManager.popMatrix();

        StateManager.pushMatrix();
        StateManager.translated(x + 0.5, y + 0.7F, z + 0.5);
        StateManager.rotatef((float) (client.player.age % 90 * 4), 0.0F, 1.0F, 0.0F);
        drawRotatingTriangles(color);
        StateManager.popMatrix();

        StateManager.pushMatrix();
        StateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);
        StateManager.translated(x + 0.5, y + 1.9, z + 0.5);
        StateManager.rotatef(client.gameRenderer.getCamera().getYaw(), 0.0F, -1.0F, 0.0F);
        StateManager.rotatef(client.gameRenderer.getCamera().getPitch(), 1.0F, 0.0F, 0.0F);

        TrueTypeFont font = FontUtils.HELVETICA_LIGHT_25;

        StateManager.pushMatrix();
        StateManager.scalef(-0.009F * scale, -0.009F * scale, -0.009F * scale);
        StateManager.translated(0.0, -20.0 * Math.sqrt(Math.sqrt(scale)), 0.0);

        int bgColor = ColorHelper.applyAlpha(
                ColorHelper.blendColors(ClientColors.LIGHT_GREYISH_BLUE, ClientColors.DEEP_TEAL, 75.0F),
                0.5F
        );

        drawColoredRect(
                (float) (-font.getWidth(label) / 2 - 14),
                -5.0F,
                (float) font.getWidth(label) / 2.0F + 14.0F,
                (float) (font.getHeight() + 7),
                bgColor
        );

        drawRoundedRect(
                (float) (-font.getWidth(label) / 2 - 14),
                -5.0F,
                (float) (font.getWidth(label) + 28),
                (float) (font.getHeight() + 12),
                20.0F,
                0.5F
        );

        StateManager.translated((double) -font.getWidth(label) / 2, 0.0, 0.0);
        drawString(font, 0.0F, 0.0F, label, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, 0.8F));

        StateManager.popMatrix();
        StateManager.popMatrix();

        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();

        if (lightingWasEnabled) {
            StateManager.enableLighting();
        } else {
            StateManager.disableLighting();
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public static void drawRect(float x, float y, float width, float height, int color) {
        drawColoredRect(x, y, x + width, y + height, color);
    }

    public static void drawTargetIndicatorRing(java.awt.Color baseColor, Entity target, float progress) {
        StateManager.pushMatrix();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableTexture();
        StateManager.disableLighting();
        GL11.glEnable(GL13.GL_MULTISAMPLE);
        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1.4F);

        double tickDelta = client.getTickDelta();
        if (!target.isAlive()) {
            tickDelta = 0.0;
        }

        StateManager.translated(
                target.prevX + (target.getX() - target.prevX) * tickDelta,
                target.prevY + (target.getY() - target.prevY) * tickDelta,
                target.prevZ + (target.getZ() - target.prevZ) * tickDelta);
        StateManager.translated(
                -client.gameRenderer.getCamera().getPos().getX(),
                -client.gameRenderer.getCamera().getPos().getY(),
                -client.gameRenderer.getCamera().getPos().getZ());

        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        StateManager.enableAlphaTest();
        RenderSystem.enableBlend();
        StateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);

        long animationPeriodMs = 1800;
        float phase = (float) (System.currentTimeMillis() % animationPeriodMs) / (float) animationPeriodMs;
        boolean reverseGradient = phase > 0.5F;

        phase = !reverseGradient ? phase * 2.0F : 1.0F - phase * 2.0F % 1.0F;

        StateManager.translatef(0.0F, (target.getHeight() + 0.4F) * phase, 0.0F);
        float pulse = (float) Math.sin((double) phase * Math.PI);
        drawAnimatedRing(baseColor, reverseGradient, 0.45F * pulse, 0.6F, 0.35F * pulse, progress);
        StateManager.pushMatrix();
        StateManager.translated(
                client.gameRenderer.getCamera().getPos().getX(),
                client.gameRenderer.getCamera().getPos().getY(),
                client.gameRenderer.getCamera().getPos().getZ());
        StateManager.popMatrix();
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        StateManager.disableAlphaTest();

        RenderSystem.enableTexture();
        GL11.glDisable(GL13.GL_MULTISAMPLE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        StateManager.popMatrix();
    }

    public static void drawAnimatedRing(java.awt.Color baseColor, boolean reverseGradient, float ringHeight, float ringRadius, float ringAlphaScale, float progressAlpha) {
        StateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        RenderSystem.disableDepthTest();
        StateManager.glBegin(GL11.GL_TRIANGLE_STRIP);

        int angleStep = (int) (360.0F / (40.0F * ringRadius));
        float red = (float) baseColor.getRed() / 255.0F;
        float green = (float) baseColor.getGreen() / 255.0F;
        float blue = (float) baseColor.getBlue() / 255.0F;

        for (int angle = 0; angle <= 360 + angleStep; angle += angleStep) {
            int clampedAngle = angle;
            if (angle > 360) {
                clampedAngle = 0;
            }

            double x = Math.sin((double) clampedAngle * Math.PI / 180.0) * (double) ringRadius;
            double z = Math.cos((double) clampedAngle * Math.PI / 180.0) * (double) ringRadius;
            StateManager.color4f(red, green, blue, !reverseGradient ? 0.0F : ringAlphaScale * progressAlpha);
            StateManager.glVertex3d(x, 0.0, z);
            StateManager.color4f(red, green, blue, reverseGradient ? 0.0F : ringAlphaScale * progressAlpha);
            StateManager.glVertex3d(x, ringHeight, z);
        }

        StateManager.glEnd();
        RenderSystem.lineWidth(2.2F);
        StateManager.glBegin(GL11.GL_LINE_STRIP);

        for (int angle = 0; angle <= 360 + angleStep; angle += angleStep) {
            int clampedAngle = angle;
            if (angle > 360) {
                clampedAngle = 0;
            }

            double x = Math.sin((double) clampedAngle * Math.PI / 180.0) * (double) ringRadius;
            double z = Math.cos((double) clampedAngle * Math.PI / 180.0) * (double) ringRadius;
            StateManager.color4f(red, green, blue, (0.5F + 0.5F * ringAlphaScale) * progressAlpha);
            StateManager.glVertex3d(x, !reverseGradient ? (double) ringHeight : 0.0, z);
        }

        StateManager.glEnd();
        RenderSystem.enableDepthTest();
        StateManager.shadeModel(GL11.GL_FLAT);
    }

    public static void resetHudGlState() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        TextureImpl.unbind();
        StateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        StateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.disableScissor();
        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO
        );
    }

    public static void resetGlUnpackState() {
        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
    }

    public static void drawLayeredCircle(int centerX, int centerY, int color, float alpha) {
        final float baseRadius = 14.0F;

        drawCircle(
                (float) centerX,
                (float) centerY,
                baseRadius,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL, 0.1F * alpha)
        );
        drawCircle(
                (float) centerX,
                (float) centerY,
                baseRadius - 1.0F,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL, 0.14F * alpha)
        );
        drawCircle(
                (float) centerX,
                (float) centerY,
                baseRadius - 2.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, alpha)
        );
        drawCircle(
                (float) centerX,
                (float) centerY,
                baseRadius - 6.0F,
                ColorHelper.applyAlpha(ColorHelper.shiftTowardsOther(color, ClientColors.DEEP_TEAL, 0.7F), alpha)
        );
        drawCircle(
                (float) centerX,
                (float) centerY,
                baseRadius - 7.0F,
                ColorHelper.applyAlpha(color, alpha)
        );
    }

    public static void renderFadeOut(float backgroundOpacity, float progress) {
        StateManager.enableAlphaTest();
        RenderSystem.enableBlend();

        float screenWidth = (float) client.getWindow().getWidth();
        float screenHeight = (float) client.getWindow().getHeight();

        drawImage(0.0F, 0.0F, screenWidth, screenHeight, Resources.LOADING_SCREEN_BACKGROUND, backgroundOpacity);
        drawRoundedRect2(0.0F, 0.0F, screenWidth, screenHeight, ColorHelper.applyAlpha(0, 0.75F));

        final int logoWidth = 455;
        final int logoHeight = 78;

        int logoX = (client.getWindow().getWidth() - logoWidth) / 2;
        int logoY = Math.round(((client.getWindow().getHeight() - logoHeight) / 2.0F) - (14.0F * backgroundOpacity));

        float logoScale = 0.75F + (float) (Math.pow(backgroundOpacity, 4.0) * 0.25F);

        StateManager.pushMatrix();
        StateManager.translatef(client.getWindow().getWidth() / 2.0F, client.getWindow().getHeight() / 2.0F, 0.0F);
        StateManager.scalef(logoScale, logoScale, 0.0F);
        StateManager.translatef(-client.getWindow().getWidth() / 2.0F, -client.getWindow().getHeight() / 2.0F, 0.0F);

        drawImage(
                (float) logoX,
                (float) logoY,
                (float) logoWidth,
                (float) logoHeight,
                Resources.LOGO,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, backgroundOpacity)
        );

        float clampedProgress = Math.min(1.0F, progress * 1.02F);
        final float progressBarYOffset = 80.0F;

        if (backgroundOpacity == 1.0F) {
            drawRoundedRect(
                    (float) logoX,
                    logoY + logoHeight + progressBarYOffset,
                    (float) logoWidth,
                    20.0F,
                    10.0F,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, 0.3F * backgroundOpacity)
            );
            drawRoundedRect(
                    (float) (logoX + 1),
                    logoY + logoHeight + progressBarYOffset + 1,
                    (float) (logoWidth - 2),
                    18.0F,
                    9.0F,
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL, backgroundOpacity)
            );
        }

        drawRoundedRect(
                (float) (logoX + 2),
                logoY + logoHeight + progressBarYOffset + 2,
                (float) ((int) ((float) (logoWidth - 4) * clampedProgress)),
                16.0F,
                8.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, 0.9F * backgroundOpacity)
        );

        StateManager.popMatrix();
    }

    public static void drawVerticalGradientRect(int left, int top, int right, int bottom, int topColor, int bottomColor) {
        float topA = (float) ((topColor >> 24) & 0xFF) / 255.0F;
        float topR = (float) ((topColor >> 16) & 0xFF) / 255.0F;
        float topG = (float) ((topColor >> 8) & 0xFF) / 255.0F;
        float topB = (float) (topColor & 0xFF) / 255.0F;

        float botA = (float) ((bottomColor >> 24) & 0xFF) / 255.0F;
        float botR = (float) ((bottomColor >> 16) & 0xFF) / 255.0F;
        float botG = (float) ((bottomColor >> 8) & 0xFF) / 255.0F;
        float botB = (float) (bottomColor & 0xFF) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        StateManager.disableAlphaTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
        );

        StateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(right, top, 0.0).color(topR, topG, topB, topA).next();
        buffer.vertex(left, top, 0.0).color(topR, topG, topB, topA).next();
        buffer.vertex(left, bottom, 0.0).color(botR, botG, botB, botA).next();
        buffer.vertex(right, bottom, 0.0).color(botR, botG, botB, botA).next();

        tess.draw();

        StateManager.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        StateManager.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void renderWireframeBox(Box box, float width, int color) {
        if (box != null) {
            float red = (float) (color >> 16 & 0xFF) / 255.0f;
            float green = (float) (color >> 8 & 0xFF) / 255.0f;
            float blue = (float) (color & 0xFF) / 255.0f;
            float alpha = (float) (color >> 24 & 0xFF) / 255.0f;

            StateManager.color4f(red, green, blue, alpha);
            RenderSystem.disableTexture();
            StateManager.disableLighting();
            RenderSystem.lineWidth(width);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            RenderSystem.enableBlend();
            StateManager.glBegin(GL11.GL_LINE_STRIP);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_LINE_STRIP);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_LINES);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.maxZ);
            StateManager.glEnd();
            RenderSystem.enableTexture();
            StateManager.enableLighting();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            RenderSystem.disableBlend();
        }
    }

    public static void render3DColoredBox(Box box, int color) {
        if (box != null) {
            float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
            float red = (float) (color >> 16 & 0xFF) / 255.0f;
            float green = (float) (color >> 8 & 0xFF) / 255.0f;
            float blue = (float) (color & 0xFF) / 255.0f;

            StateManager.color4f(red, green, blue, alpha);
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            StateManager.disableLighting();
            RenderSystem.lineWidth(1.8f * getScaleFactor());
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.minX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.maxZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.maxX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.maxZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glVertex3d(box.minX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.minX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.maxZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.maxX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.maxZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.maxX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.minZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.maxX, box.minY, box.minZ);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.minZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.maxZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.maxX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.minZ);
            StateManager.glVertex3d(box.minX, box.maxY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.maxY, box.maxZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.minZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.minX, box.minY, box.maxZ);
            StateManager.glEnd();
            StateManager.glBegin(GL11.GL_QUADS);
            StateManager.glVertex3d(box.maxX, box.minY, box.minZ);
            StateManager.glVertex3d(box.minX, box.minY, box.minZ);
            StateManager.glVertex3d(box.minX, box.minY, box.maxZ);
            StateManager.glVertex3d(box.maxX, box.minY, box.maxZ);
            StateManager.glEnd();
            RenderSystem.enableTexture();
            StateManager.enableLighting();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            RenderSystem.disableBlend();
        }
    }
}