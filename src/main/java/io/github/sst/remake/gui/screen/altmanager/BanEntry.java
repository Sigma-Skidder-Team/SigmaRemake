package io.github.sst.remake.gui.screen.altmanager;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.alt.AccountBan;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.texture.Texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

public class BanEntry extends Widget {
    public Texture serverIconTexture = null;
    public Texture serverBannerTexture = null;

    public AccountBan ban;
    public ServerInfo serverInfo;

    private final AnimationUtils hoverPulseAnim = new AnimationUtils(200, 200, AnimationUtils.Direction.FORWARDS);

    public BanEntry(GuiComponent parent, String text, int x, int y, int width, int height, AccountBan ban) {
        super(parent, text, x, y, width, height, false);
        this.ban = ban;
        this.serverInfo = ban.getServer();
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();

        float hoverBackEase = EasingFunctions.easeOutBack(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        float hoverQuadEase = QuadraticEasing.easeInQuad(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);

        if (this.isHoveredInHierarchy()) {
            this.hoverPulseAnim.changeDirection(AnimationUtils.Direction.BACKWARDS);
        } else if ((double) Math.abs(hoverBackEase - hoverQuadEase) < 0.7) {
            this.hoverPulseAnim.changeDirection(AnimationUtils.Direction.FORWARDS);
        }

        // Only render heavy stuff if on-screen
        if (this.getAbsoluteY() + this.getTranslateY() < MinecraftClient.getInstance().getWindow().getHeight() - 36
                && this.getAbsoluteY() + this.getTranslateY() > 52) {

            // Lazy-load server icon + blurred banner
            if (this.serverInfo != null && this.serverBannerTexture == null) {
                try {
                    BufferedImage decodedIcon = ImageUtils.decodeBase64Image(this.serverInfo.getIcon());
                    if (decodedIcon != null) {
                        this.serverIconTexture = ImageUtils.createTexture("servericon", decodedIcon);
                        this.serverBannerTexture = ImageUtils.createTexture(
                                "servericon",
                                ImageUtils.applyBlur(
                                        ImageUtils.adjustImageHSB(ImageUtils.scaleSquareImage(decodedIcon, 2.5, 2.5), 0.0F, 1.1F, 0.0F),
                                        25
                                )
                        );
                    }
                } catch (IOException e) {
                    Client.LOGGER.warn("Failed to lazy-load server icon & banner", e);
                }
            }

            ScissorUtils.startScissor(this);

            RenderUtils.drawRoundedRect(
                    (float) this.x,
                    (float) this.y,
                    (float) (this.x + this.width),
                    (float) (this.y + this.height),
                    ClientColors.LIGHT_GREYISH_BLUE.getColor()
            );

            GL11.glTexParameteri(3553, 10241, 9728);

            GL11.glPushMatrix();
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            if (this.hoverPulseAnim.getDirection() == AnimationUtils.Direction.FORWARDS) {
                hoverBackEase = QuadraticEasing.easeInQuad(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
            }

            GL11.glTranslatef((float) (this.getX() + centerX), (float) (this.getY() + centerY), 0.0F);
            GL11.glScaled(1.0 + 0.4 * (double) hoverBackEase, 1.0 + 0.4 * (double) hoverBackEase, 0.0);
            GL11.glTranslatef((float) (-this.getX() - centerX), (float) (-this.getY() - centerY), 0.0F);

            if (this.serverBannerTexture != null) {
                RenderUtils.drawImage(
                        (float) this.x,
                        (float) (this.y - (this.width - this.height) / 2),
                        (float) this.width,
                        (float) this.width,
                        this.serverBannerTexture,
                        ColorHelper.applyAlpha(
                                ColorHelper.shiftTowardsOther(
                                        ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                                        ClientColors.DEEP_TEAL.getColor(),
                                        0.7F
                                ),
                                0.8F
                        )
                );
            }

            GL11.glPopMatrix();
            ScissorUtils.restoreScissor();

            RenderUtils.drawRoundedRect(
                    (float) this.x,
                    (float) this.y,
                    (float) (this.x + this.width),
                    (float) (this.y + this.height),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + 0.3F * this.hoverPulseAnim.calcPercent())
            );
        }

        if (this.ban != null) {
            drawServerIcon();
            drawBanInfo();
            super.draw(partialTicks);
        }
    }

    private void drawServerIcon() {
        GL11.glPushMatrix();

        float iconScaleEase = EasingFunctions.easeOutBack(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.hoverPulseAnim.getDirection() == AnimationUtils.Direction.FORWARDS) {
            iconScaleEase = QuadraticEasing.easeInQuad(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        GL11.glTranslatef((float) (this.getX() + 44), (float) (this.getY() + 44), 0.0F);
        GL11.glScaled(1.0 + 0.1 * (double) iconScaleEase, 1.0 + 0.1 * (double) iconScaleEase, 0.0);
        GL11.glTranslatef((float) (-this.getX() - 44), (float) (-this.getY() - 44), 0.0F);

        if (this.serverIconTexture == null) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("textures/misc/unknown_server.png"));
            RenderUtils.drawTexturedQuad(
                    (float) (this.x + 12),
                    (float) (this.y + 12),
                    64.0F,
                    64.0F,
                    ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                    0.0F,
                    0.0F,
                    64.0F,
                    64.0F
            );
        } else {
            RenderUtils.drawImage(
                    (float) (this.x + 12),
                    (float) (this.y + 12),
                    64.0F,
                    64.0F,
                    this.serverIconTexture,
                    ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                    true
            );
        }

        GL11.glPopMatrix();
    }

    private void drawBanInfo() {
        ScissorUtils.startScissorNoGL(
                this.getAbsoluteX() + this.getTranslateX(),
                this.getAbsoluteY() + this.getTranslateY(),
                this.getAbsoluteX() + this.getTranslateX() + this.width,
                this.getAbsoluteY() + this.getTranslateY() + this.height
        );

        GL11.glPushMatrix();

        float infoScaleEase = EasingFunctions.easeOutBack(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.hoverPulseAnim.getDirection() == AnimationUtils.Direction.FORWARDS) {
            infoScaleEase = QuadraticEasing.easeInQuad(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        GL11.glTranslatef((float) (this.getX() + 76), (float) (this.getY() + 44), 0.0F);
        GL11.glScaled(1.0 - 0.1 * (double) infoScaleEase, 1.0 - 0.1 * (double) infoScaleEase, 0.0);
        GL11.glTranslatef((float) (-this.getX() - 76), (float) (-this.getY() - 44), 0.0F);

        String displayName;
        if (this.serverInfo != null) {
            displayName = !this.serverInfo.name.equals("Minecraft Server")
                    ? this.serverInfo.name
                    : this.serverInfo.address.substring(0, 1).toUpperCase() + this.serverInfo.address.substring(1);
        } else {
            displayName = this.ban.address;
        }

        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_25,
                (float) (this.x + 94),
                (float) (this.y + 16),
                displayName,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.9F)
        );

        int textX = 94;
        int textY = 46;

        long banTime = this.ban.date.getTime();
        if (banTime != 9223372036854775806L) {
            if (banTime >= Long.MAX_VALUE - 1000L) {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_18,
                        (float) (this.x + textX),
                        (float) (this.y + textY),
                        "Permanently banned!",
                        ColorHelper.shiftTowardsOther(ClientColors.PALE_YELLOW.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F)
                );
            } else {
                long millisUntilUnban = banTime - new Date().getTime();

                long seconds = (millisUntilUnban / 1000L) % 60;
                long minutes = (millisUntilUnban / 60000L % 60L);
                long hours = (millisUntilUnban / 3600000L % 24L);
                long days = (millisUntilUnban / 86400000L);

                if (millisUntilUnban > 0L) {
                    RenderUtils.drawString(
                            FontUtils.HELVETICA_LIGHT_18,
                            (float) (this.x + textX),
                            (float) (this.y + textY),
                            "Unban: " + days + " days, " + hours + "h " + minutes + "m " + seconds + "s",
                            ColorHelper.shiftTowardsOther(ClientColors.DEEP_TEAL.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.2F)
                    );
                } else {
                    RenderUtils.drawString(
                            FontUtils.HELVETICA_LIGHT_18,
                            (float) (this.x + textX),
                            (float) (this.y + textY),
                            "Unbanned!",
                            ColorHelper.shiftTowardsOther(ClientColors.DARK_SLATE_GREY.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F)
                    );
                }
            }
        } else {
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_18,
                    (float) (this.x + textX),
                    (float) (this.y + textY),
                    "Compromised ban (unbannable)!",
                    ColorHelper.shiftTowardsOther(ClientColors.DARK_OLIVE.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F)
            );
        }

        GL11.glPopMatrix();
        ScissorUtils.restoreScissor();
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
