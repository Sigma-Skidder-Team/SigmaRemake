package com.skidders.sigma.utils.render.shader

import com.mojang.blaze3d.platform.GlStateManager.*
import com.skidders.SigmaReborn
import com.skidders.sigma.utils.mc
import com.skidders.sigma.utils.render.ColorUtil
import com.skidders.sigma.utils.render.image.TextureLoader
import com.skidders.sigma.utils.render.shader.shader.Shader
import com.skidders.sigma.utils.render.shader.shader.impl.GlowShader
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.sin

object ShaderRenderUtil {
    val glowCache: HashMap<Int, Int> = HashMap<Int, Int>()
    private val ROUNDED: Shader = Shader("rounded.frag")
    private val ROUNDED_GRADIENT: Shader = Shader("rounded_gradient.frag")
    private val ROUNDED_BLURRED: Shader = Shader("rounded_blurred.frag")
    private val ROUNDED_BLURRED_GRADIENT: Shader = Shader("rounded_blurred_gradient.frag")
    private val ROUNDED_OUTLINE: Shader = Shader("rounded_outline.frag")
    private val ROUNDED_TEXTURE: Shader = Shader("rounded_texture.frag")
    const val STEPS: Int = 60
    val ANGLE: Double = java.lang.Math.PI * 2 / STEPS
    const val EX_STEPS: Int = 120
    val EX_ANGLE: Double = java.lang.Math.PI * 2 / EX_STEPS

    fun drawCircle(x: Double, y: Double, radius: Double, color: Color) {
        drawSetup()
        applyColor(color)

        glBegin(GL_TRIANGLE_FAN)
        for (i in 0..STEPS) {
            glVertex2d(
                x + radius * sin(ANGLE * i),
                y + radius * cos(ANGLE * i)
            )
        }
        glEnd()

        glLineWidth(1.5f)
        glEnable(GL_LINE_SMOOTH)

        glBegin(GL_LINE_LOOP)
        for (i in 0..STEPS) {
            glVertex2d(
                x + radius * sin(ANGLE * i),
                y + radius * cos(ANGLE * i)
            )
        }
        glEnd()

        glDisable(GL_LINE_SMOOTH)
        drawFinish()
    }

    // progress [1;100]
    // direction: 1 - по часовой стрелке; 0 - против часовой стрелки
    fun drawCircle(x: Double, y: Double, radius: Double, progress: Int, direction: Int, color: Color) {
        val angle1: Double = if (direction == 0) ANGLE else -ANGLE
        val steps: Float = (STEPS / 100f) * progress

        drawSetup()
        applyColor(color)

        glBegin(GL_TRIANGLE_FAN)
        glVertex2d(x, y)
        run {
            var i: Int = 0
            while (i <= steps) {
                glVertex2d(
                    x + radius * sin(angle1 * i),
                    y + radius * cos(ANGLE * i)
                )
                i++
            }
        }
        glEnd()

        glLineWidth(1.5f)
        glEnable(GL_LINE_SMOOTH)

        glBegin(GL_LINE_LOOP)
        glVertex2d(x, y)
        var i: Int = 0
        while (i <= steps) {
            glVertex2d(
                x + radius * sin(angle1 * i),
                y + radius * cos(ANGLE * i)
            )
            i++
        }
        glEnd()

        glDisable(GL_LINE_SMOOTH)
        drawFinish()
    }

    fun drawCirclePart(x: Double, y: Double, radius: Double, part: Part, color: Color) {
        val angle: Double = ANGLE / part.ratio

        drawSetup()
        applyColor(color)

        glBegin(GL_TRIANGLE_FAN)
        glVertex2d(x, y)
        for (i in 0..STEPS) {
            glVertex2d(
                x + radius * sin(part.additionalAngle + angle * i),
                y + radius * cos(part.additionalAngle + angle * i)
            )
        }
        glEnd()

        glLineWidth(1.5f)
        glEnable(GL_LINE_SMOOTH)

        glBegin(GL_LINE_LOOP)
        glVertex2d(x, y)
        for (i in 0..STEPS) {
            glVertex2d(
                x + radius * sin(part.additionalAngle + angle * i),
                y + radius * cos(part.additionalAngle + angle * i)
            )
        }
        glEnd()

        glDisable(GL_LINE_SMOOTH)
        drawFinish()
    }

