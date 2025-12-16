package io.github.sst.remake.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.image.ResourceRegistry;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;

public class RenderUtils {

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

        drawImage(x - radius, y - radius, radius, radius, Resources.shadowCorner1PNG, color);
        drawImage(x + width, y - radius, radius, radius, Resources.shadowCorner2PNG, color);
        drawImage(x - radius, y + height, radius, radius, Resources.shadowCorner3PNG, color);
        drawImage(x + width, y + height, radius, radius, Resources.shadowCorner4PNG, color);
        drawImage(x - radius, y, radius, height, Resources.shadowLeftPNG, color, false);
        drawImage(x + width, y, radius, height, Resources.shadowRightPNG, color, false);
        drawImage(x, y - radius, width, radius, Resources.shadowTopPNG, color, false);
        drawImage(x, y + height, width, radius, Resources.shadowBottomPNG, color, false);
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
            if (font == ResourceRegistry.JelloLightFont20) {
                font = ResourceRegistry.JelloLightFont40;
            } else if (font == ResourceRegistry.JelloLightFont25) {
                font = ResourceRegistry.JelloLightFont50;
            } else if (font == ResourceRegistry.JelloLightFont12) {
                font = ResourceRegistry.JelloLightFont24;
            } else if (font == ResourceRegistry.JelloLightFont14) {
                font = ResourceRegistry.JelloLightFont28;
            } else if (font == ResourceRegistry.JelloLightFont18) {
                font = ResourceRegistry.JelloLightFont36;
            } else if (font == ResourceRegistry.RegularFont20) {
                font = ResourceRegistry.RegularFont40;
            } else if (font == ResourceRegistry.JelloMediumFont20) {
                font = ResourceRegistry.JelloMediumFont40;
            } else if (font == ResourceRegistry.JelloMediumFont25) {
                font = ResourceRegistry.JelloMediumFont50;
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

    public static void drawSprite(float var0, float var1, float var2, float var3, int var4, float var5, float var6, float var7, float var8) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        var0 = (float) Math.round(var0);
        var2 = (float) Math.round(var2);
        var1 = (float) Math.round(var1);
        var3 = (float) Math.round(var3);
        float var11 = (float) (var4 >> 24 & 0xFF) / 255.0F;
        float var12 = (float) (var4 >> 16 & 0xFF) / 255.0F;
        float var13 = (float) (var4 >> 8 & 0xFF) / 255.0F;
        float var14 = (float) (var4 & 0xFF) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(var12, var13, var14, var11);
        GL11.glEnable(3042);
        GL11.glEnable(3553);
        GL11.glPixelStorei(3312, 0);
        GL11.glPixelStorei(3313, 0);
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3315, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3317, 4);

        float var16 = 1.0f;
        float var17 = 1.0f;
        float var18 = var5 / var7;
        float var19 = var6 / var8;

        GL11.glBegin(7);
        GL11.glTexCoord2f(var18, var19);
        GL11.glVertex2f(var0, var1);
        GL11.glTexCoord2f(var18, var19 + var17);
        GL11.glVertex2f(var0, var1 + var3);
        GL11.glTexCoord2f(var18 + var16, var19 + var17);
        GL11.glVertex2f(var0 + var2, var1 + var3);
        GL11.glTexCoord2f(var18 + var16, var19);
        GL11.glVertex2f(var0 + var2, var1);
        GL11.glEnd();
        GL11.glDisable(3553);
        GL11.glDisable(3042);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void method11465(int var0, int var1, int var2, int var3, int var4) {
        method11466(var0, var1, var2, var3, var4, var0, var1);
    }

