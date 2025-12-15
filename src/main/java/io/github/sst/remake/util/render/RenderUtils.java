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
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;

import java.nio.FloatBuffer;

public class RenderUtils {

    private static float getScaleFactor() {
        return Client.INSTANCE.screenManager.scaleFactor;
    }

    public static void drawRoundedRect(float x, float y, float sizedX, float sizedY, int color) {
        if (x < sizedX) {
            int var7 = (int) x;
            x = sizedX;
            sizedX = (float) var7;
        }

        if (y < sizedY) {
            int var13 = (int) y;
            y = sizedY;
            sizedY = (float) var13;
        }

        float a = (float) (color >> 24 & 0xFF) / 255.0F;
        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;
        Tessellator tessel = Tessellator.getInstance();
        BufferBuilder buffer = tessel.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.color4f(r, g, b, a);
        buffer.begin(7, VertexFormats.POSITION);
        buffer.vertex(x, sizedY, 0.0).next();
        buffer.vertex(sizedX, sizedY, 0.0).next();
        buffer.vertex(sizedX, y, 0.0).next();
        buffer.vertex(x, y, 0.0).next();
        tessel.draw();
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

    public static void drawRoundedRect(float var0, float var1, float var2, float var3, float var4, float var5) {
        GL11.glAlphaFunc(519, 0.0F);
        int var8 = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5);
        drawImage(var0 - var4, var1 - var4, var4, var4, Resources.shadowCorner1PNG, var8);
        drawImage(var0 + var2, var1 - var4, var4, var4, Resources.shadowCorner2PNG, var8);
        drawImage(var0 - var4, var1 + var3, var4, var4, Resources.shadowCorner3PNG, var8);
        drawImage(var0 + var2, var1 + var3, var4, var4, Resources.shadowCorner4PNG, var8);
        drawImage(var0 - var4, var1, var4, var3, Resources.shadowLeftPNG, var8, false);
        drawImage(var0 + var2, var1, var4, var3, Resources.shadowRightPNG, var8, false);
        drawImage(var0, var1 - var4, var2, var4, Resources.shadowTopPNG, var8, false);
        drawImage(var0, var1 + var3, var2, var4, Resources.shadowBottomPNG, var8, false);
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

    /**
     * Draws a sub-image of a texture to the screen.
     *
     * @param x               The x-coordinate of the top-left corner of the image.
     * @param y               The y-coordinate of the top-left corner of the image.
     * @param width           The width of the image.
     * @param height          The height of the image.
     * @param texture         The texture to draw from.
     * @param color           The color to draw the image in, represented as an integer.
     * @param tlX             The x-coordinate of the top-left corner of the sub-image within the texture.
     * @param tlY             The y-coordinate of the top-left corner of the sub-image within the texture.
     * @param siW             The width of the sub-image.
     * @param siH             The height of the sub-image.
     * @param linearFiltering Whether to use linear filtering for the texture.
     */
    public static void drawImage(float x, float y, float width, float height, Texture texture, int color, float tlX, float tlY, float siW, float siH, boolean linearFiltering) {
        if (texture != null) {
            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
            x = (float) Math.round(x);
            width = (float) Math.round(width);
            y = (float) Math.round(y);
            height = (float) Math.round(height);
            float a = (float) (color >> 24 & 0xFF) / 255.0F;
            float r = (float) (color >> 16 & 0xFF) / 255.0F;
            float g = (float) (color >> 8 & 0xFF) / 255.0F;
            float b = (float) (color & 0xFF) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.color4f(r, g, b, a);
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

    public static void drawRoundedButton(float var0, float var1, float var2, float var3, float var4, int color) {
        drawRoundedRect(var0, var1 + var4, var0 + var2, var1 + var3 - var4, color);
        drawRoundedRect(var0 + var4, var1, var0 + var2 - var4, var1 + var3, color);
        FloatBuffer var8 = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, var8);
        float var9 = 1.0F;
        drawCircle(var0 + var4, var1 + var4, var4 * 2.0F * var9, color);
        drawCircle(var0 - var4 + var2, var1 + var4, var4 * 2.0F * var9, color);
        drawCircle(var0 + var4, var1 - var4 + var3, var4 * 2.0F * var9, color);
        drawCircle(var0 - var4 + var2, var1 - var4 + var3, var4 * 2.0F * var9, color);
    }

    public static void drawCircle(float centerX, float centerY, float size, int color) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float a = (float) (color >> 24 & 0xFF) / 255.0F;
        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;
        Tessellator var10 = Tessellator.getInstance();
        BufferBuilder var11 = var10.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(r, g, b, a);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glPointSize(size * getScaleFactor());
        GL11.glBegin(0);
        GL11.glVertex2f(centerX, centerY);
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
        int adjustedWidth = 0;
        int adjustedHeight = 0;

        switch (widthAlignment) {
            case CENTER:
                adjustedWidth = -font.getWidth(text) / 2;
                break;

            case RIGHT:
                adjustedWidth = -font.getWidth(text);
                break;

            default:
                break;
        }

        switch (heightAlignment) {
            case CENTER:
                adjustedHeight = -font.getHeight(text) / 2;
                break;

            case BOTTOM:
                adjustedHeight = -font.getHeight(text);
                break;

            default:
                break;
        }

        float var12 = (float) (color >> 24 & 0xFF) / 255.0F;
        float var13 = (float) (color >> 16 & 0xFF) / 255.0F;
        float var14 = (float) (color >> 8 & 0xFF) / 255.0F;
        float var15 = (float) (color & 0xFF) / 255.0F;
        GL11.glPushMatrix();
        boolean var16 = false;
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
                var16 = true;
            }

            if (!var16) {
                GL11.glTranslatef(x, y, 0.0F);
                GL11.glScalef(1.0F / getScaleFactor(), 1.0F / getScaleFactor(), 1.0F / getScaleFactor());
                GL11.glTranslatef(-x, -y, 0.0F);
                adjustedWidth = (int) ((float) adjustedWidth * getScaleFactor());
                adjustedHeight = (int) ((float) adjustedHeight * getScaleFactor());
            }
        }

