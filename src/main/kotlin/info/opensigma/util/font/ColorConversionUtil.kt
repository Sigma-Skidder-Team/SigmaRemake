package info.opensigma.util.font

import org.newdawn.slick.Color

object ColorConversionUtil {

    fun convertIntToSlickColor(color: Int): Color {
        val red = (color shr 16) and 0xFF
        val green = (color shr 8) and 0xFF
        val blue = color and 0xFF
        val alpha = (color shr 24) and 0xFF

        return Color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    fun convertAwtToSlickColor(awtColor: java.awt.Color): Color {
        return Color(
            awtColor.red / 255f,
            awtColor.green / 255f,
            awtColor.blue / 255f,
            awtColor.alpha / 255f
        )
    }
}

