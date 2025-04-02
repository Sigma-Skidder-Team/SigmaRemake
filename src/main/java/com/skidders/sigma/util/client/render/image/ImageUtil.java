package com.skidders.sigma.util.client.render.image;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import com.skidders.sigma.util.client.render.ColorUtil;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImageUtil {
    public static void drawImage(float x, float y, float width, float height, Texture texture) {
        drawImage(x, y, width, height, texture, -1);
    }

    public static void drawImage(String image, float x, float y, float width, float height) {
        IMinecraft.mc.getTextureManager().bindTexture(new Identifier("sigma-reborn", image));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        DrawableHelper.drawTexture(new MatrixStack(), (int) x, (int) y, 0, 0, (int) width, (int) height, (int) width, (int) height);
        RenderSystem.disableBlend();
    }

    public static void drawImage(String image, float x, float y, float width, float height, int color) {
        IMinecraft.mc.getTextureManager().bindTexture(new Identifier("sigma-reborn", image));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(color, color, color, color);
        DrawableHelper.drawTexture(new MatrixStack(), (int) x, (int) y, 0, 0, (int) width, (int) height, (int) width, (int) height);
        RenderSystem.disableBlend();
    }

    public static void drawImage(float x, float y, float var2, float var3, Texture tex, float alphaValue) {
        drawImage(x, y, var2, var3, tex, ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor(), alphaValue));
    }

    public static void drawImage(float var0, float var1, float var2, float var3, Texture var4, int var5) {
        drawImage(var0, var1, var2, var3, var4, var5, 0.0F, 0.0F, (float) var4.getImageWidth(), (float) var4.getImageHeight(), true);
    }

    public static void drawImage(float var0, float var1, float var2, float var3, Texture var4, int var5, float var6, float var7, float var8, float var9, boolean var10) {
        if (var4 != null) {
            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
            var0 = (float)Math.round(var0);
            var2 = (float)Math.round(var2);
            var1 = (float)Math.round(var1);
            var3 = (float)Math.round(var3);
            float var13 = (float)(var5 >> 24 & 0xFF) / 255.0F;
            float var14 = (float)(var5 >> 16 & 0xFF) / 255.0F;
            float var15 = (float)(var5 >> 8 & 0xFF) / 255.0F;
            float var16 = (float)(var5 & 0xFF) / 255.0F;
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.color4f(var14, var15, var16, var13);
            GL11.glEnable(3042);
            GL11.glEnable(3553);
            var4.bind();
            float var17 = var2 / (float)var4.getTextureWidth() / (var2 / (float)var4.getImageWidth());
            float var18 = var3 / (float)var4.getTextureHeight() / (var3 / (float)var4.getImageHeight());
            float var19 = var8 / (float)var4.getImageWidth() * var17;
            float var20 = var9 / (float)var4.getImageHeight() * var18;
            float var21 = var6 / (float)var4.getImageWidth() * var17;
            float var22 = var7 / (float)var4.getImageHeight() * var18;
            if (!var10) {
                GL11.glTexParameteri(3553, 10240, 9729);
            } else {
                GL11.glTexParameteri(3553, 10240, 9728);
            }

            GL11.glBegin(7);
            GL11.glTexCoord2f(var21, var22);
            GL11.glVertex2f(var0, var1);
            GL11.glTexCoord2f(var21, var22 + var20);
            GL11.glVertex2f(var0, var1 + var3);
            GL11.glTexCoord2f(var21 + var19, var22 + var20);
            GL11.glVertex2f(var0 + var2, var1 + var3);
            GL11.glTexCoord2f(var21 + var19, var22);
            GL11.glVertex2f(var0 + var2, var1);
            GL11.glEnd();
            GL11.glDisable(3553);
            GL11.glDisable(3042);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
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
}