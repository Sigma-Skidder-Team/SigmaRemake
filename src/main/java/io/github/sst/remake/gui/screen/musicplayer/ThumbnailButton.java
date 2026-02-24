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
import org.newdawn.slick.util.image.BufferedImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ThumbnailButton extends Widget {
    private static final ColorHelper field20771 = new ColorHelper(
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            FontAlignment.LEFT,
            FontAlignment.CENTER
    );
    public URL videoUrl;
    public BufferedImage field20773;
    public boolean field20774 = false;
    private Texture field20775;
    private Texture field20776;
    private final AnimationUtils animation = new AnimationUtils(125, 125);

    public ThumbnailButton(GuiComponent var1, int x, int y, int width, int height, SongData video) {
        super(var1, video.id, x, y, width, height, field20771, video.title, false);

        try {
            this.videoUrl = new URL(video.getThumbnailUrl());
        } catch (MalformedURLException e) {
            Client.LOGGER.error("Failed to parse url", e);
        }

    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        boolean hovered = this.isHoveredInHierarchy() && this.getParent().getParent().isMouseOverComponent(mouseX, mouseY);
        this.animation.changeDirection(!hovered ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);

        super.updatePanelDimensions(mouseX, mouseY);
    }

    public boolean method13157() {
        if (this.getParent() != null && this.getParent().getParent() != null) {
            GuiComponent var3 = this.getParent().getParent();
            if (var3 instanceof ScrollablePanel) {
                ScrollablePanel var4 = (ScrollablePanel) var3;
                int var5 = var4.getScrollOffset() + var4.getHeight() + this.getHeight();
                int var6 = var4.getScrollOffset() - this.getHeight();
                return this.getY() <= var5 && this.getY() >= var6;
            }
        }

        return true;
    }

    @Override
    public void draw(float partialTicks) {
        if (!this.method13157()) {
            if (this.field20775 != null) {
                this.field20775.release();
                this.field20775 = null;
            }

            if (this.field20776 != null) {
                this.field20776.release();
                this.field20776 = null;
            }
        } else {
            if (this.method13157() && !this.field20774) {
                this.field20774 = true;
                new Thread(() -> {
                    try {
                        BufferedImage var3 = ImageIO.read(this.videoUrl);
                        if (var3.getHeight() != var3.getWidth()) {
                            this.field20773 = var3.getSubimage(70, 0, 180, 180);
                        } else {
                            this.field20773 = var3;
                        }
                    } catch (IOException | NumberFormatException e) {
                        Client.LOGGER.warn("Failed to do something with the image", e);
                    }
                }).start();
            }

            float var4 = this.animation.calcPercent();
            float var5 = (float) Math.round((float) (this.getX() + 15) - 5.0F * var4);
            float var6 = (float) Math.round((float) (this.getY() + 15) - 5.0F * var4);
            float var7 = (float) Math.round((float) (this.getWidth() - 30) + 10.0F * var4);
            float var8 = (float) Math.round((float) (this.getWidth() - 30) + 10.0F * var4);
            RenderUtils.drawRoundedRect(
                    (float) (this.getX() + 15) - 5.0F * var4,
                    (float) (this.getY() + 15) - 5.0F * var4,
                    (float) (this.getWidth() - 30) + 10.0F * var4,
                    (float) (this.getWidth() - 30) + 10.0F * var4,
                    20.0F,
                    partialTicks
            );
            if (this.field20775 == null && this.field20773 == null) {
                RenderUtils.drawImage(var5, var6, var7, var8, Resources.ARTWORK, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * (1.0F - var4)));
                if (this.field20776 != null) {
                    RenderUtils.drawImage(var5, var6, var7, var8, Resources.ARTWORK, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * partialTicks));
                }
            } else {
                if (this.field20775 == null) {
                    try {
                        if (this.field20775 != null) {
                            this.field20775.release();
                        }

                        this.field20775 = BufferedImageUtil.getTexture("picture", this.field20773);
                    } catch (IOException e) {
                        Client.LOGGER.warn("Failed to get texture 'picture'", e);
                    }
                }

                if (this.field20776 == null && var4 > 0.0F) {
                    try {
                        if (this.field20776 != null) {
                            this.field20776.release();
                        }

                        this.field20776 = BufferedImageUtil.getTexture("picture", ImageUtils.applyBlur(this.field20773, 14));
                    } catch (IOException e) {
                        Client.LOGGER.warn("Failed to get texture 'picture'", e);
                    }
                } else if (var4 == 0.0F && this.field20776 != null) {
                    this.field20776 = null;
                }

                RenderUtils.drawImage(var5, var6, var7, var8, this.field20775, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * (1.0F - var4)));
                if (this.field20776 != null) {
                    RenderUtils.drawImage(var5, var6, var7, var8, this.field20776, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * partialTicks));
                }
            }

            float var9 = 50;
            if (this.isMouseDownOverComponent()) {
                var9 = 40;
            }

            float var10 = 0.5F + var4 / 2.0F;
            RenderUtils.drawImage(
                    (float) (this.getX() + this.getWidth() / 2) - (var9 / 2) * var10,
                    (float) (this.getY() + this.getWidth() / 2) - (var9 / 2) * var10,
                    var9 * var10,
                    var9 * var10,
                    Resources.PLAY,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * partialTicks)
            );
            TrueTypeFont var11 = FontUtils.HELVETICA_LIGHT_12;
            if (this.text != null) {
                ScissorUtils.startScissor(this);
                String[] var12 = this.getText().replaceAll("\\(.*\\)", "").replaceAll("\\[.*\\]", "").split(" - ");
                if (var12.length > 1) {
                    RenderUtils.drawString(
                            var11,
                            (float) (this.getX() + (this.getWidth() - var11.getWidth(var12[1])) / 2),
                            (float) (this.getY() + this.getWidth() - 2),
                            var12[1],
                            ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
                    );
                    RenderUtils.drawString(
                            var11,
                            (float) (this.getX() + (this.getWidth() - var11.getWidth(var12[0])) / 2),
                            (float) (this.getY() + this.getWidth() - 2 + 13),
                            var12[0],
                            ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
                    );
                } else {
                    RenderUtils.drawString(
                            var11,
                            (float) (this.getX() + (this.getWidth() - var11.getWidth(var12[0])) / 2),
                            (float) (this.getY() + this.getWidth() - 2 + 6),
                            var12[0],
                            ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
                    );
                }

                ScissorUtils.restoreScissor();
            }
        }
    }
}
