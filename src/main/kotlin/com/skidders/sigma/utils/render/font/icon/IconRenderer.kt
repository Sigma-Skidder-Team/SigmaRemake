package com.skidders.sigma.utils.render.font.icon

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f
import org.lwjgl.opengl.GL30

@Deprecated("")
object IconRenderer {
    fun drawIcon(matrices: MatrixStack, font: IconFont, c: Char, x: Double, y: Double, color: java.awt.Color): Float {
        return renderIcon(matrices, font, c, x, y, color)
    }

    fun drawCenteredXIcon(
        matrices: MatrixStack,
        font: IconFont,
        c: Char,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderIcon(matrices, font, c, x - font.getWidth(c) / 2.0f, y, color)
    }

    fun drawCenteredYIcon(
        matrices: MatrixStack,
        font: IconFont,
        c: Char,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderIcon(matrices, font, c, x, y + font.lifting / 2.0f + 0.5f, color)
    }

    fun drawCenteredXYIcon(
        matrices: MatrixStack,
        font: IconFont,
        c: Char,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        return renderIcon(matrices, font, c, x - font.getWidth(c) / 2.0f, y + font.lifting / 2.0f + 0.5f, color)
    }

    private fun renderIcon(
        matrices: MatrixStack,
        font: IconFont,
        c: Char,
        x: Double,
        y: Double,
        color: java.awt.Color
    ): Float {
        var y: Double = y
        y -= font.lifting

        val red: Float = color.getRed() / 255.0f
        val green: Float = color.getGreen() / 255.0f
        val blue: Float = color.getBlue() / 255.0f
        val alpha: Float = color.getAlpha() / 255.0f

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)
        matrices.push()
        matrices.scale(0.5f, 0.5f, 1f)

        val matrix: Matrix4f = matrices.peek().model

        font.bindTex()

        val w: Float = font.renderGlyph(matrix, c, x.toFloat() * 2f, y.toFloat() * 2f, red, green, blue, alpha)

        font.unbindTex()

        matrices.pop()
        GlStateManager.disableBlend()

        return w / 2.0f
    }
}