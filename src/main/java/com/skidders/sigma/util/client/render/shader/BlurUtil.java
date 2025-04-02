package com.skidders.sigma.util.client.render.shader;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class BlurUtil {
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
}
