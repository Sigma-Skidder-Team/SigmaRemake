package com.skidders.sigma.utils.render;

import java.awt.*;

public class ColorUtil {
    public static int applyAlpha(int color, float alpha) {
        return (int)(alpha * 255.0F) << 24 | color & 16777215;
    }

    // alpha [0, 255]
    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static float[] getColorComps(Color color) {
        return new float[]{color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f};
    }

    public enum ClientColors {
        DEEP_TEAL(-16711423),
        DARK_BLUE_GREY(-16723258),
        DARK_GREEN(-15698006),
        DARK_SLATE_GREY(-9581017),
        GREYISH_BLUE(-11231458),
        LIGHT_GREYISH_BLUE(-65794),
        DARK_PURPLE(-14163205),
        DARK_NAVY_BLUE(-16548724),
        MID_GREY(-6710887),
        DULL_GREEN(-12303292),
        PALE_YELLOW(-43691),
        DARK_OLIVE(-7864320),
        PALE_ORANGE(-21931),
        DARK_MAROON(-7846912),
        VERY_LIGHT_GREY(-171),
        DARK_MAUVE(-7829504),
        PALE_YELLOW_GREEN(-43521),
        PALE_RED(-7864184),
        BRIGHT_PINK(-16724271);

        public final int color;
        ClientColors(int color) {
            this.color = color;
        }
    }
}
