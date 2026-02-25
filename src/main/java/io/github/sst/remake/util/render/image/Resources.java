package io.github.sst.remake.util.render.image;

import io.github.sst.remake.Client;
import net.minecraft.util.Identifier;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.opengl.texture.TextureLoader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Resources {
    ///         WATERMARK         ///
    public static final Identifier WATERMARK = new Identifier("sigma", "jello/jello_watermark.png");
    public static final Identifier WATERMARK_2X = new Identifier("sigma", "jello/jello_watermark2x.png");

    ///         MENU         ///
    public static final Texture MENU_FOREGROUND = loadTexture("jello/menu/foreground.png");
    public static final Texture MENU_BACKGROUND = loadTexture("jello/menu/background.png");
    public static final Texture MENU_MIDDLEGROUND = loadTexture("jello/menu/middle.png");
    public static final Texture MENU_PANORAMA = createBlurredDarkenedTexture("jello/menu/panorama5.png", 0.25F, 30);
    public static final Texture MENU_PANORAMA_2 = createPaddedBlurredTexture("jello/menu/panorama5.png", 0.075F, 8);

    ///         LOAD SCREEN         ///
    public static final Texture LOADING_SCREEN_BACKGROUND = createPaddedBlurredTexture("jello/menu/loading/back.png", 0.25F, 25);
    //public static final Texture LOADING_SCREEN_BACKGROUND = loadTexture("jello/menu/loading/back.png");
    //public static final Texture NO_ADDONS = loadTexture("jello/menu/loading/noaddons.png");
    //public static final Texture JELLO = loadTexture("jello/menu/loading/jello.png");

    ///         ALT MANAGER         ///
    public static final Texture SELECTED_ICON = loadTexture("jello/menu/alt/select.png");
    public static final Texture CHECKMARK_ICON = loadTexture("jello/menu/alt/active.png");
    public static final Texture X_ICON = loadTexture("jello/menu/alt/errors.png");
    public static final Texture SHADOW = loadTexture("jello/menu/alt/shadow.png");
    public static final Texture OUTLINE = loadTexture("jello/menu/alt/outline.png");
    public static final Texture INFORMATION = loadTexture("jello/menu/alt/img.png");
    public static final Texture STEVE_HEAD = loadTexture("jello/menu/alt/skin.png");

    ///         WIDGETS         ///
    public static final Texture SEARCH_ICON = loadTexture("jello/widget/search.png");
    public static final Texture MORE_ICON = loadTexture("jello/widget/options.png");
    public static final Texture FLOATING_BORDER = loadTexture("jello/widget/floating_border.png");
    public static final Texture FLOATING_CORNER = loadTexture("jello/widget/floating_corner.png");
    public static final Texture VERTICAL_SCROLL_BAR_TOP = loadTexture("jello/widget/verticalscrollbartop.png");
    public static final Texture VERTICAL_SCROLL_BAR_BOTTOM = loadTexture("jello/widget/verticalscrollbarbottom.png");
    public static final Texture CHECKMARK = loadTexture("jello/widget/check.png");
    public static final Texture TRASHCAN = loadTexture("jello/widget/trashcan.png");
    public static final Texture WAYPOINT = loadTexture("jello/widget/waypoint.png");

    ///         NOTIFICATIONS         ///
    public static final Texture PLAY_ICON = loadTexture("jello/notifications/play-icon.png");
    public static final Texture INFO_ICON = loadTexture("jello/notifications/info-icon.png");
    public static final Texture SHOUT_ICON = loadTexture("jello/notifications/shout-icon.png");
    public static final Texture ALERT_ICON = loadTexture("jello/notifications/alert-icon.png");
    public static final Texture DIRECTION_ICON = loadTexture("jello/notifications/direction-icon.png");
    public static final Texture CANCEL_ICON = loadTexture("jello/notifications/cancel-icon.png");
    public static final Texture DONE_ICON = loadTexture("jello/notifications/done-icon.png");

    ///         ICONS         ///
    public static final Texture MULTIPLAYER_ICON = loadTexture("jello/icons/multiplayer.png");
    public static final Texture OPTIONS_ICON = loadTexture("jello/icons/options.png");
    public static final Texture SINGLEPLAYER_ICON = loadTexture("jello/icons/singleplayer.png");
    public static final Texture SHOP_ICON = loadTexture("jello/icons/shop.png");
    public static final Texture ALT_ICON = loadTexture("jello/icons/alt.png");
    public static final Texture DVD = loadTexture("jello/icons/dvd.png");
    public static final Texture LOADING_INDICATOR = loadTexture("jello/icons/loading_indicator.png");

    ///         FLAPPY BIRD         ///
    public static final Texture GAME_BACKGROUND = loadTexture("jello/games/bg.png");
    public static final Texture GAME_FOREGROUND = loadTexture("jello/games/fg.png");
    public static final Texture GAME_BIRD = loadTexture("jello/games/bird.png");
    public static final Texture GAME_PIPE_DOWN = loadTexture("jello/games/pipe.png");
    public static final Texture GAME_PIPE_UP = loadTexture("jello/games/pipe2.png");

    ///         LOGO         ///
    public static final Texture LOGO = loadTexture("jello/logo.png");
    public static final Texture LOGO_LARGE = loadTexture("jello/logo_large.png");
    public static final Texture LOGO_LARGE_2X = loadTexture("jello/logo_large2x.png");

    ///         SHADOWS         ///
    public static final Texture SHADOW_LEFT = loadTexture("jello/shadow/shadow_left.png");
    public static final Texture SHADOW_RIGHT = loadTexture("jello/shadow/shadow_right.png");
    public static final Texture SHADOW_TOP = loadTexture("jello/shadow/shadow_top.png");
    public static final Texture SHADOW_BOTTOM = loadTexture("jello/shadow/shadow_bottom.png");
    public static final Texture SHADOW_CORNER_1 = loadTexture("jello/shadow/shadow_corner.png");
    public static final Texture SHADOW_CORNER_2 = loadTexture("jello/shadow/shadow_corner_2.png");
    public static final Texture SHADOW_CORNER_3 = loadTexture("jello/shadow/shadow_corner_3.png");
    public static final Texture SHADOW_CORNER_4 = loadTexture("jello/shadow/shadow_corner_4.png");

    ///         MUSIC PLAYER         ///
    public static final Texture PLAY = loadTexture("jello/music/play.png");
    public static final Texture PAUSE = loadTexture("jello/music/pause.png");
    public static final Texture FORWARDS = loadTexture("jello/music/forwards.png");
    public static final Texture BACKWARDS = loadTexture("jello/music/backwards.png");
    public static final Texture BACKGROUND = loadTexture("jello/music/bg.png");
    public static final Texture ARTWORK = loadTexture("jello/music/artwork.png");
    public static final Texture PARTICLES = loadTexture("jello/music/particle.png");
    public static final Texture REPEAT = loadTexture("jello/music/repeat.png");

    public static Texture loadTexture(String filePath) {
        try {
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toUpperCase();
            return loadTexture(filePath, extension);
        } catch (Exception e) {
            Client.LOGGER.warn("Unable to load texture {}. Please make sure it is a valid path and has a valid extension.", filePath);
            throw new IllegalStateException("Texture failed to load: " + filePath, e);
        }
    }

    public static Texture loadTexture(String filePath, String fileType) {
        try (InputStream inputStream = readInputStream(filePath)) {
            return loadTextureSafe(fileType, inputStream);
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

    private static Texture loadTextureSafe(String fileType, InputStream inputStream) throws IOException {
        int prevUnpackAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
        int prevUnpackRowLength = GL11.glGetInteger(GL12.GL_UNPACK_ROW_LENGTH);
        int prevUnpackSkipRows = GL11.glGetInteger(GL12.GL_UNPACK_SKIP_ROWS);
        int prevUnpackSkipPixels = GL11.glGetInteger(GL12.GL_UNPACK_SKIP_PIXELS);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL12.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_PIXELS, 0);

        try {
            return TextureLoader.getTexture(fileType, inputStream);
        } finally {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, prevUnpackAlignment);
            GL11.glPixelStorei(GL12.GL_UNPACK_ROW_LENGTH, prevUnpackRowLength);
            GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_ROWS, prevUnpackSkipRows);
            GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_PIXELS, prevUnpackSkipPixels);
        }
    }

    public static InputStream readInputStream(String fileName) {
        try {
            String assetPath = "assets/sigma/" + fileName;

            InputStream resourceStream = Client.class.getClassLoader().getResourceAsStream(assetPath);

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

    public static Texture createBlurredDarkenedTexture(
            String resourcePath,
            float scaleFactor,
            int blurRadius
    ) {
        return createProcessedTexture(
                resourcePath,
                scaleFactor,
                blurRadius,
                false,
                1.3f,
                -0.35f
        );
    }

    public static Texture createPaddedBlurredTexture(
            String resourcePath,
            float scaleFactor,
            int blurRadius
    ) {
        return createProcessedTexture(
                resourcePath,
                scaleFactor,
                blurRadius,
                true,
                1.1f,
                0.0f
        );
    }

    private static Texture createProcessedTexture(
            String resourcePath,
            float scaleFactor,
            int blurRadius,
            boolean addPadding,
            float saturationMultiplier,
            float brightnessOffset
    ) {
        try {
            BufferedImage originalImage =
                    ImageIO.read(readInputStream(resourcePath));

            BufferedImage scaledImage = new BufferedImage(
                    (int) (originalImage.getWidth() * scaleFactor),
                    (int) (originalImage.getHeight() * scaleFactor),
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D graphics = scaledImage.createGraphics();
            try {
                graphics.scale(scaleFactor, scaleFactor);
                graphics.drawImage(originalImage, 0, 0, null);
            } finally {
                graphics.dispose();
            }

            BufferedImage processedImage = addPadding
                    ? ImageUtils.applyBlur(
                    ImageUtils.addPadding(scaledImage, blurRadius),
                    blurRadius
            )
                    : ImageUtils.applyBlur(scaledImage, blurRadius);

            processedImage = ImageUtils.adjustImageHSB(
                    processedImage,
                    0.0f,
                    saturationMultiplier,
                    brightnessOffset
            );

            return ImageUtils.createTexture(resourcePath, processedImage);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load texture resource: " + resourcePath +
                            ". Ensure assets are present and package names were not stripped.",
                    e
            );
        }
    }
}