    public static void method11466(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        int var9 = 36;
        int var10 = 10;
        int var11 = var9 - var10;
        drawRoundedRect((float) (var0 + var10), (float) (var1 + var10), (float) (var0 + var2 - var10), (float) (var1 + var3 - var10), var4);
        drawImage((float) (var0 - var11), (float) (var1 - var11), (float) var9, (float) var9, Resources.floatingCornerPNG, var4);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 + var2 - var9 / 2), (float) (var1 + var9 / 2), 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var2 - var9 / 2), (float) (-var1 - var9 / 2), 0.0F);
        drawImage((float) (var0 + var2 - var11), (float) (var1 - var11), (float) var9, (float) var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 + var2 - var9 / 2), (float) (var1 + var3 + var9 / 2), 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var2 - var9 / 2), (float) (-var1 - var3 - var9 / 2), 0.0F);
        drawImage((float) (var0 + var2 - var11), (float) (var1 + var10 + var3), (float) var9, (float) var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 - var9 / 2), (float) (var1 + var3 + var9 / 2), 0.0F);
        GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var9 / 2), (float) (-var1 - var3 - var9 / 2), 0.0F);
        drawImage((float) (var0 + var10), (float) (var1 + var10 + var3), (float) var9, (float) var9, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        ScissorUtils.startScissor(var5 - var9, var6 + var10, var5 - var11 + var9, var6 - var10 + var3);

        for (int var12 = 0; var12 < var3; var12 += var9) {
            drawImage((float) (var0 - var11), (float) (var1 + var10 + var12), (float) var9, (float) var9, Resources.floatingBorderPNG, var4);
        }

        ScissorUtils.restoreScissor();
        ScissorUtils.startScissor(var5, var6 - var11, var5 + var2 - var10, var6 + var10);

        for (int var13 = 0; var13 < var2; var13 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var9 / 2), (float) (var1 + var9 / 2), 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var9 / 2), (float) (-var1 - var9 / 2), 0.0F);
            drawImage((float) (var0 - var11), (float) (var1 - var10 - var13), (float) var9, (float) var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();
        ScissorUtils.startScissor(var5 + var2 - var10, var6 - var11, var0 + var2 + var11, var6 + var3 - var10);

        for (int var14 = 0; var14 < var3; var14 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var9 / 2), (float) (var1 + var9 / 2), 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var9 / 2), (float) (-var1 - var9 / 2), 0.0F);
            drawImage((float) (var0 - var2 + var10), (float) (var1 - var10 - var14), (float) var9, (float) var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();
        ScissorUtils.startScissor(var5 - var10, var6 - var11 + var3 - var9, var5 + var2 - var10, var6 + var3 + var10 * 2);

        for (int var15 = 0; var15 < var2; var15 += var9) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var9 / 2), (float) (var1 + var9 / 2), 0.0F);
            GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var9 / 2), (float) (-var1 - var9 / 2), 0.0F);
            drawImage((float) (var0 - var3 + var10), (float) (var1 + var10 + var15), (float) var9, (float) var9, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();
    }

    public static void method11467(int var0, int var1, int var2, int var3, int var4) {
        int var7 = 36;
        int var8 = 10;
        int var9 = var7 - var8;
        drawRoundedRect((float) (var0 + var8), (float) (var1 + var8), (float) (var0 + var2 - var8), (float) (var1 + var3 - var8), var4);
        drawImage((float) (var0 - var9), (float) (var1 - var9), (float) var7, (float) var7, Resources.floatingCornerPNG, var4);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 + var2 - var7 / 2), (float) (var1 + var7 / 2), 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var2 - var7 / 2), (float) (-var1 - var7 / 2), 0.0F);
        drawImage((float) (var0 + var2 - var9), (float) (var1 - var9), (float) var7, (float) var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 + var2 - var7 / 2), (float) (var1 + var3 + var7 / 2), 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var2 - var7 / 2), (float) (-var1 - var3 - var7 / 2), 0.0F);
        drawImage((float) (var0 + var2 - var9), (float) (var1 + var8 + var3), (float) var7, (float) var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (var0 - var7 / 2), (float) (var1 + var3 + var7 / 2), 0.0F);
        GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-var0 - var7 / 2), (float) (-var1 - var3 - var7 / 2), 0.0F);
        drawImage((float) (var0 + var8), (float) (var1 + var8 + var3), (float) var7, (float) var7, Resources.floatingCornerPNG, var4);
        GL11.glPopMatrix();
        ScissorUtils.startScissor(var0 - var7, var1 + var8, var0 - var9 + var7, var1 - var8 + var3, true);

        for (int var10 = 0; var10 < var3; var10 += var7) {
            drawImage((float) (var0 - var9), (float) (var1 + var8 + var10) - 0.4F, (float) var7, (float) var7 + 0.4F, Resources.floatingBorderPNG, var4);
        }

        ScissorUtils.restoreScissor();
        ScissorUtils.startScissor(var0, var1 - var9, var0 + var2 - var8, var1 + var8, true);

        for (int var11 = 0; var11 < var2; var11 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var7 / 2), (float) (var1 + var7 / 2), 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var7 / 2), (float) (-var1 - var7 / 2), 0.0F);
            drawImage((float) (var0 - var9), (float) (var1 - var8 - var11) - 0.4F, (float) var7, (float) var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();
        ScissorUtils.startScissor(var0 + var2 - var8, var1 - var9, var0 + var2 + var9, var1 + var3 - var8, true);

        for (int var12 = 0; var12 < var3; var12 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var7 / 2), (float) (var1 + var7 / 2), 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var7 / 2), (float) (-var1 - var7 / 2), 0.0F);
            drawImage((float) (var0 - var2 + var8), (float) (var1 - var8 - var12) - 0.4F, (float) var7, (float) var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();
        ScissorUtils.startScissor(var0 - var8, var1 - var9 + var3 - var7, var0 + var2 - var8, var1 + var3 + var8 * 2, true);

        for (int var13 = 0; var13 < var2; var13 += var7) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (var0 + var7 / 2), (float) (var1 + var7 / 2), 0.0F);
            GL11.glRotatef(270.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-var0 - var7 / 2), (float) (-var1 - var7 / 2), 0.0F);
            drawImage((float) (var0 - var3 + var8), (float) (var1 + var8 + var13) - 0.4F, (float) var7, (float) var7 + 0.4F, Resources.floatingBorderPNG, var4);
            GL11.glPopMatrix();
        }

        ScissorUtils.restoreScissor();
    }

    public static void drawShadow(float var0, float var1, float var2, float var3, float var4, float var5) {
        int var8 = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5);
        drawImage(var0, var1, var4, var3, Resources.shadowRightPNG, var8, false);
        drawImage(var0 + var2 - var4, var1, var4, var3, Resources.shadowLeftPNG, var8, false);
        drawImage(var0, var1, var2, var4, Resources.shadowBottomPNG, var8, false);
        drawImage(var0, var1 + var3 - var4, var2, var4, Resources.shadowTopPNG, var8, false);
    }

}
