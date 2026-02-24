package io.github.sst.remake.gui.screen.altmanager;

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
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.util.image.BufferedImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

public class BanEntry extends Widget {
    public Texture serverIconTexture = null;
    public Texture serverBanner = null;

    public AccountBan ban;
    public ServerInfo info;

    private final AnimationUtils hoverPulseAnim = new AnimationUtils(200, 200, AnimationUtils.Direction.FORWARDS);

    public BanEntry(GuiComponent parent, String text, int x, int y, int width, int height, AccountBan ban) {
        super(parent, text, x, y, width, height, false);
        this.ban = ban;
        this.info = ban.getServer();
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        float var4 = EasingFunctions.easeOutBack(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        float var5 = QuadraticEasing.easeInQuad(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.isHoveredInHierarchy()) {
            this.hoverPulseAnim.changeDirection(AnimationUtils.Direction.BACKWARDS);
        } else if ((double) Math.abs(var4 - var5) < 0.7) {
            this.hoverPulseAnim.changeDirection(AnimationUtils.Direction.FORWARDS);
        }

        if (this.getAbsoluteY() + this.getTranslateY() < MinecraftClient.getInstance().getWindow().getHeight() - 36 && this.getAbsoluteY() + this.getTranslateY() > 52) {
            if (this.info != null && this.serverBanner == null) {
                try {
                    BufferedImage var6 = decodeBase64Image(this.info.getIcon());
                    if (var6 != null) {
                        this.serverIconTexture = BufferedImageUtil.getTexture("servericon", var6);
                        this.serverBanner = BufferedImageUtil.getTexture(
                                "servericon", ImageUtils.applyBlur(ImageUtils.adjustImageHSB(scaleImage(var6, 2.5, 2.5), 0.0F, 1.1F, 0.0F), 25)
                        );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
            int var9 = this.width / 2;
            int var7 = this.height / 2;
            if (this.hoverPulseAnim.getDirection() == AnimationUtils.Direction.FORWARDS) {
                var4 = QuadraticEasing.easeInQuad(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
            }

            GL11.glTranslatef((float) (this.getX() + var9), (float) (this.getY() + var7), 0.0F);
            GL11.glScaled(1.0 + 0.4 * (double) var4, 1.0 + 0.4 * (double) var4, 0.0);
            GL11.glTranslatef((float) (-this.getX() - var9), (float) (-this.getY() - var7), 0.0F);
            if (this.serverBanner != null) {
                RenderUtils.drawImage(
                        (float) this.x,
                        (float) (this.y - (this.width - this.height) / 2),
                        (float) this.width,
                        (float) this.width,
                        this.serverBanner,
                        ColorHelper.applyAlpha(ColorHelper.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 0.7F), 0.8F)
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
            if (this.info != null) {
                this.drawServerIcon();
                this.drawBanInfo();
                super.draw(partialTicks);
            }
        }
    }

    public void drawServerIcon() {
        GL11.glPushMatrix();
        float var5 = EasingFunctions.easeOutBack(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.hoverPulseAnim.getDirection() == AnimationUtils.Direction.FORWARDS) {
            var5 = QuadraticEasing.easeInQuad(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        GL11.glTranslatef((float) (this.getX() + 44), (float) (this.getY() + 44), 0.0F);
        GL11.glScaled(1.0 + 0.1 * (double) var5, 1.0 + 0.1 * (double) var5, 0.0);
        GL11.glTranslatef((float) (-this.getX() - 44), (float) (-this.getY() - 44), 0.0F);
        if (this.serverIconTexture == null) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("textures/misc/unknown_server.png"));
            RenderUtils.drawTexturedQuad(
                    (float) (this.x + 12), (float) (this.y + 12), 64.0F, 64.0F, ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.0F, 0.0F, 64.0F, 64.0F
            );
        } else {
            RenderUtils.drawImage(
                    (float) (this.x + 12), (float) (this.y + 12), 64.0F, 64.0F, this.serverIconTexture, ClientColors.LIGHT_GREYISH_BLUE.getColor(), true
            );
        }

        GL11.glPopMatrix();
    }

    public void drawBanInfo() {
        long var3 = this.ban.date.getTime() - new Date().getTime();
        int var5 = (int) (var3 / 1000L) % 60;
        int var6 = (int) (var3 / 60000L % 60L);
        int var7 = (int) (var3 / 3600000L % 24L);
        int var8 = (int) (var3 / 86400000L);
        ScissorUtils.startScissorNoGL(
                this.getAbsoluteX() + this.getTranslateX(),
                this.getAbsoluteY() + this.getTranslateY(),
                this.getAbsoluteX() + this.getTranslateX() + this.width,
                this.getAbsoluteY() + this.getTranslateY() + this.height
        );
        GL11.glPushMatrix();
        float var11 = EasingFunctions.easeOutBack(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.hoverPulseAnim.getDirection() == AnimationUtils.Direction.FORWARDS) {
            var11 = QuadraticEasing.easeInQuad(this.hoverPulseAnim.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        GL11.glTranslatef((float) (this.getX() + 76), (float) (this.getY() + 44), 0.0F);
        GL11.glScaled(1.0 - 0.1 * (double) var11, 1.0 - 0.1 * (double) var11, 0.0);
        GL11.glTranslatef((float) (-this.getX() - 76), (float) (-this.getY() - 44), 0.0F);
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_25,
                (float) (this.x + 94),
                (float) (this.y + 16),
                !this.info.name.equals("Minecraft Server")
                        ? this.info.name
                        : this.info.address.substring(0, 1).toUpperCase() + this.info.address.substring(1),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.9F)
        );
        int var12 = 94;
        int var13 = 46;
        if (this.ban.date.getTime() != 9223372036854775806L) {
            if (var3 > 0L && this.ban.date.getTime() != Long.MAX_VALUE) {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_18,
                        (float) (this.x + var12),
                        (float) (this.y + var13),
                        "Unban: " + var8 + " days, " + var7 + "h " + var6 + "m " + var5 + "s",
                        ColorHelper.shiftTowardsOther(ClientColors.DEEP_TEAL.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.2F)
                );
            } else if (this.ban.date.getTime() != Long.MAX_VALUE) {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_18,
                        (float) (this.x + var12),
                        (float) (this.y + var13),
                        "Unbanned!",
                        ColorHelper.shiftTowardsOther(ClientColors.DARK_SLATE_GREY.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F)
                );
            } else {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_18,
                        (float) (this.x + var12),
                        (float) (this.y + var13),
                        "Permanently banned!",
                        ColorHelper.shiftTowardsOther(ClientColors.PALE_YELLOW.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F)
                );
            }
        } else {
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_18,
                    (float) (this.x + var12),
                    (float) (this.y + var13),
                    "Compromised ban (unbannable)!",
                    ColorHelper.shiftTowardsOther(ClientColors.DARK_OLIVE.getColor(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3F)
            );
        }

        GL11.glPopMatrix();
        ScissorUtils.restoreScissor();
    }

    public static BufferedImage decodeBase64Image(String input) {
        if (input == null) {
            return null;
        } else if (!Base64.isBase64(input)) {
            return null;
        } else {
            try {
                return ImageIO.read(new ByteArrayInputStream(Base64.decodeBase64(input)));
            } catch (IOException var4) {
                return null;
            }
        }
    }

    private static BufferedImage scaleImage(BufferedImage img, double width, double height) {
        BufferedImage out = null;
        if (img != null) {
            int var8 = (int) ((double) img.getHeight() * height);
            int var9 = (int) ((double) img.getWidth() * width);
            out = new BufferedImage(var9, var8, img.getType());
            Graphics2D var10 = out.createGraphics();
            AffineTransform var11 = AffineTransform.getScaleInstance(width, height);
            var10.drawRenderedImage(img, var11);
        }

        return out;
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
