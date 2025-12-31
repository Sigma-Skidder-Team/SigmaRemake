package io.github.sst.remake.gui.element.impl.alts;

import io.github.sst.remake.alt.AccountBan;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

public class BanElement extends AnimatedIconPanel {
    public AccountBan ban = null;
    public ServerInfo info = null;
    public Texture servericon = null;
    public Texture serverBanner = null;
    private BufferedImage field21247;
    private final AnimationUtils field21248;

    public BanElement(CustomGuiScreen parent, String text, int x, int y, int width, int height, AccountBan ban) {
        super(parent, text, x, y, width, height, false);
        this.ban = ban;
        this.info = ban.getServer();
        this.field21248 = new AnimationUtils(200, 200, AnimationUtils.Direction.BACKWARDS);
    }

    @Override
    public void draw(float partialTicks) {
        this.method13225();
        float var4 = EasingFunctions.easeOutBack(this.field21248.calcPercent(), 0.0F, 1.0F, 1.0F);
        float var5 = QuadraticEasing.easeInQuad(this.field21248.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.method13298()) {
            this.field21248.changeDirection(AnimationUtils.Direction.FORWARDS);
        } else if ((double) Math.abs(var4 - var5) < 0.7) {
            this.field21248.changeDirection(AnimationUtils.Direction.BACKWARDS);
        }

        if (this.method13272() + this.method13282() < MinecraftClient.getInstance().getWindow().getHeight() - 36 && this.method13272() + this.method13282() > 52) {
            if (this.info != null && this.serverBanner == null) {
                try {
                    BufferedImage var6 = method13578(this.info.getIcon());
                    if (var6 != null) {
                        this.servericon = BufferedImageUtil.getTexture("servericon", var6);
                        this.serverBanner = BufferedImageUtil.getTexture(
                                "servericon", ImageUtils.applyBlur(ImageUtils.adjustImageHSB(method13579(var6, 2.5, 2.5), 0.0F, 1.1F, 0.0F), 25)
                        );
                    }
                } catch (IOException var8) {
                    var8.printStackTrace();
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
            if (this.field21248.getDirection() == AnimationUtils.Direction.BACKWARDS) {
                var4 = QuadraticEasing.easeInQuad(this.field21248.calcPercent(), 0.0F, 1.0F, 1.0F);
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
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + 0.3F * this.field21248.calcPercent())
            );
        }

        if (this.ban != null) {
            if (this.info != null) {
                this.method13576();
                this.method13577();
                Resources.shoutIconPNG.bind();
                Resources.shoutIconPNG.bind();
                super.draw(partialTicks);
            }
        }
    }

    public void method13576() {
        GL11.glPushMatrix();
        float var5 = EasingFunctions.easeOutBack(this.field21248.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.field21248.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            var5 = QuadraticEasing.easeInQuad(this.field21248.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        GL11.glTranslatef((float) (this.getX() + 44), (float) (this.getY() + 44), 0.0F);
        GL11.glScaled(1.0 + 0.1 * (double) var5, 1.0 + 0.1 * (double) var5, 0.0);
        GL11.glTranslatef((float) (-this.getX() - 44), (float) (-this.getY() - 44), 0.0F);
        if (this.servericon == null) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("textures/misc/unknown_server.png"));
            RenderUtils.drawTexturedQuad(
                    (float) (this.x + 12), (float) (this.y + 12), 64.0F, 64.0F, ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.0F, 0.0F, 64.0F, 64.0F
            );
        } else {
            RenderUtils.drawImage(
                    (float) (this.x + 12), (float) (this.y + 12), 64.0F, 64.0F, this.servericon, ClientColors.LIGHT_GREYISH_BLUE.getColor(), true
            );
        }

        GL11.glPopMatrix();
        Resources.shoutIconPNG.bind();
        Resources.shoutIconPNG.bind();
    }

    public void method13577() {
        long var3 = this.ban.date.getTime() - new Date().getTime();
        int var5 = (int) (var3 / 1000L) % 60;
        int var6 = (int) (var3 / 60000L % 60L);
        int var7 = (int) (var3 / 3600000L % 24L);
        int var8 = (int) (var3 / 86400000L);
        ScissorUtils.startScissor(
                this.method13271() + this.method13280(),
                this.method13272() + this.method13282(),
                this.method13271() + this.method13280() + this.width,
                this.method13272() + this.method13282() + this.height
        );
        GL11.glPushMatrix();
        float var11 = EasingFunctions.easeOutBack(this.field21248.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.field21248.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            var11 = QuadraticEasing.easeInQuad(this.field21248.calcPercent(), 0.0F, 1.0F, 1.0F);
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

    public static BufferedImage method13578(String var0) {
        if (var0 == null) {
            return null;
        } else if (!Base64.isBase64(var0)) {
            return null;
        } else {
            try {
                return ImageIO.read(new ByteArrayInputStream(Base64.decodeBase64(var0)));
            } catch (IOException var4) {
                return null;
            }
        }
    }

    private static BufferedImage method13579(BufferedImage var0, double var1, double var3) {
        BufferedImage var7 = null;
        if (var0 != null) {
            int var8 = (int) ((double) var0.getHeight() * var3);
            int var9 = (int) ((double) var0.getWidth() * var1);
            var7 = new BufferedImage(var9, var8, var0.getType());
            Graphics2D var10 = var7.createGraphics();
            AffineTransform var11 = AffineTransform.getScaleInstance(var1, var3);
            var10.drawRenderedImage(var0, var11);
        }

        return var7;
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        return false;
    }
}
