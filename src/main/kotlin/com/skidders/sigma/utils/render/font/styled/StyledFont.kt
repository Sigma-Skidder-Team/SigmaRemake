package com.skidders.sigma.utils.render.font.styled

import com.skidders.sigma.utils.render.font.common.AbstractFont
import com.skidders.sigma.utils.render.font.common.FontLanguage
import net.minecraft.util.math.Matrix4f
import java.awt.Font

class StyledFont(
    fileName: String,
    size: Int,
    stretching: Float,
    spacing: Float,
    lifting: Float,
    fontLanguage: FontLanguage
) {
    private val regular: GlyphPage
    private val bold: GlyphPage
    private val italic: GlyphPage
    private val boldItalic: GlyphPage

    init {
        val codes: IntArray = fontLanguage.charCodes
        val chars: CharArray = CharArray((codes[1] - codes[0] + codes[3] - codes[2]))

        var c: Int = 0
        var d: Int = 0
        while (d <= 2) {
            for (i in codes[d]..<codes[d + 1]) {
                chars[c] = i.toChar()
                c++
            }
            d += 2
        }

        this.regular = GlyphPage(AbstractFont.getFont(fileName, Font.PLAIN, size)!!, chars, stretching, spacing, lifting)
        this.bold = GlyphPage(AbstractFont.getFont(fileName, Font.BOLD, size)!!, chars, stretching, spacing, lifting)
        this.italic = GlyphPage(AbstractFont.getFont(fileName, Font.ITALIC, size)!!, chars, stretching, spacing, lifting)
        this.boldItalic = GlyphPage(
            AbstractFont.getFont(fileName, Font.BOLD or Font.ITALIC, size)!!,
            chars,
            stretching,
            spacing,
            lifting
        )
    }

    fun renderGlyph(
        matrix: Matrix4f?,
        c: Char,
        x: Float,
        y: Float,
        bold: Boolean,
        italic: Boolean,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ): Float {
        return getGlyphPage(bold, italic).renderGlyph(matrix, c, x, y, red, green, blue, alpha)
    }

    fun getWidth(text: String): Float {
        var bold: Boolean = false
        var italic: Boolean = false
        var width: Float = 0.0f

        var i: Int = 0
        while (i < text.length) {
            val c0: Char = text.get(i)

            if (c0.code == 167 && i + 1 < text.length && StyledFontRenderer.STYLE_CODES.indexOf(
                    text.lowercase().get(i + 1)
                ) != -1
            ) {
                val i1: Int = StyledFontRenderer.STYLE_CODES.indexOf(text.lowercase().get(i + 1))

                if (i1 < 16) {
                    bold = false
                    italic = false
                } else if (i1 == 17) {
                    bold = true
                } else if (i1 == 20) {
                    italic = true
                } else if (i1 == 21) {
                    bold = false
                    italic = false
                }

                i++
            } else {
                width += getGlyphPage(bold, italic).getWidth(c0) + regular.spacing
            }
            i++
        }

        return (width - regular.spacing) / 2.0f
    }

    private fun getGlyphPage(boldStyle: Boolean, italicStyle: Boolean): GlyphPage {
        return if (boldStyle && italicStyle) boldItalic
        else if (boldStyle) bold
        else if (italicStyle) italic
        else regular
    }

    val fontName: String
        get() = regular.fontName!!

    val fontHeight: Float
        get() = regular.fontHeight

    val stretching: Float
        get() = regular.stretching

    val spacing: Float
        get() = regular.spacing

    val lifting: Float
        get() = regular.lifting
}