    fun drawBlurredCircle(x: Double, y: Double, radius: Double, blurRadius: Double, color: Color) {
        val transparent: Color = ColorUtil.injectAlpha(color, 0)

        drawSetup()
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0001f)
        glShadeModel(GL_SMOOTH)
        applyColor(color)

        glBegin(GL_TRIANGLE_FAN)
        for (i in 0..EX_STEPS) {
            glVertex2d(
                x + radius * sin(EX_ANGLE * i),
                y + radius * cos(EX_ANGLE * i)
            )
        }
        glEnd()

        glBegin(GL_TRIANGLE_STRIP)
        for (i in 0..EX_STEPS + 1) {
            if (i % 2 == 1) {
                applyColor(transparent)
                glVertex2d(
                    x + (radius + blurRadius) * sin(EX_ANGLE * i),
                    y + (radius + blurRadius) * cos(EX_ANGLE * i)
                )
            } else {
                applyColor(color)
                glVertex2d(
                    x + radius * sin(EX_ANGLE * i),
                    y + radius * cos(EX_ANGLE * i)
                )
            }
        }
        glEnd()

        glShadeModel(GL_FLAT)
        glDisable(GL_ALPHA_TEST)
        drawFinish()
    }

    fun drawCircleOutline(x: Double, y: Double, radius: Double, thikness: Float, color: Color) {
        drawSetup()
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(thikness)
        applyColor(color)

        glBegin(GL_LINE_LOOP)
        for (i in 0..STEPS) {
            glVertex2d(
                x + radius * sin(ANGLE * i),
                y + radius * cos(ANGLE * i)
            )
        }
        glEnd()

        glDisable(GL_LINE_SMOOTH)
        drawFinish()
    }

    // progress [1;100]
    // direction: 1 - по часовой стрелке; 0 - против часовой стрелки
    fun drawCircleOutline(
        x: Double,
        y: Double,
        radius: Double,
        thikness: Float,
        progress: Int,
        direction: Int,
        color: Color
    ) {
        val angle1: Double = if (direction == 0) ANGLE else -ANGLE
        val steps: Float = (STEPS / 100f) * progress

        drawSetup()
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(thikness)
        applyColor(color)

        glBegin(GL_LINE_STRIP)
        var i: Int = 0
        while (i <= steps) {
            glVertex2d(
                x + radius * sin(angle1 * i),
                y + radius * cos(ANGLE * i)
            )
            i++
        }
        glEnd()

        glDisable(GL_LINE_SMOOTH)
        drawFinish()
    }

    fun drawRainbowCircle(x: Double, y: Double, radius: Double, blurRadius: Double) {
        drawSetup()
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0001f)
        glShadeModel(GL_SMOOTH)
        applyColor(Color.WHITE)

        glBegin(GL_TRIANGLE_FAN)
        glVertex2d(x, y)
        for (i in 0..EX_STEPS) {
            applyColor(Color.getHSBColor(i.toFloat() / EX_STEPS, 1f, 1f))
            glVertex2d(
                x + radius * sin(EX_ANGLE * i),
                y + radius * cos(EX_ANGLE * i)
            )
        }
        glEnd()

        glBegin(GL_TRIANGLE_STRIP)
        for (i in 0..EX_STEPS + 1) {
            if (i % 2 == 1) {
                applyColor(ColorUtil.injectAlpha(Color.getHSBColor(i.toFloat() / EX_STEPS, 1f, 1f), 0))
                glVertex2d(
                    x + (radius + blurRadius) * sin(EX_ANGLE * i),
                    y + (radius + blurRadius) * cos(EX_ANGLE * i)
                )
            } else {
                applyColor(Color.getHSBColor(i.toFloat() / EX_STEPS, 1f, 1f))
                glVertex2d(
                    x + radius * sin(EX_ANGLE * i),
                    y + radius * cos(EX_ANGLE * i)
                )
            }
        }
        glEnd()

        glShadeModel(GL_FLAT)
        glDisable(GL_ALPHA_TEST)
        drawFinish()
    }

    fun drawRect(x: Double, y: Double, width: Double, height: Double, color: Color) {
        drawSetup()
        applyColor(color)

        glBegin(GL_QUADS)
        glVertex2d(x, y)
        glVertex2d(x + width, y)
        glVertex2d(x + width, y - height)
        glVertex2d(x, y - height)
        glEnd()

        drawFinish()
    }

    fun drawGradientRect(x: Double, y: Double, width: Double, height: Double, vararg clrs: Color) {
        drawSetup()
        glShadeModel(GL_SMOOTH)

        glBegin(GL_QUADS)
        applyColor(clrs[1])
        glVertex2d(x, y)
        applyColor(clrs[2])
        glVertex2d(x + width, y)
        applyColor(clrs[3])
        glVertex2d(x + width, y - height)
        applyColor(clrs[0])
        glVertex2d(x, y - height)
        glEnd()

        glShadeModel(GL_FLAT)
        drawFinish()
    }

    fun drawRoundedRect(x: Double, y: Double, width: Double, height: Double, radius: Double, color: Color) {
        val c: FloatArray = ColorUtil.getColorComps(color)

        drawSetup()

        ROUNDED.load()
        ROUNDED.setUniformf("size", width.toFloat() * 2, height.toFloat() * 2)
        ROUNDED.setUniformf("round", radius.toFloat() * 2)
        ROUNDED.setUniformf("color", c[0], c[1], c[2], c[3])
        Shader.draw(x, y, width, height)
        ROUNDED.unload()

        drawFinish()
    }

    fun drawRoundedHorizontalGradientRect(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        radius: Double,
        topLeftColor: Color,
        bottomLeftColor: Color,
        topRightColor: Color,
        bottomRightColor: Color
    ) {
        val c: FloatArray = ColorUtil.getColorComps(topLeftColor) //top left
        val c1: FloatArray = ColorUtil.getColorComps(bottomLeftColor) //bottom left
        val c2: FloatArray = ColorUtil.getColorComps(topRightColor) //top right
        val c3: FloatArray = ColorUtil.getColorComps(bottomRightColor) //bottom right

        drawSetup()

        ROUNDED_GRADIENT.load()
        ROUNDED_GRADIENT.setUniformf("size", width.toFloat() * 2, height.toFloat() * 2)
        ROUNDED_GRADIENT.setUniformf("round", radius.toFloat() * 2)
        ROUNDED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3])
        ROUNDED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3])
        ROUNDED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3])
        ROUNDED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3])
        Shader.draw(x, y, width, height)
        ROUNDED_GRADIENT.unload()

        drawFinish()
    }

    fun drawRoundedVerticalGradientRect(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        radius: Double,
        color1: Color,
        color2: Color
    ) {
        val c: FloatArray = ColorUtil.getColorComps(color1) //top left
        val c1: FloatArray = ColorUtil.getColorComps(color2) //bottom left
        val c2: FloatArray = ColorUtil.getColorComps(color1) //top right
        val c3: FloatArray = ColorUtil.getColorComps(color2) //bottom right

        drawSetup()

        ROUNDED_GRADIENT.load()
        ROUNDED_GRADIENT.setUniformf("size", width.toFloat() * 2, height.toFloat() * 2)
        ROUNDED_GRADIENT.setUniformf("round", radius.toFloat() * 2)
        ROUNDED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3])
        ROUNDED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3])
        ROUNDED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3])
        ROUNDED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3])
        Shader.draw(x, y, width, height)
        ROUNDED_GRADIENT.unload()

        drawFinish()
    }

    fun drawRoundedHorizontalGradientRect(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        radius: Double,
        color1: Color,
        color2: Color
    ) {
        val c: FloatArray = ColorUtil.getColorComps(color1) //top left
        val c1: FloatArray = ColorUtil.getColorComps(color1) //bottom left
        val c2: FloatArray = ColorUtil.getColorComps(color2) //top right
        val c3: FloatArray = ColorUtil.getColorComps(color2) //bottom right

        drawSetup()

        ROUNDED_GRADIENT.load()
        ROUNDED_GRADIENT.setUniformf("size", width.toFloat() * 2, height.toFloat() * 2)
        ROUNDED_GRADIENT.setUniformf("round", radius.toFloat() * 2)
        ROUNDED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3])
        ROUNDED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3])
        ROUNDED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3])
        ROUNDED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3])
        Shader.draw(x, y, width, height)
        ROUNDED_GRADIENT.unload()

        drawFinish()
    }

    fun drawRoundedBlurredRect(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        roundR: Double,
        blurR: Float,
        color: Color
    ) {
        val c: FloatArray = ColorUtil.getColorComps(color)

        drawSetup()
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0001f)

        ROUNDED_BLURRED.load()
        ROUNDED_BLURRED.setUniformf("size", (width + 2 * blurR).toFloat(), (height + 2 * blurR).toFloat())
        ROUNDED_BLURRED.setUniformf("softness", blurR)
        ROUNDED_BLURRED.setUniformf("radius", roundR.toFloat())
        ROUNDED_BLURRED.setUniformf("color", c[0], c[1], c[2], c[3])
        Shader.draw(x - blurR, y - blurR, width + blurR * 2, height + blurR * 2)
        ROUNDED_BLURRED.unload()

        glDisable(GL_ALPHA_TEST)
        drawFinish()
    }

    fun drawRoundedGradientBlurredRect(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        roundR: Double,
        blurR: Float,
        vararg colors: Color
    ) {
        val c: FloatArray = ColorUtil.getColorComps(colors[0])
        val c1: FloatArray = ColorUtil.getColorComps(colors[1])
        val c2: FloatArray = ColorUtil.getColorComps(colors[2])
        val c3: FloatArray = ColorUtil.getColorComps(colors[3])

        drawSetup()
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0001f)

        ROUNDED_BLURRED_GRADIENT.load()
        ROUNDED_BLURRED_GRADIENT.setUniformf("size", (width + 2 * blurR).toFloat(), (height + 2 * blurR).toFloat())
        ROUNDED_BLURRED_GRADIENT.setUniformf("softness", blurR)
        ROUNDED_BLURRED_GRADIENT.setUniformf("radius", roundR.toFloat())
        ROUNDED_BLURRED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3])
        ROUNDED_BLURRED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3])
        ROUNDED_BLURRED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3])
        ROUNDED_BLURRED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3])
        Shader.draw(x - blurR, y - blurR, width + blurR * 2, height + blurR * 2)
        ROUNDED_BLURRED_GRADIENT.unload()

        glDisable(GL_ALPHA_TEST)
        drawFinish()
    }

    fun drawSmoothRect(x: Double, y: Double, width: Double, height: Double, color: Color) {
        drawRoundedRect(x, y, width, height, 1.5, color)
    }

    fun drawRoundedRectOutline(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        radius: Double,
        thickness: Float,
        color: Color
    ) {
        val c: FloatArray = ColorUtil.getColorComps(color)

        drawSetup()

        ROUNDED_OUTLINE.load()
        ROUNDED_OUTLINE.setUniformf("size", width.toFloat() * 2, height.toFloat() * 2)
        ROUNDED_OUTLINE.setUniformf("round", radius.toFloat() * 2)
        ROUNDED_OUTLINE.setUniformf("thickness", thickness)
        ROUNDED_OUTLINE.setUniformf("color", c[0], c[1], c[2], c[3])
        Shader.draw(x, y, width, height)
        ROUNDED_OUTLINE.unload()

        drawFinish()
    }

    fun drawTexture(
        identifier: Identifier?,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        texX: Double,
        texY: Double,
        texWidth: Double,
        texHeight: Double
    ) {
        drawTexture(TextureLoader.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight)
    }

    fun drawTexture(
        texId: Int,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        texX: Double,
        texY: Double,
        texWidth: Double,
        texHeight: Double
    ) {
        var y: Double = y
        var texX: Double = texX
        var texY: Double = texY
        var texWidth: Double = texWidth
        var texHeight: Double = texHeight
        enableBlend()
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        resetColor()

        bindTexture(texId)

        val iWidth: Int = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH)
        val iHeight: Int = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT)
        y -= height
        texX = texX / iWidth
        texY = texY / iHeight
        texWidth = texWidth / iWidth
        texHeight = texHeight / iHeight

        glBegin(GL_QUADS)
        glTexCoord2d(texX, texY)
        glVertex2d(x, y)
        glTexCoord2d(texX, texY + texHeight)
        glVertex2d(x, y + height)
        glTexCoord2d(texX + texWidth, texY + texHeight)
        glVertex2d(x + width, y + height)
        glTexCoord2d(texX + texWidth, texY)
        glVertex2d(x + width, y)
        glEnd()

        bindTexture(0)
        disableBlend()
    }

    fun drawRoundedTexture(
        identifier: Identifier?,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        texX: Double,
        texY: Double,
        texWidth: Double,
        texHeight: Double,
        radius: Double
    ) {
        drawRoundedTexture(
            TextureLoader.getTextureId(identifier),
            x,
            y,
            width,
            height,
            texX,
            texY,
            texWidth,
            texHeight,
            radius
        )
    }

    fun drawRoundedTexture(
        texId: Int,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        texX: Double,
        texY: Double,
        texWidth: Double,
        texHeight: Double,
        radius: Double
    ) {
        var y: Double = y
        var texX: Double = texX
        var texY: Double = texY
        var texWidth: Double = texWidth
        var texHeight: Double = texHeight
        enableBlend()
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.3f)

        mc.getFramebuffer().beginWrite(false)
        StencilUtils.initStencilReplace()
        drawRoundedRect(x, y, width, height, radius, Color.WHITE)
        StencilUtils.uninitStencilReplace()

        bindTexture(texId)

        val iWidth: Int = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH)
        val iHeight: Int = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT)
        y -= height
        texX = texX / iWidth
        texY = texY / iHeight
        texWidth = texWidth / iWidth
        texHeight = texHeight / iHeight

        glBegin(GL_QUADS)
        glTexCoord2d(texX, texY)
        glVertex2d(x, y)
        glTexCoord2d(texX, texY + texHeight)
        glVertex2d(x, y + height)
        glTexCoord2d(texX + texWidth, texY + texHeight)
        glVertex2d(x + width, y + height)
        glTexCoord2d(texX + texWidth, texY)
        glVertex2d(x + width, y)
        glEnd()

        bindTexture(0)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_ALPHA_TEST)
        disableBlend()
    }

    fun drawRoundedTexture(
        identifier: Identifier?,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        radius: Double
    ) {
        drawRoundedTexture(TextureLoader.getTextureId(identifier), x, y, width, height, radius)
    }

    fun drawRoundedTexture(texId: Int, x: Double, y: Double, width: Double, height: Double, radius: Double) {
        enableBlend()
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        resetColor()

        ROUNDED_TEXTURE.load()
        ROUNDED_TEXTURE.setUniformf("size", width.toFloat() * 2, height.toFloat() * 2)
        ROUNDED_TEXTURE.setUniformf("round", radius.toFloat() * 2)
        bindTexture(texId)
        Shader.draw(x, y, width, height)
        bindTexture(0)
        ROUNDED_TEXTURE.unload()

        disableBlend()
    }

    fun drawGlow(x: Double, y: Double, width: Int, height: Int, glowRadius: Int, color: Color) {
        var x: Double = x
        var y: Double = y
        var width: Int = width
        var height: Int = height
        enableBlend()
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0001f)

        bindTexture(getGlowTexture(width, height, glowRadius))
        width += glowRadius * 2
        height += glowRadius * 2
        x -= glowRadius.toDouble()
        y -= glowRadius.toDouble()

        applyColor(color)
        glBegin(GL_QUADS)
        glTexCoord2d(0.0, 1.0)
        glVertex2d(x, y)
        glTexCoord2d(0.0, 0.0)
        glVertex2d(x, y + height)
        glTexCoord2d(1.0, 0.0)
        glVertex2d(x + width, y + height)
        glTexCoord2d(1.0, 1.0)
        glVertex2d(x + width, y)
        glEnd()

        bindTexture(0)
        glDisable(GL_ALPHA_TEST)
        disableBlend()
    }

    fun getGlowTexture(width: Int, height: Int, blurRadius: Int): Int {
        val identifier: Int = (width * 401 + height) * 407 + blurRadius
        var texId: Int = glowCache.getOrDefault(identifier, -1)

        if (texId == -1) {
            val original =
                BufferedImage(width + blurRadius * 2, height + blurRadius * 2, BufferedImage.TYPE_INT_ARGB_PRE)

            val g = original.getGraphics()
            g.color = Color.WHITE
            g.fillRect(blurRadius, blurRadius, width, height)
            g.dispose()

            val glow: GlowShader = GlowShader(blurRadius)
            val blurred: BufferedImage = glow.filter(original, null)
            try {
                texId = TextureLoader.loadTexture2(blurred)
                glowCache[identifier] = texId
            } catch (e: java.lang.Exception) {
                SigmaReborn.LOGGER.error(e.message)
            }
        }
        return texId
    }

    // для scaledHeight, scale - юзайте WINDOW.getGuiScaledHeight(), WINDOW.getGuiScale() если сами не меняли их
    fun scissor(x: Double, y: Double, width: Double, height: Double, scale: Double, scaledHeight: Double) {
        glScissor(
            (x * scale).toInt(),
            ((scaledHeight - y) * scale).toInt(),
            (width * scale).toInt(),
            (height * scale).toInt()
        )
    }

    fun applyColor(color: Color) {
        glColor4f(
            color.getRed() / 255.0f,
            color.getGreen() / 255.0f,
            color.getBlue() / 255.0f,
            color.getAlpha() / 255.0f
        )
    }

    fun resetColor() {
        glColor4f(1f, 1f, 1f, 1f)
    }

    fun enableScissor() {
        glEnable(GL_SCISSOR_TEST)
    }

    fun disableScissor() {
        glDisable(GL_SCISSOR_TEST)
    }

    fun drawSetup() {
        disableTexture()
        enableBlend()
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    fun drawFinish() {
        enableTexture()
        disableBlend()
        resetColor()
    }

    enum class Part(internal val ratio: Int, val additionalAngle: Double) {
        FIRST_QUARTER(4, java.lang.Math.PI / 2),
        SECOND_QUARTER(4, java.lang.Math.PI),
        THIRD_QUARTER(4, 3 * java.lang.Math.PI / 2),
        FOURTH_QUARTER(4, 0.0),
        FIRST_HALF(2, java.lang.Math.PI / 2),
        SECOND_HALF(2, java.lang.Math.PI),
        THIRD_HALF(2, 3 * java.lang.Math.PI / 2),
        FOURTH_HALF(2, 0.0)
    }
}