        RenderSystem.enableBlend();
        GL11.glBlendFunc(770, 771);

        if (shadow) {
            font.drawString((float) Math.round(x + (float) adjustedWidth), (float) (Math.round(y + (float) adjustedHeight) + 2), text, new Color(0.0F, 0.0F, 0.0F, 0.35F));
        }

        if (text != null) {
            font.drawString((float) Math.round(x + (float) adjustedWidth), (float) Math.round(y + (float) adjustedHeight), text, new Color(var13, var14, var15, var12));
        }

        RenderSystem.disableBlend();
        GL11.glPopMatrix();
    }

    public static void drawFilledArc(float var0, float var1, float var2, int var3) {
        drawFilledArc(var0, var1, 0.0F, 360.0F, var2 - 1.0F, var3);
    }

    public static void drawFilledArc(float var0, float var1, float var2, float var3, float var4, int var5) {
        drawFilledArc(var0, var1, var2, var3, var4, var4, var5);
    }

    /**
     * Draws a filled arc with the specified center, radii, start and end angles, and color.
     *
     * @param x          The x-coordinate of the center of the arc.
     * @param y          The y-coordinate of the center of the arc.
     * @param startAngle The start angle of the arc in degrees.
     * @param endAngle   The end angle of the arc in degrees.
     * @param hRadius    The horizontal radius of the arc.
     * @param vRadius    The vertical radius of the arc.
     * @param color      The color of the arc in ARGB format.
     */
    public static void drawFilledArc(float x, float y, float startAngle, float endAngle, float hRadius, float vRadius, int color) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float var9 = 0.0F;
        if (startAngle > endAngle) {
            var9 = endAngle;
            endAngle = startAngle;
            startAngle = var9;
        }

        float var10 = (float) (color >> 24 & 0xFF) / 255.0F;
        float var11 = (float) (color >> 16 & 0xFF) / 255.0F;
        float var12 = (float) (color >> 8 & 0xFF) / 255.0F;
        float var13 = (float) (color & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(var11, var12, var13, var10);
        if (var10 > 0.5F) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0F);
            GL11.glBegin(3);

            for (float var16 = endAngle; var16 >= startAngle; var16 -= 4.0F) {
                float var17 = (float) Math.cos((double) var16 * Math.PI / 180.0) * hRadius * 1.001F;
                float var18 = (float) Math.sin((double) var16 * Math.PI / 180.0) * vRadius * 1.001F;
                GL11.glVertex2f(x + var17, y + var18);
            }

            GL11.glEnd();
            GL11.glDisable(2848);
        }

        GL11.glBegin(6);

        for (float var20 = endAngle; var20 >= startAngle; var20 -= 4.0F) {
            float var21 = (float) Math.cos((double) var20 * Math.PI / 180.0) * hRadius;
            float var22 = (float) Math.sin((double) var20 * Math.PI / 180.0) * vRadius;
            GL11.glVertex2f(x + var21, y + var22);
        }

        GL11.glEnd();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void method11431(int var0, int var1, int var2, int var3, int var4, int var5) {
        float var8 = (float) (var4 >> 24 & 0xFF) / 255.0F;
        float var9 = (float) (var4 >> 16 & 0xFF) / 255.0F;
        float var10 = (float) (var4 >> 8 & 0xFF) / 255.0F;
        float var11 = (float) (var4 & 0xFF) / 255.0F;
        float var12 = (float) (var5 >> 24 & 0xFF) / 255.0F;
        float var13 = (float) (var5 >> 16 & 0xFF) / 255.0F;
        float var14 = (float) (var5 >> 8 & 0xFF) / 255.0F;
        float var15 = (float) (var5 & 0xFF) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        Tessellator var16 = Tessellator.getInstance();
        BufferBuilder var17 = var16.getBuffer();
        var17.begin(7, VertexFormats.POSITION_COLOR);
        var17.vertex(var2, var1, 0.0).color(var9, var10, var11, var8).next();
        var17.vertex(var0, var1, 0.0).color(var9, var10, var11, var8).next();
        var17.vertex(var0, var3, 0.0).color(var13, var14, var15, var12).next();
        var17.vertex(var2, var3, 0.0).color(var13, var14, var15, var12).next();
        var16.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void method11429(float var0, float var1, float var2, float var3, int var4, int var5) {
        drawRoundedRect(var0, var3 - (float) var4, var2 - (float) var4, var3, var5);
        drawRoundedRect(var0, var1, var2 - (float) var4, var1 + (float) var4, var5);
        drawRoundedRect(var0, var1 + (float) var4, var0 + (float) var4, var3 - (float) var4, var5);
        drawRoundedRect(var2 - (float) var4, var1, var2, var3, var5);
    }

    public static void method11428(float var0, float var1, float var2, float var3, int var4) {
        method11429(var0, var1, var2, var3, 1, var4);
    }

    public static void method11434(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float var9 = (float) (var6 >> 24 & 0xFF) / 255.0F;
        float var10 = (float) (var6 >> 16 & 0xFF) / 255.0F;
        float var11 = (float) (var6 >> 8 & 0xFF) / 255.0F;
        float var12 = (float) (var6 & 0xFF) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.color4f(var10, var11, var12, var9);
        GL11.glBegin(6);
        GL11.glVertex2f(var0, var1);
        GL11.glVertex2f(var4, var5);
        GL11.glVertex2f(var2, var3);
        GL11.glVertex2f(var0, var1);
        GL11.glEnd();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

}
