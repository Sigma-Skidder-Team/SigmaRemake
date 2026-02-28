package io.github.sst.remake.gui.screen.musicplayer;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.util.client.yt.SongData;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ThumbnailButton extends Widget {
    private static final ColorHelper DEFAULT_TEXT_STYLE = new ColorHelper(
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            FontAlignment.LEFT,
            FontAlignment.CENTER
    );
    public URL thumbnailUrl;
    public BufferedImage thumbnailImage;
    public boolean thumbnailLoadStarted = false;
    private Texture thumbnailTexture;
    private Texture blurredThumbnailTexture;
    private final AnimationUtils hoverAnimation = new AnimationUtils(125, 125);

    public ThumbnailButton(GuiComponent parent, int x, int y, int width, int height, SongData video) {
        super(parent, video.id, x, y, width, height, DEFAULT_TEXT_STYLE, video.title, false);

        try {
            this.thumbnailUrl = new URL(video.getThumbnailUrl());
        } catch (MalformedURLException e) {
            Client.LOGGER.error("Failed to parse url", e);
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        boolean isHoveredInVisibleArea =
                this.isHoveredInHierarchy()
                        && this.getParent() != null
                        && this.getParent().getParent() != null
                        && this.getParent().getParent().isMouseOverComponent(mouseX, mouseY);

        // hovered -> BACKWARDS, not hovered -> FORWARDS.
        this.hoverAnimation.changeDirection(
                isHoveredInVisibleArea ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS
        );

        super.updatePanelDimensions(mouseX, mouseY);
    }

    private boolean isWithinScrollableViewport() {
        if (this.getParent() == null || this.getParent().getParent() == null) {
            return true;
        }

        GuiComponent container = this.getParent().getParent();
        if (!(container instanceof ScrollablePanel)) {
            return true;
        }

        ScrollablePanel scrollablePanel = (ScrollablePanel) container;

        int maxY = scrollablePanel.getScrollOffset() + scrollablePanel.getHeight() + this.getHeight();
        int minY = scrollablePanel.getScrollOffset() - this.getHeight();

        return this.getY() <= maxY && this.getY() >= minY;
    }

    @Override
    public void draw(float partialTicks) {
        if (!this.isWithinScrollableViewport()) {
            this.releaseTexturesIfPresent();
            return;
        }

        if (!this.thumbnailLoadStarted) {
            this.thumbnailLoadStarted = true;
            this.loadThumbnailAsync();
        }

        float hoverPercent = this.hoverAnimation.calcPercent();

        float imageX = (float) Math.round((this.getX() + 15) - 5.0F * hoverPercent);
        float imageY = (float) Math.round((this.getY() + 15) - 5.0F * hoverPercent);
        float imageW = (float) Math.round((this.getWidth() - 30) + 10.0F * hoverPercent);
        float imageH = (float) Math.round((this.getWidth() - 30) + 10.0F * hoverPercent);

        RenderUtils.drawRoundedRect(
                (float) (this.getX() + 15) - 5.0F * hoverPercent,
                (float) (this.getY() + 15) - 5.0F * hoverPercent,
                (float) (this.getWidth() - 30) + 10.0F * hoverPercent,
                (float) (this.getWidth() - 30) + 10.0F * hoverPercent,
                20.0F,
                partialTicks
        );

        this.drawThumbnail(imageX, imageY, imageW, imageH, hoverPercent, partialTicks);
        this.drawPlayOverlay(hoverPercent, partialTicks);
        this.drawTitleText(partialTicks);
    }

    private void loadThumbnailAsync() {
        new Thread(() -> {
            try {
                BufferedImage loaded = ImageIO.read(this.thumbnailUrl);
                if (loaded == null) {
                    return;
                }

                if (loaded.getHeight() != loaded.getWidth()) {
                    // TODO: verify these crop constants
                    this.thumbnailImage = loaded.getSubimage(70, 0, 180, 180);
                } else {
                    this.thumbnailImage = loaded;
                }
            } catch (IOException | NumberFormatException e) {
                Client.LOGGER.warn("Failed to load/process thumbnail image", e);
            }
        }).start();
    }

    private void releaseTexturesIfPresent() {
        if (this.thumbnailTexture != null) {
            this.thumbnailTexture.release();
            this.thumbnailTexture = null;
        }

        if (this.blurredThumbnailTexture != null) {
            this.blurredThumbnailTexture.release();
            this.blurredThumbnailTexture = null;
        }
    }

    private void drawThumbnail(float x, float y, float w, float h, float hoverPercent, float partialTicks) {
        if (this.thumbnailTexture == null && this.thumbnailImage == null) {
            RenderUtils.drawImage(
                    x, y, w, h,
                    Resources.ARTWORK,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * (1.0F - hoverPercent))
            );

            if (this.blurredThumbnailTexture != null) {
                RenderUtils.drawImage(
                        x, y, w, h,
                        Resources.ARTWORK,
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), hoverPercent * partialTicks)
                );
            }
            return;
        }

        if (this.thumbnailTexture == null) {
            try {
                this.thumbnailTexture = ImageUtils.createTexture("picture-" + this.getName(), this.thumbnailImage);
            } catch (IOException e) {
                Client.LOGGER.warn("Failed to get texture 'picture'", e);
            }
        }

        if (this.blurredThumbnailTexture == null && hoverPercent > 0.0F) {
            try {
                this.blurredThumbnailTexture = ImageUtils.createTexture(
                        "picture-blur-" + this.getName(),
                        ImageUtils.applyBlur(this.thumbnailImage, 14)
                );
            } catch (IOException e) {
                Client.LOGGER.warn("Failed to get texture 'picture'", e);
            }
        } else if (hoverPercent == 0.0F && this.blurredThumbnailTexture != null) {
            this.blurredThumbnailTexture.release();
            this.blurredThumbnailTexture = null;
        }

        RenderUtils.drawImage(
                x, y, w, h,
                this.thumbnailTexture,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * (1.0F - hoverPercent))
        );

        if (this.blurredThumbnailTexture != null) {
            RenderUtils.drawImage(
                    x, y, w, h,
                    this.blurredThumbnailTexture,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), hoverPercent * partialTicks)
            );
        }
    }

    private void drawPlayOverlay(float hoverPercent, float partialTicks) {
        float playIconSize = this.isMouseDownOverComponent() ? 40.0F : 50.0F;
        float scale = 0.5F + hoverPercent / 2.0F;

        RenderUtils.drawImage(
                (float) (this.getX() + this.getWidth() / 2) - (playIconSize / 2) * scale,
                (float) (this.getY() + this.getWidth() / 2) - (playIconSize / 2) * scale,
                playIconSize * scale,
                playIconSize * scale,
                Resources.PLAY_ICON,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), hoverPercent * partialTicks)
        );
    }

    private void drawTitleText(float partialTicks) {
        if (this.text == null) {
            return;
        }

        TrueTypeFont font = FontUtils.HELVETICA_LIGHT_12;

        ScissorUtils.startScissor(this);

        String[] parts = this.getText()
                .replaceAll("\\(.*\\)", "")
                .replaceAll("\\[.*\\]", "")
                .split(" - ");

        if (parts.length > 1) {
            RenderUtils.drawString(
                    font,
                    (float) (this.getX() + (this.getWidth() - font.getWidth(parts[1])) / 2),
                    (float) (this.getY() + this.getWidth() - 2),
                    parts[1],
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
            );
            RenderUtils.drawString(
                    font,
                    (float) (this.getX() + (this.getWidth() - font.getWidth(parts[0])) / 2),
                    (float) (this.getY() + this.getWidth() - 2 + 13),
                    parts[0],
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
            );
        } else {
            RenderUtils.drawString(
                    font,
                    (float) (this.getX() + (this.getWidth() - font.getWidth(parts[0])) / 2),
                    (float) (this.getY() + this.getWidth() - 2 + 6),
                    parts[0],
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
            );
        }

        ScissorUtils.restoreScissor();
    }
}
