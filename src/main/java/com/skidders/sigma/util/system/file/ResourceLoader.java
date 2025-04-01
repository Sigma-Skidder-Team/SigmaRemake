package com.skidders.sigma.util.system.file;

import com.skidders.sigma.util.client.interfaces.ILogger;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.BufferedImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader implements ILogger {
    public static Texture loadTexture(String filePath) {
        try {
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toUpperCase();
            return loadTexture(filePath, extension);
        } catch (Exception e) {
            logger.error("Unable to load texture {}. Please make sure it is a valid path and has a valid extension.", filePath);
            throw e;
        }
    }

    public static Texture loadTexture(String filePath, String fileType) {
        try {
            return TextureLoader.getTexture(fileType, readInputStream(filePath));
        } catch (IOException e) {
            try (InputStream inputStream = readInputStream(filePath)) {
                byte[] header = new byte[8];
                inputStream.read(header);
                StringBuilder headerInfo = new StringBuilder();
                for (int value : header) {
                    headerInfo.append(" ").append(value);
                }
                throw new IllegalStateException("Unable to load texture " + filePath + " header: " + headerInfo);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to load texture " + filePath, ex);
            }
        }
    }

    public static InputStream readInputStream(String fileName) {
        try {
            // The file path within the sigma-reborn folder.
            String assetPath = "assets/sigma-reborn/" + fileName;

            // Attempt to load the resource directly from the classpath
            InputStream resourceStream = ResourceLoader.class.getClassLoader().getResourceAsStream(assetPath);

            if (resourceStream != null) {
                return resourceStream;
            } else {
                throw new IllegalStateException("Resource not found: " + assetPath);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to load resource " + fileName + ". Error during resource loading.", e
            );
        }
    }

    public static Kernel createGaussianKernel(float radius) {
        int kernelRadius = (int) Math.ceil(radius);
        int kernelSize = kernelRadius * 2 + 1; // Size of the kernel (diameter)
        float[] kernelData = new float[kernelSize];

        // Standard deviation for Gaussian distribution
        float standardDeviation = radius / 3.0F;
        float twoSigmaSquared = 2.0F * standardDeviation * standardDeviation;
        float normalizationFactor = (float) (Math.sqrt(Math.PI * 2) * standardDeviation);
        float maxDistanceSquared = radius * radius;
        float sum = 0.0F;
        int index = 0;

        // Populate the kernel data
        for (int offset = -kernelRadius; offset <= kernelRadius; offset++) {
            float distanceSquared = offset * offset;
            if (distanceSquared <= maxDistanceSquared) {
                kernelData[index] = (float) Math.exp(-distanceSquared / twoSigmaSquared) / normalizationFactor;
            } else {
                kernelData[index] = 0.0F;
            }

            sum += kernelData[index];
            index++;
        }

        // Normalize the kernel values so they sum up to 1
        for (int i = 0; i < kernelSize; i++) {
            kernelData[i] /= sum;
        }

        return new Kernel(kernelSize, 1, kernelData);
    }

    public static BufferedImage applyGaussianBlur(BufferedImage inputImage, int radius) {
        if (inputImage == null) {
            return null; // Return null if the input image is null
        }

        // Ensure the image is large enough for the specified blur radius
        if (inputImage.getWidth() > radius * 2 && inputImage.getHeight() > radius * 2) {
            ConvolveOp blurOperation = new ConvolveOp(createGaussianKernel(radius));

            // Apply the Gaussian blur
            BufferedImage blurredImage = blurOperation.filter(inputImage, null);
            blurredImage = blurOperation.filter(applyEdgeWrap(blurredImage), null);
            blurredImage = applyEdgeWrap(blurredImage);

            // Crop the image to remove the blurred edges
            return blurredImage.getSubimage(
                    radius,
                    radius,
                    inputImage.getWidth() - radius * 2,
                    inputImage.getHeight() - radius * 2
            );
        } else {
            return inputImage; // Return the original image if it's too small for the blur
        }
    }

    public static BufferedImage applyEdgeWrap(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        // Create a new BufferedImage with swapped width and height
        BufferedImage wrappedImage = new BufferedImage(height, width, inputImage.getType());

        // Copy pixels from the input image to the wrapped image with reversed coordinates
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                wrappedImage.setRGB(height - 1 - y, width - 1 - x, inputImage.getRGB(x, y));
            }
        }

        return wrappedImage;
    }

    public static Texture generateTexture(String imagePath, float scale, int blurRadius) {
        try {
            // Read the image from the specified path
            BufferedImage originalImage = ImageIO.read(readInputStream(imagePath));

            // Scale the image
            int scaledWidth = (int) (scale * originalImage.getWidth(null));
            int scaledHeight = (int) (scale * originalImage.getHeight(null));
            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaledImage.createGraphics();
            graphics.scale(scale, scale);
            graphics.drawImage(originalImage, 0, 0, null);
            graphics.dispose();

            // Add padding and apply Gaussian blur
            BufferedImage paddedImage = addPadding(scaledImage, blurRadius);
            BufferedImage blurredImage = applyGaussianBlur(paddedImage, blurRadius);

            // Adjust the image's HSB properties
            BufferedImage finalImage = adjustImageHSB(blurredImage, 0.0F, 1.1F, 0.0F);

            // Create and return the texture
            return BufferedImageUtil.getTexture(imagePath, finalImage);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find " + imagePath + ".", e);
        }
    }

    public static BufferedImage addPadding(BufferedImage inputImage, int padding) {
        // Calculate the new dimensions with padding
        int paddedWidth = inputImage.getWidth() + padding * 2;
        int paddedHeight = inputImage.getHeight() + padding * 2;

        // Create a scaled version of the image
        BufferedImage paddedImage = scaleImage(inputImage,
                (float) paddedWidth / inputImage.getWidth(),
                (float) paddedHeight / inputImage.getHeight());

        // Copy the original image's pixels into the center of the padded image
        for (int x = 0; x < inputImage.getWidth(); x++) {
            for (int y = 0; y < inputImage.getHeight(); y++) {
                paddedImage.setRGB(padding + x, padding + y, inputImage.getRGB(x, y));
            }
        }

        return paddedImage;
    }

    public static BufferedImage scaleImage(BufferedImage inputImage, double scaleX, double scaleY) {
        if (inputImage == null) {
            return null; // Return null if the input image is null
        }

        // Calculate the new dimensions
        int newWidth = (int) (inputImage.getWidth() * scaleX);
        int newHeight = (int) (inputImage.getHeight() * scaleY);

        // Create a new scaled BufferedImage
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, inputImage.getType());

        // Use Graphics2D to perform the scaling
        Graphics2D graphics = scaledImage.createGraphics();
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        graphics.drawRenderedImage(inputImage, scaleTransform);
        graphics.dispose(); // Clean up resources

        return scaledImage;
    }

    public static BufferedImage adjustImageHSB(BufferedImage image, float hueAdjust, float saturationMultiplier, float brightnessAdjust) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the RGB color of the pixel
                int rgb = image.getRGB(x, y);

                // Extract the red, green, and blue components
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Convert RGB to HSB (Hue, Saturation, Brightness)
                float[] hsb = Color.RGBtoHSB(red, green, blue, null);

                // Adjust HSB values
                float adjustedHue = Math.max(0.0F, Math.min(1.0F, hsb[0] + hueAdjust));
                float adjustedSaturation = Math.max(0.0F, Math.min(1.0F, hsb[1] * saturationMultiplier));
                float adjustedBrightness = Math.max(0.0F, Math.min(1.0F, hsb[2] + brightnessAdjust));

                // Convert back to RGB
                int adjustedRGB = Color.HSBtoRGB(adjustedHue, adjustedSaturation, adjustedBrightness);

                // Set the adjusted RGB value back to the image
                image.setRGB(x, y, adjustedRGB);
            }
        }

        return image;
    }
}