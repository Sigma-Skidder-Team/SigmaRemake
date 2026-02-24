package io.github.sst.remake.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.newdawn.slick.util.math.Color;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.opengl.texture.TextureImpl;

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

        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder buffer = tesselator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.color4f(red, green, blue, alpha);

        buffer.begin(7, VertexFormats.POSITION);
        buffer.vertex(x, height, 0.0).next();
        buffer.vertex(width, height, 0.0).next();
        buffer.vertex(width, y, 0.0).next();
        buffer.vertex(x, y, 0.0).next();
        tesselator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect2(float x, float y, float width, float height, int color) {
        drawRoundedRect(x, y, x + width, y + height, color);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float size, int color) {
        drawRoundedRect(x, y + size, x + width, y + height - size, color);
        drawRoundedRect(x + size, y, x + width - size, y + size, color);
        drawRoundedRect(x + size, y + height - size, x + width - size, y + height, color);

        ScissorUtils.startScissor(x, y, x + size, y + size);
        drawCircle(x + size, y + size, size * 2.0F, color);
        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(x + width - size, y, x + width, y + size);
        drawCircle(x - size + width, y + size, size * 2.0F, color);
        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(x, y + height - size, x + size, y + height);
        drawCircle(x + size, y - size + height, size * 2.0F, color);
        ScissorUtils.restoreScissor();

        ScissorUtils.startScissor(x + width - size, y + height - size, x + width, y + height);
        drawCircle(x - size + width, y - size + height, size * 2.0F, color);
        ScissorUtils.restoreScissor();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, float alpha) {
        GL11.glAlphaFunc(519, 0.0F);
        int color = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha);

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
        drawImage(x, y, width, height, tex, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alphaValue));
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
            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

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
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.color4f(red, green, blue, alpha);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            texture.bind();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            float var17 = width / (float) texture.getTextureWidth() / (width / (float) texture.getImageWidth());
            float var18 = height / (float) texture.getTextureHeight() / (height / (float) texture.getImageHeight());
            float var19 = siW / (float) texture.getImageWidth() * var17;
            float var20 = siH / (float) texture.getImageHeight() * var18;
            float var21 = tlX / (float) texture.getImageWidth() * var17;
            float var22 = tlY / (float) texture.getImageHeight() * var18;

            if (!linearFiltering) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            } else {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            }

            GL11.glBegin(7);
            GL11.glTexCoord2f(var21, var22);
            GL11.glVertex2f(x, y);

            GL11.glTexCoord2f(var21, var22 + var20);
            GL11.glVertex2f(x, y + height);

            GL11.glTexCoord2f(var21 + var19, var22 + var20);
            GL11.glVertex2f(x + width, y + height);

            GL11.glTexCoord2f(var21 + var19, var22);
            GL11.glVertex2f(x + width, y);
            GL11.glEnd();

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);

            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public static void drawTexture(float x, float y, float width, float height, Texture texture, int color) {
        if (texture == null) {
            return;
        }

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
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(red, green, blue, alpha);

        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glPointSize(size * getScaleFactor());
        GL11.glBegin(0);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);

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
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

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

        GL11.glPushMatrix();

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
                GL11.glTranslatef(x, y, 0.0F);
                GL11.glScalef(1.0F / getScaleFactor(), 1.0F / getScaleFactor(), 1.0F / getScaleFactor());
                GL11.glTranslatef(-x, -y, 0.0F);
                adjustedX = (int) ((float) adjustedX * getScaleFactor());
                adjustedY = (int) ((float) adjustedY * getScaleFactor());
            }
        }

        RenderSystem.enableBlend();
        GL11.glBlendFunc(770, 771);

        if (shadow) {
            font.drawString((float) Math.round(x + (float) adjustedX), (float) (Math.round(y + (float) adjustedY) + 2), text, new Color(0.0F, 0.0F, 0.0F, 0.35F));
        }

        if (text != null) {
            float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
            float red = (float) (color >> 16 & 0xFF) / 255.0F;
            float green = (float) (color >> 8 & 0xFF) / 255.0F;
            float blue = (float) (color & 0xFF) / 255.0F;

            font.drawString((float) Math.round(x + (float) adjustedX), (float) Math.round(y + (float) adjustedY), text, new Color(red, green, blue, alpha));
        }

        RenderSystem.disableBlend();
        GL11.glPopMatrix();
    }

    public static void drawFilledArc(float x, float y, float radius, int color) {
        drawFilledArc(x, y, 0.0F, 360.0F, radius - 1.0F, color);
    }

    public static void drawFilledArc(float x, float y, float startAngle, float endAngle, float radius, int color) {
        drawFilledArc(x, y, startAngle, endAngle, radius, radius, color);
    }

    public static void drawFilledArc(float x, float y, float startAngle, float endAngle, float hRadius, float vRadius, int color) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

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
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(red, green, blue, alpha);

        if (alpha > 0.5F) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0F);
            GL11.glBegin(3);

            for (float angle = endAngle; angle >= startAngle; angle -= 4.0F) {
                float radiusX = (float) Math.cos((double) angle * Math.PI / 180.0) * hRadius * 1.001F;
                float radiusY = (float) Math.sin((double) angle * Math.PI / 180.0) * vRadius * 1.001F;
                GL11.glVertex2f(x + radiusX, y + radiusY);
            }

            GL11.glEnd();
            GL11.glDisable(2848);
        }

        GL11.glBegin(6);

        for (float angle = endAngle; angle >= startAngle; angle -= 4.0F) {
            float radiusX = (float) Math.cos((double) angle * Math.PI / 180.0) * hRadius;
            float radiusY = (float) Math.sin((double) angle * Math.PI / 180.0) * vRadius;
            GL11.glVertex2f(x + radiusX, y + radiusY);
        }

        GL11.glEnd();
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
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);

        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder buffer = tesselator.getBuffer();

        buffer.begin(7, VertexFormats.POSITION_COLOR);
        buffer.vertex(width, y, 0.0).color(red1, green1, blue1, alpha1).next();
        buffer.vertex(x, y, 0.0).color(red1, green1, blue1, alpha1).next();
        buffer.vertex(x, height, 0.0).color(red2, green2, blue2, alpha2).next();
        buffer.vertex(width, height, 0.0).color(red2, green2, blue2, alpha2).next();
        tesselator.draw();

        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
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
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(red, green, blue, alpha);
        GL11.glBegin(6);
        GL11.glVertex2f(left, top);
        GL11.glVertex2f(tipX, tipY);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(left, top);
        GL11.glEnd();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawTexturedQuad(float x, float y, float width, float height, int color, float u, float v, float textureWidth, float textureHeight) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

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
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(red, green, blue, alpha);

        GL11.glEnable(3042);
        GL11.glEnable(3553);

        GL11.glPixelStorei(3312, 0);
        GL11.glPixelStorei(3313, 0);
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3315, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3317, 4);

        float texU = u / textureWidth;
        float texV = v / textureHeight;

        GL11.glBegin(7);
        GL11.glTexCoord2f(texU, texV);
        GL11.glVertex2f(x, y);

        GL11.glTexCoord2f(texU, texV + 1.0f);
        GL11.glVertex2f(x, y + height);

        GL11.glTexCoord2f(texU + 1.0f, texV + 1.0f);
        GL11.glVertex2f(x + width, y + height);

        GL11.glTexCoord2f(texU + 1.0f, texV);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();

        GL11.glDisable(3553);
        GL11.glDisable(3042);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawFloatingPanel(int x, int y, int width, int height, int color) {
        drawFloatingPanelClipped(x, y, width, height, color, x, y);
    }


    public static void drawFloatingPanelClipped(int x, int y, int width, int height, int color, int scissorX, int scissorY) {
        int tileSize = 36;
        int padding = 10;
        int innerOffset = tileSize - padding;

        drawRoundedRect(
                (float) (x + padding),
                (float) (y + padding),
                (float) (x + width - padding),
                (float) (y + height - padding),
                color
        );

        drawImage(
                (float) (x - innerOffset),
                (float) (y - innerOffset),
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );

        GL11.glPushMatrix();
        GL11.glTranslatef(x + width - tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-x - width - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
        drawImage(
                (float) (x + width - innerOffset),
                (float) (y - innerOffset),
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(x + width - tileSize / 2.0f, y + height + tileSize / 2.0f, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-x - width - tileSize / 2.0f, -y - height - tileSize / 2.0f, 0.0F);
        drawImage(
                (float) (x + width - innerOffset),
                (float) (y + padding + height),
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(x - tileSize / 2.0f, y + height + tileSize / 2.0f, 0.0F);
        GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-x - tileSize / 2.0f, -y - height - tileSize / 2.0f, 0.0F);
        drawImage(
                (float) (x + padding),
                (float) (y + padding + height),
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        GL11.glPopMatrix();

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
            GL11.glPushMatrix();
            GL11.glTranslatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
            drawImage(
                    (float) (x - innerOffset),
                    (float) (y - padding - offsetX),
                    (float) tileSize,
                    (float) tileSize,
                    Resources.FLOATING_BORDER,
                    color
            );
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();

        ScissorUtils.startScissorNoGL(
                scissorX + width - padding,
                scissorY - innerOffset,
                x + width + innerOffset,
                scissorY + height - padding
        );

        for (int offsetY = 0; offsetY < height; offsetY += tileSize) {
            GL11.glPushMatrix();
            GL11.glTranslatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
            drawImage(
                    (float) (x - width + padding),
                    (float) (y - padding - offsetY),
                    (float) tileSize,
                    (float) tileSize,
                    Resources.FLOATING_BORDER,
                    color
            );
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();

        ScissorUtils.startScissorNoGL(
                scissorX - padding,
                scissorY - innerOffset + height - tileSize,
                scissorX + width - padding,
                scissorY + height + padding * 2
        );

        for (int offsetX = 0; offsetX < width; offsetX += tileSize) {
            GL11.glPushMatrix();
            GL11.glTranslatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
            GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
            drawImage(
                    (float) (x - height + padding),
                    (float) (y + padding + offsetX),
                    (float) tileSize,
                    (float) tileSize,
                    Resources.FLOATING_BORDER,
                    color
            );
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();
    }

    public static void drawFloatingPanelScaled(int x, int y, int width, int height, int color) {
        int tileSize = 36;
        int padding = 10;
        int innerOffset = tileSize - padding;

        drawRoundedRect(
                (float) (x + padding),
                (float) (y + padding),
                (float) (x + width - padding),
                (float) (y + height - padding),
                color
        );

        drawImage(
                (float) (x - innerOffset),
                (float) (y - innerOffset),
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );

        GL11.glPushMatrix();
        GL11.glTranslatef(x + width - tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-x - width - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
        drawImage(
                (float) (x + width - innerOffset),
                (float) (y - innerOffset),
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(x + width - tileSize / 2.0f, y + height + tileSize / 2.0f, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-x - width - tileSize / 2.0f, -y - height - tileSize / 2.0f, 0.0F);
        drawImage(
                (float) (x + width - innerOffset),
                (float) (y + padding + height),
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(x - tileSize / 2.0f, y + height + tileSize / 2.0f, 0.0F);
        GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-x - tileSize / 2.0f, -y - height - tileSize / 2.0f, 0.0F);
        drawImage(
                (float) (x + padding),
                (float) (y + padding + height),
                (float) tileSize,
                (float) tileSize,
                Resources.FLOATING_CORNER,
                color
        );
        GL11.glPopMatrix();

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
            GL11.glPushMatrix();
            GL11.glTranslatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
            drawImage(
                    (float) (x - innerOffset),
                    (float) (y - padding - offsetX) - 0.4F,
                    (float) tileSize,
                    (float) tileSize + 0.4F,
                    Resources.FLOATING_BORDER,
                    color
            );
            GL11.glPopMatrix();
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
            GL11.glPushMatrix();
            GL11.glTranslatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
            drawImage(
                    (float) (x - width + padding),
                    (float) (y - padding - offsetY) - 0.4F,
                    (float) tileSize,
                    (float) tileSize + 0.4F,
                    Resources.FLOATING_BORDER,
                    color
            );
            GL11.glPopMatrix();
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
            GL11.glPushMatrix();
            GL11.glTranslatef(x + tileSize / 2.0f, y + tileSize / 2.0f, 0.0F);
            GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-x - tileSize / 2.0f, -y - tileSize / 2.0f, 0.0F);
            drawImage(
                    (float) (x - height + padding),
                    (float) (y + padding + offsetX) - 0.4F,
                    (float) tileSize,
                    (float) tileSize + 0.4F,
                    Resources.FLOATING_BORDER,
                    color
            );
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();
    }

    public static void drawPanelShadow(float x, float y, float width, float height, float shadowSize, float alpha) {
        int shadowColor = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha);

        drawImage(x, y, shadowSize, height, Resources.SHADOW_RIGHT, shadowColor, false);
        drawImage(x + width - shadowSize, y, shadowSize, height, Resources.SHADOW_LEFT, shadowColor, false);
        drawImage(x, y, width, shadowSize, Resources.SHADOW_BOTTOM, shadowColor, false);
        drawImage(x, y + height - shadowSize, width, shadowSize, Resources.SHADOW_TOP, shadowColor, false);
    }

    public static void renderItemStack(ItemStack stack, int x, int y, int width, int height) {
        if (stack != null) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);

            GL11.glPushMatrix();
            GL11.glTranslatef((float) x, (float) y, 0.0F);
            GL11.glScalef((float) width / 16.0F, (float) height / 16.0F, 0.0F);

            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            if (stack.getCount() == 0) {
                stack = new ItemStack(stack.getItem());
            }

            RenderHelper.setupGuiFlatDiffuseLighting();
            GL11.glLightModelfv(2899, new float[]{0.4F, 0.4F, 0.4F, 1.0F});

            RenderSystem.enableColorMaterial();
            RenderSystem.disableLighting();
            RenderSystem.enableBlend();

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDepthFunc(519);

            itemRenderer.renderInGui(stack, 0, 0);

            GL11.glDepthFunc(515);
            RenderSystem.popMatrix();

            GL11.glAlphaFunc(519, 0.0F);
            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
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
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO
        );
        RenderSystem.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, VertexFormats.POSITION_COLOR);

        buffer.vertex(x2, y1, 0.0).color(r2, g2, b2, a2).next();
        buffer.vertex(x1, y1, 0.0).color(r1, g1, b1, a1).next();
        buffer.vertex(x1, y2, 0.0).color(r4, g4, b4, a4).next();
        buffer.vertex(x2, y2, 0.0).color(r3, g3, b3, a3).next();

        tessellator.draw();

        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
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

    public static void drawTexturedQuad(
            float x,
            float y,
            float width,
            float height,
            ByteBuffer pixelBuffer,
            int color,
            float textureOffsetX,
            float textureOffsetY,
            float textureWidth,
            float textureHeight,
            boolean flipX,
            boolean flipY
    ) {
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
        RenderSystem.color4f(red, green, blue, alpha);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        if (dynamicPixelTextureId == -1) {
            dynamicPixelTextureId = GL11.glGenTextures();
            dynamicPixelTextureWidth = -1;
            dynamicPixelTextureHeight = -1;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, dynamicPixelTextureId);

        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
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

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(u + (flipX ? uMax : 0.0F), v + (flipY ? vMax : 0.0F));
        GL11.glVertex2f(x, y);

        GL11.glTexCoord2f(u + (flipX ? uMax : 0.0F), v + (flipY ? 0.0F : vMax));
        GL11.glVertex2f(x, y + height);

        GL11.glTexCoord2f(u + (flipX ? 0.0F : uMax), v + (flipY ? 0.0F : vMax));
        GL11.glVertex2f(x + width, y + height);

        GL11.glTexCoord2f(u + (flipX ? 0.0F : uMax), v + (flipY ? vMax : 0.0F));
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRotatedTriangle() {
        GL11.glTranslatef(0.0F, 0.0F, 0.3F);
        GL11.glNormal3f(0.0F, 0.0F, 1.0F);
        GL11.glRotated(-37.0, 1.0, 0.0, 0.0);

        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(0.0F, 0.4985F);
        GL11.glVertex2f(-0.3F, 0.0F);
        GL11.glVertex2f(0.3F, 0.0F);
        GL11.glEnd();
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

        RenderSystem.color4f(red, green, blue, alpha);

        buffer.begin(7, VertexFormats.POSITION);
        buffer.vertex(x1, y2, 0.0).next();
        buffer.vertex(x2, y2, 0.0).next();
        buffer.vertex(x2, y1, 0.0).next();
        buffer.vertex(x1, y1, 0.0).next();

        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawColoredRotatedTriangle(int color) {
        GL11.glColor4fv(ColorHelper.unpackColorToRGBA(color));
        drawRotatedTriangle();
    }

    public static void drawCircleOutline(float radius) {
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int angle = 0; angle < 360; angle++) {
            double rad = angle * Math.PI / 180.0;
            GL11.glVertex2d(Math.cos(rad) * radius, Math.sin(rad) * radius);
        }
        GL11.glEnd();
    }

    public static void drawFilledCircle(float radius) {
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        for (int angle = 0; angle < 360; angle++) {
            double rad = angle * Math.PI / 180.0;
            GL11.glVertex2d(Math.cos(rad) * radius, Math.sin(rad) * radius);
        }
        GL11.glEnd();
    }

    public static void drawRotatingTriangles(int color) {
        for (int angle = 0; angle <= 270; angle += 90) {
            GL11.glPushMatrix();
            GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            drawColoredRotatedTriangle(ColorHelper.blendColors(ClientColors.DEEP_TEAL.getColor(), color, 0.04F * angle / 90.0F));
            GL11.glPopMatrix();
        }

        for (int angle = 0; angle <= 270; angle += 90) {
            GL11.glPushMatrix();
            GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);
            drawColoredRotatedTriangle(ColorHelper.blendColors(ClientColors.DEEP_TEAL.getColor(), color, 0.04F * angle / 90.0F));
            GL11.glPopMatrix();
        }
    }

    public static void drawWaypointIndicator(float x, float y, float z, String label, int color, float scale) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDisable(2896);
        GL11.glDepthMask(false);

        // Draw shadow circle
        GL11.glPushMatrix();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.114F);
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(90.0F, -1.0F, 0.0F, 0.0F);
        drawFilledCircle(0.5F);
        GL11.glPopMatrix();

        // Draw rotating outline circle
        GL11.glPushMatrix();
        GL11.glColor4fv(ColorHelper.unpackColorToRGBA(color));
        GL11.glTranslated(x + 0.5, y + 0.7F, z + 0.5);
        GL11.glRotatef((float) (client.player.age % 90 * 4), 0.0F, -1.0F, 0.0F);
        GL11.glLineWidth(1.4F + 1.4F / scale);
        drawCircleOutline(0.6F);
        GL11.glPopMatrix();

        // Draw rotating triangles
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.7F, z + 0.5);
        GL11.glRotatef((float) (client.player.age % 90 * 4), 0.0F, 1.0F, 0.0F);
        drawRotatingTriangles(color);
        GL11.glPopMatrix();

        // Draw label background and text
        GL11.glPushMatrix();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glTranslated(x + 0.5, y + 1.9, z + 0.5);
        GL11.glRotatef(client.gameRenderer.getCamera().getYaw(), 0.0F, -1.0F, 0.0F);
        GL11.glRotatef(client.gameRenderer.getCamera().getPitch(), 1.0F, 0.0F, 0.0F);

        TrueTypeFont font = FontUtils.HELVETICA_LIGHT_25;

        GL11.glPushMatrix();
        GL11.glScalef(-0.009F * scale, -0.009F * scale, -0.009F * scale);
        GL11.glTranslated(0.0, -20.0 * Math.sqrt(Math.sqrt(scale)), 0.0);

        int bgColor = ColorHelper.applyAlpha(
                ColorHelper.blendColors(ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                        ClientColors.DEEP_TEAL.getColor(), 75.0F),
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

        GL11.glTranslated((double) -font.getWidth(label) / 2, 0.0, 0.0);
        drawString(font, 0.0F, 0.0F, label, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F));

        GL11.glPopMatrix();
        GL11.glPopMatrix();

        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glEnable(2896);
        GL11.glDisable(2848);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void drawRect(float x, float y, float width, float height, int color) {
        drawColoredRect(x, y, x + width, y + height, color);
    }

    public static void drawTargetIndicatorRing(java.awt.Color baseColor, Entity target, float progress) {
        GL11.glPushMatrix();
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glEnable(32925);
        GL11.glEnable(2929);
        GL11.glLineWidth(1.4F);

        double tickDelta = client.getTickDelta();
        if (!target.isAlive()) {
            tickDelta = 0.0;
        }

        GL11.glTranslated(
                target.prevX + (target.getX() - target.prevX) * tickDelta,
                target.prevY + (target.getY() - target.prevY) * tickDelta,
                target.prevZ + (target.getZ() - target.prevZ) * tickDelta);
        GL11.glTranslated(
                -client.gameRenderer.getCamera().getPos().getX(),
                -client.gameRenderer.getCamera().getPos().getY(),
                -client.gameRenderer.getCamera().getPos().getZ());
        GL11.glEnable(32823);
        GL11.glEnable(3008);
        GL11.glEnable(3042);
        GL11.glAlphaFunc(519, 0.0F);

        long animationPeriodMs = 1800;
        float phase = (float) (System.currentTimeMillis() % animationPeriodMs) / (float) animationPeriodMs;
        boolean reverseGradient = phase > 0.5F;

        phase = !reverseGradient ? phase * 2.0F : 1.0F - phase * 2.0F % 1.0F;

        GL11.glTranslatef(0.0F, (target.getHeight() + 0.4F) * phase, 0.0F);
        float pulse = (float) Math.sin((double) phase * Math.PI);
        drawAnimatedRing(baseColor, reverseGradient, 0.45F * pulse, 0.6F, 0.35F * pulse, progress);
        GL11.glPushMatrix();
        GL11.glTranslated(
                client.gameRenderer.getCamera().getPos().getX(),
                client.gameRenderer.getCamera().getPos().getY(),
                client.gameRenderer.getCamera().getPos().getZ());
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(32925);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
    }

    public static void drawAnimatedRing(java.awt.Color baseColor, boolean reverseGradient, float ringHeight, float ringRadius, float ringAlphaScale, float progressAlpha) {
        RenderSystem.shadeModel(7425);
        GL11.glDisable(32823);
        GL11.glDisable(2929);
        GL11.glBegin(5);
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
            GL11.glColor4f(red, green, blue, !reverseGradient ? 0.0F : ringAlphaScale * progressAlpha);
            GL11.glVertex3d(x, 0.0, z);
            GL11.glColor4f(red, green, blue, reverseGradient ? 0.0F : ringAlphaScale * progressAlpha);
            GL11.glVertex3d(x, ringHeight, z);
        }

        GL11.glEnd();
        GL11.glLineWidth(2.2F);
        GL11.glBegin(3);

        for (int angle = 0; angle <= 360 + angleStep; angle += angleStep) {
            int clampedAngle = angle;
            if (angle > 360) {
                clampedAngle = 0;
            }

            double x = Math.sin((double) clampedAngle * Math.PI / 180.0) * (double) ringRadius;
            double z = Math.cos((double) clampedAngle * Math.PI / 180.0) * (double) ringRadius;
            GL11.glColor4f(red, green, blue, (0.5F + 0.5F * ringAlphaScale) * progressAlpha);
            GL11.glVertex3d(x, !reverseGradient ? (double) ringHeight : 0.0, z);
        }

        GL11.glEnd();
        GL11.glEnable(2929);
        RenderSystem.shadeModel(7424);
    }
}
