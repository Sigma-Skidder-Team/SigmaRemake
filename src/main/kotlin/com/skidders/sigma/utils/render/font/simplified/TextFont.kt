package com.skidders.sigma.utils.render.font.simplified

import com.mojang.blaze3d.systems.RenderSystem
import com.skidders.sigma.utils.render.font.common.AbstractFont
import com.skidders.sigma.utils.render.font.common.FontLanguage
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

class TextFont(
    fileName: String,
    size: Int,
    stretching: Float,
    spacing: Float,
    lifting: Float,
    fontLanguage: FontLanguage
) :
    AbstractFont() {
    override val stretching: Float
    override val spacing: Float
    override val lifting: Float
        get() = fontHeight + field

    init {
        val font: Font = getFont(fileName, Font.PLAIN, size)!!
        val fontRenderContext = FontRenderContext(font.getTransform(), true, true)

        var maxWidth = 0.0
        var maxHeight = 0.0

        val codes: IntArray = fontLanguage.charCodes
        val chars = CharArray((codes.get(1) - codes.get(0) + codes.get(3) - codes[2]))

        var n = 0
        run {
            var d = 0
            while (d <= 2) {
                for (i in codes[d]..<codes[d + 1]) {
                    chars[n] = i.toChar()
                    val bound: Rectangle2D = font.getStringBounds(chars[n].toString(), fontRenderContext)
                    maxWidth = max(maxWidth, bound.width)
                    maxHeight = max(maxHeight, bound.height)
                    n++
                }
                d += 2
            }
        }

        val d: Int = ceil(sqrt((maxHeight + 2) * (maxWidth + 2) * chars.size)).toInt()

        this.stretching = stretching
        this.spacing = spacing
        this.lifting = lifting
        this.fontName = font.getFontName(Locale.ENGLISH)
        this.fontHeight = (maxHeight / 2).toFloat()
        this.imgHeight = d
        this.imgWidth = d

        val image = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = setupGraphics(image, font)

        val fontMetrics = graphics.fontMetrics
        var posX = 1
        var posY = 2

        for (c: Char in chars) {
            val glyph = Glyph()
            val bounds: Rectangle2D = fontMetrics.getStringBounds(c.toString(), graphics)
            glyph.width = bounds.getWidth().toInt() + 1
            glyph.height = bounds.getHeight().toInt() + 2

            if (posX + glyph.width >= imgWidth) {
                posX = 1
                posY = (posY + (maxHeight + fontMetrics.getDescent() + 2)).toInt()
            }

            glyph.x = posX
            glyph.y = posY

            graphics.drawString(c.toString(), posX, posY + fontMetrics.getAscent())

            posX += glyph.width + 4
            glyphs[c] = glyph
        }

        RenderSystem.recordRenderCall { setTexture(image) }
    }
}