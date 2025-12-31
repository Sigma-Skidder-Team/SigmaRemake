package io.github.sst.remake.util.http;

import io.github.sst.remake.Client;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.opengl.texture.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
            return TextureLoader.getTexture("PNG", inputStream);
        } catch (IOException e) {
            Client.LOGGER.error("Failed to load head from URL", e);
            return null;
        }
    }

}
