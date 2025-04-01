package com.skidders.sigma.utils.file

import org.newdawn.slick.opengl.Texture
import org.newdawn.slick.opengl.TextureLoader
import org.newdawn.slick.util.BufferedImageUtil
import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.*

object ResourceLoader {
    fun loadTexture(filePath: String): Texture {
        try {
            val extension = filePath.substring(filePath.lastIndexOf(".") + 1).uppercase(Locale.getDefault())
            return loadTexture(filePath, extension)
        } catch (e: Exception) {
            System.err.println(
                "Unable to load texture " + filePath +
                        ". Please make sure it is a valid path and has a valid extension."
            )
            throw e
        }
    }

    fun loadTexture(filePath: String, fileType: String?): Texture {
        try {
            return TextureLoader.getTexture(fileType, readInputStream(filePath))
        } catch (e: IOException) {
            try {
                readInputStream(filePath).use { inputStream ->
                    val header = ByteArray(8)
                    inputStream.read(header)
                    val headerInfo = StringBuilder()
                    for (value in header) {
                        headerInfo.append(" ").append(value)
                    }
                    throw IllegalStateException("Unable to load texture $filePath header: $headerInfo")
                }
            } catch (ex: IOException) {
                throw IllegalStateException("Unable to load texture $filePath", ex)
            }
        }
    }

    fun readInputStream(fileName: String): InputStream {
        try {
            // The file path within the sigma-reborn folder.
            val assetPath = "assets/sigma-reborn/$fileName"

            // Attempt to load the resource directly from the classpath
            val resourceStream = ResourceLoader::class.java.classLoader.getResourceAsStream(assetPath)

            if (resourceStream != null) {
                return resourceStream
            } else {
                throw IllegalStateException("Resource not found: $assetPath")
            }
        } catch (e: Exception) {
            throw IllegalStateException(
                "Unable to load resource $fileName. Error during resource loading.", e
            )
        }
    }

    fun createGaussianKernel(radius: Float): Kernel {
        val kernelRadius = ceil(radius.toDouble()).toInt()
        val kernelSize = kernelRadius * 2 + 1 // Size of the kernel (diameter)
        val kernelData = FloatArray(kernelSize)

        // Standard deviation for Gaussian distribution
        val standardDeviation = radius / 3.0f
        val twoSigmaSquared = 2.0f * standardDeviation * standardDeviation
        val normalizationFactor = (sqrt(Math.PI * 2) * standardDeviation).toFloat()
        val maxDistanceSquared = radius * radius
        var sum = 0.0f
        var index = 0

        // Populate the kernel data
        for (offset in -kernelRadius..kernelRadius) {
            val distanceSquared = (offset * offset).toFloat()
            if (distanceSquared <= maxDistanceSquared) {
                kernelData[index] = exp((-distanceSquared / twoSigmaSquared).toDouble()).toFloat() / normalizationFactor
            } else {
                kernelData[index] = 0.0f
            }

            sum += kernelData[index]
            index++
        }

        // Normalize the kernel values so they sum up to 1
        for (i in 0..<kernelSize) {
            kernelData[i] /= sum
        }

        return Kernel(kernelSize, 1, kernelData)
    }

