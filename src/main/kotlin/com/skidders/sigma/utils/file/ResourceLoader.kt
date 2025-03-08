package com.skidders.sigma.utils.file

import org.newdawn.slick.opengl.Texture
import org.newdawn.slick.opengl.TextureLoader

import java.awt.image.BufferedImage
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.sqrt

class ResourceLoader {
    companion object {

        @Throws(IOException::class)
        fun loadTexture(filePath: String): Texture {
            val extension = filePath.substring(filePath.lastIndexOf(".") + 1).uppercase(Locale.getDefault())
            return loadTexture(filePath, extension)
        }

        @Throws(IOException::class)
        fun loadTexture(filePath: String, fileType: String): Texture {
            val inputStream = readInputStream(filePath)
            return TextureLoader.getTexture(fileType, inputStream)
        }

        @Throws(IOException::class)
        fun readInputStream(fileName: String): InputStream {
            val assetPath = "assets/sigma-reborn/$fileName"
            val resourceStream = ResourceLoader::class.java.classLoader.getResourceAsStream(assetPath)
            if (resourceStream != null) {
                return resourceStream
            } else {
                throw IllegalStateException("Resource not found: $assetPath")
            }
        }

        fun applyGaussianBlur(inputImage: BufferedImage, radius: Int): BufferedImage? {
            if (inputImage == null) {
                return null
            }

            if (inputImage.width > radius * 2 && inputImage.height > radius * 2) {
                val blurOperation = ConvolveOp(createGaussianKernel(radius.toFloat()))

                // Apply the Gaussian blur
                var blurredImage = blurOperation.filter(inputImage, null)
                blurredImage = blurOperation.filter(applyEdgeWrap(blurredImage), null)
                blurredImage = applyEdgeWrap(blurredImage)

                // Crop the image to remove the blurred edges
                return blurredImage.getSubimage(
                    radius,
                    radius,
                    inputImage.width - radius * 2,
                    inputImage.height - radius * 2
                )
            } else {
                return inputImage
            }
        }

        public fun applyEdgeWrap(inputImage: BufferedImage): BufferedImage {
            val width = inputImage.width
            val height = inputImage.height

            // Create a new BufferedImage with swapped width and height
            val wrappedImage = BufferedImage(height, width, inputImage.type)

            // Copy pixels from the input image to the wrapped image with reversed coordinates
            for (x in 0..width) {
                for (y in 0..height) {
                    wrappedImage.setRGB(height - 1 - y, width - 1 - x, inputImage.getRGB(x, y))
                }
            }

            return wrappedImage
        }

        fun createGaussianKernel(radius: Float): Kernel {
            val kernelRadius = (ceil(radius.toDouble()).toInt())
            val kernelSize = kernelRadius * 2 + 1
            val kernelData = FloatArray(kernelSize)

            val standardDeviation = radius / 3.0F
            val twoSigmaSquared = 2.0F * standardDeviation * standardDeviation
            val normalizationFactor = (sqrt(Math.PI * 2.0) * standardDeviation).toFloat()
            val maxDistanceSquared = radius * radius
            var sum = 0.0F
            var index = 0

            for (offset in -kernelRadius..kernelRadius) {
                val distanceSquared = offset * offset
                if (distanceSquared <= maxDistanceSquared) {
                    kernelData[index] = exp((-distanceSquared / twoSigmaSquared).toDouble()).toFloat() / normalizationFactor
                } else {
                    kernelData[index] = 0.0F
                }

                sum += kernelData[index]
                index++
            }

            for (i in 0 until kernelSize) {
                kernelData[i] /= sum
            }

            return Kernel(kernelSize, 1, kernelData)
        }

    }
}
