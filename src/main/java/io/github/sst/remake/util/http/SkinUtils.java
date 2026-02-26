package io.github.sst.remake.util.http;

import io.github.sst.remake.Client;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.opengl.texture.TextureLoader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SkinUtils {
    private static String getSkinUrlByID(String uuid) {
        return "https://minotar.net/skin/" + uuid;
    }

    private static String getHeadUrlByID(String uuid, int size) {
        return "https://minotar.net/helm/" + uuid + "/" + size + ".png";
    }

    public static BufferedImage getSkin(String uuid) {
        try {
            return ImageIO.read(new URL(getSkinUrlByID(uuid)));
        } catch (IOException e) {
            Client.LOGGER.error("Failed to load skin from URL", e);
            return null;
        }
    }

    public static Texture getHead(String uuid) {
        try (InputStream inputStream = NetUtils.getInputStreamFromURL(getHeadUrlByID(uuid, 75))) {
            return loadTextureSafe("PNG", inputStream);
        } catch (IOException e) {
            Client.LOGGER.error("Failed to load head from URL", e);
            return null;
        }
    }

    public static byte[] getHeadBytes(String uuid, int size) {
        try (InputStream inputStream = NetUtils.getInputStreamFromURL(getHeadUrlByID(uuid, size));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            Client.LOGGER.error("Failed to load head bytes from URL", e);
            return null;
        }
    }

    public static Texture loadHeadTexture(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            return loadTextureSafe("PNG", inputStream);
        } catch (IOException e) {
            Client.LOGGER.error("Failed to decode head texture", e);
            return null;
        }
    }

    private static Texture loadTextureSafe(String format, InputStream inputStream) throws IOException {
        int prevUnpackAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
        int prevUnpackRowLength = GL11.glGetInteger(GL12.GL_UNPACK_ROW_LENGTH);
        int prevUnpackSkipRows = GL11.glGetInteger(GL12.GL_UNPACK_SKIP_ROWS);
        int prevUnpackSkipPixels = GL11.glGetInteger(GL12.GL_UNPACK_SKIP_PIXELS);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL12.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_PIXELS, 0);

        try {
            return TextureLoader.getTexture(format, inputStream);
        } finally {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, prevUnpackAlignment);
            GL11.glPixelStorei(GL12.GL_UNPACK_ROW_LENGTH, prevUnpackRowLength);
            GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_ROWS, prevUnpackSkipRows);
            GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_PIXELS, prevUnpackSkipPixels);
        }
    }
}
