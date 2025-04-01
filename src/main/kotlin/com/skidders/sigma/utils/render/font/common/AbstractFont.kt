package com.skidders.sigma.utils.render.font.common

import com.mojang.blaze3d.platform.GlStateManager
import com.skidders.SigmaReborn
import com.skidders.sigma.utils.render.image.TextureLoader
import kotlinx.io.IOException
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.Matrix4f
import java.awt.Font
import java.awt.FontFormatException
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

abstract class AbstractFont {
    protected var glyphs: MutableMap<Char, Glyph> = HashMap()
    protected var texId: Int = 0
    protected var imgWidth: Int = 0
    protected var imgHeight: Int = 0
    var fontHeight: Float = 0f
        protected set
    var fontName: String? = null
        protected set

    abstract val stretching: Float

    abstract val spacing: Float

    abstract val lifting: Float

    protected fun setTexture(img: BufferedImage) {
        texId = TextureLoader.loadTexture1(img)
    }

    fun bindTex() {
        GlStateManager.bindTexture(texId)
    }

    fun unbindTex() {
        GlStateManager.bindTexture(0)
    }

    fun setupGraphics(img: BufferedImage, font: Font?): Graphics2D {
        val graphics: Graphics2D = img.createGraphics()

        graphics.setFont(font)
        graphics.setColor(java.awt.Color(255, 255, 255, 0))
        graphics.fillRect(0, 0, imgWidth, imgHeight)
        graphics.setColor(java.awt.Color.WHITE)
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        return graphics
    }

    // color components should be in range [0;1]
    open fun renderGlyph(
        matrix: Matrix4f?,
        c: Char,
        x: Float,
        y: Float,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ): Float {
        val glyph: Glyph? = glyphs.get(c)

        if (glyph == null) return 0f

        val pageX: Float = glyph.x / imgWidth.toFloat()
        val pageY: Float = glyph.y / imgHeight.toFloat()
        val pageWidth: Float = glyph.width / imgWidth.toFloat()
        val pageHeight: Float = glyph.height / imgHeight.toFloat()
        val width: Float = glyph.width + stretching
        val height: Float = glyph.height.toFloat()

        Tessellator.getInstance().getBuffer().begin(7, VertexFormats.POSITION_COLOR_TEXTURE)
        Tessellator.getInstance().getBuffer().vertex(matrix, x, y + height, 0f).color(red, green, blue, alpha)
            .texture(pageX, pageY + pageHeight).next()
        Tessellator.getInstance().getBuffer().vertex(matrix, x + width, y + height, 0f).color(red, green, blue, alpha)
            .texture(pageX + pageWidth, pageY + pageHeight).next()
        Tessellator.getInstance().getBuffer().vertex(matrix, x + width, y, 0f).color(red, green, blue, alpha)
            .texture(pageX + pageWidth, pageY).next()
        Tessellator.getInstance().getBuffer().vertex(matrix, x, y, 0f).color(red, green, blue, alpha)
            .texture(pageX, pageY).next()
        Tessellator.getInstance().draw()

        return width + spacing
    }

    fun getWidth(ch: Char): Float {
        return glyphs.get(ch)!!.width + stretching
    }

    fun getWidth(text: String): Float {
        var width: Float = 0.0f
        val sp: Float = spacing
        for (i in 0..<text.length) {
            width += getWidth(text.get(i)) + sp
        }

        return (width - sp) / 2f
    }

    class Glyph {
        var x: Int = 0
        var y: Int = 0
        var width: Int = 0
        var height: Int = 0
    }

    companion object {
        private val FONT_DIR: String = "/assets/" + SigmaReborn.MOD_ID + "/jello/fonts/"

        fun getFont(fileName: String, style: Int, size: Int): Font? {
            val path: String = FONT_DIR + fileName
            var font: Font? = null

            try {
                font = Font.createFont(Font.TRUETYPE_FONT, SigmaReborn::class.java.getResourceAsStream(path))
                    .deriveFont(style, size.toFloat())
            } catch (e: FontFormatException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return font
        }
    }
}