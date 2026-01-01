package io.github.sst.remake.util.math.color;

import io.github.sst.remake.util.render.font.FontAlignment;

import java.awt.*;

/**
 * A utility class for managing colors, likely used for UI rendering.
 * It allows storing and manipulating different color components and related properties.
 */
public class ColorHelper {
    /**
     * A default instance of ColorHelper with a specific color value.
     */
    public static final ColorHelper DEFAULT_COLOR = new ColorHelper(-12871171);
    /**
     * The primary color component.
     */
    public int primaryColor;
    /**
     * A secondary color component, often a shifted version of the primary color.
     */
    public int secondaryColor;
    /**
     * A tertiary color component, possibly used for accent or highlighting.
     */
    public int tertiaryColor;
    /**
     * The color used for text rendering in conjunction with this ColorHelper.
     */
    public int textColor;

    public FontAlignment widthAlignment;
    public FontAlignment heightAlignment;

    /**
     * Constructs a ColorHelper with the given color and a darker shade of it.
     *
     * @param color The base color for this ColorHelper.
     */
    public ColorHelper(int color) {
        this(color, shiftTowardsBlack(color, 0.05F));
    }

    /**
     * Constructs a ColorHelper with a primary color and a secondary color.
     * The tertiary color and text color are set to default values from {@link ClientColors}.
     *
     * @param primary   The primary color.
     * @param secondary The secondary color.
     */
    public ColorHelper(int primary, int secondary) {
        this(primary, secondary, ClientColors.DEEP_TEAL.getColor());
    }

    /**
     * Constructs a ColorHelper with primary, secondary, and tertiary colors.
     * The text color is set to a default value from {@link ClientColors}.
     *
     * @param primary   The primary color.
     * @param secondary The secondary color.
     * @param tertiary  The tertiary color.
     */
    public ColorHelper(int primary, int secondary, int tertiary) {
        this(primary, secondary, tertiary, ClientColors.LIGHT_GREYISH_BLUE.getColor());
    }

    /**
     * Constructs a ColorHelper with primary, secondary, tertiary, and text colors.
     *
     * @param primary   The primary color.
     * @param secondary The secondary color.
     * @param tertiary  The tertiary color.
     * @param text      The text color.
     */
    public ColorHelper(int primary, int secondary, int tertiary, int text) {
        this(primary, secondary, tertiary, text, FontAlignment.CENTER, FontAlignment.CENTER);
    }

    /**
     * Constructs a ColorHelper with all color components and alignments specified.
     *
     * @param primary         The primary color.
     * @param secondary       The secondary color.
     * @param tertiary        The tertiary color.
     * @param text            The text color.
     * @param widthAlignment  The width FontAlignment instance.
     * @param heightAlignment The height FontAlignment instance.
     */
    public ColorHelper(int primary, int secondary, int tertiary, int text, FontAlignment widthAlignment, FontAlignment heightAlignment) {
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.tertiaryColor = tertiary;
        this.textColor = text;
        this.widthAlignment = widthAlignment;
        this.heightAlignment = heightAlignment;
    }

    /**
     * Copy constructor to create a new ColorHelper instance from an existing one.
     *
     * @param var1 The ColorHelper instance to copy.
     */
    public ColorHelper(ColorHelper var1) {
        this(var1.primaryColor, var1.secondaryColor, var1.tertiaryColor, var1.textColor, var1.widthAlignment, var1.heightAlignment);
    }

    /**
     * Gets the secondary color component.
     *
     * @return The secondary color.
     */
    public int getSecondaryColor() {
        return this.secondaryColor;
    }

    /**
     * Sets the secondary color component.
     *
     * @param var1 The new secondary color.
     * @return This ColorHelper instance for chaining.
     */
    public ColorHelper setSecondaryColor(int var1) {
        this.secondaryColor = var1;
        return this;
    }

    /**
     * Gets the primary color component.
     *
     * @return The primary color.
     */
    public int getPrimaryColor() {
        return this.primaryColor;
    }

    /**
     * Sets the primary color component.
     *
     * @param primary The new primary color.
     * @return This ColorHelper instance for chaining.
     */
    public ColorHelper setPrimaryColor(int primary) {
        this.primaryColor = primary;
        return this;
    }

    /**
     * Gets the tertiary color component.
     *
     * @return The tertiary color.
     */
    public int getTertiary() {
        return this.tertiaryColor;
    }

    /**
     * Sets the tertiary color component.
     *
     * @param tertiary The new tertiary color.
     * @return This ColorHelper instance for chaining.
     */
    public ColorHelper setTertiary(int tertiary) {
        this.tertiaryColor = tertiary;
        return this;
    }

    /**
     * Gets the text color.
     *
     * @return The text color.
     */
    public int getTextColor() {
        return this.textColor;
    }

    /**
     * Sets the text color.
     *
     * @param color The new text color.
     * @return This ColorHelper instance for chaining.
     */
    public ColorHelper setTextColor(int color) {
        this.textColor = color;
        return this;
    }

    public FontAlignment getWidthAlignment() {
        return this.widthAlignment;
    }

    public ColorHelper setWidthAlignment(FontAlignment widthAlignment) {
        this.widthAlignment = widthAlignment;
        return this;
    }


    public FontAlignment getHeightAlignment() {
        return this.heightAlignment;
    }


    public ColorHelper setHeightAlignment(FontAlignment heightAlignment) {
        this.heightAlignment = heightAlignment;
        return this;
    }

    /**
     * Creates and returns a new ColorHelper instance with the same color values as this one.
     *
     * @return A new clone of this color helper.
     */
    @SuppressWarnings("all")
    @Override
    public ColorHelper clone() {
        return new ColorHelper(this.primaryColor, this.secondaryColor, this.tertiaryColor, this.textColor, this.widthAlignment, this.heightAlignment);
    }

    /**
     * Adjusts the RGB components of a color towards black by a specified factor.
     * The alpha component remains unchanged.
     *
     * @param color The original color represented as an integer, where the highest byte is the alpha component,
     *              followed by red, green, and blue components.
     * @param shift The factor by which to adjust the color towards black. A value of 0.0 will leave the color unchanged,
     *              while a value of 1.0 will result in a completely black color.
     * @return The adjusted color as an integer, with the same alpha component as the original color and RGB components
     * adjusted towards black by the specified factor.
     */
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

    /**
     * Adjusts the RGB components of 2 colors towards black by 1 specified factor.
     * The alpha component remains unchanged.
     *
     * @param color  The 1st original color represented as an integer, where the highest byte is the alpha component,
     *               followed by red, green, and blue components.
     * @param color2 The 2nd original color represented as an integer, where the highest byte is the alpha component,
     *               followed by red, green, and blue components.
     * @param shift  The factor by which to adjust the color towards black. A value of 0.0 will leave the color unchanged,
     *               while a value of 1.0 will result in the same color as the other.
     * @return The adjusted color as an integer, with the same alpha component as the original color and RGB components
     * adjusted towards each-other, I think...
     */
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