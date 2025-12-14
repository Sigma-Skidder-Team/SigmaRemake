package io.github.sst.remake.util.math.color;

import io.github.sst.remake.util.render.font.FontSize;

/**
 * A utility class for managing colors, likely used for UI rendering.
 * It allows storing and manipulating different color components and related properties.
 */
public class ColorHelper {
    /**
     * A default instance of ColorHelper with a specific color value.
     */
    public static final ColorHelper field27961 = new ColorHelper(-12871171);
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
    /**
     * An associated Class2218 instance, purpose unclear without further context.
     */
    public FontSize field27966;
    /**
     * Another associated Class2218 instance, purpose unclear without further context.
     */
    public FontSize field27967;

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
     * The Class2218 instances are set to default values ({@link FontSize#NEGATE_AND_DIVIDE_BY_2}).
     *
     * @param primary   The primary color.
     * @param secondary The secondary color.
     * @param tertiary  The tertiary color.
     * @param text      The text color.
     */
    public ColorHelper(int primary, int secondary, int tertiary, int text) {
        this(primary, secondary, tertiary, text, FontSize.NEGATE_AND_DIVIDE_BY_2, FontSize.NEGATE_AND_DIVIDE_BY_2);
    }

    /**
     * Constructs a ColorHelper with all color components and Class2218 instances specified.
     *
     * @param primary   The primary color.
     * @param secondary The secondary color.
     * @param tertiary  The tertiary color.
     * @param text      The text color.
     * @param var5      The first Class2218 instance.
     * @param var6      The second Class2218 instance.
     */
    public ColorHelper(int primary, int secondary, int tertiary, int text, FontSize var5, FontSize var6) {
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.tertiaryColor = tertiary;
        this.textColor = text;
        this.field27966 = var5;
        this.field27967 = var6;
    }

    /**
     * Copy constructor to create a new ColorHelper instance from an existing one.
     *
     * @param var1 The ColorHelper instance to copy.
     */
    public ColorHelper(ColorHelper var1) {
        this(var1.primaryColor, var1.secondaryColor, var1.tertiaryColor, var1.textColor, var1.field27966, var1.field27967);
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

    /**
     * Gets the first Class2218 instance.
     *
     * @return The first Class2218 instance.
     */
    public FontSize method19411() {
        return this.field27966;
    }

    /**
     * Sets the first Class2218 instance.
     *
     * @param var1 The new Class2218 instance.
     * @return This ColorHelper instance for chaining.
     */
    public ColorHelper method19412(FontSize var1) {
        this.field27966 = var1;
        return this;
    }

    /**
     * Gets the second Class2218 instance.
     *
     * @return The second Class2218 instance.
     */
    public FontSize method19413() {
        return this.field27967;
    }

    /**
     * Sets the second Class2218 instance.
     *
     * @param var1 The new Class2218 instance.
     * @return This ColorHelper instance for chaining.
     */
    public ColorHelper method19414(FontSize var1) {
        this.field27967 = var1;
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
        return new ColorHelper(this.primaryColor, this.secondaryColor, this.tertiaryColor, this.textColor, this.field27966, this.field27967);
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

    public static int applyAlpha(int color, float alpha) {
        return (int) (alpha * 255.0F) << 24 | color & 16777215;
    }

    public static float getAlpha(int color) {
        return (float) (color >> 24 & 0xFF) / 255.0F;
    }
}