    fun applyGaussianBlur(inputImage: BufferedImage?, radius: Int): BufferedImage? {
        if (inputImage == null) {
            return null // Return null if the input image is null
        }

        // Ensure the image is large enough for the specified blur radius
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
            return inputImage // Return the original image if it's too small for the blur
        }
    }

    fun applyEdgeWrap(inputImage: BufferedImage): BufferedImage {
        val width = inputImage.width
        val height = inputImage.height

        // Create a new BufferedImage with swapped width and height
        val wrappedImage = BufferedImage(height, width, inputImage.type)

        // Copy pixels from the input image to the wrapped image with reversed coordinates
        for (x in 0..<width) {
            for (y in 0..<height) {
                wrappedImage.setRGB(height - 1 - y, width - 1 - x, inputImage.getRGB(x, y))
            }
        }

        return wrappedImage
    }

    fun generateTexture(imagePath: String, scale: Float, blurRadius: Int): Texture {
        try {
            // Read the image from the specified path
            val originalImage = ImageIO.read(readInputStream(imagePath))

            // Scale the image
            val scaledWidth = (scale * originalImage.getWidth(null)).toInt()
            val scaledHeight = (scale * originalImage.getHeight(null)).toInt()
            val scaledImage = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB)
            val graphics = scaledImage.createGraphics()
            graphics.scale(scale.toDouble(), scale.toDouble())
            graphics.drawImage(originalImage, 0, 0, null)
            graphics.dispose()

            // Add padding and apply Gaussian blur
            val paddedImage = addPadding(scaledImage, blurRadius)
            val blurredImage = applyGaussianBlur(paddedImage, blurRadius)

            // Adjust the image's HSB properties
            val finalImage = adjustImageHSB(blurredImage!!, 0.0f, 1.1f, 0.0f)

            // Create and return the texture
            return BufferedImageUtil.getTexture(imagePath, finalImage)
        } catch (e: IOException) {
            throw IllegalStateException("Unable to find $imagePath.", e)
        }
    }

    fun addPadding(inputImage: BufferedImage, padding: Int): BufferedImage? {
        // Calculate the new dimensions with padding
        val paddedWidth = inputImage.width + padding * 2
        val paddedHeight = inputImage.height + padding * 2

        // Create a scaled version of the image
        val paddedImage = scaleImage(
            inputImage,
            (paddedWidth.toFloat() / inputImage.width).toDouble(),
            (paddedHeight.toFloat() / inputImage.height).toDouble()
        )

        // Copy the original image's pixels into the center of the padded image
        for (x in 0..<inputImage.width) {
            for (y in 0..<inputImage.height) {
                paddedImage!!.setRGB(padding + x, padding + y, inputImage.getRGB(x, y))
            }
        }

        return paddedImage
    }

    fun scaleImage(inputImage: BufferedImage?, scaleX: Double, scaleY: Double): BufferedImage? {
        if (inputImage == null) {
            return null // Return null if the input image is null
        }

        // Calculate the new dimensions
        val newWidth = (inputImage.width * scaleX).toInt()
        val newHeight = (inputImage.height * scaleY).toInt()

        // Create a new scaled BufferedImage
        val scaledImage = BufferedImage(newWidth, newHeight, inputImage.type)

        // Use Graphics2D to perform the scaling
        val graphics = scaledImage.createGraphics()
        val scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY)
        graphics.drawRenderedImage(inputImage, scaleTransform)
        graphics.dispose() // Clean up resources

        return scaledImage
    }

    fun adjustImageHSB(
        image: BufferedImage,
        hueAdjust: Float,
        saturationMultiplier: Float,
        brightnessAdjust: Float
    ): BufferedImage {
        val width = image.width
        val height = image.height

        for (y in 0..<height) {
            for (x in 0..<width) {
                // Get the RGB color of the pixel
                val rgb = image.getRGB(x, y)

                // Extract the red, green, and blue components
                val red = (rgb shr 16) and 0xFF
                val green = (rgb shr 8) and 0xFF
                val blue = rgb and 0xFF

                // Convert RGB to HSB (Hue, Saturation, Brightness)
                val hsb = Color.RGBtoHSB(red, green, blue, null)

                // Adjust HSB values
                val adjustedHue = max(0.0, min(1.0, (hsb[0] + hueAdjust).toDouble())).toFloat()
                val adjustedSaturation = max(0.0, min(1.0, (hsb[1] * saturationMultiplier).toDouble())).toFloat()
                val adjustedBrightness = max(0.0, min(1.0, (hsb[2] + brightnessAdjust).toDouble())).toFloat()

                // Convert back to RGB
                val adjustedRGB = Color.HSBtoRGB(adjustedHue, adjustedSaturation, adjustedBrightness)

                // Set the adjusted RGB value back to the image
                image.setRGB(x, y, adjustedRGB)
            }
        }

        return image
    }
}