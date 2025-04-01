package com.skidders.sigma.utils.render.font.styled

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f
import org.lwjgl.opengl.GL30

object StyledFontRenderer {
    const val STYLE_CODES: String = "0123456789abcdefklmnor"
    private val COLOR_CODES: IntArray = IntArray(32)

    init {
        for (i in 0..31) {
            val j: Int = (i shr 3 and 1) * 85
            var k: Int = (i shr 2 and 1) * 170 + j
            var l: Int = (i shr 1 and 1) * 170 + j
            var i1: Int = (i and 1) * 170 + j

            if (i == 6) {
                k += 85
            }

            if (i >= 16) {
                k /= 4
                l /= 4
                i1 /= 4
            }

            COLOR_CODES[i] = (k and 255) shl 16 or ((l and 255) shl 8) or (i1 and 255)
        }
    }

    fun drawString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderString(matrices, font, text, x, y, false, color)
    }

    fun drawCenteredXString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderString(matrices, font, text, x - font.getWidth(text) / 2.0f, y, false, color)
    }

    fun drawCenteredYString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderString(matrices, font, text, x, y + font.lifting / 2.0f + 0.5f, false, color)
    }

    fun drawCenteredXYString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderString(
            matrices,
            font,
            text,
            x - font.getWidth(text) / 2.0f,
            y + font.lifting / 2.0f + 0.5f,
            false,
            color
        )
    }

    fun drawShadowedString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderStringWithShadow(matrices, font, text, x, y, color, getShadowColor(color))
    }

    fun drawShadowedCenteredXString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderStringWithShadow(
            matrices,
            font,
            text,
            x - font.getWidth(text) / 2.0f,
            y,
            color,
            getShadowColor(color)
        )
    }

    fun drawShadowedCenteredYString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderStringWithShadow(
            matrices,
            font,
            text,
            x,
            y + font.lifting / 2.0f + 0.5f,
            color,
            getShadowColor(color)
        )
    }

    fun drawShadowedCenteredXYString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderStringWithShadow(
            matrices,
            font,
            text,
            x - font.getWidth(text) / 2.0f,
            y + font.lifting / 2.0f + 0.5f,
            color,
            getShadowColor(color)
        )
    }

    fun drawShadowedString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color,
        shadowColor: java.awt.Color
    ): Float {
        return renderStringWithShadow(matrices, font, text, x, y, color, shadowColor)
    }

    fun drawShadowedCenteredXString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color,
        shadowColor: java.awt.Color
    ): Float {
        return renderStringWithShadow(matrices, font, text, x - font.getWidth(text) / 2.0f, y, color, shadowColor)
    }

    fun drawShadowedCenteredYString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color,
        shadowColor: java.awt.Color
    ): Float {
        return renderStringWithShadow(matrices, font, text, x, y + font.lifting / 2.0f + 0.5f, color, shadowColor)
    }

    fun drawShadowedCenteredXYString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color,
        shadowColor: java.awt.Color
    ): Float {
        return renderStringWithShadow(
            matrices,
            font,
            text,
            x - font.getWidth(text) / 2.0f,
            y + font.lifting / 2.0f + 0.5f,
            color,
            shadowColor
        )
    }

    private fun renderStringWithShadow(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        color: java.awt.Color,
        shadowColor: java.awt.Color
    ): Float {
        renderString(matrices, font, text, x + 1.0f, y, true, shadowColor)
        return renderString(matrices, font, text, x, y - 1.0f, false, color) + 1.0f
    }

    // returns string width
    private fun renderString(
        matrices: MatrixStack,
        font: StyledFont,
        text: String,
        x: Double,
        y: Double,
        shadow: Boolean,
        color: java.awt.Color
    ): Float {
        val startPos: Float = x.toFloat() * 2.0f
        var posX: Float = startPos
        val posY: Float = y.toFloat() * 2.0f
        var red: Float = color.getRed() / 255.0f
        var green: Float = color.getGreen() / 255.0f
        var blue: Float = color.getBlue() / 255.0f
        var alpha: Float = color.getAlpha() / 255.0f
        var bold: Boolean = false
        var italic: Boolean = false
        var strikethrough: Boolean = false
        var underline: Boolean = false

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
        matrices.push()
        matrices.scale(0.5f, 0.5f, 1f)

        val matrix: Matrix4f = matrices.peek().getModel()

        var i: Int = 0
        while (i < text.length) {
            val c0: Char = text.get(i)

            if (c0.code == 167 && i + 1 < text.length && STYLE_CODES.indexOf(text.lowercase().get(i + 1)) != -1) {
                var i1: Int = STYLE_CODES.indexOf(text.lowercase().get(i + 1))

                if (i1 < 16) {
                    bold = false
                    strikethrough = false
                    underline = false
                    italic = false

                    if (shadow) {
                        i1 += 16
                    }

                    val j1: Int = COLOR_CODES.get(i1)

                    red = (j1 shr 16 and 255).toFloat() / 255.0f
                    green = (j1 shr 8 and 255).toFloat() / 255.0f
                    blue = (j1 and 255).toFloat() / 255.0f
                    alpha = 1f
                } else if (i1 == 16) {
                } else if (i1 == 17) {
                    bold = true
                } else if (i1 == 18) {
                    strikethrough = true
                } else if (i1 == 19) {
                    underline = true
                } else if (i1 == 20) {
                    italic = true
                } else if (i1 == 21) {
                    bold = false
                    strikethrough = false
                    underline = false
                    italic = false
                }

                i++
            } else {
                val f: Float = font.renderGlyph(matrix, c0, posX, posY, bold, italic, red, green, blue, alpha)

                if (strikethrough) {
                    val h: Float = font.lifting + 2
                    GlStateManager.disableTexture()

                    Tessellator.getInstance().buffer.begin(7, VertexFormats.POSITION_COLOR)
                    Tessellator.getInstance().buffer.vertex(matrix, posX, posY + h + 3, 0f)
                        .color(red, green, blue, alpha).next()
                    Tessellator.getInstance().buffer.vertex(matrix, posX + f, posY + h + 3, 0f)
                        .color(red, green, blue, alpha).next()
                    Tessellator.getInstance().buffer.vertex(matrix, posX + f, posY + h, 0f)
                        .color(red, green, blue, alpha).next()
                    Tessellator.getInstance().buffer.vertex(matrix, posX, posY + h, 0f)
                        .color(red, green, blue, alpha).next()
                    Tessellator.getInstance().draw()

                    GlStateManager.enableTexture()
                }

                if (underline) {
                    val y1: Float = posY + font.lifting * 2.0f - 4
                    GlStateManager.disableTexture()

                    Tessellator.getInstance().buffer.begin(7, VertexFormats.POSITION_COLOR)
                    Tessellator.getInstance().buffer.vertex(matrix, posX, y1 + 4, 0f).color(red, green, blue, alpha)
                        .next()
                    Tessellator.getInstance().buffer.vertex(matrix, posX + f, y1 + 4, 0f)
                        .color(red, green, blue, alpha).next()
                    Tessellator.getInstance().buffer.vertex(matrix, posX + f, y1 + 2, 0f)
                        .color(red, green, blue, alpha).next()
                    Tessellator.getInstance().buffer.vertex(matrix, posX, y1 + 2, 0f).color(red, green, blue, alpha)
                        .next()
                    Tessellator.getInstance().draw()

                    GlStateManager.enableTexture()
                }
                posX += f
            }
            i++
        }

        matrices.pop()
        GlStateManager.disableBlend()

        return (posX - startPos) / 2.0f
    }

    fun getShadowColor(color: java.awt.Color): java.awt.Color {
        return java.awt.Color((color.getRGB() and 16579836) shr 2 or (color.getRGB() and -16777216))
    }
}