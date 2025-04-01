package com.skidders.sigma.utils.render.shader

import com.mojang.blaze3d.platform.GlStateManager
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.exp

object StencilUtils {
    private val kernelCache: HashMap<Int, FloatBuffer> = HashMap()

    fun getKernel(radius: Int): FloatBuffer {
        var buffer: FloatBuffer? = kernelCache[radius]
        if (buffer == null) {
            buffer = BufferUtils.createFloatBuffer(radius)
            val kernel = FloatArray(radius)
            val sigma = radius / 2.0f
            var total = 0.0f
            for (i in 0..<radius) {
                val multiplier: Float = i / sigma
                kernel[i] = 1.0f / (abs(sigma) * 2.5066283f) * exp(-0.5 * multiplier * multiplier).toFloat()
                total += if (i > 0) kernel[i] * 2 else kernel[0]
            }
            for (i in 0..<radius) {
                kernel[i] /= total
            }
            buffer.put(kernel)
            buffer.flip()
            kernelCache[radius] = buffer
        }
        return buffer!!
    }

    fun initStencilReplace() {
        GL11.glEnable(GL11.GL_STENCIL_TEST)
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT)
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1)
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE)
        GlStateManager.colorMask(false, false, false, false)
    }

    fun uninitStencilReplace() {
        GlStateManager.colorMask(true, true, true, true)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP)
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1)
    }
}