package io.github.sst.remake.util.render.image;

import io.github.sst.remake.Client;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.opengl.texture.TextureLoader;
import org.newdawn.slick.util.image.BufferedImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Resources {
    //TODO: REMAP
    public static Texture multiplayerPNG = loadTexture("jello/icons/multiplayer.png");
    public static Texture optionsPNG = loadTexture("jello/icons/options.png");
    public static Texture singleplayerPNG = loadTexture("jello/icons/singleplayer.png");
    public static Texture shopPNG = loadTexture("jello/icons/shop.png");
    public static Texture altPNG = loadTexture("jello/icons/alt.png");
    public static Texture logoLargePNG = loadTexture("jello/logo_large.png");
    public static Texture logoLarge2xPNG = loadTexture("jello/logo_large2x.png");
    public static Texture verticalScrollBarTopPNG = loadTexture("component/verticalscrollbartop.png");
    public static Texture verticalScrollBarBottomPNG = loadTexture("component/verticalscrollbarbottom.png");
    public static Texture checkPNG = loadTexture("component/check.png");
    public static Texture trashcanPNG = loadTexture("component/trashcan.png");
    public static Texture jelloWatermarkPNG = loadTexture("watermark/jello_watermark.png");
    public static Texture jelloWatermark2xPNG = loadTexture("watermark/jello_watermark2x.png");
    public static Texture shadowLeftPNG = loadTexture("jello/shadow_left.png");
    public static Texture shadowRightPNG = loadTexture("jello/shadow_right.png");
    public static Texture shadowTopPNG = loadTexture("jello/shadow_top.png");
    public static Texture shadowBottomPNG = loadTexture("jello/shadow_bottom.png");
    public static Texture shadowCorner1PNG = loadTexture("jello/shadow_corner.png");
    public static Texture shadowCorner2PNG = loadTexture("jello/shadow_corner_2.png");
    public static Texture shadowCorner3PNG = loadTexture("jello/shadow_corner_3.png");
    public static Texture shadowCorner4PNG = loadTexture("jello/shadow_corner_4.png");
    public static Texture playPNG = loadTexture("music/play.png");
    public static Texture pausePNG = loadTexture("music/pause.png");
    public static Texture forwardsPNG = loadTexture("music/forwards.png");
    public static Texture backwardsPNG = loadTexture("music/backwards.png");
    public static Texture bgPNG = loadTexture("music/bg.png");
    public static Texture artworkPNG = loadTexture("music/artwork.png");
    public static Texture particlePNG = loadTexture("music/particle.png");
    public static Texture repeatPNG = loadTexture("music/repeat.png");
    public static Texture playIconPNG = loadTexture("notifications/play-icon.png");
    public static Texture infoIconPNG = loadTexture("notifications/info-icon.png");
    public static Texture shoutIconPNG = loadTexture("notifications/shout-icon.png");
    public static Texture alertIconPNG = loadTexture("notifications/alert-icon.png");
    public static Texture directionIconPNG = loadTexture("notifications/direction-icon.png");
    public static Texture cancelIconPNG = loadTexture("notifications/cancel-icon.png");
    public static Texture doneIconPNG = loadTexture("notifications/done-icon.png");
    public static Texture gingerbreadIconPNG = loadTexture("notifications/gingerbread-icon.png");
    public static Texture floatingBorderPNG = loadTexture("jello/floating_border.png");
    public static Texture floatingCornerPNG = loadTexture("jello/floating_corner.png");
    public static Texture cerclePNG = loadTexture("alt/cercle.png");
    public static Texture selectPNG = loadTexture("alt/select.png");
    public static Texture activePNG = loadTexture("alt/active.png");
    public static Texture errorsPNG = loadTexture("alt/errors.png");
    public static Texture shadowPNG = loadTexture("alt/shadow.png");
    public static Texture imgPNG = loadTexture("alt/img.png");
    public static Texture head = loadTexture("alt/skin.png");
    public static Texture loadingIndicatorPNG = loadTexture("jello/loading_indicator.png");
    public static Texture mentalfrostbytePNG = loadTexture("mentalfrostbyte/mentalfrostbyte.png");
    public static Texture sigmaPNG = loadTexture("mentalfrostbyte/sigma.png");
    public static Texture tomyPNG = loadTexture("mentalfrostbyte/tomy.png");
    public static Texture androPNG = loadTexture("user/andro.png");
    public static Texture lpPNG = loadTexture("user/lp.png");
    public static Texture cxPNG = loadTexture("user/cx.png");
    public static Texture codyPNG = loadTexture("user/cody.png");
    public static Texture accountPNG = loadTexture("jello/account.png");
    public static Texture waypointPNG = loadTexture("component/waypoint.png");
    public static Texture noaddonsPNG = loadTexture("loading/noaddons.png");
    public static Texture jelloPNG = loadTexture("loading/jello.png");
    public static Texture sigmaLigmaPNG = loadTexture("loading/sigma.png");
    public static Texture searchPNG = loadTexture("jello/search.png");
    public static Texture optionsPNG1 = loadTexture("jello/options.png");
    public static Texture dvdPNG = loadTexture("jello/dvd.png");
    public static Texture foregroundPNG = loadTexture("background/foreground.png");
    public static Texture backgroundPNG = loadTexture("background/background.png");
    public static Texture middlePNG = loadTexture("background/middle.png");
    public static Texture youtubePNG = loadTexture("loading/youtube.png");
    public static Texture guildedPNG = loadTexture("loading/guilded.png");
    public static Texture redditPNG = loadTexture("loading/reddit.png");
    public static Texture panoramaPNG = createBlurredDarkenedTexture("background/panorama5.png", 0.25F, 30);

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

            return BufferedImageUtil.getTexture(resourcePath, processedImage);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load texture resource: " + resourcePath +
                            ". Ensure assets are present and package names were not stripped.",
                    e
            );
        }
    }
}
