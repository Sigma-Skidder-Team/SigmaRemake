package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import io.github.sst.remake.util.render.shader.StencilUtils;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.texture.Texture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapZoomControl extends InteractiveWidget {
    public int blurredBackgroundAverageColor;
    public List<ZoomRipple> zoomRippleAnimations = new ArrayList<>();
    public int clickRepeatCooldownTicks = 0;
    public boolean shouldRecaptureBackground = true;
    private Texture blurredBackgroundTexture;
    private String blurredBackgroundTextureId;

    public MapZoomControl(GuiComponent parent, String name, int x, int y, int width, int height) {
        super(parent, name, x, y, width, height, false);
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        if (this.isMouseDownOverComponent && this.clickRepeatCooldownTicks <= 0) {
            boolean zoomIn = mouseY < this.getAbsoluteY() + this.getHeight() / 2;
            ((WorldMapView) this.parent).zoom(zoomIn);
            this.zoomRippleAnimations.add(new ZoomRipple(this, zoomIn));

            if (this.clickRepeatCooldownTicks != 0) {
                this.clickRepeatCooldownTicks = 14;
            } else {
                this.clickRepeatCooldownTicks = 3;
            }
        }

        this.clickRepeatCooldownTicks--;
        if (!this.isMouseDownOverComponent) {
            this.clickRepeatCooldownTicks = -1;
        }
    }

    @Override
    public void draw(float partialTicks) {
        Iterator<ZoomRipple> rippleIterator = this.zoomRippleAnimations.iterator();

        try {
            if (this.shouldRecaptureBackground) {
                BufferedImage captured = ImageUtils.captureRegionImage(
                        this.getAbsoluteX(),
                        this.getAbsoluteY(),
                        this.width,
                        this.height,
                        3,
                        10,
                        true
                );

                this.blurredBackgroundAverageColor = ColorHelper
                        .calculateAverageColor(new Color(captured.getRGB(6, 7)), new Color(captured.getRGB(6, 22)))
                        .getRGB();
                this.blurredBackgroundAverageColor = ColorHelper.shiftTowardsBlack(this.blurredBackgroundAverageColor, 0.25F);

                if (this.blurredBackgroundTexture != null) {
                    this.blurredBackgroundTexture.release();
                }

                this.blurredBackgroundTexture = ImageUtils.createTexture(this.getBlurredBackgroundTextureId(), captured);
                this.shouldRecaptureBackground = false;
            }

            if (this.blurredBackgroundTexture != null) {
                float inset = 8.0F;

                RenderUtils.drawRoundedRect(
                        (float) (this.x + inset),
                        (float) (this.y + inset),
                        (float) (this.width - inset * 2.0F),
                        (float) (this.height - inset * 2.0F),
                        20.0F,
                        partialTicks * 0.5F
                );
                RenderUtils.drawRoundedRect(
                        (float) (this.x + inset),
                        (float) (this.y + inset),
                        (float) (this.width - inset * 2.0F),
                        (float) (this.height - inset * 2.0F),
                        14.0F,
                        partialTicks
                );

                GL11.glPushMatrix();
                StencilUtils.beginStencilWrite();

                RenderUtils.drawRoundedButton(
                        (float) this.x,
                        (float) this.y,
                        (float) this.width,
                        (float) this.height,
                        8.0F,
                        ClientColors.LIGHT_GREYISH_BLUE.getColor()
                );

                StencilUtils.beginStencilRead();

                RenderUtils.drawTexture(
                        (float) (this.x - 1),
                        (float) (this.y - 1),
                        (float) (this.width + 2),
                        (float) (this.height + 2),
                        this.blurredBackgroundTexture,
                        ClientColors.LIGHT_GREYISH_BLUE.getColor()
                );

                while (rippleIterator.hasNext()) {
                    ZoomRipple ripple = rippleIterator.next();

                    int halfHeight = this.height / 2;
                    int halfWidth = this.width / 2;

                    int regionY = this.y + (ripple.zoomIn ? 0 : halfHeight);
                    ScissorUtils.startScissor(this.x, regionY, this.x + this.width, regionY + halfHeight, true);

                    float radius = (float) (halfWidth * 2 - 4) * ripple.progress + 4.0F;
                    float alphaFactor = (1.0F - ripple.progress * (0.5F + ripple.progress * 0.5F)) * 0.4F;

                    RenderUtils.drawFilledArc(
                            (float) (this.x + halfWidth),
                            (float) (regionY + this.height / 4),
                            radius,
                            ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alphaFactor)
                    );

                    ScissorUtils.restoreScissor();

                    ripple.progress = Math.min(ripple.progress + 3.0F / (float) MinecraftClient.currentFps, 1.0F);
                    if (ripple.progress == 1.0F) {
                        rippleIterator.remove();
                    }
                }

                StencilUtils.endStencil();

                RenderUtils.drawRoundedRect(
                        (float) this.x,
                        (float) this.y,
                        (float) this.width,
                        (float) this.height,
                        6.0F,
                        ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F)
                );

                GL11.glPopMatrix();

                RenderUtils.drawString(
                        FontUtils.HELVETICA_MEDIUM_20,
                        (float) (this.x + 14),
                        (float) (this.y + 8),
                        "+",
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F)
                );

                RenderUtils.drawRoundedRect2(
                        (float) (this.x + 16),
                        (float) (this.y + 65),
                        8.0F,
                        2.0F,
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F)
                );
            }
        } catch (IOException e) {
            Client.LOGGER.error("Failed to draw zoom", e);
        }

        super.draw(partialTicks);
    }

    private String getBlurredBackgroundTextureId() {
        if (this.blurredBackgroundTextureId == null) {
            this.blurredBackgroundTextureId = "zoom-blur-" + System.nanoTime();
        }
        return this.blurredBackgroundTextureId;
    }
}