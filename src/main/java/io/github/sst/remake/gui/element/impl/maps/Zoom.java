package io.github.sst.remake.gui.element.impl.maps;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.element.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.StencilUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.util.image.BufferedImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Zoom extends InteractiveWidget {
    public int averageBackgroundColor;
    public List<ZoomParticle> zoomParticles = new ArrayList<>();
    public int zoomCooldown = 0;
    public boolean needsRedraw = true;
    private Texture backgroundTexture;

    public Zoom(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.isMouseDownOverComponent && this.zoomCooldown <= 0) {
            if (mouseY >= this.getAbsoluteY() + this.getHeight() / 2) {
                ((MapFrame) this.parent).zoom(false);
                this.zoomParticles.add(new ZoomParticle(this, false));
            } else {
                ((MapFrame) this.parent).zoom(true);
                this.zoomParticles.add(new ZoomParticle(this, true));
            }

            if (this.zoomCooldown != 0) {
                this.zoomCooldown = 14;
            } else {
                this.zoomCooldown = 3;
            }
        }

        this.zoomCooldown--;
        if (!this.isMouseDownOverComponent) {
            this.zoomCooldown = -1;
        }
    }

    @Override
    public void draw(float partialTicks) {
        Iterator<ZoomParticle> var4 = this.zoomParticles.iterator();

        try {
            if (this.needsRedraw) {
                BufferedImage var6 = ImageUtils.captureRegionImage(this.getAbsoluteX(), this.getAbsoluteY(), this.width, this.height, 3, 10, true);
                this.averageBackgroundColor = ColorHelper.calculateAverageColor(new Color(var6.getRGB(6, 7)), new Color(var6.getRGB(6, 22))).getRGB();
                this.averageBackgroundColor = ColorHelper.shiftTowardsBlack(this.averageBackgroundColor, 0.25F);
                if (this.backgroundTexture != null) {
                    this.backgroundTexture.release();
                }

                this.backgroundTexture = BufferedImageUtil.getTexture("blur", var6);
                this.needsRedraw = false;
            }

            if (this.backgroundTexture != null) {
                RenderUtils.drawRoundedRect(
                        (float) (this.x + 8),
                        (float) (this.y + 8),
                        (float) (this.width - 8 * 2),
                        (float) (this.height - 8 * 2),
                        20.0F,
                        partialTicks * 0.5F
                );
                RenderUtils.drawRoundedRect(
                        (float) (this.x + 8),
                        (float) (this.y + 8),
                        (float) (this.width - 8 * 2),
                        (float) (this.height - 8 * 2),
                        14.0F,
                        partialTicks
                );
                GL11.glPushMatrix();
                StencilUtils.beginStencilWrite();
                RenderUtils.drawRoundedButton(
                        (float) this.x, (float) this.y, (float) this.width, (float) this.height, 8.0F, ClientColors.LIGHT_GREYISH_BLUE.getColor()
                );
                StencilUtils.beginStencilRead();
                RenderUtils.drawTexture(
                        (float) (this.x - 1),
                        (float) (this.y - 1),
                        (float) (this.width + 2),
                        (float) (this.height + 2),
                        this.backgroundTexture,
                        ClientColors.LIGHT_GREYISH_BLUE.getColor()
                );

                while (var4.hasNext()) {
                    ZoomParticle var11 = var4.next();
                    int var7 = this.height / 2;
                    int var8 = this.y + (var11.isZoomIn ? 0 : var7);
                    int var9 = this.width / 2;
                    ScissorUtils.startScissor(this.x, var8, this.x + this.width, var8 + var7, true);
                    RenderUtils.drawFilledArc(
                            (float) (this.x + var9),
                            (float) (var8 + this.height / 4),
                            (float) (var9 * 2 - 4) * var11.animationProgress + 4.0F,
                            ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), (1.0F - var11.animationProgress * (0.5F + var11.animationProgress * 0.5F)) * 0.4F)
                    );
                    ScissorUtils.restoreScissor();
                    var11.animationProgress = Math.min(var11.animationProgress + 3.0F / (float) MinecraftClient.currentFps, 1.0F);
                    if (var11.animationProgress == 1.0F) {
                        var4.remove();
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
                        (float) (this.x + 16), (float) (this.y + 65), 8.0F, 2.0F, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F)
                );
            }
        } catch (IOException ignored) {
        }

        super.draw(partialTicks);
    }

    public static class ZoomParticle {
        public float animationProgress;
        public boolean isZoomIn;
        public final Zoom parentZoom;

        public ZoomParticle(Zoom parent, boolean isZoomIn) {
            this.parentZoom = parent;
            this.animationProgress = 0.0F;
            this.isZoomIn = isZoomIn;
        }
    }
}

