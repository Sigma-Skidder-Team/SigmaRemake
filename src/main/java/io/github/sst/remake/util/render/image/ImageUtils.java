package io.github.sst.remake.util.render.image;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.math.color.ClientColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.util.image.BufferedImageUtil;
import org.newdawn.slick.opengl.texture.Texture;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.io.IOException;
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

        int framebufferWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        int framebufferHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        y = framebufferHeight - y - height;

        if (downscaleFactor <= 0) {
            downscaleFactor = 1;
        }

        if (width <= 0 || height <= 0) {
            BufferedImage empty = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            empty.setRGB(0, 0, paddingColor);
            return empty;
        }

        if (x < 0) {
            width += x;
            x = 0;
        }
        if (y < 0) {
            height += y;
            y = 0;
        }

        if (x + width > framebufferWidth) {
            width = framebufferWidth - x;
        }
        if (y + height > framebufferHeight) {
            height = framebufferHeight - y;
        }

        if (width <= 0 || height <= 0) {
            BufferedImage empty = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            empty.setRGB(0, 0, paddingColor);
            return empty;
        }

        ByteBuffer pixelBuffer = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL);

        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
        if (framebuffer != null) {
            framebuffer.beginRead();
        }

        int prevReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        if (GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING) != 0) {
            GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        } else {
            GL11.glReadBuffer(GL11.GL_BACK);
        }

        int prevPackAlignment = GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT);
        int prevPackRowLength = GL11.glGetInteger(GL12.GL_PACK_ROW_LENGTH);
        int prevPackSkipRows = GL11.glGetInteger(GL12.GL_PACK_SKIP_ROWS);
        int prevPackSkipPixels = GL11.glGetInteger(GL12.GL_PACK_SKIP_PIXELS);

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL12.GL_PACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL12.GL_PACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL12.GL_PACK_SKIP_PIXELS, 0);

        GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, prevPackAlignment);
        GL11.glPixelStorei(GL12.GL_PACK_ROW_LENGTH, prevPackRowLength);
        GL11.glPixelStorei(GL12.GL_PACK_SKIP_ROWS, prevPackSkipRows);
        GL11.glPixelStorei(GL12.GL_PACK_SKIP_PIXELS, prevPackSkipPixels);

        GL11.glReadBuffer(prevReadBuffer);
        if (framebuffer != null) {
            framebuffer.beginWrite(true);
        }

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

    public static BufferedImage copyImage(BufferedImage image) {
        if (image == null) {
            return null;
        }
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(image, 0, 0, null);
        copy.getGraphics().dispose();
        return copy;
    }

    public static BufferedImage copySubImageSafe(BufferedImage source, int x, int y, int width, int height) {
        if (source == null || width <= 0 || height <= 0) {
            return null;
        }
        int safeX = Math.max(0, Math.min(x, source.getWidth() - 1));
        int safeY = Math.max(0, Math.min(y, source.getHeight() - 1));
        int safeWidth = Math.max(1, Math.min(width, source.getWidth() - safeX));
        int safeHeight = Math.max(1, Math.min(height, source.getHeight() - safeY));
        return copyImage(source.getSubimage(safeX, safeY, safeWidth, safeHeight));
    }

    public static BufferedImage createSquareThumbnail(BufferedImage source, int size) {
        if (source == null || size <= 0) {
            return source;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        int side = Math.min(width, height);
        if (side <= 0) {
            return source;
        }
        int x = (width - side) / 2;
        int y = (height - side) / 2;
        BufferedImage square = copySubImageSafe(source, x, y, side, side);
        if (square == null) {
            return null;
        }
        if (side == size) {
            return square;
        }
        double scale = (double) size / (double) side;
        return toCompatibleImageType(scaleImage(square, scale, scale));
    }

    public static BufferedImage toCompatibleImageType(BufferedImage image) {
        if (image == null || image.getType() == BufferedImage.TYPE_INT_ARGB) {
            return image;
        }
        BufferedImage compatibleImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        compatibleImage.getGraphics().drawImage(image, 0, 0, null);
        compatibleImage.getGraphics().dispose();
        return compatibleImage;
    }

    public static Texture createTexture(String key, BufferedImage image) throws IOException {
        int prevUnpackAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
        int prevUnpackRowLength = GL11.glGetInteger(GL12.GL_UNPACK_ROW_LENGTH);
        int prevUnpackSkipRows = GL11.glGetInteger(GL12.GL_UNPACK_SKIP_ROWS);
        int prevUnpackSkipPixels = GL11.glGetInteger(GL12.GL_UNPACK_SKIP_PIXELS);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL12.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_PIXELS, 0);

        try {
            return BufferedImageUtil.getTexture(key, image);
        } finally {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, prevUnpackAlignment);
            GL11.glPixelStorei(GL12.GL_UNPACK_ROW_LENGTH, prevUnpackRowLength);
            GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_ROWS, prevUnpackSkipRows);
            GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_PIXELS, prevUnpackSkipPixels);
        }
    }
}
