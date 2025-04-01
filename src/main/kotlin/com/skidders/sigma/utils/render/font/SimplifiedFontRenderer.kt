package com.skidders.sigma.utils.render.font

import com.mojang.blaze3d.platform.GlStateManager
import com.skidders.sigma.utils.render.font.simplified.TextFont
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f
import org.lwjgl.opengl.GL30
import java.awt.Color

object SimplifiedFontRenderer {
    fun drawString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
    ): Float {
        return renderString(matrices, font, text, x, y, color)
    }

    fun drawCenteredXString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
    ): Float {
        return renderString(matrices, font, text, x - font.getWidth(text) / 2.0f, y, color)
    }

    fun drawCenteredYString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
    ): Float {
        return renderString(matrices, font, text, x, y + font.lifting
                / 2.0f + 0.5f, color)
    }

    fun drawCenteredXYString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
    ): Float {
        return renderString(
            matrices,
            font,
            text,
            x - font.getWidth(text) / 2.0f,
            y + font.lifting
                    / 2.0f + 0.5f,
            color
        )
    }

    fun drawShadowedString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
    ): Float {
        return renderStringWithShadow(matrices, font, text, x, y, color, getShadowColor(color))
    }

    fun drawShadowedCenteredXString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
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
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
    ): Float {
        return renderStringWithShadow(
            matrices,
            font,
            text,
            x,
            y + font.lifting
                    / 2.0f + 0.5f,
            color,
            getShadowColor(color)
        )
    }

    fun drawShadowedCenteredXYString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
    ): Float {
        return renderStringWithShadow(
            matrices,
            font,
            text,
            x - font.getWidth(text) / 2.0f,
            y + font.lifting
                    / 2.0f + 0.5f,
            color,
            getShadowColor(color)
        )
    }

    fun drawShadowedString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color,
        shadowColor: Color
    ): Float {
        return renderStringWithShadow(matrices, font, text, x, y, color, shadowColor)
    }

    fun drawShadowedCenteredXString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color,
        shadowColor: Color
    ): Float {
        return renderStringWithShadow(matrices, font, text, x - font.getWidth(text) / 2.0f, y, color, shadowColor)
    }

    fun drawShadowedCenteredYString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color,
        shadowColor: Color
    ): Float {
        return renderStringWithShadow(matrices, font, text, x, y + font.lifting
                / 2.0f + 0.5f, color, shadowColor)
    }

    fun drawShadowedCenteredXYString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color,
        shadowColor: Color
    ): Float {
        return renderStringWithShadow(
            matrices,
            font,
            text,
            x - font.getWidth(text) / 2.0f,
            y + font.lifting
                    / 2.0f + 0.5f,
            color,
            shadowColor
        )
    }

    private fun renderStringWithShadow(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color,
        shadowColor: Color
    ): Float {
        var y: Double = y
        renderString(matrices, font, text, x + 1.0f, y, shadowColor)
        return renderString(matrices, font, text, x, 1.0f.let { y -= it; y }, color) + 1.0f
    }

    // returns string width
    private fun renderString(
        matrices: MatrixStack,
        font: TextFont,
        text: String,
        x: Double,
        y: Double,
        color: Color
    ): Float {
        var y: Double = y
        y -= font.lifting


        val startPos: Float = x.toFloat() * 2f
        var posX: Float = startPos
        val posY: Float = y.toFloat() * 2f
        val red: Float = color.getRed() / 255.0f
        val green: Float = color.getGreen() / 255.0f
        val blue: Float = color.getBlue() / 255.0f
        val alpha: Float = color.getAlpha() / 255.0f

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
        matrices.push()
        matrices.scale(0.5f, 0.5f, 1f)

        val matrix: Matrix4f = matrices.peek().getModel()

        font.bindTex()

        for (i in 0..<text.length) {
            posX += font.renderGlyph(matrix, text.get(i), posX, posY, red, green, blue, alpha)
        }

        font.unbindTex()

        matrices.pop()
        GlStateManager.disableBlend()

        return (posX - startPos) / 2.0f
    }

    fun getShadowColor(color: Color): Color {
        return Color((color.getRGB() and 16579836) shr 2 or (color.getRGB() and -16777216))
    }
}