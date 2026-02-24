package io.github.sst.remake.util.math.color;

import io.github.sst.remake.util.render.font.FontAlignment;
import lombok.Getter;

import java.awt.*;

@Getter
public class ColorHelper {
    public static final ColorHelper DEFAULT_COLOR = new ColorHelper(-12871171);

    public int primaryColor;
    public int secondaryColor;

    public int textColor;

    public FontAlignment widthAlignment;
    public FontAlignment heightAlignment;

    public ColorHelper(int color) {
        this(color, shiftTowardsBlack(color, 0.05F));
    }

    public ColorHelper(int primary, int secondary) {
        this(primary, secondary, ClientColors.LIGHT_GREYISH_BLUE.getColor());
    }

    public ColorHelper(int primary, int secondary, int text) {
        this(primary, secondary, text, FontAlignment.CENTER, FontAlignment.CENTER);
    }

    public ColorHelper(int primary, int secondary, int text,
                       FontAlignment widthAlignment, FontAlignment heightAlignment) {
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.textColor = text;
        this.widthAlignment = widthAlignment;
        this.heightAlignment = heightAlignment;
    }

    public ColorHelper(ColorHelper var1) {
        this(var1.primaryColor, var1.secondaryColor, var1.textColor, var1.widthAlignment, var1.heightAlignment);
    }

    public ColorHelper setSecondaryColor(int var1) {
        this.secondaryColor = var1;
        return this;
    }

    public ColorHelper setPrimaryColor(int primary) {
        this.primaryColor = primary;
        return this;
    }

    public ColorHelper setTextColor(int color) {
        this.textColor = color;
        return this;
    }

    public ColorHelper setWidthAlignment(FontAlignment widthAlignment) {
        this.widthAlignment = widthAlignment;
        return this;
    }

    public ColorHelper setHeightAlignment(FontAlignment heightAlignment) {
        this.heightAlignment = heightAlignment;
        return this;
    }

    @SuppressWarnings("all")
    @Override
    public ColorHelper clone() {
        return new ColorHelper(this.primaryColor, this.secondaryColor, this.textColor, this.widthAlignment, this.heightAlignment);
    }

    public static int shiftTowardsBlack(int color, float shift) {
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int shiftedR = (int) ((float) r * (1.0F - shift));
        int shiftedG = (int) ((float) g * (1.0F - shift));
        int shiftedB = (int) ((float) b * (1.0F - shift));
        return a << 24 | (shiftedR & 0xFF) << 16 | (shiftedG & 0xFF) << 8 | shiftedB & 0xFF;
    }

    public static int shiftTowardsOther(int color, int color2, float shift) {
        int a1 = color >> 24 & 0xFF;
        int r1 = color >> 16 & 0xFF;
        int g1 = color >> 8 & 0xFF;
        int b1 = color & 0xFF;
        int a2 = color2 >> 24 & 0xFF;
        int r2 = color2 >> 16 & 0xFF;
        int g2 = color2 >> 8 & 0xFF;
        int b2 = color2 & 0xFF;
        float factor = 1.0F - shift;
        float shiftedA = (float) a1 * shift + (float) a2 * factor;
        float shiftedR = (float) r1 * shift + (float) r2 * factor;
        float shiftedG = (float) g1 * shift + (float) g2 * factor;
        float shiftedB = (float) b1 * shift + (float) b2 * factor;
        return (int) shiftedA << 24 | ((int) shiftedR & 0xFF) << 16 | ((int) shiftedG & 0xFF) << 8 | (int) shiftedB & 0xFF;
    }

    public static int adjustColorTowardsWhite(int original, float shift) {
        int a = original >> 24 & 0xFF;
        int r = original >> 16 & 0xFF;
        int g = original >> 8 & 0xFF;
        int b = original & 0xFF;
        int var8 = (int) ((float) r + (float) (255 - r) * shift);
        int var9 = (int) ((float) g + (float) (255 - g) * shift);
        int var10 = (int) ((float) b + (float) (255 - b) * shift);
        return a << 24 | (var8 & 0xFF) << 16 | (var9 & 0xFF) << 8 | var10 & 0xFF;
    }

    public static int applyAlpha(int color, float alpha) {
        return (int) (alpha * 255.0F) << 24 | color & 16777215;
    }

    public static float getAlpha(int color) {
        return (float) (color >> 24 & 0xFF) / 255.0F;
    }

    public static Color calculateAverageColor(Color... colors) {
        if (colors == null || colors.length == 0) {
            return Color.WHITE;
        }

        float weight = 1.0F / colors.length;
        float totalRed = 0.0F;
        float totalGreen = 0.0F;
        float totalBlue = 0.0F;
        float totalAlpha = 0.0F;

        for (Color color : colors) {
            if (color == null) {
                color = Color.BLACK;
            }

            totalRed += color.getRed() * weight;
            totalGreen += color.getGreen() * weight;
            totalBlue += color.getBlue() * weight;
            totalAlpha += color.getAlpha() * weight;
        }

        return new Color(totalRed / 255.0F, totalGreen / 255.0F, totalBlue / 255.0F, totalAlpha / 255.0F);
    }

    public static Color blendColor(Color first, Color second, float factor) {
        float newFactor = 1.0F - factor;
        float blendedR = (float) first.getRed() * factor + (float) second.getRed() * newFactor;
        float blendedG = (float) first.getGreen() * factor + (float) second.getGreen() * newFactor;
        float blendedB = (float) first.getBlue() * factor + (float) second.getBlue() * newFactor;
        return new Color(blendedR / 255.0F, blendedG / 255.0F, blendedB / 255.0F);
    }

    public static float[] unpackColorToRGBA(int color) {
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;

        return new float[]{red, green, blue, alpha};
    }

    public static int blendColors(int color1, int color2, float factor) {
        int a1 = color1 >> 24 & 0xFF;
        int r1 = color1 >> 16 & 0xFF;
        int g1 = color1 >> 8 & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = color2 >> 24 & 0xFF;
        int r2 = color2 >> 16 & 0xFF;
        int g2 = color2 >> 8 & 0xFF;
        int b2 = color2 & 0xFF;

        float inverseFactor = 1.0F - factor;

        float blendedA = a1 * factor + a2 * inverseFactor;
        float blendedR = r1 * factor + r2 * inverseFactor;
        float blendedG = g1 * factor + g2 * inverseFactor;
        float blendedB = b1 * factor + b2 * inverseFactor;

        return ((int) blendedA << 24) | (((int) blendedR & 0xFF) << 16) | (((int) blendedG & 0xFF) << 8) | ((int) blendedB & 0xFF);
    }
}