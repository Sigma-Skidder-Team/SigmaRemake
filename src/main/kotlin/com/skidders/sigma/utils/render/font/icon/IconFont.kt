package com.skidders.sigma.utils.render.font.icon

import com.mojang.blaze3d.systems.RenderSystem
import com.skidders.sigma.utils.render.font.common.AbstractFont
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.max

@Deprecated("")
class IconFont(fileName: String, size: Int, vararg chars: Char) : AbstractFont() {
    init {
        val font: Font = getFont(fileName, Font.PLAIN, size)!!

        val fontRenderContext = FontRenderContext(font.getTransform(), true, true)
        var maxWidth = 0.0
        var maxHeight = 0.0

        for (c: Char in chars) {
            val bound: Rectangle2D = font.getStringBounds(c.toString(), fontRenderContext)
            maxWidth = max(maxWidth, bound.width)
            maxHeight = max(maxHeight, bound.height)
        }

        this.fontName = font.getFontName(Locale.ENGLISH)
        this.fontHeight = (maxHeight / 2.0f).toFloat()
        this.imgHeight = maxHeight.toInt() + 4
        this.imgWidth = (maxWidth.toInt() + 4) * chars.size

        val image: BufferedImage = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics: Graphics2D = setupGraphics(image, font)

        val fontMetrics: FontMetrics = graphics.fontMetrics
        var posX: Int = 2
        val posY: Int = 2

        for (c: Char in chars) {
            val glyph = Glyph()
            val bounds: Rectangle2D = fontMetrics.getStringBounds(c.toString(), graphics)
            glyph.width = bounds.width.toInt() + 1
            glyph.height = bounds.height.toInt() + 2

            glyph.x = posX
            glyph.y = posY

            graphics.drawString(c.toString(), posX, posY + fontMetrics.ascent)

            super.glyphs[c] = glyph

            posX += glyph.width + 2
        }

        RenderSystem.recordRenderCall { setTexture(image) }
    }

    override val stretching: Float
        get() = 0.0f

    override val spacing: Float
        get() = 0.0f

    override val lifting: Float
        get() = fontHeight
}