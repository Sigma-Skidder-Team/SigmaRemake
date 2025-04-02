package com.skidders.sigma.util.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.skidders.sigma.util.client.interfaces.ILogger;
import com.skidders.sigma.util.client.render.image.ImageUtil;
import com.skidders.sigma.util.client.render.shader.BlurUtil;
import com.skidders.sigma.util.system.file.FileUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.BufferedImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;

import static org.lwjgl.stb.STBImage.stbi_load;

public class Loader {


    public static Texture loadTexture(String filePath) {
        try {
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toUpperCase();
            return loadTexture(filePath, extension);
        } catch (Exception e) {
            ILogger.logger.error("Unable to load texture {}. Please make sure it is a valid path and has a valid extension.", filePath);
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
            InputStream resourceStream = Loader.class.getClassLoader().getResourceAsStream(assetPath);

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

    public static int loadTextureSafe(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);

        try {
            for (int pixel : pixels) {
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
            buffer.flip();
        } catch (BufferOverflowException | ReadOnlyBufferException ex) {
            return -1;
        }

        int textureID = GlStateManager.genTextures();
        GlStateManager.bindTexture(textureID);
        GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
        GlStateManager.bindTexture(0);

        return textureID;
    }

    public static int loadTexture(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);

        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();

        int textureID = GlStateManager.genTextures();
        GlStateManager.bindTexture(textureID);
        GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GlStateManager.bindTexture(0);
        return textureID;
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
            BufferedImage paddedImage = ImageUtil.addPadding(scaledImage, blurRadius);
            BufferedImage blurredImage = BlurUtil.applyGaussianBlur(paddedImage, blurRadius);

            // Adjust the image's HSB properties
            BufferedImage finalImage = ImageUtil.adjustImageHSB(blurredImage, 0.0F, 1.1F, 0.0F);

            // Create and return the texture
            return BufferedImageUtil.getTexture(imagePath, finalImage);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find " + imagePath + ".", e);
        }
    }

    public record ImageParser(int width, int height, ByteBuffer image) {
        public static ImageParser loadImage(String resource, String path) {
            try {
                FileUtil.copyResourceToFile(resource, path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteBuffer image;
            int width, height;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer comp = stack.mallocInt(1);
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);

                image = stbi_load(path, w, h, comp, 4);
                if (image == null) {
                    throw new RuntimeException("Could not load image " + path);
                }
                width = w.get();
                height = h.get();
            }
            return new ImageParser(width, height, image);
        }
    }
}
