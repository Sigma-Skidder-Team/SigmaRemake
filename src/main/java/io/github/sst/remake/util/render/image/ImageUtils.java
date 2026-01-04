package io.github.sst.remake.util.render.image;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.math.color.ClientColors;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.util.image.BufferedImageUtil;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.nio.ByteBuffer;

public class ImageUtils {

    private static float getScaleFactor() {
        return Client.INSTANCE.screenManager.scaleFactor;
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

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

    public static BufferedImage captureFramebufferRegion(
            int x,
            int y,
            int width,
            int height,
            int downscaleFactor,
            int blurRadius,
            int paddingColor,
            boolean blurAfterPadding
    ) {
        final int BYTES_PER_PIXEL = 4;

        x = (int) (x * getScaleFactor());
        y = (int) (y * getScaleFactor());
        width = (int) (width * getScaleFactor());
        height = (int) (height * getScaleFactor());
        downscaleFactor = (int) (downscaleFactor * getScaleFactor());

        y = MinecraftClient.getInstance().getWindow().getFramebufferHeight() - y - height;

        if (downscaleFactor <= 0) {
            downscaleFactor = 1;
        }

        ByteBuffer pixelBuffer = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL);

        GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);

        BufferedImage image = new BufferedImage(width / downscaleFactor, height / downscaleFactor, BufferedImage.TYPE_INT_ARGB);

        for (int sx = downscaleFactor / 2; sx < width; sx += downscaleFactor) {
            for (int sy = downscaleFactor / 2; sy < height; sy += downscaleFactor) {

                int targetX = sx / downscaleFactor;
                int targetY = sy / downscaleFactor;

                if (targetX >= image.getWidth() || targetY >= image.getHeight()) {
                    continue;
                }

                int bufferIndex = (sx + width * sy) * BYTES_PER_PIXEL;

                int r = pixelBuffer.get(bufferIndex) & 0xFF;
                int g = pixelBuffer.get(bufferIndex + 1) & 0xFF;
                int b = pixelBuffer.get(bufferIndex + 2) & 0xFF;

                // Flip Y for BufferedImage coordinate system
                image.setRGB(
                        targetX,
                        image.getHeight() - targetY - 1,
                        0xFF000000 | (r << 16) | (g << 8) | b
                );
            }
        }

        if (blurRadius <= 1) {
            return image;
        }

        if (blurAfterPadding) {
            return applyBlur(addPadding(image, blurRadius), blurRadius);
        } else {
            return applyBlur(expandCanvas(image, blurRadius, paddingColor), blurRadius);
        }
    }

    public static BufferedImage expandCanvas(BufferedImage source, int padding, int fillColor) {
        int newWidth = source.getWidth() + padding * 2;
        int newHeight = source.getHeight() + padding * 2;

        BufferedImage result = new BufferedImage(newWidth, newHeight, source.getType());

        if (fillColor != ClientColors.DEEP_TEAL.getColor()) {
            for (int x = 0; x < newWidth; x++) {
                for (int y = 0; y < newHeight; y++) {
                    result.setRGB(x, y, fillColor);
                }
            }
        }

        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                result.setRGB(
                        padding + x,
                        padding + y,
                        source.getRGB(x, y)
                );
            }
        }

        return result;
    }

    public static BufferedImage captureRegionImage(
            int x,
            int y,
            int width,
            int height,
            int downscaleFactor,
            int blurRadius,
            boolean padBeforeBlur
    ) {
        return captureAndProcessRegion(
                x,
                y,
                width,
                height,
                downscaleFactor,
                blurRadius,
                ClientColors.DEEP_TEAL.getColor(),
                padBeforeBlur
        );
    }

    private static BufferedImage captureAndProcessRegion(
            int x,
            int y,
            int width,
            int height,
            int downscaleFactor,
            int blurRadius,
            int paddingColor,
            boolean padBeforeBlur
    ) {
        int bytesPerPixel = 4;

        y = (int) (y * getScaleFactor());
        x = (int) (x * getScaleFactor());
        width = (int) (width * getScaleFactor());
        height = (int) (height * getScaleFactor());
        downscaleFactor = (int) (downscaleFactor * getScaleFactor());

        y = MinecraftClient.getInstance().getWindow().getFramebufferHeight() - y - height;

        if (downscaleFactor <= 0) {
            downscaleFactor = 1;
        }

        ByteBuffer pixelBuffer = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
        GL11.glReadPixels(x, y, width, height, 6408, 5121, pixelBuffer);

        BufferedImage image = new BufferedImage(
                width / downscaleFactor,
                height / downscaleFactor,
                BufferedImage.TYPE_INT_ARGB
        );

        for (int px = downscaleFactor / 2; px < width; px += downscaleFactor) {
            for (int py = downscaleFactor / 2; py < height; py += downscaleFactor) {
                int imgX = px / downscaleFactor;
                int imgY = py / downscaleFactor;

                if (imgX < image.getWidth() && imgY < image.getHeight()) {
                    int index = (px + width * py) * bytesPerPixel;
                    int r = pixelBuffer.get(index) & 255;
                    int g = pixelBuffer.get(index + 1) & 255;
                    int b = pixelBuffer.get(index + 2) & 255;

                    image.setRGB(
                            imgX,
                            image.getHeight() - (imgY + 1),
                            0xFF000000 | (r << 16) | (g << 8) | b
                    );
                }
            }
        }

        if (blurRadius <= 1) {
            return image;
        }

        if (padBeforeBlur) {
            return applyBlur(addPadding(image, blurRadius), blurRadius);
        }

        return applyBlur(
                addPaddingWithColor(image, blurRadius, paddingColor),
                blurRadius
        );
    }

    private static BufferedImage addPaddingWithColor(
            BufferedImage source,
            int padding,
            int color
    ) {
        int newWidth = source.getWidth() + padding * 2;
        int newHeight = source.getHeight() + padding * 2;

        BufferedImage padded = new BufferedImage(newWidth, newHeight, source.getType());

        if (color != ClientColors.DEEP_TEAL.getColor()) {
            for (int x = 0; x < newWidth; x++) {
                for (int y = 0; y < newHeight; y++) {
                    padded.setRGB(x, y, color);
                }
            }
        }

        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                padded.setRGB(padding + x, padding + y, source.getRGB(x, y));
            }
        }

        return padded;
    }

    public static BufferedImage captureAndProcessRegion(int x, int y, int width, int height, int downscaleFactor, int blurRadius) {
        return captureAndProcessRegion(x, y, width, height, downscaleFactor, blurRadius, ClientColors.DEEP_TEAL.getColor(), false);
    }

}
