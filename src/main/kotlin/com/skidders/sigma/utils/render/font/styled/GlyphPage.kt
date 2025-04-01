package com.skidders.sigma.utils.render.font.styled

import com.mojang.blaze3d.systems.RenderSystem
import com.skidders.sigma.utils.render.font.common.AbstractFont
import net.minecraft.util.math.Matrix4f
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

class GlyphPage(font: Font, chars: CharArray, stretching: Float, spacing: Float, lifting: Float) :
    AbstractFont() {
    private val italicSpacing: Int
    override val stretching: Float
    override val spacing: Float
    override val lifting: Float
        get() = fontHeight + field

    init {
        val fontRenderContext = FontRenderContext(font.getTransform(), true, true)
        var maxWidth = 0.0
        var maxHeight = 0.0

        for (c: Char in chars) {
            val bound: Rectangle2D = font.getStringBounds(c.toString(), fontRenderContext)
            maxWidth = max(maxWidth, bound.width)
            maxHeight = max(maxHeight, bound.height)
        }

        this.italicSpacing = if (font.isItalic) 5 else 0
        val d: Int = ceil(sqrt((maxHeight + 2) * (maxWidth + 2 + italicSpacing) * chars.size)).toInt()

        this.fontName = font.getFontName(Locale.ENGLISH)
        this.fontHeight = (maxHeight / 2).toFloat()
        this.imgHeight = d
        this.imgWidth = d
        this.stretching = stretching
        this.spacing = spacing
        this.lifting = lifting

        val image = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = setupGraphics(image, font)

        val fontMetrics: FontMetrics = graphics.fontMetrics
        var posX = 1
        var posY = 2

        for (c: Char in chars) {
            val glyph = Glyph()
            val bounds: Rectangle2D = fontMetrics.getStringBounds(c.toString(), graphics)
            glyph.width = bounds.width.toInt() + 1 + italicSpacing
            glyph.height = bounds.height.toInt() + 2

            if (posX + glyph.width >= imgWidth) {
                posX = 1
                posY = (posY + (maxHeight + fontMetrics.descent + 2)).toInt()
            }

            glyph.x = posX
            glyph.y = posY

            graphics.drawString(c.toString(), posX, posY + fontMetrics.ascent)

            posX += glyph.width + 4
            glyphs[c] = glyph
        }

        RenderSystem.recordRenderCall { setTexture(image) }
    }

    override fun renderGlyph(
        matrix: Matrix4f?,
        c: Char,
        x: Float,
        y: Float,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ): Float {
        bindTex()
        val w: Float = super.renderGlyph(matrix, c, x, y, red, green, blue, alpha) - italicSpacing
        unbindTex()

        return w
    }
}