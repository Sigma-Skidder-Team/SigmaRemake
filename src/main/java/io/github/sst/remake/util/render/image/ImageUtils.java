package io.github.sst.remake.util.render.image;

import org.newdawn.slick.util.BufferedImageUtil;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;

public class ImageUtils {

    public static BufferedImage applyBlur(BufferedImage image, int blurRadius) {
        if (image == null) {
            return null;
        }

        if (image.getWidth() <= blurRadius * 2 || image.getHeight() <= blurRadius * 2) {
            return image;
        }

        ConvolveOp blurOp =
                new ConvolveOp(BufferedImageUtil.createGaussianKernel((float) blurRadius));

        BufferedImage blurred = blurOp.filter(image, null);
        blurred = blurOp.filter(BufferedImageUtil.applyEdgeWrap(blurred), null);
        blurred = BufferedImageUtil.applyEdgeWrap(blurred);

        return blurred.getSubimage(
                blurRadius,
                blurRadius,
                image.getWidth() - blurRadius * 2,
                image.getHeight() - blurRadius * 2
        );
    }

    public static BufferedImage adjustImageHSB(
            BufferedImage image,
            float hueOffset,
            float saturationMultiplier,
            float brightnessOffset
    ) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                float[] hsb = Color.RGBtoHSB(red, green, blue, null);

                float hue = clamp(hsb[0] + hueOffset);
                float saturation = clamp(hsb[1] * saturationMultiplier);
                float brightness = clamp(hsb[2] + brightnessOffset);

                image.setRGB(x, y, Color.HSBtoRGB(hue, saturation, brightness));
            }
        }

        return image;
    }

    public static BufferedImage addPadding(BufferedImage image, int padding) {
        int paddedWidth = image.getWidth() + padding * 2;
        int paddedHeight = image.getHeight() + padding * 2;

        BufferedImage paddedImage = scaleImage(
                image,
                (double) paddedWidth / image.getWidth(),
                (double) paddedHeight / image.getHeight()
        );

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                paddedImage.setRGB(x + padding, y + padding, image.getRGB(x, y));
            }
        }

        return paddedImage;
    }

    public static BufferedImage scaleImage(
            BufferedImage image,
            double scaleX,
            double scaleY
    ) {
        if (image == null) {
            return null;
        }

        int newWidth = (int) (image.getWidth() * scaleX);
        int newHeight = (int) (image.getHeight() * scaleY);

        BufferedImage scaledImage =
                new BufferedImage(newWidth, newHeight, image.getType());

        Graphics2D graphics = scaledImage.createGraphics();
        try {
            AffineTransform transform =
                    AffineTransform.getScaleInstance(scaleX, scaleY);
            graphics.drawRenderedImage(image, transform);
        } finally {
            graphics.dispose();
        }

        return scaledImage;
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

}
