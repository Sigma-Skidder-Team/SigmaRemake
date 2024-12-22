package info.opensigma.util.font

import org.newdawn.slick.Color
import org.newdawn.slick.TrueTypeFont

class Font(name: String, size: Int) {

    private val trueTypeFont: TrueTypeFont = FontLoader.getFont("fonts/$name", 0, size.toFloat())

    fun drawString(text: String, x: Float, y: Float, color: Color) {
        trueTypeFont.drawString(x, y, text, color)
    }

    fun drawString(text: String, x: Float, y: Float, color: Int) {
        drawString(text, x, y, ColorConversionUtil.convertIntToSlickColor(color))
    }

    fun drawString(text: String, x: Int, y: Int, color: Int) {
        drawString(text, x.toFloat(), y.toFloat(), ColorConversionUtil.convertIntToSlickColor(color))
    }

    fun drawString(text: String, x: Float, y: Float, awtColor: java.awt.Color) {
        drawString(text, x, y, ColorConversionUtil.convertAwtToSlickColor(awtColor))
    }

    fun drawString(text: String, x: Int, y: Int, awtColor: java.awt.Color) {
        drawString(text, x.toFloat(), y.toFloat(), ColorConversionUtil.convertAwtToSlickColor(awtColor))
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Int) {
        drawStringWithShadow(text, x, y, ColorConversionUtil.convertIntToSlickColor(color))
    }

    fun drawStringWithShadow(text: String, x: Int, y: Int, color: Int) {
        drawStringWithShadow(text, x.toFloat(), y.toFloat(), ColorConversionUtil.convertIntToSlickColor(color))
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, awtColor: java.awt.Color) {
        drawStringWithShadow(text, x, y, ColorConversionUtil.convertAwtToSlickColor(awtColor))
    }

    fun drawStringWithShadow(text: String, x: Int, y: Int, awtColor: java.awt.Color) {
        drawStringWithShadow(text, x.toFloat(), y.toFloat(), ColorConversionUtil.convertAwtToSlickColor(awtColor))
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Color) {
        val shadowColor = Color(
            color.r / 4,
            color.g / 4,
            color.b / 4,
            color.a
        )

        trueTypeFont.drawString(x + 1.0F, y + 1.0F, text, shadowColor)
        trueTypeFont.drawString(x, y, text, color)
    }
}

