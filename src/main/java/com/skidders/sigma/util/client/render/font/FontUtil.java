package com.skidders.sigma.util.client.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.SigmaReborn;
import com.skidders.sigma.util.client.interfaces.IFonts;
import com.skidders.sigma.util.client.render.Loader;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

import java.awt.*;
import java.io.InputStream;

public class FontUtil {

    public static void drawString(TrueTypeFont font, String text, float x, float y, int color) {
        drawString(font, x, y, text, color, FontSizeAdjust.field14488, FontSizeAdjust.field14489, false);
    }

    public static void drawString(TrueTypeFont font, String text, float x, float y, java.awt.Color color) {
        drawString(font, x, y, text, color.getRGB(), FontSizeAdjust.field14488, FontSizeAdjust.field14489, false);
    }

    public static void drawString(TrueTypeFont font, float x, float y, String text, int color) {
        drawString(font, x, y, text, color, FontSizeAdjust.field14488, FontSizeAdjust.field14489, false);
    }

    public static void drawString(TrueTypeFont font, float x, float y, String text, int color, FontSizeAdjust widthAdjust, FontSizeAdjust heightAdjust, boolean var7) {
        RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        int adjustedWidth = 0;
        int adjustedHeight = 0;
        adjustedWidth = switch (widthAdjust) {
            case NEGATE_AND_DIVIDE_BY_2 -> -font.getWidth(text) / 2;
            case WIDTH_NEGATE -> -font.getWidth(text);
            default -> adjustedWidth;
        };

        adjustedHeight = switch (heightAdjust) {
            case NEGATE_AND_DIVIDE_BY_2 -> -font.getHeight(text) / 2;
            case HEIGHT_NEGATE -> -font.getHeight(text);
            default -> adjustedHeight;
        };

        float var12 = (float) (color >> 24 & 0xFF) / 255.0F;
        float var13 = (float) (color >> 16 & 0xFF) / 255.0F;
        float var14 = (float) (color >> 8 & 0xFF) / 255.0F;
        float var15 = (float) (color & 0xFF) / 255.0F;
        GL11.glPushMatrix();
        boolean var16 = false;
        if ((double) SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor == 2.0) {
            if (font == IFonts.JelloLightFont20) {
                font = IFonts.JelloLightFont40;
            } else if (font == IFonts.JelloLightFont25) {
                font = IFonts.JelloLightFont50;
            } else if (font == IFonts.JelloLightFont12) {
                font = IFonts.JelloLightFont24;
            } else if (font == IFonts.JelloLightFont14) {
                font = IFonts.JelloLightFont28;
            } else if (font == IFonts.JelloLightFont18) {
                font = IFonts.JelloLightFont36;
            } else if (font == IFonts.RegularFont20) {
                font = IFonts.RegularFont40;
            } else if (font == IFonts.JelloMediumFont20) {
                font = IFonts.JelloMediumFont40;
            } else if (font == IFonts.JelloMediumFont25) {
                font = IFonts.JelloMediumFont50;
            } else {
                var16 = true;
            }

            if (!var16) {
                GL11.glTranslatef(x, y, 0.0F);
                GL11.glScalef(1.0F / SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor, 1.0F / SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor, 1.0F / SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor);
                GL11.glTranslatef(-x, -y, 0.0F);
                adjustedWidth = (int) ((float) adjustedWidth * SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor);
                adjustedHeight = (int) ((float) adjustedHeight * SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor);
            }
        }

        RenderSystem.enableBlend();
        GL11.glBlendFunc(770, 771);
        if (var7) {
            font.drawString((float) Math.round(x + (float) adjustedWidth), (float) (Math.round(y + (float) adjustedHeight) + 2), text, new Color(0.0F, 0.0F, 0.0F, 0.35F));
        }

        if (text != null) {
            font.drawString((float) Math.round(x + (float) adjustedWidth), (float) Math.round(y + (float) adjustedHeight), text, new Color(var13, var14, var15, var12));
        }

        RenderSystem.disableBlend();
        GL11.glPopMatrix();
    }

    public static TrueTypeFont getFont2(String fontPath, int style, float size) {
        try {
            InputStream fontFile = Loader.readInputStream(fontPath);
            Font font = Font.createFont(0, fontFile);
            font = font.deriveFont(style, size);
            return new TrueTypeFont(font, (int) size);
        } catch (Exception ex) {
            return new TrueTypeFont(new Font("Arial", Font.PLAIN, (int) size), true);
        }
    }

    public static TrueTypeFont getFont(String fontPath, int style, float size) {
        try {
            InputStream fontFile = Loader.readInputStream(fontPath);
            Font font = Font.createFont(0, fontFile);
            font = font.deriveFont(style, size);
            return new TrueTypeFont(font, true);
        } catch (Exception ex) {
            return new TrueTypeFont(new Font("Arial", Font.PLAIN, (int) size), !SigmaReborn.MODE.equals(SigmaReborn.Mode.CLASSIC));
        }
    }

    public enum FontSizeAdjust {
        field14488,
        field14489,
        WIDTH_NEGATE,
        HEIGHT_NEGATE,
        NEGATE_AND_DIVIDE_BY_2
    }
}
