package com.skidders.sigma.utils.render

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.skidders.SigmaReborn
import com.skidders.sigma.utils.mc
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.newdawn.slick.opengl.Texture
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

object RenderUtil {
    private val buffer: java.util.Stack<java.nio.IntBuffer> = java.util.Stack<java.nio.IntBuffer>()

    fun hovered(mouseX: Double, mouseY: Double, x: Float, y: Float, width: Float, height: Float): Boolean {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
    }

    fun drawRectangle(
        matrixStack: MatrixStack,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: java.awt.Color
    ) {
        fill(matrixStack.peek().model, x, y, x + width, y + height, color.rgb)
    }

    private fun fill(matrix: Matrix4f, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        var x1: Float = x1
        var y1: Float = y1
        var x2: Float = x2
        var y2: Float = y2
        if (x1 < x2) {
            val i: Float = x1
            x1 = x2
            x2 = i
        }

        if (y1 < y2) {
            val i: Float = y1
            y1 = y2
            y2 = i
        }

        val f: Float = (color shr 24 and 255).toFloat() / 255.0f
        val g: Float = (color shr 16 and 255).toFloat() / 255.0f
        val h: Float = (color shr 8 and 255).toFloat() / 255.0f
        val j: Float = (color and 255).toFloat() / 255.0f
        val bufferBuilder: BufferBuilder = Tessellator.getInstance().buffer
        RenderSystem.enableBlend()
        RenderSystem.disableTexture()
        RenderSystem.defaultBlendFunc()
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR)
        bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(g, h, j, f).next()
        bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(g, h, j, f).next()
        bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(g, h, j, f).next()
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(g, h, j, f).next()
        bufferBuilder.end()
        BufferRenderer.draw(bufferBuilder)
        RenderSystem.enableTexture()
        RenderSystem.disableBlend()
    }

    private const val STEPS: Int = 60
    private val ANGLE: Double = Math.PI * 2 / STEPS

    fun drawCircle(x: Float, y: Float, radius: Double, color: java.awt.Color) {
        drawSetup()
        GL11.glColor4f(
            color.red / 255.0f,
            color.green / 255.0f,
            color.blue / 255.0f,
            color.alpha / 255.0f
        )
        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        for (i in 0..STEPS) {
            GL11.glVertex2d(
                x + radius * sin(ANGLE * i),
                y + radius * cos(ANGLE * i)
            )
        }
        GL11.glEnd()

        GL11.glLineWidth(1.5f)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)

        GL11.glBegin(GL11.GL_LINE_LOOP)
        for (i in 0..STEPS) {
            GL11.glVertex2d(
                x + radius * sin(ANGLE * i),
                y + radius * cos(ANGLE * i)
            )
        }
        GL11.glEnd()

        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        drawFinish()
    }

    private fun drawSetup() {
        RenderSystem.disableTexture()
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(
            GlStateManager.SrcFactor.SRC_ALPHA.field_22545,
            GlStateManager.SrcFactor.ONE_MINUS_SRC_ALPHA.field_22545
        )
    }

    private fun drawFinish() {
        RenderSystem.enableTexture()
        RenderSystem.disableBlend()
        RenderSystem.clearCurrentColor()
    }

    fun drawImage(image: String?, x: Int, y: Int, width: Float, height: Float, xd: Int, xd2: Int) {
        mc.textureManager.bindTexture(Identifier("sigma-reborn", image))
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        DrawableHelper.drawTexture(MatrixStack(), x, y, 0F, 0F, width.toInt(), height.toInt(), xd, xd2)
        RenderSystem.disableBlend()
    }

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, cornerRadius: Float, color: Int) {
        drawRoundedRect(x, y + cornerRadius, x + width, y + height - cornerRadius, color)
        drawRoundedRect(x + cornerRadius, y, x + width - cornerRadius, y + cornerRadius, color)
        drawRoundedRect(x + cornerRadius, y + height - cornerRadius, x + width - cornerRadius, y + height, color)
        applyScaledScissor(x, y, x + cornerRadius, y + cornerRadius)
        drawPoint(x + cornerRadius, y + cornerRadius, cornerRadius * 2.0f, color)
        restoreScissor()
        applyScaledScissor(x + width - cornerRadius, y, x + width, y + cornerRadius)
        drawPoint(x - cornerRadius + width, y + cornerRadius, cornerRadius * 2.0f, color)
        restoreScissor()
        applyScaledScissor(x, y + height - cornerRadius, x + cornerRadius, y + height)
        drawPoint(x + cornerRadius, y - cornerRadius + height, cornerRadius * 2.0f, color)
        restoreScissor()
        applyScaledScissor(x + width - cornerRadius, y + height - cornerRadius, x + width, y + height)
        drawPoint(x - cornerRadius + width, y - cornerRadius + height, cornerRadius * 2.0f, color)
        restoreScissor()
    }

    fun drawImage(x: Float, y: Float, var2: Float, var3: Float, tex: Texture, alphaValue: Float) {
        drawImage(
            x,
            y,
            var2,
            var3,
            tex,
            ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.color, alphaValue)
        )
    }

    fun drawImage(x: Float, y: Float, var2: Float, var3: Float, tex: Texture, var5: Int) {
        drawImage(
            x,
            y,
            var2,
            var3,
            tex,
            var5,
            0.0f,
            0.0f,
            tex.getImageWidth().toFloat(),
            tex.getImageHeight().toFloat(),
            true
        )
    }

    fun drawImage(
        var0: Float,
        var1: Float,
        var2: Float,
        var3: Float,
        var4: Texture?,
        var5: Int,
        var6: Float,
        var7: Float,
        var8: Float,
        var9: Float,
        var10: Boolean
    ) {
        var var0: Float = var0
        var var1: Float = var1
        var var2: Float = var2
        var var3: Float = var3
        if (var4 != null) {
            RenderSystem.color4f(0.0f, 0.0f, 0.0f, 1.0f)
            GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f)
            var0 = Math.round(var0).toFloat()
            var2 = Math.round(var2).toFloat()
            var1 = Math.round(var1).toFloat()
            var3 = Math.round(var3).toFloat()
            val var13: Float = (var5 shr 24 and 0xFF).toFloat() / 255.0f
            val var14: Float = (var5 shr 16 and 0xFF).toFloat() / 255.0f
            val var15: Float = (var5 shr 8 and 0xFF).toFloat() / 255.0f
            val var16: Float = (var5 and 0xFF).toFloat() / 255.0f
            RenderSystem.enableBlend()
            RenderSystem.disableTexture()
            RenderSystem.blendFuncSeparate(770, 771, 1, 0)
            RenderSystem.color4f(var14, var15, var16, var13)
            GL11.glEnable(3042)
            GL11.glEnable(3553)
            var4.bind()
            val var17: Float = var2 / var4.getTextureWidth().toFloat() / (var2 / var4.getImageWidth().toFloat())
            val var18: Float = var3 / var4.getTextureHeight().toFloat() / (var3 / var4.getImageHeight().toFloat())
            val var19: Float = var8 / var4.getImageWidth().toFloat() * var17
            val var20: Float = var9 / var4.getImageHeight().toFloat() * var18
            val var21: Float = var6 / var4.getImageWidth().toFloat() * var17
            val var22: Float = var7 / var4.getImageHeight().toFloat() * var18
            if (!var10) {
                GL11.glTexParameteri(3553, 10240, 9729)
            } else {
                GL11.glTexParameteri(3553, 10240, 9728)
            }

            GL11.glBegin(7)
            GL11.glTexCoord2f(var21, var22)
            GL11.glVertex2f(var0, var1)
            GL11.glTexCoord2f(var21, var22 + var20)
            GL11.glVertex2f(var0, var1 + var3)
            GL11.glTexCoord2f(var21 + var19, var22 + var20)
            GL11.glVertex2f(var0 + var2, var1 + var3)
            GL11.glTexCoord2f(var21 + var19, var22)
            GL11.glVertex2f(var0 + var2, var1)
            GL11.glEnd()
            GL11.glDisable(3553)
            GL11.glDisable(3042)
            RenderSystem.enableTexture()
            RenderSystem.disableBlend()
        }
    }

    fun applyScaledScissor(x: Float, y: Float, width: Float, height: Float) {
        applyScissorArea(x.toInt(), y.toInt(), width.toInt(), height.toInt(), true)
    }

    fun restoreScissor() {
        if (buffer.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST) // Disable scissor test if buffer is empty
        } else {
            val scissorParams: java.nio.IntBuffer = buffer.pop()
            GL11.glScissor(scissorParams.get(0), scissorParams.get(1), scissorParams.get(2), scissorParams.get(3))
        }
    }

    fun drawPoint(x: Float, y: Float, size: Float, color: Int) {
        RenderSystem.color4f(0.0f, 0.0f, 0.0f, 0.0f)
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f)

        // Extracting the RGBA components from the color integer
        val alpha: Float = (color shr 24 and 0xFF).toFloat() / 255.0f
        val red: Float = (color shr 16 and 0xFF).toFloat() / 255.0f
        val green: Float = (color shr 8 and 0xFF).toFloat() / 255.0f
        val blue: Float = (color and 0xFF).toFloat() / 255.0f

        RenderSystem.disableTexture()
        RenderSystem.blendFuncSeparate(770, 771, 1, 0)
        RenderSystem.color4f(red, green, blue, alpha)

        GL11.glEnable(2832) // Enable point smoothing
        GL11.glEnable(3042) // Enable blending

        GL11.glPointSize(size * SigmaReborn.INSTANCE.screenProcessor.resizingScaleFactor)
        GL11.glBegin(GL11.GL_POINTS)
        GL11.glVertex2f(x, y)
        GL11.glEnd()

        GL11.glDisable(2832) // Disable point smoothing
        GL11.glDisable(3042) // Disable blending

        RenderSystem.enableTexture()
        RenderSystem.disableBlend()
    }

    fun drawRoundedRect(x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        var x1: Float = x1
        var y1: Float = y1
        var x2: Float = x2
        var y2: Float = y2
        if (x1 < x2) {
            val tempX: Int = x1.toInt()
            x1 = x2
            x2 = tempX.toFloat()
        }

        if (y1 < y2) {
            val tempY: Int = y1.toInt()
            y1 = y2
            y2 = tempY.toFloat()
        }

        // Extract RGBA color components from the color integer
        val alpha: Float = (color shr 24 and 0xFF).toFloat() / 255.0f
        val red: Float = (color shr 16 and 0xFF).toFloat() / 255.0f
        val green: Float = (color shr 8 and 0xFF).toFloat() / 255.0f
        val blue: Float = (color and 0xFF).toFloat() / 255.0f

        val tessellator: Tessellator = Tessellator.getInstance()
        val bufferBuilder: BufferBuilder = tessellator.buffer

        RenderSystem.enableBlend()
        RenderSystem.disableTexture()
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ZERO
        )
        RenderSystem.color4f(red, green, blue, alpha)

        bufferBuilder.begin(7, VertexFormats.POSITION)
        bufferBuilder.vertex(x1.toDouble(), y2.toDouble(), 0.0).next()
        bufferBuilder.vertex(x2.toDouble(), y2.toDouble(), 0.0).next()
        bufferBuilder.vertex(x2.toDouble(), y1.toDouble(), 0.0).next()
        bufferBuilder.vertex(x1.toDouble(), y1.toDouble(), 0.0).next()
        tessellator.draw()

        RenderSystem.enableTexture()
        RenderSystem.disableBlend()
    }

    fun applyScissorArea(x: Int, y: Int, width: Int, height: Int, isScaled: Boolean) {
        var x: Int = x
        var y: Int = y
        var width: Int = width
        var height: Int = height
        if (!isScaled) {
            x = (x.toFloat() * SigmaReborn.INSTANCE.screenProcessor.resizingScaleFactor) as Int
            y = (y.toFloat() * SigmaReborn.INSTANCE.screenProcessor.resizingScaleFactor) as Int
            width = (width.toFloat() * SigmaReborn.INSTANCE.screenProcessor.resizingScaleFactor) as Int
            height = (height.toFloat() * SigmaReborn.INSTANCE.screenProcessor.resizingScaleFactor) as Int
        } else {
            val scaledPosition1: FloatArray = getScaledPosition(x, y)
            x = scaledPosition1.get(0).toInt()
            y = scaledPosition1.get(1).toInt()
            val scaledPosition2: FloatArray = getScaledPosition(width, height)
            width = scaledPosition2.get(0).toInt()
            height = scaledPosition2.get(1).toInt()
        }

        if (GL11.glIsEnabled(3089)) {
            val viewportBuffer: java.nio.IntBuffer = BufferUtils.createIntBuffer(16)
            GL11.glGetIntegerv(3088, viewportBuffer)
            buffer.push(viewportBuffer)
            val viewportHeight: Int = viewportBuffer.get(0)
            val viewportY: Int = mc.window.framebufferHeight - viewportBuffer.get(1) - viewportBuffer.get(3)
            val viewportWidth: Int = viewportHeight + viewportBuffer.get(2)
            val viewportMaxHeight: Int = viewportY + viewportBuffer.get(3)

            if (x < viewportHeight) {
                x = viewportHeight
            }

            if (y < viewportY) {
                y = viewportY
            }

            if (width > viewportWidth) {
                width = viewportWidth
            }

            if (height > viewportMaxHeight) {
                height = viewportMaxHeight
            }

            if (y > height) {
                height = y
            }

            if (x > width) {
                width = x
            }
        }

        val scissorY: Int = mc.window.framebufferHeight - height
        val scissorWidth: Int = width - x
        val scissorHeight: Int = height - y

        GL11.glEnable(3089)
        if (scissorWidth >= 0 && scissorHeight >= 0) {
            GL11.glScissor(x, scissorY, scissorWidth, scissorHeight)
        }
    }

    fun getScaledPosition(x: Int, y: Int): FloatArray {
        val matrixBuffer: FloatBuffer = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloatv(2982, matrixBuffer)

        var transformedX: Float =
            matrixBuffer.get(0) * x.toFloat() + matrixBuffer.get(4) * y.toFloat() + matrixBuffer.get(8) * 0.0f + matrixBuffer.get(
                12
            )
        var transformedY: Float =
            matrixBuffer.get(1) * x.toFloat() + matrixBuffer.get(5) * y.toFloat() + matrixBuffer.get(9) * 0.0f + matrixBuffer.get(
                13
            )
        val w: Float =
            matrixBuffer.get(3) * x.toFloat() + matrixBuffer.get(7) * y.toFloat() + matrixBuffer.get(11) * 0.0f + matrixBuffer.get(
                15
            )

        transformedX /= w
        transformedY /= w

        return floatArrayOf(
            Math.round(transformedX * windowScaleFactor).toFloat(),
            Math.round(transformedY * windowScaleFactor).toFloat()
        )
    }

    private val windowScaleFactor: Float
        get() = mc.window.scaleFactor.toFloat()

    fun drawRoundedRect2(var0: Float, var1: Float, var2: Float, var3: Float, var4: Int) {
        drawRoundedRect(var0, var1, var0 + var2, var1 + var3, var4)
    }
}