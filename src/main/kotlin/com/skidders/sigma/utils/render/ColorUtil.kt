package com.skidders.sigma.utils.render

import java.awt.*

object ColorUtil {
    fun applyAlpha(color: Int, alpha: Float): Int {
        return (alpha * 255.0F).toInt() shl 24 or (color and 16777215)
    }

    // alpha [0, 255]
    fun injectAlpha(color: Color, alpha: Int): Color {
        return Color(color.red, color.green, color.blue, alpha)
    }

    fun getColorComps(color: Color): FloatArray {
        return floatArrayOf(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
    }

    enum class ClientColors(val color: Int) {
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
        BRIGHT_PINK(-16724271)
    }
}
