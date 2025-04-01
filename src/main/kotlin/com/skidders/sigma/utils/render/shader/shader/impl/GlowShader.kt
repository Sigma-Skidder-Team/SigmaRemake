package com.skidders.sigma.utils.render.shader.shader.impl

import java.awt.image.BufferedImage
import java.awt.image.Kernel
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.sqrt

class GlowShader(radius: Int) {
    private val radius: Float
    private var alpha: Boolean = true
    private var premultiplyAlpha: Boolean = true
    private var kernel: Kernel

    init {
        this.radius = radius.toFloat()
        kernel = kernelCache.getOrDefault(radius, null) ?: makeKernel(radius.toFloat()); kernelCache[radius] =
            kernel
    }

    fun setUseAlpha(useAlpha: Boolean) {
        this.alpha = useAlpha
    }

    fun setPremultiplyAlpha(premultiplyAlpha: Boolean) {
        this.premultiplyAlpha = premultiplyAlpha
    }

    fun filter(src: BufferedImage, pDst: BufferedImage?): BufferedImage {
        val dst: BufferedImage = pDst ?: createCompatibleDestImage(src)
        val width: Int = src.width
        val height: Int = src.height

        val inPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        src.getRGB(0, 0, width, height, inPixels, 0, width)

        if (radius > 0) {
            convolveAndTranspose(
                kernel,
                inPixels,
                outPixels,
                width,
                height,
                alpha,
                alpha && premultiplyAlpha,
                false,
                CLAMP_EDGES
            )
            convolveAndTranspose(
                kernel,
                outPixels,
                inPixels,
                height,
                width,
                alpha,
                false,
                alpha && premultiplyAlpha,
                CLAMP_EDGES
            )
        }

        dst.setRGB(0, 0, width, height, inPixels, 0, width)
        return dst
    }

    private fun createCompatibleDestImage(src: BufferedImage, dstCM: java.awt.image.ColorModel = src.colorModel): BufferedImage {
        return BufferedImage(
            dstCM,
            dstCM.createCompatibleWritableRaster(src.width, src.height),
            dstCM.isAlphaPremultiplied,
            null
        )
    }

    companion object {
        private val kernelCache: HashMap<Int, Kernel> = HashMap<Int, Kernel>()
        const val CLAMP_EDGES: Int = 1
        private const val WRAP_EDGES: Int = 2
        fun convolveAndTranspose(
            kernel: Kernel,
            inPixels: IntArray,
            outPixels: IntArray,
            width: Int,
            height: Int,
            alpha: Boolean,
            premultiply: Boolean,
            unPremultiply: Boolean,
            edgeAction: Int
        ) {
            val matrix: FloatArray = kernel.getKernelData(null)
            val cols: Int = kernel.width
            val cols2: Int = cols / 2

            for (y in 0..<height) {
                var index: Int = y
                val iOffset: Int = y * width
                for (x in 0..<width) {
                    var r: Float = 0f
                    var g: Float = 0f
                    var b: Float = 0f
                    var a: Float = 0f
                    val mOffset: Int = cols2
                    for (col in -cols2..cols2) {
                        val f: Float = matrix[mOffset + col]

                        if (f != 0f) {
                            var ix: Int = x + col
                            if (ix < 0) {
                                if (edgeAction == CLAMP_EDGES) ix = 0
                                else if (edgeAction == WRAP_EDGES) ix = (x + width) % width
                            } else if (ix >= width) {
                                if (edgeAction == CLAMP_EDGES) ix = width - 1
                                else if (edgeAction == WRAP_EDGES) ix = (x + width) % width
                            }
                            val rgb: Int = inPixels.get(iOffset + ix)
                            val pa: Int = (rgb shr 24) and 0xff
                            var pr: Int = (rgb shr 16) and 0xff
                            var pg: Int = (rgb shr 8) and 0xff
                            var pb: Int = rgb and 0xff
                            if (premultiply) {
                                val a255: Float = pa * (1.0f / 255.0f)
                                pr = (pr * a255).toInt()
                                pg = (pg * a255).toInt()
                                pb = (pb * a255).toInt()
                            }
                            a += f * pa
                            r += f * pr
                            g += f * pg
                            b += f * pb
                        }
                    }
                    if (unPremultiply && a != 0f && a != 255f) {
                        val f: Float = 255.0f / a
                        r *= f
                        g *= f
                        b *= f
                    }
                    val ia: Int = if (alpha) clamp((a + 0.5).toInt()) else 0xff
                    val ir: Int = clamp((r + 0.5).toInt())
                    val ig: Int = clamp((g + 0.5).toInt())
                    val ib: Int = clamp((b + 0.5).toInt())
                    outPixels[index] = (ia shl 24) or (ir shl 16) or (ig shl 8) or ib
                    index += height
                }
            }
        }

        private fun clamp(c: Int): Int {
            if (c < 0) return 0
            if (c > 255) return 255
            return c
        }

        fun makeKernel(radius: Float): Kernel {
            val r: Int = ceil(radius).toInt()
            val rows: Int = r * 2 + 1
            val matrix = FloatArray(rows)
            val sigma: Float = radius / 3
            val sigma22: Float = 2 * sigma * sigma
            val sigmaPi2: Float = 2 * Math.PI.toFloat() * sigma
            val sqrtSigmaPi2: Float = sqrt(sigmaPi2)
            val radius2: Float = radius * radius
            var total: Float = 0f
            var index: Int = 0
            for (row in -r..r) {
                val distance: Float = (row * row).toFloat()
                if (distance > radius2) matrix[index] = 0f
                else matrix[index] = exp(-(distance) / sigma22) as Float / sqrtSigmaPi2
                total += matrix[index]
                index++
            }
            for (i in 0..<rows) matrix[index] /= total

            return Kernel(rows, 1, matrix)
        }
    }
}