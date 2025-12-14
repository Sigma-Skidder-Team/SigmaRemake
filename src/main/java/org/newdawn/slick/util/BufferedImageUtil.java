package org.newdawn.slick.util;

import org.newdawn.slick.opengl.ImageIOImageData;
import org.newdawn.slick.opengl.InternalTextureLoader;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.opengl.renderer.SGL;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This is a utility class that allows you to convert a BufferedImage into a
 * texture.
 *
 * @author James Chambers (Jimmy)
 * @author Jeremy Adams (elias_naur)
 * @author Kevin Glass (kevglass)
 */

public class BufferedImageUtil {

    /**
     * Load a texture
     *
     * @param resourceName  The location of the resource to load
     * @param resourceImage The BufferedImage we are converting
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public static Texture getTexture(String resourceName,
                                     BufferedImage resourceImage) throws IOException {
        Texture tex = getTexture(resourceName, resourceImage,
                SGL.GL_TEXTURE_2D, // target
                SGL.GL_RGBA8, // dest pixel format
                SGL.GL_LINEAR, // min filter (unused)
                SGL.GL_LINEAR);

        return tex;
    }

    /**
     * Load a texture
     *
     * @param resourceName  The location of the resource to load
     * @param resourceImage The BufferedImage we are converting
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public static Texture getTexture(String resourceName,
                                     BufferedImage resourceImage, int filter) throws IOException {
        Texture tex = getTexture(resourceName, resourceImage,
                SGL.GL_TEXTURE_2D, // target
                SGL.GL_RGBA8, // dest pixel format
                filter, // min filter (unused)
                filter);

        return tex;
    }

    /**
     * Load a texture into OpenGL from a BufferedImage
     *
     * @param resourceName   The location of the resource to load
     * @param resourceimage  The BufferedImage we are converting
     * @param target         The GL target to load the texture against
     * @param dstPixelFormat The pixel format of the screen
     * @param minFilter      The minimising filter
     * @param magFilter      The magnification filter
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public static Texture getTexture(String resourceName,
                                     BufferedImage resourceimage, int target, int dstPixelFormat,
                                     int minFilter, int magFilter) throws IOException {
        ImageIOImageData data = new ImageIOImageData();
        int srcPixelFormat = 0;

        // create the texture ID for this texture
        int textureID = InternalTextureLoader.createTextureID();
        TextureImpl texture = new TextureImpl(resourceName, target, textureID);

        // Enable texturing
        Renderer.get().glEnable(SGL.GL_TEXTURE_2D);

        // bind this texture
        Renderer.get().glBindTexture(target, textureID);

        BufferedImage bufferedImage = resourceimage;
        texture.setWidth(bufferedImage.getWidth());
        texture.setHeight(bufferedImage.getHeight());

        if (bufferedImage.getColorModel().hasAlpha()) {
            srcPixelFormat = SGL.GL_RGBA;
        } else {
            srcPixelFormat = SGL.GL_RGB;
        }

        // convert that image into a byte buffer of texture data
        ByteBuffer textureBuffer = data.imageToByteBuffer(bufferedImage, false, false, null);
        texture.setTextureHeight(data.getTexHeight());
        texture.setTextureWidth(data.getTexWidth());
        texture.setAlpha(data.getDepth() == 32);

        if (target == SGL.GL_TEXTURE_2D) {
            Renderer.get().glTexParameteri(target, SGL.GL_TEXTURE_MIN_FILTER, minFilter);
            Renderer.get().glTexParameteri(target, SGL.GL_TEXTURE_MAG_FILTER, magFilter);

            if (Renderer.get().canTextureMirrorClamp()) {
                Renderer.get().glTexParameteri(SGL.GL_TEXTURE_2D, SGL.GL_TEXTURE_WRAP_S, SGL.GL_MIRROR_CLAMP_TO_EDGE_EXT);
                Renderer.get().glTexParameteri(SGL.GL_TEXTURE_2D, SGL.GL_TEXTURE_WRAP_T, SGL.GL_MIRROR_CLAMP_TO_EDGE_EXT);
            } else {
                Renderer.get().glTexParameteri(SGL.GL_TEXTURE_2D, SGL.GL_TEXTURE_WRAP_S, SGL.GL_CLAMP);
                Renderer.get().glTexParameteri(SGL.GL_TEXTURE_2D, SGL.GL_TEXTURE_WRAP_T, SGL.GL_CLAMP);
            }
        }

        Renderer.get().glTexImage2D(target,
                0,
                dstPixelFormat,
                texture.getTextureWidth(),
                texture.getTextureHeight(),
                0,
                srcPixelFormat,
                SGL.GL_UNSIGNED_BYTE,
                textureBuffer);

        return texture;
    }

    /**
     * Implement of transform copy area for 1.4
     *
     * @param image  The image to copy
     * @param x      The x position to copy to
     * @param y      The y position to copy to
     * @param width  The width of the image
     * @param height The height of the image
     * @param dx     The transform on the x axis
     * @param dy     The transform on the y axis
     */
    private static void copyArea(BufferedImage image, int x, int y, int width, int height, int dx, int dy) {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.drawImage(image.getSubimage(x, y, width, height), x + dx, y + dy, null);
    }

    public static BufferedImage applyGaussianBlur(BufferedImage image, int blurRadius) {
        if (image == null) {
            return image;
        } else {
            ConvolveOp blurOperation = new ConvolveOp(createGaussianKernel((float) blurRadius), ConvolveOp.EDGE_NO_OP, null);

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            BufferedImage extendedImage = new BufferedImage(imageWidth + blurRadius * 2, imageHeight + blurRadius * 2, image.getType());

            for (int x = 0; x < imageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    extendedImage.setRGB(x + blurRadius, y + blurRadius / 2, image.getRGB(x, y));
                }
            }

            BufferedImage blurredImage = blurOperation.filter(extendedImage, null);
            blurredImage = blurOperation.filter(applyEdgeWrap(blurredImage), null);
            blurredImage = applyEdgeWrap(blurredImage);

            return blurredImage.getSubimage(blurRadius, blurRadius, extendedImage.getWidth() - blurRadius * 2, extendedImage.getHeight() - blurRadius * 2);
        }
    }

    public static Kernel createGaussianKernel(float radius) {
        int kernelRadius = (int) Math.ceil(radius);
        int kernelSize = kernelRadius * 2 + 1;

        float[] kernelData = new float[kernelSize];
        float standardDeviation = radius / 3.0F;
        float twoSigmaSquared = 2.0F * standardDeviation * standardDeviation;

        float normalizationFactor = (float) ((Math.PI * 2) * (double) standardDeviation);
        normalizationFactor = (float) Math.sqrt(normalizationFactor);

        float maxDistanceSquared = radius * radius;
        float sum = 0.0F;

        int index = 0;

        for (int offset = -kernelRadius; offset <= kernelRadius; offset++) {
            float distanceSquared = (float) (offset * offset);

            if (!(distanceSquared > maxDistanceSquared)) {
                kernelData[index] = (float) Math.exp(-distanceSquared / twoSigmaSquared) / normalizationFactor;
            } else {
                kernelData[index] = 0.0F;
            }

            sum += kernelData[index];
            index++;
        }

        for (int i = 0; i < kernelSize; i++) {
            kernelData[i] /= sum;
        }

        return new Kernel(kernelSize, 1, kernelData);
    }

    public static BufferedImage applyEdgeWrap(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage wrappedImage = new BufferedImage(height, width, inputImage.getType());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                wrappedImage.setRGB(height - 1 - y, width - 1 - x, inputImage.getRGB(x, y));
            }
        }

        return wrappedImage;
    }
}